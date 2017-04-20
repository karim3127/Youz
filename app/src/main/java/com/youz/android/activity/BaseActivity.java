package com.youz.android.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.youz.android.R;

import java.util.HashMap;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public abstract class BaseActivity extends AppCompatActivity {

    protected static final String TAG = BaseActivity.class.getName();
    public static boolean isAppWentToBg = false;
    public static boolean isWindowFocused = false;
    public static boolean isMenuOpened = false;
    public static boolean isBackPressed = false;
    public static Context context;


    FirebaseDatabase mRootRef = FirebaseDatabase.getInstance();
    DatabaseReference mUserRef;
    private SharedPreferences prefs;
    private String userId;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        prefs = getSharedPreferences("com.youz.android", Context.MODE_PRIVATE);
        userId = prefs.getString("UserId", "");

        mUserRef = mRootRef.getReference("users").child(userId);
    }

    @Override
    protected void onStart() {
        applicationWillEnterForeground();
        super.onStart();
    }

    private void applicationWillEnterForeground() {
        if (isAppWentToBg) {
            isAppWentToBg = false;

            HashMap<String, Object> status = new HashMap<>();
            status.put("status", "online");
            mUserRef.updateChildren(status);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        applicationdidenterbackground();
    }

    public void applicationdidenterbackground() {
        if (!isWindowFocused) {
            isAppWentToBg = true;

            HashMap<String, Object> status = new HashMap<>();
            status.put("status", "offline");
            mUserRef.updateChildren(status);

            //Crouton.cancelAllCroutons();
        }
    }

    @Override
    public void onBackPressed() {
        if (this instanceof MainActivity) {

        } else {
            isBackPressed = true;
        }
        super.onBackPressed();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        isWindowFocused = hasFocus;
        if (isBackPressed && !hasFocus) {
            isBackPressed = false;
            isWindowFocused = true;
        }

        super.onWindowFocusChanged(hasFocus);
    }

    public static void showMessageCrouton(String message) {
        Style style = new Style.Builder().setBackgroundColorValue(R.color.colorPrimaryDark).build();
        Crouton.makeText((Activity) context, message, style).show();
    }
}