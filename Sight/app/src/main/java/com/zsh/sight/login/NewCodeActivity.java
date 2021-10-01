package com.zsh.sight.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.zsh.sight.R;

public class NewCodeActivity extends AppCompatActivity {

    private Button bt_confirm;
    private EditText et_pwd, et_pwd_confirm;
    private TextView back;
    private String account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_code);

        // 读取账户
        Intent intent = getIntent();
        if(intent != null){
            account = intent.getStringExtra("account");
        }

        // 初始化组件
        initView();
    }

//    private Button bt_confirm;
//    private EditText et_pwd, et_pwd_confirm;
//    private TextView back;
//    private String account;

    private void initView(){
        bt_confirm = (Button) findViewById(R.id.send);

//        et_phone = (EditText) findViewById(R.id.account);
//        et_check = (EditText) findViewById(R.id.check_code);
//        warn = (TextView) findViewById(R.id.warn);

    }

}