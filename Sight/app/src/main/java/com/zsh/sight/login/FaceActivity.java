package com.zsh.sight.login;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.zsh.sight.R;
import com.zsh.sight.Utils.Base64Util;
import com.zsh.sight.Utils.FileUtil;
import com.zsh.sight.Utils.HttpUtil;
import com.zsh.sight.Utils.HttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FaceActivity extends Activity {

    private static int REQ_1 = 1;	//自定义参数
    private static int REQ_2 = 2;	//自定义参数
    private ImageView Image;
    private String mFilePath;
    Bitmap bitmap;                  // 拍的图像
    private String username;
    private int type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face);
        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        type = intent.getIntExtra("type", 0);
        Image = findViewById(R.id.iv);
        Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri photoUri = getImageUri();
        it.putExtra(MediaStore.EXTRA_OUTPUT,photoUri);
        startActivityForResult(it, REQ_2);
    }
    /**
     *调用原相机，返回原图
     *原理：将拍好的照片保存到指定路径，再将该照片从指定路径调出来
     **/
    public void startCamera(View view){
        String result;
        Intent intent = new Intent();
        try{
            if(type == 0){
                result = faceMatch();
                JSONObject jsonObject;
                jsonObject = new JSONObject(result);
                result = jsonObject.getString("result");
                jsonObject = new JSONObject(result);
                Double score = jsonObject.getDouble("score");
                if(score > 80){
                    Toast.makeText(FaceActivity.this, "识别成功", Toast.LENGTH_SHORT).show();
                    intent.putExtra("username", username);
                    setResult(1, intent);
                }
                else{
                    Toast.makeText(FaceActivity.this, "识别失败", Toast.LENGTH_SHORT).show();
                    intent.putExtra("username", username);
                    setResult(0, intent);
                }
            }
            else{
                result = HttpUtils.send("http://121.5.169.147:8000/uploadImage", mFilePath);
                if(!result.equals("time out")){
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("username", username);
                    jsonObject.put("face", result);
                    String data = HttpUtils.getJsonData(jsonObject, "http://121.5.169.147:8000/insertFace");
                    if(data.equals("time out")){
                        Toast.makeText(FaceActivity.this, "请检查网络连接",Toast.LENGTH_LONG ).show();
                    }
                    else{
                        Toast.makeText(FaceActivity.this, "上传成功",Toast.LENGTH_LONG ).show();
                    }
                }
                else{
                    Toast.makeText(FaceActivity.this, "请检查网络连接",Toast.LENGTH_LONG ).show();
                }
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        File file = new File(mFilePath);
        file.delete();
        finish();
    }

    /**
     *将拍好的照片保存到指定路径
     **/
    public Uri getImageUri() {
        Uri uri;
        mFilePath = this.getExternalFilesDir(null).getAbsolutePath()+"/FCa/"
                + System.currentTimeMillis() + ".jpg";
        File file = new File(mFilePath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        String path = file.getPath();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            uri = Uri.fromFile(file);
        } else {
            //兼容android7.0 使用共享文件的形式
            ContentValues contentValues = new ContentValues(1);
            contentValues.put(MediaStore.Images.Media.DATA, path);
            uri = this.getApplication().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        }
        return uri;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == REQ_1){//缩略图
                Bundle bundle = data.getExtras();
                bitmap = (Bitmap) bundle.get("data");
                Image.setImageBitmap(bitmap);
            }else if(requestCode == REQ_2){//原图
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(mFilePath);
                    Bitmap bitmap = BitmapFactory.decodeStream(fis);
                    Image.setImageBitmap(bitmap);
                    compressBitmap(mFilePath, new File(mFilePath));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }finally {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private String faceMatch(){
        // 请求url
        String url = "https://aip.baidubce.com/rest/2.0/face/v3/match";
        try {
            byte[] imgData = FileUtil.readFileByBytes(mFilePath);
            String imgStr = Base64Util.encode(imgData);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("username", username);
            String data =  HttpUtils.getJsonData(jsonObject, "http://121.5.169.147:8000/getFace");//后端没写限制，在每次插入新人脸的时候都写一下这个防止重复
            String param = "[" +
                    "{\"image\":" + "\""+data.substring(0, data.length() - 1)+"\""
                    + ",\"image_type\":\"URL\"}," +
                    "{\"image\":" + "\"" + imgStr +"\""
                    + ",\"image_type\":\"BASE64\"}" +
                    "]";
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

    public static void compressBitmap(String filePath, File file){
        // 数值越高，图片像素越低
        int inSampleSize = 10;
        BitmapFactory.Options options = new BitmapFactory.Options();
        //采样率
        options.inSampleSize = inSampleSize;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 把压缩后的数据存放到baos中
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100 ,baos);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(baos.toByteArray());
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
