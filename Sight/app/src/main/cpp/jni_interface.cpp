//
// Created by zsh_a on 2021/9/21.
//

#include <jni.h>
#include <string>
#include <ncnn/gpu.h>
#include <android/asset_manager_jni.h>
#include <android/log.h>
#include "yolov4.h"

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    ncnn::create_gpu_instance();
    if(ncnn::get_gpu_count() > 0){
        yolov4::hasGPU = true;
    }
    return JNI_VERSION_1_4;
}

JNIEXPORT void JNI_OnUnload(JavaVM* vm, void* reserved)
{
    ncnn::destroy_gpu_instance();
}

extern "C" JNIEXPORT void JNICALL
Java_com_zsh_sight_detect_YOLO_init(JNIEnv* env, jclass, jobject assetManager){
    if(yolov4::detector == nullptr){
        AAssetManager* mgr = AAssetManager_fromJava(env, assetManager);
        yolov4::detector = new yolov4(mgr, "yolov4-tiny.param", "yolov4-tiny.bin");
    }
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_zsh_sight_detect_YOLO_detect(JNIEnv* env, jclass, jobject image, jdouble threshold, jdouble nms_threshold) {
    auto result = yolov4::detector->detect(env, image);
    auto box_cls = env->FindClass("com/zsh/sight/detect/Box");
    auto cid = env->GetMethodID(box_cls, "<init>", "(FFFFIF)V");
    jobjectArray ret = env->NewObjectArray( result.size(), box_cls, nullptr);
    int i = 0;
    for(auto& box:result){
        env->PushLocalFrame(1);
        jobject obj = env->NewObject(box_cls, cid,box.x1,box.y1,box.x2,box.y2,box.label,box.score);
        obj = env->PopLocalFrame(obj);
        env->SetObjectArrayElement( ret, i++, obj);
    }
    return ret;
}
