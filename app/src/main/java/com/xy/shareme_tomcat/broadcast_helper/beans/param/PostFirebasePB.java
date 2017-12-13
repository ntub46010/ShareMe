package com.xy.shareme_tomcat.broadcast_helper.beans.param;

import com.xy.network.beans.BaseHeaderBean;

public class PostFirebasePB extends BaseHeaderBean {

    public PostFirebasePB(String key) {
        this.header.put("Authorization", key);
        this.header.put("Content-Type", "application/json; charset=utf-8");
    }
}
