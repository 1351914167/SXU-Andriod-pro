package com.zsh.sight.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.zsh.sight.R;

import java.util.List;

public class ShareAdapter extends ArrayAdapter {

    private final int ImageId;
    public ShareAdapter(Context context, int headImage, List<Need> obj){
        super(context,headImage,obj);
        ImageId = headImage;//这个是传入我们自己定义的界面

    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Need myBean = (Need) getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(ImageId,null);//这个是实例化一个我们自己写的界面Item
        LinearLayout linearLayout = view.findViewById(R.id.dialog);
        ImageView headImage = view.findViewById(R.id.user_image);
        TextView headText = view.findViewById(R.id.user_name);
        headImage.setImageResource(myBean.getImageID());
        headText.setText(myBean.getText());
        return view;
    }
}