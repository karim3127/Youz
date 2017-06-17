package com.youz.android.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.youz.android.util.ConnectionDetector;
import com.youz.android.util.UtilDateTime;
import com.youz.android.util.UtilUserAvatar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by macbook on 12/05/15.
 */
public class BlocksListItemAdapter extends RecyclerView.Adapter<BlocksListItemAdapter.MyViewHolder> {

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
    Query mPostQuery;
    HashMap<String, Integer> hashMapAvatar = new HashMap<>();

    public BlocksListItemAdapter(Context context, List<Pair<String, HashMap<String, Object>>> listItems){
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_block_item, parent, false);
        MyViewHolder holder = new MyViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        HashMap<String, Object> current = listItems.get(position).second;

        holder.blockUserId = listItems.get(position).first;

        Date createdAt = null;
        try {
            createdAt = format.parse((String) current.get("createdAt"));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.tvDate.setText(UtilDateTime.formatTime(context, createdAt));

        int res = UtilUserAvatar.getAvatarRes(holder.blockUserId, hashMapAvatar);

        holder.imgBlock.setImageResource(res);

        mPostQuery = mPostRef.child((String) current.get("postId"));
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    HashMap<String, Object> post = (HashMap<String, Object>) dataSnapshot.getValue();
                    String title = (String) post.get("title");
                    String color = (post.get("color") == null) ? "" : (String) post.get("color");
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
                            holder.vBack.setBackgroundColor(Color.parseColor("#" + color));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mPostQuery.addListenerForSingleValueEvent(valueEventListener);

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

        ImageView imgBlock;
        TextView tvDate;
        LinearLayout llBlock;

        TextView tvStatus;
        ImageView imgBack;
        View vBack;

        String blockUserId;

        public MyViewHolder(View itemView) {
            super(itemView);

            llBlock = (LinearLayout) itemView.findViewById(R.id.ll_block);
            imgBlock = (ImageView) itemView.findViewById(R.id.img_block);
            tvDate = (TextView) itemView.findViewById(R.id.tv_date);
            tvStatus = (TextView) itemView.findViewById(R.id.tv_status);
            imgBack = (ImageView) itemView.findViewById(R.id.img_back);
            vBack = itemView.findViewById(R.id.v_back);

            llBlock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deblockPost();
                }
            });
        }

        public void deblockPost() {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            String positiveBtnText = "";
            String negativeBtnText = "";

            builder.setMessage(""+context.getString(R.string.dialog_message_un_block));
            positiveBtnText = context.getResources().getString(R.string.dialog_validate);
            negativeBtnText = context.getResources().getString(R.string.dialog_cancel);

            builder.setPositiveButton(positiveBtnText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //do your work here
                    if (connectionDetector.isConnectingToInternet()) {

                        DatabaseReference mBlocksRef = mRootRef.getReference("blocks").child(userId).child(blockUserId);
                        mBlocksRef.removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError == null) {
                                    Toast.makeText(context,
                                            context.getString(R.string.info_un_block)+"", Toast.LENGTH_SHORT).show();
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

    }
}
