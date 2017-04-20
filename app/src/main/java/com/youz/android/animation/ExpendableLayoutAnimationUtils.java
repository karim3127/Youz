package com.youz.android.animation;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

/** Delegate class handling all the animation part */
public class ExpendableLayoutAnimationUtils {

    Context context;
    private View viewAnimated;
    private int initialHeight;
    private int animationDuration = 300;
    private boolean animating = false;

    public ExpendableLayoutAnimationUtils(Context context, View view, int height) {
        this.context = context;
        viewAnimated = view;
        initialHeight = dpToPx(height);
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public Animation animateToolbar(final int startHeight, final int endHeight) {
        final int deltaHeight = endHeight - startHeight;

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                viewAnimated.getLayoutParams().height = startHeight + (int) (deltaHeight * interpolatedTime);
                viewAnimated.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };
        a.setInterpolator(new LinearInterpolator());
        a.setDuration(animationDuration);
        return a;
    }

    public void expand() {
        if (animating)
            return;
        animating = true;
        Animation a = animateToolbar(0, initialHeight);

        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animating = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        viewAnimated.startAnimation(a);
    }

    public void collapse() {
        if (animating)
            return;
        animating = true;
        Animation a = animateToolbar(initialHeight, 0);

        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animating = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        viewAnimated.startAnimation(a);
    }

}