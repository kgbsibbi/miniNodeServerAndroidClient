package com.codesample.mymemo.data;

public class User {
    public String userid;
    public String name;
    public String password;
    public String token;

    public User(){ }
    public User(String userid, String password){
        this.userid=userid;
        this.password=password;
    }
    public User(String userid, String password, String name){
        this.userid=userid;
        this.password=password;
        this.name=name;
    }
}
