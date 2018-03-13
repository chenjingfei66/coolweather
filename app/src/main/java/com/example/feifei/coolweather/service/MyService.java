package com.example.feifei.coolweather.service;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.widget.RemoteViews;

import com.example.feifei.coolweather.R;
import com.example.feifei.coolweather.gson.Now;
import com.example.feifei.coolweather.util.HttpUtil;
import com.example.feifei.coolweather.util.UtilJson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.app.NotificationManager.IMPORTANCE_HIGH;

public class MyService extends Service {


    String location= "";
    String tianqi= "";
    String wendu= "";
    String parseAdress;
    String parseAdress_2;
    Notification notification;
    int state = 0;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        System.out.println("flags---" + flags);
        System.out.println("startId---" + startId);
        if (intent != null) {
            parseAdress_2 = intent.getStringExtra("parseAdress");
            state = intent.getIntExtra("state", -1);
        }
        if (state == 0) {
            parseAdress = parseAdress_2;
        }


        if (parseAdress != null) {
            HttpUtil.sendOkHttpRequest(parseAdress, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    System.out.println("onFailure ---" + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String result = response.body().string();
                    final Now now = UtilJson.parseNowWeather(result);
                    if (now != null) {
                        location = now.getHeWeather6().get(0).getBasic().getLocation();
                        tianqi = now.getHeWeather6().get(0).getNow().getCond_txt();
                        wendu = now.getHeWeather6().get(0).getNow().getTmp() + "℃";

                        if (Build.VERSION.SDK_INT >= 26) {
                            after26();
                        } else if (Build.VERSION.SDK_INT < 26) {
                            before26();
                        }
                    } else {   //如果now为空

                    }
                }
            });
        }

        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int hour = 20 * 60* 1000;
        long time = System.currentTimeMillis() + hour;
        Intent intent1 = new Intent(this, MyService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent1, 0);
        manager.cancel(pendingIntent);
        if (Build.VERSION.SDK_INT < 19) {
            manager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        } else {
            manager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void before26() {
        NotificationManager manager = (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.mynotification);
        System.out.println("myservie中");
        System.out.println("location--- " + location);
        System.out.println("wendu--- " + wendu);
        System.out.println("tianqi--- " + tianqi);
        contentView.setTextViewText(R.id.notification_location, location);
        contentView.setTextViewText(R.id.notification_wendu, wendu);
        contentView.setTextViewText(R.id.notification_tianqi, tianqi);
        Notification.Builder builder = new Notification.Builder(MyService.this)
                .setPriority(Notification.PRIORITY_MAX)
                .setSmallIcon(R.mipmap.tianqi)
                .setContent(contentView);

        notification = builder.build();
        startForeground(1, notification);
    }

    @SuppressLint("WrongConstant")
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void after26() {

        String channelID = "1";
        String channelName = "channel_name";
        NotificationManager manager = (NotificationManager) getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("1", "Channel1", NotificationManager.IMPORTANCE_DEFAULT);
        channel.enableLights(false); //是否在桌面icon右上角展示小红点
        //channel.setLightColor(Color.GREEN); //小红点颜色
        channel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知
        channel.setSound(null, null);
        channel.enableVibration(false);
        channel.setImportance(NotificationManager.IMPORTANCE_MAX);
        manager.createNotificationChannel(channel);
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.mynotification);
        //  contentView.setTextViewText(R.id.text, "Remote View Title");
        contentView.setTextViewText(R.id.notification_location, location);
        contentView.setTextViewText(R.id.notification_wendu, wendu);
        contentView.setTextViewText(R.id.notification_tianqi, tianqi);

        System.out.println("myservie中");
        System.out.println("location--- " + location);
        System.out.println("wendu--- " + wendu);
        System.out.println("tianqi--- " + tianqi);

        int notificationId = 1;
        Notification.Builder builder = new Notification.Builder(MyService.this, "1"); //与channelId对应
                                                                                             //这里的channelId与上面的channel对应，就把Notification和channel关联在了一起
        //icon title text必须包含，不然影响桌面图标小红点的展示
        builder.setSmallIcon(android.R.drawable.stat_notify_chat)
                .setPriority(Notification.PRIORITY_MAX)
                .setSmallIcon(R.mipmap.tianqi)
                .setCustomContentView(contentView);
        //           .setNumber(3); //久按桌面图标时允许的此条通知的数量
        //    manager.notify(notificationId, builder.build());
        notification = builder.build();
        startForeground(1, notification);
    }

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
