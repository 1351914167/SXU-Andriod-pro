package com.zsh.sight.server;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Telephony;
import android.util.Log;

import androidx.annotation.Nullable;

import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.tencentmap.mapsdk.maps.model.MyLocationStyle;
import com.zsh.sight.MyApplication;
import com.zsh.sight.R;
import com.zsh.sight.Utils.HttpUtils;
import com.zsh.sight.feature.LocateFragment;
import com.zsh.sight.feature.TrunkActivity;
import com.zsh.sight.feature.VideoActivity;
import com.zsh.sight.login.InitActivity;
import com.zsh.sight.login.LoginActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HelpService extends Service {
    NotificationManager notificationManager;
    MyApplication myApplication;
    String username;
    String protect;
    Boolean flag = true;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MyThread myThread = new MyThread();
        myThread.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate(){
        super.onCreate();
        NotificationChannel notificationChannel = new NotificationChannel("id Ex", "听听视界", NotificationManager.IMPORTANCE_HIGH);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);
        myApplication = (MyApplication)getApplication();
        username = myApplication.getUsername();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", username);
            protect = HttpUtils.getJsonData(jsonObject, "http://121.5.169.147:8000/getCare");
        } catch (JSONException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public class MyThread extends Thread {
        @Override
        public void run() {
            JSONObject jsonObject = new JSONObject();
            while (flag){
                try {
                    jsonObject.put("username", protect.substring(0, protect.length() - 1));
                    String data = HttpUtils.getJsonData(jsonObject, "http://121.5.169.147:8000/getUsual");
                    Log.e("####", data + "in help");
                    if(!data.equals("error\n")){
                        flag = false;
                    }
                    if(!flag){
                        showExceptionNotification(data);
                        Intent intent = new Intent();
                        intent.setAction("android.intent.action.CART_BROADCAST");
                        sendBroadcast(intent);
                        flag = true;
                    }
                    Thread.sleep(10000);
                } catch (JSONException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void showExceptionNotification(String data) {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.start_location);

        Intent intent = new Intent(this, TrunkActivity.class);
        intent.putExtra("loc", data);

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                intent, 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.icon_small)  // the status icon
                .setTicker("被监护人的位置出现异常")  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle("被监护人的位置出现异常")  // the label of the entry
                .setContentText("被监护人的位置出现异常")  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .setAutoCancel(true)
                .setChannelId("id Ex")
                .build();

        // Send the notification.
        startForeground(R.string.location_exception, notification);
    }
}