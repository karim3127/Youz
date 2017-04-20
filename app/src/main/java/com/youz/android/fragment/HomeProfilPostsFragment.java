package com.youz.android.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
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
import com.youz.android.activity.PostDetails;
import com.youz.android.adapter.HomeRecentItemAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HomeProfilPostsFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private View layout;

    private AVLoadingIndicatorView avloadingIndicatorView;
    private RelativeLayout rlNoPost;
    private RecyclerView rvPosts;
    private boolean hasMoreLoadind = true;
    private HomeRecentItemAdapter adapter;
    List<Pair<String, HashMap<String, Object>>> listPosts = new ArrayList<>();

    FirebaseDatabase mRootRef = FirebaseDatabase.getInstance();
    DatabaseReference mPostPopularRef = mRootRef.getReference("posts");
    private Query mPostQuery;
    private ChildEventListener childEventListener;
    private SharedPreferences prefs;
    private String userId;


    public HomeProfilPostsFragment() {
        // Required empty public constructor
    }

    public static HomeProfilPostsFragment newInstance() {
        HomeProfilPostsFragment fragment = new HomeProfilPostsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_home_profil_posts, container, false);
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
        mPostQuery.removeEventListener(childEventListener);
        super.onDestroy();
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        broadcaster = LocalBroadcastManager.getInstance(getContext());

        prefs = getContext().getSharedPreferences("com.youz.android", Context.MODE_PRIVATE);
        userId = prefs.getString("UserId", "");

        mPostQuery = mPostPopularRef.orderByChild("postOwner").equalTo(userId);

        avloadingIndicatorView = (AVLoadingIndicatorView) layout.findViewById(R.id.avloadingIndicatorView);

        rlNoPost = (RelativeLayout) layout.findViewById(R.id.rl_no_post);

        rvPosts = (RecyclerView) layout.findViewById(R.id.rv_posts);
        rvPosts.setHasFixedSize(true);
        rvPosts.setItemViewCacheSize(300);
        LinearLayoutManager llm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvPosts.setLayoutManager(llm);

        adapter = new HomeRecentItemAdapter(getActivity(), listPosts, false, false);
        rvPosts.setAdapter(adapter);

        getListPosts();
    }

    public void getListPosts() {

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
               if (dataSnapshot.getValue() != null) {
                    boolean isHere = false;
                    int i = 0;
                    while (i < adapter.getItemCount() && !isHere) {
                        if (adapter.listItems.get(i).first.equals(dataSnapshot.getKey())) {
                            isHere = true;
                        }else {
                            i++;
                        }
                    }
                    if (!isHere) {
                        Pair<String, HashMap<String, Object>> item = new Pair<>(dataSnapshot.getKey(), (HashMap<String, Object>) dataSnapshot.getValue());
                        adapter.addItemInRightPosition(item);
                    }
                }
                avloadingIndicatorView.setVisibility(View.GONE);
                if (adapter.getItemCount() > 0) {
                    rlNoPost.setVisibility(View.GONE);
                } else {
                    rlNoPost.setVisibility(View.VISIBLE);
                }
               // scrollUp();
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

                if (isHere && PostDetails.currentPost != null && dataSnapshot.getKey().equals(PostDetails.currentPost.first)) {
                    Pair<String, HashMap<String, Object>> item = new Pair<>(dataSnapshot.getKey(), (HashMap<String, Object>) dataSnapshot.getValue());
                    PostDetails.currentPost = item;
                    sendMessageResult(dataSnapshot.getKey(), "update");
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
                    rlNoPost.setVisibility(View.GONE);
                } else {
                    rlNoPost.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mPostQuery.addChildEventListener(childEventListener);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                avloadingIndicatorView.setVisibility(View.GONE);
                if (adapter.getItemCount() > 0) {
                    rlNoPost.setVisibility(View.GONE);
                } else {
                    rlNoPost.setVisibility(View.VISIBLE);
                }
            }
        }, 5000);
    }

    private void scrollUp() {
        rvPosts.post(new Runnable() {
            @Override
            public void run() {
                rvPosts.smoothScrollToPosition(0);
            }
        });
    }

    private LocalBroadcastManager broadcaster;
    public void sendMessageResult(String id, String message) {
        Intent intent = new Intent(PostDetails.COPA_RESULT);
        if(message != null) {
            intent.putExtra(PostDetails.COPA_MESSAGE, id);
            if (message.equals("update")) {
                intent.putExtra(PostDetails.COPA_UPDATE, message);
            } else if (message.equals("delete")) {
                intent.putExtra(PostDetails.COPA_DELETE, message);
            }
        }
        broadcaster.sendBroadcast(intent);
    }
}
