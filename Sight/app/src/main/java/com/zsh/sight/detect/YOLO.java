package com.zsh.sight.detect;

import android.content.res.AssetManager;
import android.graphics.Bitmap;

import com.zsh.sight.detect.Box;

public class YOLO {
    static {
        System.loadLibrary("yolov5");
    }

    public static native void init(AssetManager manager);
    public static native Box[] detect(Bitmap bitmap, double threshold, double nms_threshold);
}
