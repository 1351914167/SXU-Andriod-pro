package com.zsh.sight.login;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.zsh.sight.R;
import com.zsh.sight.feature.TrunkActivity;
import com.zsh.sight.shared.User;
import com.zsh.sight.shared.UserInfo;

public class LoginActivity extends AppCompatActivity{
    // 组件
    private Button bt_login;
    private TextInputEditText passwordEditText, loginEditText;
    private TextInputLayout password_input, account_input;
    private EditText et_account, et_password;
    private TextView tv_warn, tv_forget, tv_register;
    private MaterialButton loginButton;

    private final String share_login_info = "login_info";

    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,  // 读外存权限
            Manifest.permission.WRITE_EXTERNAL_STORAGE, // 读外存权限
            Manifest.permission.CAMERA,                 // 相机权限

            Manifest.permission.ACCESS_NETWORK_STATE,   // 网络权限
            Manifest.permission.INTERNET,               // 互联网权限

            Manifest.permission.ACCESS_COARSE_LOCATION, // 定位权限
            Manifest.permission.ACCESS_FINE_LOCATION,   // 定位权限
            Manifest.permission.ACCESS_WIFI_STATE,      // wifi权限
    };
    //用户名合法判断
    private boolean isAccountValid(@Nullable Editable login, Editable password){
        return  (login.toString().equals("123") && password.toString().equals("123456"));
    }
    //密码合法判断
    private boolean isPasswordValid(@Nullable Editable text){
        return text != null && text.length() >= 8;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        // 动态申请权限
        requestPermission();

        // 初始化控件
        initView();

        // 判断自动登录
        SharedPreferences userInfo = getSharedPreferences(share_login_info, MODE_PRIVATE);

        // 自动输入上次登录的账户
        loginEditText.setText(userInfo.getString("last_account", ""));
        passwordEditText.setText(userInfo.getString("last_password", ""));
        Editable login = loginEditText.getText();
        Editable password = passwordEditText.getText();
        // 自动登录
        if(isAccountValid(login, password)) {
            login(login.toString(), password.toString());
        }

    }

    // 动态申请权限
    private void requestPermission(){
        boolean isPermissionRequested = true;
        for (String perm : PERMISSIONS) {
            if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(perm)) {
                isPermissionRequested = false;
                break;
            }
        }
        if(!isPermissionRequested){
            ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
        }
    }

    // 初始化组件
    private void initView(){
        tv_forget = (TextView) findViewById(R.id.forget_pwd);
        tv_register = (TextView) findViewById(R.id.register);
        loginEditText = (TextInputEditText) findViewById(R.id.account_edit_text);
        passwordEditText = (TextInputEditText)findViewById(R.id.password_edit_text);
        password_input = (TextInputLayout) findViewById(R.id.password_input);
        account_input = (TextInputLayout) findViewById(R.id.account_input);
        loginButton = (MaterialButton) findViewById(R.id.next_button);

        // Clear the error once more than 8 characters are typed.
        passwordEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (isPasswordValid(passwordEditText.getText())) {
                    password_input.setError(null); //Clear the error
                }
                return false;
            }
        });


        //登录确认
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isAccountValid(loginEditText.getText(), passwordEditText.getText())){
                    account_input.setError(getString(R.string.account_error));
                }else{
                    account_input.setError(null);
                    login(loginEditText.getText().toString(),
                            passwordEditText.getText().toString());
                }
            }
        });

        // 忘记密码
        tv_forget.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0){
                Intent intentEnter = new Intent(LoginActivity.this, RetrieveActivity.class);
                startActivity(intentEnter);
            }
        });
        // 注册账号
        tv_register.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0){
                Intent intentRegister = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intentRegister);
            }
        });
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        tv_warn.setText("");
        et_password.setText("");
        if(intent != null){
            //判断是否退出应用程序
            boolean isExit = intent.getBooleanExtra("exit",false);
            if(isExit){
                finish();
            }
        }
    }

    private void login(String account, String password){
        SharedPreferences loginInfo = getSharedPreferences(share_login_info, MODE_PRIVATE);
        SharedPreferences.Editor editor = loginInfo.edit();

        // 保存登录账号与密码
        editor.putString("last_account", account);
        editor.putString("last_password", password);
        editor.apply();

        // 从服务器端加载用户数据
        UserInfo userInfo = server_load_account(account, password);

        // 进入首页
        Intent intentEnter=new Intent(LoginActivity.this, TrunkActivity.class);
        intentEnter.putExtra("account", userInfo.getAccount());
        intentEnter.putExtra("type", userInfo.getUserType());
        startActivity(intentEnter);
    }

    private UserInfo server_load_account(String account, String password){
        // 测试账号
        if(account.equals("111")){
            return new UserInfo(account, 0);
        }
        else if(account.equals("222")){
            return new UserInfo(account, 1);
        }

        // 服务器端加载
        int userType = 0;
        UserInfo userInfo = new UserInfo(account, userType);

        return userInfo;
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
}