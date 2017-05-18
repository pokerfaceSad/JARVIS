package pokerface.Sad.mail;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

import org.apache.commons.mail.EmailException;  
import org.apache.commons.mail.HtmlEmail;  

import pokerface.Sad.util.Util;

  
  
/**  
 * 邮件发送工具实现类  
 */  
public class MailUtil { 
	public static void main(String[] args) throws FileNotFoundException, IOException
	{
		
	}
	public static void sendScreenShot() throws FileNotFoundException, IOException, EmailException{
		Properties pro = Util.getProperties();
		Mail mail = new Mail();  
	    mail.setHost(pro.getProperty("mailHost")); // 设置邮件服务器  
	    mail.setSender(pro.getProperty("mailSender"));  
	    mail.setReceiver(pro.getProperty("mailReceiver")); // 接收人  
	    mail.setUsername(pro.getProperty("mailUsername")); // 登录账号  
	    mail.setPassword(pro.getProperty("mailPassword")); // 发件人邮箱的登录密码  
	    mail.setSubject("ScreenShot");  
	    mail.setMessage(Util.getDate());
	    mail.setAttachment(pro.getProperty("RobotWorkPlace")+"PrtSc/screen.jpg", "", "");
	    new MailUtil().send(mail);
	}
	
    public void send(Mail mail) throws EmailException {  
        // 发送email  
        HtmlEmail email = new HtmlEmail();  
        // 这里是SMTP发送服务器的名字：163的如下："smtp.163.com"  
        email.setHostName(mail.getHost());  
        // 字符编码集的设置  
        email.setCharset(Mail.ENCODEING);  
        // 收件人的邮箱  
        email.addTo(mail.getReceiver());  
        // 发送人的邮箱  
        email.setFrom(mail.getSender(), mail.getName());  
        // 如果需要认证信息的话，设置认证：用户名-密码。分别为发件人在邮件服务器上的注册名称和密码  
        email.setAuthentication(mail.getUsername(), mail.getPassword());  
        // 要发送的邮件主题  
        email.setSubject(mail.getSubject());  
        // 要发送的信息，由于使用了HtmlEmail，可以在邮件内容中使用HTML标签  
        email.setMsg(mail.getMessage());  
        //添加附件
        if(mail.getAttachment()!=null)
        	email.attach(mail.getAttachment());
        // 发送  
        email.send();  
        System.out.println(mail.getSender() + " 发送邮件到 " + mail.getReceiver());  
    }  
  
}  