package com.example.test.feature;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;


import com.example.test.MainActivity;
import com.example.test.R;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EditFragment extends Fragment {
    private View view;
    private MainActivity mActivity;

    private ImageView back, picture;
    private Button publish;
    private EditText contend;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_edit, container, false);
        mActivity = (MainActivity)getActivity();
        initView();
        return view;
    }

    private void initView(){
        back = view.findViewById(R.id.back);
        picture = view.findViewById(R.id.picture);
        publish = view.findViewById(R.id.publish);
        contend = view.findViewById(R.id.contend);

        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                mActivity.showFragment("CommunityFragment");
            }
        });
        picture.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                addPicture();
            }
        });
        publish.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                publish();
            }
        });
    }

    /* 添加照片!!! */
    private void addPicture(){
        Toast.makeText(mActivity, "添加图片", Toast.LENGTH_SHORT).show();

    }

    public String confirm(){
        return "here is Edit";
    }

    /* 发布动态！！！ */
    private void publish(){
        // 获取信息
        String text = contend.getText().toString().trim();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");// HH:mm:ss
        Date date = new Date(System.currentTimeMillis());
        String time = simpleDateFormat.format(date);

        // 上传服务器

        // 发布完成，退出编辑界面
        Toast.makeText(mActivity, "发布成功："+text+time, Toast.LENGTH_SHORT).show();
        mActivity.showFragment("CommunityFragment");
        contend.setText("");
    }
}
