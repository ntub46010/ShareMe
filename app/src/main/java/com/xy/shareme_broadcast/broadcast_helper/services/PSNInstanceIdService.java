package com.xy.shareme_broadcast.broadcast_helper.services;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.xy.shareme_broadcast.broadcast_helper.managers.UserDataManager;

public class PSNInstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        String currentToken = FirebaseInstanceId.getInstance().getToken();
        UserDataManager.getInstance().setPushToken(currentToken);
    }
}
