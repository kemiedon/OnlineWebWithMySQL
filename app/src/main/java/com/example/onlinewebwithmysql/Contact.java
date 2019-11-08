package com.example.onlinewebwithmysql;

import android.graphics.Bitmap;

public class Contact {
    private Bitmap pic;
    private String name;
    private String email;
    private String phoneNum;
    private String birthday;
    public Contact(Bitmap pic, String name, String phone, String email, String birthday) {
        super();
        this.pic = pic;
        this.name = name;
        this.phoneNum = phone;
        this.email = email;
        this.birthday = birthday;
    }
    public Bitmap getPic() {
        return pic;
    }

    public void setPic(Bitmap pic) {
        this.pic = pic;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }
}
