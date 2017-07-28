package pokerface.Sad.connect;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import pokerface.Sad.service.Service;
import pokerface.Sad.service.ServiceImpl1;
import pokerface.Sad.util.Util;

public class RPiClient extends Socket{

	static Logger logger = null;
	static {
		PropertyConfigurator.configure("log4j.properties");
		logger = Logger.getLogger(RPiClient.class);
	}
	
	private String serverIP;
	private Integer rPiServerPort;
	public boolean keyBoardClosed = false;
	final static String heatbeatMsg = "heartbeating";
	static String closeOrder = "closeClient";
	
	
	public RPiClient() throws FileNotFoundException, IOException {
		super();
		logger.debug("创建RPiClient对象成功");
		Properties pro = Util.getProperties();
		this.serverIP = pro.getProperty("serverIP");
		this.rPiServerPort = new Integer(pro.getProperty("rPiServerPort"));
		this.connect(new InetSocketAddress(InetAddress.getByName(this.serverIP), this.rPiServerPort));
		logger.debug("RPiClient连接rPiServer成功");
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
				else
					logger.debug("接收到心跳信息");
			}
		}
	}
	
	//正则表达式判断是否为心跳信息
	public static boolean isHeartMsg(String msg){
		Pattern p = Pattern.compile(RPiClient.heatbeatMsg);
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
		RPiClient rPiClient = null;
		String order = null;
		
		Service service = new ServiceImpl1(); //通过Service的实现类来响应命令
		
		try {
			rPiClient = new RPiClient();
			if(rPiClient.isConnected())
			{
				logger.info("connected to Server successfully");
			}
			new Thread(new ReceiveInput(rPiClient)).start(); //接收键盘输入的关闭指令
			while(true)
			{
				//阻塞式方法
				order = rPiClient.receiveOrder();
				logger.info("接收到"+order+"指令");
				//执行指令并获取结果信息
				String result = service.execOrder(order);
				//将结果反馈给Server
				OutputStream os = rPiClient.getOutputStream();
				os.write(result.getBytes("GBK"));
				os.flush();
			}
		} catch (IOException e) {
			if (rPiClient.keyBoardClosed == true) {
				logger.info("连接关闭");
			}else{
				logger.error("连接异常",e);
			}
		}
	}

}
class ReceiveInput implements Runnable
{
	RPiClient client = null;
	public ReceiveInput(RPiClient client) {
		this.client = client;
	}
	@Override
	public void run() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			while(true)
			{
				if(RPiClient.closeOrder.equals(new String(br.readLine())))
				{
					this.client.close();
					this.client.keyBoardClosed = true;
					return;
				}	
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}