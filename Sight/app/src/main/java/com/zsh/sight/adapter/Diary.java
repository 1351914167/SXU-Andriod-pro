package com.zsh.sight.adapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Diary {

    private String headPath;
    private String nickname;
    private String contend;
    private ArrayList<String> imgPathList;
    private Date date;

    public Diary(String headPath, String nickname, String contend, List<String> imgPathList){
        this.headPath = headPath;
        this.nickname = nickname;
        this.contend = contend;

        this.imgPathList = new ArrayList<String>();
        this.imgPathList.addAll(imgPathList);
    }

    public String getHeadPath() {
        return headPath;
    }

    public String getNickname() {
        return nickname;
    }

    public String getContend() {
        return contend;
    }

    public int getImgNum(){
        return imgPathList.size();
    }

    public String getImgPath(int pos){
        return imgPathList.get(pos);
    }

    public List<String> getUrlList(){
        return imgPathList;
    }

    public ArrayList<String> getUrlArrayList(){
        return imgPathList;
    }

    public Date getDate() {
        return date;
    }
}
