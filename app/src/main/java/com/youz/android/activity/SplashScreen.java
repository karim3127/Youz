package com.youz.android.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import butterknife.ButterKnife;

public class SplashScreen extends AppCompatActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.youz.android",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }


        prefs = getSharedPreferences("com.youz.android", Context.MODE_PRIVATE);

        if (!prefs.getString("UserId", "").equals("")) {
            startActivity(new Intent(this, MainActivity.class));
           /* if(!prefs.getString("CountryName", "").equals(""))
                startActivity(new Intent(this, MainActivity.class));
            else
                startActivity(new Intent(this, GetLocationActivity.class));*/
            //MainActivity.class
        } else {
            startActivity(new Intent(this, IntroAnimation.class));
        }
        finish();
    }

}
