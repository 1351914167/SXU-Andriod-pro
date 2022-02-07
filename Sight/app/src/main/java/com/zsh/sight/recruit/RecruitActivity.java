package com.zsh.sight.recruit;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.zsh.sight.R;
import com.zsh.sight.feature.StatusBarUtil;

import java.util.ArrayList;
import java.util.List;

public class RecruitActivity extends AppCompatActivity {

    private ListView listView;
    private JobAdapter jobAdapter;
    private List<Job> jobList = new ArrayList<>();
    private TextView tv_diary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_recruit);

        initView();
        initClickEvent();
    }

    void initView(){
        // ListView
        listView = (ListView) findViewById(R.id.listView);
        jobAdapter = new JobAdapter(getBaseContext(), jobList);
        listView.setAdapter(jobAdapter);
        initData();

        // go to diary
        tv_diary = (TextView) findViewById(R.id.diary);
        tv_diary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    private void initClickEvent() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(RecruitActivity.this, ResumeActivity.class);
                intent.putExtra("company", jobList.get(position).getCompany());
                intent.putExtra("position", jobList.get(position).getPosition());
                intent.putExtra("require", jobList.get(position).getRequire());
                intent.putExtra("address", jobList.get(position).getAddress());
                intent.putExtra("contact", jobList.get(position).getContact());
                intent.putExtra("salary", jobList.get(position).getSalary());
                intent.putExtra("number", jobList.get(position).getNumber());
                startActivity(intent);
            }
        });
    }

    // 从服务器拉取招聘信息，加入工作列表
    void initData(){
        //  String company, String position, String require, String address, String contact, int salary, int number
        jobList.add(new Job("山西大学","老师","大学老师","坞城路92号","110",8000,80));
        jobList.add(new Job("山西小学","老师","小学老师","坞城路90号", "110",5000,50));
        jobList.add(new Job("山西小学","老师","小学老师","坞城路90号", "110",5000,50));
        jobList.add(new Job("山西小学","老师","小学老师","坞城路90号", "110",5000,50));
        jobList.add(new Job("山西小学","老师","小学老师","坞城路90号", "110",5000,50));
        jobList.add(new Job("山西小学","老师","小学老师","坞城路90号", "110",5000,50));
        jobList.add(new Job("山西小学","老师","小学老师","坞城路90号", "110",5000,50));
        jobList.add(new Job("山西小学","老师","小学老师","坞城路90号", "110",5000,50));
        jobList.add(new Job("山西小学","老师","小学老师","坞城路90号", "110",5000,50));

        jobAdapter.notifyDataSetChanged();
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