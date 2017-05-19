package com.youz.android.service;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationDisplayedResult;
import com.onesignal.OSNotificationReceivedResult;
import com.youz.android.activity.BaseActivity;

public class NotificationExtenderExample extends NotificationExtenderService {

   private SharedPreferences prefs;

   @Override
   protected boolean onNotificationProcessing(OSNotificationReceivedResult receivedResult) {
      // Read properties from result.


      prefs = getSharedPreferences("com.youz.android", Context.MODE_PRIVATE);

      if (!prefs.getString("UserId", "").equals("")) {
         if (BaseActivity.isAppWentToBg) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancelAll();

            OverrideSettings overrideSettings = new OverrideSettings();
            /*overrideSettings.extender = new NotificationCompat.Extender() {
               @Override
               public NotificationCompat.Builder extend(NotificationCompat.Builder builder) {

                  builder.setSmallIcon(R.drawable.ic_stat_onesignal_default);
                  builder.setColor(getResources().getColor(R.color.colorPrimary));

                  builder.setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_LIGHTS|Notification.DEFAULT_VIBRATE);

                  Uri sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.onesignal_default_sound);
                  builder.setSound(sound);

                  return builder;
               }
            };*/

            OSNotificationDisplayedResult result = displayNotification(overrideSettings);
            Log.d("OneSignalExample", "Notification displayed with id: " + result.androidNotificationId);
         } else {
            // app is opened

         }
      }

      // Return true to stop the notification from displaying.
      return false;
   }

}