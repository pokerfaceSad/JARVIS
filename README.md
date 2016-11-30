#JARVIS

##*功能*

	实现对电脑的远程监控，目前已实现实时截图，发送关机指令功能

##*结构* 
	Web应用（JARVIS）和Java应用（Server，PCClient）配合实现功能
![enter image description here](https://raw.githubusercontent.com/pokerfaceSad/JARVIS/master/System.png)
 

 -  JARVIS和Server运行在服务器上，两者以Socket流本地连接(Server为ServerSocket)，JARVIS负责从移动端接收命令后通过Socket流发送给Server。
 
 
 - PCClient和运行在PC端，以Socket流和Server远程连接(Server为ServerSocket)，PCclient负责从Server处接收指令，并执行。

		JARVIS负责接收指令，PCClient负责执行指令，Server负责调度转发。

 1. Server服务端(Server)   
 （1）先初始化Server对象  
 （2）先等待Web端连接  
 （3）再等待PC端Client对象连接  
 （4） 两线程共享Server对象，并以成员变量pcConnectState来共享PC端的连接情况。  

	- 监听PC端Client断开线程(ClientMonitor)
	    每十秒钟isConnected()方法判断一次PC端是否还在线，若已离线则修改成员变量pcConnectState
	    并终止线程
		    
	- 等待Web命令线程(WebMonitor)
	
	    read()等待Web端传来指令，调用Socket类成员方法setSoTimeOut()设置read()的超时时间以解决read()方法的阻塞问题，每次跳出read()则依pcConnectState判断客户端是否已离线，是则终止线程
	- 主线程(main)
	
	    主线程监听以上两线程是否都已终止，是则等待PC客户端连接，然后重新创建线程对象并启动

 2. Web端(JARVIS) web.xml中添加 
 ```html
 <load-on-startup>1</load-on-startup>
 ```
 让 Servlet对象在服务器启动时就被创建，并在init()方法中连接Server Service()方法通过Socket向Server发送指令

 3. Client端(PCClient) 
 
 	- 主线程(main) 
		等待接收从Server传来的命令，并执行指令 
	- 等待键盘输入线程(ReceiveInput) 
		接受键盘输入，若是“closeClient”则关闭客户端
 
##*遇到的问题*

 1. Windows端发送心跳包，Socket的成员方法
	 `public void sendUrgentData(int data)
			throws IOException`
    会导致异常，原因Windows10(Windows 8似乎也会出现类似问题)不允许应用多次发送紧急信息。

	解决方法：自己实现一个发送心跳包的方法，发送特定的心跳信息，在接收端将心跳信息滤除。
	```
	static String heatbeatMsg = "heartbeating";
	public boolean isConnected(){
		try{
			/*
			 * 此发送紧急数据的方法在Windows环境下会出现异常
			 * this.pcClient.sendUrgentData(0xff);
			 * */
			 //封装好的向客户端发送字符串的方法 
			 this.sendOrder(Server.heatbeatMsg);
			return true;
		}catch(Exception e){
		    return false;
		}
	}
	```
 2. 开放服务器端用于Server和PCClient连接的端口。

