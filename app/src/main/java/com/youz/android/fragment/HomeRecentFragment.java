package com.youz.android.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.youz.android.R;
import com.youz.android.adapter.PagerRecentAdapter;

public class HomeRecentFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private View layout;
    private ViewPager vpRecent;
    private SmartTabLayout tabRecent;
    private String defaultLangage;
    private Boolean theLanguageisEnglish = true;
    private SharedPreferences prefs;

    public HomeRecentFragment() {
        // Required empty public constructor
    }

    public static HomeRecentFragment newInstance() {
        HomeRecentFragment fragment = new HomeRecentFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getActivity().getSharedPreferences("com.youz.android", Context.MODE_PRIVATE);
        defaultLangage = prefs.getString("Langage","en");

        if (defaultLangage.equals("ar") || defaultLangage.equals("wi")) {
            theLanguageisEnglish = false;
        }else{
            theLanguageisEnglish = true;
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_home_recent, container, false);
        return layout;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        vpRecent = (ViewPager) layout.findViewById(R.id.vp_recent);
        vpRecent.setAdapter(new PagerRecentAdapter(getChildFragmentManager(), getContext(),theLanguageisEnglish));
        vpRecent.setOffscreenPageLimit(3);

        tabRecent = (SmartTabLayout) layout.findViewById(R.id.tab_recent);
        tabRecent.setViewPager(vpRecent);

        if(!theLanguageisEnglish)
            vpRecent.setCurrentItem(2);

    }

}
