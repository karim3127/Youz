package com.youz.android.activity;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.util.Pair;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wang.avi.AVLoadingIndicatorView;
import com.youz.android.R;
import com.youz.android.adapter.TagsItemAdapter;
import com.youz.android.animation.AnimationUtil;
import com.youz.android.view.paperonboarding.listeners.AnimatorEndListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Tags extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.tv_toolbar)
    TextView tvToolbar;

    @BindView(R.id.rl_no_tags)
    RelativeLayout rlNoTags;

    @BindView(R.id.rv_tags)
    RecyclerView rvTags;

    @BindView(R.id.rv_search_tags)
    RecyclerView rvSearchTags;

    @BindView(R.id.avloadingIndicatorView)
    AVLoadingIndicatorView avloadingIndicatorView;

    @BindView(R.id.ll_search)
    LinearLayout llSearch;

    @BindView(R.id.et_search)
    EditText etSearch;

    private Typeface typeFaceGras;
    private TagsItemAdapter adapter;
    private TagsItemAdapter adapterSearch;
    List<Pair<String, HashMap<String, Object>>> listTags = new ArrayList<>();
    List<Pair<String, HashMap<String, Object>>> listAllTags = new ArrayList<>();

    FirebaseDatabase mRootRef = FirebaseDatabase.getInstance();
    DatabaseReference mTagRef;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tags);
        ButterKnife.bind(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        typeFaceGras = Typeface.createFromAsset(getAssets(), "fonts/optima_bold.ttf");
        tvToolbar.setTypeface(typeFaceGras);

        mTagRef = mRootRef.getReference("tags");

        rvSearchTags.setHasFixedSize(true);
        rvSearchTags.setItemViewCacheSize(100);
        rvSearchTags.setLayoutManager(new GridLayoutManager(this, 3));

        rvTags.setHasFixedSize(true);
        rvTags.setItemViewCacheSize(100);
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (listTags.get(position) == null) {
                    return gridLayoutManager.getSpanCount();
                }
                return 1;
            }
        });
        rvTags.setLayoutManager(gridLayoutManager);

        adapter = new TagsItemAdapter(this, listTags);
        rvTags.setAdapter(adapter);

        getListTags();

        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH && !etSearch.getText().toString().trim().equals("")) {

                    View viewFocus = getCurrentFocus();
                    if (viewFocus != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(viewFocus.getWindowToken(), 0);
                        etSearch.clearFocus();
                    }

                    searchTags(etSearch.getText().toString().trim());
                    return true;
                }
                return false;
            }
        });

    }

    public void getListTags() {

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    HashMap<String, Object> tags = (HashMap<String, Object>) dataSnapshot.getValue();

                    List<Pair<String, HashMap<String, Object>>> listItems = new ArrayList<Pair<String, HashMap<String, Object>>>();
                    for (Map.Entry<String, Object> tag : tags.entrySet()) {
                        Pair<String, HashMap<String, Object>> item = new Pair<>(tag.getKey(), (HashMap<String, Object>) tag.getValue());
                        listItems.add(item);
                    }

                    listItems = sortList(listItems);

                    listAllTags.addAll(listItems);

                    showTags(0, 10);
                }

                avloadingIndicatorView.setVisibility(View.GONE);
                if (adapter.getItemCount() > 0) {
                    rlNoTags.setVisibility(View.GONE);
                } else {
                    rlNoTags.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mTagRef.addListenerForSingleValueEvent(valueEventListener);
    }

    public List<Pair<String, HashMap<String, Object>>> sortList(List<Pair<String, HashMap<String, Object>>> list) {
        List<Pair<String, HashMap<String, Object>>> result = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            int pos = 0;
            boolean isHere = false;
            while (!isHere && pos < result.size()) {
                if (list.get(i).second.size() > result.get(pos).second.size()) {
                    isHere = true;
                } else {
                    pos++;
                }
            }
            result.add(pos, list.get(i));
        }

        return result;
    }

    public void showTags(int indexFrom, int indexTo) {
        int to = (listAllTags.size() > indexTo) ? indexTo : listAllTags.size();

        for (int i = indexFrom; i < to; i++) {
            adapter.addItemOnPosition(listAllTags.get(i), i);
        }
        if (indexFrom == 0 && indexTo < listAllTags.size()) {
            adapter.addItem(null);
        } else if (to == listAllTags.size()) {
            adapter.deleteFooter();
        }
    }

    private void searchTags(String tagName) {
        avloadingIndicatorView.setVisibility(View.VISIBLE);
        rvTags.setVisibility(View.GONE);
        rlNoTags.setVisibility(View.GONE);
        rvSearchTags.setVisibility(View.GONE);

        final List<Pair<String, HashMap<String, Object>>> listSearch = new ArrayList<>();
        for (int i = 0; i < listAllTags.size(); i++) {
            if (listAllTags.get(i).first.contains(tagName)) {
                listSearch.add(listAllTags.get(i));
            }
        }

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                avloadingIndicatorView.setVisibility(View.GONE);
                if (listSearch.size() > 0) {
                    adapterSearch = new TagsItemAdapter(Tags.this, listSearch);
                    rvSearchTags.setAdapter(adapterSearch);
                    rvSearchTags.setVisibility(View.VISIBLE);
                } else {
                    rlNoTags.setVisibility(View.VISIBLE);
                }
            }
        }, 1000);
    }

    private void setVisibleWithAnimation() {
        AnimationUtil.AnimationListener animationListener = new AnimationUtil.AnimationListener() {
            @Override
            public boolean onAnimationStart(View view) {
                return false;
            }

            @Override
            public boolean onAnimationEnd(View view) {
                etSearch.requestFocus();
                InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInputFromWindow(etSearch.getWindowToken(), InputMethodManager.SHOW_FORCED, 0);

                return false;
            }

            @Override
            public boolean onAnimationCancel(View view) {
                return false;
            }
        };

        llSearch.setAlpha(1);
        etSearch.setText("");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            llSearch.setVisibility(View.VISIBLE);
            AnimationUtil.reveal(llSearch, animationListener);

        } else {
            AnimationUtil.fadeInView(llSearch, 400, animationListener);
        }
    }

    @OnClick(R.id.iv_search)
    public void showSearchLayout() {
        setVisibleWithAnimation();
    }

    @OnClick(R.id.iv_go_back)
    public void hideSearchLayout() {

        View viewFocus = getCurrentFocus();
        if (viewFocus != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(viewFocus.getWindowToken(), 0);
            etSearch.clearFocus();
        }

        llSearch.animate().alpha(0).setListener(new AnimatorEndListener() {
            @Override
            public void onAnimationEnd(Animator animator) {
                llSearch.setVisibility(View.GONE);
            }
        }).start();

        avloadingIndicatorView.setVisibility(View.GONE);
        if (adapter.getItemCount() > 0) {
            rlNoTags.setVisibility(View.GONE);
            rvTags.setVisibility(View.VISIBLE);
            rvSearchTags.setVisibility(View.GONE);
        } else {
            rlNoTags.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        if (llSearch.getVisibility() == View.VISIBLE) {
            hideSearchLayout();
        } else {
            super.onBackPressed();
        }
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
