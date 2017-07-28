package pokerface.Sad.connect;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import pokerface.Sad.util.Util;

public class RPiServer extends ServerSocket{
	static Logger logger = null;
	static {
		PropertyConfigurator.configure("log4j.properties");
		logger = Logger.getLogger(RPiServer.class);
	}
	final static String heatbeatMsg = "heartbeating";
	Socket rPiClient = null;  //rPi端Client
	boolean rPiConnectState = false;
	
	public RPiServer() throws IOException {
		//从配置文件中读取端口，并创建Server对象
		super(new Integer(Util.getProperties().getProperty("rPiServerPort")));
		logger.debug("创建RPiServer对象成功   Port:"+Util.getProperties().getProperty("rPiServerPort"));
	}
	
	//等待rPi连接
	public void acceptRPi() throws IOException{
		logger.info("wait for rPi connect......");
		rPiClient = this.accept();
		logger.info("rPi :"+rPiClient.getInetAddress()+" connect");
		rPiConnectState = true;
	}
	
	//向客户端端发送命令
	public void sendMsgToClient(Socket client,String msg) throws IOException{
		OutputStream os = client.getOutputStream();
		os.write(msg.getBytes("GBK"));
		os.flush();
		//os.close(); socket流不能关闭 
	}
	
	//从客户顿端接收结果信息
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
	//关闭客户端连接
	public void close(){
		if(this.rPiClient!=null)
		{
			try {
				this.rPiClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.close();
	}
	
	//向客户端发送
	public boolean isConnected(Socket client){
        try{
        	logger.debug("向rPi发送心跳信息");
        	this.sendMsgToClient(client,Server.heatbeatMsg);
        	logger.debug("向rPi发送心跳信息成功");
        	return true;
        }catch(Exception e){
        	logger.debug("向rPi发送心跳信息失败");
            return false;
        }
	}
}
