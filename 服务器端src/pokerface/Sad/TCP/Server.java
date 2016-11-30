package pokerface.Sad.TCP;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Server {
	static String heatbeatMsg = "heartbeating";
	ServerSocket server = null;
	Socket pcClient = null;
	Socket webClient = null;
	boolean pcConnectState = false;
	public static void main(String[] args) throws IOException {
		Server s = new Server();
		System.out.println("wait for Web part...");
		s.acceptWeb();//等待Web连接
		//服务正常启动
		System.out.println("JARVIS Service Start Up Normally......");
		s.acceptPC();//等待PC连接
		Thread webMonitorThread = new Thread(new WebMonitor(s));
		Thread clientMonitorThead = new Thread(new ClientMonitor(s));
		webMonitorThread.start();
		clientMonitorThead.start();
		while(true)
		{
			if((!webMonitorThread.isAlive())&&(!clientMonitorThead.isAlive()))
			{
				s.acceptPC(); //等待两线程终止则PC端已断开连接，等待PC再次连接
				webMonitorThread = new Thread(new WebMonitor(s));
				clientMonitorThead = new Thread(new ClientMonitor(s));
				webMonitorThread.start();
				clientMonitorThead.start();
			}
		}
		
	}
	public Server() throws IOException {
		server = new ServerSocket(10001);
	}
	//等待PC连接
	public void acceptPC() throws IOException{
		System.out.println("wait for PC connect......");
		pcClient = server.accept();
		System.out.println("PC :"+pcClient.getInetAddress()+" connect");
		pcConnectState = true;
	}
	//等待Web应用连接
	public void acceptWeb() throws IOException{
		webClient = server.accept();
		System.out.println("Web :"+webClient.getInetAddress()+" connect");
	}
	public void sendOrder(String msg) throws IOException{
		OutputStream os = this.pcClient.getOutputStream();
		os.write(msg.getBytes());
	}
	
	//从web应用处接收命令,阻塞等待指令直至PC端断开为止
	public String receiveOrder() throws IOException{
		this.webClient.setSoTimeout(5000);//设置超时策略，防止阻塞于read方法中
		InputStream is = this.webClient.getInputStream();
		byte[] buf = new byte[1024];
		Integer len = null;
		String msg = null;
		//若PC端仍连接，则一直等待Web端传来指令
		while(this.pcConnectState)
		{
			try {
				if((len=is.read(buf))!=-1)
				{
					msg = new String(buf, 0, len);
					return msg;
				}
			} catch (SocketTimeoutException e) {
				//每五秒跳出一次判断PC端是否还在线
			}
		}
		return null;
	}
	public void close(){
		if(this.pcClient!=null)
		{
			try {
				this.pcClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			this.server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public boolean isConnected(){
        try{
        	/*
        	 * 此发送紧急数据的方法在Windows环境下会出现异常
        	 * this.pcClient.sendUrgentData(0xff);
        	 * */
        	this.sendOrder(Server.heatbeatMsg);
        	return true;
        }catch(Exception e){
            return false;
        }
}
}
//等待Web命令线程
class WebMonitor implements Runnable{
	Server Server = null;
	public WebMonitor(Server Server) {
		this.Server = Server;
	}
	public void run() {
		try {
			
			String order = null;
			
			while(true)
			{
				if(!Server.isConnected())
				{
					//若PC端已断开则终止线程
					return;
				}
				
				//阻塞接收Web端指令直至PC端断开则返回null
				order = Server.receiveOrder();
				if(order==null) 
				{
					System.out.println("等待Web命令线程终止");
					return; //若PC端已断开则终止线程
				}
				Server.sendOrder(order);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
//等待客户端关闭线程
class ClientMonitor implements Runnable{

	Server Server = null;
	public ClientMonitor(Server s) {
		this.Server = s;
	}
	public void run() {

		while(Server.isConnected()){
			//检测PC端是否还在线
			try {
				Thread.currentThread().sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}	
		System.out.println("PC端已离线");
		this.Server.pcConnectState = false;
		System.out.println("等待客户端关闭线程终止");
	}
}
