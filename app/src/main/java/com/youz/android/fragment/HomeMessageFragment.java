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
import com.youz.android.adapter.HomeMessageItemAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

public class HomeMessageFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private View layout;

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private String userId;
    SimpleDateFormat format;

    private AVLoadingIndicatorView avloadingIndicatorView;
    private RelativeLayout rlNoMessage;
    private RecyclerView rvMessage;
    private HomeMessageItemAdapter adapter;
    List<Pair<String, HashMap<String, Object>>> listMessages = new ArrayList<>();
    List<String> listMessageIDs = new ArrayList<>();
    public static long lastDateConsult;

    FirebaseDatabase mRootRef = FirebaseDatabase.getInstance();
    DatabaseReference mChatRef;
    Query mQueryChat;
    ChildEventListener childEventListener;

    public static HashMap<String, Integer> hashMapAvatar = new HashMap<>();

    public HomeMessageFragment() {
        // Required empty public constructor
    }

    public static HomeMessageFragment newInstance() {
        HomeMessageFragment fragment = new HomeMessageFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_home_message, container, false);
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
        mChatRef.removeEventListener(childEventListener);
        hashMapAvatar = new HashMap<>();
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

        mChatRef = mRootRef.getReference("chats");
        mQueryChat = mChatRef.orderByChild("members/" + userId).equalTo(true);

        format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        format.setTimeZone(TimeZone.getDefault());

        avloadingIndicatorView = (AVLoadingIndicatorView) layout.findViewById(R.id.avloadingIndicatorView);

        rlNoMessage = (RelativeLayout) layout.findViewById(R.id.rl_no_message);

        rvMessage = (RecyclerView) layout.findViewById(R.id.rv_message);
        rvMessage.setHasFixedSize(true);
        rvMessage.setItemViewCacheSize(300);
        LinearLayoutManager llm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvMessage.setLayoutManager(llm);

        adapter = new HomeMessageItemAdapter(getActivity(), listMessages);
        rvMessage.setAdapter(adapter);

        getMessages();
    }

    public void getMessages() {

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getValue() != null) {
                    HashMap<String, Object> details = (HashMap<String, Object>) dataSnapshot.getValue();
                    Pair<String, HashMap<String, Object>> item = new Pair<>(dataSnapshot.getKey(), (HashMap<String, Object>) dataSnapshot.getValue());


                    if (details.get("lastMessageDateSent") != null && !details.get("lastMessageDateSent").equals("")) {

                        String deletedAt = (details.get("chatDeletes") != null && ((HashMap<String, Object>) details.get("chatDeletes")).get(userId) != null) ? (String) ((HashMap<String, Object>) details.get("chatDeletes")).get(userId) : null;
                        String itemDate = (String) item.second.get("lastMessageDateSent");

                        boolean showChat = true;
                        if (deletedAt != null) {

                            Date dateDelete = null;
                            try {
                                dateDelete = format.parse(deletedAt);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            Date dateDialog = null;
                            try {
                                dateDialog = format.parse(itemDate);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            if (dateDelete.getTime() > dateDialog.getTime()) {
                                showChat = false;
                            }
                        }

                        if (deletedAt == null || showChat) {
                            adapter.addItemInRightPosition(item);
                            scrollUp();
                        }
                    }
                }
                avloadingIndicatorView.setVisibility(View.GONE);
                if (adapter.getItemCount() > 0) {
                    rlNoMessage.setVisibility(View.GONE);
                } else {
                    rlNoMessage.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getValue() != null) {
                    HashMap<String, Object> details = (HashMap<String, Object>) dataSnapshot.getValue();
                    Pair<String, HashMap<String, Object>> item = new Pair<>(dataSnapshot.getKey(), (HashMap<String, Object>) dataSnapshot.getValue());

                    if (details.get("lastMessageDateSent") != null && !details.get("lastMessageDateSent").equals("")) {

                        String deletedAt = (details.get("chatDeletes") != null && ((HashMap<String, Object>) details.get("chatDeletes")).get(userId) != null) ? (String) ((HashMap<String, Object>) details.get("chatDeletes")).get(userId) : null;
                        String itemDate = (String) item.second.get("lastMessageDateSent");

                        boolean showChat = true;
                        if (deletedAt != null) {

                            Date dateDelete = null;
                            try {
                                dateDelete = format.parse(deletedAt);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            Date dateDialog = null;
                            try {
                                dateDialog = format.parse(itemDate);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            if (dateDelete.getTime() > dateDialog.getTime()) {
                                showChat = false;
                            }
                        }

                        if (deletedAt == null || showChat) {
                            int exist = 0;
                            for (int i = 0; i < adapter.getItemCount(); i++) {
                                if (adapter.listItems.get(i).first.equals(dataSnapshot.getKey())) {

                                    adapter.deleteItem(i);
                                    adapter.addItemInRightPosition(item);

                                    if (i > 0) {
                                        adapter.notifyItemChanged(i - 1);
                                    }
                                    if (i < adapter.getItemCount() - 1) {
                                        adapter.notifyItemChanged(i + 1);
                                    }
                                    exist = 1;
                                }
                            }
                            if (exist == 0) {
                                adapter.addItemInRightPosition(item);
                                scrollUp();
                            }
                        } else {
                            for (int i = 0; i < adapter.getItemCount(); i++) {
                                if (adapter.listItems.get(i).first.equals(dataSnapshot.getKey())) {
                                    adapter.deleteItem(i);
                                }
                            }
                        }
                    }
                }
                if (adapter.getItemCount() > 0) {
                    rlNoMessage.setVisibility(View.GONE);
                } else {
                    rlNoMessage.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    for (int i = adapter.getItemCount() - 1; i >= 0; i--) {
                        if (adapter.listItems.get(i).first.equals(dataSnapshot.getKey())) {

                            adapter.deleteItem(i);

                            if (adapter.getItemCount() == 0) {
                                rlNoMessage.setVisibility(View.VISIBLE);
                            } else {
                                rlNoMessage.setVisibility(View.GONE);
                            }
                        }
                    }
                    for (int i = 0; i < listMessageIDs.size(); i++) {
                        if (listMessageIDs.get(i).equals(dataSnapshot.getKey())) {
                            listMessageIDs.remove(i);
                        }
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mQueryChat.addChildEventListener(childEventListener);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                avloadingIndicatorView.setVisibility(View.GONE);
                if (adapter.getItemCount() > 0) {
                    rlNoMessage.setVisibility(View.GONE);
                } else {
                    rlNoMessage.setVisibility(View.VISIBLE);
                }
            }
        }, 5000);
    }

    private void scrollUp() {
        rvMessage.post(new Runnable() {
            @Override
            public void run() {
                rvMessage.smoothScrollToPosition(0);
            }
        });
    }

}
