package com.xy.shareme_tomcat.broadcast_helper.services;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.xy.shareme_tomcat.R;
import static com.xy.shareme_tomcat.data.DataHelper.haveNewMsg;
import com.xy.shareme_tomcat.broadcast_helper.PSNApplication;
import com.xy.shareme_tomcat.broadcast_helper.constants.KeyData;
import com.xy.shareme_tomcat.broadcast_helper.managers.NotificationManager;

import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

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
                                public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                                    SharedPreferences sp = getSharedPreferences(getString(R.string.sp_fileName), MODE_PRIVATE);
                                    if (sp.getBoolean(getString(R.string.sp_showNotification), true)) {
                                        NotificationManager.getInstance().generateNotification(PSNApplication.getAPPLICATION(), bitmap, title, message);
                                        haveNewMsg = true;
                                    }
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
