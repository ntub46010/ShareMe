package com.xy.shareme_broadcast.broadcast_helper.services;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.xy.shareme_broadcast.broadcast_helper.PSNApplication;
import com.xy.shareme_broadcast.broadcast_helper.constants.KeyData;
import com.xy.shareme_broadcast.broadcast_helper.managers.NotificationManager;

import java.util.Map;

public class PSNMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData().size() > 0) {

            Map<String, String> map = remoteMessage.getData();
            final String photo = map.get(KeyData.PHOTO);
            final String title = map.get(KeyData.TITLE);
            final String message = map.get(KeyData.MESSAGE);

            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Glide.with(PSNApplication.getAPPLICATION())
                            .load(photo)
                            .asBitmap()
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                    NotificationManager.getInstance().generateNotification(PSNApplication.getAPPLICATION(), resource, title, message);
                                }

                                @Override
                                public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                    super.onLoadFailed(e, errorDrawable);
                                    NotificationManager.getInstance().generateNotification(PSNApplication.getAPPLICATION(), title, message);
                                }
                            });
                }
            });
        }
    }
}
