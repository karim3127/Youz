package com.youz.android.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.wang.avi.AVLoadingIndicatorView;
import com.youz.android.R;
import com.youz.android.adapter.BlocksListItemAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class BlocksList extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.tv_toolbar)
    TextView tvToolbar;

    @BindView(R.id.rv_blocks)
    RecyclerView rvBlocks;

    @BindView(R.id.avloadingIndicatorView)
    AVLoadingIndicatorView avloadingIndicatorView;

    @BindView(R.id.rl_no_blocks)
    RelativeLayout rlNoBlocks;

    private Typeface typeFaceGras;
    private BlocksListItemAdapter adapter;
    List<Pair<String, HashMap<String, Object>>> listBlocks = new ArrayList<>();

    FirebaseDatabase mRootRef = FirebaseDatabase.getInstance();
    DatabaseReference mBlockRef;
    private ChildEventListener childEventListenerUpdate;
    private SharedPreferences prefs;
    private String userId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocks_list);
        ButterKnife.bind(this);

        prefs = getSharedPreferences("com.youz.android", Context.MODE_PRIVATE);
        userId = prefs.getString("UserId", "");

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        typeFaceGras = Typeface.createFromAsset(getAssets(), "fonts/optima_bold.ttf");
        tvToolbar.setTypeface(typeFaceGras);

        mBlockRef = mRootRef.getReference("blocks").child(userId);

        rvBlocks.setHasFixedSize(true);
        rvBlocks.setItemViewCacheSize(100);
        rvBlocks.setLayoutManager(new LinearLayoutManager(this));

        adapter = new BlocksListItemAdapter(this, listBlocks);
        rvBlocks.setAdapter(adapter);

        getListBlocks();
    }

    public void getListBlocks() {

        childEventListenerUpdate = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getValue() != null) {
                    Pair<String, HashMap<String, Object>> item = new Pair<>(dataSnapshot.getKey(), (HashMap<String, Object>) dataSnapshot.getValue());
                    adapter.addItemInRightPosition(item);
                    scrollUp();
                }
                avloadingIndicatorView.setVisibility(View.GONE);
                if (adapter.getItemCount() > 0) {
                    rlNoBlocks.setVisibility(View.GONE);
                } else {
                    rlNoBlocks.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

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

                        if (adapter.getItemCount() > 0) {
                            rlNoBlocks.setVisibility(View.GONE);
                        } else {
                            rlNoBlocks.setVisibility(View.VISIBLE);
                        }

                    } else {
                        i++;
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
        mBlockRef.addChildEventListener(childEventListenerUpdate);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                avloadingIndicatorView.setVisibility(View.GONE);
                if (adapter.getItemCount() > 0) {
                    rlNoBlocks.setVisibility(View.GONE);
                } else {
                    rlNoBlocks.setVisibility(View.VISIBLE);
                }
            }
        }, 5000);
    }

    private void scrollUp() {
        rvBlocks.post(new Runnable() {
            @Override
            public void run() {
                rvBlocks.smoothScrollToPosition(0);
            }
        });
    }

    @Override
    protected void onDestroy() {
        mBlockRef.removeEventListener(childEventListenerUpdate);
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
}
