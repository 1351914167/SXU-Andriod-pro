package com.zsh.sight.feature.gridview;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.viewpager.widget.ViewPager;

import androidx.appcompat.app.AppCompatActivity;

import android.os.StrictMode;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.zsh.sight.API.ImageDetect;
import com.zsh.sight.R;
import com.zsh.sight.feature.StatusBarUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PlusImageActivity extends AppCompatActivity
        implements ViewPager.OnPageChangeListener, View.OnClickListener, TextToSpeech.OnInitListener{

    private ViewPager viewPager; //展示图片的ViewPager
    private TextView positionTv; //图片的位置，第几张图片
    private ArrayList<String> imgList; //图片的数据源
    private int mPosition; //第几张图片
    private ViewPagerAdapter mAdapter;
    private MaterialButton materialButton;

    private TextToSpeech text_speech;

    private ImageView iv_delete;
    private boolean if_show_delete; // 是否显示删除选项

    // config
    public static final String IMG_LIST = "img_list"; //第几张图片
    public static final String POSITION = "position"; //第几张图片
    public static final int RESULT_CODE_VIEW_IMG = 11; //查看大图页面的结果码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plus_image);

        imgList = getIntent().getStringArrayListExtra(IMG_LIST);
        mPosition = getIntent().getIntExtra(POSITION, 0);

        if_show_delete = getIntent().getBooleanExtra("if_show_delete", false);
        initView();

        // 初始化TextToSpeech
        initTextToSpeech();
    }

    private void initView() {
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        positionTv = (TextView) findViewById(R.id.position_tv);
        findViewById(R.id.back_iv).setOnClickListener(this);
        iv_delete = (ImageView) findViewById(R.id.delete_iv);
        iv_delete.setOnClickListener(this);
        viewPager.addOnPageChangeListener(this);

        if(!if_show_delete){
            iv_delete.setVisibility(View.INVISIBLE);
        }

        materialButton = (MaterialButton) findViewById(R.id.bd_detect);
        materialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String data = ImageDetect.advancedGeneral(imgList.get(mPosition), true);
                try {
                    JSONObject jsonObject = new JSONObject(data);
                    if(jsonObject.has("error_code")){
                        Toast.makeText(PlusImageActivity.this, "识别失败", Toast.LENGTH_LONG).show();
                    }
                    else{
                        JSONArray jsonArray = new JSONArray(jsonObject.getString("result"));
                        String env_description = "";
                        for(int i = 0; i < jsonArray.length(); i++){
                            JSONObject json = jsonArray.getJSONObject(i);
                            double score = Double.parseDouble(json.getString("score"));
                            if(score >= 0.6){
                                env_description += json.getString("keyword") + ",";
                            }
                        }
                        Toast.makeText(PlusImageActivity.this, env_description, Toast.LENGTH_LONG).show();
                        text_speech.speak(env_description, TextToSpeech.QUEUE_FLUSH, null);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        mAdapter = new ViewPagerAdapter(this, imgList);
        viewPager.setAdapter(mAdapter);
        positionTv.setText(mPosition + 1 + "/" + imgList.size());
        viewPager.setCurrentItem(mPosition);
    }

    private String analyseGson(JSONObject json){
        String result  = "";

//        result = json.getString("keyword");

        Log.e("# analyse: ", result);

        return result;
    }

    @Override  //设置状态栏透明属性
    protected void onStart() {
        StatusBarUtil.transparencyBar(this);
        StatusBarUtil.BlackFontStatusBar(this.getWindow());
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onStart();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // setLanguage设置语言
            int result = text_speech.setLanguage(Locale.CHINA);
            // TextToSpeech.LANG_MISSING_DATA：表示语言的数据丢失
            // TextToSpeech.LANG_NOT_SUPPORTED：不支持
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "数据丢失或不支持", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 设置语音播报的参数
    private void initTextToSpeech() {
        // 参数Context,TextToSpeech.OnInitListener
        text_speech = new TextToSpeech(this, this);
        // 设置音调，值越大声音越尖（女生），值越小则变成男声,1.0是常规
        text_speech.setPitch(1.0f);
        // 设置语速
        SharedPreferences speechInfo = getSharedPreferences("speech_info", MODE_PRIVATE);
        float speed_init = (float) ((speechInfo.getInt("speed", 10) + 1) / 10);
        text_speech.setSpeechRate(speed_init);
    }

    //删除图片
    private void deletePic() {
        CancelOrOkDialog dialog = new CancelOrOkDialog(this, "要删除这张图片吗?") {
            @Override
            public void ok() {
                super.ok();
                imgList.remove(mPosition); //从数据源移除删除的图片
                setPosition();
                dismiss();
            }
        };
        dialog.show();
    }

    //设置当前位置
    private void setPosition() {
        positionTv.setText(mPosition + 1 + "/" + imgList.size());
        viewPager.setCurrentItem(mPosition);
        mAdapter.notifyDataSetChanged();
    }

    //返回上一个页面
    private void back() {
        Intent intent = getIntent();
        intent.putStringArrayListExtra(IMG_LIST, imgList);
        setResult(RESULT_CODE_VIEW_IMG, intent);
        finish();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mPosition = position;
        positionTv.setText(position + 1 + "/" + imgList.size());
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_iv:
                //返回
                back();
                break;
            case R.id.delete_iv:
                //删除图片
                deletePic();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //按下了返回键
            back();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
