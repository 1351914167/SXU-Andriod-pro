package com.example.test;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.example.test.feature.CommunityFragment;
import com.example.test.feature.EditFragment;
import com.example.test.feature.EquipFragment;
import com.example.test.feature.LinkFragment;
import com.example.test.feature.LocateFragment;
import com.example.test.feature.MineFragment;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Button buttonLocate, buttonCommunity, buttonEquip, buttonMine;
    private Fragment locateFragment, communityFragment, equipFragment, mineFragment, nowFragment;
    private Fragment editFragment, linkFragment;
    private boolean isPermissionRequested;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_activity);
        initView();
        requestPermission();
        loadFragment();
        showFragment(locateFragment);
    }

    //初始化控件
    private void initView() {
        buttonLocate = (Button) findViewById(R.id.locate);
        buttonCommunity = (Button) findViewById(R.id.community);
        buttonEquip = (Button) findViewById(R.id.equip);
        buttonMine = (Button) findViewById(R.id.mine);
        // 底部导航栏的点击相应事件
        buttonLocate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                changeButton(buttonLocate);
                showFragment(locateFragment);
            }
        });
        buttonCommunity.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                changeButton(buttonCommunity);
                showFragment(communityFragment);
            }
        });
        buttonEquip.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                changeButton(buttonEquip);
                showFragment(equipFragment);
            }
        });
        buttonMine.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                changeButton(buttonMine);
                showFragment(mineFragment);
            }
        });
    }

    //加载碎片布局
    private void loadFragment(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        locateFragment = new LocateFragment();
        communityFragment = new CommunityFragment();
        equipFragment = new EquipFragment();
        mineFragment =  new MineFragment();
        editFragment = new EditFragment();
        linkFragment = new LinkFragment();

        transaction.add(R.id.trunk, locateFragment);
        transaction.add(R.id.trunk, communityFragment);
        transaction.add(R.id.trunk, equipFragment);
        transaction.add(R.id.trunk, mineFragment);
        transaction.add(R.id.trunk, editFragment);
        transaction.add(R.id.trunk, linkFragment);

        transaction.hide(locateFragment);
        transaction.hide(communityFragment);
        transaction.hide(equipFragment);
        transaction.hide(mineFragment);
        transaction.hide(editFragment);
        transaction.hide(linkFragment);
        transaction.hide(locateFragment);

        nowFragment = locateFragment;
        transaction.commit();
    }

    // 切换界面
    private void showFragment(Fragment to) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.hide(nowFragment).show(to).commit();
        nowFragment = to;
    }

    public void showFragment(String to) {
        switch(to){
            case "CommunityFragment":
                showFragment(communityFragment);
                break;
            case "EquipFragment":
                showFragment(equipFragment);
                break;
            case "MineFragment":
                showFragment(mineFragment);
                break;
            case "EditFragment":
                showFragment(editFragment);
                break;
            case "LinkFragment":
                showFragment(linkFragment);
                break;
        }
    }

    // 完成导航栏变化
    private void changeButton(Button nowButton){
        buttonLocate.setTextSize(18);
        buttonLocate.setTextColor(Color.parseColor("#545454"));
        buttonCommunity.setTextSize(18);
        buttonCommunity.setTextColor(Color.parseColor("#545454"));
        buttonEquip.setTextSize(18);
        buttonEquip.setTextColor(Color.parseColor("#545454"));
        buttonMine.setTextSize(18);
        buttonMine.setTextColor(Color.parseColor("#545454"));

        nowButton.setTextSize(20);
        nowButton.setTextColor(Color.parseColor("#000000"));
    }

    // 连续点击两次返回键退出程序
    private long exitTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            if(nowFragment == editFragment){
                showFragment(communityFragment);
            }
            else if(nowFragment == linkFragment){
                showFragment(equipFragment);
            }
            else if((System.currentTimeMillis()-exitTime) > 1000){
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            }
            else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Android6.0之后需要动态申请权限
     */
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= 23 && !isPermissionRequested) {
            isPermissionRequested = true;
            ArrayList<String> permissionsList = new ArrayList<>();
            String[] permissions = {
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_WIFI_STATE,
            };

            for (String perm : permissions) {
                if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(perm)) {
                    permissionsList.add(perm);
                    // 进入到这里代表没有权限.
                }
            }

            if (!permissionsList.isEmpty()) {
                String[] strings = new String[permissionsList.size()];
                requestPermissions(permissionsList.toArray(strings), 0);
            }
        }
    }
}