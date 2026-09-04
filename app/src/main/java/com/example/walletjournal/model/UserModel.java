package com.example.walletjournal.model;

public class UserModel {
    private String name;
    private int age;

    public UserModel(String name,int age){
        this.name = name;
        this.age = age;
    }

    public String getName(){return name;}
    public int getAge(){return age;}
}
