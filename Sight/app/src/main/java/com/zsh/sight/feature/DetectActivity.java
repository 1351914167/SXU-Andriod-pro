package com.zsh.sight.feature;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.text.TextPaint;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.KeyEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.camera.core.UseCase;
import androidx.lifecycle.LifecycleOwner;
import androidx.palette.graphics.Palette;

import com.google.gson.Gson;
import com.zsh.sight.R;
import com.zsh.sight.Utils.NavigationIconClickListener;
import com.zsh.sight.detect.Box;
import com.zsh.sight.detect.YOLO;
import com.zsh.sight.shared.AccurateBasic;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class DetectActivity  extends AppCompatActivity implements TextToSpeech.OnInitListener{
    // 布局组件
    private TextView tv_fps, tv_detect, colorInfo, colorShow, textInfo;
    private ImageView video_show;
    private TextureView viewFinder;
    private Button bt_speech;

    private boolean continuous_speech = false;

    // 设置阈值
    private final double threshold = 0.6;
    private final double nms_threshold = 0.6;

    private AtomicBoolean detecting = new AtomicBoolean(false);
    private AtomicBoolean detectPhoto = new AtomicBoolean(false);

    // 图像尺寸
    private final int IMG_HEIGHT = 544;
    private final int IMG_WIDTH = 408;

    private final int IMG_HEIGHT_31 = (int)(IMG_HEIGHT / 3);
    private final int IMG_HEIGHT_32 = (int)(IMG_HEIGHT / 3 * 2);
    private final int IMG_WIDTH_31 = (int)(IMG_WIDTH / 3);
    private final int IMG_WIDTH_32 = (int)(IMG_WIDTH / 3 * 2);

    // 检测结果
    private Box[] detect_result;
    private String[] color_result;

    // 计时
    private long startTime = 0;
    private long endTime = 0;

    private TextToSpeech text_speech;
    private Bitmap bitmap, bitmap3;

    private int choose_model;
    private int color_int;

    private boolean isOCRing = false;
    private String textResult;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect);

        // 初始化组件
        initView();
        setUpToolbar();
        // 模型选择
        Intent intent = getIntent();
        choose_model = intent.getIntExtra("choose_model", 1);

        detect_result = null;
        if(choose_model == 1){      // 初始化YOLO模块
            YOLO.init(getAssets());
        }
        else if(choose_model == 2){ // 颜色识别
            tv_detect.setText("颜色检测");
            colorInfo.setText("颜色");
        }
        else if(choose_model == 3){ //OCR
            bt_speech.setVisibility(View.INVISIBLE);
            tv_detect.setText("文字提取");
        }

        // 初始化TextToSpeech
        initTextToSpeech();
    }

    // 初始化组件
    private void initView() {
        video_show = findViewById(R.id.video_show);
        tv_fps = findViewById(R.id.fps_info);
        tv_detect = findViewById(R.id.detect_info);
        viewFinder = findViewById(R.id.view_finder);
        bt_speech = findViewById(R.id.speech);
        colorShow = findViewById(R.id.color_show);
        colorInfo = findViewById(R.id.color_info);
        textInfo = findViewById(R.id.text_info);

        // ImageView时间响应，视频展示
        video_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                text_speech.speak("", TextToSpeech.QUEUE_FLUSH, null);

                String speech_info = "";
                detectPhoto.set(false);
                if(choose_model == 1){
                    speech_info = speechDetect();
                }
                else if(choose_model == 2){
                    speech_info = speechColor();
                }
                else if(choose_model == 3){
                    if(!isOCRing){
                        // 保存位图
                        saveBitmap(bitmap);
                        // 调用OCR接口
                        String imgPath = Environment.getExternalStorageDirectory() +"/ocr.png";
                        String textGson = AccurateBasic.accurateBasic(imgPath);
                        textResult = analyseGson(textGson);
                        // 提取内容
                        textInfo.setText(textResult);
                        speech_info = "文字提取，" + textResult;
                        isOCRing = false;
                    }
                }
                Log.e("DetectActivity onClick: ", speech_info);
                text_speech.speak(speech_info, TextToSpeech.QUEUE_FLUSH, null);
            }
        });

        // 播报模式切换
        bt_speech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                continuous_speech = !continuous_speech;
                if(continuous_speech){
                    bt_speech.setText("连 续 播 报");
                }
                else{
                    bt_speech.setText("单 点 播 报");
                }
            }
        });

        // TextureView监听器，取景
        viewFinder.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                updateTransform();
            }
        });

        viewFinder.post(new Runnable() {
            @Override
            public void run() {
                startCamera();
            }
        });
    }

    //导航栏控制
    private void setUpToolbar() {
        Toolbar toolbar = findViewById(R.id.app_bar);
        AppCompatActivity activity = this;
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
        }

        toolbar.setNavigationOnClickListener(new NavigationIconClickListener(
                this,
                findViewById(R.id.video_show)){
            @Override
            public void onClick(View view) {
                super.onClick(view);
                finish();
            }
        });
    }

    private String analyseGson(String json){
        Gson gson = new Gson();
        Map<String,Object> data = gson.fromJson(json, Map.class);
        List<Map<String,Object>> list = (List<Map<String,Object>>)data.get("words_result");
        StringBuffer words = new StringBuffer();
        for(Map<String,Object> map : list){
            words.append(map.get("words") + "，");
        }
        return words.toString();
    }

    @Override  //设置状态栏透明属性
    protected void onStart() {
        StatusBarUtil.transparencyBar(this);
        StatusBarUtil.BlackFontStatusBar(this.getWindow());
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onStart();
    }

    public String speechDetect(){
        String[] record = new String[9];
        for(int i=0; i<9; i++){
            record[i] = "";
        }

        int center_x, center_y;
        for(int i=0; i<detect_result.length; i++){
            center_x = (int)((detect_result[i].x0 + detect_result[i].x1) / 2);
            center_y = (int)((detect_result[i].y0 + detect_result[i].y1) / 2);

            int col, row;
            if(center_x < IMG_WIDTH_31){
                row = 0;
            }
            else if(center_x < IMG_WIDTH_32){
                row = 3;
            }
            else{
                row = 6;
            }
            if(center_y < IMG_HEIGHT_31){
                col = 0;
            }
            else if(center_y < IMG_HEIGHT_32){
                col = 1;
            }
            else{
                col = 2;
            }
            record[row + col] += detect_result[i].getLabel();
        }

        String[] directions = {"左上", "左中", "左下", "中上", "中间", "中下", "右上", "右中", "右下"};
        String speech_info = "";
        for(int i=0; i<9; i++){
            if(record[i] != ""){
                speech_info += directions[i] + "，" + record[i] + "。";
            }
        }

        return speech_info;
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

    private void updateTransform() {
        Matrix matrix = new Matrix();
        // Compute the center of the view finder
        float centerX = viewFinder.getWidth() / 2f;
        float centerY = viewFinder.getHeight() / 2f;
        float[] rotations = {0, 90, 180, 270};
        // Correct preview output to account for display rotation
        float rotationDegrees = rotations[viewFinder.getDisplay().getRotation()];
        matrix.postRotate(-rotationDegrees, centerX, centerY);
        // Finally, apply transformations to our TextureView
        viewFinder.setTransform(matrix);
    }

    private void startCamera() {
        CameraX.unbindAll();
        // 1. preview
        PreviewConfig previewConfig = new PreviewConfig.Builder()
                .setLensFacing(CameraX.LensFacing.BACK)
                .setTargetAspectRatio(new Rational(1, 1))
                .setTargetResolution(new Size(416, 416))  // 分辨率
                .build();

        Preview preview = new Preview(previewConfig);
        preview.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener() {
            @Override
            public void onUpdated(Preview.PreviewOutput output) {
                ViewGroup parent = (ViewGroup) viewFinder.getParent();
                parent.removeView(viewFinder);
                parent.addView(viewFinder, 0);
                viewFinder.setSurfaceTexture(output.getSurfaceTexture());
                updateTransform();
            }
        });
        DetectAnalyzer detectAnalyzer = new DetectAnalyzer();
        CameraX.bindToLifecycle((LifecycleOwner) this, preview, gainAnalyzer(detectAnalyzer));
    }

    private UseCase gainAnalyzer(DetectAnalyzer detectAnalyzer) {
        ImageAnalysisConfig.Builder analysisConfigBuilder = new ImageAnalysisConfig.Builder();
        analysisConfigBuilder.setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE);
        analysisConfigBuilder.setTargetResolution(new Size(416, 416));  // 输出预览图像尺寸
        ImageAnalysisConfig config = analysisConfigBuilder.build();
        ImageAnalysis analysis = new ImageAnalysis(config);
        analysis.setAnalyzer(detectAnalyzer);
        return analysis;
    }

    private void setTrafficLight(Bitmap cutBitmap){
        // 交通信号灯判断
        for(int i=0; i<detect_result.length; i++){
            if(detect_result[i].getLabel().equals("红绿灯")){
                int count_red = 0, count_green = 0, count_orange = 0;
                for(int x=(int)detect_result[i].x0+1; x<(int)detect_result[i].x1; x++){
                    for(int y=(int)detect_result[i].y0+1; y<(int)detect_result[i].y1; y++) {
                        int rgb = cutBitmap.getPixel(x, y);
                        float[] hsv = new float[3];
                        Color.colorToHSV(rgb, hsv);
                        int h = (int)(hsv[0] / 2);
                        int s = (int)(hsv[1] * 255);
                        int v = (int)(hsv[2] * 255);

                        if((((0 < h && h < 10) || (156 < h && h < 180) )) && 43 < s && s < 255 && 46 < v && v < 255){
                            count_red++;
                        }
                        else if(11 < h && h < 25 && 43 < s && s < 255 && 46 < v && v < 255){
                            count_orange++;
                        }
                        else if(26 < h && h < 34 && 43 < s && s < 255 && 46 < v && v < 255){
                            count_orange++;
                        }
                        else if(35 < h && h < 77 && 43 < s && s < 255 && 46 < v && v < 255){
                            count_green++;
                        }
                        else if(78 < h && h < 99 && 43 < s && s < 255 && 46 < v && v < 255){
                            count_green++;
                        }
                    }
                }
                String mColor = "红绿灯";
                if(count_red > count_green && count_red >= count_orange){
                    mColor = "红灯";
                }
                else if(count_green > count_red && count_green >= count_orange){
                    mColor = "绿灯";
                }
                else if(count_orange > count_green && count_orange >= count_red){
                    mColor = "橙灯";
                }
                detect_result[i].setLabel(mColor);
            }
        }
    }

    private void saveBitmap(Bitmap bitmap){
        File file = new File(Environment.getExternalStorageDirectory() +"/ocr.png");
        try {
            //文件输出流
            FileOutputStream fileOutputStream=new FileOutputStream(file);
            //压缩图片，如果要保存png，就用Bitmap.CompressFormat.PNG，要保存jpg就用Bitmap.CompressFormat.JPEG,质量是100%，表示不压缩
            bitmap.compress(Bitmap.CompressFormat.PNG,100,fileOutputStream);
            //写入，这里会卡顿，因为图片较大
            fileOutputStream.flush();
            //记得要关闭写入流
            fileOutputStream.close();
            //成功的提示，写入成功后，请在对应目录中找保存的图片
//            Toast.makeText(DetectActivity.this,"写入成功！目录"+Environment.getExternalStorageDirectory()+"/ocr.png",Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            //失败的提示
            Toast.makeText(DetectActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }

    private class DetectAnalyzer implements ImageAnalysis.Analyzer {
        @Override
        public void analyze(ImageProxy image, final int rotationDegrees) {
            if (detecting.get() || detectPhoto.get()) {
                return;
            }
            detecting.set(true);
            startTime = System.currentTimeMillis();

            Bitmap bitmapsrc0 = imageToBitmap(image);  // 格式转换
            final Bitmap bitmapsrc = Bitmap.createScaledBitmap(bitmapsrc0, IMG_HEIGHT, IMG_WIDTH, true);

            Thread detectThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(rotationDegrees);
                    bitmap = Bitmap.createBitmap(bitmapsrc, 0, 0, IMG_HEIGHT, IMG_WIDTH, matrix, false);

                    // 检测
                    if(choose_model == 1){
                        // yolov4-tiny
                        detect_result = YOLO.detect(bitmap, threshold, nms_threshold);

                        // 红绿灯
                        setTrafficLight(bitmap);

                        tv_detect.setText("检测目标: " + detect_result.length + " 个");
                        // 播报
                        if(continuous_speech && !text_speech.isSpeaking()){
                            speechColor();
                        }
                    }
                    else if(choose_model == 2){
                        // 截取中心位置的像素
                        Bitmap bitmap2 = Bitmap.createBitmap(bitmap, IMG_WIDTH_31, IMG_HEIGHT_31, IMG_HEIGHT_31, IMG_WIDTH_31, matrix, false);
                        // 获取主色调
                        Palette.from(bitmap2).generate(new Palette.PaletteAsyncListener() {
                            @Override
                            public void onGenerated(Palette palette) {
                                color_int = palette.getDominantColor(0x000000);
                                colorShow.setBackgroundColor(color_int);
                                colorInfo.setText("主色调：" + Integer.toHexString(color_int));
                                // 播报
                                if(continuous_speech && !text_speech.isSpeaking()){
                                    speechColor();
                                }
                            }
                        });
                    }
                    else if(choose_model == 3){
                        bitmap3 = Bitmap.createBitmap(bitmap, IMG_HEIGHT_31, 0, IMG_HEIGHT_31, IMG_WIDTH, matrix, false);
                    }

                    final Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    Canvas canvas = new Canvas(mutableBitmap);

                    // 区域分割线
                    final Paint dividePaint = new Paint();
                    dividePaint.setColor(Color.argb(255,255,255,255));
                    canvas.drawLine(0,(int)(IMG_HEIGHT/3), (int)IMG_WIDTH, (int)(IMG_HEIGHT/3), dividePaint);
                    canvas.drawLine(0,(int)(IMG_HEIGHT/3*2), (int)IMG_WIDTH, (int)(IMG_HEIGHT/3*2), dividePaint);
                    canvas.drawLine((int)(IMG_WIDTH/3), 0, (int)(IMG_WIDTH/3), (int)IMG_HEIGHT, dividePaint);
                    canvas.drawLine((int)(IMG_WIDTH/3*2), 0, (int)(IMG_WIDTH/3*2), (int)IMG_HEIGHT, dividePaint);

                    // 画出检测框
                    final Paint boxPaint = new Paint();
                    boxPaint.setAlpha(200);
                    boxPaint.setStyle(Paint.Style.STROKE);
                    boxPaint.setStrokeWidth((int)(4 * mutableBitmap.getWidth() / 800));
                    boxPaint.setTextSize((int)(40 * mutableBitmap.getWidth() / 800));

                    if(detect_result != null){
                        for (Box box : detect_result) {
                            boxPaint.setColor(box.getColor());
                            boxPaint.setStyle(Paint.Style.FILL);
                            canvas.drawText(box.getLabel(), box.x0 + 3, box.y0 + (int)(40 * mutableBitmap.getWidth() / 1000), boxPaint);
                            boxPaint.setStyle(Paint.Style.STROKE);
                            canvas.drawRect(box.getRect(), boxPaint);
                        }
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            video_show.setImageBitmap(mutableBitmap);
                            detecting.set(false);
                            endTime = System.currentTimeMillis();
                            long dur = endTime - startTime;
                            float fps = (float) (1000.0 / dur);
                            tv_fps.setText(String.format(Locale.CHINESE, "FPS: %.3f", fps));
                        }
                    });
                }
            }, "detect");
            detectThread.start();
        }

        private Bitmap imageToBitmap(ImageProxy image) {
            ImageProxy.PlaneProxy[] planes = image.getPlanes();
            ImageProxy.PlaneProxy y = planes[0];
            ImageProxy.PlaneProxy u = planes[1];
            ImageProxy.PlaneProxy v = planes[2];
            ByteBuffer yBuffer = y.getBuffer();
            ByteBuffer uBuffer = u.getBuffer();
            ByteBuffer vBuffer = v.getBuffer();
            int ySize = yBuffer.remaining();
            int uSize = uBuffer.remaining();
            int vSize = vBuffer.remaining();

            byte[] nv21 = new byte[ySize + uSize + vSize];
            // U and V are swapped
            yBuffer.get(nv21, 0, ySize);
            vBuffer.get(nv21, ySize, vSize);
            uBuffer.get(nv21, ySize + vSize, uSize);

            YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 100, out);
            byte[] imageBytes = out.toByteArray();

            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        }
    }

    private String speechColor(){
        // 色调范围0-360 饱和度范围0-1 亮度范围0-1
        float[] hsv = new float[3];
        // rgb 转 hsv
        Color.colorToHSV(color_int, hsv);
        int h = (int)(hsv[0] / 2);
        int s = (int)(hsv[1] * 255);
        int v = (int)(hsv[2] * 255);
        // 判断颜色
        String mColor = "模糊";
        if(0 < h && h < 180 && 0 < s && s < 255 && 0 < v && v < 46){
            mColor = "黑色";
        }
        else if(0 < h && h < 180 && 0 < s && s < 43 && 46 < v && v < 220){
            mColor = "灰色";
        }
        else if(0 < h && h < 180 && 0 < s && s < 30 && 221 < v && v < 255){
            mColor = "白色";
        }
        else if((((0 < h && h < 10) || (156 < h && h < 180) )) && 43 < s && s < 255 && 46 < v && v < 255){
            mColor = "红色";
        }
        else if(11 < h && h < 25 && 43 < s && s < 255 && 46 < v && v < 255){
            mColor = "橙色";
        }
        else if(26 < h && h < 34 && 43 < s && s < 255 && 46 < v && v < 255){
            mColor = "黄色";
        }
        else if(35 < h && h < 77 && 43 < s && s < 255 && 46 < v && v < 255){
            mColor = "绿色";
        }
        else if(78 < h && h < 99 && 43 < s && s < 255 && 46 < v && v < 255){
            mColor = "青色";
        }
        else if(100 < h && h < 124 && 43 < s && s < 255 && 46 < v && v < 255){
            mColor = "蓝色";
        }
        else if(125 < h && h < 155 && 43 < s && s < 255 && 46 < v && v < 255){
            mColor = "紫色";
        }
        // 播报
        String tip = "色调：" + mColor + "，饱和度：百分之" + (int)(hsv[1] * 100) + "，亮度：百分之" + (int)(hsv[2] * 100);

        return tip;
    }

    @Override
    public void onDestroy() {
        CameraX.unbindAll();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                finish();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) {
            return;
        }
        detectPhoto.set(true);
        Bitmap image = getPicture(data.getData());
        Box[] result = YOLO.detect(image, threshold, nms_threshold);
        Bitmap mutableBitmap = image.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        final Paint boxPaint = new Paint();
        boxPaint.setAlpha(200);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(4 * image.getWidth() / 800);
        boxPaint.setTextSize(40 * image.getWidth() / 800);
        for (Box box : result) {
            boxPaint.setColor(box.getColor());
            boxPaint.setStyle(Paint.Style.FILL);
            canvas.drawText(box.getLabel(), box.x0 + 3, box.y0 + 17, boxPaint);
            boxPaint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(box.getRect(), boxPaint);
        }

        boxPaint.setColor(Color.argb(255,0, 0, 0));
        boxPaint.setStyle(Paint.Style.FILL);
        canvas.drawText("文字！", 100, 100, boxPaint);

        video_show.setImageBitmap(mutableBitmap);
    }

    public Bitmap getPicture(Uri selectedImage) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
        int rotate = readPictureDegree(picturePath);
        return rotateBitmapByDegree(bitmap, rotate);
    }

    public int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    public Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Bitmap returnBm = null;
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }

    // 连续点击两次返回键退出程序
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            text_speech.speak("", TextToSpeech.QUEUE_FLUSH, null);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
