package pokerface.Sad.JARVIS;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JARVIS implements Servlet{
	
	Socket s = null;
	@Override
	public void init(ServletConfig config) throws ServletException {
		//服务器启动时就创建此对象，并在创建对象时建立于Server的Socket连接
		
		//Socket连接
		try {
			this.s = new Socket(InetAddress.getByName("127.0.0.1"),10001);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
				
	}

	@Override
	public ServletConfig getServletConfig() {
		return null;
	}

	@Override
	public void service(ServletRequest req, ServletResponse res)
			throws ServletException, IOException {
		//获取命令
		String order = req.getParameter("order");
		OutputStream os = s.getOutputStream();
		//发送至Server
		os.write(order.getBytes());
		os.flush();
		//接收Server转发来的反馈信息
		InputStream is = s.getInputStream();
		byte[] buf = new byte[1024];
		int Len = is.read(buf);
		String result = new String(buf,0,Len);
		res.setContentType("text/html;charset=GBK");
		PrintWriter PW = res.getWriter();
		
		PW.write("<html>");
		PW.write("<title>");
		PW.write("</title>");
		PW.write("<body align=\"center\">");
		PW.write("<font size=70px>"+result+"<br><br><font>");
		PW.write("</body>");
		PW.write("</html>");
		System.out.println(req.getRemoteAddr());
	}

	@Override
	public String getServletInfo() {
		return null;
	}

	@Override
	public void destroy() {
		
	}
	

}

