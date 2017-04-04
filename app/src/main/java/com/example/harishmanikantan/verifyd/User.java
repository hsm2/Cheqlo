package com.example.harishmanikantan.verifyd;

import com.orm.SugarRecord;

public class User extends SugarRecord{

    private String name, username, email, password;

    public User(){

    }

    public User(String name,String username, String email, String password){
        this.name = name;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public User(String email, String password){
        this.email=email;
        this.password=password;
        this.name="";
        this.username="";
    }

    public String getName(){
        return name;
    }

    public String getUsername(){
        return username;
    }

    public String getEmail(){
        return email;
    }

    public String getPassword(){
        return password;
    }
}
