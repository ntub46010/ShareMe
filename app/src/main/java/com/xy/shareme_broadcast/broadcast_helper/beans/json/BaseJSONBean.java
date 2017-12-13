package com.xy.shareme_broadcast.broadcast_helper.beans.json;

import org.json.JSONObject;

import java.io.Serializable;

public abstract class BaseJSONBean implements Serializable {
    protected static final long serialVersionUID = 1L;

    public abstract JSONObject toJSONObject();
}
