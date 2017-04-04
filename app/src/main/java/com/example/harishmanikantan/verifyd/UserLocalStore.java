package com.example.harishmanikantan.verifyd;

import android.content.Context;
import android.content.SharedPreferences;

public class UserLocalStore {

    public static final String SP_NAME = "userDetails";
    SharedPreferences userLocalDatabase;

    public UserLocalStore(Context context){
        userLocalDatabase = context.getSharedPreferences(SP_NAME,0);
    }

    public void storeUserData(User user){
        SharedPreferences.Editor spEditor = userLocalDatabase.edit();
        spEditor.putString("name",user.getName());
        spEditor.putString("username",user.getUsername());
        spEditor.putString("email",user.getEmail());
        spEditor.putString("password",user.getPassword());
        spEditor.commit();
    }

    public User getLoggedInUser(){
        String name = userLocalDatabase.getString("name","");
        String username = userLocalDatabase.getString("username","");
        String email = userLocalDatabase.getString("email","");
        String password = userLocalDatabase.getString("password","");

        User storedUser = new User(name,username,email,password);
        return storedUser;
    }

    public void setUserLoggedIn(boolean loggedIn){
        SharedPreferences.Editor spEditor = userLocalDatabase.edit();
        spEditor.putBoolean("loggedIn",loggedIn);
        spEditor.commit();
    }

    public boolean isUserLoggedIn(){
        return userLocalDatabase.getBoolean("loggedIn",false);
    }

    public void clearUserData(){
        SharedPreferences.Editor spEditor = userLocalDatabase.edit();
        spEditor.clear();
        spEditor.commit();
    }
}
