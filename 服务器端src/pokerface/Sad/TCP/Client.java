package pokerface.Sad.TCP;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pokerface.Sad.util.Util;

public class Client {
	Socket client = null;
	String serverIP = null;
	Integer serverPort = null;
	static String closeOrder = "closeClient";
	public Client() throws FileNotFoundException, IOException {
		client = new Socket();
		Properties pro = Util.getProperties();
		this.serverIP = pro.getProperty("serverIP");
		this.serverPort = new Integer(pro.getProperty("serverPort"));
		client.connect(new InetSocketAddress(InetAddress.getByName(this.serverIP), this.serverPort));
	}
	
	//阻塞式方法
	public String receiveOrder() throws IOException{
		InputStream is = client.getInputStream();
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
		Pattern p = Pattern.compile(Server.heatbeatMsg);
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
		//反射机制加载工具类
		try {
			cls = Class.forName("pokerface.Sad.util.Util");
			pro = Util.getProperties();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
				c1 = new Client();
				if(c1.client.isConnected())
				{
					System.out.println("connected to JARVIS successfully");
				}
				new Thread(new ReceiveInput(c1)).start();
				while(true)
				{
					//阻塞式方法
					order = c1.receiveOrder();
					//先判断是否为关闭客户端指令
					if(order.equals(new String("closeClient")))
					{
						System.out.println("Client closed!");
						c1.client.close();
						return;
					}
					cmdOrder = pro.getProperty(order);
					if(cmdOrder!=null)
					{
						System.out.println("接收到"+order+"指令");
						System.out.println("执行"+cmdOrder+"命令");
						//反射机制调用方法
						cls.getDeclaredMethod(cmdOrder).invoke(cls);
						order=null;
						cmdOrder=null;
	//						MailUtil.sendOrderFinshMsg();
					}else{
						//没有对应命令
						System.out.println("接收到"+order+"指令");
						System.out.println("没有对应指令");
					}
				}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch(SocketException e){
			return;
		}catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
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
					this.client.client.close();
					return;
				}	
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}