package com.zsh.sight.login;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.Window;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.zsh.sight.R;
import com.zsh.sight.feature.StatusBarUtil;

import java.util.ArrayList;


public class InitActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_init);

        // 如果是第一次打开程序，初始化应用
        if(!exist()){
            initApplication();
        }

        // 展示欢迎动画，延时1秒进入登录界面
        new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                //实现页面跳转
                Intent intent=new Intent(InitActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();

                return false;
            }
        }).sendEmptyMessageDelayed(0,1000);
    }

    // 判断应用是否初始化过
    private boolean exist() {
        final String share_init_info = "init_info";
        SharedPreferences sharedPreferences = getSharedPreferences(share_init_info, MODE_PRIVATE);
        if(!sharedPreferences.getBoolean("if_init",false)){
            SharedPreferences.Editor editor = getSharedPreferences(share_init_info, MODE_PRIVATE).edit();
            editor.putBoolean("if_init",true);
            editor.apply();
            return false;
        }
        return true;
    }

    // 应用初始化
    private void initApplication(){

    }

    @Override  //设置状态栏透明属性
    protected void onStart() {
        StatusBarUtil.transparencyBar(this);
        StatusBarUtil.BlackFontStatusBar(this.getWindow());
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onStart();
        Log.e(TAG, getClass().getSimpleName()+": onStart ");
    }

}