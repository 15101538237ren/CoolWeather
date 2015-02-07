package util;

import model.City;
import model.County;
import model.Province;
import activity.ChooseAreaActivity;
import android.R.integer;
import android.text.TextUtils;
import db.CoolWeatherDB;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.util.EncodingUtils;
public class Utility {
	/**
	 * 解析和处理返回的省级数据
	 * @param coolWeatherDB
	 * @param response
	 * @return
	 */
	public synchronized static boolean handleProvinceResponse(CoolWeatherDB coolWeatherDB,String response) {
		if(!TextUtils.isEmpty(response))
		{
			return true;
		}
		return false;
	}
	
	/**
	 * 解析和处理返回的市级数据
	 * @param coolWeatherDB
	 * @param response
	 * @return
	 */
	public synchronized static boolean handleCityResponse(CoolWeatherDB coolWeatherDB,String response,String provinceId) {
		if(!TextUtils.isEmpty(response))
		{
			String [] allCities=response.split(",");
			if(allCities!=null&& allCities.length > 0)
			{
				for (String p : allCities) {
					String [] array=p.split("\\|");
					City city=new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(Integer.valueOf(provinceId));
					coolWeatherDB.saveCity(city);
				}
			}
			return true;
		}
		return false;
	}
	/**
	 * 解析和处理返回的县级数据
	 * @param coolWeatherDB
	 * @param response
	 * @return
	 */
	public synchronized static boolean handleCountyResponse(CoolWeatherDB coolWeatherDB,String response,String cityId) {
		if(!TextUtils.isEmpty(response))
		{
			String [] allCounties=response.split(",");
			if(allCounties!=null&& allCounties.length > 0)
			{
				for (String p : allCounties) {
					String [] array=p.split("\\|");
					County county=new County();
					county.setCountyCode(array[0]);
					county.setCountyName(array[1]);
					county.setCityId(Integer.valueOf(cityId));
					coolWeatherDB.saveCounty(county);
				}
			}
			return true;
		}
		return false;
	}
}
