package com.zsh.sight.detect;

import android.graphics.Color;
import android.graphics.RectF;

import java.util.Random;

public class Box {
    public float x0,y0,x1,y1;
    private int label;
    private float score;

    private static String[] labels={"人", "自行车", "汽车", "摩托车", "飞机", "公共汽车", "火车",
            "卡车", "小船", "红绿灯", "消防栓", "停车牌", "停车收费表", "长椅", "鸟", "猫", "狗",
            "马", "绵羊", "奶牛", "大象", "熊", "斑马", "长颈鹿", "书包", "雨伞", "手提包", "领带",
            "手提箱", "飞盘", "滑雪", "滑雪板", "运动球", "风筝", "棒球棍", "棒球手套","滑板鞋", "冲浪板",
            "网球拍", "瓶子", "酒杯", "杯子", "叉子", "小刀", "勺子", "碗", "香蕉", "苹果",
            "三明治", "橘子", "西兰花", "胡萝卜", "热狗", "披萨", "甜甜圈", "蛋糕", "椅子", "沙发",
            "盆栽", "床", "餐桌", "厕所", "屏幕", "笔记本", "鼠标", "偏僻", "键盘", "手机",
            "微波", "烤箱", "烤面包机", "水槽", "冰箱", "书本", "时钟", "花瓶", "剪刀", "泰迪熊",
            "吹风机", "牙刷"};

    public Box(float x0,float y0, float x1, float y1, int label, float score){
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        this.label = label;
        this.score = score;
    }

    public RectF getRect(){
        return new RectF(x0,y0,x1,y1);
    }

    public String getLabel(){
        return labels[label];
    }

    public float getScore(){
        return score;
    }

    public int getColor(){
        Random random = new Random(label);
        return Color.argb(255,random.nextInt(256),random.nextInt(256),random.nextInt(256));
    }
}
