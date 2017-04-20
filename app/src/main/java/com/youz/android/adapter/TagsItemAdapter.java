package com.youz.android.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.util.Pair;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wang.avi.AVLoadingIndicatorView;
import com.youz.android.R;
import com.youz.android.activity.TagPosts;
import com.youz.android.activity.Tags;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

/**
 * Created by macbook on 12/05/15.
 */
public class TagsItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int FOOTER = 0;
    private final SimpleDateFormat format;
    public List<Pair<String, HashMap<String, Object>>> listItems;
    Context context;
    Typeface typeFaceGras;
    Random random = new Random();

    int[] listColor = new int[]{R.color.colorPrimary, R.color.color1, R.color.color2, R.color.color3, R.color.color4, R.color.color5, R.color.color6, R.color.color7,
            R.color.color8, R.color.color9, R.color.color10, R.color.color11, R.color.color12, R.color.color13, R.color.color14, R.color.color15, R.color.color16, R.color.color17, R.color.color18};

    public TagsItemAdapter(Context context, List<Pair<String, HashMap<String, Object>>> listItems){
        this.context = context;
        this.listItems = listItems;

        typeFaceGras = Typeface.createFromAsset(context.getAssets(), "fonts/optima_bold.ttf");
        format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ", Locale.ENGLISH);
        format.setTimeZone(TimeZone.getDefault());
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
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_more_item, parent, false);
            return new FooterViewHolder(v);
        } else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_tags_item, parent, false);
            return new MyViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (listItems.get(position) != null) {
            MyViewHolder myViewHolder = (MyViewHolder) holder;
            Pair<String, HashMap<String, Object>> current = listItems.get(position);
            myViewHolder.tvStatus.setText(current.first);
            myViewHolder.cardView.setCardBackgroundColor(context.getResources().getColor(listColor[random.nextInt(18)]));
        } else {
            FooterViewHolder footerViewHolder = (FooterViewHolder) holder;
            footerViewHolder.tvMore.setText(""+context.getString(R.string.show_more_tags));
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

    public void addItemOnPosition(Pair<String, HashMap<String, Object>> item, int position) {
        listItems.add(position, item);
        notifyItemInserted(position);
    }


    public void addItemInRightPosition(Pair<String, HashMap<String, Object>> item) {
        int i = 0;
        while (i < listItems.size() && listItems.get(i).second.size() > item.second.size()) {
            i++;
        }
        listItems.add(i, item);
        notifyItemInserted(i);
    }

    public void deleteItem(int position) {
        listItems.remove(position);
        notifyItemRemoved(position);
    }

    public void deleteFooter() {
        int pos = getItemCount() - 1;
        listItems.remove(pos);
        notifyItemRemoved(pos);
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvStatus;
        CardView cardView;

        public MyViewHolder(View itemView) {
            super(itemView);

            cardView = (CardView) itemView.findViewById(R.id.card_view);
            cardView.setOnClickListener(this);
            tvStatus = (TextView) itemView.findViewById(R.id.tv_status);
            tvStatus.setTypeface(typeFaceGras);

        }

        @Override
        public void onClick(View v) {
            ArrayList<String> listPostIds = new ArrayList<>();
            HashMap<String, Object> current = listItems.get(getPosition()).second;
            for (Map.Entry<String, Object> postId : current.entrySet()) {
                listPostIds.add(postId.getKey());
            }

            Intent intent = new Intent(context, TagPosts.class);
            intent.putExtra("TagName", listItems.get(getPosition()).first);
            intent.putStringArrayListExtra("PostIds", listPostIds);
            context.startActivity(intent);
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
