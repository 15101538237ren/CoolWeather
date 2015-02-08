package activity;

import util.HttpCallBackListener;
import util.HttpUtil;
import util.Utility;

import com.coolweather.app.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeatherActivity extends Activity {

	private LinearLayout weatherInfoLayout;
	private TextView cityNameText;
	private TextView publishText;
	private TextView weatherDespText;
	private TextView templText;
	private TextView temphText;
	private TextView currentDateText;

	/**
	 * 切换城市按钮
	 */
	private Button switchCity;
	/**
	 * 更新天气按钮
	 */
	private Button refreshWeather;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.weather_layout);
		weatherInfoLayout =(LinearLayout)findViewById(R.id.weather_info_layout);
		
		cityNameText=(TextView)findViewById(R.id.city_name);
		publishText=(TextView)findViewById(R.id.publish_text);
		weatherDespText=(TextView)findViewById(R.id.weather_desp);
		templText=(TextView)findViewById(R.id.templ);
		temphText=(TextView)findViewById(R.id.temph);
		currentDateText=(TextView)findViewById(R.id.current_date);
		
		switchCity=(Button)findViewById(R.id.switch_city);
		refreshWeather=(Button)findViewById(R.id.refresh_weather);
		switchCity.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(WeatherActivity.this, ChooseAreaActivity.class);
				intent.putExtra("from_weather_activity", true);
				startActivity(intent);
				finish();
			}
		});
		refreshWeather.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				publishText.setText("同步中...");
				SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
				String countyCode=prefs.getString("weather_code", "");
				if(!TextUtils.isEmpty(countyCode))
				{
					queryWeatherCode(countyCode);
				}
			}
		});
		String countyCode=getIntent().getStringExtra("county_code");
		if(!TextUtils.isEmpty(countyCode))
		{
			//有县级代号时就去查询天气
			publishText.setText("同步中...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		}
		else {
			//没有县级代号时就直接显示本地天气
			showWeather();
		}
	}
	
	/**
	 * 查询县级代号所对应的天气信息
	 * @param countyCode
	 */
	private void queryWeatherCode(String countyCode) {
		String address="http://www.weather.com.cn/data/cityinfo/"+countyCode+".html";
		queryFromServer(address,"weatherCode");
	}
	/**
	 * 根据传入的地址和类型去向服务器查询天气代号和天气信息
	 * @param address
	 * @param type
	 */
	private void queryFromServer(final String address,final String type) {
		HttpUtil.sendHttpRequest(address, new HttpCallBackListener() {
			
			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				//处理服务器返回的天气信息
				if("weatherCode".equals(type)) {
					Utility.handleWeatherResponse(WeatherActivity.this, response);
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							showWeather();
						}
					});
				}
			}
			
			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					publishText.setText("同步失败");
				}
			});
			}
		});
	}
	/**
	 * 从SharedPreferences文件中读取存储的天气信息并显示到界面上
	 */
	private void showWeather() {
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
		cityNameText.setText(prefs.getString("city_name", ""));
		templText.setText(prefs.getString("templ", ""));
		temphText.setText(prefs.getString("temph", ""));
		weatherDespText.setText(prefs.getString("weather_desp", ""));
		publishText.setText("今天"+prefs.getString("publish_time", "")+"发布");
		currentDateText.setText(prefs.getString("current_date", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
	}
}
