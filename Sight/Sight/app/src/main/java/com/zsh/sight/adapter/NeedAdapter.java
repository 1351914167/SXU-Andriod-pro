package com.zsh.sight.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.zsh.sight.R;

import java.util.List;

public class NeedAdapter extends BaseAdapter implements OnClickListener {
    private List<Need> needList;
    private Context mContext;
    private InnerItemOnclickListener mListener;
    private ViewHolder viewHolder;

    public NeedAdapter(List<Need> needList, Context mContext) {
        this.needList = needList;
        this.mContext = mContext;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.need_item, null);
            initView(convertView);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        setView(position);
        return convertView;
    }
    // 初始化组件
    private void initView(View convertView){
        viewHolder.name = (TextView) convertView.findViewById(R.id.name);
        viewHolder.time = (TextView) convertView.findViewById(R.id.time);
        viewHolder.contend = (TextView) convertView.findViewById(R.id.contend);
    }
    @Override
    public int getCount() {
        // TODO 自动生成的方法存根
        return needList.size();
    }
    @Override
    public Object getItem(int position) {
        // TODO 自动生成的方法存根
        return needList.get(position);
    }
    @Override
    public long getItemId(int position) {
        // TODO 自动生成的方法存根
        return position;
    }
    public final static class ViewHolder {
        TextView name, time, contend;
    }
    // 设置组件
    void setView(int position){
        viewHolder.name.setText(needList.get(position).getName());
        viewHolder.time.setText(needList.get(position).getTime());
        viewHolder.contend.setText(needList.get(position).getContend());
    }
    public interface InnerItemOnclickListener {
        void itemClick(View v);
    }
    public void setOnInnerItemOnClickListener(InnerItemOnclickListener listener){
        this.mListener=listener;
    }
    @Override
    public void onClick(View v) {
        mListener.itemClick(v);
    }
}
