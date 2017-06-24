package com.youz.android.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.youz.android.R;
import com.youz.android.messageType.ImageCellMessage;
import com.youz.android.messageType.TextCellMessage;
import com.youz.android.util.UtilDateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by macbook on 12/05/15.
 */
public class MessageItemAdapter extends RecyclerView.Adapter<MessageItemAdapter.MessageHolder> {

    public List<Pair<String, HashMap<String, Object>>> listItems;
    private LayoutInflater inflater;
    Context context;
    ViewGroup parent;
    SharedPreferences prefs;
    SimpleDateFormat format;
    String userId;
    String chatId;

    FirebaseDatabase mRootRef = FirebaseDatabase.getInstance();
    private final DatabaseReference mMessageRef;

    public MessageItemAdapter(Context context, List<Pair<String, HashMap<String, Object>>> listItems, String chatId) {
        this.listItems = listItems;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.chatId = chatId;

        prefs = context.getSharedPreferences("com.youz.android", Context.MODE_PRIVATE);
        userId = prefs.getString("UserId", "");

        format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        format.setTimeZone(TimeZone.getDefault());

        mMessageRef = mRootRef.getReference("messages").child(chatId);

    }

    @Override
    public MessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = inflater.inflate(R.layout.rv_message_item, parent, false);
        this.parent = parent;
        MessageHolder myHolder = new MessageHolder(v);
        return myHolder;
    }

    @Override
    public void onBindViewHolder(final MessageHolder holder, final int position) {
        final HashMap<String, Object> messageDetails = listItems.get(position).second;
        View messageView;

        final DatabaseReference likesQuery = mMessageRef.child(listItems.get(position).first);

        Date date = null;
        try {
            date = format.parse((String) messageDetails.get("dateSent"));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String msgDay = UtilDateTime.formatTimeDay(context, date);
        if (position == 0) {
            holder.rlDate.setVisibility(View.VISIBLE);
            holder.txtDay.setText(msgDay);
        } else {
            Date lastDate = null;
            try {
                lastDate = format.parse((String) listItems.get(position - 1).second.get("dateSent"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String lastDateDay = UtilDateTime.formatTimeDay(context, lastDate);

            if (lastDateDay.equals(msgDay)) {
                holder.rlDate.setVisibility(View.GONE);
            } else {
                holder.rlDate.setVisibility(View.VISIBLE);
                holder.txtDay.setText(msgDay);
            }
        }

        if (messageDetails.get("senderId").equals(userId)) {
            if (messageDetails.get("type").equals("text")) {
                messageView = TextCellMessage.createMyCellMessage(parent, LayoutInflater.from(context), messageDetails, likesQuery);
                holder.chatItemContainer.addView(messageView);

            } else if (messageDetails.get("type").equals("image")) {
                messageView = ImageCellMessage.createMyCellMessage(parent, LayoutInflater.from(context), messageDetails, likesQuery);
                holder.chatItemContainer.addView(messageView);

            }
        } else {
            boolean isLastOwnerMsg = false;
            if (position > 0) {
                String idLastOwnerMsg = listItems.get(position - 1).second.get("senderId").toString();
                if (idLastOwnerMsg.equals(messageDetails.get("senderId").toString())) {
                    isLastOwnerMsg = true;
                }
            }

            if (messageDetails.get("type").equals("text")) {
                messageView = TextCellMessage.createThemCellMessage(parent, LayoutInflater.from(context), messageDetails, isLastOwnerMsg, chatId, likesQuery);
                holder.chatItemContainer.addView(messageView);

            } else if (messageDetails.get("type").equals("image")) {
                messageView = ImageCellMessage.createThemCellMessage(parent, LayoutInflater.from(context), messageDetails, isLastOwnerMsg, chatId, likesQuery);
                holder.chatItemContainer.addView(messageView);

            }
        }

    }

    public void addOnBottom(Pair<String, HashMap<String, Object>> message) {
        int pos = getItemCount();
        listItems.add(pos, message);
        notifyItemInserted(pos);
    }

    public void addOnTop(Pair<String, HashMap<String, Object>> message) {
        listItems.add(0, message);
        notifyItemInserted(0);
    }

    public void addOnPosition(Pair<String, HashMap<String, Object>> message, int position) {
        listItems.add(position, message);
        notifyItemInserted(position);
    }

    public void delete(int position) {
        listItems.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    public class MessageHolder extends RecyclerView.ViewHolder {

        public LinearLayout chatItemContainer;
        public RelativeLayout rlDate;
        TextView txtDay;

        public MessageHolder(View itemView) {
            super(itemView);

            chatItemContainer = (LinearLayout) itemView.findViewById(R.id.msg_item_container);
            rlDate = (RelativeLayout) itemView.findViewById(R.id.rlDate);
            txtDay = (TextView) itemView.findViewById(R.id.txtDay);

        }

    }
}
