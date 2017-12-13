package com.xy.shareme_broadcast.broadcast_helper.managers;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.xy.network.base.IApiResponseListener;
import com.xy.shareme_broadcast.R;
import com.xy.shareme_broadcast.broadcast_helper.PSNApplication;
import com.xy.shareme_broadcast.broadcast_helper.apis.PostFirebaseApiManager;
import com.xy.shareme_broadcast.broadcast_helper.beans.custom.DeviceData;
import com.xy.shareme_broadcast.broadcast_helper.beans.custom.UserData;
import com.xy.shareme_broadcast.broadcast_helper.beans.json.PostFirebaseJb;
import com.xy.shareme_broadcast.broadcast_helper.beans.param.PostFirebasePB;

public class RequestManager {

    private static RequestManager FIREBASE_D2D_MANAGER = null;
    private DatabaseReference databaseRefUsers;
    private PostFirebaseApiManager postFirebaseApiManager;

    private RequestManager() {
        databaseRefUsers = FirebaseDatabase.getInstance().getReference();
        postFirebaseApiManager = PostFirebaseApiManager.getMemberApiManager(PSNApplication.getAPPLICATION());
    }

    public synchronized static final RequestManager getInstance() {
        if (FIREBASE_D2D_MANAGER == null) {
            FIREBASE_D2D_MANAGER = new RequestManager();
        }

        return FIREBASE_D2D_MANAGER;
    }

    public void insertUserPushData(String id) {
        insertUserData(id);
    }

    private void insertUserData(final String id) {
        final DeviceData device = new DeviceData(UserDataManager.getInstance().getPushToken());

        databaseRefUsers.child(UserData.DATABASE_USERS).child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserData userData = dataSnapshot.getValue(UserData.class);

                if (userData == null) {
                    userData = new UserData();
                }

                for (DeviceData pushDevice : userData.getDeviceList()) {
                    if (pushDevice.getToken().equals(device.getToken())) {
                        return;
                    }

                    if (pushDevice.getDevice() != null && pushDevice.getDevice().equals("android")) {
                        return;
                    }
                }

                userData.addDevice(device);
                databaseRefUsers.child(UserData.DATABASE_USERS).child(id).setValue(userData);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void prepareNotification(final String sendId, final String title, final String message, final String photoUrl) {
        databaseRefUsers.child(UserData.DATABASE_USERS).child(sendId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final UserData pushUser = dataSnapshot.getValue(UserData.class);

                if (pushUser == null || pushUser.getDeviceList() == null || pushUser.getDeviceList().size() == 0) {
                    return;
                }

                if (pushUser.getDeviceList() != null) {
                    for (DeviceData device : pushUser.getDeviceList()) {
                        if (device.getDevice() == null) {
                            continue;
                        }
                        pushNotification(title, message, photoUrl, device.getToken());
                    }
                }else {
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void pushNotification(String title, String message, String photoUrl, String targetToken) {
        PostFirebaseJb postFirebaseJb = new PostFirebaseJb();
        postFirebaseJb.setTitle(title);
        postFirebaseJb.setMessage(message);
        postFirebaseJb.setPhotoUrl(photoUrl);
        postFirebaseJb.setTargetToken(targetToken);

        this.postFirebaseApiManager.launchPostFirebaseApi(
                new PostFirebasePB(PSNApplication.getRESOURCE().getString(R.string.key_firebase_server)),
                postFirebaseJb.toJSONObject(),
                new IApiResponseListener<String>() {
                    @Override
                    public void preExecute() {
                    }

                    @Override
                    public void onApiSuccess(String response) {
                    }

                    @Override
                    public void onApiError(String statusCode){
                    }

                    @Override
                    public void postSuccessExecute() {
                    }

                    @Override
                    public void postErrorExecute() {
                    }
                }
        );
    }
}
