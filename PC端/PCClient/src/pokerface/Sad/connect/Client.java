package pokerface.Sad.connect;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import pokerface.Sad.service.Service;
import pokerface.Sad.service.ServiceImpl1;
import pokerface.Sad.util.Util;

public class Client extends Socket{
	String serverIP = null;
	Integer serverPort = null;
	static String closeOrder = "closeClient";
	final static String heatbeatMsg = "heartbeating";
	public Client() throws FileNotFoundException, IOException {
		super();
		Properties pro = Util.getProperties();
		this.serverIP = pro.getProperty("serverIP");
		this.serverPort = new Integer(pro.getProperty("serverPort"));
		this.connect(new InetSocketAddress(InetAddress.getByName(this.serverIP), this.serverPort));
	}
	
	//阻塞式方法
	public String receiveOrder() throws IOException{
		InputStream is = this.getInputStream();
		byte[] buf = new byte[1024];
		Integer len = null;
		String msg = null;
		while(true)
		{
			if((len=is.read(buf))!=-1)
			{
				msg = new String(buf, 0, len);
				//滤除心跳信息
				if(!isHeartMsg(msg))
					return msg;
			}
		}
	}
	//正则表达式判断是否为心跳信息
	public static boolean isHeartMsg(String msg){
		Pattern p = Pattern.compile(Client.heatbeatMsg);
		Matcher m = p.matcher(msg);
		if(m.find())
		{
			return true;
		}
		else{
			return false;
		}		
	}
	public static void main(String[] args) {
		Client c1 = null;
		String order = null;
		String cmdOrder = null;
		Properties pro = null;
		Class cls = null;
		Method method = null;
		
		Service service = new ServiceImpl1(); //通过Service的实现类来响应命令
		
		try {
				c1 = new Client();
				if(c1.isConnected())
				{
					System.out.println("connected to JARVIS successfully");
				}
				new Thread(new ReceiveInput(c1)).start();
				while(true)
				{
					//阻塞式方法
					order = c1.receiveOrder();
					//执行指令并获取结果信息
					String result = service.execOrder(order);
					//将结果反馈给Server
					OutputStream os = c1.getOutputStream();
					os.write(result.getBytes("GBK"));
					os.flush();
				}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch(SocketException e){
			return;
		}catch (IOException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}
}
class ReceiveInput implements Runnable
{
	Client client = null;
	public ReceiveInput(Client client) {
		this.client = client;
	}
	@Override
	public void run() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			while(true)
			{
				if(Client.closeOrder.equals(new String(br.readLine())))
				{
					this.client.close();
					return;
				}	
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}