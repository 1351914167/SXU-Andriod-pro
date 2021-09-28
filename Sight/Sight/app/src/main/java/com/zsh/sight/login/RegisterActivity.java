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
    private EditText et_phone, et_pwd, et_pwd_confirm, et_nickname;
    private String account, pwd, nickname, pwd_confirm;
    private TextView warn, back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_register);

        // 绑定控件
        initView();
    }

//    private Button register_v, register_b;
//    private EditText et_phone, et_pwd, et_pwd_confirm, et_nickname;
//    private String account, pwd, nickname, pwd_confirm;
//    private TextView warn, back;

    void initView(){
        register_v = (Button)findViewById(R.id.register_v);
        register_b = (Button)findViewById(R.id.register_b);

//        et_phone = (EditText)findViewById(R.id.account);

        // 注册监听器
//        register_v.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View arg0){
//                // 获取EditText中用户输入的注册信息
//                account = et1.getText().toString().trim();
//                studentId = et2.getText().toString().trim();
//                name = et3.getText().toString().trim();
//                password = et4.getText().toString().trim();
//                confirm = et5.getText().toString().trim();
//                // 判断是否注册成功
//                if( registerUser() ){
//                    // 显示注册成功界面
////                    setContentView(R.layout.register_successfully);
//                    // 延时2秒返回登录界面
//                    new Handler(new Handler.Callback() {
//                        @Override
//                        public boolean handleMessage(Message msg) {
//                            //实现页面跳转
//                            Intent intent=new Intent(RegisterActivity.this, LoginActivity.class);
//                            startActivity(intent);
//                            finish();
//                            return false;
//                        }
//                    }).sendEmptyMessageDelayed(0,2000);
//                }
//            }
//        });
//        back.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View arg0){
//                finish();
//            }
//        });
    }

    // 进行注册，返回值表示是否成功
//    private boolean registerUser(){
//        if(account.length()!=11){
//            warn.setText(" * 注册失败：请输入正确的手机号！（11位）");
//            return false;
//        }
//        if(studentId.length()!=12){
//            warn.setText(" * 注册失败：请输入正确的学号（12位）！");
//            return false;
//        }
//        if(name.length()<2 || name.length()>7){
//            warn.setText(" * 注册失败：昵称长度不规范(2~7位)！");
//            return false;
//        }
//
//        // 注册信息错误提示
//        if(password.length()<6){
//            warn.setText(" * 注册失败：密码长度不得少于6位！");
//            return false;
//        }
//        if( !password.equals(confirm) ){
//            warn.setText(" * 注册失败：确认密码不匹配！！");
//            return false;
//        }
//        return true;
//    }
}
