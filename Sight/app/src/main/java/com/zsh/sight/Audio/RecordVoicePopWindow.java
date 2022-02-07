package com.zsh.sight.Audio;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.zsh.sight.R;

public class RecordVoicePopWindow extends PopupWindow {
    private View mContentView;
    TextView mTvRcStatus;
    ImageView mIvRcStatus;

    public RecordVoicePopWindow(Context context) {
        super(context);
        mContentView = LayoutInflater.from(context).inflate(R.layout.pop_record_voice, null);
        setContentView(mContentView);
        mTvRcStatus = mContentView.findViewById(R.id.tv_rc_status);
        mIvRcStatus = mContentView.findViewById(R.id.iv_rc_status);
        // 设置SelectPicPopupWindow弹出窗体的宽
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        // 设置SelectPicPopupWindow弹出窗体的高
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        // 设置SelectPicPopupWindow弹出窗体可点击
        setFocusable(true);
        setOutsideTouchable(false);
        setTouchable(false);
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0x00000000);
        // 设置SelectPicPopupWindow弹出窗体的背景
        setBackgroundDrawable(dw);
    }


    /**
     * 正常录制
     */
    public void showRecordingTipView() {
        mIvRcStatus.setVisibility(View.VISIBLE);
        mIvRcStatus.setImageResource(R.mipmap.community_record_volume_microphone);
        mTvRcStatus.setVisibility(View.VISIBLE);
        mTvRcStatus.setText("手指上划，取消发送");
    }

    /**
     * 录制时间太短
     */
    public void showRecordTooShortTipView() {
        mIvRcStatus.setVisibility(View.VISIBLE);
        mIvRcStatus.setImageResource(R.mipmap.community_record_volume_warning);
        mTvRcStatus.setText("录制时间太短");
    }

    /**
     * 松开手指，取消发送
     */
    public void showCancelTipView() {
        mIvRcStatus.setVisibility(View.VISIBLE);
        mIvRcStatus.setImageResource(R.mipmap.community_record_volume_cancel);
        mTvRcStatus.setVisibility(View.VISIBLE);
        mTvRcStatus.setText("松开手指，取消发送");
    }

}
