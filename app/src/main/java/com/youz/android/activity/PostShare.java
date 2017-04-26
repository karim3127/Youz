package com.youz.android.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.like.LikeButton;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.youz.android.R;
import com.youz.android.util.ConnectionDetector;
import com.youz.android.util.UtilDateTime;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class PostShare extends BaseActivity {

    @BindView(R.id.tv_date)
    TextView tvDate;

    @BindView(R.id.tv_status)
    TextView tvStatus;

    @BindView(R.id.tv_nb_fav)
    TextView tvNbFav;

    @BindView(R.id.tv_nb_comment)
    TextView tvNbComment;

    @BindView(R.id.img_back)
    ImageView imgBack;

    @BindView(R.id.v_back)
    View vBack;

    @BindView(R.id.tv_location)
    TextView tvLocation;

    @BindView(R.id.ll_location)
    LinearLayout llLocation;

    @BindView(R.id.lb_fav)
    LikeButton lbFav;

    @BindView(R.id.rl_top)
    RelativeLayout rlTop;

    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 21;
    public static Pair<String, HashMap<String, Object>> currentPost;
    DisplayImageOptions options;
    SimpleDateFormat format;
    TimeZone timeZone;
    private Typeface typeFaceGras;
    ConnectionDetector connectionDetector;

    private int nbLikes;
    private int nbComments;
    private String title, color, font, photo;
    private long fontSize;
    private SharedPreferences prefs;
    private String userId;
    boolean isFbClicked = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_share);
        ButterKnife.bind(this);

        prefs = getSharedPreferences("com.youz.android", Context.MODE_PRIVATE);
        userId = prefs.getString("UserId", "");

        typeFaceGras = Typeface.createFromAsset(getAssets(), "fonts/optima_bold.ttf");
        connectionDetector = new ConnectionDetector(this);

        timeZone = TimeZone.getDefault();
        format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        format.setTimeZone(timeZone);

        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        showPostDetails();

    }

    @OnClick(R.id.tv_facebook)
    public void facebookClicked() {
        isFbClicked = true;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            shareWithFacebook();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    @OnClick(R.id.tv_twitter)
    public void twitterClicked() {
        isFbClicked = false;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            shareWithTwitter();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    @OnClick(R.id.tv_cancel)
    public void shareCancel() {
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE == requestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (isFbClicked) {
                    shareWithFacebook();
                } else {
                    shareWithTwitter();
                }
            }
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public void showPostDetails() {

        final HashMap<String, Object> current = currentPost.second;

        title = (String) current.get("title");
        color = (current.get("color") == null) ? "" : (String) current.get("color");
        photo = (current.get("photo") == null) ? "" : (String) current.get("photo");
        font = (current.get("font") == null) ? "" : (String) current.get("font");
        fontSize = (current.get("fontSize") == null) ? -1 : (long) current.get("fontSize");
        String location = (current.get("location") == null) ? "" : (String) current.get("location");
        String city = (current.get("city") == null) ? "" : " - " + current.get("city");
        nbLikes = (current.get("likes") == null) ? 0 : ((HashMap<String, Object>) current.get("likes")).size();
        nbComments = (current.get("comments") == null) ? 0 : ((HashMap<String, Object>) current.get("comments")).size();

        Date dateDialog = null;
        try {
            dateDialog = format.parse((String) current.get("createdAt"));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        tvDate.setText(UtilDateTime.formatTime(this, dateDialog));
        tvDate.setTypeface(typeFaceGras);

        tvStatus.setText(title);

        tvNbFav.setText(nbLikes + "");
        tvNbFav.setTypeface(typeFaceGras);

        tvNbComment.setText(nbComments + "");
        tvNbComment.setTypeface(typeFaceGras);

        if (location.equals("")) {
            llLocation.setVisibility(View.INVISIBLE);
        } else {
            llLocation.setVisibility(View.VISIBLE);
            if(!city.equals(" - ")) {
                tvLocation.setText(location + city);
            }else{
                tvLocation.setText(location);
            }
            tvLocation.setTypeface(typeFaceGras);
        }

        if (!font.equals("")) {
            try {
                Typeface typefaceStatus = Typeface.createFromAsset(getAssets(), "fonts/" + font + ".ttf");
                tvStatus.setTypeface(typefaceStatus);
            } catch (Exception e) {

            }
        }
        if (fontSize > 0) {
            double size;
            if (fontSize == 6) {
                size = 5;
            } else if (fontSize == 8) {
                size = 5.5;
            } else if (fontSize == 10) {
                size = 6;
            } else if (fontSize == 12) {
                size = 6.5;
            } else if (fontSize == 14) {
                size = 7;
            } else if (fontSize == 16) {
                size = 7.5;
            } else if (fontSize == 18) {
                size = 8;
            } else {
                size = 5.5;
            }

            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            long px = Math.round(size * (displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
            tvStatus.setTextSize(px);
        }
        if (!photo.equals("")) {
            ImageLoader.getInstance().displayImage(photo, imgBack, options);
            vBack.setAlpha(0.5f);
        } else {
            vBack.setAlpha(1f);
        }

        if (!color.equals("")) {
            try {
                vBack.setBackgroundColor(Color.parseColor("#" + color));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (current.get("likes") != null && ((HashMap<String, Object>) current.get("likes")).get(userId) != null) {
            lbFav.setLiked(true);
        } else {
            lbFav.setLiked(false);
        }

    }

    public void shareWithFacebook() {
        Bitmap bitmap = takeScreenShot(rlTop);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "shareFB", null);
        Uri imageUri = Uri.parse(path);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");

        intent.putExtra(Intent.EXTRA_STREAM, imageUri);

        // See if official Facebook app is found
        boolean facebookAppFound = false;
        List<ResolveInfo> matches = getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo info : matches) {
            if (info.activityInfo.packageName.toLowerCase().startsWith("com.facebook.katana")) {
                intent.setPackage(info.activityInfo.packageName);
                facebookAppFound = true;
                break;
            }
        }

        if (!facebookAppFound) {
            Toast.makeText(this, "Facebook App not found", Toast.LENGTH_SHORT).show();
        } else {
            startActivity(intent);
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 1000);
        }

    }

    public void shareWithTwitter() {

        Bitmap bitmap = takeScreenShot(rlTop);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "shareTW", null);
        Uri imageUri = Uri.parse(path);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");

        intent.putExtra(Intent.EXTRA_STREAM, imageUri);

        // See if official Twitter app is found
        boolean twitterAppFound = false;
        List<ResolveInfo> matches = getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo info : matches) {
            if (info.activityInfo.name.contains("twitter")) {
                intent.setPackage(info.activityInfo.packageName);
                twitterAppFound = true;
                break;
            }
        }

        if (!twitterAppFound) {
            Toast.makeText(this, "Twitter App not found", Toast.LENGTH_SHORT).show();
        } else {
            startActivity(intent);
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 1000);
        }


    }

    public Bitmap takeScreenShot(View view) {
        Bitmap bitmap;
        view.setDrawingCacheEnabled(true);
        bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        return bitmap;
    }

}
