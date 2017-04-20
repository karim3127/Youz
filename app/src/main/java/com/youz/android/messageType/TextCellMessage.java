package com.youz.android.messageType;

import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.flyco.dialog.listener.OnOperItemClickL;
import com.flyco.dialog.widget.ActionSheetDialog;
import com.google.firebase.database.DatabaseReference;
import com.youz.android.R;
import com.youz.android.util.UtilUserAvatar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by macbook on 27/05/16.
 */
public class TextCellMessage {

    public static boolean isLongClick = false;

    public static View createMyCellMessage(ViewGroup cellView, LayoutInflater layoutInflater, final HashMap<String, Object> message, final DatabaseReference messageQuery) {
        final Context context = cellView.getContext();
        final View v = layoutInflater.inflate(R.layout.message_me_text_item, cellView, false);

        TextView txtMsg = (TextView) v.findViewById(R.id.txtMsg);

        String textMessage = (String) message.get("message");
        txtMsg.setText(textMessage);

        TextView txtTime = (TextView) v.findViewById(R.id.txtTime);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ", Locale.ENGLISH);
        format.setTimeZone(TimeZone.getDefault());
        Date date = null;
        try {
            date = format.parse((String) message.get("dateSent"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        txtTime.setText(date.getHours() + ":" + ((date.getMinutes() < 10) ? "0" : "") + date.getMinutes());

        txtMsg.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                isLongClick = true;
                ActionSheetDialogNoTitle(context, true, (String) message.get("message"), messageQuery);
                return false;
            }
        });
        return v;
    }

    public static View createThemCellMessage(ViewGroup cellView, LayoutInflater layoutInflater, final HashMap<String, Object> message, boolean isLastOwnerMsg, final String userId, final DatabaseReference messageQuery) {
        final Context context = cellView.getContext();
        final View v = layoutInflater.inflate(R.layout.message_them_text_item, cellView, false);

        TextView txtMsg = (TextView) v.findViewById(R.id.txtMsg);

        String textMessage = (String) message.get("message");
        txtMsg.setText(textMessage);

        TextView txtTime = (TextView) v.findViewById(R.id.txtTime);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        format.setTimeZone(TimeZone.getDefault());
        Date date = null;
        try {
            date = format.parse((String) message.get("dateSent"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        txtTime.setText(date.getHours() + ":" + ((date.getMinutes() < 10) ? "0" : "") + date.getMinutes());

        ImageView ivUser = (ImageView) v.findViewById(R.id.iv_user);
        if (isLastOwnerMsg) {
            ivUser.setVisibility(View.INVISIBLE);
        } else {
            ivUser.setVisibility(View.VISIBLE);
            int res = UtilUserAvatar.getDrawableRes(context, userId);
            ivUser.setImageResource(res);
        }

        txtMsg.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                isLongClick = true;
                ActionSheetDialogNoTitle(context, false, (String) message.get("message"), messageQuery);
                return false;
            }
        });

        return v;
    }

    static void ActionSheetDialogNoTitle(final Context context, boolean isMe, final String textMessage, final DatabaseReference messageQuery) {

        final String[] stringMyItems = {""+context.getString(R.string.Delete_message), ""+context.getString(R.string.Copy_message)};
        String[] stringYourItems = new String[]{""+context.getString(R.string.Copy_message)};

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

        final ClipboardManager clipboard = (ClipboardManager) context.getSystemService(context.CLIPBOARD_SERVICE);

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
                            clipboard.setText(textMessage);
                            Toast.makeText(context, "Text has been copied", Toast.LENGTH_SHORT).show();
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
                            clipboard.setText(textMessage);
                            Toast.makeText(context, "Text has been copied", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            });
        }
    }

}
