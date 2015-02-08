package com.buaa.renhonglei.service;

import com.buaa.renhonglei.receiver.AutoUpdateReceiver;
import com.buaa.renhonglei.util.HttpCallBackListener;
import com.buaa.renhonglei.util.HttpUtil;
import com.buaa.renhonglei.util.Utility;
import android.R.integer;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

public class AutoUpdateService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public int onStartCommand(Intent intent,int flags ,int startId)
	{
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				updateWeather();
			}
		}).start();
		AlarmManager manager=(AlarmManager)getSystemService(ALARM_SERVICE);
		int anHour=8*60*60*1000;//8小时的毫秒数
		long trigerAtTime=SystemClock.elapsedRealtime()+anHour;
		Intent i=new Intent(this,AutoUpdateReceiver.class);
		PendingIntent pi=PendingIntent.getBroadcast(this, 0, i, 0);
		manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, trigerAtTime, pi);
		return super.onStartCommand(intent, flags, startId);
	}
	/**
	 * 更新天气信息
	 */
	private void updateWeather() {
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
		String weatherCode=prefs.getString("weather_code", "");
		String address="http://www.weather.com.cn/data/cityinfo/"+weatherCode+".html";
		HttpUtil.sendHttpRequest(address, new HttpCallBackListener() {
			
			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				Utility.handleWeatherResponse(AutoUpdateService.this, response);
				
			}
			
			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				e.printStackTrace();
			}
		});
	}
}