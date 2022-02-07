package com.zsh.sight.login;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.zsh.sight.MyApplication;
import com.zsh.sight.R;
import com.zsh.sight.Utils.HttpUtils;
import com.zsh.sight.feature.StatusBarUtil;
import com.zsh.sight.server.LoginServer;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {
    private Button register_v, register_b;
    private TextInputLayout user_input, nick_input, pass_input, pass_confirm_input;
    private TextInputEditText user_editText, nick_editText, pass_editText, confirm_editText;
    private TextView warn, back;
    private MyApplication myApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_register);
        myApplication = (MyApplication) getApplication();
        // 绑定控件
        initView();
    }

    void initView(){
        register_v = (Button)findViewById(R.id.register_v);
        register_b = (Button)findViewById(R.id.register_b);

        user_input = (TextInputLayout)findViewById(R.id.user_input);
        nick_input = (TextInputLayout)findViewById(R.id.nick_input);
        pass_input = (TextInputLayout)findViewById(R.id.pass_input);
        pass_confirm_input = (TextInputLayout)findViewById(R.id.pass_confirm_input);

        user_editText = (TextInputEditText) findViewById(R.id.user_edit_text);
        nick_editText = (TextInputEditText) findViewById(R.id.nick_edit_text);
        pass_editText = (TextInputEditText) findViewById(R.id.pass_edit_text);
        confirm_editText = (TextInputEditText) findViewById(R.id.pass_confirm_edit_text);


        back = (TextView)findViewById(R.id.back);

        // 注册监听器
        register_v.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0){
                // 获取EditText中用户输入的注册信息
                String account, pwd, nickname, pwd_confirm;
                account = user_editText.getText().toString();
                nickname = nick_editText.getText().toString();
                pwd = pass_editText.getText().toString();
                pwd_confirm = confirm_editText.getText().toString();

                // 进行注册
                boolean register_successfully = false;
                try {
                    register_successfully = register_user(account, nickname, pwd, pwd_confirm, 1);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

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
                account = user_editText.getText().toString();
                nickname = nick_editText.getText().toString();
                pwd = pass_editText.getText().toString();
                pwd_confirm = confirm_editText.getText().toString();

                // 进行注册
                boolean register_successfully = false;
                try {
                    register_successfully = register_user(account, nickname, pwd, pwd_confirm, 0);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.e("register", register_successfully + "");
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
    private boolean register_user(String account, String nickname, String pwd, String pwd_confirm, int blind_volunteer) throws JSONException, InterruptedException {
        int failure_info = 0;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", account);
        String result = HttpUtils.getJsonData(jsonObject, "http://121.5.169.147:8000/userInfo");
        Log.e("####", "result  " + result);
        if(account.length() != 11){
            user_input.setError("用户名长度需为11位");
            pass_confirm_input.setError("");
        }
        else if(pwd.length() < 6){
            user_input.setError("");
            pass_confirm_input.setError("密码长度大于6位");
        }
        else if(nickname.length() < 2){
            nick_input.setError("昵称长度不少于2位");
        }
        else if(!pwd.equals(pwd_confirm)){
            user_input.setError("");
            pass_confirm_input.setError("两次密码输入不一致");
        }
        else if(!result.equals("do not have record\n")){
            pass_confirm_input.setError("");
            user_input.setError("用户名已存在");
        }
        else {
            pass_confirm_input.setError("");
            user_input.setError("");
            nick_input.setError("");
            LoginServer.register(account, nickname, pwd, blind_volunteer);
            myApplication.setUsername(account);
            return true;
        }
        return false;
    }

    @Override  //设置状态栏透明属性
    protected void onStart() {
        StatusBarUtil.transparencyBar(this);
        StatusBarUtil.BlackFontStatusBar(this.getWindow());
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onStart();
        Log.e("register", getClass().getSimpleName()+": onStart ");
    }
}
