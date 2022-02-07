package com.zsh.sight.login;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.zsh.sight.R;
import com.zsh.sight.feature.StatusBarUtil;
import com.zsh.sight.server.LoginServer;

public class RetrieveActivity extends AppCompatActivity {

    private EditText et_phone, et_check;
    private Button bt_send, bt_next;
    private TextView warn;

    // 验证码计时
    private boolean isSending = false;
    private long startTime = 0;

    private String check_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retrieve);

        initView();

        // 计时
        new Thread(mRunnable).start();
    }

    void initView() {
        bt_send = (Button) findViewById(R.id.send);
        bt_next = (Button) findViewById(R.id.next_step);

        // 发送验证码
        bt_send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0){
                String phone = et_phone.getText().toString().trim();
                // 账号存在，则发送验证码
                if(!isSending){
                    if(LoginServer.exist_account(phone)){
                        check_code = LoginServer.send_auth_code(phone);
                        startTime = System.currentTimeMillis();
                        isSending = true;
                    }
                    else{
                        warn.setText("* 验证失败：手机号不存在！");
                    }
                }
                else{
                    warn.setText("* 验证已发送：请稍候重试！");
                }
            }
        });

        // 下一步
        bt_next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0){
                if(check_code.equals(et_check.getText().toString().trim())){
                    Intent intentRegister = new Intent(RetrieveActivity.this, NewCodeActivity.class);
                    intentRegister.putExtra("account", et_phone.getText().toString().trim());
                    startActivity(intentRegister);
                }
                else{
                    warn.setText("* 验证失败：验证码错误！");
                }
            }
        });
    }

    // 定时调用函数
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(isSending){
                long deltaTime = System.currentTimeMillis() - startTime;
                bt_send.setText("" + (60 - deltaTime / 1000));
                if((60 - deltaTime / 1000) == 0){
                    isSending = false;
                    bt_send.setText("发送");
                }
            }
        }
    };

    //实现定时刷新
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            while(true){
                try {
                    //sleep2秒，可根据需求更换为响应的时间
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mHandler.sendMessage(mHandler.obtainMessage());
            }
        }
    };

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
