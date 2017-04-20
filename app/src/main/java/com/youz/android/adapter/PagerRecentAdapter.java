package com.youz.android.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.youz.android.R;
import com.youz.android.fragment.HomeRecentFriendsFragment;
import com.youz.android.fragment.HomeRecentNearFriendsFragment;
import com.youz.android.fragment.HomeRecentPopularFragment;

/**
 * Created by macbook on 14/05/15.
 */
public class PagerRecentAdapter extends FragmentStatePagerAdapter {

    Context context;
    String[] titles ;
    Boolean theLanguageisEnglish;

    public PagerRecentAdapter(FragmentManager fm, Context c, Boolean theLanguageisEnglish) {
        super(fm);
        context = c;
        this.theLanguageisEnglish = theLanguageisEnglish;
        if(theLanguageisEnglish){
            titles = new String[]{c.getResources().getString(R.string.home_friend),
                    c.getResources().getString(R.string.home_nearby),
                    c.getResources().getString(R.string.home_populair)};
        }else{
            titles = new String[]{c.getResources().getString(R.string.home_populair),
                    c.getResources().getString(R.string.home_nearby),
                    c.getResources().getString(R.string.home_friend)};
        }

    }

    @Override
    public Fragment getItem(int i) {
        Fragment myFragment;
        if(theLanguageisEnglish){
            if (i == 0) {
                myFragment = HomeRecentFriendsFragment.newInstance();
            } else if (i == 1) {
                myFragment = HomeRecentNearFriendsFragment.newInstance();
            } else {
                myFragment = HomeRecentPopularFragment.newInstance();
            }
        }else{
            if (i == 0) {
                myFragment = HomeRecentPopularFragment.newInstance();
            } else if (i == 1) {
                myFragment = HomeRecentNearFriendsFragment.newInstance();
            } else {
                myFragment = HomeRecentFriendsFragment.newInstance();
            }
        }

        return myFragment;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

}