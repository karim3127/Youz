package com.youz.android.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.youz.android.R;
import com.youz.android.activity.MainActivity;
import com.youz.android.activity.PostDetails;
import com.youz.android.fragment.HomeNotifFragment;
import com.youz.android.util.ConnectionDetector;
import com.youz.android.util.UtilDateTime;
import com.youz.android.util.UtilUserAvatar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by macbook on 12/05/15.
 */
public class HomeNotifItemAdapter extends RecyclerView.Adapter<HomeNotifItemAdapter.MyViewHolder> {

    SharedPreferences prefs;
    String userId;
    SimpleDateFormat format;
    TimeZone timeZone;
    DisplayImageOptions options;
    public List<Pair<String, HashMap<String, Object>>> listItems;
    Context context;
    Typeface typeFaceGras;
    ConnectionDetector connectionDetector;
    FirebaseDatabase mRootRef = FirebaseDatabase.getInstance();
    DatabaseReference mPostRef = mRootRef.getReference("posts");
    DatabaseReference mAlertRef = mRootRef.getReference("alerts");
    Query mPostQuery;
    public List<String> listUnreadDialog = new ArrayList<>();

    public HomeNotifItemAdapter(Context context, List<Pair<String, HashMap<String, Object>>> listItems){
        this.context = context;
        this.listItems = listItems;
        connectionDetector = new ConnectionDetector(context);
        typeFaceGras = Typeface.createFromAsset(context.getAssets(), "fonts/optima_bold.ttf");

        timeZone = TimeZone.getDefault();
        format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        format.setTimeZone(timeZone);

        prefs = context.getSharedPreferences("com.youz.android", Context.MODE_PRIVATE);
        userId = prefs.getString("UserId", "");

        options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_notif_item, parent, false);
        MyViewHolder holder = new MyViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        HashMap<String, Object> current = listItems.get(position).second;

        holder.alertId = listItems.get(position).first;

        String type = (String) current.get("type");
        if (type.equals("like")) {
            holder.tvTitle.setText(""+context.getString(R.string.notif_like));
            holder.ivLike.setVisibility(View.VISIBLE);
        } else if (type.equals("comment")) {
            holder.tvTitle.setText(""+context.getString(R.string.notif_comment));
            holder.ivLike.setVisibility(View.GONE);
        } else  if (type.equals("reply")) {
            holder.tvTitle.setText(""+context.getString(R.string.notif_reply));
            holder.ivLike.setVisibility(View.GONE);
        } else if (type.equals("share")) {
            holder.tvTitle.setText(""+context.getString(R.string.notif_share));
            holder.ivLike.setVisibility(View.GONE);
        }

        Date createdAt = null;
        try {
            createdAt = format.parse((String) current.get("createdAt"));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.tvDate.setText(UtilDateTime.formatTime(context, createdAt));

        if (createdAt.getTime() > (HomeNotifFragment.lastDateConsult)) {
            boolean exist = false;
            for (int i = 0; i < listUnreadDialog.size(); i++) {
                if (listUnreadDialog.get(i).equals(listItems.get(position).first)) {
                    exist = true;
                }
            }
            if (!exist) {
                listUnreadDialog.add(listItems.get(position).first);
            }
        }

        int res = UtilUserAvatar.getDrawableRes(context, (String) current.get("userId"));
        holder.imgNotif.setImageResource(res);

        mPostQuery = mPostRef.child((String) current.get("postId"));
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    HashMap<String, Object> post = (HashMap<String, Object>) dataSnapshot.getValue();
                    String title = (String) post.get("title");
                    String color = (post.get("color") == null) ? "" : "#" + post.get("color");
                    String photo = (post.get("photo") == null) ? "" : (String) post.get("photo");

                    holder.tvStatus.setText(title);
                    if (!photo.equals("")) {
                        ImageLoader.getInstance().displayImage(photo, holder.imgBack, options);
                        holder.vBack.setAlpha(0.5f);
                    } else {
                        holder.vBack.setAlpha(1f);
                    }

                    if (!color.equals("")) {
                        try {
                            holder.vBack.setBackgroundColor(Color.parseColor(color));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    holder.llNotif.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            for (int i = 0; i < listUnreadDialog.size(); i++) {
                                if (listUnreadDialog.get(i).equals(listItems.get(position).first)) {
                                    listUnreadDialog.remove(i);
                                }
                            }
                            if (listUnreadDialog.size() > 0) {
                                MainActivity.tvBadgeNotif.setVisibility(View.VISIBLE);
                                MainActivity.tvBadgeNotif.setText(listUnreadDialog.size() + "");
                            } else {
                                MainActivity.tvBadgeNotif.setVisibility(View.GONE);
                            }

                            context.startActivity(new Intent(context, PostDetails.class));
                            Pair<String, HashMap<String, Object>> item = new Pair<>(dataSnapshot.getKey(), (HashMap<String, Object>) dataSnapshot.getValue());
                            PostDetails.currentPost = item;
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mPostQuery.addListenerForSingleValueEvent(valueEventListener);


        if (listUnreadDialog.size() > 0) {
            MainActivity.tvBadgeNotif.setVisibility(View.VISIBLE);
            MainActivity.tvBadgeNotif.setText(listUnreadDialog.size() + "");
        } else {
            MainActivity.tvBadgeNotif.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return listItems.size();
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

    class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView imgNotif, ivLike, ivDelete;
        TextView tvDate, tvTitle;
        LinearLayout llNotif;

        TextView tvStatus;
        ImageView imgBack;
        View vBack;

        String alertId;


        public MyViewHolder(View itemView) {
            super(itemView);

            llNotif = (LinearLayout) itemView.findViewById(R.id.ll_notif);

            ivDelete = (ImageView) itemView.findViewById(R.id.iv_delete);
            ivLike = (ImageView) itemView.findViewById(R.id.iv_like);
            imgNotif = (ImageView) itemView.findViewById(R.id.img_notif);
            tvTitle = (TextView) itemView.findViewById(R.id.tv_title);
            tvDate = (TextView) itemView.findViewById(R.id.tv_date);

            tvStatus = (TextView) itemView.findViewById(R.id.tv_status);
            imgBack = (ImageView) itemView.findViewById(R.id.img_back);
            vBack = itemView.findViewById(R.id.v_back);

            ivDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (connectionDetector.isConnectingToInternet()) {
                        mAlertRef.child(userId).child(alertId).removeValue();
                    } else {
                        Toast.makeText(context,context.getString(R.string.conx_down), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }
}
