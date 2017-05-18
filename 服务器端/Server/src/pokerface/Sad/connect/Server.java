package pokerface.Sad.connect;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import pokerface.Sad.util.Util;


public class Server extends ServerSocket{
	
	final static String heatbeatMsg = "heartbeating";
	Socket pcClient = null;  //PC端Client
	Socket webClient = null; //web端Client
	boolean pcConnectState = false;
	
	public static void main(String[] args) throws IOException {
		Server s = new Server();
		System.out.println("wait for Web part...");
		s.acceptWeb();//等待Web连接
		//服务正常启动
		System.out.println("JARVIS Service Start Up Normally......");
		Thread webMonitorThread = new Thread(new WebOrderMonitor(s));
		webMonitorThread.start();
		s.acceptPC();//等待PC连接
		Thread clientMonitorThead = new Thread(new PCStateMonitor(s));
		clientMonitorThead.start();
		while(true)
		{
			//if((!webMonitorThread.isAlive())&&(!clientMonitorThead.isAlive()))
			if(!clientMonitorThead.isAlive())
			{
				s.acceptPC(); //等待两线程终止则PC端已断开连接，等待PC再次连接
				//webMonitorThread = new Thread(new WebOrderMonitor(s));
				clientMonitorThead = new Thread(new PCStateMonitor(s));
//				webMonitorThread.start();
				clientMonitorThead.start();
			}
		}
		
	}
	public Server() throws IOException {
		//从配置文件中读取端口，并创建Server对象
		super(new Integer(Util.getProperties().getProperty("serverPort")));
	}
	//等待PC连接
	public void acceptPC() throws IOException{
		System.out.println("wait for PC connect......");
		pcClient = this.accept();
		System.out.println("PC :"+pcClient.getInetAddress()+" connect");
		pcConnectState = true;
	}
	//等待Web应用连接
	public void acceptWeb() throws IOException{
		webClient = this.accept();
		System.out.println("Web :"+webClient.getInetAddress()+" connect");
	}
	//向PC端发送命令
	public void sendMsgToClient(Socket client,String msg) throws IOException{
		OutputStream os = client.getOutputStream();
		os.write(msg.getBytes());
		os.flush();
		//os.close(); socket流不能关闭
	}
	//从PC端接收结果信息
	public String getMsgFromClient(Socket client){
		InputStream is = null;
		byte[] buf = null;
		int Len = 0;
		try {
			is = client.getInputStream();
			buf = new byte[1024];
			Len = is.read(buf);
		} catch (IOException e) {
			e.printStackTrace();
		}

		String result = null;
		try {
			result = new String(buf,0,Len,"GBK");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	
		return result;
	}
	
	////从web应用处接收命令,阻塞等待指令直至PC端断开为止
	//与Web保持长连接
	public String receiveOrder() throws IOException{
//		this.webClient.setSoTimeout(5000);//设置超时策略，防止阻塞于read方法中
		InputStream is = this.webClient.getInputStream();
		byte[] buf = new byte[1024];
		Integer len = null;
		String msg = null;
		
		if((len=is.read(buf))!=-1)
		{
			msg = new String(buf, 0, len);
			return msg;
		}
		/*
		//若PC端仍连接，则一直等待Web端传来指令
		//pcConnectState由ClientMonitor线程维护
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
		*/
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
		this.close();
	}
	public boolean isConnected(){
        try{
        	/*
        	 * 此发送紧急数据的方法在Windows环境下会出现异常
        	 * this.pcClient.sendUrgentData(0xff);
        	 * */
        	this.sendMsgToClient(this.pcClient,Server.heatbeatMsg);
        	return true;
        }catch(Exception e){
            return false;
        }
}
}

//等待Web命令线程
class WebOrderMonitor implements Runnable{
	Server server = null;
	public WebOrderMonitor(Server Server) {
		this.server = Server;
	}
	public void run() {
		try {
			
			String order = null;
			
			while(true)
			{
//				if(!server.isConnected())
//				{
//					//若PC端已断开则终止线程
//					return;
//				}
				
				//阻塞接收Web端指令直至PC端断开并返回null
				order = server.receiveOrder();
//				if(order==null) 
//				{
//					System.out.println("等待Web命令线程终止");
//					return; //若PC端已断开则终止线程
//				}
				//将接收到的命令转发给PC端
				String result = null;
				if(server.pcConnectState == true)
				{
					server.sendMsgToClient(server.pcClient,order);
					//接收PC端反馈的结果信息
					result = server.getMsgFromClient(server.pcClient);
				}else{
					result = "PC不在线";
				}
				//将结果信息转发给web端
				server.sendMsgToClient(server.webClient, result);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
//等待客户端关闭线程
class PCStateMonitor implements Runnable{

	Server Server = null;
	public PCStateMonitor(Server s) {
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
