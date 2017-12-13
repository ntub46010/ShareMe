package com.xy.shareme_broadcast.broadcast_helper.beans.json;

import com.xy.shareme_broadcast.broadcast_helper.constants.KeyData;

import org.json.JSONException;
import org.json.JSONObject;

public class PostFirebaseJb extends BaseJSONBean {
    private String photoUrl;
    private String title;
    private String message;
    private String registerId;
    private String targetToken;

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTargetToken() {
        return targetToken;
    }

    public void setTargetToken(String targetToken) {
        this.targetToken = targetToken;
    }

    public String getRegisterId() {
        return registerId;
    }

    public void setRegisterId(String registerId) {
        this.registerId = registerId;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject object = new JSONObject();
        JSONObject dataObj = new JSONObject();

        try {
            dataObj.put(KeyData.PHOTO, getPhotoUrl());
            dataObj.put(KeyData.TITLE, getTitle());
            dataObj.put(KeyData.MESSAGE, getMessage());

            object.put(KeyData.TO, getTargetToken());
            object.put(KeyData.DATA, dataObj);
        }catch (JSONException e) {
            e.printStackTrace();
        }

        return object;
    }
}
