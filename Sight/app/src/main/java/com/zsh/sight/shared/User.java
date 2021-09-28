package com.zsh.sight.shared;

public class User {
    private String account;
    private String password;

    public User(String account, String password){
        this.account = account;
        this.password = password;
    }

    public String getAccount(){
        return account;
    }

    public String getPassword(){
        return password;
    }
}
