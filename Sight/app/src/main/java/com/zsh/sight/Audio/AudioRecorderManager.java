package com.zsh.sight.Audio;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Executors;

public class AudioRecorderManager {
    //    audioResource:音频采集的来源
//    audioSampleRate:音频采样率
//    channelConfig:声道
//    audioFormat:音频采样精度，指定采样的数据的格式和每次采样的大小。
//    bufferSizeInBytes：AudioRecord 采集到的音频数据所存放的缓冲区大小。获取最小的缓冲区大小，用于存放AudioRecord采集到的音频数据。


    private boolean isRecording;
    //指的是麦克风
    private int audioSource = MediaRecorder.AudioSource.MIC;
    //指定采样率 （MediaRecoder 的采样率通常是8000Hz AAC的通常是44100Hz。 设置采样率为44100，目前为常用的采样率，官方文档表示这个值可以兼容所有的设置）
    private int sampleRateInHz = 16000;
    //指定捕获音频的声道数目。在AudioFormat类中指定用于此的常量  (单声道CHANNEL_IN_MONO就是一个喇叭 ，双声道CHANNEL_IN_STEREO两个喇叭 )
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    //指定音频量化位数 ,在AudioFormaat类中指定了以下各种可能的常量。通常我们选择ENCODING_PCM_16BIT和ENCODING_PCM_8BIT PCM代表的是脉冲编码调制，它实际上是原始音频样本。
    //因此可以设置每个样本的分辨率为16位或者8位，16位将占用更多的空间和处理能力,表示的音频也更加接近真实。
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    //指定缓冲区大小。调用AudioRecord类的getMinBufferSize方法可以获得。
    //特别注意：这个大小不能随便设置，AudioRecord 提供对应的 API 来获取这个值。
    private int bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
    /**/
    private AudioRecord audioRecord;

    private static AudioRecorderManager manager;

    public String filePath;
    private Context context;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static AudioRecorderManager newInstance(Context context) {
        if (null == manager) {
            synchronized (AudioRecorderManager.class) {
                if (null == manager) {
                    manager = new AudioRecorderManager(context);
                }
            }
        }
        return manager;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private AudioRecorderManager(Context context) {
        this.context = context;
        /*实例化音频捕获的实例*/
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.e("####", "in audio permission check");
        }
        audioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);
        Log.e("####", audioRecord.getFormat()+ " ");
    }


    /**
     * 开始录音
     */
    public void startRecording(){
        /**
         *  还没准备
         *  AudioRecord.STATE_UNINITIALIZED ;
         *
         *  准备完成
         *  AudioRecord.STATE_INITIALIZED
         *
         *  开始采集之后，状态自动变为
         *  AudioRecord.RECORDSTATE_RECORDING
         *
         *  停止采集时调用mAudioRecord.stop()停止录音。
         *  AudioRecord.RECORDSTATE_STOPPED
         *
         */
        if (AudioRecord.ERROR_BAD_VALUE == bufferSizeInBytes || AudioRecord.ERROR == bufferSizeInBytes) {
            throw new RuntimeException("Unable to getMinBufferSize");
        }

        //bufferSizeInBytes is available...
        //或者检测AudioRecord是否确保了获得适当的硬件资源。
        if (audioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
            throw new RuntimeException("The AudioRecord is not uninitialized");
        }


        if (audioRecord.getState()==AudioRecord.STATE_INITIALIZED){
            new Thread(new RecorderRunnable()).start();
        }
    }





    public class RecorderRunnable implements Runnable{

        @Override
        public void run() {

            isRecording = true ;

            try {
                String rootPath = context.getExternalFilesDir(null).getAbsolutePath();
                File file= new File(rootPath,System.currentTimeMillis()+".pcm");

                if (!file.exists()){
                    file.createNewFile();
                }

                /*开始录音*/
                audioRecord.startRecording();

                byte[] bytes = new byte[bufferSizeInBytes];
                /*缓存输入流*/
                BufferedOutputStream bufferedOutputStream=new BufferedOutputStream(new FileOutputStream(file));
                DataOutputStream outputStream=new DataOutputStream(bufferedOutputStream);
                while (isRecording){
                    if (!isPause){//录音开关
                        /*如果处于录音状态  那么不断的读取录音结果*/
                        int bufferReadResult= audioRecord.read(bytes, 0 ,bufferSizeInBytes);
                        for (int i = 0; i < bufferReadResult; i++) {
                            outputStream.write(bytes[i]);
                        }
                    }
                }


                filePath = file.getAbsolutePath();
/*                *//*转换成wav*//*
                PcmToWavUtil pcmToWavUtil = new PcmToWavUtil(sampleRateInHz,channelConfig,audioFormat);
                *//*转换后文件*//*
                File outFile=new File(rootPath,System.currentTimeMillis()+".wav");
                pcmToWavUtil.pcmToWav(file.getAbsolutePath(),outFile.getAbsolutePath());
                *//*播放文件地址*//*
                playFilePath  =  outFile.getAbsolutePath() ;*/
                bufferedOutputStream.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 是否暂停开关
     */
    private boolean isPause = false;



    /**
     * 暂停
     */
    public void pause(){
        isPause = true ;
    }


    /**
     * 恢复
     */
    public void resume(){
        isPause = false ;
    }

    public String getFilePath() {
        return filePath;
    }

    /**
     * 停止录音
     */
    public void stopRecord() {
        isRecording = false;
        //停止录音，回收AudioRecord对象，释放内存
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
            manager = null;
        }
        Toast.makeText(context, "录制成功", Toast.LENGTH_LONG);
    }

    public void deleteFile(String filePath){
        File file = new File(filePath);
        file.delete();
    }



    AudioTrack mAudioTrack;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void initPlayRecord(){
        AudioFormat audioF=new AudioFormat.Builder()
                .setSampleRate(sampleRateInHz)
                .setEncoding(audioFormat)
                .setChannelMask(channelConfig)
                .build();
        AudioAttributes mAudioAttributes=new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        mAudioTrack=new AudioTrack(mAudioAttributes,audioF,bufferSizeInBytes, AudioTrack.MODE_STREAM, AudioManager.AUDIO_SESSION_ID_GENERATE);
    }



    /**
     * 播放录音
     */

    String playFilePath ;
    public void playRecord(Context context){
        if (TextUtils.isEmpty(playFilePath)){
            Toast.makeText(context,"播放音频文件不存在！", Toast.LENGTH_LONG).show();
            return;
        }
        mAudioTrack.play();
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    FileInputStream fileInputStream=new FileInputStream(playFilePath);
                    byte[] tempBuffer=new byte[bufferSizeInBytes];
                    while (fileInputStream.available()>0){
                        int readCount= fileInputStream.read(tempBuffer);
                        if (readCount == AudioTrack.ERROR_INVALID_OPERATION||readCount==AudioTrack.ERROR_BAD_VALUE){
                            continue;
                        }
                        if (readCount!=0&&readCount!=-1){
                            mAudioTrack.write(tempBuffer,0,readCount);
                        }
                    }
                    Log.e("TAG","end");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
