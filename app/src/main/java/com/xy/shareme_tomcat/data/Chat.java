package com.xy.shareme_tomcat.data;

public class Chat extends ImageObj {
    //private String imgURL;
    private String msg, date, time;
    private String name, productId, title, memberId, sender;

    public Chat(String sender, String msg, String date, String time) { //用於聊天室每則訊息
        this.sender = sender;
        this.msg = msg;
        this.date = date;
        this.time = time;
    }

    public Chat(String imgURL, String name, String msg, String date, String time, String productId, String title, String memberId) { //信箱
        super.imgURL = imgURL;
        this.msg = msg;
        this.date = date;
        this.time = time;
        this.name = name;
        this.productId = productId;
        this.title = title;
        this.memberId = memberId;
    }

    /*public String getImgURL() {
        return imgURL;
    }
    */

    public String getMsg() {
        return msg;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getName() {
        return name;
    }

    public String getProductId() {
        return productId;
    }

    public String getTitle() {
        return title;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getSender() {
        return sender;
    }

}
