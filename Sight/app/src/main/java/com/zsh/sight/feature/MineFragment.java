package com.zsh.sight.feature;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;
import static com.bumptech.glide.request.RequestOptions.bitmapTransform;
import static com.zsh.sight.Utils.pxUtil.dip2px;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.tencent.map.tools.json.annotation.Json;
import com.zsh.sight.MyApplication;
import com.zsh.sight.R;
import com.zsh.sight.Utils.CornerTransform;
import com.zsh.sight.Utils.HttpUtils;
import com.zsh.sight.Utils.ScreenSizeUtils;
import com.zsh.sight.adapter.ImageAdapter;
import com.zsh.sight.feature.gridview.GlideEngine;
import com.zsh.sight.login.FaceActivity;
import com.zsh.sight.login.LoginActivity;
import com.zsh.sight.login.NewCodeActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.CropCircleTransformation;


public class MineFragment extends Fragment {
    private View view;
    private TrunkActivity mActivity;
    private List<String> mPicList;
    private MyApplication myApplication;
    private String username;
    private int userType;

    private ImageView iv_head, iv_back;
    private TextView textView;
    private SeekBar s_speed;
    private String url1 = "http://121.5.169.147:8000/uploadImage";
    private String url2 = "http://121.5.169.147:8000/upload/head";
    private String url3 = "http://121.5.169.147:8000/getHead";
    private String url4 = "http://121.5.169.147:8000/getHelpInfo";

