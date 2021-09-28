package com.zsh.sight.login;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.zsh.sight.R;
import com.zsh.sight.feature.TrunkActivity;
import com.zsh.sight.shared.User;
import com.zsh.sight.shared.UserInfo;

public class LoginActivity extends AppCompatActivity{
    private Button bt_enter, bt_register;
    private RadioGroup radioGroup;
    private EditText et_account, et_password;
    private CheckBox cb_save, cb_auto;
    private TextView tv_warn, tv_forget;
    private int user_type = 0;

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
        if(userInfo.getBoolean("auto_login", false)){
            String account = userInfo.getString("last_account", "nan");
            String password = userInfo.getString("last_password", "nan");
            if(authenticate(account, password)){
                Toast.makeText(getApplicationContext(), "自动登录", Toast.LENGTH_SHORT).show();
                login(account, password);
            }
        }

        // 自动输入上次登录的账户
        String account = userInfo.getString("last_account", "");
        et_account.setText(account);

        // 显示上次登录设置的登录状态
        cb_save.setChecked(userInfo.getBoolean("save_login", true));
        cb_auto.setChecked(userInfo.getBoolean("auto_login", false));

        // 如果上次勾选了记住密码，自动输入上次登录密码
        if( userInfo.getBoolean("save_login", false) ){
            String password = userInfo.getString("last_password", "");
            et_password.setText(password);
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

    void initView(){
        bt_enter = (Button)findViewById(R.id.enter);
        bt_register = (Button)findViewById(R.id.register);
        cb_save = (CheckBox) findViewById(R.id.remember);
        cb_auto = (CheckBox) findViewById(R.id.auto_login);
        et_account = (EditText)findViewById(R.id.account);
        et_password = (EditText)findViewById(R.id.password);
        tv_warn = (TextView) findViewById(R.id.warn);
        tv_forget = (TextView) findViewById(R.id.forget);
        radioGroup = (RadioGroup) findViewById(R.id.rd_group);

        // 为部分控件注册监听器
        tv_forget.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0){
                Intent intentEnter = new Intent(LoginActivity.this, RetrieveActivity.class);
                startActivity(intentEnter);
            }
        });
        bt_register.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0){
                Intent intentRegister = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intentRegister);
            }
        });
        cb_auto.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0){
                if(cb_auto.isChecked()){
                    cb_save.setChecked(true);
                }
            }
        });
        bt_enter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0){
                String account = et_account.getText().toString().trim();
                String password = et_password.getText().toString().trim();
                // 如果账号和密码是否匹配，则登录
                if(authenticate(account, password)) {
                    login(account, password);
                }
            }
        });
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (radioGroup.getCheckedRadioButtonId()){
                    case R.id.for_blind: user_type = 1; break;
                    case R.id.for_norm: user_type = 0; break;
                }
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

        // 保存登录状态，获取账户的相关信息
        if(cb_save.isChecked() || cb_auto.isChecked()){
            editor.putString("last_password", password);
        }
        else{
            editor.remove("last_password");
        }
        editor.putString("last_account", account);
        editor.putBoolean("save_login", cb_save.isChecked());
        editor.putBoolean("auto_login", cb_auto.isChecked());
        editor.apply();

        // 从服务器端加载用户数据
        User user = new User(account, password);
        UserInfo userInfo = loadInfo(user);

        // 进入首页
        Intent intentEnter=new Intent(LoginActivity.this, TrunkActivity.class);
        intentEnter.putExtra("account", account);
        intentEnter.putExtra("type", user_type);
        startActivity(intentEnter);
    }

    // 身份验证
    private boolean authenticate(String account, String password){
        String test_account = "123";
        String test_password = "123";

        boolean isOk = account.equals(test_account) && password.equals(test_password);

        return isOk;
    }

    private UserInfo loadInfo(User use){
        UserInfo userInfo = new UserInfo(use.getAccount());

        // 从服务器端加载用户数据

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