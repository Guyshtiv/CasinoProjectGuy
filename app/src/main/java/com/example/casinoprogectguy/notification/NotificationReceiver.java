package com.example.casinoprogectguy.notification;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.casinoprogectguy.MainActivity;
import com.example.casinoprogectguy.R;

public class NotificationReceiver extends BroadcastReceiver {

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        String message = intent.getStringExtra("textExtra");
        String title = intent.getStringExtra("titleExtra");
        String CHANNEL_ID = "casino";
        Intent it = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder b = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setDefaults(android.app.Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.dolar_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setDefaults(android.app.Notification.DEFAULT_LIGHTS | android.app.Notification.DEFAULT_SOUND);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(1, b.build());
    }
}
