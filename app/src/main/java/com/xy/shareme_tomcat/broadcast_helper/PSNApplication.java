package com.xy.shareme_tomcat.broadcast_helper;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import com.xy.network.helpers.VolleyRequestHelper;
import com.xy.shareme_tomcat.broadcast_helper.managers.UserDataManager;

public class PSNApplication extends Application {
    private static Context APPLICATION;
    private static Resources RESOURCE;

    @Override
    public void onCreate() {
        super.onCreate();
        this.initData();
    }

    public static Context getAPPLICATION() {
        return APPLICATION;
    }

    public static Resources getRESOURCE() {
        return RESOURCE;
    }

    private void initData() {
        APPLICATION = this;
        RESOURCE = this.getResources();

        VolleyRequestHelper.init(this);
        UserDataManager.initialize(this.getApplicationContext());
    }
}
