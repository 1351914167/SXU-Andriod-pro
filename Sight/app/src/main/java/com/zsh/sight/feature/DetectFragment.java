package com.zsh.sight.feature;

import android.content.Intent;
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
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Rational;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import com.zsh.sight.R;
import com.zsh.sight.Utils.NavigationIconClickListener;
import com.zsh.sight.detect.Box;
import com.zsh.sight.detect.YOLO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class DetectFragment extends Fragment implements TextToSpeech.OnInitListener{
    // 布局组件
    private TextView tv_fps, tv_detect;
    private ImageView video_show;
    private TextureView viewFinder;
    private Button bt_speech;

    private boolean continuous_speech = false;

    // 设置阈值
    private final double threshold = 0.6;
    private final double nms_threshold = 0.6;

    private static final int REQUEST_PICK_IMAGE = 2;
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

    // 计时
    private long startTime = 0;
    private long endTime = 0;

    private TextToSpeech text_speech;

    private View view;
    private TrunkActivity mActivity;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_detect, container, false);
        mActivity = (TrunkActivity) getActivity();

        // 初始化YOLO模块
        YOLO.init(mActivity.getAssets());
        // 初始化组件
        initView();
        // 初始化TextToSpeech
        initTextToSpeech();
        setUpToolbar(view);
        return view;
    }

    // 初始化组件
    private void initView() {
        video_show = view.findViewById(R.id.video_show);
        tv_fps = view.findViewById(R.id.fps_info);
        tv_detect = view.findViewById(R.id.detect_info);
        viewFinder = view.findViewById(R.id.view_finder);
        bt_speech = view.findViewById(R.id.speech);

        // ImageView时间响应，视频展示
        video_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detectPhoto.set(false);
                speechDetect();
            }
        });

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
    private void setUpToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.app_bar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
        }

        toolbar.setNavigationOnClickListener(new NavigationIconClickListener(
                getContext(),
                view.findViewById(R.id.detect_view),
                new AccelerateDecelerateInterpolator(),
                getContext().getResources().getDrawable(R.drawable.shr_menu),
                getContext().getResources().getDrawable(R.drawable.ic_close)));
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.shr_toolbar_menu, menu);
        super.onCreateOptionsMenu(menu, menuInflater);
    }

    public void speechDetect(){
        text_speech.speak("", TextToSpeech.QUEUE_FLUSH, null);

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

        String[] directions = {"1", "2", "3", "4", "5", "6", "7", "8", "9"};
        for(int i=0; i<9; i++){
            if(record[i] != ""){
                text_speech.speak(directions[i] + "，" + record[i] + "。", TextToSpeech.QUEUE_ADD, null);
            }
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // setLanguage设置语言
            int result = text_speech.setLanguage(Locale.CHINA);
            // TextToSpeech.LANG_MISSING_DATA：表示语言的数据丢失
            // TextToSpeech.LANG_NOT_SUPPORTED：不支持
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(mActivity, "数据丢失或不支持", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // 设置语音播报的参数
    private void initTextToSpeech() {
        // 参数Context,TextToSpeech.OnInitListener
        text_speech = new TextToSpeech(mActivity, this);
        // 设置音调，值越大声音越尖（女生），值越小则变成男声,1.0是常规
        text_speech.setPitch(1.0f);
        // 设置语速
        text_speech.setSpeechRate(3.0f);
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
                    Bitmap bitmap = Bitmap.createBitmap(bitmapsrc, 0, 0, IMG_HEIGHT, IMG_WIDTH, matrix, false);

                    // 目标检测
                    detect_result = YOLO.detect(bitmap, threshold, nms_threshold);
                    tv_detect.setText("检测目标: " + detect_result.length + " 个");
                    if(continuous_speech && !text_speech.isSpeaking()){
                        speechDetect();
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

                    for (Box box : detect_result) {
                        boxPaint.setColor(box.getColor());
                        boxPaint.setStyle(Paint.Style.FILL);
                        canvas.drawText(box.getLabel(), box.x0 + 3, box.y0 + (int)(40 * mutableBitmap.getWidth() / 1000), boxPaint);
                        boxPaint.setStyle(Paint.Style.STROKE);
                        canvas.drawRect(box.getRect(), boxPaint);
                    }
                    mActivity.runOnUiThread(new Runnable() {
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
                mActivity.finish();
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
        video_show.setImageBitmap(mutableBitmap);
    }

    public Bitmap getPicture(Uri selectedImage) {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = mActivity.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
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

}
