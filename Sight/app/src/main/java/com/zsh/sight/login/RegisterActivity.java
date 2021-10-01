package com.zsh.sight.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.zsh.sight.R;

public class RegisterActivity extends AppCompatActivity {
    private Button register_v, register_b;
    private EditText et_account, et_pwd, et_pwd_confirm, et_nickname;
    private TextView warn, back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_register);

        // 绑定控件
        initView();
    }

    void initView(){
        register_v = (Button)findViewById(R.id.register_v);
        register_b = (Button)findViewById(R.id.register_b);

        et_account = (EditText)findViewById(R.id.account);
        et_pwd = (EditText)findViewById(R.id.password);
        et_pwd_confirm = (EditText)findViewById(R.id.password_confirm);
        et_nickname = (EditText)findViewById(R.id.nickname);

        warn = (TextView)findViewById(R.id.warn);
        back = (TextView)findViewById(R.id.back);

        // 注册监听器
        register_v.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0){
                // 获取EditText中用户输入的注册信息
                String account, pwd, nickname, pwd_confirm;
                account = et_account.getText().toString().trim();
                nickname = et_nickname.getText().toString().trim();
                pwd = et_pwd.getText().toString().trim();
                pwd_confirm = et_pwd_confirm.getText().toString().trim();

                // 进行注册
                boolean register_successfully = register_user(account, nickname, pwd, pwd_confirm, 1);

                // 注册 成功跳转界面
                if( register_successfully ){
                    // 显示注册成功界面
                    setContentView(R.layout.register_successfully);
                    // 延时2秒返回登录界面
                    new Handler(new Handler.Callback() {
                        @Override
                        public boolean handleMessage(Message msg) {
                            //实现页面跳转
                            Intent intent=new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                            return false;
                        }
                    }).sendEmptyMessageDelayed(0,1000);
                }
            }
        });

        // 注册监听器
        register_b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0){
                // 获取EditText中用户输入的注册信息
                String account, pwd, nickname, pwd_confirm;
                account = et_account.getText().toString().trim();
                nickname = et_nickname.getText().toString().trim();
                pwd = et_pwd.getText().toString().trim();
                pwd_confirm = et_pwd_confirm.getText().toString().trim();

                // 进行注册
                boolean register_successfully = register_user(account, nickname, pwd, pwd_confirm, 0);

                // 注册 成功跳转界面
                if( register_successfully ){
                    // 显示注册成功界面
                    setContentView(R.layout.register_successfully);
                    // 延时2秒返回登录界面
                    new Handler(new Handler.Callback() {
                        @Override
                        public boolean handleMessage(Message msg) {
                            //实现页面跳转
                            Intent intent=new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                            return false;
                        }
                    }).sendEmptyMessageDelayed(0,1000);
                }
            }
        });

        // 返回键
        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0){
                finish();
            }
        });
    }

    // 进行注册，返回值表示是否成功
    private boolean register_user(String account, String nickname, String pwd, String pwd_confirm, int blind_volunteer){
        int failure_info = 0;
        String[] register_info = {
                "注册成功！",
                "请输入正确的手机号(11位)！",
                "昵称长度不规范(2~10位)！",
                "密码长度不得少于6位！",
                "两次输入密码不匹配！",
                "账号已存在！"
        };

        // 本地检验
        if(account.length() != 11){
            failure_info = 1;
        }
        else if(nickname.length()<2 || nickname.length()>7){
            failure_info = 2;
        }
        else if(pwd.length()<6){
            failure_info = 3;
        }
        else if(!pwd.equals(pwd_confirm)){
            failure_info = 4;
        }
        if(failure_info != 0){
            warn.setText("* 注册失败：" + register_info[failure_info]);
            return false;
        }

        // 服务器端检验
        if(server_exist_account(account)){
            warn.setText("* 注册失败：" + register_info[5]);
            return false;
        }
        else{
            server_register_user(account, nickname, pwd, blind_volunteer);
            return true;
        }
    }

    private boolean server_exist_account(String account){
        return false;
    }

    private void server_register_user(String account, String nickname, String password, int blind_volunteer){

    }

}
