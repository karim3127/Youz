package com.youz.android.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.youz.android.fragment.HomeMessageFragment;
import com.youz.android.fragment.HomeNotifFragment;
import com.youz.android.fragment.HomeProfilFragment;
import com.youz.android.fragment.HomeRecentFragment;

/**
 * Created by macbook on 14/05/15.
 */
public class PagerHomeAdapter extends FragmentStatePagerAdapter {

    Context context;
    Boolean theLanguageisEnglish;

    public PagerHomeAdapter(FragmentManager fm, Context c, Boolean theLanguageisEnglish) {
        super(fm);
        context = c;
        this.theLanguageisEnglish = theLanguageisEnglish;
    }

    @Override
    public Fragment getItem(int i) {
        Fragment myFragment;
        if(theLanguageisEnglish){
            if (i == 0) {
                myFragment = HomeRecentFragment.newInstance();
            } else if (i == 1) {
                myFragment = HomeMessageFragment.newInstance();
            } else if (i == 2) {
                myFragment = HomeNotifFragment.newInstance();
            } else {
                myFragment = HomeProfilFragment.newInstance();
            }
        }else{
            if (i == 0) {
                myFragment = HomeProfilFragment.newInstance();
            } else if (i == 1) {
                myFragment = HomeNotifFragment.newInstance();
            } else if (i == 2) {
                myFragment = HomeMessageFragment.newInstance();
            } else {
                myFragment = HomeRecentFragment.newInstance();
            }
        }


        return myFragment;
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return null;
    }

}