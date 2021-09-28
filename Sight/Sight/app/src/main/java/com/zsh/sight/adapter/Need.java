package com.zsh.sight.adapter;

public class Need {
    private int id, account, mark;
    private String name, time, contend;
    // 构造函数
    public Need(int id, int account, String name, String time, String contend, int mark){
        this.id = id;
        this.account = account;
        this.name = name;
        this.time = time;
        this.contend = contend;
        this.mark = mark;
    }
    // 访问器
    public int getId(){
        return id;
    }
    public int getAccount(){
        return account;
    }
    public String getName(){
        return name;
    }
    public String getTime(){
        return time;
    }
    public String getContend(){
        return contend;
    }
    public int getMark(){
        return mark;
    }
    // 设置器
    public void setName(String name){
        this.name = name;
    }
    public void setTime(String time){
        this.time = time;
    }
    public void setLContend(String contend){
        this.contend = contend;
    }
    public void setMark(int mark){
        this.mark = mark;
    }
}
