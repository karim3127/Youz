package com.youz.android.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wang.avi.AVLoadingIndicatorView;
import com.youz.android.R;
import com.youz.android.adapter.HomeRecentItemAdapter;
import com.youz.android.fragment.HomeRecentFriendsFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class TagClickedPosts extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.tv_toolbar)
    TextView tvToolbar;

    @BindView(R.id.rv_posts)
    RecyclerView rvPosts;

    @BindView(R.id.avloadingIndicatorView)
    AVLoadingIndicatorView avloadingIndicatorView;

    @BindView(R.id.rl_no_post)
    RelativeLayout rlNoPost;

    private Typeface typeFaceGras;
    private HomeRecentItemAdapter adapter;
    List<Pair<String, HashMap<String, Object>>> listPosts = new ArrayList<>();
    //List<String> listPostIds = new ArrayList<>();

    FirebaseDatabase mRootRef = FirebaseDatabase.getInstance();
    DatabaseReference mTagRef;
    DatabaseReference mPostRef;
    DatabaseReference mPostQuery;
    private ChildEventListener childEventListenerUpdate;
    private SharedPreferences prefs;
    private String userId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_posts);
        ButterKnife.bind(this);

        broadcaster = LocalBroadcastManager.getInstance(this);

        prefs = getSharedPreferences("com.youz.android", Context.MODE_PRIVATE);
        userId = prefs.getString("UserId", "");

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        typeFaceGras = Typeface.createFromAsset(getAssets(), "fonts/optima_bold.ttf");
        String tagName = getIntent().getExtras().getString("TagName");
        tvToolbar.setTypeface(typeFaceGras);
        tvToolbar.setText(tagName);

        mTagRef = mRootRef.getReference("tags").child(tagName.trim());
        mPostRef = mRootRef.getReference("posts");

        rvPosts.setHasFixedSize(true);
        rvPosts.setItemViewCacheSize(100);
        rvPosts.setLayoutManager(new LinearLayoutManager(this));

        adapter = new HomeRecentItemAdapter(this, listPosts, false);
        rvPosts.setAdapter(adapter);

        getListTags();
    }

    public void getListTags() {

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {

                    HashMap<String, Object> tags = (HashMap<String, Object>) dataSnapshot.getValue();

                    for (Map.Entry<String, Object> tag : tags.entrySet()) {

                        mPostQuery = mPostRef.child(tag.getKey());
                        ValueEventListener valueEventListener = new ValueEventListener() {

                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() != null) {
                                    HashMap<String, Object> postDetails = (HashMap<String, Object>) dataSnapshot.getValue();
                                    boolean isBlocked = false;
                                    if (MainActivity.listYouzBlocks.size() > 0) {
                                        if (MainActivity.listYouzBlocks.contains(postDetails.get("postOwner"))) {
                                            isBlocked = true;
                                        }
                                    }

                                    boolean isPublic = (boolean) postDetails.get("public");

                                    String postOwner = (String) postDetails.get("postOwner");
                                    boolean isFriend = HomeRecentFriendsFragment.listYouzContacts.contains(postOwner);

                                    if (!isBlocked && (isPublic || isFriend)) {
                                        Pair<String, HashMap<String, Object>> item = new Pair<>(dataSnapshot.getKey(), (HashMap<String, Object>) dataSnapshot.getValue());
                                        adapter.addItemInRightPosition(item);
                                        scrollUp();
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

                        mPostQuery.addListenerForSingleValueEvent(valueEventListener);
                    }

                    addUpdateListener();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mTagRef.addListenerForSingleValueEvent(valueEventListener);

    }

    private void addUpdateListener() {

        childEventListenerUpdate = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                int i = 0;
                boolean isHere = false;

                while (i < adapter.getItemCount() && !isHere) {
                    if (adapter.listItems.get(i).first.equals(dataSnapshot.getKey())) {
                        isHere = true;

                        HashMap<String, Object> postDetails = (HashMap<String, Object>) dataSnapshot.getValue();
                        boolean isBlocked = false;
                        if (MainActivity.listYouzBlocks.size() > 0) {
                            if (MainActivity.listYouzBlocks.contains(postDetails.get("postOwner"))) {
                                isBlocked = true;
                            }
                        }
                        boolean isPublic = (boolean) postDetails.get("public");

                        String postOwner = (String) postDetails.get("postOwner");
                        boolean isFriend = HomeRecentFriendsFragment.listYouzContacts.contains(postOwner);

                        if (!isBlocked && (isPublic || isFriend)) {
                            Pair<String, HashMap<String, Object>> item = new Pair<>(dataSnapshot.getKey(), (HashMap<String, Object>) dataSnapshot.getValue());
                            adapter.listItems.set(i, item);
                            adapter.notifyItemChanged(i);
                        } else {
                            adapter.listItems.remove(i);
                            adapter.notifyItemRemoved(i);

                            if (adapter.getItemCount() > 0) {
                                rlNoPost.setVisibility(View.GONE);
                            } else {
                                rlNoPost.setVisibility(View.VISIBLE);
                            }
                        }
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

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mPostRef.addChildEventListener(childEventListenerUpdate);
    }

    private void scrollUp() {
        rvPosts.post(new Runnable() {
            @Override
            public void run() {
                rvPosts.smoothScrollToPosition(0);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (childEventListenerUpdate != null) {
            mPostRef.removeEventListener(childEventListenerUpdate);
        }
        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_normal, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
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
