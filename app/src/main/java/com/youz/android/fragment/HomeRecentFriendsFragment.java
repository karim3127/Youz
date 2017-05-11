package com.youz.android.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import com.google.firebase.database.ValueEventListener;
import com.wang.avi.AVLoadingIndicatorView;
import com.youz.android.R;
import com.youz.android.activity.MainActivity;
import com.youz.android.activity.PostDetails;
import com.youz.android.adapter.HomeRecentItemAdapter;
import com.youz.android.model.Contact;
import com.youz.android.util.UtilContact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeRecentFriendsFragment extends Fragment {

    private static final int PERMISSIONS_REQUEST_CODE_PICK_CONTACTS = 23;
    private static final int REQUEST_CODE_PICK_CONTACTS = 7;

    private OnFragmentInteractionListener mListener;
    private View layout;

    private AVLoadingIndicatorView avloadingIndicatorView;
    private RelativeLayout rlNoPost;
    private RecyclerView rvPosts;
    private boolean hasMoreLoadind = true;
    private HomeRecentItemAdapter adapter;
    UtilContact utilContact;
    List<Contact> contactList = new ArrayList<>();
    public static List<String> listYouzContacts = new ArrayList<>();
    List<String> listYouzContactsCuurent = new ArrayList<>();
    List<Pair<String, HashMap<String, Object>>> listPosts = new ArrayList<>();

    private SharedPreferences prefs;
    private String userId, CodeCountry;
    public static String MyNumber;

    FirebaseDatabase mRootRef = FirebaseDatabase.getInstance();
    DatabaseReference mPostRef = mRootRef.getReference("posts");

    DatabaseReference mContactRef;
    private ChildEventListener childEventListener;
    private ChildEventListener childEventListenerUpdate;
    private Query mQueryFriendPost;

    public HomeRecentFriendsFragment() {
        // Required empty public constructor
    }

    public static HomeRecentFriendsFragment newInstance() {
        HomeRecentFriendsFragment fragment = new HomeRecentFriendsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_home_recent_friends, container, false);
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
        mContactRef.removeEventListener(childEventListener);
        mPostRef.removeEventListener(childEventListenerUpdate);
        listYouzContacts = new ArrayList<>();
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
        CodeCountry = prefs.getString("CodeCountry", "");
        MyNumber = prefs.getString("PhoneNumber", "");

        ((MainActivity) getActivity()).homeRecentFriendsFragment = this;
        utilContact = new UtilContact(getContext(), CodeCountry);

        mContactRef = mRootRef.getReference("contacts").child(userId);

        avloadingIndicatorView = (AVLoadingIndicatorView) layout.findViewById(R.id.avloadingIndicatorView);

        rlNoPost = (RelativeLayout) layout.findViewById(R.id.rl_no_post);

        rvPosts = (RecyclerView) layout.findViewById(R.id.rv_posts);
        rvPosts.setHasFixedSize(true);
        rvPosts.setItemViewCacheSize(300);
        LinearLayoutManager llm = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rvPosts.setLayoutManager(llm);

        if (adapter == null) {
            adapter = new HomeRecentItemAdapter(getActivity(), listPosts, false);
            rvPosts.setAdapter(adapter);

            getListPosts();

            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                contactList = utilContact.getContactList();
                if (contactList.size() > 0) {
                    getListYouzContact();
                }

            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_CODE_PICK_CONTACTS);
            }
        }
    }

    public void getPhoneContacts() {
        contactList = utilContact.getContactList();
    }

    public void getListYouzContact() {

        listYouzContactsCuurent = new ArrayList<>();

        for (int i = 0; i < contactList.size(); i++) {

            String phoneNumber = contactList.get(i).getNumber();

            final DatabaseReference mUserFriendRef = mRootRef.getReference("users").child(phoneNumber);

            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.getValue() != null) {

                        listYouzContacts.add(dataSnapshot.getKey());
                        listYouzContactsCuurent.add(dataSnapshot.getKey());

                        try {
                            ((MainActivity) getActivity()).tvNbFriend.setText(listYouzContactsCuurent.size() + "");
                        } catch (Exception e) {

                        }

                        HashMap<String, Object> newContact = new HashMap<>();
                        newContact.put(dataSnapshot.getKey(), true);
                        mContactRef.updateChildren(newContact);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            mUserFriendRef.addListenerForSingleValueEvent(valueEventListener);
        }
    }

    public void getListPosts() {

        Query mQueryMyPost = mPostRef.orderByChild("postOwner").equalTo(userId);
        ValueEventListener valueEventListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {

                    HashMap<String, Object> posts = (HashMap<String, Object>) dataSnapshot.getValue();

                    for (Map.Entry<String, Object> post : posts.entrySet()) {

                        int exist = 0;
                        for (int j = 0; j < listPosts.size() && exist == 0; j++) {
                            if (listPosts.get(j).first.equals(post.getKey())) {
                                exist = 1;
                            }
                        }

                        if (exist == 0) {
                            HashMap<String, Object> postDetails = (HashMap<String, Object>) post.getValue();
                            boolean isPublic = (boolean) postDetails.get("public");
                            if (!isPublic) {
                                Pair<String, HashMap<String, Object>> item = new Pair<>(post.getKey(), (HashMap<String, Object>) post.getValue());
                                adapter.addItemInRightPosition(item);
                                scrollUp();
                            }
                        }
                    }

                    avloadingIndicatorView.setVisibility(View.GONE);
                    if (adapter.getItemCount() > 0) {
                        rlNoPost.setVisibility(View.GONE);
                    } else {
                        rlNoPost.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        };
        //mQueryMyPost.addListenerForSingleValueEvent(valueEventListener);

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getValue() != null) {

                    mQueryFriendPost = mPostRef.orderByChild("postOwner").equalTo(dataSnapshot.getKey());

                    ValueEventListener valueEventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() != null) {

                                HashMap<String, Object> posts = (HashMap<String, Object>) dataSnapshot.getValue();

                                for (Map.Entry<String, Object> post : posts.entrySet()) {

                                    HashMap<String, Object> postDetails = (HashMap<String, Object>) post.getValue();
                                    boolean isBlocked = false;
                                    if (MainActivity.listYouzBlocks.size() > 0) {
                                        if (MainActivity.listYouzBlocks.contains(postDetails.get("postOwner"))) {
                                            isBlocked = true;
                                        }
                                    }
                                    boolean isPublic = (boolean) postDetails.get("public");

                                    if (!isBlocked && !isPublic) {
                                        Pair<String, HashMap<String, Object>> item = new Pair<>(post.getKey(), (HashMap<String, Object>) post.getValue());
                                        adapter.addItemInRightPosition(item);
                                        scrollUp();
                                    }
                                }

                                avloadingIndicatorView.setVisibility(View.GONE);
                                if (adapter.getItemCount() > 0) {
                                    rlNoPost.setVisibility(View.GONE);
                                } else {
                                    rlNoPost.setVisibility(View.VISIBLE);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }

                    };

                    mQueryFriendPost.addListenerForSingleValueEvent(valueEventListener);
                }

                avloadingIndicatorView.setVisibility(View.GONE);
                if (adapter.getItemCount() > 0) {
                    rlNoPost.setVisibility(View.GONE);
                } else {
                    rlNoPost.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mContactRef.addChildEventListener(childEventListener);

        childEventListenerUpdate = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getValue() != null) {

                    int exist = 0;
                    for (int j = 0; j < listPosts.size() && exist == 0; j++) {
                        if (listPosts.get(j).first.equals(dataSnapshot.getKey())) {
                            exist = 1;
                        }
                    }

                    if (exist == 0) {
                        HashMap<String, Object> postDetails = (HashMap<String, Object>) dataSnapshot.getValue();

                        if (listYouzContacts.contains(postDetails.get("postOwner")) && !postDetails.get("postOwner").equals(userId)) {
                            boolean isBlocked = false;
                            if (MainActivity.listYouzBlocks.size() > 0) {
                                if (MainActivity.listYouzBlocks.contains(postDetails.get("postOwner"))) {
                                    isBlocked = true;
                                }
                            }
                            boolean isPublic = (boolean) postDetails.get("public");

                            if (!isBlocked && !isPublic) {
                                Pair<String, HashMap<String, Object>> item = new Pair<>(dataSnapshot.getKey(), (HashMap<String, Object>) dataSnapshot.getValue());
                                adapter.addItemInRightPosition(item);
                                scrollUp();
                            }
                        }
                    }

                }

                if (adapter.getItemCount() > 0) {
                    rlNoPost.setVisibility(View.GONE);
                } else {
                    rlNoPost.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                int i = 0;
                boolean isHere = false;

                while (i < adapter.getItemCount() && !isHere) {
                    if (adapter.listItems.get(i).first.equals(dataSnapshot.getKey())) {
                        isHere = true;
                    } else {
                        i++;
                    }
                }

                HashMap<String, Object> postDetails = (HashMap<String, Object>) dataSnapshot.getValue();
                boolean isBlocked = false;
                if (MainActivity.listYouzBlocks.size() > 0) {
                    if (MainActivity.listYouzBlocks.contains(postDetails.get("postOwner"))) {
                        isBlocked = true;
                    }
                }
                boolean isPublic = (boolean) postDetails.get("public");

                if (isHere) {
                    if (!isBlocked && !isPublic) {
                        Pair<String, HashMap<String, Object>> item = new Pair<>(dataSnapshot.getKey(), (HashMap<String, Object>) dataSnapshot.getValue());
                        adapter.listItems.set(i, item);
                        adapter.notifyItemChanged(i);
                    } else {
                        adapter.listItems.remove(i);
                        adapter.notifyItemRemoved(i);
                    }
                } else {
                    if (listYouzContacts.contains(postDetails.get("postOwner")) && !postDetails.get("postOwner").equals(userId)) {
                        if (!isBlocked && !isPublic) {
                            Pair<String, HashMap<String, Object>> item = new Pair<>(dataSnapshot.getKey(), (HashMap<String, Object>) dataSnapshot.getValue());
                            adapter.addItemInRightPosition(item);
                        }
                        scrollUp();
                    }
                }

                if (adapter.getItemCount() > 0) {
                    rlNoPost.setVisibility(View.GONE);
                } else {
                    rlNoPost.setVisibility(View.VISIBLE);
                }

                if (isHere && PostDetails.currentPost != null && dataSnapshot.getKey().equals(PostDetails.currentPost.first)) {
                    Pair<String, HashMap<String, Object>> item = new Pair<>(dataSnapshot.getKey(), (HashMap<String, Object>) dataSnapshot.getValue());
                    PostDetails.currentPost = item;
                    sendMessageResult(dataSnapshot.getKey(), "update");
                }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                sendMessageResult(dataSnapshot.getKey(), "delete");
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mPostRef.addChildEventListener(childEventListenerUpdate);

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

    public void notifyBlockPost() {
        if (adapter != null && adapter.listItems.size() > 0) {
            int i = 0;
            while (i < adapter.listItems.size()) {
                String postOwner = (String) adapter.listItems.get(i).second.get("postOwner");
                if (MainActivity.listYouzBlocks.contains(postOwner)) {
                    sendMessageResult(adapter.listItems.get(i).first, "delete");
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
