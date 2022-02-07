package com.zsh.sight.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.zsh.sight.MyApplication;
import com.zsh.sight.R;
import com.zsh.sight.Utils.HttpUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

public class NewCodeActivity extends AppCompatActivity {

    private Button bt_confirm;
    private TextInputEditText new_password_editText, confirm_editText;
    private TextInputLayout new_password_input, confirm_input;
    private MyApplication myApplication;
    private String username;
    private String url = "http://121.5.169.147:8000/updatePassword";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_code);
        // 读取账户
        myApplication = (MyApplication)getApplication();
        username = myApplication.getUsername();
        // 初始化组件
        initView();
    }

    private void initView(){
        bt_confirm = (Button) findViewById(R.id.next_step);
        new_password_editText = (TextInputEditText) findViewById(R.id.new_edit_text);
        new_password_input = (TextInputLayout) findViewById(R.id.new_password_input);
        confirm_editText = (TextInputEditText) findViewById(R.id.confirm_edit_text);
        confirm_input = (TextInputLayout) findViewById(R.id.confirm_input);
        bt_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String new_password = new_password_editText.getText().toString();
                String con_password = confirm_editText.getText().toString();
                if(!new_password.equals(con_password)){
                    confirm_input.setError("两次输入不一致");
                }
                else{
                    confirm_input.setError("");
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("username", username);
                        jsonObject.put("password", con_password);
                        String data = HttpUtils.getJsonData(jsonObject, url);
                        Log.e("###", "in update password" + data);
                        Toast.makeText(NewCodeActivity.this, "更新完成", Toast.LENGTH_LONG).show();
                        finish();
                    } catch (JSONException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

}