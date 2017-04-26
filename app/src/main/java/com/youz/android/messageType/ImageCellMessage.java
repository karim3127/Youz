package com.youz.android.messageType;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.flyco.dialog.listener.OnOperItemClickL;
import com.flyco.dialog.widget.ActionSheetDialog;
import com.google.firebase.database.DatabaseReference;
import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.youz.android.R;
import com.youz.android.activity.ImageViewer;
import com.youz.android.util.UtilUserAvatar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

/**
 * Created by macbook on 27/05/16.
 */
public class ImageCellMessage {

    public static DisplayImageOptions options = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .considerExifParams(true)
            .showImageOnLoading(R.drawable.ic_white)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();

    public static View createMyCellMessage(ViewGroup cellView, LayoutInflater layoutInflater, final HashMap<String, Object> message, final DatabaseReference messageQuery) {
        final Context context = cellView.getContext();
        final View v = layoutInflater.inflate(R.layout.message_me_image_item, cellView, false);

        final RoundedImageView imgMsg = (RoundedImageView) v.findViewById(R.id.imgMsg);
        ImageLoader.getInstance().displayImage((String) message.get("url"), imgMsg, options);
        ImageLoader.getInstance().loadImage((String) message.get("url"), options, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {

            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, final Bitmap loadedImage) {
                imgMsg.setImageBitmap(loadedImage);
                imgMsg.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        ActionSheetDialogNoTitle(context, true, messageQuery);
                        return false;
                    }
                });

            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }
        });
        imgMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageViewer.start((Activity) context, imgMsg, (String) message.get("url"));
            }
        });

        TextView txtTime = (TextView) v.findViewById(R.id.txtTime);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        format.setTimeZone(TimeZone.getDefault());
        Date date = null;
        try {
            date = format.parse((String) message.get("dateSent"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        txtTime.setText(date.getHours() + ":" + ((date.getMinutes() < 10)? "0" : "") + date.getMinutes());

        return v;
    }

    public static View createThemCellMessage(ViewGroup cellView, LayoutInflater layoutInflater, final HashMap<String, Object> message, boolean isLastOwnerMsg, final String userId, final DatabaseReference messageQuery) {
        final Context context = cellView.getContext();
        final View v = layoutInflater.inflate(R.layout.message_them_image_item, cellView, false);

        final RoundedImageView imgMsg = (RoundedImageView) v.findViewById(R.id.imgMsg);
        ImageLoader.getInstance().displayImage((String) message.get("url"), imgMsg, options);
        ImageLoader.getInstance().loadImage((String) message.get("url"), options, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {

            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, final Bitmap loadedImage) {
                imgMsg.setImageBitmap(loadedImage);
                imgMsg.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        ActionSheetDialogNoTitle(context, false, messageQuery);
                        return false;
                    }
                });

            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }
        });
        imgMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageViewer.start((Activity) context, imgMsg, (String) message.get("url"));
            }
        });

        TextView txtTime = (TextView) v.findViewById(R.id.txtTime);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        format.setTimeZone(TimeZone.getDefault());
        Date date = null;
        try {
            date = format.parse((String) message.get("dateSent"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        txtTime.setText(date.getHours() + ":" + ((date.getMinutes() < 10)? "0" : "") + date.getMinutes());

        ImageView ivUser = (ImageView) v.findViewById(R.id.iv_user);
        if (isLastOwnerMsg) {
            ivUser.setVisibility(View.INVISIBLE);
        } else {
            ivUser.setVisibility(View.VISIBLE);
            int res = UtilUserAvatar.getDrawableRes(context, userId);
            ivUser.setImageResource(res);
        }

        return v;
    }

    static void ActionSheetDialogNoTitle(final Context context, boolean isMe, final DatabaseReference messageQuery) {

        final String[] stringMyItems = {""+context.getString(R.string.Delete_image), ""+context.getString(R.string.Download_image)};
        final String[] stringYourItems = {context.getString(R.string.Download_image)};

        final ActionSheetDialog dialog;

        if (isMe) {
            dialog = new ActionSheetDialog(context, stringMyItems, null);
        } else {
            dialog = new ActionSheetDialog(context, stringYourItems, null);
        }
        dialog.isTitleShow(false).show();

        dialog.itemTextColor(context.getResources().getColor(R.color.colorPrimary));
        dialog.itemTextSize(context.getResources().getInteger(R.integer.option_menu_text_size));

        dialog.cancelText("Cancel");
        dialog.cancelText(context.getResources().getColor(R.color.colorRed));
        dialog.cancelTextSize(context.getResources().getInteger(R.integer.option_menu_text_size));

        if (isMe) {
            dialog.setOnOperItemClickL(new OnOperItemClickL() {
                @Override
                public void onOperItemClick(AdapterView<?> parent, View view, int position, long id) {
                    switch (position) {
                        case 0:
                            dialog.dismiss();
                            messageQuery.removeValue();
                            break;

                        case 1:
                            dialog.dismiss();
                            //NotificationUtil.notificationForDLAPK(context, urlMessage, "image_" + new Date().getTime() + ".jpg");
                            break;
                    }
                }
            });
        } else {
            dialog.setOnOperItemClickL(new OnOperItemClickL() {
                @Override
                public void onOperItemClick(AdapterView<?> parent, View view, int position, long id) {
                    switch (position) {
                        case 0:
                            dialog.dismiss();
                            //NotificationUtil.notificationForDLAPK(context, urlMessage, "image_" + new Date().getTime() + ".jpg");
                            break;
                    }
                }
            });
        }
    }

}
