package com.xy.shareme_tomcat.data;

public class Member extends ImageObj {
    private String acc, pwd, pwd2;
    private String name, department, email;
    private String positive, negative;
    private String gender;

    public Member() {

    }

    //編輯個人檔案
    public Member(String imgURL, String name, String department, String email, String pwd) {
        super.imgURL = imgURL;
        this.name = name;
        this.department = department;
        this.email = email;
        this.pwd = pwd;
    }

    //顯示個人檔案
    public Member(String imgURL, String name, String department, String positive, String negative, String email) {
        super.imgURL = imgURL;
        this.name = name;
        this.department = department;
        this.email = email;
        this.positive = positive;
        this.negative = negative;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public String getEmail() {
        return email;
    }

    public String getPositive() {
        return positive;
    }

    public String getNegative() {
        return negative;
    }

    public String getAcc() {
        return acc;
    }

    public String getPwd() {
        return pwd;
    }

    public String getPwd2() {
        return pwd2;
    }

    public String getGender() {
        return gender;
    }

    public void setImgURL(String imgURL) {
        this.imgURL = imgURL;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAcc(String acc) {
        this.acc = acc;
    }

    public void setPwd (String pwd) {
        this.pwd = pwd;
    }

    public void setPwd2 (String pwd2) {
        this.pwd2 = pwd2;
    }

    public void setGender (String gender) {
        this.gender = gender;
    }
}
