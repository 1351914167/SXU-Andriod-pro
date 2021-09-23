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


public class EquipFragment extends Fragment {
    private View view;
    private MainActivity mActivity;
    private TextView link, setting;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_equip, container, false);
        mActivity = (MainActivity)getActivity();
        initView();
        return view;
    }

    // 初始化组件
    private void initView(){
        link = view.findViewById(R.id.link);
        setting = view.findViewById(R.id.setting);

        setting.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Toast.makeText(mActivity, "设备管理", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
