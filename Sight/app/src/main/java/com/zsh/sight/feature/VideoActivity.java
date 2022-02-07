package com.zsh.sight.feature;

import static android.content.ContentValues.TAG;

import static androidx.camera.core.CameraX.getContext;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.idlestar.ratingstar.RatingStarView;
import com.zsh.sight.R;
import com.zsh.sight.login.InitActivity;
import com.zsh.sight.login.LoginActivity;

import java.io.File;

public class VideoActivity extends AppCompatActivity {

    private ImageView iv_interrupt, iv_call1, iv_call2;
    private TextView tv_mode;
    private MediaPlayer mediaPlayer;

    private VideoActivity mActivity;

    private boolean isCall1 = true;
    private boolean isCall2 = true;
    private boolean can_enter = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scoring);
        mActivity = this;
        /*initView();

        Intent intent = getIntent();
        String[] show_ask_who = {"正在呼叫", "正在紧急求助", "正在呼叫监护人", "正在呼叫志愿者"};
        if(intent != null){
            int mode = intent.getIntExtra("mode", 0);
            tv_mode.setText(show_ask_who[mode]);
        }

        // 播放音频
        mediaPlayer = new MediaPlayer();
        mediaPlayer = MediaPlayer.create(this, R.raw.call_voice);
        mediaPlayer.setLooping(true);//设置为循环播放
        mediaPlayer.start();

        // 展示欢迎动画，延时1秒进入登录界面
        new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                //实现页面跳转
                if(can_enter){
                    Toast.makeText(getBaseContext(), "已接通", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(VideoActivity.this, TRTCCActivity.class);
                    startActivity(intent);
                }
                finish();
                return false;
            }
        }).sendEmptyMessageDelayed(0,4000);*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        can_enter = false;
        if(mediaPlayer != null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    private void initView() {
/*        iv_interrupt = findViewById(R.id.interrupt);
        tv_mode = findViewById(R.id.ask_who);
        iv_call1 = findViewById(R.id.call1);
        iv_call2 = findViewById(R.id.call2);*/

        iv_interrupt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                can_enter = false;
                finish();
            }
        });

        iv_call1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                if(isCall1){
                    iv_call1.setImageResource(R.drawable.call1_close);
                    mediaPlayer.stop();
                }
                else{
                    iv_call1.setImageResource(R.drawable.call1_open);
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer = MediaPlayer.create(mActivity, R.raw.call_voice);
                    mediaPlayer.setLooping(true);//设置为循环播放
                    mediaPlayer.start();
                }
                isCall1 = !isCall1;
            }
        });

        iv_call2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                if(!isCall2){
                    iv_call2.setImageResource(R.drawable.call2_close);
                }
                else{
                    iv_call2.setImageResource(R.drawable.call2_open);
                }
                isCall2 = !isCall2;
            }
        });
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