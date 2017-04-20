package com.youz.android.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.youz.android.R;
import com.youz.android.fragment.HomeProfilLikedFragment;
import com.youz.android.fragment.HomeProfilPostsFragment;
import com.youz.android.fragment.HomeProfilSavedFragment;

/**
 * Created by macbook on 14/05/15.
 */
public class PagerProfilAdapter extends FragmentStatePagerAdapter {

    Context context;
    String[] titles;
    Boolean theLanguageisEnglish;

    public PagerProfilAdapter(FragmentManager fm, Context c, Boolean theLanguageisEnglish) {
        super(fm);
        context = c;
        this.theLanguageisEnglish = theLanguageisEnglish;
        if(theLanguageisEnglish){
            titles = new String[]{c.getResources().getString(R.string.profil_post),
                    c.getResources().getString(R.string.profil_likes),
                    c.getResources().getString(R.string.profil_saved)};
        }else{
            titles = new String[]{c.getResources().getString(R.string.profil_saved),
                    c.getResources().getString(R.string.profil_likes),
                    c.getResources().getString(R.string.profil_post)};
        }

    }

    @Override
    public Fragment getItem(int i) {
        Fragment myFragment;
        if(theLanguageisEnglish){
            if (i == 0) {
                myFragment = HomeProfilPostsFragment.newInstance();
            } else if (i == 1) {
                myFragment = HomeProfilLikedFragment.newInstance();
            } else {
                myFragment = HomeProfilSavedFragment.newInstance();
            }
        }else{
            if (i == 0) {
                myFragment = HomeProfilSavedFragment.newInstance();
            } else if (i == 1) {
                myFragment = HomeProfilLikedFragment.newInstance();
            } else {
                myFragment = HomeProfilPostsFragment.newInstance();
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