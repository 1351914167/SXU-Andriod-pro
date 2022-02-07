package com.zsh.sight.recruit;

public class Job {
    private String company;
    private String position;
    private String require;
    private String address;
    private String contact;
    private int salary;
    private int number;

    public Job(String company, String position, String require, String address, String contact, int salary, int number){
        this.company = company;
        this.position = position;
        this.require = require;
        this.address = address;
        this.contact = contact;
        this.salary = salary;
        this.number = number;
    }

    public String getCompany(){
        return company;
    }

    public String getPosition(){
        return position;
    }

    public String getRequire(){
        return require;
    }

    public String getAddress(){
        return address;
    }

    public String getContact(){
        return contact;
    }

    public int getSalary(){
        return salary;
    }

    public int getNumber(){
        return number;
    }
}
