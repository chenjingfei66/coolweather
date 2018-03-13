package com.example.feifei.coolweather.util;

import android.text.TextUtils;

import com.example.feifei.coolweather.db.City;
import com.example.feifei.coolweather.db.County;
import com.example.feifei.coolweather.db.Province;
import com.example.feifei.coolweather.gson.Air;
import com.example.feifei.coolweather.gson.Forecast;
import com.example.feifei.coolweather.gson.Life;
import com.example.feifei.coolweather.gson.Now;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by FeiFei on 2018/2/28.
 */

public class UtilJson {


    public static Life parseLifeWeather(String result) {

        if (!TextUtils.isEmpty(result)) {
           Life life = new Gson().fromJson(result,Life.class);
            if(life.getHeWeather6().get(0).getStatus().equals("no more requests")){
                System.out.println("no more requests");
            }
           if(life.getHeWeather6().get(0).getStatus().equals("ok")){
               return life;
           }
        }
        return null;
    }

    public static Air parseAirWeather(String result) {

        if (!TextUtils.isEmpty(result)) {
            Gson gson = new Gson();
            Air air = gson.fromJson(result,Air.class);
            if(air.getHeWeather6().get(0).getStatus().equals("ok")){
               return air;
            }
        }
        return null;
    }

    public static Forecast parseForecaseWeather(String result) {

        if (!TextUtils.isEmpty(result)) {
            Gson gson = new Gson();
            Forecast list = gson.fromJson(result, Forecast.class);
            if (list.getHeWeather6().get(0).getStatus().equals("ok")) {
                return list;
            }
        }
        return null;
    }

    public static Now parseNowWeather(String result) {

        if (!TextUtils.isEmpty(result)) {
            Gson gson = new Gson();
            Now now = gson.fromJson(result, Now.class);
            if (now.getHeWeather6().get(0).getStatus().equals("ok")) {
                return now;
            }
        }
        return null;
    }

    public static boolean parseProvinces(String result) {
        if (!TextUtils.isEmpty(result)) {
            try {
                JSONArray jsonArray = new JSONArray(result);
                if (jsonArray != null) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        Province province = new Province();
                        province.setProvinceName(jsonObject.getString("name"));
                        province.setId(jsonObject.getInt("id"));
                        province.save();
                    }

                    return true;
                }
            } catch (Exception e) {
                System.out.println("解析parseProvinces时出错");
            }
        }
        return false;
    }


    public static boolean parseCity(String result, int provinceId) {

        if (!TextUtils.isEmpty(result)) {
            try {
                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    City city = new City();
                    city.setCituName(jsonObject.getString("name"));
                    city.setCityid(jsonObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }

                return true;
            } catch (Exception e) {
                System.out.println("解析parseCity出错");
            }
        }
        return false;
    }


    public static boolean parseCounty(String result, int cityId) {

        if (!TextUtils.isEmpty(result)) {

            try {
                JSONArray jsonArray = new JSONArray(result);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    County county = new County();
                    county.setCityId(cityId);
                    county.setCuntyName(jsonObject.getString("name"));
                    county.setWeatherId(jsonObject.getString("weather_id"));
                    county.save();
                }
                return true;
            } catch (Exception e) {
                System.out.println("解析parseCounty时出错");
            }
        }
        return false;
    }
}
