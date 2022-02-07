package com.zsh.sight.feature;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;
import com.zsh.sight.MyApplication;
import com.zsh.sight.R;
import com.zsh.sight.Utils.HttpUtils;

import org.json.JSONException;
import org.json.JSONObject;


public class HelpFragment extends Fragment {

    private ImageView bt_help1, bt_help2, bt_help3, bt_join;
    private boolean help1, help2, help3;
    private View view;
    private TrunkActivity mActivity;
    private MyApplication myApplication;
    private int userType;
    private String username;
    private int roomID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mActivity = (TrunkActivity) getActivity();
        myApplication = (MyApplication) mActivity.getApplication();
        userType = myApplication.getUserType();
        username = myApplication.getUsername();
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("username", username);
            if(userType == 0) {
                String data = HttpUtils.getJsonData(jsonObject, "http://121.5.169.147:8000/getCare");
                if (data.equals("100")) {
                    Toast toast = Toast.makeText(mActivity.getBaseContext(), "请绑定被监护人", Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    jsonObject = new JSONObject();
                    jsonObject.put("username", data.substring(0, data.length() - 1));
                }
            }
            String id = HttpUtils.getJsonData(jsonObject, "http://121.5.169.147:8000/getRoomID");

            if (!id.equals("error\n") && !id.equals("time out")) {
                roomID = Integer.parseInt(id.substring(0, id.length() - 1));
                Log.e("# HelpFragment", "in success get id " + roomID);
            } else {
                Log.e("# HelpFragment", "in help frag error");
            }
        } catch (JSONException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_help, container, false);
        // 初始化组件
        initView();
        return view;
    }

    private void initView(){
        bt_help1 = view.findViewById(R.id.help1);
        bt_help2 = view.findViewById(R.id.help2);
        bt_help3 = view.findViewById(R.id.help4);
        help1 = false;
        help2 = false;
        help3 = false;

        bt_help1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                if(help1){
                    help1 = false;
                    Intent intent = new Intent(mActivity, TRTCCActivity.class);
                    intent.putExtra("mode", 1);
                    intent.putExtra("roomId", roomID);
                    startActivity(intent);
                }
                else{
                    help1 = true;
                    Glide.with(getContext()).load(R.drawable.help1_big)
                            .override(300,200)
                            .into(bt_help1);
                }

            }
        });
        bt_help2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Intent intent = new Intent(mActivity, TRTCCActivity.class);
                intent.putExtra("mode", 1);
                intent.putExtra("roomId", roomID);
                startActivity(intent);
            }
        });
        bt_help3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Intent intent = new Intent(mActivity, TRTCCActivity.class);
                intent.putExtra("mode", 1);
                intent.putExtra("roomId", roomID);
                startActivity(intent);
            }
        });
    }
}