package com.youz.android.activity;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tomergoldst.tooltips.ToolTip;
import com.tomergoldst.tooltips.ToolTipsManager;
import com.youz.android.R;
import com.youz.android.adapter.PagerHomeAdapter;
import com.youz.android.fragment.HomeProfilFragment;
import com.youz.android.fragment.HomeProfilLikedFragment;
import com.youz.android.fragment.HomeProfilPostsFragment;
import com.youz.android.fragment.HomeProfilSavedFragment;
import com.youz.android.fragment.HomeRecentFriendsFragment;
import com.youz.android.fragment.HomeRecentNearFriendsFragment;
import com.youz.android.fragment.HomeRecentPopularFragment;
import com.youz.android.model.Country;
import com.youz.android.service.GPSTracker;
import com.youz.android.view.NonSwipeableViewPager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends BaseActivity {

    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 21;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 22;
    private static final int PERMISSIONS_REQUEST_CODE_PICK_CONTACTS = 23;
    public static final int REQUEST_CODE_NEW_POST = 11;

    @BindView(R.id.main_activity)
    RelativeLayout mainActivity;

    @BindView(R.id.img_recent)
    ImageView imgRecent;

    @BindView(R.id.img_message)
    ImageView imgMessage;

    @BindView(R.id.img_add_new)
    ImageView imgAddNew;

    @BindView(R.id.img_notif)
    ImageView imgNotif;

    @BindView(R.id.img_profil)
    ImageView imgProfil;

    @BindView(R.id.v_index_recent)
    View vIndexRecent;

    @BindView(R.id.v_index_message)
    View vIndexMessage;

    @BindView(R.id.v_index_notif)
    View vIndexNotif;

    @BindView(R.id.v_index_profil)
    View vIndexProfil;

    @BindView(R.id.vp_home)
    NonSwipeableViewPager vpHome;

    @BindView(R.id.img_setting)
    ImageView imgSetting;

    @BindView(R.id.img_tags)
    ImageView imgTags;

    @BindView(R.id.tv_nb_friend)
    public TextView tvNbFriend;

    @BindView(R.id.iv_friend)
    public ImageView ivFriend;

    @BindView(R.id.v_block_option)
    View vBlockOption;

    public static List<String> listYouzBlocks = new ArrayList<>();
    public static TextView tvBadgeMsg, tvBadgeNotif;
    public static Context context;
    public static String localeCode;
    public static String locale;
    public static String city;
    List<Country> mCountriesList = new ArrayList<Country>();
    private int tabSelected = 0;

    public HomeRecentFriendsFragment homeRecentFriendsFragment;
    public HomeRecentNearFriendsFragment homeRecentNearFriendsFragment;
    public HomeRecentPopularFragment homeRecentPopularFragment;
    public HomeProfilLikedFragment homeProfilLikedFragment;
    public HomeProfilSavedFragment homeProfilSavedFragment;
    public HomeProfilPostsFragment homeProfilPostsFragment;
    public HomeProfilFragment homeProfilFragment;

    FirebaseDatabase mRootRef = FirebaseDatabase.getInstance();
    DatabaseReference mBlocksRef;

    private ValueEventListener valueEventListener;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private String userId;
    private String defaultLangage;
    private Boolean theLanguageisEnglish = true;
    private LocalBroadcastManager broadcaster;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        context = this;
        prefs = getSharedPreferences("com.youz.android", Context.MODE_PRIVATE);
        editor = prefs.edit();
        userId = prefs.getString("UserId", "");
        defaultLangage = prefs.getString("Langage","en_US");
        broadcaster = LocalBroadcastManager.getInstance(this);

        theLanguageisEnglish = !defaultLangage.equals("en_AU");

        mBlocksRef = mRootRef.getReference("blocks/" + userId);
        getBlockContacts();

        tvBadgeMsg = (TextView) findViewById(R.id.tv_badge_msg);
        tvBadgeNotif = (TextView) findViewById(R.id.tv_badge_notif);

        (new CountryAsyncTask()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        initTabBar();

        vpHome.setOffscreenPageLimit(4);
        vpHome.setAdapter(new PagerHomeAdapter(getSupportFragmentManager(), this,theLanguageisEnglish));
        if (!theLanguageisEnglish) {
            vpHome.setCurrentItem(3  - tabSelected, false);
        }
        try {
            Field mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            FixedSpeedScroller scroller = new FixedSpeedScroller(this);
            mScroller.set(vpHome, scroller);
        } catch (NoSuchFieldException e) {
        } catch (IllegalArgumentException e) {
        } catch (IllegalAccessException e) {
        }

        vBlockOption.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (anchorView != null) {
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                anchorView = null;
                            }
                        }, 300);
                        mToolTipsManager.findAndDismiss(anchorView);
                    }
                    vBlockOption.setVisibility(View.GONE);
                    ivFriend.setEnabled(true);
                }
                return false;
            }
        });

    }


    ToolTipsManager mToolTipsManager = new ToolTipsManager();
    View anchorView;

    @OnClick(R.id.iv_friend)
    public void showNbFriend() {
        if (anchorView == null) {
            anchorView = ivFriend;
            String nbFriends = (tvNbFriend.getText().toString().isEmpty()) ? "0" : tvNbFriend.getText().toString();
            ToolTip.Builder builder = new ToolTip.Builder(this, ivFriend, mainActivity, getResources().getString(R.string.friends_number) + " " + nbFriends, ToolTip.POSITION_BELOW);
            builder.setAlign(ToolTip.ALIGN_LEFT);
            builder.setBackgroundColor(getResources().getColor(R.color.colorGrayLight));
            builder.setTextColor(getResources().getColor(R.color.colorPrimary));
            mToolTipsManager.show(builder.build());
            vBlockOption.setVisibility(View.VISIBLE);
            ivFriend.setEnabled(false);
        } else {
            mToolTipsManager.findAndDismiss(anchorView);
        }
    }

    @OnClick(R.id.ll_menu)
    public void menuClick() {

    }

    @OnClick(R.id.ll_recent)
    public void showRecent() {
        imgRecent.setSelected(true);
        imgMessage.setSelected(false);
        imgNotif.setSelected(false);
        imgProfil.setSelected(false);

        if (tabSelected != 0) {
            animeScaleUp(vIndexRecent);
            animeScaleDown(vIndexMessage);
            animeScaleDown(vIndexNotif);
            animeScaleDown(vIndexProfil);
        }
        tabSelected = 0;
        if (!theLanguageisEnglish) {
            vpHome.setCurrentItem(3  - tabSelected, false);
        }else {
            vpHome.setCurrentItem(tabSelected, true);
        }

        imgTags.setVisibility(View.VISIBLE);
        imgSetting.setVisibility(View.GONE);
    }

    @OnClick(R.id.ll_message)
    public void showMessage() {
        imgRecent.setSelected(false);
        imgMessage.setSelected(true);
        imgNotif.setSelected(false);
        imgProfil.setSelected(false);

        if (tabSelected != 1) {
            animeScaleDown(vIndexRecent);
            animeScaleUp(vIndexMessage);
            animeScaleDown(vIndexNotif);
            animeScaleDown(vIndexProfil);
        }
        tabSelected = 1;
        if (!theLanguageisEnglish) {
            vpHome.setCurrentItem(3  - tabSelected, false);
        }else {
            vpHome.setCurrentItem(tabSelected, true);
        }

        imgTags.setVisibility(View.VISIBLE);
        imgSetting.setVisibility(View.GONE);
    }

    @OnClick(R.id.img_add_new)
    public void addNewPost() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            startActivityForResult(new Intent(this, NewPost.class), REQUEST_CODE_NEW_POST);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    @OnClick(R.id.ll_notif)
    public void showNotif() {
        imgRecent.setSelected(false);
        imgMessage.setSelected(false);
        imgNotif.setSelected(true);
        imgProfil.setSelected(false);

        if (tabSelected != 2) {
            animeScaleDown(vIndexRecent);
            animeScaleDown(vIndexMessage);
            animeScaleUp(vIndexNotif);
            animeScaleDown(vIndexProfil);
        }
        tabSelected = 2;
        if (!theLanguageisEnglish) {
            vpHome.setCurrentItem(3  - tabSelected, false);
        }else {
            vpHome.setCurrentItem(tabSelected, true);
        }

        imgTags.setVisibility(View.VISIBLE);
        imgSetting.setVisibility(View.GONE);
    }

    @OnClick(R.id.ll_profil)
    public void showProfil() {
        imgRecent.setSelected(false);
        imgMessage.setSelected(false);
        imgNotif.setSelected(false);
        imgProfil.setSelected(true);

        if (tabSelected != 3) {
            animeScaleDown(vIndexRecent);
            animeScaleDown(vIndexMessage);
            animeScaleDown(vIndexNotif);
            animeScaleUp(vIndexProfil);
        }
        tabSelected = 3;
        if (!theLanguageisEnglish) {
            vpHome.setCurrentItem(3  - tabSelected, false);
        }else {
            vpHome.setCurrentItem(tabSelected, true);
        }

        imgTags.setVisibility(View.GONE);
        imgSetting.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.img_setting)
    public void showSetting() {
        startActivity(new Intent(this, Setting.class));
    }

    @OnClick(R.id.img_tags)
    public void showTags() {
        startActivity(new Intent(this, Tags.class));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE == requestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivityForResult(new Intent(this, NewPost.class), REQUEST_CODE_NEW_POST);
            }
        } else if (PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION == requestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            }
        } else if (PERMISSIONS_REQUEST_CODE_PICK_CONTACTS == requestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                homeRecentFriendsFragment.getPhoneContacts();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_NEW_POST) {
            if (resultCode == RESULT_OK) {
                imgRecent.setSelected(false);
                imgMessage.setSelected(false);
                imgNotif.setSelected(false);
                imgProfil.setSelected(true);

                if (tabSelected != 3) {
                    animeScaleDown(vIndexRecent);
                    animeScaleDown(vIndexMessage);
                    animeScaleDown(vIndexNotif);
                    animeScaleUp(vIndexProfil);
                }
                tabSelected = 3;
                if (!theLanguageisEnglish) {
                    vpHome.setCurrentItem(3  - tabSelected, false);
                }else {
                    vpHome.setCurrentItem(tabSelected, true);
                }

                imgTags.setVisibility(View.GONE);
                imgSetting.setVisibility(View.VISIBLE);



                if(!theLanguageisEnglish) {
                    homeProfilFragment.vpProfil.setCurrentItem(2);
                } else {
                    homeProfilFragment.vpProfil.setCurrentItem(0);
                }

                homeProfilPostsFragment.rvPosts.post(new Runnable() {
                    @Override
                    public void run() {
                        homeProfilPostsFragment.rvPosts.smoothScrollToPosition(0);
                    }
                });
            }
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onDestroy() {
        context = null;
        listYouzBlocks = new ArrayList<>();
        mBlocksRef.removeEventListener(valueEventListener);
        super.onDestroy();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

    public void initTabBar() {

        imgRecent.setSelected(true);
        imgMessage.setSelected(false);
        imgNotif.setSelected(false);
        imgProfil.setSelected(false);

        vIndexMessage.setScaleX(0f);
        vIndexMessage.setScaleY(0f);

        vIndexNotif.setScaleX(0f);
        vIndexNotif.setScaleY(0f);

        vIndexProfil.setScaleX(0f);
        vIndexProfil.setScaleY(0f);
    }

    public void animeScaleDown(View view) {
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 0f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 0f);
        scaleDownX.setDuration(200);
        scaleDownY.setDuration(200);

        AnimatorSet scaleDown = new AnimatorSet();
        scaleDown.play(scaleDownX).with(scaleDownY);

        scaleDown.start();
    }

    public void animeScaleUp(View view) {
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 1f);
        scaleUpX.setDuration(200);
        scaleUpY.setDuration(200);

        AnimatorSet scaleUp = new AnimatorSet();
        scaleUp.play(scaleUpX).with(scaleUpY);

        scaleUp.start();
    }

    public void getBlockContacts() {
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    HashMap<String, Object> blocks = (HashMap<String, Object>) dataSnapshot.getValue();
                    listYouzBlocks.clear();
                    for (Map.Entry<String, Object> block : blocks.entrySet()) {
                        if (!listYouzBlocks.contains(block.getKey())) {
                            listYouzBlocks.add(block.getKey());
                        }
                    }
                    if (listYouzBlocks.size() > 0) {
                        if (homeRecentFriendsFragment != null) {
                            homeRecentFriendsFragment.notifyBlockPost();
                        }
                        if (homeRecentNearFriendsFragment != null) {
                            homeRecentNearFriendsFragment.notifyBlockPost();
                        }
                        if (homeRecentPopularFragment != null) {
                            homeRecentPopularFragment.notifyBlockPost();
                        }
                        if (homeProfilSavedFragment != null) {
                            homeProfilSavedFragment.notifyBlockPost();
                        }
                        if (homeProfilLikedFragment != null) {
                            homeProfilLikedFragment.notifyBlockPost();
                        }
                    }
                } else {
                    listYouzBlocks = new ArrayList<>();
                }
                sendMessageResult("Block");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mBlocksRef.addValueEventListener(valueEventListener);
    }

    public class FixedSpeedScroller extends Scroller {

        private int mDuration = 600;

        public FixedSpeedScroller(Context context) {
            super(context);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            // Ignore received duration, use fixed one instead
            super.startScroll(startX, startY, dx, dy, mDuration);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy) {
            // Ignore received duration, use fixed one instead
            super.startScroll(startX, startY, dx, dy, mDuration);
        }
    }

    void getCurrentLocation() {

        double longitude;
        double latitude;

        GPSTracker tracker = new GPSTracker(MainActivity.this);
        if (!tracker.canGetLocation()) {
            tracker.showSettingsAlert();
        } else {

            String cityName = "";
            String countryName = "";
            String countryCode = "";

            latitude = tracker.getLatitude();
            longitude = tracker.getLongitude();

            if (latitude != 0.0) {
                editor.putString("Latitude", latitude + "");
                editor.commit();
            } else {
                try {
                    latitude = Double.parseDouble(prefs.getString("Latitude", "0.0"));
                } catch (Exception e) {
                    latitude = 0.0;
                }
            }
            if (longitude != 0.0) {
                editor.putString("Longitude", longitude + "");
                editor.commit();
            } else {
                try {
                    longitude = Double.parseDouble(prefs.getString("Longitude", "0.0"));
                } catch (Exception e) {
                    longitude = 0.0;
                }
            }

            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (addresses != null && addresses.size() > 0) {

                cityName = addresses.get(0).getLocality();
                if (cityName == null)
                    cityName = "";

                if (cityName != null && !cityName.isEmpty()) {
                    editor.putString("CityName", cityName);
                    editor.commit();
                } else {
                        cityName = prefs.getString("CityName", "");
                }

                try {
                    countryCode =  addresses.get(0).getCountryCode();
                } catch (Exception e) {
                    countryCode = "";
                }

                if (!TextUtils.isEmpty(countryCode) && countryCode != null) {
                    editor.putString("CountryCode", countryCode);
                    editor.commit();
                } else {
                    countryCode = prefs.getString("CountryCode", "");
                }

                for (int i = 0; i < mCountriesList.size(); i++) {
                    if (countryCode.toLowerCase().equals((mCountriesList.get(i).getCountryISO() + "").toLowerCase())) {
                        countryName = mCountriesList.get(i).getName();
                    }
                }

                if (countryName != null && !countryName.isEmpty()) {
                    editor.putString("CountryName", countryName);
                    editor.commit();
                } else {
                    countryName = prefs.getString("CountryName", "");
                }

            } else {
                countryName = prefs.getString("CountryName", "");
                cityName = prefs.getString("CityName", "");
            }

            localeCode = countryCode;
            locale = countryName;
            city = cityName;
        }
    }

    public void sendMessageResult(String message) {
        Intent intent = new Intent(ChatMessage.COPA_RESULT);
        if(message != null) {
            intent.putExtra(ChatMessage.COPA_MESSAGE, message);
        }
        broadcaster.sendBroadcast(intent);
    }

    protected class CountryAsyncTask extends AsyncTask<Void, Void, ArrayList<Country>> {

        @Override
        protected ArrayList<Country> doInBackground(Void... params) {
            ArrayList<Country> data = new ArrayList<Country>(233);
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(getAssets().open("countries.dat"), "UTF-8"));

                // do reading, usually loop until end of file reading
                String line;
                int i = 0;
                while ((line = reader.readLine()) != null) {
                    //process line
                    Country c = new Country(MainActivity.this, line, i);
                    mCountriesList.add(c);
                    i++;
                }
            } catch (IOException e) {
                //log the exception
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        //log the exception
                    }
                }
            }

            return data;
        }
    }
}
