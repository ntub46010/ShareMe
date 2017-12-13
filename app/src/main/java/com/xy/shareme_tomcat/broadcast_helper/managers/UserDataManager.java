package com.xy.shareme_tomcat.broadcast_helper.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class UserDataManager {

    private static final String PUSH_TOKEN = "push_token";
    private static UserDataManager INSTANCE = null;

    private SharedPreferences noClearSharedPreferences;
    private SharedPreferences.Editor noClearEditor;
    private String pushToken;

    private UserDataManager(Context context) {
        init(context);
    }

    public synchronized static UserDataManager getInstance() {
        return INSTANCE;
    }

    public synchronized static void initialize(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new UserDataManager(context);
        }
    }

    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
        noClearEditor.putString(PUSH_TOKEN, pushToken);
        noClearEditor.commit();
    }

    public String getPushToken() {
        return pushToken;
    }

    private void init(Context context) {
        noClearSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        noClearEditor = noClearSharedPreferences.edit();
        pushToken = noClearSharedPreferences.getString(PUSH_TOKEN, null);
    }
}
