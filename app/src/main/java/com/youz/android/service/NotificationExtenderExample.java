package com.youz.android.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationDisplayedResult;
import com.onesignal.OSNotificationPayload;
import com.youz.android.R;
import com.youz.android.activity.BaseActivity;

public class NotificationExtenderExample extends NotificationExtenderService {

   private SharedPreferences prefs;

   @Override
   protected boolean onNotificationProcessing(OSNotificationPayload notification) {

      try {
         ContentValues cv = new ContentValues();
         cv.put("badgecount", 0);
         getContentResolver().update(Uri.parse("content://com.sec.badge/apps"), cv, "package=?", new String[] {getPackageName()});
      } catch (Exception ex) {

      }

      prefs = getSharedPreferences("com.youz.android", Context.MODE_PRIVATE);

      if (!prefs.getString("UserId", "").equals("")) {
         if (BaseActivity.isAppWentToBg) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancelAll();

            OverrideSettings overrideSettings = new OverrideSettings();
            overrideSettings.extender = new NotificationCompat.Extender() {
               @Override
               public NotificationCompat.Builder extend(NotificationCompat.Builder builder) {

                  builder.setSmallIcon(R.drawable.ic_logo_header);
                  builder.setColor(getResources().getColor(R.color.colorPrimary));

                  builder.setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_LIGHTS|Notification.DEFAULT_VIBRATE);

                  Uri sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notification);
                  builder.setSound(sound);

                  return builder;
               }
            };

            OSNotificationDisplayedResult result = displayNotification(overrideSettings);
            Log.d("OneSignalExample", "Notification displayed with id: " + result.notificationId);
         } else {
            // app is opened

         }
      }
      return true;
   }

}