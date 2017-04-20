package com.youz.android.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.wang.avi.AVLoadingIndicatorView;
import com.youz.android.R;
import com.youz.android.adapter.HomeNotifItemAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class HomeNotifFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private View layout;

    private AVLoadingIndicatorView avloadingIndicatorView;
    private RelativeLayout rlNoNotif;
    private RecyclerView rvNotif;
    private boolean hasMoreLoadind = true;
    private HomeNotifItemAdapter adapter;
    List<Pair<String, HashMap<String, Object>>> listNotifs = new ArrayList<>();

    FirebaseDatabase mRootRef = FirebaseDatabase.getInstance();
    DatabaseReference mAlertRef = mRootRef.getReference("alerts");
    private Query mAlertQuery;
    private ChildEventListener childEventListener;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private String userId;
    public static long lastDateConsult;

    public HomeNotifFragment() {
        // Required empty public constructor
    }

    public static HomeNotifFragment newInstance() {
        HomeNotifFragment fragment = new HomeNotifFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_home_notif, container, false);
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

    @Override
    public void onDestroy() {
        mAlertQuery.removeEventListener(childEventListener);
        editor.putLong("LastDateConsult", new Date().getTime());
        super.onDestroy();
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        prefs = getContext().getSharedPreferences("com.youz.android", Context.MODE_PRIVATE);
        editor = prefs.edit();
        userId = prefs.getString("UserId", "");
        lastDateConsult = prefs.getLong("LastDateConsult", new Date().getTime());

        mAlertQuery = mAlertRef.child(userId);

        avloadingIndicatorView = (AVLoadingIndicatorView) layout.findViewById(R.id.avloadingIndicatorView);

        rlNoNotif = (RelativeLayout) layout.findViewById(R.id.rl_no_notif);

        rvNotif = (RecyclerView) layout.findViewById(R.id.rv_notif);
        rvNotif.setHasFixedSize(true);
        rvNotif.setItemViewCacheSize(300);
        LinearLayoutManager llm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvNotif.setLayoutManager(llm);

        adapter = new HomeNotifItemAdapter(getActivity(), listNotifs);
        rvNotif.setAdapter(adapter);

        getNotifs();
        editor.putLong("LastDateConsult", new Date().getTime());
        editor.commit();
    }

    public void getNotifs() {

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getValue() != null) {
                    Pair<String, HashMap<String, Object>> item = new Pair<>(dataSnapshot.getKey(), (HashMap<String, Object>) dataSnapshot.getValue());
                    adapter.addItemInRightPosition(item);
                }
                avloadingIndicatorView.setVisibility(View.GONE);
                if (adapter.getItemCount() > 0) {
                    rlNoNotif.setVisibility(View.GONE);
                } else {
                    rlNoNotif.setVisibility(View.VISIBLE);
                }
                scrollUp();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                int i = 0;
                boolean isHere = false;

                while (i < adapter.getItemCount() && !isHere) {
                    if (adapter.listItems.get(i).first.equals(dataSnapshot.getKey())) {
                        isHere = true;
                        Pair<String, HashMap<String, Object>> item = new Pair<>(dataSnapshot.getKey(), (HashMap<String, Object>) dataSnapshot.getValue());
                        adapter.listItems.set(i, item);
                        adapter.notifyItemChanged(i);
                    } else {
                        i++;
                    }
                }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                int i = 0;
                boolean isHere = false;

                while (i < adapter.getItemCount() && !isHere) {
                    if (adapter.listItems.get(i).first.equals(dataSnapshot.getKey())) {
                        isHere = true;
                        adapter.listItems.remove(i);
                        adapter.notifyItemRemoved(i);
                    } else {
                        i++;
                    }
                }
                if (adapter.getItemCount() > 0) {
                    rlNoNotif.setVisibility(View.GONE);
                } else {
                    rlNoNotif.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mAlertQuery.addChildEventListener(childEventListener);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                avloadingIndicatorView.setVisibility(View.GONE);
                if (adapter.getItemCount() > 0) {
                    rlNoNotif.setVisibility(View.GONE);
                } else {
                    rlNoNotif.setVisibility(View.VISIBLE);
                }
            }
        }, 5000);
    }

    private void scrollUp() {
        rvNotif.post(new Runnable() {
            @Override
            public void run() {
                rvNotif.smoothScrollToPosition(0);
            }
        });
    }

}
