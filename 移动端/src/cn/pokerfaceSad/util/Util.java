package cn.pokerfaceSad.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import cn.pokerfaceSad.jarvis.MainActivity;
import android.content.Context;



public class Util {
	public synchronized static String sendOrder(String order){
		String serverIP = MainActivity.serverIP;
		String url = "http://"+serverIP+"/JARVIS/JARVIS";     //提交的地址  
		
		HttpClient client = new HttpClient();  
		PostMethod post = new PostMethod(url);  

		post.setParameter("order", order);  
		StringBuffer sb = new StringBuffer();
		try {  
		  
		     int status  =  client.executeMethod(post);     //执行，模拟POST方法提交到服务器  
		     if(status == 200)
		     {
		    	 BufferedReader br = new BufferedReader(new InputStreamReader(post.getResponseBodyAsStream(),"GBK"));
			     
			     String str = null;
			     while((str = br.readLine()) != null)
			     {
			    	 sb.append(str);
			     }
		     }else{
		    	 return "请检查网络！";
		     }
		     
		  
		 }catch (HttpException e) {  
		  
		    e.printStackTrace();  
		    return "请检查网络！";
		  
		 } catch (IOException e)  {  
		  
		    e.printStackTrace();  
		    return "请检查网络！";
		  
		 }
		
		
		return resolveHtml(sb.toString());
		
	}
	
	public static String resolveHtml(String html){
		
		Document doc = Jsoup.parse(html);
		Element ele = doc.select("body > font").get(0);
		return ele.text();
	}
	

	public static Properties loadConfig(Context context, String file) {  
	    Properties properties = new Properties();  
	    try {  
	        FileInputStream s = new FileInputStream(file);  
	        properties.load(s);
	        s.close();
	    } catch (Exception e) {  
	        e.printStackTrace();  
	        return null;  
	    }  
	    return properties;  
	}  
	public static boolean saveConfig(Context context, String file,  
	        Properties properties) {  
	    try {  
	        File fil = new File(file);  
	        if (!fil.exists())  
	            fil.createNewFile();  
	        FileOutputStream s = new FileOutputStream(fil);  
	        properties.store(s, "");  
	        s.close();
	    } catch (Exception e) {  
	        e.printStackTrace();  
	        return false;  
	    }  
	    return true;  
	}
}
