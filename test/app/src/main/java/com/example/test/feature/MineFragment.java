package com.example.test.feature;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.test.MainActivity;
import com.example.test.R;


public class MineFragment extends Fragment {
    private View view;
    private MainActivity mActivity;
    private TextView account, setting;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_mine, container, false);
        mActivity = (MainActivity)getActivity();
        initView();
        return view;
    }

    // 初始化组件
    private void initView(){
        account = view.findViewById(R.id.account);
        setting = view.findViewById(R.id.setting);

        account.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Toast.makeText(mActivity, "我的账户", Toast.LENGTH_SHORT).show();
            }
        });
        setting.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Toast.makeText(mActivity, "设置", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
