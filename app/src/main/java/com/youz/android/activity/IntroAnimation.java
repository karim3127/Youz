package com.youz.android.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.youz.android.R;
import com.youz.android.view.paperonboarding.PaperOnboardingEngine;
import com.youz.android.view.paperonboarding.PaperOnboardingPage;
import com.youz.android.view.paperonboarding.listeners.PaperOnboardingOnChangeListener;
import com.youz.android.view.paperonboarding.listeners.PaperOnboardingOnRightOutListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class IntroAnimation extends AppCompatActivity {

    @BindView(R.id.tvSkip)
    TextView tvSkip;

    @BindView(R.id.indicateur0)
    View indicateur0;

    @BindView(R.id.indicateur1)
    View indicateur1;

    @BindView(R.id.indicateur2)
    View indicateur2;

    @BindView(R.id.indicateur3)
    View indicateur3;

    static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_animation);
        ButterKnife.bind(this);

        context = this;

        PaperOnboardingEngine engine = new PaperOnboardingEngine(findViewById(R.id.onboardingRootView), getDataForOnboarding(), getApplicationContext());

        engine.setOnChangeListener(new PaperOnboardingOnChangeListener() {
            @Override
            public void onPageChanged(int oldElementIndex, int newElementIndex) {
                switch (oldElementIndex) {
                    case 0 :
                        indicateur0.setSelected(false);
                        break;
                    case 1 :
                        indicateur1.setSelected(false);
                        break;
                    case 2 :
                        indicateur2.setSelected(false);
                        break;
                    case 3 :
                        indicateur3.setSelected(false);
                        break;

                }

                switch (newElementIndex) {
                    case 0 :
                        indicateur0.setSelected(true);
                        break;
                    case 1 :
                        indicateur1.setSelected(true);
                        break;
                    case 2 :
                        indicateur2.setSelected(true);
                        break;
                    case 3 :
                        indicateur3.setSelected(true);
                        break;

                }
                //Toast.makeText(getApplicationContext(), "Swiped from " + oldElementIndex + " to " + newElementIndex, Toast.LENGTH_SHORT).show();
            }
        });

        indicateur0.setSelected(true);

        engine.setOnRightOutListener(new PaperOnboardingOnRightOutListener() {
            @Override
            public void onRightOut() {
                // Probably here will be your exit action
                //Toast.makeText(getApplicationContext(), "Swiped out right", Toast.LENGTH_SHORT).show();
            }
        });

        Typeface typefaceBold = Typeface.createFromAsset(getAssets(), "fonts/ElMessiri-Medium.ttf");
        tvSkip.setTypeface(typefaceBold);
    }

    @OnClick(R.id.tvSkip)
    public void skipClic() {
        startActivity(new Intent(this, SignUp.class));
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    // Just example data for Onboarding
    private ArrayList<PaperOnboardingPage> getDataForOnboarding() {
        // prepare data
        PaperOnboardingPage scr1 = new PaperOnboardingPage("", getResources().getString(R.string.slid_intro_desc_0),
                getResources().getColor(R.color.slider_color_0), R.drawable.ic_slide_page_0, R.drawable.ic_slide_small_icon_0);
        PaperOnboardingPage scr2 = new PaperOnboardingPage("", getResources().getString(R.string.slid_intro_desc_1),
                getResources().getColor(R.color.slider_color_1), R.drawable.ic_slide_page_1, R.drawable.ic_slide_small_icon_1);
        PaperOnboardingPage scr3 = new PaperOnboardingPage("", getResources().getString(R.string.slid_intro_desc_2),
                getResources().getColor(R.color.slider_color_2), R.drawable.ic_slide_page_2, R.drawable.ic_slide_small_icon_2);
        PaperOnboardingPage scr4 = new PaperOnboardingPage("", getResources().getString(R.string.slid_intro_desc_3),
                getResources().getColor(R.color.slider_color_3), R.drawable.ic_slide_page_3, R.drawable.ic_slide_small_icon_3);
        PaperOnboardingPage scr5 = new PaperOnboardingPage("", getResources().getString(R.string.slid_intro_desc_4),
                getResources().getColor(R.color.slider_color_4), R.drawable.ic_slide_page_4, R.drawable.ic_slide_small_icon_4);

        ArrayList<PaperOnboardingPage> elements = new ArrayList<>();
        elements.add(scr1);
        elements.add(scr2);
        elements.add(scr3);
        elements.add(scr4);
        elements.add(scr5);
        return elements;
    }

    @Override
    protected void onDestroy() {
        context = null;
        super.onDestroy();
    }

    public static void finishActivity() {
        if (context != null) {
            ((IntroAnimation) context).finish();
        }
    }
}
