package com.zsh.sight.recruit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zsh.sight.R;
import com.zsh.sight.feature.StatusBarUtil;

public class ResumeActivity extends AppCompatActivity {

    private String company, position, require, address, contact;
    private int salary, number;

    private TextView tv_position, tv_salary, tv_number;
    private TextView tv_company, tv_address, tv_contact, tv_require;

    private EditText et_name, et_age, et_education, et_contact, et_introduction;
    private CheckBox cb_male, cb_female;

    private Button bt_send;

    private String sex = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_resume);

        // get value
        Intent intent = getIntent();
        company = intent.getStringExtra("company");
        position = intent.getStringExtra("position");
        require = intent.getStringExtra("require");
        address = intent.getStringExtra("address");
        contact = intent.getStringExtra("contact");
        salary = intent.getIntExtra("salary", 0);
        number = intent.getIntExtra("number", 0);

        // init view
        initView();
    }

    // 将审核好的简历发送至服务器
    private void send_resume(String name, String sex, int age, String education, String contact, String introduction){
        Toast.makeText(getApplicationContext(), "简历：" + name, Toast.LENGTH_SHORT).show();

    }

    private void initView(){
        tv_company = (TextView) findViewById(R.id.company);
        tv_position = (TextView) findViewById(R.id.position);
        tv_require = (TextView) findViewById(R.id.require);
        tv_address = (TextView) findViewById(R.id.address);
        tv_contact = (TextView) findViewById(R.id.contact);
        tv_salary = (TextView) findViewById(R.id.salary);
        tv_number = (TextView) findViewById(R.id.number);

        // 设置文本属性
        tv_position.setText(position);
        tv_salary.setText("￥" + salary + "/人");
        tv_number.setText("需要" + number + "人");
        tv_company.setText("公司：" + company);
        tv_address.setText("工作地址：" + address);
        tv_contact.setText("联系方式：" + contact);
        tv_require.setText("岗位描述：" + require);

        et_name = (EditText) findViewById(R.id.name);
        et_age = (EditText) findViewById(R.id.age);
        et_education = (EditText) findViewById(R.id.education);
        et_contact = (EditText) findViewById(R.id.phone);
        et_introduction = (EditText) findViewById(R.id.introduction);

        cb_male = (CheckBox) findViewById(R.id.male);
        cb_female = (CheckBox) findViewById(R.id.female);
        cb_male.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sex = "男";
                cb_male.setChecked(true);
                cb_female.setChecked(false);
            }
        });
        cb_female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sex = "女";
                cb_male.setChecked(false);
                cb_female.setChecked(true);
            }
        });

        bt_send = (Button) findViewById(R.id.send);
        bt_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = et_name.getText().toString();
                String age_s = et_age.getText().toString();
                String education = et_education.getText().toString();
                String contact = et_contact.getText().toString();
                String introduction = et_introduction.getText().toString();

                if(sex.equals("") ||name.equals("") || age_s.equals("") ||
                        education.equals("") || contact.equals("") || introduction.equals("")){
                    Toast.makeText(getApplicationContext(), "请填写完整！", Toast.LENGTH_SHORT).show();
                }
                else{
                    int age = Integer.parseInt(et_age.getText().toString());
                    send_resume(name, sex, age, education, contact, introduction);
                }
            }
        });
    }

    // 设置状态栏透明属性
    @Override
    protected void onStart() {
        StatusBarUtil.transparencyBar(this);
        StatusBarUtil.BlackFontStatusBar(this.getWindow());
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onStart();
    }

}