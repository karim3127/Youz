package com.youz.android.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SwipeLayout;
import com.google.firebase.database.FirebaseDatabase;
import com.youz.android.R;
import com.youz.android.activity.ChatMessage;
import com.youz.android.activity.MainActivity;
import com.youz.android.fragment.HomeMessageFragment;
import com.youz.android.util.ConnectionDetector;
import com.youz.android.util.UtilDateTime;
import com.youz.android.util.UtilUserAvatar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by macbook on 12/05/15.
 */
public class HomeMessageItemAdapter extends RecyclerView.Adapter<HomeMessageItemAdapter.MyViewHolder> {

    private final SimpleDateFormat format;
    private final SharedPreferences prefs;
    public List<Pair<String, HashMap<String, Object>>> listItems;
    Context context;
    Typeface typeFaceGras, typeFace;
    ConnectionDetector connectionDetector;
    String userId;
    public List<String> listUnreadDialog = new ArrayList<>();
    FirebaseDatabase mRootRef = FirebaseDatabase.getInstance();

    public HomeMessageItemAdapter(Context context, List<Pair<String, HashMap<String, Object>>> listItems){
        this.context = context;
        this.listItems = listItems;
        typeFaceGras = Typeface.createFromAsset(context.getAssets(), "fonts/optima_bold.ttf");
        typeFace = Typeface.createFromAsset(context.getAssets(), "fonts/Optima-Regular.ttf");
        format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        format.setTimeZone(TimeZone.getDefault());
        prefs = context.getSharedPreferences("com.youz.android", Context.MODE_PRIVATE);
        userId = prefs.getString("UserId", "");
        connectionDetector = new ConnectionDetector(context);

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_dialog_item, parent, false);
        MyViewHolder holder = new MyViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        HashMap<String, Object> dialog = listItems.get(position).second;

        holder.chatId = listItems.get(position).first;

