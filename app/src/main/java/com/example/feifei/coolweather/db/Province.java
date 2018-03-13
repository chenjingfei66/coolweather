package com.example.feifei.coolweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by FeiFei on 2018/2/28.
 */

public class Province extends DataSupport {

    int id;

    String provinceName;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

}
