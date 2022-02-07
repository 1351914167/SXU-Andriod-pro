package com.zsh.sight.adapter;

import static androidx.camera.core.CameraX.getContext;
import static com.zsh.sight.Utils.pxUtil.dip2px;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.bumptech.glide.Glide;
import com.zsh.sight.R;
import com.zsh.sight.Utils.CornerTransform;
import com.zsh.sight.feature.TrunkActivity;
import com.zsh.sight.feature.gridview.GridViewAdapter;
import com.zsh.sight.feature.gridview.MyGridView;
import com.zsh.sight.feature.gridview.PlusImageActivity;

import java.util.ArrayList;
import java.util.List;

public class ShareAdapter extends android.widget.BaseAdapter {

    private TrunkActivity mActivity;
    private Context mContext;
    private List<Diary> diaryList;
    private LayoutInflater inflater;

    public ShareAdapter(TrunkActivity mActivity, Context mContext, List<Diary> diaryList) {
        this.mActivity = mActivity;
        this.mContext = mContext;
        this.diaryList = diaryList;
        inflater = LayoutInflater.from(mContext);
    }

    @SuppressLint("ViewHolder")
    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder = new ViewHolder();
        convertView = inflater.inflate(R.layout.diary_item, parent,false);
        viewHolder.iv_head = (ImageView) convertView.findViewById(R.id.head_diary);

        // 设置昵称
        viewHolder.tv_nickname = (TextView) convertView.findViewById(R.id.diary_username);
        viewHolder.tv_nickname.setText(diaryList.get(position).getNickname());

        // 设置头像
        String headUrl = diaryList.get(position).getHeadPath(); //图片路径
        CornerTransform transformation = new CornerTransform(mContext,dip2px(mContext, 20));
        transformation.setExceptCorner(false, true, true, false);
        Glide.with(mContext).load(headUrl)
                .transform(transformation)
                .into(viewHolder.iv_head);

        // 设置图片
        viewHolder.gridView = (MyGridView) convertView.findViewById(R.id.diary_gridView);
        GridViewAdapter gridViewAdapter = new GridViewAdapter(mContext, diaryList.get(position).getUrlList(), diaryList.get(position).getImgNum());
        viewHolder.gridView.setAdapter(gridViewAdapter);

        // 图片点击响应事件
        viewHolder.gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int grid_position, long id) {
                viewPluImg(grid_position, diaryList.get(position).getUrlArrayList());
            }
        });

        // 设置内容
        viewHolder.tv_contend = (TextView) convertView.findViewById(R.id.diary_content);
        viewHolder.tv_contend.setText(diaryList.get(position).getContend());

        return convertView;
    }

    //查看大图
    public static final int REQUEST_CODE_MAIN = 10; //请求码

    // 放大查看图片
    private void viewPluImg(int position, ArrayList<String> mPicList) {
        Intent intent = new Intent(mContext, PlusImageActivity.class);
        intent.putStringArrayListExtra("img_list", mPicList);
        intent.putExtra("position", position);
        intent.putExtra("if_show_delete", false);
        mActivity.startActivityForResult(intent, REQUEST_CODE_MAIN);
    }

    @Override
    public Object getItem(int position) {
        return diaryList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return diaryList.size();
    }

    private class ViewHolder {
        ImageView iv_head;
        MyGridView gridView;
        TextView tv_nickname, tv_contend;
    }

}