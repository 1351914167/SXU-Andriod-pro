package com.zsh.sight.feature;

import static com.zsh.sight.Utils.pxUtil.dip2px;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.zsh.sight.R;
import com.zsh.sight.Utils.CornerTransform;
import com.zsh.sight.Utils.HttpUtils;
import com.zsh.sight.adapter.Diary;
import com.zsh.sight.adapter.ShareAdapter;
import com.zsh.sight.recruit.RecruitActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CommunityFragment extends Fragment implements AdapterView.OnItemClickListener {
    private View view;
    private List<Diary> diaryList = new ArrayList<>();
    private TrunkActivity mActivity;
    private ImageView bt_edit, community_img;
    private TextView tv_job;
    private SwipeRefreshLayout refreshLayout;
    private ShareAdapter adapter;
    private ImageView head_img;
    private String head_path;
    private final String DEFAULT_IMAGE_PATH = "android.resource://" + "com.zsh.sight" + "/";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.community_fragment, container, false);
        mActivity = (TrunkActivity)getActivity();
        ListView listView = (ListView) view.findViewById(R.id.listview);

        try {
            initDiary();
        } catch (InterruptedException | JSONException e) {
            e.printStackTrace();
        }
        initView();
        adapter = new ShareAdapter(mActivity, getContext(), diaryList);
        if(!diaryList.isEmpty()){
            listView.setAdapter(adapter);
        }
        return view;
    }

    private void initDiary() throws InterruptedException, JSONException {
        String url = "http://121.5.169.147:8000/getComm";
        String url_head = "http://121.5.169.147:8000/getHead";
        String url_nickname = "http://121.5.169.147:8000/getNickname";
        String shareInfo = HttpUtils.getJsonData(new JSONObject(), url);
        JSONArray list = new JSONArray(shareInfo);
        for(int j = 0; j < list.length(); j++){
            JSONObject jsonObject = new JSONObject(list.getString(j));
            JSONArray jsonArray = jsonObject.getJSONArray("image");
            List<String> imgPathList = new ArrayList<>();
            for(int i = 0; i < jsonArray.length(); i++){
                imgPathList.add(jsonArray.getString(i));
            }
            String getHead = HttpUtils.getJsonData(jsonObject, url_head);
            JSONObject jsHead = new JSONObject(getHead);
            String getNickname = HttpUtils.getJsonData(jsonObject, url_nickname);
            JSONObject jsNickname = new JSONObject(getNickname);
            Diary diary = new Diary(jsHead.getString("path"),
                    jsNickname.getString("nickname"), jsonObject.getString("comment"),
                    imgPathList);
            diaryList.add(diary);
        }
    }

    // 初始化组件
    private void initView(){
        bt_edit = (ImageView) view.findViewById(R.id.edit);
        head_img = (ImageView) view.findViewById(R.id.head_imgg);
        bt_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentEnter = new Intent(mActivity, EditActivity.class);
                startActivity(intentEnter);
            }
        });
        refreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.refresh_layout);
        /*tv_job = (TextView) view.findViewById(R.id.job);
        tv_job.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 求职界面
                Intent intentEnter = new Intent(mActivity, RecruitActivity.class);
                startActivity(intentEnter);
            }
        });*/
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshDiary();
                Log.e("###", "in refresh done");
            }
        });
        CornerTransform transformation = new CornerTransform(mActivity,dip2px(mActivity, 20));
        transformation.setExceptCorner(false, true, true, false);
        Glide.with(mActivity).load(R.drawable.default_head)
                .transform(transformation)
                .into(head_img);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        // 如果重新在最前端显示，执行刷新操作
        if (!hidden){
            //refreshDiary();
        }
    }

    private void refreshDiary(){
        try {
            diaryList.clear();
            initDiary();
        } catch (InterruptedException | JSONException e) {
            e.printStackTrace();
        }
        adapter.notifyDataSetChanged();
        refreshLayout.setRefreshing(false);
    }
}
