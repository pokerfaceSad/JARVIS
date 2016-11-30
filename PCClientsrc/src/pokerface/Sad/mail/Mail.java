package pokerface.Sad.mail;
import java.io.Serializable;  

import org.apache.commons.mail.EmailAttachment;
  
/**  
 * Mail属性实体  
 *   
 * @author shadow  
 *   
 */  
@SuppressWarnings("serial")  
public class Mail implements Serializable {  
  
    public static final String ENCODEING = "UTF-8";  
  
    private String host; // 服务器地址  
  
    private String sender; // 发件人的邮箱  
  
    private String receiver; // 收件人的邮箱  
  
    private String name; // 发件人昵称  
  
    private String username; // 账号  
  
    private String password; // 密码  
  
    private String subject; // 主题  
  
    private String message; // 信息(支持HTML)  
    
    private EmailAttachment attachment; //附件
  
    public EmailAttachment getAttachment() {
		return attachment;
	}

	public void setAttachment(String path,String Description,String name) {
		EmailAttachment attachment = new EmailAttachment();//创建附件  
		attachment.setPath(path);//本地附件，绝对路径    
        attachment.setDisposition(EmailAttachment.ATTACHMENT);  
        attachment.setDescription(Description);//附件描述   
        attachment.setName(name);//附件名称  
		this.attachment = attachment;
	}

	public String getHost() {  
        return host;  
    }  
  
    public void setHost(String host) {  
        this.host = host;  
    }  
  
    public String getSender() {  
        return sender;  
    }  
  
    public void setSender(String sender) {  
        this.sender = sender;  
    }  
  
    public String getReceiver() {  
        return receiver;  
    }  
  
    public void setReceiver(String receiver) {  
        this.receiver = receiver;  
    }  
  
    public String getName() {  
        return name;  
    }  
  
    public void setName(String name) {  
        this.name = name;  
    }  
  
    public String getUsername() {  
        return username;  
    }  
  
    public void setUsername(String username) {  
        this.username = username;  
    }  
  
    public String getPassword() {  
        return password;  
    }  
  
    public void setPassword(String password) {  
        this.password = password;  
    }  
  
    public String getSubject() {  
        return subject;  
    }  
  
    public void setSubject(String subject) {  
        this.subject = subject;  
    }  
  
    public String getMessage() {  
        return message;  
    }  
  
    public void setMessage(String message) {  
        this.message = message;  
    }


  
}  