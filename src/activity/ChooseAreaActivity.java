package activity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.coolweather.app.R;

import model.City;
import model.County;
import model.Province;

import db.CoolWeatherDB;
import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class ChooseAreaActivity extends Activity {
	public static final int LEVEL_PROVINCE=0;
	public static final int LEVEL_CITY=1;
	public static final int LEVEL_COUNTY=2;
	private TextView titleText;
	private ListView listView;
	private ProgressDialog progressDialog;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;
	private List<String> dataList=new ArrayList<String>();
	
	/**
	 * 省列表
	 */
	private List<Province> provinceList;
	
	/**
	 * 市列表
	 */
	private List<City> cityList;
	
	/**
	 * 县列表
	 */
	private List<County> countyList;
	
	/**
	 * 选中的省份
	 */
	private Province selectedProvince;
	
	/**
	 * 选中的市
	 */
	private City selectedCity;
	
	/**
	 * 当前选中的级别
	 */
	private int currentLevel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		
		listView=(ListView)findViewById(R.id.list_view);
		titleText=(TextView)findViewById(R.id.title_text);
		adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,dataList);
		listView.setAdapter(adapter);
		
		coolWeatherDB=CoolWeatherDB.getInstanceDb(this);
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if(currentLevel==LEVEL_PROVINCE)
				{
					selectedProvince=provinceList.get(position);
					queryCities();
				}
				else if (currentLevel==LEVEL_CITY) {
					selectedCity=cityList.get(position);
					queryCounties();
				}
			}
		});
		queryProvinces();
		Toast.makeText(this, "省信息查询完毕", Toast.LENGTH_SHORT).show();
	}
	/**
	 * 查询全国的所有省,优先从数据库查询,如果没有从本地文件获取
	 */
	private void  queryProvinces() {
		showProgressDialog();
		provinceList=coolWeatherDB.loadProvinces();
		if(provinceList.size()>0)
		{
			dataList.clear();
			for (Province province : provinceList) {
				dataList.add(province.getProvinceName());
			}
			//
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel=LEVEL_PROVINCE;
		}
		else {
			String fileName="provinces.txt";
			Pattern p=Pattern.compile("([0-9]{9})=([^\\r\\n]+)");
			Matcher m =null;
			try{ 
				InputStreamReader inputReader = new InputStreamReader( getResources().getAssets().open(fileName) ); 
	            BufferedReader reader = new BufferedReader(inputReader);
	            String tempString ="";
	            // 一次读入一行，直到读入null为文件结束
	            while ((tempString = reader.readLine()) != null) {
	            	m=p.matcher(tempString);
	            	if(m.find())
	                {
	            		String provinceCode=m.group(1);
	               	 	String provinceName=m.group(2);
	               	 	Province province=new Province();
            	 		province.setProvinceName(provinceName);
            	 		province.setProvinceCode(provinceCode);
            	 		coolWeatherDB.saveProvince(province);
            	 		Log.d("ChooseAreaActivity", "province:"+provinceName+",id:"+provinceCode);
	            }
	          }
	        reader.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			queryProvinces();
		}
		closeProgressDialog();
	}
	/**
	 * 查询选中省份的所有市,优先从数据库查询,如果没有从本地文件获取
	 */
	private void  queryCities()
	{
		showProgressDialog();
		cityList=coolWeatherDB.loadCitys(selectedProvince.getId());
		if(cityList!=null&&cityList.size()>0)
		{
			dataList.clear();
			for (City city : cityList) {
				dataList.add(city.getCityName());
			}
			//Toast.makeText(this, "市信息查询完毕", Toast.LENGTH_SHORT).show();
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel=LEVEL_CITY;
		}
		else {
			String fileName="cities.txt";
			String cityCode=selectedProvince.getProvinceCode();
			Log.d("CityCodeBefore",cityCode);
			cityCode=cityCode.substring(0,cityCode.length()-4);
			Pattern p=Pattern.compile("("+cityCode+")"+"([0-9]{4})=([^\\r\\n]+)");
			Matcher m =null;
			try{ 
				InputStreamReader inputReader = new InputStreamReader( getResources().getAssets().open(fileName) ); 
	            BufferedReader reader = new BufferedReader(inputReader);
	            String tempString ="";
	            // 一次读入一行，直到读入null为文件结束
	            while ((tempString = reader.readLine()) != null) {
	            	m=p.matcher(tempString);
	            	if(m.find())
	                {
	            		String cityPreCode=m.group(1);
	            		String cityPostCode=m.group(2);
	               	 	String cityName=m.group(3);
	               	 	City city=new City();
            	 		city.setCityName(cityName);
            	 		city.setCityCode(cityPreCode+cityPostCode);
            	 		String provinceCode=cityCode.substring(0, cityCode.length()-4)+"0100";
            	 		city.setProvinceId(selectedProvince.getId());
            	 		coolWeatherDB.saveCity(city);
            	 		Log.d("ChooseAreaActivity", "city:"+cityName+",id:"+cityCode+",provinceCode:"+provinceCode);
	                }
	            }
	            reader.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			queryCities();
		}
		closeProgressDialog();
	}
	
	/**
	 * 查询选中城市的所有县,优先从数据库查询,如果没有从本地文件获取
	 */
	private void queryCounties() {
		showProgressDialog();
		countyList=coolWeatherDB.loadCountys(selectedCity.getId());
		if(countyList!=null&&countyList.size()>0)
		{
			dataList.clear();
			for (County county : countyList) {
				dataList.add(county.getCountyName());
			}
			//Toast.makeText(this, "市"+selectedCity.getCityName()+"的县信息查询完毕！", Toast.LENGTH_SHORT).show();
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel=LEVEL_COUNTY;
		}
		else {
			String fileName="counties.txt";
			String cityCode=selectedCity.getCityCode();
			cityCode=cityCode.substring(0,cityCode.length()-2);
			Pattern p=Pattern.compile("("+cityCode+")"+"([0-9]{2})=([^\\r\\n]+)");
			Matcher m =null;
			boolean flag=false;
			try{ 
				InputStreamReader inputReader = new InputStreamReader( getResources().getAssets().open(fileName) ); 
	            BufferedReader reader = new BufferedReader(inputReader);
	            String tempString ="";
	            // 一次读入一行，直到读入null为文件结束
	            while ((tempString = reader.readLine()) != null) {
	            	m=p.matcher(tempString);
	            	if(m.find())
	                {
	            		String cityPreCode=m.group(1);
	               	 	String countyCode=m.group(2);
	               	 	String countyName=m.group(3);
	               	 	County county=new County();
	               	 	
						county.setCountyName(countyName);
						county.setCountyCode(cityCode+countyCode);
						county.setCityId(selectedCity.getId());
						coolWeatherDB.saveCounty(county);
						Log.d("ChooseAreaActivity", "county:"+countyName+",id:"+countyCode+",cityCode:"+cityCode);
						flag=true;
	                }
	            }
	            reader.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			if (flag) {
				queryCounties();
			}
		}
		closeProgressDialog();
	}
	/**
	 * 显示进度对话框
	 */
	private void showProgressDialog() {
		if(progressDialog==null)
		{
			progressDialog=new ProgressDialog(this);
			progressDialog.setMessage("正在加载...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	/**
	 * 关闭进度对话框
	 */
	private void closeProgressDialog() {
		if(progressDialog!=null)
		{
			progressDialog.dismiss();
		}
	}
	@Override
	public void onBackPressed()
	{
		if(currentLevel==LEVEL_COUNTY)
		{
			queryCities();
		}
		else if(currentLevel==LEVEL_CITY) {
			queryProvinces();
		}
		else {
			finish();
		}
	}
}
