# JARVIS

### 通过手机（或任何可以发送Http请求的设备）对PC进行监控

## 功能

1. 唤醒 唤醒处于关机或休眠状态的PC（此功能需要与PC处于同一局域网的设备支持，测试用RPi——树莓派）
2. 拍照 调用电脑摄像头拍照发送至指定邮箱（需要有摄像头）
3. 关机 向PC发送关机指令
4. 截图 截取当前显示器画面发送至指定邮箱
5. 检测 检测PC和RPi是否连接

## 结构
![结构图][1]
## 伪代码

#### JARVIS（Web应用，负责接收命令发送给Server）

	1. init()方法中，主动连接Server
	2. Service()方法中，将前端传来的指令发送给Server,然后将反馈信息，输出至前端
	

#### Server（ extends ServerSocket	负责转发调度）
	
	主线程Main Thread
	
	1. 启动后首先等待JARVIS(Web端)连接
	2. Web连接成功后，创建
		Thread--webMonitorThread
		此线程负责Server与Web的连接
        {
		
    		1>  等待Web端传来指令（阻塞）

    		2>  检测指令是否为检测PC状态指令，若是则只返回PC的连接状态
    			检测指令是否为WOL指令，若是则转发至RPiClient端
    		  	若PC在线，则转发指令至PC端，然后等待指令执行结果，再将此结果反馈给Web端
    		  	若PC不在线，向Web返回（通过Socket）“PC不在线”信息		
    		
    		    Note:这是一个长连接，不断开
		}

	3. 创建
		Thread--pcMonitorThead
		由此线程负责PC的连接
        {
		
    		1>等待PCClient连接（阻塞）

    		2>每隔一秒向PCClient发送心跳信息
    			若发送失败则说明PC已离线,回到->1

		}
	4. 创建
		Thread--rPiMonitorThread
		由此线程负责RPi的连接
        {
		
    		1>等待RPiClient连接（阻塞）
    		2>每隔一秒向RPiClient发送心跳信息
    			若发送失败则说明RPi已离线,回到->1

		}	

#### PCClient(extends Socket 运行于PC端，和Server连接，从Server处接收命令，执行并反馈)

	主线程Main Thread
	
	1.主动连接Server
	2.创建
		Thread--ReceiveInput
		此线程负责监听键盘输入的关闭客户端指令
	3.等待Server端传来指令
		接收到指令后通过Service接口调用execOrder()方法使用反射机制执行响应指令
	4.将执行结果反馈到Server端，回到->1


#### RPiClient(extends Socket 运行于RPi端，和Server连接等待Server传来WOL指令，则执行对应方法唤醒PC)
	
	主线程Main Thread
	
	1. 主动连接Server
	2. 创建
		Thread--ReceiveInput
		此线程负责监听键盘输入的关闭客户端指令
	3. 等待Server端传来指令
		接收到指令后通过Service接口调用execOrder()方法使用反射机制执行响应指令
	4. 将执行结果反馈到Server端，回到->1	


#### Browser/App(向JARVIS发送Http请求并分析响应)


## 仍待完善的功能

1. PCClient和RPiClient的断线重连，以避免网络波动导致掉线。

## 更新日志

## *更新 4.0*
1. 增加了远程唤醒功能。让PC处于同一局域网下的设备（用RPi——树莓派做测试）连接到Server端，响应WakeOn指令唤醒PC
2. Server端增加了RPiServer类管理RPi的连接
3. Server端增加了RPiStateMonitor线程，通过RPiServer对象，等待RPi连接并监听RPi的连接状态 
4. PC端增加了RPiClient（运行于与PC处于同一局域网的设备上）负责
5. 修改了Server端PCStateMonitor线程，通过Server对象， 等待PC连接并负责监听PC的连接状态

## *更新 3.0*
1. 增加了（简单丑陋的）Android客户端
```
安装后需要在在安装目录中找到JARVIS.properties配置文件，修改其serverIP项为服务器的IP地址，默认为127.0.0.1
```
![apk][2]
## *更新 2.0*
1. 增加了拍照（takepicture）功能，使用了开源库[webcam-capture][3]调用电脑摄像头，然后将通过邮件发送照片

## *更新 1.0*

1. 增加了指令执行结果的反馈，Server端转发PC端的执行结果至Web端
2. 修改Client类和Server类，~~从自定义类将Socket类和ServerSocket类作为成员变量~~（当时只有我和上帝知道为什么要这么写，现在只有上帝知道）修改为直接继承Socket类和ServerSocket类
3. 修改了执行指令的方法，~~从在Client类main方法中直接反射机制调用Util类~~修改为定义Service接口，PCClient通过Service接口执行指令
4. 修改了Server端和Web端的连接方式，~~从仅当PC连接时才接收指令~~修改为始终接收web端的指令，若PC未连接则将将“PC不在线”的信息反馈给Web端


 
## *遇到的问题*

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


  [1]:https://raw.githubusercontent.com/pokerfaceSad/JARVIS/master/pic/Syetem01.png
  [2]: https://raw.githubusercontent.com/pokerfaceSad/JARVIS/master/pic/apk.png
  [3]: https://github.com/sarxos/webcam-capture