package com.zsh.sight.feature;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.zsh.sight.R;
import com.zsh.sight.server.HelpService;
import com.zsh.sight.server.MapService;

public class TrunkActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    NavController navController;
    Context context;
    boolean user_type;
    String pos = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();

        if(intent != null){
            int type = intent.getIntExtra("type", 0);
            user_type = type == 0;
            String loc = intent.getStringExtra("loc");
            if(loc != null && !loc.equals("")){
                pos = loc;
            }
        }
        if(!user_type){
            Log.e("###", "start Service");
            startService(new Intent(this, MapService.class));
        }
        else{
            startService(new Intent(this, HelpService.class));
        }
        setContentView(user_type ? R.layout.activity_trunk: R.layout.activity_trunk2);
        context = this;
        initView();
        if(pos != null && !pos.equals("")){
            confirm_message_box();
        }
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

    private void initView(){
        bottomNavigationView = findViewById(R.id.bottom_nav);
        bottomNavigationView.inflateMenu(user_type ? R.menu.bottom_nav_menu:R.menu.bottom_nav_menu2);
        navController = Navigation.findNavController(this, R.id.fragment_container_view);
        //建立绑定关系
        NavigationUI.setupWithNavController(bottomNavigationView, navController);
    }

    // 连续点击两次返回键退出程序
    private long exitTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            if((System.currentTimeMillis()-exitTime) > 1500){
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // 确认弹窗
    private void confirm_message_box(){
        AlertDialog alertDialog1 = new AlertDialog.Builder(this)
                .setTitle("被监护人位置异常")//标题
                .setMessage("附近标志：" + pos +
                        "\n是否接入视频通话？")//内容
                .setIcon(R.drawable.dog)//图标
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加"Yes"按钮
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(TrunkActivity.this, TRTCCActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {//添加取消
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .create();
        alertDialog1.show();
    }
}