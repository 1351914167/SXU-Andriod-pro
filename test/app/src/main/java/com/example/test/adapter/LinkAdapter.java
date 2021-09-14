package com.example.test.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.test.R;

import java.util.List;

public class LinkAdapter extends BaseAdapter implements OnClickListener {
    private List<Link> linkList;
    private Context mContext;
    private InnerItemOnclickListener mListener;
    private ViewHolder viewHolder;

    public LinkAdapter(List<Link> linkList, Context mContext) {
        this.linkList = linkList;
        this.mContext = mContext;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.link_item, null);
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
    }
    @Override
    public int getCount() {
        // TODO 自动生成的方法存根
        return linkList.size();
    }
    @Override
    public Object getItem(int position) {
        // TODO 自动生成的方法存根
        return linkList.get(position);
    }
    @Override
    public long getItemId(int position) {
        // TODO 自动生成的方法存根
        return position;
    }
    public final static class ViewHolder {
        TextView name;
    }
    // 设置组件
    void setView(int position){
//        viewHolder.confirm.setOnClickListener(this);
//        viewHolder.confirm.setTag(position);
        viewHolder.name.setText(linkList.get(position).getName());
    }
    public interface InnerItemOnclickListener {
        void itemClick(View v);
    }
    public void setOnInnerItemOnClickListener(InnerItemOnclickListener listener){
        this.mListener = listener;
    }
    @Override
    public void onClick(View v) {
        mListener.itemClick(v);
    }
}
