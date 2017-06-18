package com.youz.android.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;

import com.backendless.push.BackendlessBroadcastReceiver;
import com.youz.android.R;
import com.youz.android.activity.BaseActivity;
import com.youz.android.activity.MainActivity;

/**
 * Created by karizmaltd1 on 20/11/15.
 */
public class MyBackendlessPushBroadcastReceiver extends BackendlessBroadcastReceiver {
    public static final String PARSE_DATA_KEY = "com.parse.Data";
    private final String TAG = "MyBackendlessPushBroadcastReceiver";
    public Context context;

    @Override
    public boolean onMessage(Context context, Intent intent) {
        this.context = context;

        SharedPreferences prefs = context.getSharedPreferences("com.youz.android", Context.MODE_PRIVATE);

        if (!prefs.getString("UserId", "").equals("")) {
            if (BaseActivity.isAppWentToBg) {

                Bundle bundle = intent.getExtras();
                Intent notificationIntent = new Intent(context, MainActivity.class);
                notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);

                String alert = "";
                if (bundle != null) {
                    alert = intent.getStringExtra("android-content-title");
                    if (bundle.get("chatId") != null) {
                        notificationIntent.putExtra("chatId", bundle.getString("chatId"));
                    } else if (bundle.get("postId") != null) {
                        notificationIntent.putExtra("postId", bundle.getString("postId"));
                        notificationIntent.putExtra("type", bundle.getString("type"));
                    }
                }

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

                PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(contentIntent);
                builder.setContentTitle("Youz");
                builder.setContentText(alert);
                builder.setAutoCancel(true);
                builder.setSmallIcon(R.drawable.ic_stat_onesignal_default);
                builder.setColor(context.getResources().getColor(R.color.colorPrimary));

                Uri sound = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.notif_sound);
                builder.setSound(sound);

                builder.setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_LIGHTS|Notification.DEFAULT_VIBRATE);

                notificationManager.notify(0, builder.build());

            } else {


            }
        }

        return false;
    }

}
