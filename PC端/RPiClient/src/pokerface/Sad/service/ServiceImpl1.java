package pokerface.Sad.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

import org.apache.commons.mail.EmailException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import pokerface.Sad.connect.RPiClient;
import pokerface.Sad.util.Util;

public class ServiceImpl1 implements Service{
	
	static Logger logger = null;
	static{
		PropertyConfigurator.configure("log4j.properties");
		logger = Logger.getLogger(ServiceImpl1.class);
	}
	
	@Override
	public String execOrder(String order) {
		String result = "执行失败";
		RPiClient c1 = null;
		String methodName = null;
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
		
		methodName = pro.getProperty(order); //从配置文件中获取命令对应的方法名
		
		if(methodName!=null)
		{
			logger.info("接收到"+order+"指令");
			logger.info("执行"+methodName+"命令");
			//反射机制调用方法
			try {
				cls.getDeclaredMethod(methodName).invoke(cls);
				result = order+"执行成功";
			} catch (IllegalAccessException | IllegalArgumentException  | InvocationTargetException e) {
				e.printStackTrace();
				Throwable cause = e.getCause();
				if(cause instanceof EmailException){
					result = "邮件发送失败";
				}
			} catch (NoSuchMethodException e) {
				logger.error("发生异常",e);
			} catch (SecurityException e) {
				logger.error("发生异常",e);
			}
			
			order=null;
			methodName=null;
		}else{
			//没有对应命令
			logger.info("接收到"+order+"指令");
			logger.info("没有对应方法");
			result = order+"指令错误，没有对应方法";
		}
		return result;
	}

}
