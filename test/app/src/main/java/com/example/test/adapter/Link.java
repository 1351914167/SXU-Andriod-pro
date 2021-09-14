package com.example.test.adapter;

public class Link {
    private String name;
    private String address;
    private boolean ifLinked;

    public Link(String name, String address){
        this.name = name;
        this.address = address;
    }
    public String getName(){
        return name;
    }
    public String getAddress(){
        return address;
    }
    public boolean isLinked(){
        return ifLinked;
    }
    public void setLinked(boolean ifLinked){
        this.ifLinked = ifLinked;
    }
}
