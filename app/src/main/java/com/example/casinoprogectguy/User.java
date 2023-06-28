package com.example.casinoprogectguy;

import android.net.Uri;

public class User {
    private String userName;
    private String email;
    private int money;
    private Uri profileImageUri;

    public User(String userName, String email,Uri profileImageUri) {
        this.userName = userName;
        this.email = email;
        money = 0; //כמות הכסף ההתחלתית של כל משתמש
        this.profileImageUri = profileImageUri;
    }
    public User(String userName, String email) {
        this.userName = userName;
        this.email = email;
        money = 0; //כמות הכסף ההתחלתית של כל משתמש
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public Uri getProfileImageUri() {
        return profileImageUri;
    }

    public void setProfileImageUri(Uri profileImageUri) {
        this.profileImageUri = profileImageUri;
    }
}

