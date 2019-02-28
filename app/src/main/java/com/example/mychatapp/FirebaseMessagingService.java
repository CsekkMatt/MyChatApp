package com.example.mychatapp;

import android.app.NotificationManager;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("Friend Request")
                .setContentText("You have revieved a new Friend Request")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        int mNotificationId = (int)System.currentTimeMillis();
        //Gets an instance of the Notification Manager service
        NotificationManager mNotificationMgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mNotificationMgr.notify(mNotificationId,mBuilder.build());
    }
}
