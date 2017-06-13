package com.youz.android.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.rey.material.widget.RadioButton;
import com.wang.avi.AVLoadingIndicatorView;
import com.youz.android.R;
import com.youz.android.activity.MainActivity;
import com.youz.android.activity.PostDetails;
import com.youz.android.activity.PostShare;
import com.youz.android.activity.Tags;
import com.youz.android.activity.UpdatePost;
import com.youz.android.fragment.HomeRecentFriendsFragment;
import com.youz.android.util.BackendlessUtil;
import com.youz.android.util.ConnectionDetector;
import com.youz.android.util.UtilDateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by macbook on 12/05/15.
 */
public class HomeRecentItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int FOOTER = 0;
    SharedPreferences prefs;
    String userId;
    DisplayImageOptions options;
    SimpleDateFormat format;
    TimeZone timeZone;
    public List<Pair<String, HashMap<String, Object>>> listItems;
    Context context;
    Typeface typeFaceGras;
    ConnectionDetector connectionDetector;
    boolean isSmall;
    FirebaseDatabase mRootRef = FirebaseDatabase.getInstance();
    DatabaseReference mPostRef;
    DatabaseReference mAlertRef;
    DatabaseReference mTagRef;

    public HomeRecentItemAdapter(Context context, List<Pair<String, HashMap<String, Object>>> listItems, boolean isSmall){
        if (context != null) {
            this.context = context;
            this.listItems = listItems;
            this.isSmall = isSmall;
            connectionDetector = new ConnectionDetector(context);
            typeFaceGras = Typeface.createFromAsset(context.getAssets(), "fonts/optima_bold.ttf");
            prefs = context.getSharedPreferences("com.youz.android", Context.MODE_PRIVATE);
            userId = prefs.getString("UserId", "");

            timeZone = TimeZone.getDefault();
            format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
            format.setTimeZone(timeZone);

            options = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .considerExifParams(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();

            mPostRef = mRootRef.getReference("posts");
            mTagRef = mRootRef.getReference("tags");
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (listItems.get(position) == null) {
            return FOOTER;
        } else {
            return 1;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        if (viewType == FOOTER) {
            v = LayoutInflater.from(context).inflate(R.layout.rv_more_item, parent, false);
            FooterViewHolder holder = new FooterViewHolder(v);
            return holder;
        } else {
            if (isSmall) {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_recent_small_item, parent, false);
            } else {
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_recent_item, parent, false);
            }
            MyViewHolder holder = new MyViewHolder(v);
            return holder;
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder currentHolder, int position) {
        if (listItems.get(position) != null) {
            MyViewHolder holder = (MyViewHolder) currentHolder;

            HashMap<String, Object> current = listItems.get(position).second;

            holder.postId = listItems.get(position).first;
            holder.postOwner = (String) current.get("postOwner");

            String title = (String) current.get("title");
            String color = (current.get("color") == null) ? "" : (String) current.get("color");
            String photo = (current.get("photo") == null) ? "" : (String) current.get("photo");
            String font = (current.get("font") == null) ? "" : (String) current.get("font");
            long fontSize = (current.get("fontSize") == null) ? -1 : (long) current.get("fontSize");
            int nbLikes = (current.get("likes") == null) ? 0 : ((HashMap<String, Object>) current.get("likes")).size();
            int nbComments = (current.get("comments") == null) ? 0 : ((HashMap<String, Object>) current.get("comments")).size();
            holder.nbReyouz = (current.get("reyouzCount") == null) ? 0 : (long) current.get("reyouzCount");
            String location = (current.get("location") == null) ? "" : (String) current.get("location");
            String city = (current.get("city") != null && !((String) current.get("city")).trim().isEmpty()) ? " - " + ((String) current.get("city")).trim() : "";

            if (current.get("saves") != null && ((HashMap<String, Object>) current.get("saves")).get(userId) != null) {
                holder.isSaved = true;
            } else {
                holder.isSaved = false;
            }

            holder.title = title;
            holder.color = color;
            holder.photo = photo;
            holder.font = font;
            holder.fontSize = fontSize;

            Date dateDialog = null;
            try {
                dateDialog = format.parse((String) current.get("createdAt"));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            holder.tvDate.setText(UtilDateTime.formatTime(context, dateDialog));
            holder.tvStatus.setText(title);
            holder.tvNbFav.setText(nbLikes + "");
            holder.tvNbComment.setText(nbComments + "");
            holder.tvNbReyouz.setText(holder.nbReyouz + "");

            if (!font.equals("")) {
                try {
                    Typeface typefaceStatus = Typeface.createFromAsset(context.getAssets(), "fonts/" + font + ".ttf");
                    holder.tvStatus.setTypeface(typefaceStatus);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (!photo.equals("")) {
                ImageLoader.getInstance().displayImage(photo, holder.imgBack, options);
                holder.vBack.setAlpha(0.5f);
            } else {
                holder.imgBack.setImageBitmap(null);
                holder.vBack.setAlpha(1f);
            }
            if (!color.equals("")) {
                try {
                    holder.vBack.setBackgroundColor(Color.parseColor("#" + color));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                holder.vBack.setBackgroundColor(Color.BLACK);
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

                double sizeFinal = (isSmall) ? (size / 2) : size;
                DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
                long px = Math.round(sizeFinal * (displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
                holder.tvStatus.setTextSize(px);
            }
            if (current.get("likes") != null && ((HashMap<String, Object>) current.get("likes")).get(userId) != null) {
                holder.lbFav.setLiked(true);
            } else {
                holder.lbFav.setLiked(false);
            }

            boolean isPublic = (boolean) current.get("public");
            String postOwner = (String) current.get("postOwner");
            boolean isFriend = HomeRecentFriendsFragment.listYouzContacts.contains(postOwner);
            if (!isSmall) {
                if (isPublic || !isFriend) {
                    if (location.equals("")) {
                        holder.llLocation.setVisibility(View.INVISIBLE);
                    } else {
                        holder.llLocation.setVisibility(View.VISIBLE);
                        holder.tvLocation.setText(location + city);
                        holder.tvLocation.setTypeface(typeFaceGras);
                    }
                } else {
                    holder.llLocation.setVisibility(View.INVISIBLE);
                }
            }
        } else {

        }

    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    public void addItem(Pair<String, HashMap<String, Object>> item) {
        listItems.add(item);
        notifyItemInserted(getItemCount());
    }

    public void addTagPostItemInRightPosition(Pair<String, HashMap<String, Object>> item) {
        HashMap<String, Object> itemDetails = item.second;

        String postOwner = (String) itemDetails.get("postOwner");
        boolean isFriend = HomeRecentFriendsFragment.listYouzContacts.contains(postOwner);
        long itemNote = (itemDetails.get("note") != null) ? (long) itemDetails.get("note") : 0;

        int pos = 0;
        boolean isHere = false;
        while (pos < getItemCount() && !isHere) {

            HashMap<String, Object> dialogCurrent = listItems.get(pos).second;

            boolean isCurrentFriend = HomeRecentFriendsFragment.listYouzContacts.contains(dialogCurrent.get("postOwner"));
            long currentNote = (dialogCurrent.get("note") != null) ? (long) dialogCurrent.get("note") : 0;

            if (isCurrentFriend == isFriend) {
                if (itemNote > currentNote) {
                    isHere = true;
                } else if (itemNote == currentNote) {
                    String itemDate = (String) itemDetails.get("createdAt");

                    Date dateNew = null;
                    try {
                        dateNew = format.parse(itemDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    String lastDate = (String) dialogCurrent.get("createdAt");
                    Date dateDialog = null;
                    try {
                        dateDialog = format.parse(lastDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    if (dateNew.compareTo(dateDialog) > 0) {
                        isHere = true;
                    } else {
                        pos++;
                    }
                } else {
                    pos++;
                }
            } else {
                if (isCurrentFriend) {
                    pos++;
                } else {
                    isHere = true;
                }
            }

        }
        listItems.add(pos, item);
        notifyItemInserted(pos);
    }

    public void addPopularItemInRightPosition(Pair<String, HashMap<String, Object>> item) {
        HashMap<String, Object> itemDetails = item.second;
        long itemNote = (itemDetails.get("note") != null) ? (long) itemDetails.get("note") : 0;
        int pos = 0;
        boolean isHere = false;
        while (pos < getItemCount() && !isHere) {

            HashMap<String, Object> dialogCurrent = listItems.get(pos).second;
            long currentNote = (dialogCurrent.get("note") != null) ? (long) dialogCurrent.get("note") : 0;

            if (itemNote > currentNote) {
                isHere = true;
            } else if (itemNote == currentNote) {
                String itemDate = (String) itemDetails.get("createdAt");

                Date dateNew = null;
                try {
                    dateNew = format.parse(itemDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                String lastDate = (String) dialogCurrent.get("createdAt");
                Date dateDialog = null;
                try {
                    dateDialog = format.parse(lastDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (dateNew.compareTo(dateDialog) > 0) {
                    isHere = true;
                } else {
                    pos++;
                }
            } else {
                pos++;
            }

        }
        listItems.add(pos, item);
        notifyItemInserted(pos);
    }

    public void addNearByItemInRightPosition(Pair<String, HashMap<String, Object>> item) {
        HashMap<String, Object> itemDetails = item.second;

        String itemCity = (itemDetails.get("city") != null) ? ((String) itemDetails.get("city")).toLowerCase() : "";
        String itemDate = (String) itemDetails.get("createdAt");

        Date dateNew = null;
        try {
            dateNew = format.parse(itemDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int pos = 0;
        boolean isHere = false;
        while (pos < getItemCount() && !isHere) {

            HashMap<String, Object> dialogCurrent = listItems.get(pos).second;

            String currentCity = (dialogCurrent.get("city") != null) ? ((String) dialogCurrent.get("city")).toLowerCase() : "";
            String lastDate = (String) dialogCurrent.get("createdAt");


            if (MainActivity.city != null && !MainActivity.city.isEmpty()) {
                if (itemCity.equals(MainActivity.city.toLowerCase()) && currentCity.equals(MainActivity.city.toLowerCase())) {
                    Date dateDialog = null;
                    try {
                        dateDialog = format.parse(lastDate);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    if (dateNew.compareTo(dateDialog) > 0) {
                        isHere = true;
                    } else {
                        pos++;
                    }
                } else {
                    if (currentCity.toLowerCase().equals(MainActivity.city.toLowerCase())) {
                        pos++;
                    } else {
                        isHere = true;
                    }
                }
            } else {
                Date dateDialog = null;
                try {
                    dateDialog = format.parse(lastDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (dateNew.compareTo(dateDialog) > 0) {
                    isHere = true;
                } else {
                    pos++;
                }
            }
        }

        listItems.add(pos, item);
        notifyItemInserted(pos);
    }

    public void addItemInRightPosition(Pair<String, HashMap<String, Object>> item) {
        HashMap<String, Object> itemDetails = item.second;
        String itemDate = (String) itemDetails.get("createdAt");

        Date dateNew = null;
        try {
            dateNew = format.parse(itemDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int pos = 0;
        boolean isHere = false;
        while (pos < getItemCount() && !isHere) {

            HashMap<String, Object> dialogCurrent = listItems.get(pos).second;
            String lastDate = (String) dialogCurrent.get("createdAt");

            Date dateDialog = null;
            try {
                dateDialog = format.parse(lastDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (dateNew.compareTo(dateDialog) > 0) {
                isHere = true;
            } else {
                pos++;
            }

        }

        listItems.add(pos, item);
        notifyItemInserted(pos);
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvDate, tvStatus, tvNbFav, tvNbComment, tvLocation, tvNbReyouz;
        LinearLayout llContainer, llLocation;
        CardView cardView;
        ImageView imgMore, imgBack;
        LikeButton lbFav;
        View vBack;
        long nbReyouz = 0;
        boolean isSaved;
        String postId;
        String postOwner;
        String title;
        String color;
        String font;
        String photo;
        long fontSize;

        public MyViewHolder(View itemView) {
            super(itemView);

            cardView = (CardView) itemView.findViewById(R.id.card_view);
            cardView.setOnClickListener(this);
            llContainer = (LinearLayout) itemView.findViewById(R.id.ll_container);
            llLocation = (LinearLayout) itemView.findViewById(R.id.ll_location);

            tvDate = (TextView) itemView.findViewById(R.id.tv_date);
            tvNbComment = (TextView) itemView.findViewById(R.id.tv_nb_comment);
            tvNbFav = (TextView) itemView.findViewById(R.id.tv_nb_fav);
            tvNbReyouz = (TextView) itemView.findViewById(R.id.tv_nb_reyouz);
            tvStatus = (TextView) itemView.findViewById(R.id.tv_status);
            //tvStatus.setTypeface(typeFaceGras);
            tvLocation = (TextView) itemView.findViewById(R.id.tv_location);

            imgBack = (ImageView) itemView.findViewById(R.id.img_back);
            vBack = itemView.findViewById(R.id.v_back);
            lbFav = (LikeButton) itemView.findViewById(R.id.lb_fav);
            lbFav.setOnLikeListener(new OnLikeListener() {
                @Override
                public void liked(LikeButton likeButton) {
                    if (connectionDetector.isConnectingToInternet()) {
                        HashMap<String, Object> likes = new HashMap<>();
                        likes.put(userId, true);

                        mPostRef.child(postId + "/likes").updateChildren(likes, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError == null) {
                                    modifNote(postId, true);

                                    Uri sound = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.like_sound);
                                    Ringtone ringtone = RingtoneManager.getRingtone(context, sound);
                                    ringtone.play();

                                }
                            }
                        });

                        if (!postOwner.equals(userId)) {
                            mAlertRef = mRootRef.getReference("alerts").child(postOwner);

                            final String dateSent = format.format(new Date());

                            HashMap<String, Object> alert = new HashMap<>();
                            alert.put("createdAt", dateSent);
                            alert.put("postId", postId);
                            alert.put("type", "like");
                            alert.put("userId", userId);

                            HashMap<String, Object> alertItem = new HashMap<>();
                            alertItem.put((userId + postId), alert);

                            mAlertRef.updateChildren(alertItem);

                            mRootRef.getReference("users").child(postOwner).runTransaction(new Transaction.Handler() {
                                @Override
                                public Transaction.Result doTransaction(MutableData mutableData) {
                                    BackendlessUtil.sendLikePush((HashMap<String, Object>) mutableData.getValue(), postId, userId);

                                    return Transaction.success(mutableData);
                                }

                                @Override
                                public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                                }
                            });

                        }
                    } else {
                        Toast.makeText(context, "Connexion is down", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void unLiked(LikeButton likeButton) {
                    if (connectionDetector.isConnectingToInternet()) {
                        mPostRef.child(postId + "/likes/" + userId).removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                modifNote(postId, false);
                            }
                        });

                        if (!postOwner.equals(userId)) {
                            mAlertRef = mRootRef.getReference("alerts").child(postOwner);
                            mAlertRef.child(userId + postId).removeValue();
                        }
                    } else {
                        Toast.makeText(context, "Connexion is down", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            imgMore = (ImageView) itemView.findViewById(R.id.img_more);
            imgMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popup = new PopupMenu(context, imgMore);
                    //Inflating the Popup using xml file

                    if (postOwner.equals(userId)) {
                        popup.getMenuInflater().inflate(R.menu.menu_more_post_owner, popup.getMenu());
                    } else {
                        popup.getMenuInflater().inflate(R.menu.menu_more_post, popup.getMenu());
                    }

                    MenuItem menuItemSave = popup.getMenu().findItem(R.id.action_save);

                    if (isSaved) {
                        menuItemSave.setTitle(context.getResources().getString(R.string.menu_more_post_Delete_Save));
                    } else {
                        menuItemSave.setTitle(context.getResources().getString(R.string.menu_more_post_Save));
                    }

                    popup.show();
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            int id = item.getItemId();

                            if (id == R.id.action_reyouz) {
                                if (connectionDetector.isConnectingToInternet()) {
                                    reyouzPost();
                                } else {
                                    Toast.makeText(context, context.getResources().getString(R.string.conx_down), Toast.LENGTH_SHORT).show();
                                }
                            } else if (id == R.id.action_share) {
                                context.startActivity(new Intent(context, PostShare.class));
                                PostShare.currentPost = listItems.get(getPosition());
                            } else if (id == R.id.action_report) {
                                reportPost();
                            } else if (id == R.id.action_save) {
                                if (connectionDetector.isConnectingToInternet()) {
                                    if (isSaved) {
                                        mPostRef.child(postId + "/saves").child(userId).removeValue(new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                if (databaseError == null) {
                                                    modifNote(postId, false);
                                                    Toast.makeText(context, context.getResources().getString(R.string.info_save_delete), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    } else {
                                        HashMap<String, Object> saves = new HashMap<>();
                                        saves.put(userId, true);

                                        mPostRef.child(postId + "/saves").updateChildren(saves, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                if (databaseError == null) {
                                                    modifNote(postId, true);
                                                    Toast.makeText(context, context.getResources().getString(R.string.info_save_added), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }

                                } else {
                                    Toast.makeText(context, context.getResources().getString(R.string.conx_down), Toast.LENGTH_SHORT).show();
                                }
                            } else if (id == R.id.action_delete) {
                                deletePost();
                            } else if (id == R.id.action_update) {
                                context.startActivity(new Intent(context, UpdatePost.class));
                                UpdatePost.currentPost = listItems.get(getPosition());
                            } else if (id == R.id.action_block) {
                                blockPost();
                            }

                            return false;
                        }
                    });
                }
            });
        }

        @Override
        public void onClick(View v) {
            context.startActivity(new Intent(context, PostDetails.class));
            PostDetails.currentPost = listItems.get(getPosition());
        }

        public void reyouzPost() {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            String positiveBtnText = context.getString(R.string.dialog_Public);
            String negativeBtnText = context.getString(R.string.dialog_Friends);
            String neutralBtnText = context.getString(R.string.dialog_cancel);

            builder.setMessage(context.getString(R.string.dialog_message_share));

            builder.setPositiveButton(positiveBtnText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //do your work here
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
                }});

            AlertDialog alert = builder.create();
            alert.show();
            alert.getWindow().getAttributes();

            Button btnNegatif = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
            btnNegatif.setTextColor(context.getResources().getColor(R.color.colorPrimary));

            Button btnPositif = alert.getButton(DialogInterface.BUTTON_POSITIVE);
            btnPositif.setTextColor(context.getResources().getColor(R.color.colorPrimary));

            Button btnNeutral = alert.getButton(DialogInterface.BUTTON_NEUTRAL);
            btnNeutral.setTextColor(context.getResources().getColor(R.color.colorRed));

        }

        public void reportPost() {
            View dialoglayout = LayoutInflater.from(context).inflate(R.layout.layout_report, null);
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

            builder.setTitle(context.getString(R.string.title_abuse));
            builder.setView(dialoglayout);

            positiveBtnText = context.getResources().getString(R.string.dialog_validate);
            negativeBtnText = context.getResources().getString(R.string.dialog_cancel);

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

                            mPostRef.child(postId + "/reports").updateChildren(report, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    if (databaseError == null) {
                                        Toast.makeText(context,
                                                context.getResources().getString(R.string.info_repost_done), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } else {
                            Toast.makeText(context, context.getResources().getString(R.string.conx_down), Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();

                    } else {
                        Toast.makeText(context, ""+
                                context.getResources().getString(R.string.info_to_choose_report), Toast.LENGTH_SHORT).show();
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
            alert.show();
            alert.getWindow().getAttributes();

            Button btnNegatif = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
            btnNegatif.setTextColor(context.getResources().getColor(R.color.colorRed));

            Button btnPositif = alert.getButton(DialogInterface.BUTTON_POSITIVE);
            btnPositif.setTextColor(context.getResources().getColor(R.color.colorPrimary));

        }

        public void blockPost() {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            String positiveBtnText = "";
            String negativeBtnText = "";

            builder.setMessage(context.getResources().getString(R.string.dialog_message_block_post));
            positiveBtnText = context.getResources().getString(R.string.dialog_validate);
            negativeBtnText = context.getResources().getString(R.string.dialog_cancel);

            builder.setPositiveButton(positiveBtnText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //do your work here
                    if (connectionDetector.isConnectingToInternet()) {
                        final String dateBlocks = format.format(new Date());

                        HashMap<String, Object> block = new HashMap<>();
                        block.put("postId", postId);
                        block.put("createdAt", dateBlocks);

                        DatabaseReference mBlocksRef = mRootRef.getReference("blocks").child(userId).child(postOwner);
                        mBlocksRef.updateChildren(block, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError == null) {
                                    Toast.makeText(context,
                                            context.getResources().getString(R.string.info_block_done), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(context, context.getResources().getString(R.string.conx_down), Toast.LENGTH_SHORT).show();
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

            builder.setMessage(context.getResources().getString(R.string.dialog_message_to_delete_post));
            positiveBtnText = context.getResources().getString(R.string.dialog_validate);
            negativeBtnText = context.getResources().getString(R.string.dialog_cancel);

            builder.setPositiveButton(positiveBtnText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //do your work here
                    if (connectionDetector.isConnectingToInternet()) {
                        mPostRef.child(postId).removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError == null) {
                                    List<String> listTags = getHashtagFromText(title);
                                    for (int i = 0; i < listTags.size(); i++) {
                                        mRootRef.getReference("tags").child(listTags.get(i).trim() + "/" + postId).removeValue();
                                    }
                                    Toast.makeText(context,
                                            context.getResources().getString(R.string.info_delete_is_done), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(context, context.getString(R.string.conx_down), Toast.LENGTH_SHORT).show();
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
            alert.getWindow().getAttributes();

            Button btnNegatif = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
            btnNegatif.setTextColor(context.getResources().getColor(R.color.colorRed));

            Button btnPositif = alert.getButton(DialogInterface.BUTTON_POSITIVE);
            btnPositif.setTextColor(context.getResources().getColor(R.color.colorPrimary));
        }

        public void createNewPost(boolean isPublic) {

            HashMap<String, Object> reyouzCount = new HashMap<>();
            reyouzCount.put("reyouzCount", ++nbReyouz);
            mPostRef.child(postId).updateChildren(reyouzCount);

            String key = mPostRef.push().getKey();
            final String createdAt = format.format(new Date());

            HashMap<String, Object> newPost = new HashMap<>();
            newPost.put("createdAt", createdAt);
            newPost.put("public", isPublic);
            newPost.put("title", title);
            newPost.put("postOwner", userId);
            newPost.put("location", prefs.getString("CountryName", ""));
            newPost.put("city", prefs.getString("CityName", ""));
            newPost.put("note", 0);

            if (!color.equals("")) {
                newPost.put("color", color);
            }

            if (!font.equals("")) {
                newPost.put("font", font);
            }

            if (!photo.equals("")) {
                newPost.put("photo", photo);
            }

            if (fontSize > 0) {
                newPost.put("fontSize", fontSize);
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
                        Toast.makeText(context, "Share is done", Toast.LENGTH_SHORT).show();

                        if (!postOwner.equals(userId)) {
                            mAlertRef = mRootRef.getReference("alerts").child(postOwner);

                            final String dateSent = format.format(new Date());

                            HashMap<String, Object> alert = new HashMap<>();
                            alert.put("createdAt", dateSent);
                            alert.put("postId", postId);
                            alert.put("type", "share");
                            alert.put("userId", userId);

                            String alertId = mAlertRef.push().getKey();
                            HashMap<String, Object> alertItem = new HashMap<>();
                            alertItem.put(alertId, alert);

                            mAlertRef.updateChildren(alertItem);

                            mRootRef.getReference("users").child(postOwner).runTransaction(new Transaction.Handler() {
                                @Override
                                public Transaction.Result doTransaction(MutableData mutableData) {

                                    BackendlessUtil.sendSharePush((HashMap<String, Object>) mutableData.getValue(), postId, userId);
                                    BackendlessUtil.sendNewPostPush(postId, userId, MainActivity.locale);
                                    return Transaction.success(mutableData);
                                }

                                @Override
                                public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                                }
                            });
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

        public void modifNote(String postId, final boolean notePlus) {
            mRootRef.getReference("posts").child(postId).runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    HashMap<String, Object> postDetails = (HashMap<String, Object>) mutableData.getValue();
                    long note = 0;
                    if (postDetails.containsKey("note")) {
                        note = (long) postDetails.get("note");
                    }
                    if (notePlus) {
                        note++;
                    } else {
                        note--;
                    }

                    postDetails.put("note", note);
                    mutableData.setValue(postDetails);

                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                }
            });
        }
    }

    class FooterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvMore;
        AVLoadingIndicatorView avLoadingIndicatorView;

        public FooterViewHolder(View itemView) {
            super(itemView);

            avLoadingIndicatorView = (AVLoadingIndicatorView) itemView.findViewById(R.id.avloadingIndicatorView);
            tvMore = (TextView) itemView.findViewById(R.id.tv_more);
            tvMore.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            avLoadingIndicatorView.setVisibility(View.VISIBLE);
            tvMore.setVisibility(View.INVISIBLE);

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    avLoadingIndicatorView.setVisibility(View.GONE);
                    tvMore.setVisibility(View.VISIBLE);
                    ((Tags) context).showTags(getItemCount() - 1, getItemCount() + 9);
                }
            }, 1000);
        }
    }
}
