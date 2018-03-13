package com.example.feifei.coolweather.myadapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.feifei.coolweather.R;
import com.example.feifei.coolweather.gson.Forecast;

import java.util.List;

/**
 * Created by FeiFei on 2018/3/1.
 */

public class MyForecaseAdapter extends RecyclerView.Adapter<MyForecaseAdapter.MyHolder> {

    List<Forecast.HeWeather6BeanForecast.DailyForecastBean> list;
    Context context;


    private TextView forecaseTime;
    private TextView forecaseTianqi;
    private TextView forecaseMax;
    private TextView forecaseMin;

    public class MyHolder extends RecyclerView.ViewHolder {


        public MyHolder(View itemView) {
            super(itemView);

            forecaseTime = (TextView) itemView.findViewById(R.id.forecaseTime);
            forecaseTianqi = (TextView) itemView.findViewById(R.id.forecaseTianqi);
            forecaseMax = (TextView) itemView.findViewById(R.id.forecaseMax);
            forecaseMin = (TextView) itemView.findViewById(R.id.forecaseMin);
        }
    }

    public MyForecaseAdapter(List<Forecast.HeWeather6BeanForecast.DailyForecastBean> list, Context context) {
        this.list = list;
        this.context = context;
    }


    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.forecastitem, parent, false);
        MyHolder myHolder = new MyHolder(view);
        return myHolder;
    }

    @Override
    public void onBindViewHolder(MyHolder holder, int position) {

        forecaseTime.setText(list.get(position).getDate());
        forecaseTianqi.setText(list.get(position).getCond_txt_d());
        forecaseMax.setText(list.get(position).getTmp_max() + "℃");
        forecaseMin.setText(list.get(position).getTmp_min()+ "℃");

    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
