package com.youz.android.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.youz.android.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Random;

public class UtilUserAvatar {
    static int[] avatarArray = new int[]
            {R.drawable.avatar_0, R.drawable.avatar_1, R.drawable.avatar_2, R.drawable.avatar_3, R.drawable.avatar_4, R.drawable.avatar_5, R.drawable.avatar_6, R.drawable.avatar_7, R.drawable.avatar_8, R.drawable.avatar_9,
            R.drawable.avatar_10, R.drawable.avatar_11, R.drawable.avatar_12, R.drawable.avatar_13, R.drawable.avatar_14, R.drawable.avatar_15, R.drawable.avatar_16, R.drawable.avatar_17, R.drawable.avatar_18, R.drawable.avatar_19,
            R.drawable.avatar_20, R.drawable.avatar_21, R.drawable.avatar_22, R.drawable.avatar_23, R.drawable.avatar_24, R.drawable.avatar_25, R.drawable.avatar_26, R.drawable.avatar_27, R.drawable.avatar_28, R.drawable.avatar_29,
            R.drawable.avatar_30, R.drawable.avatar_31, R.drawable.avatar_32, R.drawable.avatar_33, R.drawable.avatar_34, R.drawable.avatar_35, R.drawable.avatar_36, R.drawable.avatar_37, R.drawable.avatar_38, R.drawable.avatar_39,
            R.drawable.avatar_40, R.drawable.avatar_41, R.drawable.avatar_42, R.drawable.avatar_43, R.drawable.avatar_44, R.drawable.avatar_45, R.drawable.avatar_46, R.drawable.avatar_47, R.drawable.avatar_48, R.drawable.avatar_49,
            R.drawable.avatar_50, R.drawable.avatar_51, R.drawable.avatar_52, R.drawable.avatar_53, R.drawable.avatar_54, R.drawable.avatar_55, R.drawable.avatar_56, R.drawable.avatar_57, R.drawable.avatar_58, R.drawable.avatar_59,
            R.drawable.avatar_60, R.drawable.avatar_61, R.drawable.avatar_62, R.drawable.avatar_63, R.drawable.avatar_64, R.drawable.avatar_65, R.drawable.avatar_66, R.drawable.avatar_67, R.drawable.avatar_68, R.drawable.avatar_69,
            R.drawable.avatar_70, R.drawable.avatar_71, R.drawable.avatar_72, R.drawable.avatar_73, R.drawable.avatar_74, R.drawable.avatar_75, R.drawable.avatar_76, R.drawable.avatar_77, R.drawable.avatar_78, R.drawable.avatar_79,
            R.drawable.avatar_80, R.drawable.avatar_81, R.drawable.avatar_82, R.drawable.avatar_83, R.drawable.avatar_84, R.drawable.avatar_85, R.drawable.avatar_86, R.drawable.avatar_87, R.drawable.avatar_88, R.drawable.avatar_89,
            R.drawable.avatar_90, R.drawable.avatar_91, R.drawable.avatar_92, R.drawable.avatar_93, R.drawable.avatar_94, R.drawable.avatar_95, R.drawable.avatar_96, R.drawable.avatar_97, R.drawable.avatar_98, R.drawable.avatar_99};

    public static int getDrawableRes(Context context, String userID) {
        SharedPreferences prefs = context.getSharedPreferences("com.youz.android", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        /*HashMap<String, Integer> hashMapAvatar;
        String json = prefs.getString("UserAvatar", null);
        if (json != null) {
            hashMapAvatar = convertToHashMap(prefs.getString("UserAvatar", ""));
        } else {
            hashMapAvatar = new HashMap<>();
        }*/

        if (prefs.getInt("Secret" + userID, -1) != -1) {
            return prefs.getInt("Secret" + userID, -1);
        } else {
            Random random = new Random();
            int index = random.nextInt(avatarArray.length);
            int res = avatarArray[index];
            editor.putInt("Secret" + userID, res);
            editor.commit();

            return res;
        }
    }

    static public HashMap<String, Integer> convertToHashMap(String jsonString) {
        HashMap<String, Integer> myHashMap = new HashMap<String, Integer>();
        try {
            JSONArray jArray = new JSONArray(jsonString);
            JSONObject jObject = null;
            String keyString=null;
            for (int i = 0; i < jArray.length(); i++) {
                jObject = jArray.getJSONObject(i);

                // beacuse you have only one key-value pair in each object so I have used index 0
                keyString = (String)jObject.names().get(0);
                myHashMap.put(keyString, jObject.getInt(keyString));
            }
        } catch (JSONException e) {
            JSONObject jObject = null;
            try {
                jObject = new JSONObject(jsonString);

                // beacuse you have only one key-value pair in each object so I have used index 0
                String keyString = (String)jObject.names().get(0);
                myHashMap.put(keyString, jObject.getInt(keyString));
            } catch (JSONException e1) {
                e1.printStackTrace();

            }
        }
        return myHashMap;
    }

}