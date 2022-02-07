package com.zsh.sight;

import android.app.Application;

public class MyApplication extends Application {
    public String username;
    public int userType;

    @Override
    public void onCreate(){
        super.onCreate();
    }

    public void setUsername(String username){
        this.username = username;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public String getUsername(){
        return username;
    }

    public int getUserType() {
        return userType;
    }
}
