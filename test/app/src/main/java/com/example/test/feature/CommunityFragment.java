package com.example.test.feature;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.fragment.app.Fragment;


import com.example.test.MainActivity;
import com.example.test.R;
import com.example.test.adapter.Need;
import com.example.test.adapter.NeedAdapter;
import com.example.test.database.NeedDatabaseHelper;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class CommunityFragment extends Fragment implements AdapterView.OnItemClickListener, NeedAdapter.InnerItemOnclickListener {
    private View view;
    private List<Need> needList = new ArrayList<>();
    private ListView needListView;

    private NeedAdapter needAdapter;
    private NeedDatabaseHelper dbNeed;
    private MainActivity mActivity;
    private ImageView edit, collect;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_community, container, false);
        mActivity = (MainActivity)getActivity();
        dbNeed = new NeedDatabaseHelper(mActivity, "needs",null,1);
        initView();
        refreshNeeds();
        needAdapter.notifyDataSetChanged();
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void itemClick(View v) {

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        // 如果重新在最前端显示，执行刷新操作
        if (!hidden){
            refreshNeeds();
        }
    }

    private void initView(){
        needListView = (ListView) view.findViewById(R.id.need_list);
        needAdapter = new NeedAdapter(needList, mActivity);
        needAdapter.setOnInnerItemOnClickListener(this);
        needListView.setAdapter(needAdapter);
        needListView.setOnItemClickListener(this);
    }

    /* 应该从服务器获得社区数据，现在是通过数据库模拟 ！！！ */
    private void refreshNeeds(){
        needList.clear();
        SharedPreferences debugRecord = mActivity.getSharedPreferences("debugRecord", MODE_PRIVATE);
        if(debugRecord.getBoolean("load_need_database", true)){
            Need need1 = new Need(1,1001,"小明","2021-2-5 12:00","今天天气不错",1);
            Need need2 = new Need(2,1002,"小红","2021-2-5 13:00","我的狗找不到了，地点山是小店区，请帮我找找",0);
            Need need3 = new Need(3,1003,"小兰","2021-2-5 14:00","谁能帮我识别一下验证码呢？",1);
            Need need4 = new Need(4,1004,"小年","2021-2-5 15:00","君不见黄河之水天上来\n奔流到海不复回。",0);
            dbNeed.insertNeed(need1);
            dbNeed.insertNeed(need2);
            dbNeed.insertNeed(need3);
            dbNeed.insertNeed(need4);
            SharedPreferences.Editor editor = debugRecord.edit();
            editor.putBoolean("load_need_database", false);
            editor.apply();
        }
        List<Need> tempList = dbNeed.readNeeds();
        needList.addAll(tempList);
        needAdapter.notifyDataSetChanged();
    }
}
