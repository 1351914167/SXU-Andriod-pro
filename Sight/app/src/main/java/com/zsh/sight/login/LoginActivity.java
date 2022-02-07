package com.zsh.sight.login;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.zsh.sight.MyApplication;
import com.zsh.sight.R;
import com.zsh.sight.Utils.HttpUtils;
import com.zsh.sight.feature.StatusBarUtil;

import com.zsh.sight.feature.TrunkActivity;
import com.zsh.sight.server.LoginServer;
import com.zsh.sight.shared.UserInfo;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity{
    // 组件
    private TextInputEditText passwordEditText, accountEditText;
    private TextInputLayout password_input, account_input;
    private TextView tv_forget, tv_register;
    private MaterialButton loginButton, faceButton;
    private MyApplication myApplication;

    // 常量
    private final String share_login_info = "login_info";

    // 权限
    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,  // 读外存权限
            Manifest.permission.WRITE_EXTERNAL_STORAGE, // 读外存权限
            Manifest.permission.CAMERA,                 // 相机权限

            Manifest.permission.ACCESS_NETWORK_STATE,   // 网络权限
            Manifest.permission.INTERNET,               // 互联网权限

            Manifest.permission.ACCESS_COARSE_LOCATION, // 定位权限
            Manifest.permission.ACCESS_FINE_LOCATION,   // 定位权限
            Manifest.permission.ACCESS_WIFI_STATE,      // wifi权限
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.RECORD_AUDIO
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);
        myApplication = (MyApplication)getApplication();
        // 动态申请权限
        requestPermission();

        // 初始化控件
        initView();
        JSONObject jsonObject = new JSONObject();


        // 判断自动登录
        SharedPreferences userInfo = getSharedPreferences(share_login_info, MODE_PRIVATE);

        // 自动输入上次登录的账户
        accountEditText.setText(userInfo.getString("last_account", ""));
        passwordEditText.setText(userInfo.getString("last_password", ""));

        // 尝试登录
        String account = accountEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        try {
            tryLogin(account, password);
        } catch (JSONException | InterruptedException e) {
            e.printStackTrace();
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
        accountEditText = (TextInputEditText) findViewById(R.id.account_edit_text);
        account_input = (TextInputLayout) findViewById(R.id.account_input);
        passwordEditText = (TextInputEditText)findViewById(R.id.password_edit_text);
        password_input = (TextInputLayout) findViewById(R.id.password_input);
        loginButton = (MaterialButton) findViewById(R.id.next_button);
        faceButton = (MaterialButton) findViewById(R.id.face_button);
        tv_forget = (TextView) findViewById(R.id.forget_pwd);
        tv_register = (TextView) findViewById(R.id.register);

        //登录确认
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String account = accountEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                try {
                    tryLogin(account, password);
                } catch (JSONException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        faceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String account = accountEditText.getText().toString();
                if(account == null || account.equals("")){
                    account_input.setError("人脸检测需输入用户名");
                }
                else{
                    account_input.setError("");
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("username", account);
                        String data = HttpUtils.getJsonData(jsonObject, "http://121.5.169.147:8000/getFace");
                        if(data.equals("time out")){
                            Toast.makeText(LoginActivity.this, "未绑定人脸或网络超时", Toast.LENGTH_LONG).show();
                        }
                        else{
                            Intent intent = new Intent(LoginActivity.this, FaceActivity.class);
                            intent.putExtra("username", account);
                            intent.putExtra("type", 0);
                            startActivityForResult(intent, 0);
                        }
                    } catch (JSONException | InterruptedException e) {
                        e.printStackTrace();
                    }
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

    // 判断输入账号密码是否合法
    private boolean isInputValid(String account, String password){
        if(account.equals("111") || account.equals("333") || account.equals("222")){
            return true;
        }

        boolean accountValid = account.length() == 11 && LoginServer.exist_account(account);
        boolean passwordValid = password.length() >= 6;

        if(accountValid){
            account_input.setError(null);
            if(passwordValid){
                password_input.setError(null);
            }
            else{
                password_input.setError(getString(R.string.password_error));
            }
        }
        else{
            account_input.setError(getString(R.string.account_error));
        }

        return accountValid && passwordValid;
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent != null){
            //判断是否退出应用程序
            if(intent.getBooleanExtra("exit",false)){
                finish();
            }
        }
    }

    // 尝试登录
    private void tryLogin(String account, String password) throws JSONException, InterruptedException {
        // 本地测试账号
        boolean isTestUserV = account.equals("222") && password.equals("123456");
        if(isTestUserV){
            login(account, password);
        }
        // 正常用户身份验证
        else{
            // 服务器端进行身份验证
            if(password == null || password.equals("")){
                password_input.setError("");
            }
            else if(LoginServer.authenticate(account, password)){
                login(account, password);
            }
            else{
                password_input.setError("输入的用户名或密码有误");
            }
        }
    }

    // 登录
    private void login(String account, String password) throws JSONException, InterruptedException {
        SharedPreferences loginInfo = getSharedPreferences(share_login_info, MODE_PRIVATE);
        SharedPreferences.Editor editor = loginInfo.edit();

        // 保存登录账号与密码
        editor.putString("last_account", account);
        editor.putString("last_password", password);
        editor.apply();

        // 从服务器端加载用户数据
        UserInfo userInfo;
        if(account.equals("222")){
            userInfo = new UserInfo(account, 1);
        }
        else{
            userInfo = LoginServer.load_user_info(account);
        }

        myApplication.setUsername(userInfo.getAccount());
        myApplication.setUserType(userInfo.getUserType());

        // 进入首页
        Intent intentEnter=new Intent(LoginActivity.this, TrunkActivity.class);
        intentEnter.putExtra("account", userInfo.getAccount());
        intentEnter.putExtra("type", userInfo.getUserType());
        startActivity(intentEnter);
        finish();
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

    @Override  //设置状态栏透明属性
    protected void onStart() {
        StatusBarUtil.transparencyBar(this);
        StatusBarUtil.BlackFontStatusBar(this.getWindow());
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onStart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0 && resultCode == 1){
            String username = data.getStringExtra("username");
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("username", username);
                String password = HttpUtils.getJsonData(jsonObject,
                        "http://121.5.169.147:8000/login");
                login(username, password.substring(0, password.length() - 1));
            } catch (JSONException | InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

}