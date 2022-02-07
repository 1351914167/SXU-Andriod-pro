package com.zsh.sight.feature;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.zsh.sight.MyApplication;
import com.zsh.sight.R;
import com.zsh.sight.Utils.HttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class PathActivity extends AppCompatActivity {

    private String username;

    private TextInputLayout help_input;
    private TextInputEditText help_input_edit;
    private MaterialButton materialButton;
    private ImageView imageView;

    private MyApplication myApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path);
        myApplication = (MyApplication) getApplication();
        username = myApplication.getUsername();
        init();
    }

    private void init(){
        materialButton = findViewById(R.id.help_button);
        help_input = findViewById(R.id.help_input);
        help_input_edit = findViewById(R.id.help_edit_text);
        imageView = findViewById(R.id.path_back);

        materialButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("username", username);
                    jsonObject.put("helpname", help_input_edit.getText());
                    String data = HttpUtils.getJsonData(jsonObject, "http://121.5.169.147:8000/uploadCare");
                    if(data.equals("100")){
                        help_input.setError("请输入正确的用户名");
                    }
                    else{
                        help_input.setError(null);
                        finish();
                    }
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