//
// Created by zsh_a on 2021/9/21.
//

#ifndef SIGHT_YOLOV4_H
#define SIGHT_YOLOV4_H

#include "ncnn/net.h"

namespace cv{
    typedef struct{
        int width;
        int height;
    }Size;
}

typedef struct {
    std::string name;
    int stride;
    std::vector<cv::Size> anchors;
}YoloLayerData;


typedef struct BoxInfo {
    float x1;
    float y1;
    float x2;
    float y2;
    float score;
    int label;
} BoxInfo;

class yolov4 {
public:
    yolov4(AAssetManager* mgr, const char* param, const char* bin);
    ~yolov4();
    std::vector<BoxInfo> detect(JNIEnv* env, jobject image);
    std::vector<std::string> labels{"person", "bicycle", "car", "motorcycle", "airplane", "bus", "train", "truck", "boat", "traffic light",
                                    "fire hydrant", "stop sign", "parking meter", "bench", "bird", "cat", "dog", "horse", "sheep", "cow",
                                    "elephant", "bear", "zebra", "giraffe", "backpack", "umbrella", "handbag", "tie", "suitcase", "frisbee",
                                    "skis", "snowboard", "sports ball", "kite", "baseball bat", "baseball glove", "skateboard", "surfboard",
                                    "tennis racket", "bottle", "wine glass", "cup", "fork", "knife", "spoon", "bowl", "banana", "apple",
                                    "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza", "donut", "cake", "chair", "couch",
                                    "potted plant", "bed", "dining table", "toilet", "tv", "laptop", "mouse", "remote", "keyboard", "cell phone",
                                    "microwave", "oven", "toaster", "sink", "refrigerator", "book", "clock", "vase", "scissors", "teddy bear",
                                    "hair drier", "toothbrush"};
private:
    static std::vector<BoxInfo> decode_infer(ncnn::Mat &data,const cv::Size& frame_size);
    ncnn::Net* Net;
    int input_size = 416;
public:
    static yolov4 *detector;
    static bool hasGPU;
};

#endif //SIGHT_YOLOV4_H
