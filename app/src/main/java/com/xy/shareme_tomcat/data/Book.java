package com.xy.shareme_tomcat.data;

import java.io.Serializable;

public class Book extends ImageObj implements Serializable {
    private String id, title;
    private String price, dep, seller, sellerName, sellerAvatar;
    private String status, note, ps, postDate, editDate;

    public Book(String id, String imgURL, String title) {
        this.id = id;
        super.imgURL = imgURL;
        this.title = title;
    }

    public Book(String id, String imgURL, String title, String price) {
        this.id = id;
        super.imgURL = imgURL;
        this.title = title;
        this.price = price;
    }

    public Book(String id, String imgURL, String title, String price, String seller) {
        this.id = id;
        super.imgURL = imgURL;
        this.title = title;
        this.price = price;
        this.seller = seller;
    }

    //用於商品編輯
    public Book(String id, String imgURL, String imgURL2, String imgURL3, String imgURL4, String imgURL5, String title, String status, String note, String price, String ps, String seller) {
        this.id = id;
        super.imgURL = imgURL;
        super.imgURL2 = imgURL2;
        super.imgURL3 = imgURL3;
        super.imgURL4 = imgURL4;
        super.imgURL5 = imgURL5;
        this.title = title;
        this.price = price;
        this.seller = seller;
        this.status = status;
        this.note = note;
        this.ps = ps;
    }

    //用於商品詳情、聊天室
    public Book(String id, String imgURL, String imgURL2, String imgURL3, String imgURL4, String imgURL5, String title, String status, String note, String price, String ps, String seller, String sellerName, String dep, String postDate, String editDate) {
        this.id = id;
        super.imgURL = imgURL;
        super.imgURL2 = imgURL2;
        super.imgURL3 = imgURL3;
        super.imgURL4 = imgURL4;
        super.imgURL5 = imgURL5;
        this.title = title;
        this.price = price;
        this.dep = dep;
        this.seller = seller;
        this.sellerName = sellerName;
        this.status = status;
        this.note = note;
        this.ps = ps;
        this.postDate = postDate;
        this.editDate = editDate;
    }

    public Book(String id, String imgURL, String title, String status, String note, String price, String ps, String seller, String sellerName) {
        this.id = id;
        super.imgURL = imgURL;
        this.title = title;
        this.price = price;
        this.seller = seller;
        this.sellerName = sellerName;
        this.status = status;
        this.note = note;
        this.ps = ps;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getPrice() {
        return price;
    }

    public String getDep() {
        return dep;
    }

    public String getSeller() {
        return this.seller;
    }

    public String getSellerName() {
        return this.sellerName;
    }

    public String getStatus(){return this.status;}

    public String getNote(){return this.note;}

    public String getPs(){return this.ps;}

    public String getPostDate(){return this.postDate;}

    public String getEditDate(){return this.editDate;}

    public void setTitle(String title) {
        this.title = title;
    }

}