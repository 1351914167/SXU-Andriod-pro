package com.zsh.sight.feature;

import static com.tencent.trtc.TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FIT;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.tencent.liteav.TXLiteAVCode;
import com.tencent.liteav.device.TXDeviceManager;
import com.tencent.rtmp.ui.TXCloudVideoView;
import com.tencent.trtc.TRTCCloud;
import com.tencent.trtc.TRTCCloudDef;
import com.tencent.trtc.TRTCCloudListener;
import com.zsh.sight.MyApplication;
import com.zsh.sight.R;
import com.zsh.sight.Utils.GenerateTestUserSig;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Timer;

public class TRTCCActivity extends AppCompatActivity {
    private TRTCCloud mTRTCCloud;
    private TXDeviceManager mTXDeviceManager;
    private List<String> mRemoteUidList;
    private TXCloudVideoView mTXCVVLocalPreviewView;
    private TXCloudVideoView mRemoteView;
    private ImageView mute, muteRemote, exit;
    private boolean mIsFrontCamera = true;
    private int mUserCount = 0;
    private MyApplication myApplication;
    private String username;
    private String other = null;
    private int userType;
    private Boolean ismute = false, ismuteR = false;
    private String user;
    private int roomID = 3400490;

    private boolean isCall1 = true;
    private boolean isCall2 = true;

    private double startTime;
    private double endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trtccactivity);
        myApplication = (MyApplication) getApplication();
        username = myApplication.getUsername();
        userType = myApplication.getUserType();
        Intent intent = getIntent();
        if(intent != null){
            roomID = intent.getIntExtra("roomID", 3400490);
        }
        startTime = System.currentTimeMillis();
        initView();
        enter();
    }

    private void initView(){
        mute = findViewById(R.id.mute);
        muteRemote = findViewById(R.id.muteRemote);
        exit = findViewById(R.id.exit);
        mTXCVVLocalPreviewView = findViewById(R.id.txcvv_my);
        mRemoteView = findViewById(R.id.txcvv_main);


        mute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isCall2){
                    mute.setImageResource(R.drawable.call2_close);
                }
                else{
                    mute.setImageResource(R.drawable.call2_open);
                }
                isCall2 = !isCall2;
                Log.e("###", "Click mute");
                if(!ismute) {
                    mTRTCCloud.muteLocalAudio(true);
                    ismute = true;
                }
                else{
                    mTRTCCloud.muteLocalAudio(false);
                    ismute = false;
                }
            }
        });
        muteRemote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isCall1){
                    muteRemote.setImageResource(R.drawable.call1_close);
                }
                else{
                    muteRemote.setImageResource(R.drawable.call1_open);
                }
                isCall1 = !isCall1;

                Log.e("###", "Click muteRemote");
                if(user != null){
                    if(!ismute) {
                        mTRTCCloud.muteRemoteAudio(user, true);;
                        ismuteR = true;
                    }
                    else{
                        mTRTCCloud.muteRemoteAudio(user, false);
                        ismuteR = false;
                    }
                }

            }
        });
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endTime = System.currentTimeMillis();
                double during = (endTime - startTime) / 1000 / 60;
                if(userType == 1){
                    Intent intent = new Intent(TRTCCActivity.this, ScoringActivity.class);
                    intent.putExtra("during", during);
                    intent.putExtra("other", other);
                    startActivity(intent);
                }
                finish();
            }
        });
    }


    private void enter(){
        mTRTCCloud = TRTCCloud.sharedInstance(getApplicationContext());
        mTRTCCloud.setListener(new TRTCCloudImplListener(this));
        mTXDeviceManager = mTRTCCloud.getDeviceManager();

        TRTCCloudDef.TRTCParams trtcParams = new TRTCCloudDef.TRTCParams();
        trtcParams.sdkAppId = GenerateTestUserSig.SDKAPPID;
        trtcParams.userId = username;
        trtcParams.roomId = roomID;
        trtcParams.userSig = GenerateTestUserSig.genTestUserSig(trtcParams.userId);

        mTRTCCloud.startLocalPreview(mIsFrontCamera, mTXCVVLocalPreviewView);
        mTRTCCloud.startLocalAudio(TRTCCloudDef.TRTC_AUDIO_QUALITY_SPEECH);
        mTRTCCloud.enterRoom(trtcParams, TRTCCloudDef.TRTC_APP_SCENE_VIDEOCALL);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        exitRoom();
    }

    private void exitRoom() {
        if (mTRTCCloud != null) {
            mTRTCCloud.stopLocalAudio();
            mTRTCCloud.stopLocalPreview();
            mTRTCCloud.exitRoom();
            mTRTCCloud.setListener(null);
        }
        mTRTCCloud = null;
        TRTCCloud.destroySharedInstance();
    }

    private class TRTCCloudImplListener extends TRTCCloudListener {

        private WeakReference<TRTCCActivity> mContext;

        public TRTCCloudImplListener(TRTCCActivity activity) {
            super();
            mContext = new WeakReference<>(activity);
        }

        @Override
        public void onUserVideoAvailable(String userId, boolean available) {
            Log.d("###", "onUserVideoAvailable userId " + userId + ", mUserCount " + mUserCount + ",available " + available);
            user = userId;
            if (available) {
                mTRTCCloud.startRemoteView(userId, TRTC_VIDEO_RENDER_MODE_FIT ,mRemoteView);
            } else {
                Log.e("###", "异常");
            }
        }

        @Override
        public void onRemoteUserEnterRoom(String userId){
            Log.e("###", "in enter room  " + userId);
            Toast.makeText(TRTCCActivity.this, userId + "进入房间", Toast.LENGTH_LONG).show();
            other = userId;
        }


        @Override
        public void onError(int errCode, String errMsg, Bundle extraInfo) {
            Log.d("###", "sdk callback onError");
            TRTCCActivity activity = mContext.get();
            if (activity != null) {
                Toast.makeText(activity, "onError: " + errMsg + "[" + errCode+ "]" , Toast.LENGTH_SHORT).show();
                if (errCode == TXLiteAVCode.ERR_ROOM_ENTER_FAIL) {
                    activity.exitRoom();
                }
            }
        }
    }


}