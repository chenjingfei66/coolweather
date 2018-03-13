package com.example.feifei.coolweather;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.feifei.coolweather.db.City;
import com.example.feifei.coolweather.db.County;
import com.example.feifei.coolweather.db.Province;
import com.example.feifei.coolweather.myadapter.MyFragmentAdapter;
import com.example.feifei.coolweather.util.HttpUtil;
import com.example.feifei.coolweather.util.UtilJson;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {

    private Button backButton;
    private TextView titleText;
    private ListView listview;

    List<String> dataList = new ArrayList<>();

    List<Province> provincelist;
    List<City> citylist;
    List<County> countylist;
    ArrayAdapter<String> arrayAdapter;

    //判断当前页面是省还是县或者是地区
    int state = 1;

    int provinceId;
    String provinceName;

    int cityId;
    String cityName;

    List<String> weatherIdList;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.myfragment, container, false);
        initView(view);

        arrayAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, dataList);
        listview.setAdapter(arrayAdapter);

        //设置ListView的点击事件
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (state) {
                    case 1:
                        provinceId = provincelist.get(position).getId();
                        provinceName = provincelist.get(position).getProvinceName();
                        getCity();
                        break;
                    case 2:
                        cityId = citylist.get(position).getCityid();
                        cityName = citylist.get(position).getCityName();
                        getCounty();
                        break;
                    case 3:
                        if(getActivity() instanceof  MainActivity){
                            MainActivity mainActivity = (MainActivity)getActivity();
                            mainActivity.drawerLayout.closeDrawers();
                            mainActivity.weatherId = weatherIdList.get(position);
                            mainActivity.weatherId_2 = weatherIdList.get(0);
                            mainActivity.loading();
                            mainActivity.initWeatherView();
                     //       mainActivity.getImageView();

                        }else {
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            intent.putExtra("weatherId", weatherIdList.get(position));
                            intent.putExtra("weatherId_2", weatherIdList.get(0));
                            startActivity(intent);
                            getActivity().finish();
                        }
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (state) {
                    case 2:
                        getProvinces();
                        break;

                    case 3:
                        getCity();
                        break;
                }
            }
        });
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getProvinces();
    }

    public void getCounty(){

        //如果数据库有数据就从数据库拿数据
        countylist = DataSupport.where("cityId = ?", String.valueOf(cityId)).find(County.class);
        if(countylist.size() > 0){
            dataList.clear();
            weatherIdList = new ArrayList<>();
            for(County county : countylist){
                dataList.add(county.getCuntyName());
                weatherIdList.add(county.getWeatherId());
            }

            titleText.setText(cityName);
            arrayAdapter.notifyDataSetChanged();
            listview.setSelection(0);
            backButton.setVisibility(View.VISIBLE);
            state = 3;
        }else { //否则网络获取数据

            String address = "http://guolin.tech/api/china/" + provinceId + "/" +  cityId;
            HttpUtil.sendOkHttpRequest(address, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    System.out.println("Fragment中getCounty()方法网络访问错误");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    String result = response.body().string();
                    UtilJson.parseCounty(result, cityId);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getCounty();
                        }
                    });
                }
            });
        }

    }
    public void getCity() {
        citylist = DataSupport.where("provinceId = ?", String.valueOf(provinceId)).find(City.class);
        if (citylist.size() > 0) {
            dataList.clear();
            for (City city : citylist) {
                dataList.add(city.getCityName());
            }
            titleText.setText(provinceName);
            arrayAdapter.notifyDataSetChanged();
            listview.setSelection(0);
            backButton.setVisibility(View.VISIBLE);
            state = 2;
        } else {
            String address = "http://guolin.tech/api/china/" + provinceId;
            HttpUtil.sendOkHttpRequest(address, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    System.out.println("Fragment中getCity()方法网络访问错误");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String result = response.body().string();
                    UtilJson.parseCity(result, provinceId);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getCity();
                        }
                    });
                }
            });
        }
    }

    public void getProvinces() {

        provincelist = DataSupport.findAll(Province.class);
        if (provincelist.size() > 0) {
            dataList.clear();
            for (Province province : provincelist) {
                dataList.add(province.getProvinceName());
            }
            titleText.setText("中国");
            arrayAdapter.notifyDataSetChanged();
            listview.setSelection(0);
            backButton.setVisibility(View.GONE);
            state = 1;
        } else {
            String address = "http://guolin.tech/api/china";
            HttpUtil.sendOkHttpRequest(address, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    System.out.println("Fragment中getProvinces()方法网络访问错误");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String result = response.body().string();
                    UtilJson.parseProvinces(result);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getProvinces();
                        }
                    });
                }
            });

        }
    }

    private void initView(View view) {
        backButton = (Button) view.findViewById(R.id.backButton);
        titleText = (TextView) view.findViewById(R.id.title_text);
        listview = (ListView) view.findViewById(R.id.listview);
    }
}
