package com.youz.android.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.share.model.AppInviteContent;
import com.facebook.share.widget.AppInviteDialog;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rey.material.widget.RadioButton;
import com.rey.material.widget.Switch;
import com.youz.android.R;

import java.util.HashMap;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Setting extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.tv_toolbar)
    TextView tvToolbar;

    @BindView(R.id.switch_all)
    Switch switchAll;

    @BindView(R.id.switch_like)
    Switch switchLike;

    @BindView(R.id.switch_new_post)
    Switch switchNewPost;

    @BindView(R.id.switch_reyouz)
    Switch switchReyouz;

    @BindView(R.id.switch_message)
    Switch switchMessage;

    @BindView(R.id.switch_comment)
    Switch switchComment;

    @BindView(R.id.rb_public)
    RadioButton rbPublic;

    @BindView(R.id.rb_friend)
    RadioButton rbFriend;

    private Typeface typeFaceGras;
    boolean isAllTouched = false;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private String userId;
    String defaultLangage;

    FirebaseDatabase mRootRef = FirebaseDatabase.getInstance();
    DatabaseReference mUserRef;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);

        prefs = getSharedPreferences("com.youz.android", Context.MODE_PRIVATE);
        editor = prefs.edit();
        userId = prefs.getString("UserId", "");

        mUserRef = mRootRef.getReference("users").child(userId);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        typeFaceGras = Typeface.createFromAsset(getAssets(), "fonts/optima_bold.ttf");
        tvToolbar.setTypeface(typeFaceGras);

        switchMessage.setChecked(prefs.getBoolean("notifsChats", true));
        switchReyouz.setChecked(prefs.getBoolean("notifsShares", true));
        switchLike.setChecked(prefs.getBoolean("notifsLikes", true));
        switchComment.setChecked(prefs.getBoolean("notifsComments", true));
        switchNewPost.setChecked(prefs.getBoolean("notifsPosts", true));
        switchAll.setChecked(switchComment.isChecked() && switchMessage.isChecked() && switchNewPost.isChecked() && switchLike.isChecked());

        rbPublic.setChecked(prefs.getBoolean("enablePublicChat", true));
        rbFriend.setChecked(!prefs.getBoolean("enablePublicChat", true));

        switchAll.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(Switch view, boolean checked) {
                if (checked && isAllTouched) {
                    switchLike.setChecked(true);
                    switchNewPost.setChecked(true);
                    switchReyouz.setChecked(true);
                    switchMessage.setChecked(true);
                    switchComment.setChecked(true);
                } else if (isAllTouched){
                    switchLike.setChecked(false);
                    switchReyouz.setChecked(false);
                    switchNewPost.setChecked(false);
                    switchMessage.setChecked(false);
                    switchComment.setChecked(false);
                }
            }
        });

        switchAll.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    isAllTouched = true;
                }

                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isAllTouched = false;
                        }
                    }, 200);
                }

                return false;
            }
        });

        switchLike.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(Switch view, boolean checked) {
                if (!checked) {
                    switchAll.setChecked(false);
                    changeNotifPermission("notifsLikes", false);
                } else {
                    changeNotifPermission("notifsLikes", true);
                    if (switchNewPost.isChecked() && switchComment.isChecked() && switchMessage.isChecked() && switchReyouz.isChecked()) {
                        switchAll.setChecked(true);
                    }
                }
            }
        });

        switchNewPost.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(Switch view, boolean checked) {
                if (!checked) {
                    switchAll.setChecked(false);
                    changeNotifPermission("notifsPosts", false);
                } else {
                    changeNotifPermission("notifsPosts", true);
                    if (switchLike.isChecked() && switchComment.isChecked() && switchMessage.isChecked() && switchReyouz.isChecked()){
                        switchAll.setChecked(true);
                    }
                }
            }
        });

        switchComment.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(Switch view, boolean checked) {
                if (!checked) {
                    switchAll.setChecked(false);
                    changeNotifPermission("notifsComments", false);
                } else {
                    changeNotifPermission("notifsComments", true);
                    if (switchLike.isChecked() && switchNewPost.isChecked() && switchMessage.isChecked() && switchReyouz.isChecked()){
                        switchAll.setChecked(true);
                    }
                }
            }
        });

        switchMessage.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(Switch view, boolean checked) {
                if (!checked) {
                    switchAll.setChecked(false);
                    changeNotifPermission("notifsChats", false);
                } else {
                    changeNotifPermission("notifsChats", true);
                    if (switchLike.isChecked() && switchComment.isChecked() && switchNewPost.isChecked() && switchReyouz.isChecked()){
                        switchAll.setChecked(true);
                    }
                }
            }
        });

        switchReyouz.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(Switch view, boolean checked) {
                if (!checked) {
                    switchAll.setChecked(false);
                    changeNotifPermission("notifsShares", false);
                } else {
                    changeNotifPermission("notifsShares", true);
                    if (switchLike.isChecked() && switchMessage.isChecked() && switchComment.isChecked() && switchNewPost.isChecked()){
                        switchAll.setChecked(true);
                    }
                }
            }
        });

        rbPublic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    rbFriend.setChecked(false);
                    HashMap<String, Object> updateNotif = new HashMap<>();
                    updateNotif.put("enablePublicChat", true);

                    mUserRef.updateChildren(updateNotif, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            editor.putBoolean("enablePublicChat", true).commit();
                        }
                    });
                }
            }
        });

        rbFriend.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    rbPublic.setChecked(false);
                    HashMap<String, Object> updateNotif = new HashMap<>();
                    updateNotif.put("enablePublicChat", false);

                    mUserRef.updateChildren(updateNotif, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            editor.putBoolean("enablePublicChat", false).commit();
                        }
                    });
                }
            }
        });
    }

    @OnClick(R.id.rl_privacy)
    void showPrivacy() {
        startActivity(new Intent(this, Privacy.class));
    }

    @OnClick(R.id.rl_about)
    void showAbout() {
        startActivity(new Intent(this, About.class));
    }

    @OnClick(R.id.rl_block)
    void showBlocksList() {
        startActivity(new Intent(this, BlocksList.class));
    }

    @OnClick(R.id.rl_Language)
    void showLnaguage() {
        defaultLangage = prefs.getString("Langage","en_US");
        showPopupChooseLanguage();
    }

    @OnClick(R.id.rl_share)
    void shareApp() {
        /*String shareBody = "Hey! I am using Youz app, it's a beautiful anonymous app, try to install it ..." + "\n" + "https://play.google.com/store/apps/details?id=com.youz.android";
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Share with friends");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, "Share with friends"));*/
        String appLinkUrl, previewImageUrl;

        appLinkUrl = "https://www.mydomain.com/myapplink";
        previewImageUrl = "https://www.mydomain.com/my_invite_image.jpg";

        if (AppInviteDialog.canShow()) {
            AppInviteContent content = new AppInviteContent.Builder()
                    .setApplinkUrl(appLinkUrl)
                    .setPreviewImageUrl(previewImageUrl)
                    .build();
            AppInviteDialog.show(Setting.this, content);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_normal, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void changeNotifPermission(final String namePermission, final boolean state) {
        HashMap<String, Object> updateNotif = new HashMap<>();
        updateNotif.put(namePermission, state);

        mUserRef.updateChildren(updateNotif, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                editor.putBoolean(namePermission, state).commit();
            }
        });
    }

    public void changeAllNotifPermission(final boolean state) {
        HashMap<String, Object> updateNotif = new HashMap<>();
        updateNotif.put("notifsChats", state);
        updateNotif.put("notifsComments", state);
        updateNotif.put("notifsLikes", state);
        updateNotif.put("notifsPosts", state);
        updateNotif.put("notifsShares", state);

        mUserRef.updateChildren(updateNotif, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                editor.putBoolean("notifsChats", state);
                editor.putBoolean("notifsComments", state);
                editor.putBoolean("notifsLikes", state);
                editor.putBoolean("notifsPosts", state);
                editor.putBoolean("notifsShares", state);
                editor.commit();
            }
        });
    }


    private void showPopupChooseLanguage() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(true);

        View dialogView;
        if (defaultLangage.equals("en_AU")) {
            dialogView = LayoutInflater.from(this).inflate(R.layout.layout_choose_language_popup_ar, null);
        }else if (defaultLangage.equals("en_CA")) {
            dialogView = LayoutInflater.from(this).inflate(R.layout.layout_choose_language_popup_fr, null);
        }else {
            dialogView = LayoutInflater.from(this).inflate(R.layout.layout_choose_language_popup, null);
        }
        dialog.setView(dialogView);

        final AlertDialog alerteLanguage = dialog.create();
        alerteLanguage.show();

        final RadioButton rb1 = (RadioButton) dialogView.findViewById(R.id.rb1);
        final RadioButton rb2 = (RadioButton) dialogView.findViewById(R.id.rb2);
        final RadioButton rb3 = (RadioButton) dialogView.findViewById(R.id.rb3);

        if (defaultLangage.equals("en_AU"))
            rb1.setChecked(true);
        if (defaultLangage.equals("en_US"))
            rb2.setChecked(true);
        if (defaultLangage.equals("en_CA"))
            rb3.setChecked(true);

        rb1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    rb2.setChecked(false);
                    rb3.setChecked(false);
                }
            }
        });

        rb2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    rb1.setChecked(false);
                    rb3.setChecked(false);
                }
            }
        });

        rb3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    rb1.setChecked(false);
                    rb2.setChecked(false);
                }
            }
        });

        TextView txtValide = (TextView) dialogView.findViewById(R.id.txtValide);
        txtValide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Locale myLocale = null;
                if (rb1.isChecked() && !defaultLangage.equals("en_AU")) {

                    editor.putString("Langage", "en_AU");
                    editor.apply();
                    myLocale = new Locale("en_AU");
                    Resources res = getResources();
                    DisplayMetrics dm = res.getDisplayMetrics();
                    Configuration conf = res.getConfiguration();
                    conf.locale = myLocale;
                    res.updateConfiguration(conf, dm);
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    finishAffinity();
                    startActivity(intent);

                } else if (rb2.isChecked() && !defaultLangage.equals("en_US")) {

                    editor.putString("Langage", "en_US");
                    editor.apply();
                    myLocale = new Locale("en_US");
                    Resources res = getResources();
                    DisplayMetrics dm = res.getDisplayMetrics();
                    Configuration conf = res.getConfiguration();
                    conf.locale = myLocale;
                    res.updateConfiguration(conf, dm);
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    finishAffinity();
                    startActivity(intent);

                } else if (rb3.isChecked() && !defaultLangage.equals("en_CA")) {

                    editor.putString("Langage", "en_CA");
                    editor.apply();
                    myLocale = new Locale("en_CA");
                    Resources res = getResources();
                    DisplayMetrics dm = res.getDisplayMetrics();
                    Configuration conf = res.getConfiguration();
                    conf.locale = myLocale;
                    res.updateConfiguration(conf, dm);
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    finishAffinity();
                    startActivity(intent);

                }

                alerteLanguage.dismiss();
            }
        });
        TextView txtCancel = (TextView) dialogView.findViewById(R.id.txtCancel);
        txtCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alerteLanguage.dismiss();
            }
        });
    }
}
