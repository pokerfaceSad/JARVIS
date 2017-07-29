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

public class Server extends ServerSocket {

	static Logger logger = null;
	static {
		PropertyConfigurator.configure("log4j.properties");
		logger = Logger.getLogger(Server.class);
	}

	final static String heatbeatMsg = "heartbeating";
	final static String pcStateCheckOrder = "pcStateCheck";
	final static String rPiStateCheckOrder = "rPiStateCheck";
	Socket pcClient = null; // PC端Client
	Socket webClient = null; // web端Client
	RPiServer rPiServer = new RPiServer(); //rPiServer
	boolean pcConnectState = false;

	public static void main(String[] args) throws IOException {
		Server s = new Server();
		logger.info("wait for Web part...");
		s.acceptWeb();// 等待Web连接
		// 服务正常启动
		logger.info("JARVIS Service Start Up Normally......");
		Thread webMonitorThread = new Thread(new WebOrderMonitor(s));
		webMonitorThread.start();
		Thread rPiMonitorThread = new Thread(new RPiStateMonitor(s.rPiServer));
		rPiMonitorThread.start();
		Thread pcMonitorThead = new Thread(new PCStateMonitor(s));
		pcMonitorThead.start();

	}

	public Server() throws IOException {
		// 从配置文件中读取端口，并创建Server对象
		super(new Integer(Util.getProperties().getProperty("serverPort")));
		logger.debug("创建Server对象成功");
	}

	// 等待PC连接
	public void acceptPC() throws IOException {
		logger.info("wait for PC connect......");
		pcClient = this.accept();
		logger.info("PC :" + pcClient.getInetAddress() + " connect");
		pcConnectState = true;
	}

	// 等待Web应用连接
	public void acceptWeb() throws IOException {
		webClient = this.accept();
		System.out.println("Web :" + webClient.getInetAddress() + " connect");
	}

	// 向客户端端发送命令
	public void sendMsgToClient(Socket client, String msg) throws IOException {
		OutputStream os = client.getOutputStream();
		os.write(msg.getBytes("GBK"));
		os.flush();
		// os.close(); socket流不能关闭
	}

	// 从客户端端接收结果信息
	public String getMsgFromClient(Socket client) {
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
			result = new String(buf, 0, Len, "GBK");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return result;
	}

	// 与Web保持长连接
	public String receiveOrder() throws IOException {
		InputStream is = this.webClient.getInputStream();
		byte[] buf = new byte[1024];
		Integer len = null;
		String msg = null;

		if ((len = is.read(buf)) != -1) {
			msg = new String(buf, 0, len);
			return msg;
		}
		return null;
	}

	public void close() {
		if (this.pcClient != null) {
			try {
				this.pcClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.close();
	}

	public boolean isConnected(Socket client) {
		try {
			/*
			 * 此发送紧急数据的方法在Windows环境下会出现异常 this.pcClient.sendUrgentData(0xff);
			 */
			logger.debug("向PC端发送心跳信息");
			this.sendMsgToClient(client, Server.heatbeatMsg);
			logger.debug("向PC端发送心跳信息成功");
			return true;
		} catch (Exception e) {
			logger.debug("向PC端发送心跳信息失败");
			return false;
		}
	}
}

// 等待Web命令线程
class WebOrderMonitor implements Runnable {

	static Logger logger = null;
	static {
		PropertyConfigurator.configure("log4j.properties");
		logger = Logger.getLogger(Server.class);
	}

	Server server = null;

	public WebOrderMonitor(Server Server) {
		this.server = Server;
	}

	public void run() {
		logger.debug("WebOrderMonitor线程启动");
		
		try {
			String order = null;

			while (true) {
				logger.debug("阻塞接收指令...");
				order = server.receiveOrder();
				logger.debug("接收到"+order+"指令");
				/*
				 * 检测接收到的order是否为检测PC状态指令 若是则直接返回PC状态
				 */
				String response = null;
				if (order.equals(Server.pcStateCheckOrder)) {
					logger.debug("返回PC状态");
					response = server.pcConnectState ? "PC在线" : "PC不在线";
				} else if(order.equals(Server.rPiStateCheckOrder)) {
					logger.debug("返回PC状态");
					response = server.rPiServer.rPiConnectState ? "rPi在线" : "rPi不在线";
				} else if(order.equals(Util.getProperties().getProperty("WOL"))){
					
					if (server.rPiServer.rPiConnectState == true) {
						logger.debug("rPi在线");
						logger.debug("将命令发送给rPi端");
						server.rPiServer.sendMsgToClient(server.rPiServer.rPiClient, order);
						// 接收PC端反馈的结果信息
						logger.debug("等待rPi端反馈");
						response = server.rPiServer.getMsgFromClient(server.rPiServer.rPiClient);
						logger.debug("接收到rPi端反馈："+response);
					} else {
						logger.debug("rPi不在线");
						response = "rPi不在线";
					}
				} else {
					// 将接收到的命令转发给PC端
					if (server.pcConnectState == true) {
						logger.debug("PC在线");
						logger.debug("将命令发送给PC端");
						server.sendMsgToClient(server.pcClient, order);
						// 接收PC端反馈的结果信息
						logger.debug("等待PC端反馈");
						response = server.getMsgFromClient(server.pcClient);
						logger.debug("接收到PC端反馈："+response);
					} else {
						logger.debug("PC不在线");
						response = "PC不在线";
					}
				}
				// 将结果信息转发给web端
				logger.debug("将PC端反馈发送到web端");
				server.sendMsgToClient(server.webClient, response);
				logger.debug("发送成功");
			}
		} catch (IOException e) {
			logger.error("出现异常",e);
		}
	}
}

/**
 * 此线程负责PC端的连接和状态监测
 * 
 */
class PCStateMonitor implements Runnable {

	static Logger logger = null;
	static {
		PropertyConfigurator.configure("log4j.properties");
		logger = Logger.getLogger(Server.class);
	}

	Server Server = null;

	public PCStateMonitor(Server s) {
		this.Server = s;
	}

	public void run() {
		logger.debug("PCStateMonitor线程启动");
		while (true) {
			try {
				// 等待PC连接
				this.Server.acceptPC();
			} catch (IOException e) {
				logger.error("PC连接异常",e);
			}
			while (Server.isConnected(Server.pcClient)) {
				// 检测PC端是否还在线
				logger.debug("PC端在线");
				try {
					Thread.currentThread().sleep(1000);
				} catch (InterruptedException e) {
					logger.error("中断异常",e);
				}
			}
			logger.info("PC端已离线");
			this.Server.pcConnectState = false;
		}

	}
}

class RPiStateMonitor implements Runnable {

	static Logger logger = null;
	static {
		PropertyConfigurator.configure("log4j.properties");
		logger = Logger.getLogger(Server.class);
	}

	RPiServer rPiServer = null;

	public RPiStateMonitor(RPiServer rPiServer) {
		this.rPiServer = rPiServer;
	}

	public void run() {
		logger.debug("RPiStateMonitor线程启动");
		while (true) {
			try {
				// 等待rPi连接
				rPiServer.acceptRPi();
			} catch (IOException e) {
				logger.error("rPi连接异常", e);
			}
			while (rPiServer.isConnected(rPiServer.rPiClient)) {
				logger.debug("rPi在线");
				// 检测rPi端是否还在线
				try {
					Thread.currentThread().sleep(1000);
				} catch (InterruptedException e) {
					logger.error("中断异常", e);
				}
			}
			logger.info("RPi端已离线");
			rPiServer.rPiConnectState = false;
		}

	}
}