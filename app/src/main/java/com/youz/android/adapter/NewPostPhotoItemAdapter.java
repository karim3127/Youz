package com.youz.android.adapter;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.makeramen.roundedimageview.RoundedImageView;
import com.youz.android.R;
import com.youz.android.activity.NewPost;
import com.youz.android.activity.UpdatePost;

import java.io.File;
import java.util.List;

/**
 * Created by macbook on 12/05/15.
 */
public class NewPostPhotoItemAdapter extends RecyclerView.Adapter<NewPostPhotoItemAdapter.MyViewHolder> {

    public List<String> listItems;
    Context context;
    ImageView imgBack;
    View vBack;

    public NewPostPhotoItemAdapter(Context context, List<String> listItems, ImageView imgBack, View vBack){
        this.context = context;
        this.listItems = listItems;
        this.imgBack = imgBack;
        this.vBack = vBack;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_photo_item, parent, false);
        MyViewHolder holder = new MyViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        if (position == 0) {
            holder.imgPhoto.setImageResource(R.drawable.ic_no_image);
        } else {
            Glide.with(context).load(listItems.get(position))
                    .centerCrop()
                    .into(holder.imgPhoto);
        }
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        RoundedImageView imgPhoto;
        public MyViewHolder(View itemView) {
            super(itemView);

            imgPhoto = (RoundedImageView) itemView.findViewById(R.id.img_photo);
            imgPhoto.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            if (context instanceof NewPost) {
                if (getPosition() == 0) {
                    ((NewPost) context).hasImage = false;
                    ((NewPost) context).hasColor = true;
                    ((NewPost) context).postColor = ((NewPost) context).chosedpostColor;
                    vBack.setBackgroundColor(Color.parseColor("#" + ((NewPost) context).chosedpostColor));
                    if (((NewPost) context).hasColor) {
                        vBack.setAlpha(1);
                    }
                    imgBack.setImageResource(0);
                } else {
                    ((NewPost) context).hasImage = true;
                    ((NewPost) context).hasColor = false;
                    ((NewPost) context).postColor = "";
                    ((NewPost) context).fileUri = Uri.fromFile(new File(listItems.get(getPosition())));
                    vBack.setBackgroundColor(context.getResources().getColor(R.color.colorBlack));
                    vBack.setAlpha(0.5f);
                    Glide.with(context).load(listItems.get(getPosition()))
                            .centerCrop()
                            .into(imgBack);
                }
            } else {
                if (getPosition() == 0) {
                    ((UpdatePost) context).hasImage = false;
                    ((UpdatePost) context).hasColor = true;
                    ((UpdatePost) context).postColor = ((UpdatePost) context).chosedpostColor;
                    vBack.setBackgroundColor(Color.parseColor("#" + ((UpdatePost) context).chosedpostColor));
                    if (((UpdatePost) context).hasColor) {
                        vBack.setAlpha(1);
                    }
                    imgBack.setImageResource(0);
                } else {
                    ((UpdatePost) context).hasImage = true;
                    ((UpdatePost) context).hasColor = false;
                    ((UpdatePost) context).postColor = "";
                    ((UpdatePost) context).fileUri = Uri.fromFile(new File(listItems.get(getPosition())));
                    vBack.setBackgroundColor(context.getResources().getColor(R.color.colorBlack));
                    vBack.setAlpha(0.5f);
                    Glide.with(context).load(listItems.get(getPosition()))
                            .centerCrop()
                            .into(imgBack);
                }
            }
        }
    }
}
