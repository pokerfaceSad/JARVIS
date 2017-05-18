package pokerface.Sad.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

import org.apache.commons.mail.EmailException;

import pokerface.Sad.connect.Client;
import pokerface.Sad.util.Util;

public class ServiceImpl1 implements Service{
	@Override
	public String execOrder(String order) {
		String result = "执行失败";
		Client c1 = null;
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
			System.out.println("接收到"+order+"指令");
			System.out.println("执行"+methodName+"命令");
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
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
			
			order=null;
			methodName=null;
		}else{
			//没有对应命令
			System.out.println("接收到"+order+"指令");
			System.out.println("没有对应方法");
			result = order+"指令错误，没有对应方法";
		}
		return result;
	}

}
