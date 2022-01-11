package com.caj.coolweather.util;

import com.caj.coolweather.db.City;
import com.caj.coolweather.db.Country;
import com.caj.coolweather.db.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utility {

    /**
     * 处理省的数据
     * @param response
     * @return
     */
    public static boolean handleProvinceResponse(String response) {
        try {
            JSONArray array = new JSONArray(response);
            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                Province province = new Province();
                province.setProvinceName(jsonObject.getString("provinceName"));
                province.save();
            }
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 处理市的数据
     * @param response
     * @return
     */
    public static boolean handleCityResponse(String response) {
        try {
            JSONArray array = new JSONArray(response);
            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                City city = new City();
                city.setProvinceName(jsonObject.getString("provinceName"));
                city.setCityName(jsonObject.getString("cityName"));
                city.save();
            }

            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 处理镇的数据
     * @param response
     * @return
     */
    public static boolean handleCountryResponse(String response) {
        try {
            JSONArray array = new JSONArray(response);
            for (int i = 0; i < array.length(); i++) {
                JSONObject jsonObject = array.getJSONObject(i);
                Country country = new Country();
                country.setProvinceName(jsonObject.getString("provinceName"));
                country.setCityName(jsonObject.getString("cityName"));
                country.setCountryName(jsonObject.getString("countryName"));
                country.save();
            }
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }
}
