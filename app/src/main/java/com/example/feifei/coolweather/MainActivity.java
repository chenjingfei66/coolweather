package com.example.feifei.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.feifei.coolweather.LBS.MyLBS;
import com.example.feifei.coolweather.gson.Air;
import com.example.feifei.coolweather.gson.Forecast;
import com.example.feifei.coolweather.gson.Life;
import com.example.feifei.coolweather.gson.Now;
import com.example.feifei.coolweather.myadapter.MyForecaseAdapter;
import com.example.feifei.coolweather.mylistener.AppBarStateChangeListener;
import com.example.feifei.coolweather.mylistener.MyLBSDataListener;
import com.example.feifei.coolweather.service.MyService;
import com.example.feifei.coolweather.util.HttpUtil;
import com.example.feifei.coolweather.util.UtilJson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private TextView nowLocation;
    private TextView nowWendu;
    private TextView nowTianqi;
    TextView loadingForecast;
    private TextView maqi;
    private TextView mpm;
    private TextView loadingair;
    private LinearLayout airLayout;
    private TextView loadingNow;
    private RelativeLayout nowLayout;
    private LinearLayout lifeLayout;
    private TextView lifecmf;
    private TextView lifCw;
    private TextView lifeSport;
    private TextView lifeTrav;
    private TextView loadingLife;
    private RecyclerView forecastrecyclerView;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private Toolbar toolbar;
    SwipeRefreshLayout swipeRefreshLayout;
    private AppBarLayout appbarlayout;
    RecyclerView recyclerViewForecast; //用来显示forecast的数据列表

    String toolLacation;   //小时toolbar的信息
    String toolTiqian;
    String toolWendu;

    DrawerLayout drawerLayout;
    ImageView imageviewBackground;

    String weatherId = "beijing";
    String stringKey = "&key=c85628df849a486994dcb7f155477931";
    static String stringNow = "https://free-api.heweather.com/s6/weather/now?location=";
    static String stringForecast = "https://free-api.heweather.com/s6/weather/forecast?location=";
    static String stringLife = "https://free-api.heweather.com/s6/weather/lifestyle?location=";
    static String stringAri = "https://free-api.heweather.com/s6/air/now?location=";

    //是通过必应的网站来获取图片的，但是无法直接获得必应网站图片的下载地址，需要访问"http://guolin.tech/api/bing_pic"才能获取到下载地址
    //"http://guolin.tech/api/bing_pic"是郭霖老师为我们封装好的，访问此地址就能获取到图片的真正地址
    String imageUri = "http://guolin.tech/api/bing_pic";
    int imageState = 0; //使用imageState来保存图片的获取状态，如果是第一次进入应用的时候imageState为0，这个时候就会从网上获取图片，
                         // 如果是刷新或者重新选择城市的时候imageState不为0，就不需要再网上获取图片，提高效率和节约时间

    //在获取空气质量时在，小县城是无法获取到空气质量的（和风免费版本），所以用市（小县城的parent_city）来获取空气质量，所以我们需要获取两个weatherId
    String weatherId_2;
    //我们循环两次来尝试不同的weatherId，但是在获取香港等特别行政区的空气质量时，两次都是无法获取的

    //所以用cunt来设置循环次数，循环两次之后就跳出循环
    int cunt;
    MyLBS myLBS;   //用来定位的
    int finishState = 0; //用来记录数据更新的情况，,获取网络图片加一，如果now更新成功或者失败加一，如果air更新成功或者失败加一，以此类推
    //当总共有5组数据（imageview,now，forecast，air，life）无论成功失败finishState都会加到5
    //一直发送数据跟Handler如果msg（也就是finishState）等于，就关闭 swipeRefreshLayout然后显示Toast提示

    int dingweiState = 0; //用来保存定位的信息，点击定位之后dingweiState = 1 ，如果dingweiState = 1，则在获取Now的时候保存获取weatherId和parent_city的信息

    String parseAdress; //用来保存访问的now，forecast，air，life的地址

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what + imageState == 5) {  //表示所有刷新数据获取完成，把weatherId存到SharedPreferences
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(MainActivity.this, "更新完成", Toast.LENGTH_SHORT).show();
                finishState = 0;

                SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                editor.putString("weatherId", weatherId);
                editor.putString("weatherId_2", weatherId_2);
                editor.commit();
            } else if (msg.what == 10) { //在使用定位时会从Now中返回数据（weather和parent_city），然后在重新initWeatherView；
                initWeatherView();
            }

        }
    };
    private ImageView dingwei;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        cunt = 0;

        getSharedAndIntentData();//把weatherId和图片的Url给保存在本地Shared中，先从Shared中获取数据
        initView(); //给控件findId
        loading();
        swipeRefreshLayout.setRefreshing(true);

        getImageView();

        initWeatherView();
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.mipmap.home);
        }

        swipeRefreshLayout.setColorSchemeColors(Color.BLUE);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loading();
                getImageView();
                initWeatherView();

            }
        });

        appbarlayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                if (state == State.EXPANDED) {

                    collapsingToolbarLayout.setTitle("");
                    //展开状态

                } else if (state == State.COLLAPSED) {
                    nowLocation.setVisibility(View.GONE);
                    nowTianqi.setVisibility(View.GONE);
                    nowWendu.setVisibility(View.GONE);
                    collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
                    collapsingToolbarLayout.setTitle("   " + toolLacation + "    " + toolTiqian + "    " + toolWendu);
                    //折叠状态

                } else {
                    nowLocation.setVisibility(View.VISIBLE);
                    nowTianqi.setVisibility(View.VISIBLE);
                    nowWendu.setVisibility(View.VISIBLE);
                    collapsingToolbarLayout.setTitle("");
                    //中间状态

                }
            }
        });

        dingwei.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myLBS = new MyLBS(getApplicationContext());
                String[] list = myLBS.beginLBS(); //返回访问权限的数据
                if (list != null) {
                    ActivityCompat.requestPermissions(MainActivity.this, list, 1);
                    myLBS.setLBSDateListener(new MyLBSDataListener() {   //这是自定义的回调接口， 当MyLBS类中获取完地址之后就会调用这个接口
                        @Override
                        public void sendData(String s) {  //把经纬度传过来
                            if (s != null) {
                                String dingwei = stringNow + s + stringKey;  //获取定位的经纬度之后，通过经纬度来获取天气信息
                                dingweiState = 1;
                                parseAdress = dingwei;
                                beginParseWeather(parseAdress, 1); //使用经纬度利用Now来获取weatherId和parent_city
                            }
                        }
                    });
                }
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0) {
                for (int permission : grantResults) {
                    if (permission != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "您没有授权", Toast.LENGTH_LONG).show();
                        return;
                    }
                    myLBS.requestLocation();
                }
            }
        }
    }

    private void getSharedAndIntentData() {
        SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        String id = sharedPreferences.getString("weatherId", null);
        String id_2 = sharedPreferences.getString("weatherId_2", null);
        if (id != null && id_2 != null) {
            weatherId = id;
            weatherId_2 = id_2;
        }
        Intent intent = getIntent(); //从Fragment传过来的
        if (intent != null && intent.getStringExtra("weatherId") != null) {
            weatherId = intent.getStringExtra("weatherId");
            weatherId_2 = intent.getStringExtra("weatherId_2");
            SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
            editor.putString("weatherId", weatherId);
            editor.putString("weatherId_2", weatherId_2);
            editor.commit();
        }
    }

    //设置背景图
    public void getImageView() {

        HttpUtil.sendOkHttpRequest(imageUri, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

                imageState = 1;
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String string = response.body().string();
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(MainActivity.this).load(string).into(imageviewBackground);
                    }
                });
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
        }
        return super.onOptionsItemSelected(item);
    }

    public void initWeatherView() {

        swipeRefreshLayout.setRefreshing(true);
        cunt = 0;
        //使用不同的state来判断要解析哪一个地址
        parseAdress = stringNow + weatherId + stringKey;
        beginParseWeather(parseAdress, 1);

        //得到Now的地址之后直接开始启动服务，服务拿着地址然后下载解析数据，显示notification
        beginService(parseAdress);

        parseAdress = stringForecast + weatherId + stringKey;
        beginParseWeather(parseAdress, 2);

        parseAdress = stringAri + weatherId + stringKey;
        beginParseWeather(parseAdress, 3);

        parseAdress = stringLife + weatherId + stringKey;
        beginParseWeather(parseAdress, 4);


    }

    private void beginService(String parseAdress) {
        Intent intentService = new Intent(MainActivity.this, MyService.class);
        intentService.putExtra("state", 0);
        intentService.putExtra("parseAdress", parseAdress);
        startService(intentService);
    }

    //设置数据
    public void beginParseWeather(final String parseAdress, final int state) {
        HttpUtil.sendOkHttpRequest(parseAdress, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                finishState++;
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                String result = response.body().string();
                //设置Now的天气参数
                if (state == 1) {
                    final Now now = UtilJson.parseNowWeather(result);
                    if (now != null) {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (dingweiState == 1) {
                                    System.out.println("dingweiState == 1111");
                                    dingweiState = 0;
                                    weatherId_2 = now.getHeWeather6().get(0).getBasic().getParent_city();
                                    weatherId = now.getHeWeather6().get(0).getBasic().getCid();
                                    handler.sendEmptyMessage(10);
                                    return;
                                }

                                loadingNow.setVisibility(View.GONE);
                                nowLayout.setVisibility(View.VISIBLE);
                                String toolLac = now.getHeWeather6().get(0).getBasic().getLocation();
                                String toolW = now.getHeWeather6().get(0).getNow().getTmp() + "℃";
                                String toolT = now.getHeWeather6().get(0).getNow().getCond_txt();

                                nowLocation.setText(toolLac);
                                nowWendu.setText(toolW);
                                nowTianqi.setText(toolT);
                                toolLacation = toolLac;
                                toolWendu = toolW;
                                toolTiqian = toolT;

                                finishState++;

                                handler.sendEmptyMessage(finishState);
                            }
                        });
                    } else if (now == null) {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadingNow.setText("加载失败");
                                System.out.println(parseAdress);
                                finishState++;
                                handler.sendEmptyMessage(finishState);
                            }
                        });
                    }

                    //设置Forecase参数
                } else if (state == 2) {
                    final Forecast forecast = UtilJson.parseForecaseWeather(result);
                    if (forecast != null) {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MyForecaseAdapter adapter = new MyForecaseAdapter(forecast.getHeWeather6().get(0).getDaily_forecast(), MainActivity.this);
                                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
                                linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                                recyclerViewForecast.setLayoutManager(linearLayoutManager);
                                loadingForecast.setVisibility(View.GONE);
                                forecastrecyclerView.setVisibility(View.VISIBLE);
                                recyclerViewForecast.setAdapter(adapter);

                                finishState++;
                                handler.sendEmptyMessage(finishState);
                            }
                        });
                    } else if (forecast == null) {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadingForecast.setText("加载失败");
                                finishState++;
                                handler.sendEmptyMessage(finishState);
                            }
                        });
                    }
                } else if (state == 3) {
                    final Air air = UtilJson.parseAirWeather(result);
                    if (air != null) {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                airLayout.setVisibility(View.VISIBLE);
                                loadingair.setVisibility(View.GONE);
                                maqi.setText(air.getHeWeather6().get(0).getAir_now_city().getAqi());
                                mpm.setText(air.getHeWeather6().get(0).getAir_now_city().getPm25());

                                finishState++;
                                handler.sendEmptyMessage(finishState);
                            }
                        });
                    } else if (air == null) {
                        cunt++;
                        if (cunt < 2) {
                            String parseAdress3 = stringAri + weatherId_2 + stringKey;
                            beginParseWeather(parseAdress3, 3);
                        } else {
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    loadingair.setText("加载失败");
                                    finishState++;
                                    handler.sendEmptyMessage(finishState);
                                    cunt = 0;
                                }
                            });
                        }
                    }
                } else if (state == 4) {
                    final Life life = UtilJson.parseLifeWeather(result);
                    if (life != null) {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                loadingLife.setVisibility(View.GONE);
                                lifeLayout.setVisibility(View.VISIBLE);
                                String comf = null;
                                String sport = null;
                                String trav = null;
                                String cw = null;
                                for (Life.HeWeather6Bean.LifestyleBean lifestyleBean : life.getHeWeather6().get(0).getLifestyle()) {
                                    if (lifestyleBean.getType().equals("comf")) {
                                        comf = lifestyleBean.getTxt();
                                    } else if (lifestyleBean.getType().equals("sport")) {
                                        sport = lifestyleBean.getTxt();
                                    } else if (lifestyleBean.getType().equals("trav")) {
                                        trav = lifestyleBean.getTxt();
                                    } else if (lifestyleBean.getType().equals("cw")) {
                                        cw = lifestyleBean.getTxt();
                                    }
                                }
                                lifecmf.setText("舒适度：" + comf);
                                lifeSport.setText("运动建议：" + sport);
                                lifeTrav.setText("旅游建议：" + trav);
                                lifCw.setText("洗车建议：" + cw);

                                finishState++;
                                handler.sendEmptyMessage(finishState);
                            }
                        });
                    } else if (life == null) {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loadingLife.setText("加载失败");
                                finishState++;
                                handler.sendEmptyMessage(finishState);
                            }
                        });
                    }
                }
            }
        });

    }

    public void loading() {
        loadingNow.setVisibility(View.VISIBLE);
        nowLayout.setVisibility(View.GONE);
        loadingNow.setText("加载中...");

        loadingForecast.setVisibility(View.VISIBLE);
        forecastrecyclerView.setVisibility(View.GONE);
        loadingForecast.setText("加载中...");

        airLayout.setVisibility(View.GONE);
        loadingair.setVisibility(View.VISIBLE);
        loadingair.setText("加载中...");

        loadingLife.setVisibility(View.VISIBLE);
        lifeLayout.setVisibility(View.GONE);
        loadingLife.setText("加载中...");
    }

    private void initView() {
        nowLocation = (TextView) findViewById(R.id.nowLocation);
        nowWendu = (TextView) findViewById(R.id.nowWendu);
        nowTianqi = (TextView) findViewById(R.id.nowTianqi);
        recyclerViewForecast = (RecyclerView) findViewById(R.id.forecaseRecyclerview);
        loadingForecast = (TextView) findViewById(R.id.loadingForecast);
        maqi = (TextView) findViewById(R.id.aqi);
        mpm = (TextView) findViewById(R.id.pm);
        loadingair = (TextView) findViewById(R.id.loadingair);
        airLayout = (LinearLayout) findViewById(R.id.airLayout);
        loadingNow = (TextView) findViewById(R.id.loadingNow);
        nowLayout = (RelativeLayout) findViewById(R.id.nowLayout);
        lifeLayout = (LinearLayout) findViewById(R.id.lifeLayout);
        lifecmf = (TextView) findViewById(R.id.lifecmf);
        lifCw = (TextView) findViewById(R.id.lifCw);
        lifeSport = (TextView) findViewById(R.id.lifeSport);
        lifeTrav = (TextView) findViewById(R.id.lifeTrav);
        loadingLife = (TextView) findViewById(R.id.loadinglife);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.CollapsingToolbarLayout);
        toolbar = (Toolbar) findViewById(R.id.Toolbar);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.SwipeRefreshLayout);
        appbarlayout = (AppBarLayout) findViewById(R.id.appbarlayout);
        drawerLayout = (DrawerLayout) findViewById(R.id.DrawerLayout);
        imageviewBackground = (ImageView) findViewById(R.id.imageviewBackground);
        forecastrecyclerView = (RecyclerView) findViewById(R.id.forecaseRecyclerview);
        dingwei = (ImageView) findViewById(R.id.dingwei);
    }


    public void sendDingweiStateListener(MyLocationDingweiState myLocationDingweiState) {

    }
}
