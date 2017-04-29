package com.youz.android;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
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

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        instance = this;

        OneSignal.startInit(this)
                .setAutoPromptLocation(true)
                .setNotificationOpenedHandler(new MyNotificationOpenedHandler())
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
            if (defaultLangage.equals("ar") ) {
                editor.putString("Langage", "ar");
            } else if (defaultLangage.equals("fr") ) {
                editor.putString("Langage", "fr");
            }else {
                editor.putString("Langage", "en");
            }
            editor.apply();
        }

        Locale locale = new Locale(prefs.getString("Langage", "en"));
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

    // This fires when a notification is opened by tapping on it or one is received while the app is running.
    private class MyNotificationOpenedHandler implements OneSignal.NotificationOpenedHandler {
        @Override
        public void notificationOpened(String message, JSONObject additionalData, boolean isActive) {
            try {
                if (additionalData != null) {
                    if (additionalData.has("actionSelected"))
                        Log.d("OneSignalExample", "OneSignal notification button with id " + additionalData.getString("actionSelected") + " pressed");

                    Log.d("OneSignalExample", "Full additionalData:\n" + additionalData.toString());
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }

            // The following can be used to open an Activity of your choice.

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

        }
    }
}
