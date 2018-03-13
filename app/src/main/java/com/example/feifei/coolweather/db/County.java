package com.example.feifei.coolweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by FeiFei on 2018/2/28.
 */

public class County extends DataSupport {

    int id;
    String cuntyName;
    String weatherId;
    int cityId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCuntyName() {
        return cuntyName;
    }

    public void setCuntyName(String cuntyName) {
        this.cuntyName = cuntyName;
    }

    public String getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }
}
