package com.zsh.sight.Utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.zsh.sight.Audio.RecordVoicePopWindow;
import com.zsh.sight.R;
import com.zsh.sight.feature.EditActivity;
import com.zsh.sight.login.LoginActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public interface HttpUtils {
    public int State = 0;
    public static String getJsonData(JSONObject jsonParam, String urls) throws InterruptedException {
        StringBuffer sb=new StringBuffer();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 创建url资源
                    URL url = new URL(urls);
                    // 建立http连接
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    // 设置允许输出
                    conn.setDoOutput(true);
                    // 设置允许输入
                    conn.setDoInput(true);
                    // 设置不用缓存
                    conn.setUseCaches(false);
                    // 设置传递方式
                    conn.setRequestMethod("POST");
                    // 设置维持长连接
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    // 设置文件字符集:
                    conn.setRequestProperty("Charset", "UTF-8");
                    // 转换为字节数组
                    byte[] data = (jsonParam.toString()).getBytes();
                    // 设置文件长度
                    conn.setRequestProperty("Content-Length", String.valueOf(data.length));
                    // 设置文件类型:
                    conn.setRequestProperty("contentType", "application/json");
                    // 开始连接请求
                    conn.connect();
                    OutputStream out = new DataOutputStream(conn.getOutputStream()) ;
                    // 写入请求的字符串
                    out.write((jsonParam.toString()).getBytes());
                    out.flush();
                    out.close();

                    System.out.println(conn.getResponseCode());

                    // 请求返回的状态
                    if (HttpURLConnection.HTTP_OK == conn.getResponseCode()) {
                        System.out.println("连接成功");
                        // 请求返回的数据
                        InputStream in1 = conn.getInputStream();
                        try {
                            String readLine = new String();
                            BufferedReader responseReader = new BufferedReader(new InputStreamReader(in1, "UTF-8"));
                            while ((readLine = responseReader.readLine()) != null) {
                                sb.append(readLine).append("\n");
                            }
                            responseReader.close();
                            System.out.println("in thread" + sb.toString());

                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }else {
                        System.out.println("error++");
                    }
                } catch (Exception e) {
                    Log.e("##", "error");
                }
            }
        });
        int cnt = 0;
        thread.start();
        while(sb.toString().equals("")) {
            cnt += 10;
            if(cnt == 5000)
                return "time out";
            Thread.sleep(10);
        }
        return sb.toString();
    }

    /**
     * 模拟form表单的形式 ，上传文件 以输出流的形式把文件写入到url中，然后用输入流来获取url的响应
     * @param url 请求地址 form表单url地址
     * @param filePath 文件在服务器保存路径
     * @return String url的响应信息返回值a
     * @throws IOException
     */
    public static String send(String url, String filePath) throws InterruptedException {
        StringBuffer buffer = new StringBuffer();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File file = new File(filePath);
                    if (!file.exists() || !file.isFile()) {
                        throw new IOException("文件不存在");
                    }
                    URL urlObj = new URL(url);
                    HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
                    con.setRequestMethod("POST"); // 设置关键值,以Post方式提交表单，默认get方式
                    con.setDoInput(true);
                    con.setDoOutput(true);
                    con.setUseCaches(false); // post方式不能使用缓存
                    con.setRequestMethod("POST");
                    // 设置请求头信息
                    con.setRequestProperty("Connection", "Keep-Alive");
                    con.setRequestProperty("Charset", "UTF-8");
                    // 设置边界
                    String BOUNDARY = "----------" + System.currentTimeMillis();
                    con.setRequestProperty("Content-Type", "multipart/form-data; boundary="+ BOUNDARY);
                    // 请求正文信息
                    // 第一部分：
                    StringBuilder sb = new StringBuilder();
                    sb.append("--"); // 必须多两道线
                    sb.append(BOUNDARY);
                    sb.append("\r\n");
                    sb.append("Content-Disposition: form-data;name=\"file\";filename=\""+ file.getName() + "\"\r\n");
                    sb.append("Content-Type:application/octet-stream\r\n\r\n");
                    byte[] head = sb.toString().getBytes("utf-8");
                    // 获得输出流
                    OutputStream out = new DataOutputStream(con.getOutputStream());
                    // 输出表头
                    out.write(head);
                    // 文件正文部分
                    // 把文件已流文件的方式 推入到url中
                    DataInputStream in = new DataInputStream(new FileInputStream(file));
                    int bytes = 0;
                    byte[] bufferOut = new byte[1024];
                    while ((bytes = in.read(bufferOut)) != -1) {
                        out.write(bufferOut, 0, bytes);
                    }
                    in.close();
                    // 结尾部分
                    byte[] foot = ("\r\n--" + BOUNDARY + "--\r\n").getBytes("utf-8");// 定义最后数据分隔线
                    out.write(foot);
                    out.flush();
                    out.close();
                    BufferedReader reader = null;
                    try {
                        // 定义BufferedReader输入流来读取URL的响应
                        reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            buffer.append(line);
                        }
                    } catch (IOException e) {
                        System.out.println("发送POST请求出现异常！" + e);
                        e.printStackTrace();
                        throw new IOException("数据读取异常");
                    } finally {
                        if (reader != null) {
                            reader.close();
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (ProtocolException e) {
                    e.printStackTrace();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        int cnt = 0;
        thread.start();
        while(buffer.toString().equals("")) {
            cnt += 10;
            if(cnt == 5000) {
                return "time out";
            }
            Thread.sleep(10);
        }
        return buffer.toString();
    }
}
