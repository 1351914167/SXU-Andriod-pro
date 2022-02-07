package com.zsh.sight.feature;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.zsh.sight.MyApplication;
import com.zsh.sight.R;
import com.zsh.sight.Utils.HttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class NickActivity extends AppCompatActivity {

    private String username;

    private TextInputLayout nick_input;
    private TextInputEditText nick_input_edit;
    private MaterialButton materialButton;
    private ImageView imageView;

    private MyApplication myApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nick);
        myApplication = (MyApplication) getApplication();
        username = myApplication.getUsername();
        init();
    }

    private void init(){
        materialButton = findViewById(R.id.nick_button);
        nick_input = findViewById(R.id.help_input);
        nick_input_edit = findViewById(R.id.nick_edit_text);
        imageView = findViewById(R.id.nick_back);

        materialButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("username", username);
                    jsonObject.put("nickname", nick_input_edit.getText());
                    String data = HttpUtils.getJsonData(jsonObject, "http://121.5.169.147:8000/updateNick");
                    Log.e("###", "in nickActivity " + data);
                    Toast.makeText(NickActivity.this, "更新完成", Toast.LENGTH_LONG).show();
                    finish();
                } catch (JSONException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        imageView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                finish();
            }
        });
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
}