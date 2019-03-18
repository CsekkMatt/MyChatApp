package com.example.mychatapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        //Get notification Title and Message from NODEJS/Firebase ...
        String notification_title = remoteMessage.getNotification().getTitle();
        String notification_message = remoteMessage.getNotification().getBody();
        String from_user_id = remoteMessage.getData().get("from_user_id");

        //Get ClickAction
        String click_action = remoteMessage.getNotification().getClickAction();

        NotificationManager mNotificationMgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);


        //This part check the SDK version..because needed to set NotificationChannel param for Builder.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default",
                    "YOUR_CHANNEL_NAME",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DISCRIPTION");
            mNotificationMgr.createNotificationChannel(channel);
        }
        //Build foreground notification. with content of background notifications.
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this,"default")
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(notification_title)
                .setContentText(notification_message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent resultIntent = new Intent(click_action);
        Log.w("UserMessage",from_user_id);
        resultIntent.putExtra("user_id",from_user_id);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        int mNotificationId = (int)System.currentTimeMillis();
        //Gets an instance of the Notification Manager service
        mNotificationMgr.notify(mNotificationId,mBuilder.build());
    }
}
