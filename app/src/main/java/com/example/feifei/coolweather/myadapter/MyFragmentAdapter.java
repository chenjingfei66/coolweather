package com.example.feifei.coolweather.myadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.feifei.coolweather.R;

import java.util.List;

/**
 * Created by FeiFei on 2018/3/2.
 */

public class MyFragmentAdapter extends BaseAdapter {

    Context context;
    List<String> list;

    TextView textView;
    public MyFragmentAdapter(Context context,List<String> list){
        System.out.println("list" + list.size());
        this.context = context;
        this.list = list;
    }
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.forecastitem,parent,false);
        textView = view.findViewById(R.id.fragmenttext);
        textView.setText(list.get(position));
        return view;
    }
}
