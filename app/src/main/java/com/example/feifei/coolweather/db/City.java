package com.example.feifei.coolweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by FeiFei on 2018/2/28.
 */

public class City extends DataSupport{

    String cituName;

    int cityid;

    int provinceId;


    public String getCityName() {
        return cituName;
    }

    public void setCituName(String cituName) {
        this.cituName = cituName;
    }

    public int getCityid() {
        return cityid;
    }

    public void setCityid(int cityid) {
        this.cityid = cityid;
    }

    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }
}
