package cn.pokerfaceSad.jarvis;

import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import cn.pokerfaceSad.util.Util;

import cn.pokerfaceSad.jarvis.R;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	public static String serverIP = null;
	public static String pcState = "pc不在线";
	public static String rPiState = "rPi不在线";
	TextView tv_pcState,tv_rPiState;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// 详见StrictMode文档
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
				.detectDiskReads().detectDiskWrites().detectNetwork()
				.penaltyLog().build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
				.detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
				.penaltyLog().penaltyDeath().build());

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// 加载配置文件
		loadProperties();
		Button take_picture = (Button) findViewById(R.id.take_picture);
		take_picture.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(MainActivity.this, "已发送指令.....",
						Toast.LENGTH_LONG).show();
				Thread sendOrder = new Thread(new SenderOrderThread(
						"takepicture", MainActivity.this));
				sendOrder.start();
			}
		});
		Button screen_shot = (Button) findViewById(R.id.screen_shot);
		screen_shot.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(MainActivity.this, "已发送指令.....",
						Toast.LENGTH_LONG).show();
				Thread sendOrder = new Thread(new SenderOrderThread(
						"screenshot", MainActivity.this));
				sendOrder.start();
			}
		});
		Button shut_down = (Button) findViewById(R.id.shut_down);
		shut_down.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(MainActivity.this, "已发送指令.....",
						Toast.LENGTH_LONG).show();
				Thread sendOrder = new Thread(new SenderOrderThread("shutdown",
						MainActivity.this));
				sendOrder.start();

			}
		});

		Button wake_on = (Button) findViewById(R.id.Wake_On);
		wake_on.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(MainActivity.this, "已发送指令.....",
						Toast.LENGTH_LONG).show();
				Thread sendOrder = new Thread(new SenderOrderThread(
						"wakeOnLAN", MainActivity.this));
				sendOrder.start();
			}
		});

		Timer timer = new Timer();
		timer.schedule(new MyTask(this), 0 , 5000);
		tv_pcState = (TextView) findViewById(R.id.pcState);
		tv_rPiState = (TextView) findViewById(R.id.rPiState);

	}

	private class MyTask extends TimerTask {
		private Activity context;

		MyTask(Activity context) {
			this.context = context;
		}

		@Override
		public void run() {
			// 更新内容
			MainActivity.pcState = Util.sendOrder("pcStateCheck");
			MainActivity.rPiState = Util.sendOrder("rPiStateCheck");

			// 更新UI内容
			context.runOnUiThread(updateThread);
		}
	}
	
    Runnable updateThread = new Runnable()   
    {  
  
        @Override  
        public void run()  
        {  
            //更新UI  
            tv_pcState.setText(MainActivity.pcState);  
            tv_rPiState.setText(MainActivity.rPiState);  
        }  
          
    };  
	public void loadProperties() {
		// 应用第一次运行时初始化配置文件
		SharedPreferences sharedPreferences = this.getSharedPreferences(
				"share", MODE_PRIVATE);
		boolean isFirstRun = sharedPreferences.getBoolean("isFirstRun", true);
		Editor editor = sharedPreferences.edit();
		Properties pro = null;
		if (isFirstRun) {
			Toast.makeText(MainActivity.this, "First Run", Toast.LENGTH_SHORT)
					.show();
			pro = new Properties();
			pro.put("serverIP", "127.0.0.1");
			MainActivity.this.getExternalFilesDir(null).getAbsolutePath();
			if (Util.saveConfig(MainActivity.this, MainActivity.this
					.getExternalFilesDir(null).getAbsolutePath()
					+ "/JARVIS.properties", pro)) {
				Toast.makeText(MainActivity.this, "配置文件保存成功",
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(MainActivity.this, "配置文件保存失败",
						Toast.LENGTH_SHORT).show();
			}
			editor.putBoolean("isFirstRun", false);
			editor.commit();
		}
		pro = Util.loadConfig(MainActivity.this, MainActivity.this
				.getExternalFilesDir(null).getAbsolutePath()
				+ "/JARVIS.properties");
		MainActivity.this.serverIP = (String) pro.get("serverIP");
		;
		Toast.makeText(MainActivity.this,
				"ServerIP:" + MainActivity.this.serverIP, Toast.LENGTH_SHORT)
				.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}

class SenderOrderThread implements Runnable {
	private String order = null;
	private Context context = null;

	public SenderOrderThread(String order, Context context) {
		this.order = order;
		this.context = context;
	}

	@Override
	public void run() {
		Log.d("Test", "start");
		String result = Util.sendOrder(this.order);
		Looper.prepare();
		Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
		Looper.loop();
		Log.d("Test", "end");
	}

}