        if (dialog.get("unreadMessages") != null && ((HashMap<String, Object>) dialog.get("unreadMessages")).containsKey(userId) && !(((HashMap<String, Object>) dialog.get("unreadMessages")).get(userId) + "").equals("0")) {
            holder.tvLastMsg.setTypeface(typeFaceGras);
            holder.tvNbUnreadMsg.setVisibility(View.VISIBLE);
            holder.tvNbUnreadMsg.setText(((HashMap<String, Object>) dialog.get("unreadMessages")).get(userId).toString());
            holder.tvLastMsg.setTextColor(context.getResources().getColor(R.color.colorBlack));
            holder.tvDate.setTextColor(context.getResources().getColor(R.color.colorPrimary));

            Date dateDialog = null;
            try {
                dateDialog = format.parse((String) dialog.get("lastMessageDateSent"));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (dateDialog.getTime() > (HomeMessageFragment.lastDateConsult - (1000 * 60)) || true) {
                boolean exist = false;
                for (int i = 0; i < listUnreadDialog.size(); i++) {
                    if (listUnreadDialog.get(i).equals(holder.chatId)) {
                        exist = true;
                    }
                }
                if (!exist) {
                    listUnreadDialog.add(holder.chatId);
                }
            }

        } else {
            holder.tvLastMsg.setTypeface(typeFace);
            holder.tvNbUnreadMsg.setVisibility(View.GONE);
            holder.tvNbUnreadMsg.setText("");
            holder.tvLastMsg.setTextColor(context.getResources().getColor(R.color.colorBlackLight));
            holder.tvDate.setTextColor(context.getResources().getColor(R.color.colorGray));

            for (int i = 0; i < listUnreadDialog.size(); i++) {
                if (listUnreadDialog.get(i).equals(holder.chatId)) {
                    listUnreadDialog.remove(i);
                }
            }
        }

        if (listUnreadDialog.size() > 0) {
            MainActivity.tvBadgeMsg.setVisibility(View.VISIBLE);
            MainActivity.tvBadgeMsg.setText(listUnreadDialog.size() + "");
        } else {
            MainActivity.tvBadgeMsg.setVisibility(View.GONE);
        }

        String memberId = "";
        for (Map.Entry<String, Object> entry : ((HashMap<String, Object>) dialog.get("members")).entrySet()) {
            if (!entry.getKey().equals(userId)) {
                memberId = entry.getKey();
            }
        }

        holder.privateContactId = memberId;

        boolean isBlocked = false;
        boolean hasBlock = false;
        if (dialog.get("blocks") != null) {
            if (((HashMap<String, Object>) dialog.get("blocks")).get(userId) != null) {
                isBlocked = true;
            }
            if (((HashMap<String, Object>) dialog.get("blocks")).get(memberId) != null) {
                hasBlock = true;
            }
        }
        holder.isBlocked = isBlocked;
        holder.hasBlock = hasBlock;

        holder.avatarRes = UtilUserAvatar.getAvatarRes(holder.privateContactId, HomeMessageFragment.hashMapAvatar);
        holder.imgDialog.setImageResource(holder.avatarRes);

        if (dialog.get("lastMessageDateSent") != null && !dialog.get("lastMessageDateSent").equals("")) {

            Date dateDialog = null;
            try {
                dateDialog = format.parse((String) dialog.get("lastMessageDateSent"));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            holder.tvLastMsg.setText(Html.fromHtml((String) dialog.get("lastMessage")));
            holder.tvDate.setText(UtilDateTime.formatTime(context, dateDialog));
        } else {
            Date dateDialog = null;
            try {
                dateDialog = format.parse((String) dialog.get("createdAt"));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            holder.tvLastMsg.setText("New discussion");
            holder.tvDate.setText(UtilDateTime.formatTime(context, dateDialog));
        }
    }

    public void addItem(Pair<String, HashMap<String, Object>> item) {
        listItems.add(item);
        notifyItemInserted(getItemCount());
    }

    public void deleteItem(int position) {
        listItems.remove(position);
        notifyItemRemoved(position);
    }

    public void addItemInRightPosition(Pair<String, HashMap<String, Object>> item) {
        HashMap<String, Object> itemDetails = item.second;
        String itemDate = (String) itemDetails.get("lastMessageDateSent");

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
            String lastDate = (String) dialogCurrent.get("lastMessageDateSent");

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

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView imgDialog;
        TextView tvDate, tvLastMsg, tvNbUnreadMsg;
        LinearLayout llDialog;
        ImageView ivDelete;
        SwipeLayout swipe;
        String chatId;
        String privateContactId;
        boolean isBlocked;
        boolean hasBlock;
        int avatarRes;

        public MyViewHolder(View itemView) {
            super(itemView);

            llDialog = (LinearLayout) itemView.findViewById(R.id.ll_dialog);
            llDialog.setOnClickListener(this);

            swipe = (SwipeLayout) itemView.findViewById(R.id.swipe);
            imgDialog = (ImageView) itemView.findViewById(R.id.img_dialog);
            tvDate = (TextView) itemView.findViewById(R.id.tv_date);
            tvNbUnreadMsg = (TextView) itemView.findViewById(R.id.tv_nb_unread_msg);
            tvLastMsg = (TextView) itemView.findViewById(R.id.tv_last_msg);
            ivDelete = (ImageView) itemView.findViewById(R.id.iv_delete);
            ivDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    String positiveBtnText = "";
                    String negativeBtnText = "";

                    builder.setMessage(""+ context.getString(R.string.dialog_message_delete_dialog));
                    positiveBtnText = context.getResources().getString(R.string.dialog_validate);
                    negativeBtnText = context.getResources().getString(R.string.dialog_cancel);

                    builder.setPositiveButton(positiveBtnText, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //do your work here
                            if (connectionDetector.isConnectingToInternet()) {

                                final String dateSent = format.format(new Date());
                                HashMap<String, Object> chatDeletes = new HashMap<>();
                                chatDeletes.put(userId, dateSent);

                                mRootRef.getReference("chats").child(chatId + "/chatDeletes").updateChildren(chatDeletes);
                                swipe.toggle();
                                Toast.makeText(context, context.getString(R.string.info_delete_dialog)+"", Toast.LENGTH_SHORT).show();
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
            });
        }

        @Override
        public void onClick(View v) {
            HashMap<String, Object> details = listItems.get(getPosition()).second;
            String deletedAt = (details.get("chatDeletes") != null && ((HashMap<String, Object>) details.get("chatDeletes")).get(userId) != null) ? (String) ((HashMap<String, Object>) details.get("chatDeletes")).get(userId) : "";

            Intent intent = new Intent(context, ChatMessage.class);
            intent.putExtra("ChatId", chatId);
            intent.putExtra("PrivateId", privateContactId);
            intent.putExtra("HasMessage", true);
            intent.putExtra("ChatDeletes", deletedAt);
            intent.putExtra("IsBlocked", isBlocked);
            intent.putExtra("HasBlock", hasBlock);

            context.startActivity(intent);
        }
    }
}
