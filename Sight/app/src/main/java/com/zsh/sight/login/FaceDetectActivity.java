package com.zsh.sight.login;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.zsh.sight.MyApplication;
import com.zsh.sight.R;
import com.zsh.sight.Utils.HttpUtil;
import com.zsh.sight.Utils.HttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class FaceDetectActivity extends AppCompatActivity {

    private String username;
    private MyApplication myApplication;
    private MaterialButton up_image, match;
    private TextView textView;
    private String image_path = null;
    private String getPath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detect);
        myApplication = (MyApplication) getApplication();
        username = myApplication.getUsername();
        up_image = (MaterialButton) findViewById(R.id.up_image);
        match = (MaterialButton) findViewById(R.id.match);
        textView = (TextView) findViewById(R.id.test_Text);
        up_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    updateImage();
                    JSONObject j = new JSONObject();
                } catch (InterruptedException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        match.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //String result = faceMatch();
                //textView.setText(result);
            }
        });
    }

    private String faceMatch(String filePath){
        // 请求url
        String url = "https://aip.baidubce.com/rest/2.0/face/v3/match";
        try {
            JSONObject jsonObject1 = new JSONObject(), jsonObject2 = new JSONObject();
            jsonObject1.put("image", "http://121.5.169.147:8080/image/1636397403913.jpeg");//本地获取图片后base64编码写在这
            jsonObject2.put("username", username);
            String data =  HttpUtils.getJsonData(jsonObject2, "http://121.5.169.147:8000/getFace");//后端没写限制，在每次插入新人脸的时候都写一下这个防止重复
            String param = "[" +
                    "{\"image\":" + "\"http://121.5.169.147:8080/image/1636397403913.jpeg\""
                    + ",\"image_type\":\"URL\"}," +
                    "{\"image\":" + "\""+data.substring(0, data.length() - 1)+"\""
                    + ",\"image_type\":\"URL\"}" +
                    "]";
            Log.e("####", param);
            // 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
            String accessToken = "24.4db87db73e5b091ddf9a650bf5812065.2592000.1641627477.282335-25117457";

            String result = HttpUtil.post(url, accessToken, "application/json", param);
            System.out.println(result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //上传个人图片的接口
    private void updateImage() throws InterruptedException, JSONException {
        String path = this.getExternalFilesDir(null).getAbsolutePath() + "/1631590350111.jpg";//本地图片路径
        image_path = HttpUtils.send("http://121.5.169.147:8000/uploadImage", path);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", username);
        jsonObject.put("face", image_path);
        String data = HttpUtils.getJsonData(jsonObject, "http://121.5.169.147:8000/insertFace");
        Log.e("####", data);
    }
}