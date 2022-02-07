package com.zsh.sight.Audio;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

public class CustomButton extends AppCompatButton {
    private OnVoiceButtonCallBack mOnVoiceButtonCallBack;

    public CustomButton(Context context) {
        super(context);
    }

    public CustomButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.v("onTouchEvent", "开始取消录音");
                if (mOnVoiceButtonCallBack != null) {
                    mOnVoiceButtonCallBack.onStartRecord();
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (isCancelled(this, event)) {
                    Log.v("onTouchEvent", "准备取消录音");
                    if (mOnVoiceButtonCallBack != null) {
                        mOnVoiceButtonCallBack.onWillCancelRecord();
                    }
                } else {
                    Log.v("onTouchEvent", "继续录音");
                    if (mOnVoiceButtonCallBack != null) {
                        mOnVoiceButtonCallBack.onContinueRecord();
                    }
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                Log.v("onTouchEvent", "停止录音");
                if (mOnVoiceButtonCallBack != null) {
                    mOnVoiceButtonCallBack.onStopRecord();
                }
                return true;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 设置监听回调
     *
     * @param onVoiceButtonCallBack
     */
    public void setOnVoiceButtonCallBack(OnVoiceButtonCallBack onVoiceButtonCallBack) {
        this.mOnVoiceButtonCallBack = onVoiceButtonCallBack;
    }

    private boolean isCancelled(View view, MotionEvent event) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        if (event.getRawX() < location[0] || event.getRawX() > location[0] + view.getWidth() || event.getRawY() < location[1] - 40) {
            return true;
        }
        return false;
    }

    public interface OnVoiceButtonCallBack {
        /**
         * 开始录音
         */
        void onStartRecord();

        /**
         * 停止录音
         */
        void onStopRecord();

        /**
         * 准备取消录音
         */
        void onWillCancelRecord();

        /**
         * 继续录音
         */
        void onContinueRecord();
    }
}
