package com.example.feifei.coolweather.LBS;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.example.feifei.coolweather.mylistener.MyLBSDataListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by FeiFei on 2018/3/3.
 */

public class MyLBS {

    LocationClient locationClient;
    Context context;

    MyLBSDataListener myLBSDataListener;


    public MyLBS(Context context) {
        this.context = context;
        locationClient = new LocationClient(context);
        locationClient.registerLocationListener(new MyLocationListener());
    }

    public void requestLocation() {
        locationClient.start();
    }

    public String[] beginLBS() {

        List<String> permissionList = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!permissionList.isEmpty()) {
            String[] permission = new String[permissionList.size()];
            permissionList.toArray(permission);
            //   ActivityCompat.requestPermissions(context,permission,1);
            return permission;
        } else {
            requestLocation();
        }

        return null;
    }

    class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation bdLocation) {
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("维度为：").append(bdLocation.getLatitude()).append("\n");
            stringBuffer.append("经度为：").append(bdLocation.getLongitude()).append("\n");
            stringBuffer.append("国家为：").append(bdLocation.getCountry()).append("\n");
            stringBuffer.append("省为：").append(bdLocation.getProvince()).append("\n");
            stringBuffer.append("市为：").append(bdLocation.getCity()).append("\n");
            stringBuffer.append("区为：").append(bdLocation.getDistrict()).append("\n");
            stringBuffer.append("街道为：").append(bdLocation.getStreet()).append("\n");
            stringBuffer.append("定位方式：");
            if (bdLocation.getLocType() == BDLocation.TypeGpsLocation) {
                stringBuffer.append("GPS");
            } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                stringBuffer.append("网络");
            } else {
                stringBuffer.append(bdLocation.getLocType());
            }

            System.out.println(stringBuffer);
            String data = String.valueOf(bdLocation.getLongitude()) + "," + String.valueOf( bdLocation.getLatitude());
            myLBSDataListener.sendData(data);

        }
    }

    public void setLBSDateListener(MyLBSDataListener myLBSDataListener) {
        this.myLBSDataListener = myLBSDataListener;
    }
}
