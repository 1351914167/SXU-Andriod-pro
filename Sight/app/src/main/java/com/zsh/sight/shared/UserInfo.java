package com.zsh.sight.shared;

public class UserInfo{
    private String account;
    private int useType;

    public UserInfo(String account, int useType){
        this.account = account;
        this.useType = useType;
    }

    public String getAccount(){
        return account;
    }

    public int getUserType(){
        return useType;
    }

}
