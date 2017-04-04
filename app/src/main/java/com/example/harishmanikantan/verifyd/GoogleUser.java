package com.example.harishmanikantan.verifyd;

import android.net.Uri;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class GoogleUser {

    private String name;
    private Uri photoUrl;
    private boolean isChecked;
    private String uid;

    public GoogleUser(String name, Uri photoUrl,String uid){
        this.name = name;
        this.photoUrl = photoUrl;
        this.isChecked = false;
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public Uri getPhotoUrl() {
        return photoUrl;
    }

    public String getUid(){
        return uid;
    }

    public void setChecked(boolean flag){
        isChecked = flag;
    }

    public boolean isChecked(){
        return isChecked;
    }
}