    private TextView setting_nickname, setting_code, setting_score, setting_guardian, setting_tobe_guardian, setting_exit, setting_face;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_mine, container, false);
        mActivity = (TrunkActivity) getActivity();
        myApplication = (MyApplication) mActivity.getApplication();
        username = myApplication.getUsername();
        userType = myApplication.getUserType();
        try {
            initView();
        } catch (InterruptedException | JSONException e) {
            e.printStackTrace();
        }

        return view;
    }

    // 初始化组件
    private void initView() throws InterruptedException, JSONException {
        iv_head = view.findViewById(R.id.mine_img);
        textView = view.findViewById(R.id.user_name);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", username);
        String data = HttpUtils.getJsonData(jsonObject, "http://121.5.169.147:8000/getNickname");
        textView.setText(new JSONObject(data).getString("nickname"));

        // 播报速度
        SharedPreferences speechInfo = mActivity.getSharedPreferences("speech_info", MODE_PRIVATE);
        int speed_init = speechInfo.getInt("speed", 50);
        /*s_speed.setProgress(speed_init);
        SeekBar.OnSeekBarChangeListener progressListener = new SeekBar.OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) { }
            public void onStartTrackingTouch(SeekBar seekBar) { }
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    SharedPreferences.Editor editor = speechInfo.edit();
                    editor.putInt("speed", progress);
                    editor.apply();
                }
            }
        };
        s_speed.setOnSeekBarChangeListener(progressListener);*/

        Dialog dialog = new Dialog(mActivity, R.style.normalDialogAnim);

        JSONObject json = new JSONObject();
        json.put("username", username);
        String p = HttpUtils.getJsonData(json, url3);
        json = new JSONObject(p);
        setHeadImg(json.getString("path"));
        Log.e("###", "in mine" + json.getString("path"));
        iv_head.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                selectPic(1);
            }
        });

        // 设置功能
        setting_nickname = view.findViewById(R.id.setting_nickname);
        setting_code = view.findViewById(R.id.setting_code);
        setting_face = view.findViewById(R.id.setting_face);
        /*setting_guardian = view.findViewById(R.id.setting_guardian);
        setting_tobe_guardian = view.findViewById(R.id.setting_tobe_guardian);*/
        setting_score = view.findViewById(R.id.setting_score);
        /*setting_exit = view.findViewById(R.id.setting_exit);*/

        /*// 注销登录
        setting_exit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                confirm_message_box();
            }
        });*/

        /*// 修改昵称
        setting_nickname.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Intent intent = new Intent(mActivity, NickActivity.class);
                startActivity(intent);
            }
        });

        // 修改密码
        setting_code.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Intent intent = new Intent(mActivity, NewCodeActivity.class);
                startActivity(intent);
            }
        });

        setting_face.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mActivity, FaceActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("type", 1);
                startActivity(intent);
            }
        });

        if(userType == 0){
            setting_score.setText("我的分数");
            setting_score.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    customDialog();
                }
            });
        }
        else {
            setting_score.setText("我的时长");
        }


        // 绑定监护人
        setting_guardian.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("username", username);
                    String url = "http://121.5.169.147:8000/train";
                    String data = HttpUtils.getJsonData(jsonObject, url);
                    JSONObject jsonObject1 = new JSONObject(data);
                    if(jsonObject1.getString("res").equals("true")){
                        Toast.makeText(mActivity, "更新完成", Toast.LENGTH_LONG).show();
                    }
                    else{
                        Toast.makeText(mActivity, "更新失败", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        // 成为监护人
        setting_tobe_guardian.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                customBindDialog();
            }
        });*/
    }
    // 确认弹窗
    private void confirm_message_box(){
        AlertDialog alertDialog1 = new AlertDialog.Builder(mActivity)
                .setTitle("注销登录")//标题
                .setMessage("是否确认注销登录？")//内容
                .setIcon(R.drawable.dog)//图标
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加"Yes"按钮
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SharedPreferences loginInfo = mActivity.getSharedPreferences("login_info", MODE_PRIVATE);
                        SharedPreferences.Editor editor = loginInfo.edit();
                        editor.remove("last_account");
                        editor.remove("last_password");
                        editor.apply();
                        Intent intent = new Intent(mActivity, LoginActivity.class);
                        startActivity(intent);
                        mActivity.finish();
                        Toast.makeText(mActivity,"注销登录", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {//添加取消
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .create();
        alertDialog1.show();
    }

    private void customBindDialog() {
        final Dialog dialog = new Dialog(mActivity, R.style.NormalDialogStyle);
        View view = View.inflate(mActivity, R.layout.activity_path, null);
        MaterialButton materialButton = (MaterialButton) view.findViewById(R.id.help_button);
        TextInputLayout help_input = (TextInputLayout) view.findViewById(R.id.help_input);
        TextInputEditText help_input_edit = (TextInputEditText) view.findViewById(R.id.help_edit_text);
        ImageView imageView = (ImageView) view.findViewById(R.id.path_back);

        materialButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Log.e("###", "in click " + help_input_edit.getText().toString());
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("username", username);
                    jsonObject.put("helpname", help_input_edit.getText());
                    String data = HttpUtils.getJsonData(jsonObject, "http://121.5.169.147:8000/uploadCare");
                    if(data.equals("100\n")){
                        help_input.setError("请输入正确的被监护人用户名");
                    }
                    else{
                        help_input.setError(null);
                        dialog.dismiss();
                    }
                } catch (JSONException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        // 设置自定义的布局
        dialog.setContentView(view);
        //使得点击对话框外部不消失对话框
        dialog.setCanceledOnTouchOutside(true);
        //设置对话框的大小
        view.setMinimumHeight((int) (ScreenSizeUtils.getInstance(mActivity).getScreenHeight() * 0.23f));
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = (int) (ScreenSizeUtils.getInstance(mActivity).getScreenWidth() * 0.75f);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        dialogWindow.setAttributes(lp);
        imageView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void customDialog() {
        final Dialog dialog = new Dialog(mActivity, R.style.NormalDialogStyle);
        View view = View.inflate(mActivity, R.layout.dialog_normal, null);
        Button confirm = (Button) view.findViewById(R.id.confirm);
        TextView my_score_set = (TextView) view.findViewById(R.id.my_score_set);
        TextView my_time_set = (TextView) view.findViewById(R.id.my_time_set);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", username);
            String data = HttpUtils.getJsonData(jsonObject, url4);
            jsonObject  = new JSONObject(data);
            String Tscore = jsonObject.getString("tscore");
            String Time = jsonObject.getString("time");
            int time = Integer.parseInt(Time);
            Double ascore = Double.parseDouble(Tscore) / (double)time;
            my_score_set.setText(String.format("%.1f", ascore));
            my_time_set.setText(time + "");
        } catch (JSONException | InterruptedException e) {
            e.printStackTrace();
        }

        // 设置自定义的布局
        dialog.setContentView(view);
        //使得点击对话框外部不消失对话框
        dialog.setCanceledOnTouchOutside(true);
        //设置对话框的大小
        view.setMinimumHeight((int) (ScreenSizeUtils.getInstance(mActivity).getScreenHeight() * 0.23f));
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = (int) (ScreenSizeUtils.getInstance(mActivity).getScreenWidth() * 0.75f);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        dialogWindow.setAttributes(lp);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void setHeadImg(String img_src){
        // 设置头像
        CornerTransform transformation = new CornerTransform(getContext(),dip2px(getContext(), 10));
        transformation.setExceptCorner(true, false, false, true);
        Glide.with(mActivity.getApplicationContext()).asBitmap().load(img_src)
                .circleCrop()
                .into(iv_head);
    }


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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:
                    // 图片选择结果回调
                    refreshAdapter(PictureSelector.obtainMultipleResult(data));
                    break;
            }
            if(mPicList.size() >= 1){
                setHeadImg(mPicList.get(0));

                JSONObject jsonObject = new JSONObject();
                try {
                    String path = HttpUtils.send(url1, mPicList.get(0));
                    jsonObject.put("username", username);
                    jsonObject.put("head", path);
                    String result = HttpUtils.getJsonData(jsonObject, url2);
                    Log.e("###", "head "+ result);
                } catch (JSONException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 处理选择的照片的地址
    private void refreshAdapter(List<LocalMedia> picList) {
        mPicList = new ArrayList<>();
        for (LocalMedia localMedia : picList) {
            //被压缩后的图片路径
            if (localMedia.isCompressed()) {
                String compressPath = localMedia.getCompressPath(); //压缩后的图片路径
                mPicList.add(compressPath); //把图片添加到将要上传的图片数组中
            }
        }
    }

}
