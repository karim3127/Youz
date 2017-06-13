package com.youz.android;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import com.backendless.Backendless;
import com.backendless.DeviceRegistration;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.youz.android.util.BackendlessUtil;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

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
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    public static SecretApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        prefs = getSharedPreferences("com.youz.android", Context.MODE_PRIVATE);
        editor = prefs.edit();

        Backendless.initApp( getApplicationContext(),
                BackendlessUtil.APPLICATION_ID,
                BackendlessUtil.API_KEY );
        Backendless.Messaging.registerDevice("670691093078", "default", new AsyncCallback<Void>() {
            @Override
            public void handleResponse(Void response) {
                Backendless.Messaging.getDeviceRegistration(new AsyncCallback<DeviceRegistration>() {
                    @Override
                    public void handleResponse(DeviceRegistration deviceRegistration) {
                        if (deviceRegistration != null) {
                            editor.putString("DeviceId", deviceRegistration.getDeviceId());
                            editor.commit();
                        }
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {

                    }
                });

            }

            @Override
            public void handleFault(BackendlessFault fault) {

            }
        });

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

        if (prefs.getString("Langage", "").equals("")) {
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

}
