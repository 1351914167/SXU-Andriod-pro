package com.zsh.sight.server;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.tencent.lbssearch.TencentSearch;
import com.tencent.lbssearch.httpresponse.BaseObject;
import com.tencent.lbssearch.httpresponse.HttpResponseListener;
import com.tencent.lbssearch.httpresponse.Poi;
import com.tencent.lbssearch.object.param.Geo2AddressParam;
import com.tencent.lbssearch.object.result.Geo2AddressResultObject;
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdate;
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory;
import com.tencent.tencentmap.mapsdk.maps.MapView;
import com.tencent.tencentmap.mapsdk.maps.TencentMap;
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory;
import com.tencent.tencentmap.mapsdk.maps.model.CircleOptions;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions;
import com.zsh.sight.MyApplication;
import com.zsh.sight.R;
import com.zsh.sight.Utils.HttpUtils;
import com.zsh.sight.feature.PathActivity;
import com.zsh.sight.feature.TRTCCActivity;
import com.zsh.sight.feature.TrunkActivity;
import com.zsh.sight.login.InitActivity;
import com.zsh.sight.login.LoginActivity;
import com.zsh.sight.login.RegisterActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.security.auth.login.LoginException;

public class MapService extends Service implements TencentLocationListener {
    private NotificationManager mNM;
    private String username;
    private MyApplication myApplication;
    private double longitude = 1;
    private double latitude = 1;
    private String poi;
    private int need = -1;
    private boolean flag = true;
    private String contact;
    private TencentLocationManager tencentLocationManager;
    private TencentLocationRequest tencentLocationRequest;
    private TencentMap tencentMap;
    private TencentLocation location;
    private String mRequestParams;
    private LatLng latLng;
    private MapView mapView;
    private TrunkActivity trunkActivity;
    private boolean isCreateChannel = false;
    private String title;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return (IBinder) intent;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("###", "in Start Command");
        tencentLocationManager = TencentLocationManager.getInstance(getApplicationContext());
        tencentLocationRequest = TencentLocationRequest.create();
        tencentLocationManager.enableForegroundLocation(2, buildNotification());
        tencentLocationManager.requestLocationUpdates(tencentLocationRequest, this);
        MyThread myThread = new MyThread();
        myThread.start();
        return super.onStartCommand(intent, flags, startId);
    }

    private Notification buildNotification() {
        Notification.Builder builder = null;
        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            //Android O??????Notification?????????????????????????????????targetSDKVersion>=26???????????????????????????????????????
            String channelId = getPackageName();
            if (!isCreateChannel) {NotificationChannel notificationChannel = new NotificationChannel(channelId,
                    "name", NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.enableLights(true);//???????????????icon????????????????????????
                notificationChannel.setLightColor(Color.BLUE); //???????????????
                notificationChannel.setShowBadge(true); //??????????????????????????????????????????????????????
                mNM.createNotificationChannel(notificationChannel);
                isCreateChannel = true;
            }
            builder = new Notification.Builder(getApplicationContext(), channelId);
        } else {
            builder = new Notification.Builder(getApplicationContext());
        }
        builder.setSmallIcon(R.drawable.icon_small)
                .setContentTitle("????????????")
                .setContentText("??????????????????")
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.icon_small))
                .setWhen(System.currentTimeMillis());

        if (android.os.Build.VERSION.SDK_INT >= 16) {
            notification = builder.build();
        } else {
            notification = builder.getNotification();
        }
        return notification;
    }

    @Override
    public void onCreate() {
        Log.e("###", "MapService");
        super.onCreate();
        NotificationChannel notificationChannel1 = new NotificationChannel("id No", "????????????", NotificationManager.IMPORTANCE_HIGH);
        NotificationChannel notificationChannel2 = new NotificationChannel("id Con", "????????????", NotificationManager.IMPORTANCE_HIGH);
        NotificationChannel notificationChannel3 = new NotificationChannel("id Ex", "????????????", NotificationManager.IMPORTANCE_HIGH);
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNM.createNotificationChannel(notificationChannel1);
        mNM.createNotificationChannel(notificationChannel2);
        mNM.createNotificationChannel(notificationChannel3);
        myApplication = (MyApplication) getApplication();
        username = myApplication.getUsername();
    }

    @Override
    public void onLocationChanged(TencentLocation tencentLocation, int i, String s) {
        if(i == TencentLocation.ERROR_OK){
            location = tencentLocation;
            // ????????????
            StringBuilder sb = new StringBuilder();
            sb.append("????????????=").append(mRequestParams).append("\n");
            sb.append("(??????=").append(location.getLatitude()).append(",??????=")
                    .append(location.getLongitude()).append(",??????=")
                    .append(location.getAccuracy()).append("), ??????=")
                    .append(location.getProvider()).append(", ??????=")
                    .append(location.getAddress());
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        }
    }

    @Override
    public void onStatusUpdate(String s, int i, String s1) {

    }

    public class MyThread extends Thread {
        @Override
        public void run() {
            while (!flag) {
                try {
                    if(longitude<0.01 || latitude<0.01 || latLng == null)
                        continue;
                    record();
                    flag = detect();
                    Log.e("###", "flag = " + flag);
                    if(flag) {
                        reGeocoder();
                    }
                    Thread.sleep(10000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.start_location);

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, InitActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.icon_small)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getText(R.string.start_location))  // the label of the entry
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .setChannelId("id No")
                .build();

        // Send the notification.
        startForeground(R.string.start_location, notification);
    }

    public void showExceptionNotification(String name) {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.start_location);

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, TRTCCActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.icon_small)  // the status icon
                .setTicker("????????????????????????")  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle("????????????????????????")  // the label of the entry
                .setContentText("????????????????????????")  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .setAutoCancel(true)
                .setChannelId("id Ex")
                .build();

        // Send the notification.
        startForeground(R.string.location_exception, notification);
    }

    public void showContactNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = "????????????????????????";

        // The PendingIntent to launch our activity if the user selects this notification
        try{
            Log.e("###", "in show");
            PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(), 0,
                    new Intent(this, LoginActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
            // Set the info for the views that show in the notification panel.
            Notification notification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.icon_small)  // the status icon
                    .setTicker(text)  // the status text
                    .setWhen(System.currentTimeMillis())  // the time stamp
                    .setContentTitle("????????????")  // the label of the entry
                    .setContentText(text)  // the contents of the entry
                    .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                    .setChannelId("id Con")
                    .build();

            // Send the notification.
            startForeground(R.string.app_name, notification);
        }
        catch (NullPointerException e){
            e.printStackTrace();
        }


    }



    // ????????????
    public void record() {
        JSONObject user = new JSONObject();
        Calendar c = Calendar.getInstance();
        int weekDay = c.get(Calendar.DAY_OF_WEEK);
        c.setTime(new Date());
        if(longitude<= 1 || latitude <= 1)
            return;
        try {
            user.put("username", username);
            user.put("longitude", longitude);
            user.put("latitude", latitude);
            if (weekDay == 1 || weekDay == 7) {
                user.put("week", 0);
            } else {
                user.put("week", 1);
            }
            Log.e("###", "in record " + user.toString());
            String recStr = HttpUtils.getJsonData(user, "http://121.5.169.147:8000//rec");
            if ("".equals(recStr)) {
                Log.e("??????record???", "??????????????????");
                return;
            }
            JSONObject rec = new JSONObject(recStr);
            boolean res = rec.getBoolean("res");
            if (res)
                Log.i("????????????-????????????", "??????:" + user.toString());
            else
                Log.i("????????????-????????????", "??????" + user.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("?????????", "???????????????");
        }
    }

    public LatLng getLatLng(){
        return this.latLng;
    }

    // ????????????
    public void train() {
        JSONObject json = new JSONObject();
        try {
            json.put("username", username);
            String recStr = HttpUtils.getJsonData(json,"http://121.5.169.147:8000/train");
            if (recStr == null || "".equals(recStr)) {
                Log.e("?????????", "??????????????????");
                return;
            }
            JSONObject rec = new JSONObject(recStr);
            boolean res = rec.getBoolean("res");
            if (res) {
                Log.i("????????????-????????????", "" + rec.getBoolean("isTrain"));
            } else {
                Log.i("????????????-????????????", "????????????");
            }
        } catch (JSONException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // ???????????? true-????????? false-??????
    public boolean detect() {
        JSONObject json = new JSONObject();
        Calendar c = Calendar.getInstance();
        int weekDay = c.get(Calendar.DAY_OF_WEEK);
        c.setTime(new Date());
        try {
            json.put("username", username);
            json.put("longitude", longitude);
            json.put("latitude", latitude);
            if (weekDay == 1 || weekDay == 7) {
                json.put("week", 0);
            } else {
                json.put("week", 1);
            }
            String recStr = HttpUtils.getJsonData(json,"http://121.5.169.147:8000/detect");
            if (recStr == null || "".equals(recStr)) {
                Log.e("??????detect???", "??????????????????");
                return true;
            }
            JSONObject rec = new JSONObject(recStr);
            boolean res = rec.getBoolean("res");
            if (res) {
                Log.i("????????????-????????????", "??????:" + json.toString());
                Log.i("????????????", "" + rec.getBoolean("unUsual"));
                return rec.getBoolean("unUsual");
            } else
                Log.i("????????????-????????????", "??????" + json.toString());
        } catch (JSONException | InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    protected String reGeocoder() {
        if (latLng == null) {
            return null;
        }
        Log.e("####", "in re " + latLng);
        TencentSearch tencentSearch = new TencentSearch(this);
        // ?????????????????????????????????????????????????????????coord_type()??????????????????
        // ????????????????????????poi?????????????????????????????????????????????????????????????????????poi?????????
        Geo2AddressParam geo2AddressParam = new Geo2AddressParam(latLng).getPoi(true)
                .setPoiOptions(new Geo2AddressParam.PoiOptions()
                        .setRadius(1000).setCategorys("??????")
                        .setPolicy(Geo2AddressParam.PoiOptions.POLICY_O2O));
        tencentSearch.geo2address(geo2AddressParam, new HttpResponseListener<BaseObject>() {

            @Override
            public void onSuccess(int arg0, BaseObject arg1) {
                if (arg1 == null) {
                    return;
                }
                Geo2AddressResultObject obj = (Geo2AddressResultObject)arg1;
                title = obj.result.pois.get(0).title;
                Log.e("###", "title + " + title);
                if(title != null && !title.equals("")){
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("username", username);
                        jsonObject.put("Info", title);
                        String data = HttpUtils.getJsonData(jsonObject, "http://121.5.169.147:8000/uploadUsual");
                        Log.e("###", "in thread detect " + data);
                    } catch (JSONException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    showExceptionNotification(username);
                    flag = true;
                }
            }
            @Override
            public void onFailure(int arg0, String arg1, Throwable arg2) {
                Log.e("###", "error code:" + arg0 + ", msg:" + arg1);
            }
        });
        return title;
    }
}
