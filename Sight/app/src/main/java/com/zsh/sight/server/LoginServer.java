package com.zsh.sight.server;

import android.util.Log;

import com.zsh.sight.MyApplication;
import com.zsh.sight.R;
import com.zsh.sight.Utils.HttpUtils;
import com.zsh.sight.shared.UserInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginServer {
    // 判断账户是否存在
    public static boolean exist_account(String account){
        String result;

        try {
            URL url=new URL("http://120.76.57.148/hongYu/v1/start/captcha");
            HttpURLConnection connect=(HttpURLConnection)url.openConnection();
            InputStream input=connect.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));

//            String line = null;
//            System.out.println(connect.getResponseCode());
//
//            StringBuffer sb = new StringBuffer();
//            while ((line = in.readLine()) != null) {
//                sb.append(line);
//            }
//            result = sb.toString();
            result = "111";
        } catch (Exception e) {
            System.out.println(e.toString());
            result = "null";
        }

        Log.e("### exist_account = ", result);
        System.out.println("exist_account = " + result);

        return false;
    }


    // 加载用户信息
    public static UserInfo load_user_info(String account) throws JSONException, InterruptedException {
        // 服务器端加载
        int userType = 0;
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", account);
        String url = "http://www.traceProj.xyz:8000/userInfo";
        String s = HttpUtils.getJsonData(jsonObject, url);
        s = s.substring(0, s.length() - 1);
        userType = Integer.parseInt(s);
        Log.e("###", "usertype" + userType);
        return new UserInfo(account, userType);
    }

    // 身份验证
    public static boolean authenticate(String account, String password) throws JSONException, InterruptedException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", account);
        System.out.println(jsonObject.toString());
        String url = "http://121.5.169.147:8000/login";
        String s = HttpUtils.getJsonData(jsonObject, url);
        password = password + "\n";
        Log.e("###", "in password auth " + s.equals(password) + "");
        if(s.equals(password))
            return true;
        return false;
    }

    // 注册新账户
    public static void register(String account, String nickname, String password, int userType) throws JSONException, InterruptedException {
        final String DEFAULT_IMAGE_PATH = "android.resource://" + "com.zsh.sight" + "/";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", account);
        jsonObject.put("password", password);
        jsonObject.put("nickname", nickname);
        jsonObject.put("userType", userType);
        jsonObject.put("image", DEFAULT_IMAGE_PATH + R.drawable.default_head);
        String url = "http://121.5.169.147:8000/register";
        String data = HttpUtils.getJsonData(jsonObject, url);

        Log.e("###", "in register" + data.toString());
    }

    // 发送验证码
    public static String send_auth_code(String phone){
        return "123456";
    }

}
