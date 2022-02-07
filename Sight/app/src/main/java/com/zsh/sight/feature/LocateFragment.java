package com.zsh.sight.feature;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.luck.picture.lib.broadcast.BroadcastManager;
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
import com.tencent.tencentmap.mapsdk.maps.model.Circle;
import com.tencent.tencentmap.mapsdk.maps.model.CircleOptions;
import com.tencent.tencentmap.mapsdk.maps.model.LatLng;
import com.tencent.tencentmap.mapsdk.maps.model.LatLngBounds;
import com.tencent.tencentmap.mapsdk.maps.model.Marker;
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions;
import com.tencent.tencentmap.mapsdk.maps.model.Polyline;
import com.tencent.tencentmap.mapsdk.maps.model.PolylineOptions;
import com.zsh.sight.MyApplication;
import com.zsh.sight.R;
import com.zsh.sight.Utils.HttpUtils;
import com.zsh.sight.Utils.NavigationIconClickListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class LocateFragment extends Fragment implements TencentLocationListener {
    private View view;
    private TrunkActivity mActivity;
    private MyApplication myApplication;
    private String username;
    private FloatingActionButton bt_set;
    private FloatingActionButton bt_showPath;
    private FloatingActionButton bt_refresh;
    private TextView show_status;
    //地图与定位
    private TencentLocationManager tencentLocationManager;
    private TencentLocationRequest tencentLocationRequest;
    private MapView mapView;
    private TencentMap tencentMap;
    private TencentLocation location;
    // 用于记录定位参数, 以显示到 UI
    private String mRequestParams;
    private Marker mLocationMarker;
    private Circle mAccuracyCircle;
    private Polyline polyline;
    private boolean hasPoly = false;
    private boolean isPoint = true;
    private IntentFilter intentFilter;
    private BroadcastReceiver mReceiver;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mActivity = (TrunkActivity)getActivity();
        myApplication = (MyApplication) mActivity.getApplication();
        username = myApplication.getUsername();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_locate, container, false);
        init(view);

        return view;
    }
    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", username);
            String data = HttpUtils.getJsonData(jsonObject, "http://121.5.169.147:8000/getCare");
            Log.e("####", "data " + data + " show");
            if(data.equals("100")){
                Toast toast = Toast.makeText(mActivity.getBaseContext(), "请绑定被监护人", Toast.LENGTH_LONG);
                toast.show();
                setShow_status(1);
            }
            else{
                setShow_status(2);
            }
        } catch (JSONException | InterruptedException e) {
            e.printStackTrace();
        }
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.CART_BROADCAST");
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent){
                Log.e("####", "on receive");
                setShow_status(3);
            }
        };
        mActivity.registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        mapView.onStop();
        super.onStop();
        mActivity.unregisterReceiver(mReceiver);
    }
    void init(View view){
        bt_set = view.findViewById(R.id.set);
        bt_showPath = view.findViewById(R.id.show_path);
        bt_refresh = view.findViewById(R.id.refresh);
        show_status = view.findViewById(R.id.show_status);
        tencentLocationManager = TencentLocationManager.getInstance(getContext());
        tencentLocationRequest = TencentLocationRequest.create();
        //setUpToolbar(view);
        tencentLocationManager.requestLocationUpdates(tencentLocationRequest, this);
        mapView = view.findViewById(R.id.map_view);
        tencentMap = mapView.getMap();
        tencentMap.setMapStyle(TencentMap.MAP_TYPE_NORMAL);
        CameraUpdate cameraUpdate = CameraUpdateFactory.zoomTo(15);
        tencentMap.moveCamera(cameraUpdate);
        bt_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPoint){
                    bt_showPath.setVisibility(View.VISIBLE);
                    bt_refresh.setVisibility(View.VISIBLE);
                    bt_set.setImageResource(R.drawable.ic_close);
                    isPoint = false;
                }
                else{
                    bt_showPath.setVisibility(View.GONE);
                    bt_refresh.setVisibility(View.GONE);
                    bt_set.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24);
                    isPoint = true;
                }
                view.invalidate();
            }
        });
        bt_showPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("username", username);
                    String data = HttpUtils.getJsonData(jsonObject, "http://121.5.169.147:8000/getCare");
                    if(data.equals("100")){
                        Toast toast = Toast.makeText(mActivity.getBaseContext(), "请绑定被监护人", Toast.LENGTH_LONG);
                        toast.show();
                    }
                    else{
                        DrawLine(data);
                    }
                } catch (JSONException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        bt_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hasPoly){
                    polyline.remove();
                    hasPoly = false;
                }
                tencentLocationManager.requestLocationUpdates(tencentLocationRequest, LocateFragment.this);
            }
        });

    }

    public void setShow_status(int status){
        switch (status){
            case 1:
                show_status.setText("未绑定");
                break;
            case 2:
                show_status.setText("正常");
                show_status.setTextColor(getResources().getColor(R.color.green));
                break;
            case 3:
                show_status.setText("异常");
                show_status.setTextColor(getResources().getColor(R.color.red));
                break;
        }
        view.invalidate();
    }


    //导航栏控制
    private void setUpToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.app_bar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
        }

        toolbar.setNavigationOnClickListener(new NavigationIconClickListener(
                getContext(),
                view.findViewById(R.id.map_view),
                new AccelerateDecelerateInterpolator(),
                getContext().getResources().getDrawable(R.drawable.shr_menu),
                getContext().getResources().getDrawable(R.drawable.ic_close)));
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.shr_toolbar_menu, menu);
        super.onCreateOptionsMenu(menu, menuInflater);
    }


    @Override
    public void onLocationChanged(TencentLocation tencentLocation, int i, String s) {
        if(i == TencentLocation.ERROR_OK){
            location = tencentLocation;

            // 定位成功
            StringBuilder sb = new StringBuilder();
            sb.append("定位参数=").append(mRequestParams).append("\n");
            sb.append("(纬度=").append(location.getLatitude()).append(",经度=")
                    .append(location.getLongitude()).append(",精度=")
                    .append(location.getAccuracy()).append("), 来源=")
                    .append(location.getProvider()).append(", 地址=")
                    .append(location.getAddress());
            LatLng latLngLocation = new LatLng(location.getLatitude(), location.getLongitude());

            Log.e("###", "sb" + sb.toString());
            // 更新 location Marker
            if (mLocationMarker == null) {
                mLocationMarker =
                        tencentMap.addMarker(new MarkerOptions().
                                position(latLngLocation).
                                icon(BitmapDescriptorFactory.fromResource(R.drawable.mark_location)));
            } else {
                mLocationMarker.setPosition(latLngLocation);
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latLngLocation);
                tencentMap.moveCamera(cameraUpdate);
            }

            if (mAccuracyCircle == null) {
                mAccuracyCircle = tencentMap.addCircle(new CircleOptions().
                        center(latLngLocation).
                        radius(location.getAccuracy()).
                        fillColor(0x884433ff).
                        strokeColor(0xaa1122ee).
                        strokeWidth(1));
            } else {
                mAccuracyCircle.setCenter(latLngLocation);
                mAccuracyCircle.setRadius(location.getAccuracy());
            }
        }
    }


    @Override
    public void onStatusUpdate(String s, int i, String s1) {

    }

    public void DrawLine(String helpname) throws JSONException, InterruptedException {
        tencentLocationManager.removeUpdates(this);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", helpname.substring(0, helpname.length() - 1));
        String data = HttpUtils.getJsonData(jsonObject, "http://121.5.169.147:8000/getTrace");
        JSONArray jsonArray = new JSONArray(data);
        // 构造折线点串
        List<LatLng> latLngs = new ArrayList<LatLng>();
        for(int i = 0; i < jsonArray.length(); i++){
            JSONObject ll = new JSONObject(jsonArray.get(i).toString());
            latLngs.add(new LatLng(ll.getDouble("latitude"), ll.getDouble("longitude")));
            Log.e("###", latLngs.toString() + "\n");
        }

        if(!hasPoly){
            hasPoly = true;
            // 构造 PolylineOpitons
            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(latLngs)
                    // 折线设置圆形线头
                    .lineCap(true)
                    .color(PolylineOptions.Colors.GRAYBLUE)
                    // 折线宽度为25像素
                    .width(25)
                    // 还可以添加描边颜色
                    .borderColor(0xffff0000)
                    // 描边颜色的宽度，线宽还是 25 像素，不过填充的部分宽度为 `width` - 2 * `borderWidth`
                    .borderWidth(5);

            // 绘制折线
            polyline = tencentMap.addPolyline(polylineOptions);
        }
        else{
            polyline.appendPoints(latLngs);
        }

        // 将地图视野移动到折线所在区域(指定西南坐标和东北坐标)，设置四周填充的像素
        tencentMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                new LatLngBounds.Builder()
                        .include(latLngs).build(),
                100));
    }
}
