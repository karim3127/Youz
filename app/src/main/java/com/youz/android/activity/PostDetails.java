package com.youz.android.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.onesignal.OneSignal;
import com.rey.material.widget.RadioButton;
import com.wang.avi.AVLoadingIndicatorView;
import com.youz.android.R;
import com.youz.android.fragment.HomeNotifFragment;
import com.youz.android.fragment.HomeRecentFriendsFragment;
import com.youz.android.util.ConnectionDetector;
import com.youz.android.util.UtilDateTime;
import com.youz.android.util.UtilUserAvatar;
import com.youz.android.view.hashtaghelper.HashTagHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class PostDetails extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.iv_message)
    ImageView ivMessage;

    @BindView(R.id.img_more)
    ImageView imgMore;

    @BindView(R.id.avloadingIndicatorView)
    AVLoadingIndicatorView avloadingIndicatorView;

    @BindView(R.id.switcher)
    ViewSwitcher switcher;

    @BindView(R.id.et_comment)
    EditText etComment;

    @BindView(R.id.tv_date)
    TextView tvDate;

    @BindView(R.id.tv_status)
    TextView tvStatus;

    @BindView(R.id.tv_nb_fav)
    TextView tvNbFav;

    @BindView(R.id.tv_nb_comment)
    TextView tvNbComment;

    @BindView(R.id.tv_nb_reyouz)
    TextView tvNbReyouz;

    @BindView(R.id.img_back)
    ImageView imgBack;

    @BindView(R.id.v_back)
    View vBack;

    @BindView(R.id.tv_location)
    TextView tvLocation;

    @BindView(R.id.ll_location)
    LinearLayout llLocation;

    @BindView(R.id.rl_no_comment)
    RelativeLayout rlNoComment;

    @BindView(R.id.ll_comment)
    LinearLayout llComment;

    @BindView(R.id.lb_fav)
    LikeButton lbFav;

    @BindView(R.id.cardview_add_comment)
    CardView cvAddComment;

    static final public String COPA_MESSAGE = "POST.COPA_MSG";
    static final public String COPA_RESULT = "POST.REQUEST_PROCESSED";
    static final public String COPA_UPDATE = "POST.REQUEST_UPDATE";
    static final public String COPA_DELETE = "POST.REQUEST_DELETE";
    private BroadcastReceiver receiver;

    HashTagHelper mTextHashTagHelper;
    public static Pair<String, HashMap<String, Object>> currentPost;
    HashMap<String, Object> blockuserComment= new HashMap<String, Object>();
    boolean isAbleToSend = false;
    DisplayImageOptions options;
    SimpleDateFormat format;
    TimeZone timeZone;
    private Typeface typeFaceGras;
    ConnectionDetector connectionDetector;
    private SharedPreferences prefs;
    private String userId;
    String postOwner;

    FirebaseDatabase mRootRef = FirebaseDatabase.getInstance();
    DatabaseReference mChatRef;
    DatabaseReference mMemberRef;
    DatabaseReference mContactRef;
    DatabaseReference mPostRef;
    DatabaseReference mAlertRef;
    DatabaseReference mTagRef;
    DatabaseReference mUserRef;

    ValueEventListener valueEventListenerUser;
    DataSnapshot dataSnapshotUser;
    boolean isHimFriend = false;

    private int nbLikes;
    private int nbComments;
    private long nbReyouz;
    private boolean hasMoreComments = false;
    private boolean isWaiting = false;
    private String title, color, font, photo;
    private long fontSize;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);
        ButterKnife.bind(this);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                String id = intent.getExtras().getString(COPA_MESSAGE, "");
                String update = intent.getExtras().getString(COPA_UPDATE, "");
                String delete = intent.getExtras().getString(COPA_DELETE, "");

                if (currentPost.first.equals(id) && !update.equals("")){
                    showPostDetails();
                } else if (currentPost.first.equals(id) && !delete.equals("")) {
                    finish();
                }
            }
        };

        prefs = getSharedPreferences("com.youz.android", Context.MODE_PRIVATE);
        userId = prefs.getString("UserId", "");

        mChatRef = mRootRef.getReference("chats");
        mMemberRef = mRootRef.getReference("members/" + userId);
        mPostRef = mRootRef.getReference("posts");
        mTagRef = mRootRef.getReference("tags");

        typeFaceGras = Typeface.createFromAsset(getAssets(), "fonts/optima_bold.ttf");
        connectionDetector = new ConnectionDetector(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        timeZone = TimeZone.getDefault();

        format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        format.setTimeZone(timeZone);

        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        etComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (etComment.getText().length() > 0 && !isAbleToSend) {
                    isAbleToSend = true;
                    switcher.showNext();
                } else if (etComment.getText().length() == 0){
                    isAbleToSend = false;
                    switcher.showPrevious();
                }
            }
        });

        showPostDetails();

        lbFav.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                if (connectionDetector.isConnectingToInternet()) {
                    HashMap<String, Object> likes = new HashMap<>();
                    likes.put(userId, true);

                    mPostRef.child(currentPost.first + "/likes").updateChildren(likes, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                modifNote(currentPost.first, 1);

                                Uri sound = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.like_sound);
                                Ringtone ringtone = RingtoneManager.getRingtone(context, sound);
                                ringtone.play();

                            }
                        }
                    });

                    if (!postOwner.equals(userId)) {
                        final String dateSent = format.format(new Date());

                        HashMap<String, Object> alert = new HashMap<>();
                        alert.put("createdAt", dateSent);
                        alert.put("postId", currentPost.first);
                        alert.put("type", "like");
                        alert.put("userId", userId);

                        HashMap<String, Object> alertItem = new HashMap<>();
                        alertItem.put((userId + currentPost.first), alert);

                        mAlertRef.updateChildren(alertItem);

                        HashMap<String, Object> membersDetails = (HashMap<String, Object>) dataSnapshotUser.getValue();
                        if ((boolean) membersDetails.get("notifsLikes") && membersDetails.get("status").equals("offline")) {
                            List<String> userIds = new ArrayList<>();
                            if (membersDetails.get("oneSignalUserId") != null) {
                                userIds.add("'" + membersDetails.get("oneSignalUserId") + "'");
                            }

                            String messagePush = "Liked your post";
                            String userIdsList = userIds.toString();
                            try {
                                OneSignal.postNotification(new JSONObject("{'contents': {'en':'" + messagePush + "'}, 'ios_sound': 'Notification.mp3', 'data': {'type'='like','postId':'" + currentPost.first + "','userId':'" + userId + "'}, 'include_player_ids': " + userIdsList + "}"), null);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    nbLikes++;
                    tvNbFav.setText(nbLikes + "");
                } else {
                    Toast.makeText(PostDetails.this, getString(R.string.conx_down), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                if (connectionDetector.isConnectingToInternet()) {
                    mPostRef.child(currentPost.first + "/likes/" + userId).removeValue(new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            modifNote(currentPost.first, -1);
                        }
                    });

                    if (!postOwner.equals(userId)) {
                        mAlertRef.child(userId + currentPost.first).removeValue();
                    }

                    nbLikes--;
                    tvNbFav.setText(nbLikes + "");
                } else {
                    Toast.makeText(PostDetails.this, getString(R.string.conx_down), Toast.LENGTH_SHORT).show();
                }
            }
        });
        getMemberDetail();
        getMemberFriends();
    }

    @OnClick(R.id.img_more)
    public void chowMoreOptions() {
        PopupMenu popup = new PopupMenu(this, imgMore);
        //Inflating the Popup using xml file
        if (userId.equals(postOwner)) {
            popup.getMenuInflater().inflate(R.menu.menu_more_post_owner, popup.getMenu());
        } else {
            popup.getMenuInflater().inflate(R.menu.menu_more_post, popup.getMenu());
        }

        MenuItem menuItemSave = popup.getMenu().findItem(R.id.action_save);
        boolean isSaved;
        if (currentPost.second.get("saves") != null && ((HashMap<String, Object>) currentPost.second.get("saves")).get(userId) != null) {
            menuItemSave.setTitle("Delete save");
            isSaved = true;
        } else {
            menuItemSave.setTitle("Save");
            isSaved = false;
        }
        popup.show();
        final boolean finalIsSaved = isSaved;
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.action_reyouz) {
                    if (connectionDetector.isConnectingToInternet()) {
                        reyouzPost();
                    } else {
                        Toast.makeText(PostDetails.this, getString(R.string.conx_down), Toast.LENGTH_SHORT).show();
                    }
                } else if (id == R.id.action_share) {
                    startActivity(new Intent(context, PostShare.class));
                    PostShare.currentPost = currentPost;
                } else if (id == R.id.action_report) {
                    reportPost();
                } else if (id == R.id.action_save) {
                    if (connectionDetector.isConnectingToInternet()) {
                        if (finalIsSaved) {
                            mPostRef.child(currentPost.first + "/saves").child(userId).removeValue(new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    if (databaseError == null) {
                                        //modifNote(currentPost.first, false, );
                                        Toast.makeText(context, "Save deleted", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            HashMap<String, Object> saves = new HashMap<>();
                            saves.put(userId, true);

                            mPostRef.child(currentPost.first + "/saves").updateChildren(saves, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    if (databaseError == null) {
                                        //modifNote(currentPost.first, true);
                                        Toast.makeText(context, "Save added", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }

                    } else {
                        Toast.makeText(PostDetails.this, getString(R.string.conx_down), Toast.LENGTH_SHORT).show();
                    }
                } else if (id == R.id.action_delete) {
                    deletePost();
                } else if (id == R.id.action_update) {
                    startActivity(new Intent(context, UpdatePost.class));
                    UpdatePost.currentPost = currentPost;
                    finish();
                } else if (id == R.id.action_block) {
                    blockPost();
                }

                return false;
            }
        });
    }

    @OnClick(R.id.iv_message)
    public void sendMessage() {
        if (connectionDetector.isConnectingToInternet()) {
            HashMap<String, Object> membersDetails = (HashMap<String, Object>) dataSnapshotUser.getValue();
            boolean enablePublicChat = (membersDetails.get("enablePublicChat") != null) ? (boolean) membersDetails.get("enablePublicChat") : true;
            if (enablePublicChat || isHimFriend) {
                searchPrivateChat(userId, (String) currentPost.second.get("postOwner"));
            } else {
                Toast.makeText(this, "Connexion with post owner is failed", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(this, getString(R.string.conx_down), Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.img_send)
    public void addComment() {
        if (connectionDetector.isConnectingToInternet() && !etComment.getText().toString().trim().equals("")) {
            Date createdDate = new Date();
            final String createdAt = format.format(createdDate);

            HashMap<String, Object> newComment = new HashMap<>();
            newComment.put("createdAt", createdAt);
            newComment.put("commentText", etComment.getText().toString().trim());
            newComment.put("commentOwner", userId);

            final String key = mPostRef.child(currentPost.first + "/comments").push().getKey();

            HashMap<String, Object> comment = new HashMap<>();
            comment.put(key, newComment);

            mPostRef.child(currentPost.first + "/comments").updateChildren(comment, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        modifNote(currentPost.first, 2);
                    }
                }
            });

            final View viewComment = LayoutInflater.from(this).inflate(R.layout.rv_comment_item, null);
            TextView tvCommentText = (TextView) viewComment.findViewById(R.id.tv_comment_text);
            TextView tvCommentDate = (TextView) viewComment.findViewById(R.id.tv_comment_date);
            ImageView ivCommentOwner = (ImageView) viewComment.findViewById(R.id.iv_comment_owner);
            final TextView tvNbCommentLike = (TextView) viewComment.findViewById(R.id.tv_nb_like);
            LikeButton lbCommentFav = (LikeButton) viewComment.findViewById(R.id.lb_comment_fav);

            final int[] nbCommentLike = {0};
            tvNbCommentLike.setText(nbCommentLike[0] + "");

            tvCommentText.setText(etComment.getText().toString().trim());

            tvCommentDate.setText(UtilDateTime.formatTime(this, createdDate));

            if (userId.equals(postOwner)) {
                ivCommentOwner.setImageResource(R.drawable.av_admin);
                tvCommentText.setTextColor(getResources().getColor(R.color.colorGreen));
            } else {
                int res = UtilUserAvatar.getDrawableRes(this, userId);
                ivCommentOwner.setImageResource(res);
            }

            lbCommentFav.setOnLikeListener(new OnLikeListener() {
                @Override
                public void liked(LikeButton likeButton) {
                    HashMap<String, Object> likes = new HashMap<>();
                    likes.put(userId, true);

                    mPostRef.child(currentPost.first + "/comments/" + key + "/likes").updateChildren(likes);
                    nbCommentLike[0]++;
                    tvNbCommentLike.setText(nbCommentLike[0] + "");
                }

                @Override
                public void unLiked(LikeButton likeButton) {
                    mPostRef.child(currentPost.first + "/comments/" + key + "/likes/" + userId).removeValue();
                    nbCommentLike[0]--;
                    tvNbCommentLike.setText(nbCommentLike[0] + "");
                }
            });

            viewComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                   /* View viewMoreComment = getLayoutInflater().inflate(R.layout.layout_more_comment, null);
                    TextView tvDeleteComment = (TextView) viewMoreComment.findViewById(R.id.tv_delete);
                    TextView tvReportComment = (TextView) viewMoreComment.findViewById(R.id.tv_report);

                    tvReportComment.setVisibility(View.GONE);

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setView(viewMoreComment);

                    final AlertDialog alert = builder.create();

                    tvDeleteComment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alert.dismiss();

                            mPostRef.child(currentPost.first + "/comments").child(key).removeValue(new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    modifNote(currentPost.first, -2);
                                    llComment.removeView(viewComment);
                                    if (llComment.getChildCount() == 0) {
                                        rlNoComment.setVisibility(View.VISIBLE);
                                    } else {
                                        rlNoComment.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }
                    });

                    alert.show();*/

                    View viewMoreComment = getLayoutInflater().inflate(R.layout.layout_more_comment, null);
                    TextView tvDeleteComment = (TextView) viewMoreComment.findViewById(R.id.tv_delete);
                    TextView tvReportComment = (TextView) viewMoreComment.findViewById(R.id.tv_report);
                    TextView tvBlockComment = (TextView) viewMoreComment.findViewById(R.id.tv_block);

                    tvReportComment.setVisibility(View.GONE);
                    tvBlockComment.setVisibility(View.GONE);

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setView(viewMoreComment);

                    final AlertDialog alert = builder.create();

                    tvDeleteComment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alert.dismiss();

                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            String positiveBtnText = "";
                            String negativeBtnText = "";

                            builder.setMessage(""+getString(R.string.dialog_message_delete_comment));
                            positiveBtnText = getString(R.string.dialog_validate);
                            negativeBtnText = getString(R.string.dialog_cancel);

                            builder.setPositiveButton(positiveBtnText, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //do your work here
                                    mPostRef.child(currentPost.first + "/comments").child(key).removeValue(new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                            modifNote(currentPost.first, -2);
                                            llComment.removeView(viewComment);
                                            if (llComment.getChildCount() == 0) {
                                                rlNoComment.setVisibility(View.VISIBLE);
                                            } else {
                                                rlNoComment.setVisibility(View.GONE);
                                            }
                                        }
                                    });
                                    dialog.dismiss();
                                }
                            });
                            builder.setNegativeButton(negativeBtnText, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            AlertDialog alert = builder.create();
                            try {
                                alert.show();
                                alert.getWindow().getAttributes();
                            } catch (Exception e) {

                            }

                            Button btnNegatif = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                            btnNegatif.setTextColor(context.getResources().getColor(R.color.colorRed));

                            Button btnPositif = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                            btnPositif.setTextColor(context.getResources().getColor(R.color.colorPrimary));


                        }
                    });

                    try {
                        alert.show();
                    } catch (Exception e) {

                    }

                    //return false;
                }
            });

            llComment.addView(viewComment, 0);

            etComment.setText("");
            nbComments++;
            tvNbComment.setText(nbComments + "");
            rlNoComment.setVisibility(View.GONE);

            if (!postOwner.equals(userId)) {
                final String dateSent = format.format(new Date());

                HashMap<String, Object> alert = new HashMap<>();
                alert.put("createdAt", dateSent);
                alert.put("postId", currentPost.first);
                alert.put("type", "comment");
                alert.put("userId", userId);

                String alertId = mAlertRef.push().getKey();
                HashMap<String, Object> alertItem = new HashMap<>();
                alertItem.put(alertId, alert);

                mAlertRef.updateChildren(alertItem);

                HashMap<String, Object> membersDetails = (HashMap<String, Object>) dataSnapshotUser.getValue();
                if ((boolean) membersDetails.get("notifsComments") && membersDetails.get("status").equals("offline")) {
                    List<String> userIds = new ArrayList<>();
                    if (membersDetails.get("oneSignalUserId") != null) {
                        userIds.add("'" + membersDetails.get("oneSignalUserId") + "'");
                    }

                    String messagePush = "Commented your post";
                    String userIdsList = userIds.toString();
                    try {
                        OneSignal.postNotification(new JSONObject("{'contents': {'en':'" + messagePush + "'}, 'ios_sound': 'Notification.mp3', 'data': {'type'='comment','postId':'" + currentPost.first + "','userId':'" + userId + "'}, 'include_player_ids': " + userIdsList + "}"), null);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            if (!connectionDetector.isConnectingToInternet()) {
                Toast.makeText(PostDetails.this,getString(R.string.conx_down), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(COPA_RESULT)
        );
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mUserRef.removeEventListener(valueEventListenerUser);
        notifyHomeNotifBadge();
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
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void notifyHomeNotifBadge() {

        int i = 0;
        while (i < HomeNotifFragment.listUnreadDialog.size()) {
            if (HomeNotifFragment.listUnreadDialog.get(i).second.equals(currentPost.first)) {
                HomeNotifFragment.listUnreadDialog.remove(i);
            } else {
                i++;
            }
        }

        if (HomeNotifFragment.listUnreadDialog.size() > 0) {
            MainActivity.tvBadgeNotif.setVisibility(View.VISIBLE);
            MainActivity.tvBadgeNotif.setText(HomeNotifFragment.listUnreadDialog.size() + "");
        } else {
            MainActivity.tvBadgeNotif.setVisibility(View.GONE);
        }
    }

    public void showPostDetails() {

        hasMoreComments = false;
        isWaiting = false;

        final HashMap<String, Object> current = currentPost.second;
        postOwner = (String) current.get("postOwner");
        mAlertRef = mRootRef.getReference("alerts").child(postOwner);
        mUserRef = mRootRef.getReference("users").child(postOwner);
        mContactRef = mRootRef.getReference("contacts/" + userId);

        blockuserComment = (HashMap<String, Object>) current.get("blocks");
        if(blockuserComment == null)
            blockuserComment = new HashMap<String, Object>();

        if (userId.equals(postOwner)) {
            ivMessage.setVisibility(View.GONE);
            avloadingIndicatorView.setVisibility(View.GONE);
            cvAddComment.setVisibility(View.VISIBLE);
        }else{

            if(blockuserComment.containsKey(userId)){
                cvAddComment.setVisibility(View.GONE);
            }else{
                cvAddComment.setVisibility(View.VISIBLE);
            }
        }

        if (userId.equals(postOwner)) {
            ivMessage.setVisibility(View.GONE);
            avloadingIndicatorView.setVisibility(View.GONE);
        }

        title = (String) current.get("title");
        color = (current.get("color") == null) ? "" : (String) current.get("color");
        photo = (current.get("photo") == null) ? "" : (String) current.get("photo");
        font = (current.get("font") == null) ? "" : (String) current.get("font");
        fontSize = (current.get("fontSize") == null) ? -1 : (long) current.get("fontSize");
        String location = (current.get("location") == null) ? "" : (String) current.get("location");
        String city = (current.get("city") == null) ? "" : " - " + current.get("city");
        nbLikes = (current.get("likes") == null) ? 0 : ((HashMap<String, Object>) current.get("likes")).size();
        nbComments = (current.get("comments") == null) ? 0 : ((HashMap<String, Object>) current.get("comments")).size();
        nbReyouz = (current.get("reyouzCount") == null) ? 0 : (long) current.get("reyouzCount");


        Date dateDialog = null;
        try {
            dateDialog = format.parse((String) current.get("createdAt"));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        tvDate.setText(UtilDateTime.formatTime(this, dateDialog));
        tvDate.setTypeface(typeFaceGras);

        tvStatus.setText(title);

        tvNbFav.setText(nbLikes + "");
        tvNbFav.setTypeface(typeFaceGras);

        tvNbReyouz.setText(nbReyouz + "");
        tvNbReyouz.setTypeface(typeFaceGras);

        tvNbComment.setText(nbComments + "");
        tvNbComment.setTypeface(typeFaceGras);

        boolean isPublic = (boolean) current.get("public");
        String postOwner = (String) current.get("postOwner");
        boolean isFriend = HomeRecentFriendsFragment.listYouzContacts.contains(postOwner);
        if (isPublic || !isFriend) {
            if (location.equals("")) {
                llLocation.setVisibility(View.INVISIBLE);
            } else {
                llLocation.setVisibility(View.VISIBLE);
                tvLocation.setTypeface(typeFaceGras);
                tvLocation.setText(Html.fromHtml(location + city));
            }
        } else {
            llLocation.setVisibility(View.INVISIBLE);
        }


        if (!font.equals("")) {
            try {
                Typeface typefaceStatus = Typeface.createFromAsset(getAssets(), "fonts/" + font + ".ttf");
                tvStatus.setTypeface(typefaceStatus);
            } catch (Exception e) {

            }
        }
        if (fontSize > 0) {
            double size;
            if (fontSize == 6) {
                size = 4;
            } else if (fontSize == 8) {
                size = 4.5;
            } else if (fontSize == 10) {
                size = 5;
            } else if (fontSize == 12) {
                size = 5.5;
            } else if (fontSize == 14) {
                size = 6;
            } else if (fontSize == 16) {
                size = 6.5;
            } else if (fontSize == 18) {
                size = 7;
            } else {
                size = 4.5;
            }

            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            long px = Math.round(size * (displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
            tvStatus.setTextSize(px);
        }
        if (!photo.equals("")) {
            ImageLoader.getInstance().displayImage(photo, imgBack, options);
            vBack.setAlpha(0.5f);
        } else {
            vBack.setAlpha(1f);
        }

        if (!color.equals("")) {
            try {
                vBack.setBackgroundColor(Color.parseColor("#" + color));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        char[] chars = {'_'};
        mTextHashTagHelper = HashTagHelper.Creator.create(context.getResources().getColor(R.color.colorAccent), new HashTagHelper.OnHashTagClickListener() {
            @Override
            public void onHashTagClicked(String hashTag) {
                Intent intent = new Intent(context, TagClickedPosts.class);
                intent.putExtra("TagName", hashTag);
                context.startActivity(intent);
            }
        }, chars);
        mTextHashTagHelper.handle(tvStatus);

        if (current.get("likes") != null && ((HashMap<String, Object>) current.get("likes")).get(userId) != null) {
            lbFav.setLiked(true);
        } else {
            lbFav.setLiked(false);
        }

        getComments(current);
    }

    private void getComments(HashMap<String, Object> current) {

        if (nbComments > 0) {
            rlNoComment.setVisibility(View.GONE);
            llComment.removeAllViews();

            HashMap<String, Object> comments = (HashMap<String, Object>) current.get("comments");

            final List<Pair<String, HashMap<String, Object>>> listSortedComment = new ArrayList();

            for (Map.Entry<String, Object> item : comments.entrySet()) {
                Pair<String, HashMap<String, Object>> newItem = new Pair<>(item.getKey(), (HashMap<String, Object>) item.getValue());
                listSortedComment.add(newItem);
            }

            Collections.sort(listSortedComment, new Comparator<Pair<String,HashMap<String,Object>>>(){
                public int compare(Pair<String,HashMap<String,Object>> item1, Pair<String,HashMap<String,Object>> item2) {
                    String date1 = (String) item1.second.get("createdAt");
                    String date2 = (String) item2.second.get("createdAt");

                    Date dateValue1 = null;
                    try {
                        dateValue1 = format.parse(date1);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    Date dateValue2 = null;
                    try {
                        dateValue2 = format.parse(date2);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    return dateValue1.compareTo(dateValue2);
                }
            });

            if (listSortedComment.size() > 20) {
                showListComments(listSortedComment, 0, 20);
            } else {
                showListComments(listSortedComment, 0, listSortedComment.size());
            }

        } else {
            rlNoComment.setVisibility(View.VISIBLE);
        }
    }

    private void showListComments(final List<Pair<String, HashMap<String, Object>>> listSortedComment, final int from, int to) {

        for (int i = from; i < listSortedComment.size() && i < to; i++) {
            final String commentId = listSortedComment.get(i).first;
            final HashMap<String, Object> itemDetails = listSortedComment.get(i).second;

            final String commentOwner = (String) itemDetails.get("commentOwner");

            final View viewComment = LayoutInflater.from(this).inflate(R.layout.rv_comment_item, null);
            TextView tvCommentText = (TextView) viewComment.findViewById(R.id.tv_comment_text);
            TextView tvCommentDate = (TextView) viewComment.findViewById(R.id.tv_comment_date);
            ImageView ivCommentOwner = (ImageView) viewComment.findViewById(R.id.iv_comment_owner);
            final TextView tvNbCommentLike = (TextView) viewComment.findViewById(R.id.tv_nb_like);
            LikeButton lbCommentFav = (LikeButton) viewComment.findViewById(R.id.lb_comment_fav);
            final LinearLayout llSubComment = (LinearLayout) viewComment.findViewById(R.id.ll_sub_comment);

            tvCommentText.setText((String) itemDetails.get("commentText"));

            final int[] nbCommentLike = {(itemDetails.get("likes") == null) ? 0 : ((HashMap<String, Object>) itemDetails.get("likes")).size()};
            tvNbCommentLike.setText(nbCommentLike[0] + "");

            if (nbCommentLike[0] > 0) {
                if (((HashMap<String, Object>) itemDetails.get("likes")).get(userId) != null) {
                    lbCommentFav.setLiked(true);
                }
            }

            Date date = null;
            try {
                date = format.parse((String) itemDetails.get("createdAt"));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            tvCommentDate.setText(UtilDateTime.formatTime(this, date));

            if (itemDetails.get("commentOwner").equals(postOwner)) {
                ivCommentOwner.setImageResource(R.drawable.av_admin);
                tvCommentText.setTextColor(getResources().getColor(R.color.colorGreen));
            } else {
                int res = UtilUserAvatar.getDrawableRes(this, (String) itemDetails.get("commentOwner"));
                ivCommentOwner.setImageResource(res);
            }

            lbCommentFav.setOnLikeListener(new OnLikeListener() {
                @Override
                public void liked(LikeButton likeButton) {
                    HashMap<String, Object> likes = new HashMap<>();
                    likes.put(userId, true);

                    mPostRef.child(currentPost.first + "/comments/" + commentId + "/likes").updateChildren(likes);
                    nbCommentLike[0]++;
                    tvNbCommentLike.setText(nbCommentLike[0] + "");
                }

                @Override
                public void unLiked(LikeButton likeButton) {
                    mPostRef.child(currentPost.first + "/comments/" + commentId + "/likes/" + userId).removeValue();
                    nbCommentLike[0]--;
                    tvNbCommentLike.setText(nbCommentLike[0] + "");
                }
            });

            viewComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    View viewMoreComment = getLayoutInflater().inflate(R.layout.layout_more_comment, null);
                    TextView tvDeleteComment = (TextView) viewMoreComment.findViewById(R.id.tv_delete);
                    TextView tvReportComment = (TextView) viewMoreComment.findViewById(R.id.tv_report);
                    TextView tvSubComment = (TextView) viewMoreComment.findViewById(R.id.tv_sub_comment);
                    TextView tvBlockComment = (TextView) viewMoreComment.findViewById(R.id.tv_block);

                    if (userId.equals(postOwner)) {
                        tvSubComment.setVisibility(View.VISIBLE);
                    }else{

                        if(blockuserComment.containsKey(userId)){
                            tvSubComment.setVisibility(View.GONE);
                        }else{
                            tvSubComment.setVisibility(View.VISIBLE);
                        }
                    }

                    if(blockuserComment.containsKey(itemDetails.get("commentOwner")))
                        tvBlockComment.setText(""+getString(R.string.UnBlockuser));
                    else
                        tvBlockComment.setText(getString(R.string.Blockuser));

                    if (userId.equals(postOwner) || itemDetails.get("commentOwner").equals(userId)) {
                        tvDeleteComment.setVisibility(View.VISIBLE);
                        tvBlockComment.setVisibility(View.VISIBLE);
                        if (itemDetails.get("commentOwner").equals(userId)) {
                            tvReportComment.setVisibility(View.GONE);
                            tvBlockComment.setVisibility(View.GONE);
                        }
                    } else {
                        tvDeleteComment.setVisibility(View.GONE);
                        tvBlockComment.setVisibility(View.GONE);
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setView(viewMoreComment);

                    final AlertDialog alert = builder.create();

                    tvBlockComment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alert.dismiss();
                            blockUserComment(itemDetails.get("commentOwner").toString(),true);
                        }
                    });

                    tvDeleteComment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alert.dismiss();

                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            String positiveBtnText = "";
                            String negativeBtnText = "";

                            builder.setMessage(getString(R.string.dialog_message_delete_comment));
                            positiveBtnText = getString(R.string.dialog_validate);
                            negativeBtnText = getString(R.string.dialog_cancel);

                            builder.setPositiveButton(positiveBtnText, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //do your work here
                                    mPostRef.child(currentPost.first + "/comments").child(commentId).removeValue(new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                            modifNote(currentPost.first, -2);
                                            llComment.removeView(viewComment);
                                            if (llComment.getChildCount() == 0) {
                                                rlNoComment.setVisibility(View.VISIBLE);
                                            } else {
                                                rlNoComment.setVisibility(View.GONE);
                                            }
                                        }
                                    });
                                    dialog.dismiss();
                                }
                            });
                            builder.setNegativeButton(negativeBtnText, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            AlertDialog alert = builder.create();
                            try {
                                alert.show();
                                alert.getWindow().getAttributes();
                            } catch (Exception e) {

                            }

                            Button btnNegatif = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                            btnNegatif.setTextColor(context.getResources().getColor(R.color.colorRed));

                            Button btnPositif = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                            btnPositif.setTextColor(context.getResources().getColor(R.color.colorPrimary));

                        }
                    });

                    tvReportComment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alert.dismiss();
                            reportComment(commentId);
                        }
                    });

                    tvSubComment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alert.dismiss();
                            showSubCommentDialog(commentId, commentOwner, llSubComment);
                        }
                    });

                    try {
                        alert.show();
                    } catch (Exception e) {

                    }

                    //return false;
                }
            });

            List<Pair<String, HashMap<String, Object>>> listSubComment = new ArrayList<>();
            if (itemDetails.get("subComments") != null) {
                HashMap<String, Object> subComments = (HashMap<String, Object>) itemDetails.get("subComments");

                for (Map.Entry<String, Object> item : subComments.entrySet()) {
                    Pair<String, HashMap<String, Object>> newItem = new Pair<>(item.getKey(), (HashMap<String, Object>) item.getValue());
                    listSubComment.add(newItem);
                }
            }
            showListSubComment(commentId, listSubComment,llSubComment);
            llComment.addView(viewComment, i);
        }

        if ((to - from) == 20 && !hasMoreComments) {
            hasMoreComments = true;
            View viewMoreComment = LayoutInflater.from(this).inflate(R.layout.rv_more_item, null);

            final TextView tvMore = (TextView) viewMoreComment.findViewById(R.id.tv_more);
            final AVLoadingIndicatorView avloadingIndicatorView = (AVLoadingIndicatorView) viewMoreComment.findViewById(R.id.avloadingIndicatorView);

            viewMoreComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!isWaiting) {
                        isWaiting = true;
                        tvMore.setVisibility(View.INVISIBLE);
                        avloadingIndicatorView.setVisibility(View.VISIBLE);

                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                tvMore.setVisibility(View.VISIBLE);
                                avloadingIndicatorView.setVisibility(View.GONE);
                                isWaiting = false;
                                int childCount = llComment.getChildCount();
                                int indexFrom = childCount - 1;
                                int indexTo = (listSortedComment.size() > (childCount + 19)) ? childCount + 19 : listSortedComment.size();
                                showListComments(listSortedComment, indexFrom, indexTo);

                            }
                        }, 1000);
                    }
                }
            });

            llComment.addView(viewMoreComment);
        } else if ((to - from) < 20 && hasMoreComments) {
            hasMoreComments = false;
            llComment.removeViewAt(llComment.getChildCount() - 1);
        }
    }

    private void showListSubComment(final String comment, final List<Pair<String, HashMap<String, Object>>> listSortedComment, final LinearLayout layoutComment) {
        for (int i = 0; i < listSortedComment.size(); i++) {
            final String subCommentId = listSortedComment.get(i).first;
            final HashMap<String, Object> itemDetails = listSortedComment.get(i).second;

            final View viewSubComment = LayoutInflater.from(this).inflate(R.layout.rv_sub_comment_item, null);
            TextView tvSubCommentText = (TextView) viewSubComment.findViewById(R.id.tv_sub_comment_text);
            TextView tvSubCommentDate = (TextView) viewSubComment.findViewById(R.id.tv_sub_comment_date);
            ImageView ivSubCommentOwner = (ImageView) viewSubComment.findViewById(R.id.iv_sub_comment_owner);
            final TextView tvNbSubCommentLike = (TextView) viewSubComment.findViewById(R.id.tv_nb_sub_like);
            LikeButton lbSubCommentFav = (LikeButton) viewSubComment.findViewById(R.id.lb_sub_comment_fav);

            tvSubCommentText.setText((String) itemDetails.get("commentText"));

            Date date = null;
            try {
                date = format.parse((String) itemDetails.get("createdAt"));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            tvSubCommentDate.setText(UtilDateTime.formatTime(this, date));

            final int[] nbCommentLike = {(itemDetails.get("likes") == null) ? 0 : ((HashMap<String, Object>) itemDetails.get("likes")).size()};
            tvNbSubCommentLike.setText(nbCommentLike[0] + "");

            if (nbCommentLike[0] > 0) {
                if (((HashMap<String, Object>) itemDetails.get("likes")).get(userId) != null) {
                    lbSubCommentFav.setLiked(true);
                }
            }

            if (itemDetails.get("commentOwner").equals(postOwner)) {
                ivSubCommentOwner.setImageResource(R.drawable.av_admin);
                tvSubCommentText.setTextColor(getResources().getColor(R.color.colorGreen));
            } else {
                int res = UtilUserAvatar.getDrawableRes(this, itemDetails.get("commentOwner") + "");
                ivSubCommentOwner.setImageResource(res);
            }

            lbSubCommentFav.setOnLikeListener(new OnLikeListener() {
                @Override
                public void liked(LikeButton likeButton) {
                    HashMap<String, Object> likes = new HashMap<>();
                    likes.put(userId, true);

                    mPostRef.child(currentPost.first + "/comments/" + comment + "/subComments/" + subCommentId + "/likes").updateChildren(likes);
                    nbCommentLike[0]++;
                    tvNbSubCommentLike.setText(nbCommentLike[0] + "");
                }

                @Override
                public void unLiked(LikeButton likeButton) {
                    mPostRef.child(currentPost.first + "/comments/" + comment + "/subComments/" + subCommentId + "/likes/" + userId).removeValue();
                    nbCommentLike[0]--;
                    tvNbSubCommentLike.setText(nbCommentLike[0] + "");
                }
            });

            viewSubComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    final View viewMoreComment = getLayoutInflater().inflate(R.layout.layout_more_comment, null);
                    TextView tvDeleteComment = (TextView) viewMoreComment.findViewById(R.id.tv_delete);
                    TextView tvReportComment = (TextView) viewMoreComment.findViewById(R.id.tv_report);
                    TextView tvBlockComment = (TextView) viewMoreComment.findViewById(R.id.tv_block);
                    if(blockuserComment.containsKey(itemDetails.get("commentOwner")))
                        tvBlockComment.setText(getString(R.string.UnBlockuser));
                    else
                        tvBlockComment.setText(getString(R.string.Blockuser));

                    final LinearLayout subComment = (LinearLayout) viewMoreComment.findViewById(R.id.ll_sub_comment);

                    subComment.setVisibility(View.GONE);

                    if (userId.equals(postOwner) || itemDetails.get("commentOwner").equals(userId)) {
                        tvDeleteComment.setVisibility(View.VISIBLE);
                        tvBlockComment.setVisibility(View.VISIBLE);
                        if (itemDetails.get("commentOwner").equals(userId)) {
                            tvReportComment.setVisibility(View.GONE);
                            tvBlockComment.setVisibility(View.GONE);
                        }
                    } else {
                        tvDeleteComment.setVisibility(View.GONE);
                        tvBlockComment.setVisibility(View.GONE);
                    }



                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setView(viewMoreComment);

                    final AlertDialog alert = builder.create();

                    tvReportComment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alert.dismiss();
                            reportSubComment(comment,subCommentId);
                        }
                    });

                    tvBlockComment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alert.dismiss();
                            blockUserComment(itemDetails.get("commentOwner").toString(),true);
                        }
                    });

                    tvDeleteComment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            alert.dismiss();


                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            String positiveBtnText = "";
                            String negativeBtnText = "";

                            builder.setMessage(getString(R.string.dialog_message_delete_comment));
                            positiveBtnText = getString(R.string.dialog_validate);
                            negativeBtnText = getString(R.string.dialog_cancel);

                            builder.setPositiveButton(positiveBtnText, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //do your work here
                                    mPostRef.child(currentPost.first + "/comments/" + comment + "/subComments/" + subCommentId).removeValue(new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                            layoutComment.removeView(viewSubComment);
                                            //modifNote(currentPost.first, -2);
                                        }
                                    });
                                    dialog.dismiss();
                                }
                            });
                            builder.setNegativeButton(negativeBtnText, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            AlertDialog alert = builder.create();
                            try {
                                alert.show();
                                alert.getWindow().getAttributes();
                            } catch (Exception e) {

                            }

                            Button btnNegatif = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                            btnNegatif.setTextColor(context.getResources().getColor(R.color.colorRed));

                            Button btnPositif = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                            btnPositif.setTextColor(context.getResources().getColor(R.color.colorPrimary));


                        }
                    });
                    try {
                        alert.show();
                    } catch (Exception e) {

                    }

                    //return false;
                }
            });
            layoutComment.addView(viewSubComment);
        }
    }

    public void showSubCommentDialog(final String commentId, final String commentOwner, final LinearLayout layoutComment) {

        View viewSubComment = getLayoutInflater().inflate(R.layout.layout_new_sub_comment, null);

        final EditText etSubComment = (EditText) viewSubComment.findViewById(R.id.et_sub_comment);
        ImageView imgSendSubComment = (ImageView) viewSubComment.findViewById(R.id.img_send_sub_comment);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(viewSubComment);

        final AlertDialog alert = builder.create();
        try {
            alert.show();
        } catch (Exception e) {

        }

        imgSendSubComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etSubComment.getText().toString().trim().length() > 0) {

                    addSubComment(etSubComment.getText().toString().trim(), commentId, layoutComment);

                    if (!commentOwner.equals(userId)) {
                        final String dateSent = format.format(new Date());

                        HashMap<String, Object> alert = new HashMap<>();
                        alert.put("createdAt", dateSent);
                        alert.put("postId", currentPost.first);
                        alert.put("type", "reply");
                        alert.put("userId", userId);

                        DatabaseReference mAlertReplyRef = mRootRef.getReference("alerts").child(commentOwner);

                        String alertId = mAlertReplyRef.push().getKey();
                        HashMap<String, Object> alertItem = new HashMap<>();
                        alertItem.put(alertId, alert);

                        mAlertReplyRef.updateChildren(alertItem);

                        DatabaseReference mUserCommentOwnerRef = mRootRef.getReference("users").child(commentOwner);
                        ValueEventListener valueEventListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() != null) {

                                    HashMap<String, Object> membersDetails = (HashMap<String, Object>) dataSnapshot.getValue();
                                    if ((boolean) membersDetails.get("notifsComments") && membersDetails.get("status").equals("offline")) {
                                        List<String> userIds = new ArrayList<>();
                                        if (membersDetails.get("oneSignalUserId") != null) {
                                            userIds.add("'" + membersDetails.get("oneSignalUserId") + "'");
                                        }

                                        String messagePush = "Replied to your comment";
                                        String userIdsList = userIds.toString();
                                        try {
                                            OneSignal.postNotification(new JSONObject("{'contents': {'en':'" + messagePush + "'}, 'ios_sound': 'Notification.mp3', 'data': {'type'='comment','postId':'" + currentPost.first + "','userId':'" + userId + "'}, 'include_player_ids': " + userIdsList + "}"), null);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        };
                        mUserCommentOwnerRef.addListenerForSingleValueEvent(valueEventListener);

                    }

                    alert.dismiss();
                }
            }
        });

    }

    public void addSubComment(String textComment, final String commentId, final LinearLayout llSubComment) {

        Date createdDate = new Date();
        final String createdAt = format.format(createdDate);

        HashMap<String, Object> newComment = new HashMap<>();
        newComment.put("createdAt", createdAt);
        newComment.put("commentText", textComment);
        newComment.put("commentOwner", userId);

        final String key = mPostRef.child(currentPost.first + "/comments").child(commentId + "/subComments").push().getKey();

        HashMap<String, Object> comment = new HashMap<>();
        comment.put(key, newComment);

        mPostRef.child(currentPost.first + "/comments").child(commentId + "/subComments").updateChildren(comment, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                   // modifNote(currentPost.first, 2);
                }
            }
        });

        final View viewSubComment = LayoutInflater.from(this).inflate(R.layout.rv_sub_comment_item, null);
        TextView tvSubCommentText = (TextView) viewSubComment.findViewById(R.id.tv_sub_comment_text);
        TextView tvSubCommentDate = (TextView) viewSubComment.findViewById(R.id.tv_sub_comment_date);
        ImageView ivSubCommentOwner = (ImageView) viewSubComment.findViewById(R.id.iv_sub_comment_owner);
        final TextView tvNbSubCommentLike = (TextView) viewSubComment.findViewById(R.id.tv_nb_sub_like);
        LikeButton lbSubCommentFav = (LikeButton) viewSubComment.findViewById(R.id.lb_sub_comment_fav);

        final int[] nbCommentLike = {0};
        tvNbSubCommentLike.setText(nbCommentLike[0] + "");

        tvSubCommentText.setText(textComment);

        tvSubCommentDate.setText(UtilDateTime.formatTime(this, createdDate));

        if (userId.equals(postOwner)) {
            ivSubCommentOwner.setImageResource(R.drawable.av_admin);
            tvSubCommentText.setTextColor(getResources().getColor(R.color.colorGreen));
        } else {
            int res = UtilUserAvatar.getDrawableRes(this, userId);
            ivSubCommentOwner.setImageResource(res);
        }

        lbSubCommentFav.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                HashMap<String, Object> likes = new HashMap<>();
                likes.put(userId, true);

                mPostRef.child(currentPost.first + "/comments/" + commentId + "/subComments/"+ key + "/likes").updateChildren(likes);
                nbCommentLike[0]++;
                tvNbSubCommentLike.setText(nbCommentLike[0] + "");
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                mPostRef.child(currentPost.first + "/comments/" +commentId + "/subComments/" + key + "/likes/" + userId).removeValue();
                nbCommentLike[0]--;
                tvNbSubCommentLike.setText(nbCommentLike[0] + "");
            }
        });

        viewSubComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final View viewMoreComment = getLayoutInflater().inflate(R.layout.layout_more_comment, null);
                TextView tvDeleteComment = (TextView) viewMoreComment.findViewById(R.id.tv_delete);
                TextView tvReportComment = (TextView) viewMoreComment.findViewById(R.id.tv_report);
                final LinearLayout subComment = (LinearLayout) viewMoreComment.findViewById(R.id.ll_sub_comment);

                subComment.setVisibility(View.GONE);
                tvReportComment.setVisibility(View.GONE);

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setView(viewMoreComment);

                final AlertDialog alert = builder.create();

                tvDeleteComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alert.dismiss();

                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        String positiveBtnText = "";
                        String negativeBtnText = "";

                        builder.setMessage(getString(R.string.dialog_message_delete_comment));
                        positiveBtnText = getString(R.string.dialog_validate);
                        negativeBtnText = getString(R.string.dialog_cancel);

                        builder.setPositiveButton(positiveBtnText, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //do your work here
                                mPostRef.child(currentPost.first + "/comments" + commentId + "/subComments").child(key).removeValue(new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        modifNote(currentPost.first, -2);
                                        llSubComment.removeView(viewMoreComment);
                                    }
                                });
                                dialog.dismiss();
                            }
                        });
                        builder.setNegativeButton(negativeBtnText, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog alert = builder.create();
                        try {
                            alert.show();
                            alert.getWindow().getAttributes();
                        } catch (Exception e) {

                        }

                        Button btnNegatif = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                        btnNegatif.setTextColor(context.getResources().getColor(R.color.colorRed));

                        Button btnPositif = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                        btnPositif.setTextColor(context.getResources().getColor(R.color.colorPrimary));


                    }
                });
                try {
                    alert.show();
                } catch (Exception e) {

                }

                // return false;
            }
        });
        llSubComment.addView(viewSubComment, 0);

        View viewFocus = getCurrentFocus();
        if (viewFocus != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(viewFocus.getWindowToken(), 0);
        }
    }

    public void getMemberDetail() {
        valueEventListenerUser = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataSnapshotUser = dataSnapshot;

                HashMap<String, Object> membersDetails = (HashMap<String, Object>) dataSnapshotUser.getValue();
                boolean enablePublicChat = (membersDetails.get("enablePublicChat") != null) ? (boolean) membersDetails.get("enablePublicChat") : true;
                if (enablePublicChat || isHimFriend) {
                    if (userId.equals(postOwner)) {
                        ivMessage.setVisibility(View.GONE);
                        avloadingIndicatorView.setVisibility(View.GONE);
                    } else {
                        ivMessage.setVisibility(View.VISIBLE);
                        avloadingIndicatorView.setVisibility(View.VISIBLE);
                    }
                } else {
                    ivMessage.setVisibility(View.GONE);
                    avloadingIndicatorView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mUserRef.addValueEventListener(valueEventListenerUser);
    }

    public void getMemberFriends() {
        ValueEventListener valueEvent = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, Object> friends = (HashMap<String, Object>) dataSnapshot.getValue();
                if (friends != null) {
                    for (Map.Entry<String, Object> friend : friends.entrySet()) {
                        if (friend.getKey().equals(userId)) {
                            isHimFriend = true;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mContactRef.addListenerForSingleValueEvent(valueEvent);
    }

    public void modifNote(String postId, final int nbIncrement) {

        mRootRef.getReference("posts").child(postId).runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                HashMap<String, Object> postDetails = (HashMap<String, Object>) mutableData.getValue();
                long note = 0;
                if (postDetails.containsKey("note")) {
                    note = (long) postDetails.get("note");
                }
                note += nbIncrement;

                postDetails.put("note", note);
                mutableData.setValue(postDetails);

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

            }
        });
    }

    public void reportPost() {

        View dialoglayout = getLayoutInflater().inflate(R.layout.layout_report, null);
        final RadioButton rb1 = (RadioButton) dialoglayout.findViewById(R.id.rb1);
        final RadioButton rb2 = (RadioButton) dialoglayout.findViewById(R.id.rb2);
        final RadioButton rb3 = (RadioButton) dialoglayout.findViewById(R.id.rb3);
        final RadioButton rb4 = (RadioButton) dialoglayout.findViewById(R.id.rb4);

        CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    rb1.setChecked(compoundButton == rb1);
                    rb2.setChecked(compoundButton == rb2);
                    rb3.setChecked(compoundButton == rb3);
                    rb4.setChecked(compoundButton == rb4);
                }
            }
        };

        rb1.setOnCheckedChangeListener(onCheckedChangeListener);
        rb2.setOnCheckedChangeListener(onCheckedChangeListener);
        rb3.setOnCheckedChangeListener(onCheckedChangeListener);
        rb4.setOnCheckedChangeListener(onCheckedChangeListener);

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        String positiveBtnText = "";
        String negativeBtnText = "";

        builder.setTitle(getString(R.string.title_abuse));
        builder.setView(dialoglayout);

        positiveBtnText = getString(R.string.dialog_validate);
        negativeBtnText = getString(R.string.dialog_cancel);

        builder.setPositiveButton(positiveBtnText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //do your work here
                if (rb1.isChecked() || rb2.isChecked() || rb3.isChecked() || rb4.isChecked()) {
                    String type = "";
                    if (rb1.isChecked()) {
                        type = "Spam";
                    } else if (rb2.isChecked()) {
                        type = "Hate speech or violence";
                    } else if (rb3.isChecked()) {
                        type = "Nudity or pornography";
                    } else {
                        type = "Piracy";
                    }
                    if (connectionDetector.isConnectingToInternet()) {

                        HashMap<String, Object> report = new HashMap<>();
                        report.put(userId, type);

                        mPostRef.child(currentPost.first + "/reports").updateChildren(report, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError == null) {
                                    Toast.makeText(PostDetails.this, getString(R.string.info_repost_done), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    } else {
                        Toast.makeText(context, getString(R.string.conx_down), Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                } else {
                    Toast.makeText(PostDetails.this, getString(R.string.info_to_choose_report), Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton(negativeBtnText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        android.app.AlertDialog alert = builder.create();
        try {
            alert.show();
            alert.getWindow().getAttributes();
        } catch (Exception e) {

        }
        alert.getWindow().getAttributes();

        Button btnNegatif = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        btnNegatif.setTextColor(context.getResources().getColor(R.color.colorRed));

        Button btnPositif = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        btnPositif.setTextColor(context.getResources().getColor(R.color.colorPrimary));

    }

    public void reportComment(final String commentId) {

        View dialoglayout = getLayoutInflater().inflate(R.layout.layout_report, null);
        final RadioButton rb1 = (RadioButton) dialoglayout.findViewById(R.id.rb1);
        final RadioButton rb2 = (RadioButton) dialoglayout.findViewById(R.id.rb2);
        final RadioButton rb3 = (RadioButton) dialoglayout.findViewById(R.id.rb3);
        final RadioButton rb4 = (RadioButton) dialoglayout.findViewById(R.id.rb4);

        CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    rb1.setChecked(compoundButton == rb1);
                    rb2.setChecked(compoundButton == rb2);
                    rb3.setChecked(compoundButton == rb3);
                    rb4.setChecked(compoundButton == rb4);
                }
            }
        };

        rb1.setOnCheckedChangeListener(onCheckedChangeListener);
        rb2.setOnCheckedChangeListener(onCheckedChangeListener);
        rb3.setOnCheckedChangeListener(onCheckedChangeListener);
        rb4.setOnCheckedChangeListener(onCheckedChangeListener);

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        String positiveBtnText = "";
        String negativeBtnText = "";

        builder.setTitle(getString(R.string.title_abuse));
        builder.setView(dialoglayout);

        positiveBtnText = getString(R.string.dialog_validate);
        negativeBtnText = getString(R.string.dialog_cancel);

        builder.setPositiveButton(positiveBtnText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //do your work here
                if (rb1.isChecked() || rb2.isChecked() || rb3.isChecked() || rb4.isChecked()) {
                    String type = "";
                    if (rb1.isChecked()) {
                        type = "Spam";
                    } else if (rb2.isChecked()) {
                        type = "Hate speech or violence";
                    } else if (rb3.isChecked()) {
                        type = "Nudity or pornography";
                    } else {
                        type = "Piracy";
                    }
                    if (connectionDetector.isConnectingToInternet()) {

                        HashMap<String, Object> report = new HashMap<>();
                        report.put(userId, type);

                        mPostRef.child(currentPost.first + "/comments/" + commentId + "/reports").updateChildren(report, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError == null) {
                                    Toast.makeText(PostDetails.this,getString(R.string.info_repost_done), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    } else {
                        Toast.makeText(context, getString(R.string.conx_down), Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                } else {
                    Toast.makeText(PostDetails.this, getString(R.string.info_to_choose_report), Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton(negativeBtnText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        android.app.AlertDialog alert = builder.create();
        try {
            alert.show();
            alert.getWindow().getAttributes();
        } catch (Exception e) {

        }
        alert.getWindow().getAttributes();

        Button btnNegatif = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        btnNegatif.setTextColor(context.getResources().getColor(R.color.colorRed));

        Button btnPositif = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        btnPositif.setTextColor(context.getResources().getColor(R.color.colorPrimary));

    }

    public void reportSubComment(final String commentId, final String suCommentId) {
        View dialoglayout = getLayoutInflater().inflate(R.layout.layout_report, null);
        final RadioButton rb1 = (RadioButton) dialoglayout.findViewById(R.id.rb1);
        final RadioButton rb2 = (RadioButton) dialoglayout.findViewById(R.id.rb2);
        final RadioButton rb3 = (RadioButton) dialoglayout.findViewById(R.id.rb3);
        final RadioButton rb4 = (RadioButton) dialoglayout.findViewById(R.id.rb4);

        CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    rb1.setChecked(compoundButton == rb1);
                    rb2.setChecked(compoundButton == rb2);
                    rb3.setChecked(compoundButton == rb3);
                    rb4.setChecked(compoundButton == rb4);
                }
            }
        };

        rb1.setOnCheckedChangeListener(onCheckedChangeListener);
        rb2.setOnCheckedChangeListener(onCheckedChangeListener);
        rb3.setOnCheckedChangeListener(onCheckedChangeListener);
        rb4.setOnCheckedChangeListener(onCheckedChangeListener);

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        String positiveBtnText = "";
        String negativeBtnText = "";

        builder.setTitle(""+getString(R.string.title_abuse));
        builder.setView(dialoglayout);

        positiveBtnText = getString(R.string.dialog_validate);
        negativeBtnText = getString(R.string.dialog_cancel);

        builder.setPositiveButton(positiveBtnText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //do your work here
                if (rb1.isChecked() || rb2.isChecked() || rb3.isChecked() || rb4.isChecked()) {
                    String type = "";
                    if (rb1.isChecked()) {
                        type = "Spam";
                    } else if (rb2.isChecked()) {
                        type = "Hate speech or violence";
                    } else if (rb3.isChecked()) {
                        type = "Nudity or pornography";
                    } else {
                        type = "Piracy";
                    }
                    if (connectionDetector.isConnectingToInternet()) {

                        HashMap<String, Object> report = new HashMap<>();
                        report.put(userId, type);

                        mPostRef.child(currentPost.first + "/comments/" + commentId + "subComments"+suCommentId+ "/reports").updateChildren(report, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError == null) {
                                    Toast.makeText(PostDetails.this, getString(R.string.info_repost_done), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    } else {
                        Toast.makeText(context, getString(R.string.conx_down), Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                } else {
                    Toast.makeText(PostDetails.this, getString(R.string.info_to_choose_report), Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton(negativeBtnText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        android.app.AlertDialog alert = builder.create();
        try {
            alert.show();
            alert.getWindow().getAttributes();
        } catch (Exception e) {

        }
        alert.getWindow().getAttributes();

        Button btnNegatif = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        btnNegatif.setTextColor(context.getResources().getColor(R.color.colorRed));

        Button btnPositif = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        btnPositif.setTextColor(context.getResources().getColor(R.color.colorPrimary));

    }

    public void blockUserComment(String userid, Boolean toBlock) {

        if (connectionDetector.isConnectingToInternet()) {

            if(toBlock) {

                HashMap<String, Object> report = new HashMap<>();
                report.put(userid + "", true);

                mPostRef.child(currentPost.first + "/blocks").updateChildren(report, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            Toast.makeText(PostDetails.this, getString(R.string.info_block_done), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }else{

                mPostRef.child(currentPost.first + "/blocks/" + userid).removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            // Toast.makeText(PostDetails.this, "DeBlock is done", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        } else {
            Toast.makeText(context, getString(R.string.conx_down), Toast.LENGTH_SHORT).show();
        }

    }

    public void blockPost() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String positiveBtnText = "";
        String negativeBtnText = "";

        builder.setMessage(getString(R.string.dialog_message_block_post));
        positiveBtnText = getString(R.string.dialog_validate);
        negativeBtnText = getString(R.string.dialog_cancel);

        builder.setPositiveButton(positiveBtnText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //do your work here
                if (connectionDetector.isConnectingToInternet()) {
                    final String dateBlocks = format.format(new Date());

                    HashMap<String, Object> block = new HashMap<>();
                    block.put("postId", currentPost.first);
                    block.put("createdAt", dateBlocks);

                    DatabaseReference mBlocksRef = mRootRef.getReference("blocks").child(userId).child(postOwner);
                    mBlocksRef.updateChildren(block, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                Toast.makeText(context, getString(R.string.info_block_done), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(context, getString(R.string.conx_down), Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(negativeBtnText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        try {
            alert.show();
            alert.getWindow().getAttributes();
        } catch (Exception e) {

        }
        alert.getWindow().getAttributes();

        Button btnNegatif = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        btnNegatif.setTextColor(context.getResources().getColor(R.color.colorRed));

        Button btnPositif = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        btnPositif.setTextColor(context.getResources().getColor(R.color.colorPrimary));

    }

    public void deletePost() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String positiveBtnText = "";
        String negativeBtnText = "";

        builder.setMessage(getString(R.string.dialog_message_to_delete_post));
        positiveBtnText = getString(R.string.dialog_validate);
        negativeBtnText = getString(R.string.dialog_cancel);

        builder.setPositiveButton(positiveBtnText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //do your work here
                if (connectionDetector.isConnectingToInternet()) {
                    mPostRef.child(currentPost.first).removeValue(new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                List<String> listTags = mTextHashTagHelper.getAllHashTags();
                                for (int i = 0; i < listTags.size(); i++) {
                                    mRootRef.getReference("tags").child(listTags.get(i).trim() + "/" + currentPost.first).removeValue();
                                }
                                Toast.makeText(PostDetails.this, getString(R.string.info_delete_is_done), Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    });
                } else {
                    Toast.makeText(context,getString(R.string.conx_down), Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(negativeBtnText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();

        Button btnNegatif = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        btnNegatif.setTextColor(context.getResources().getColor(R.color.colorRed));

        Button btnPositif = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        btnPositif.setTextColor(context.getResources().getColor(R.color.colorPrimary));
    }

    public void reyouzPost() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String positiveBtnText = getString(R.string.dialog_Public);
        String negativeBtnText = getString(R.string.dialog_Friends);
        String neutralBtnText = getString(R.string.dialog_cancel);

        builder.setMessage(context.getString(R.string.dialog_message_share));

        builder.setPositiveButton(positiveBtnText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                createNewPost(true);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(negativeBtnText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                createNewPost(false);
                dialog.dismiss();
            }
        });
        builder.setNeutralButton(neutralBtnText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        try {
            alert.show();
            alert.getWindow().getAttributes();
        } catch (Exception e) {

        }
        alert.getWindow().getAttributes();

        Button btnNegatif = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        btnNegatif.setTextColor(getResources().getColor(R.color.colorPrimary));

        Button btnPositif = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        btnPositif.setTextColor(getResources().getColor(R.color.colorPrimary));

        Button btnNeutral = alert.getButton(DialogInterface.BUTTON_NEUTRAL);
        btnNeutral.setTextColor(getResources().getColor(R.color.colorRed));
    }

    public void createNewPost(boolean isPublic) {

        HashMap<String, Object> reyouzCount = new HashMap<>();
        reyouzCount.put("reyouzCount", ++nbReyouz);
        mPostRef.child(currentPost.first).updateChildren(reyouzCount);

        String key = mPostRef.push().getKey();
        final String createdAt = format.format(new Date());

        HashMap<String, Object> newPost = new HashMap<>();
        newPost.put("createdAt", createdAt);
        newPost.put("public", isPublic);
        newPost.put("title", title);
        newPost.put("postOwner", userId);
        newPost.put("location", MainActivity.locale);
        newPost.put("note", 0);

        if (!color.equals("")) {
            newPost.put("color", color.substring(1));
        }

        if (!font.equals("")) {
            newPost.put("font", font);
        }

        if (fontSize > 0) {
            newPost.put("fontSize", fontSize);
        }

        if (!photo.equals("")) {
            newPost.put("photo", photo);
        }

        if (title.contains("#")) {
            List<String> listHashTags = getHashtagFromText(title);

            HashMap<String, Object> newTag = new HashMap<>();
            newTag.put(key, true);

            for (int i = 0; i < listHashTags.size(); i++) {
                mTagRef.child(listHashTags.get(i)).updateChildren(newTag);
            }
        }

        mPostRef.child(key).updateChildren(newPost, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Toast.makeText(PostDetails.this, getString(R.string.info_share_done), Toast.LENGTH_SHORT).show();

                    if (!postOwner.equals(userId)) {
                        final String dateSent = format.format(new Date());

                        HashMap<String, Object> alert = new HashMap<>();
                        alert.put("createdAt", dateSent);
                        alert.put("postId", currentPost.first);
                        alert.put("type", "share");
                        alert.put("userId", userId);

                        String alertId = mAlertRef.push().getKey();
                        HashMap<String, Object> alertItem = new HashMap<>();
                        alertItem.put(alertId, alert);

                        mAlertRef.updateChildren(alertItem);

                        HashMap<String, Object> membersDetails = (HashMap<String, Object>) dataSnapshotUser.getValue();
                        if ((boolean) membersDetails.get("notifsShares") && membersDetails.get("status").equals("offline")) {
                            List<String> userIds = new ArrayList<>();
                            if (membersDetails.get("oneSignalUserId") != null) {
                                userIds.add("'" + membersDetails.get("oneSignalUserId") + "'");
                            }

                            String messagePush = "Shared your post";
                            String userIdsList = userIds.toString();
                            try {
                                OneSignal.postNotification(new JSONObject("{'contents': {'en':'" + messagePush + "'}, 'ios_sound': 'Notification.mp3', 'data': {'type'='share','postId':'" + currentPost.first + "','userId':'" + userId + "'}, 'include_player_ids': " + userIdsList + "}"), null);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
    }

    public List<String> getHashtagFromText(String text) {
        List<String> listHashtag = new ArrayList<>();

        String[] words = text.split(" ");
        for (final String word : words) {
            if (word.startsWith("#") && word.length() > 1) {
                listHashtag.add(word.substring(1));
            }
        }
        return listHashtag;
    }

    public void searchPrivateChat(final String userId, final String contactId) {
        final String privateChatId;

        if (userId.compareTo(contactId) > 0) {
            privateChatId = currentPost.first + userId + contactId;
        } else {
            privateChatId = currentPost.first + contactId + userId;
        }
        DatabaseReference mChatQuery = mChatRef.child(privateChatId);
        ValueEventListener valueEventListenerChat = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    HashMap<String, Object> details = (HashMap<String, Object>) dataSnapshot.getValue();

                    boolean isBlocked = false;
                    boolean hasBlock = false;
                    if (details.get("blocks") != null) {
                        if (((HashMap<String, Object>) details.get("blocks")).get(userId) != null) {
                            isBlocked = true;
                        }
                        if (((HashMap<String, Object>) details.get("blocks")).get(contactId) != null) {
                            hasBlock = true;
                        }
                    }
                    String deletedAt = (details.get("chatDeletes") != null && ((HashMap<String, Object>) details.get("chatDeletes")).get(userId) != null) ? (String) ((HashMap<String, Object>) details.get("chatDeletes")).get(userId) : "";

                    Intent intent = new Intent(PostDetails.this, ChatMessage.class);
                    intent.putExtra("ChatId", privateChatId);
                    intent.putExtra("PrivateId", contactId);
                    if (details.get("lastMessageDateSent") != null) {
                        intent.putExtra("HasMessage", true);
                    } else {
                        intent.putExtra("HasMessage", false);
                    }
                    intent.putExtra("ChatDeletes", deletedAt);
                    intent.putExtra("IsBlocked", isBlocked);
                    intent.putExtra("HasBlock", hasBlock);

                    startActivity(intent);
                } else {
                    String date_createdAt = format.format(new Date());

                    HashMap<String, Boolean> members = new HashMap<>();
                    members.put(userId, true);
                    members.put(contactId, true);

                    HashMap<String, Integer> unreadMessages = new HashMap<>();
                    unreadMessages.put(userId, 0);
                    unreadMessages.put(contactId, 0);

                    HashMap<String, Boolean> typingIndicator = new HashMap<>();
                    typingIndicator.put(userId, false);
                    typingIndicator.put(contactId, false);

                    HashMap<String, Object> result = new HashMap<>();
                    result.put("chatOwner", userId);
                    result.put("createdAt", date_createdAt);
                    result.put("members", members);
                    result.put("unreadMessages", unreadMessages);

                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("/" + privateChatId, result);

                    mChatRef.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                        }
                    });

                    Map<String, Object> memberChild = new HashMap<>();
                    memberChild.put("/" + privateChatId, "private");

                    DatabaseReference mMemberPostRef = mRootRef.getReference("members/" + contactId);
                    mMemberPostRef.updateChildren(memberChild);

                    mMemberRef.updateChildren(memberChild, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            Intent intent = new Intent(PostDetails.this, ChatMessage.class);
                            intent.putExtra("ChatId", privateChatId);
                            intent.putExtra("PrivateId", contactId);
                            intent.putExtra("HasMessage", false);

                            startActivity(intent);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mChatQuery.addListenerForSingleValueEvent(valueEventListenerChat);
    }
}
