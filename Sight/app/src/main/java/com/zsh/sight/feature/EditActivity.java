package com.zsh.sight.feature;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.zsh.sight.Audio.CustomButton;
import com.zsh.sight.Audio.DemoException;
import com.zsh.sight.Audio.RecordVoicePopWindow;
import com.zsh.sight.Audio.uploadAudio;
import com.zsh.sight.MyApplication;
import com.zsh.sight.R;
import com.zsh.sight.Audio.AudioRecorderManager;
import com.zsh.sight.Utils.HttpUtils;
import com.zsh.sight.feature.gridview.GlideEngine;
import com.zsh.sight.feature.gridview.GridViewAdapter;
import com.zsh.sight.feature.gridview.PlusImageActivity;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EditActivity extends AppCompatActivity {

    private MyApplication myApplication;
    private Context mContext;
    private GridView gridView;
    private ArrayList<String> mPicList = new ArrayList<>(); //上传的图片凭证的数据源
    private GridViewAdapter mGridViewAddImgAdapter; //展示上传的图片的适配器
    private Button bt_publish;
    private String username;
    private EditText editText;
    private CustomButton record;

    private ArrayList<String> imagePath = new ArrayList<>();

    public static final String IMG_LIST = "img_list"; //第几张图片
    public static final String POSITION = "position"; //第几张图片
    public static final String PIC_PATH = "pic_path"; //图片路径
    public static final int MAX_SELECT_PIC_NUM = 6; // 最多上传5张图片
    public static final int REQUEST_CODE_MAIN = 10; //请求码
    public static final int RESULT_CODE_VIEW_IMG = 11; //查看大图页面的结果码

    AudioRecorderManager audioRecorderManager;
    RecordVoicePopWindow recordVoicePopWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        mContext = this;
        myApplication = (MyApplication) getApplication();
        username = myApplication.getUsername();
        initGridView();
        initView();
    }

    private void initView() {
        bt_publish = (Button) findViewById(R.id.publish);
        editText = (EditText) findViewById(R.id.contend);
        record = (CustomButton)  findViewById(R.id.RecordButton);
        final boolean[] flag = {true};
        record.setOnVoiceButtonCallBack(new CustomButton.OnVoiceButtonCallBack() {
            @Override
            public void onStartRecord() {
                audioRecorderManager = AudioRecorderManager.newInstance(EditActivity.this);
                audioRecorderManager.startRecording();
                recordVoicePopWindow = new RecordVoicePopWindow(EditActivity.this);
                recordVoicePopWindow.showAsDropDown(LayoutInflater.from(EditActivity.this).
                        inflate(R.layout.activity_edit, null));
                recordVoicePopWindow.showRecordingTipView();
            }


            @Override
            public void onStopRecord() {
                recordVoicePopWindow.dismiss();
                audioRecorderManager.stopRecord();
                String path = audioRecorderManager.getFilePath();
                uploadAudio uploadAudio = new uploadAudio(path);
                try {
                    String result = uploadAudio.run();
                    Log.e("####", "in audio " + result);
                    org.json.JSONObject jsonObject = new org.json.JSONObject(result);
                    result = jsonObject.getString("result");

                    editText.setText(editText.getText() + result.substring(2, result.length() - 2));
                    audioRecorderManager.deleteFile(path);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (DemoException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onWillCancelRecord() {
                recordVoicePopWindow.showCancelTipView();
            }

            @Override
            public void onContinueRecord() {
                recordVoicePopWindow.showRecordingTipView();
            }
        });


        bt_publish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 发布动态
//                public Diary(String headPath, String nickname, String contend, List<String> imgPathList){
//                Diary diary = new Diary("");
//                LoginServer.publish_diary();
                String url1 = "http://121.5.169.147:8000/uploadImage";
                String url2 = "http://121.5.169.147:8000/share";
                JSONArray jsonArray = new JSONArray();
                int cnt = 0;
                Log.e("####", editText.getText().toString());
                if (mPicList.size() == 0){
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("username", username);
                    jsonObject.put("image", null);
                    jsonObject.put("content", editText.getText().toString());
                    jsonObject.put("count", cnt);
                    jsonArray.add(jsonObject);
                }
                for(String path : mPicList){
                    try {
                        JSONObject jsonObject = new JSONObject();
                        String data = HttpUtils.send(url1, path);
                        jsonObject.put("username", username);
                        jsonObject.put("image", data);
                        jsonObject.put("content", editText.getText().toString());
                        jsonObject.put("count", cnt++);
                        jsonArray.add(jsonObject);
                        Log.e("###", jsonArray.toString());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    org.json.JSONObject sendString = new org.json.JSONObject();
                    sendString.put("String", jsonArray.toString());
                    Log.e("###", sendString.toString());
                    String shareResult = HttpUtils.getJsonData(sendString, url2);
                } catch (InterruptedException | JSONException e) {
                    e.printStackTrace();
                }
                finish();
            }
        });

    }

    //初始化展示上传图片的GridView
    private void initGridView() {
        gridView = (GridView) findViewById(R.id.gridView);
        mGridViewAddImgAdapter = new GridViewAdapter(mContext, mPicList, 5);
        gridView.setAdapter(mGridViewAddImgAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == parent.getChildCount() - 1) {
                    //如果“增加按钮形状的”图片的位置是最后一张，且添加了的图片的数量不超过5张，才能点击
                    if (mPicList.size() == MAX_SELECT_PIC_NUM) {
                        //最多添加5张图片
                        viewPluImg(position);
                    } else {
                        //添加凭证图片
                        selectPic(MAX_SELECT_PIC_NUM - mPicList.size());
                    }
                } else {
                    viewPluImg(position);
                }
            }
        });
    }

    //查看大图
    private void viewPluImg(int position) {
        Intent intent = new Intent(mContext, PlusImageActivity.class);
        intent.putStringArrayListExtra(IMG_LIST, mPicList);
        intent.putExtra(POSITION, position);
        startActivityForResult(intent, REQUEST_CODE_MAIN);
    }

    /**
     * 打开相册或者照相机选择凭证图片，最多5张
     *
     * @param maxTotal 最多选择的图片的数量
     */
    private void selectPic(int maxTotal) {
        PictureSelector.create(this)
                .openGallery(PictureMimeType.ofImage())
                .loadImageEngine(GlideEngine.createGlideEngine())
                .maxSelectNum(maxTotal)// 最大图片选择数量 int
                .imageSpanCount(3)// 每行显示个数 int
                .isCamera(true)// 是否显示拍照按钮 true or false
                .compress(true)// 是否压缩 true or false
                .forResult(PictureConfig.CHOOSE_REQUEST);
    }

    // 处理选择的照片的地址
    private void refreshAdapter(List<LocalMedia> picList) {
        for (LocalMedia localMedia : picList) {
            //被压缩后的图片路径
            if (localMedia.isCompressed()) {
                String compressPath = localMedia.getCompressPath(); //压缩后的图片路径
                mPicList.add(compressPath); //把图片添加到将要上传的图片数组中
                mGridViewAddImgAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:
                    // 图片选择结果回调
                    refreshAdapter(PictureSelector.obtainMultipleResult(data));
                    // 例如 LocalMedia 里面返回三种path
                    // 1.media.getPath(); 为原图path
                    // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true
                    // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true
                    // 如果裁剪并压缩了，以取压缩路径为准，因为是先裁剪后压缩的
                    break;
            }
        }
        if (requestCode == REQUEST_CODE_MAIN && resultCode == RESULT_CODE_VIEW_IMG) {
            //查看大图页面删除了图片
            ArrayList<String> toDeletePicList = data.getStringArrayListExtra(IMG_LIST); //要删除的图片的集合
            mPicList.clear();
            mPicList.addAll(toDeletePicList);
            mGridViewAddImgAdapter.notifyDataSetChanged();
        }
    }

    @Override  //设置状态栏透明属性
    protected void onStart() {
        StatusBarUtil.transparencyBar(this);
        StatusBarUtil.BlackFontStatusBar(this.getWindow());
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        super.onStart();
        Log.e(TAG, getClass().getSimpleName()+": onStart ");
    }

}