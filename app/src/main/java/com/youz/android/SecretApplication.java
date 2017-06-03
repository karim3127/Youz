package com.youz.android;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.util.Log;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.onesignal.OSNotificationAction;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;
import com.youz.android.activity.BaseActivity;
import com.youz.android.activity.MainActivity;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by karizmaltd1 on 29/03/2016.
 */

@ReportsCrashes(
        formUri = "",
        mailTo = "faresyaakoub@gmail.com"
)

public class SecretApplication extends Application {

    private static SecretApplication instance;

    public static SecretApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .setNotificationOpenedHandler(new SecretNotificationOpenedHandler())
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        ButterKnife.setDebug(BuildConfig.DEBUG);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Optima-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(getApplicationContext());
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(250 * 1024 * 1024); // 250 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
        config.writeDebugLogs(); // Remove for release app
        ImageLoader.getInstance().init(config.build());

        String defaultLangage = Locale.getDefault().getLanguage();
        SharedPreferences prefs = getSharedPreferences("com.youz.android", Context.MODE_PRIVATE);
        if (prefs.getString("Langage", "").equals("")) {
            SharedPreferences.Editor editor = prefs.edit();
            if (defaultLangage.equals("ar")) {
                editor.putString("Langage", "en_AU");
            } else if (defaultLangage.equals("fr")) {
                editor.putString("Langage", "en_CA");
            }else {
                editor.putString("Langage", "en_US");
            }
            editor.apply();
        }

        Locale locale = new Locale(prefs.getString("Langage", "en_US"));
        Locale.setDefault(locale);
        Configuration configLanguage = new Configuration();
        configLanguage.locale = locale;
        getBaseContext().getResources().updateConfiguration(configLanguage, getBaseContext().getResources().getDisplayMetrics());

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        ACRA.init(this);
    }

    class SecretNotificationOpenedHandler implements OneSignal.NotificationOpenedHandler {
        // This fires when a notification is opened by tapping on it.
        @Override
        public void notificationOpened(OSNotificationOpenResult result) {
            OSNotificationAction.ActionType actionType = result.action.type;
            JSONObject additionalData = result.notification.payload.additionalData;
            String customKey;

            if (additionalData != null) {
                customKey = additionalData.optString("customkey", null);
                if (customKey != null)
                    Log.i("OneSignalExample", "customkey set with value: " + customKey);
            }

            if (actionType == OSNotificationAction.ActionType.ActionTaken)
                Log.i("OneSignalExample", "Button pressed with id: " + result.action.actionID);


            if (BaseActivity.isAppWentToBg) {
                Intent intent = new Intent(instance, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);

                if (additionalData != null) {
                    try {
                        if (additionalData.has("chatId")) {
                            intent.putExtra("chatId", additionalData.getString("chatId"));
                        } else if (additionalData.has("invitationId")) {
                            intent.putExtra("invitationId", additionalData.getString("invitationId"));
                        } else if (additionalData.has("requestUserId")) {
                            intent.putExtra("requestUserId", additionalData.getString("requestUserId"));
                        } else if (additionalData.has("contactUserId")) {
                            intent.putExtra("contactUserId", additionalData.getString("contactUserId"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                startActivity(intent);

            } else {
                Intent intent = new Intent(instance, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);

                if (additionalData != null) {
                    try {
                        if (additionalData.has("chatId")) {
                            intent.putExtra("chatId", additionalData.getString("chatId"));
                        } else if (additionalData.has("postId")) {
                            intent.putExtra("postId", additionalData.getString("postId"));
                            intent.putExtra("type", additionalData.getString("type"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                startActivity(intent);
            }

            // The following can be used to open an Activity of your choice.
            // Replace - getApplicationContext() - with any Android Context.
            // Intent intent = new Intent(getApplicationContext(), YourActivity.class);
            // intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
            // startActivity(intent);

            // Add the following to your AndroidManifest.xml to prevent the launching of your main Activity
            //   if you are calling startActivity above.
     /*
        <application ...>
          <meta-data android:name="com.onesignal.NotificationOpened.DEFAULT" android:value="DISABLE" />
        </application>
     */
        }
    }

}
