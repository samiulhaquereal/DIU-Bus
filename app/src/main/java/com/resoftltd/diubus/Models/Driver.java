package com.resoftltd.diubus.Models;

public class Driver {
    public String email;
    public String lat;
    public String lng;
    public String name;
    public String password;
    public String vehiclenumber;

    Driver() {
    }

    public Driver(String str, String str2, String str3, String str4, String str5, String str6) {
        this.name = str;
        this.email = str2;
        this.password = str3;
        this.vehiclenumber = str4;
        this.lat = str5;
        this.lng = str6;
    }
}
