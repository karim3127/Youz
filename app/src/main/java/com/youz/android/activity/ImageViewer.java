package com.youz.android.activity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Interpolator;

import com.alexvasilkov.gestures.views.GestureImageView;
import com.nineoldandroids.view.ViewPropertyAnimator;
import com.nineoldandroids.view.animation.AnimatorProxy;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.youz.android.R;

public class ImageViewer extends BaseActivity {

    private static final String EXTRA_LEFT = "extra_left";
    private static final String EXTRA_TOP = "extra_top";
    private static final String EXTRA_WIDTH = "extra_width";
    private static final String EXTRA_HEIGHT = "extra_height";

    GestureImageView imgMsgView;
    private DisplayImageOptions options;

    private int leftDelta;
    private int topDelta;
    private float widthScale;
    private float heightScale;

    private static final long ANIMATION_DURATION = 450;
    private static final Interpolator INTERPOLATOR = new FastOutSlowInInterpolator();

    public static void start(Activity activity, View transitionView, String imageURL) {
        Intent intent = new Intent(activity, ImageViewer.class);
        intent.putExtra("imageURL", imageURL);

        int[] screenLocation = new int[2];
        transitionView.getLocationOnScreen(screenLocation);

        intent.putExtra(EXTRA_LEFT, screenLocation[0]);
        intent.putExtra(EXTRA_TOP, screenLocation[1]);
        intent.putExtra(EXTRA_WIDTH, transitionView.getWidth());
        intent.putExtra(EXTRA_HEIGHT, transitionView.getHeight());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity);
            activity.startActivity(intent, options.toBundle());
        } else {
            activity.startActivity(intent);
        }
        activity.overridePendingTransition(0, R.anim.activity_fade_out);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .showImageOnLoading(R.drawable.ic_white)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        Bundle bundle = getIntent().getExtras();

        final int thumbnailTop = bundle.getInt(EXTRA_TOP);
        final int thumbnailLeft = bundle.getInt(EXTRA_LEFT);
        final int thumbnailWidth = bundle.getInt(EXTRA_WIDTH);
        final int thumbnailHeight = bundle.getInt(EXTRA_HEIGHT);

        imgMsgView = (GestureImageView) findViewById(R.id.imgMsgView);
        String urlImg = getIntent().getStringExtra("imageURL");

        ImageLoader.getInstance().displayImage(urlImg, imgMsgView, options);

        ViewTreeObserver observer = imgMsgView.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {

                imgMsgView.getViewTreeObserver().removeOnPreDrawListener(this);

                int[] screenLocation = new int[2];
                imgMsgView.getLocationOnScreen(screenLocation);
                leftDelta = thumbnailLeft - screenLocation[0];
                topDelta = thumbnailTop - screenLocation[1];
                widthScale = (float) thumbnailWidth / (imgMsgView.getWidth());
                heightScale = (float) thumbnailHeight / imgMsgView.getHeight();

                startEnterAnimation();

                return true;
            }
        });

    }

    public void startEnterAnimation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            imgMsgView.setPivotX(0);
            imgMsgView.setPivotY(0);
            imgMsgView.setScaleX(widthScale);
            imgMsgView.setScaleY(heightScale);
            imgMsgView.setTranslationX(leftDelta);
            imgMsgView.setTranslationY(topDelta);
        } else {
            AnimatorProxy proxy = AnimatorProxy.wrap(imgMsgView);
            proxy.setPivotX(0);
            proxy.setPivotY(0);
            proxy.setScaleX(widthScale);
            proxy.setScaleY(heightScale);
            proxy.setTranslationX(leftDelta);
            proxy.setTranslationY(topDelta);
        }

        ViewPropertyAnimator.animate(imgMsgView)
                .setDuration(ANIMATION_DURATION)
                .scaleX(1).scaleY(1)
                .translationX(0).translationY(0)
                .setInterpolator(INTERPOLATOR);
    }
}
