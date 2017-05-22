package com.youz.android.util;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by macbook on 17/05/2017.
 */

public class OneSignalUtil {

    public static void sendLikePush(HashMap<String, Object> membersDetails, String postId, String userId) {

        if ((boolean) membersDetails.get("notifsLikes") && membersDetails.get("status").equals("offline")) {
            List<String> userIds = new ArrayList<>();
            if (membersDetails.get("oneSignalUserId") != null) {
                userIds.add("'" + membersDetails.get("oneSignalUserId") + "'");
            }

            String messagePush = "Liked your post";
            String userIdsList = userIds.toString();
            try {
                OneSignal.postNotification(new JSONObject("{'contents': {'en':'" + messagePush + "'}, 'ios_sound': 'Notification.mp3', 'data': {'type'='like','postId':'" + postId + "','userId':'" + userId + "'}, 'include_player_ids': " + userIdsList + "}"), null);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static void sendCommentPush(HashMap<String, Object> membersDetails, String postId, String userId) {

        if ((boolean) membersDetails.get("notifsComments") && membersDetails.get("status").equals("offline")) {
            List<String> userIds = new ArrayList<>();
            if (membersDetails.get("oneSignalUserId") != null) {
                userIds.add("'" + membersDetails.get("oneSignalUserId") + "'");
            }

            String messagePush = "Commented your post";
            String userIdsList = userIds.toString();
            try {
                OneSignal.postNotification(new JSONObject("{'contents': {'en':'" + messagePush + "'}, 'ios_sound': 'Notification.mp3', 'data': {'type'='comment','postId':'" + postId + "','userId':'" + userId + "'}, 'include_player_ids': " + userIdsList + "}"), null);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static void sendReplyPush(HashMap<String, Object> membersDetails, String postId, String userId) {

        if ((boolean) membersDetails.get("notifsComments") && membersDetails.get("status").equals("offline")) {
            List<String> userIds = new ArrayList<>();
            if (membersDetails.get("oneSignalUserId") != null) {
                userIds.add("'" + membersDetails.get("oneSignalUserId") + "'");
            }

            String messagePush = "Replied to your comment";
            String userIdsList = userIds.toString();
            try {
                OneSignal.postNotification(new JSONObject("{'contents': {'en':'" + messagePush + "'}, 'ios_sound': 'Notification.mp3', 'data': {'type'='comment','postId':'" + postId + "','userId':'" + userId + "'}, 'include_player_ids': " + userIdsList + "}"), null);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static void sendSharePush(HashMap<String, Object> membersDetails, String postId, String userId) {

        if ((boolean) membersDetails.get("notifsShares") && membersDetails.get("status").equals("offline")) {
            List<String> userIds = new ArrayList<>();
            if (membersDetails.get("oneSignalUserId") != null) {
                userIds.add("'" + membersDetails.get("oneSignalUserId") + "'");
            }

            String messagePush = "Shared your post";
            String userIdsList = userIds.toString();
            try {
                OneSignal.postNotification(new JSONObject("{'contents': {'en':'" + messagePush + "'}, 'ios_sound': 'Notification.mp3', 'data': {'type'='share','postId':'" + postId + "','userId':'" + userId + "'}, 'include_player_ids': " + userIdsList + "}"), null);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static void sendNewMessagePush(HashMap<String, Object> membersDetails, String chatId, String userId) {

        if ((boolean) membersDetails.get("notifsChats") && membersDetails.get("status").equals("offline")) {
            List<String> userIds = new ArrayList<>();
            if (membersDetails.get("oneSignalUserId") != null) {
                userIds.add("'" + membersDetails.get("oneSignalUserId") + "'");
            }

            String messagePush = "You have new chat message";
            String userIdsList = userIds.toString();
            try {
                OneSignal.postNotification(new JSONObject("{'contents': {'en':'" + messagePush + "'}, 'ios_sound': 'Notification.mp3', 'data': {'type'='chat','chatId':'" + chatId + "','userId':'" + userId + "'}, 'include_player_ids': " + userIdsList + "}"), null);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static void sendNewPostPush(final String postId, final String userId, String currentLocationCode) {
        final FirebaseDatabase mRootRef = FirebaseDatabase.getInstance();
        Query mContactRef = mRootRef.getReference("contacts").orderByChild(userId).equalTo(true);
        Query mNearUserRef = mRootRef.getReference("users").orderByChild("locationCode").equalTo(currentLocationCode);

        final List<String> listUserId = new ArrayList<>();
        final List<String> listUserOneSignalIds = new ArrayList<>();

        ValueEventListener valueEventListenerContact = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, Object> contacts = (HashMap<String, Object>) dataSnapshot.getValue();

                final int contactCount = contacts.size();
                final int[] counter = {0};
                for (Map.Entry<String, Object> contact : contacts.entrySet()) {
                    listUserId.add(contact.getKey());

                    DatabaseReference mFriendUserRef = mRootRef.getReference("users").child(contact.getKey());
                    ValueEventListener valueEventListenerFriendUser = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            HashMap<String, Object> userDetails = (HashMap<String, Object>) dataSnapshot.getValue();
                            if (userDetails != null && (boolean) userDetails.get("notifsPosts") && userDetails.get("status").equals("offline")) {
                                if (userDetails.get("oneSignalUserId") != null) {
                                    if (!listUserOneSignalIds.contains((String) userDetails.get("oneSignalUserId"))) {
                                        listUserOneSignalIds.add("'" + userDetails.get("oneSignalUserId") + "'");
                                    }
                                }
                            }
                            counter[0]++;
                            if (counter[0] == contactCount) {

                                String messagePush = "Youz friend had publish new post";
                                String userIdsList = listUserOneSignalIds.toString();
                                try {
                                    OneSignal.postNotification(new JSONObject("{'contents': {'en':'" + messagePush + "'}, 'ios_sound': 'Notification.mp3', 'android_sound': 'Notification.mp3', 'data': {'type'='post','postId':'" + postId + "','userId':'" + userId + "'}, 'include_player_ids': " + userIdsList + "}"), null);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    };
                    mFriendUserRef.addListenerForSingleValueEvent(valueEventListenerFriendUser);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mContactRef.addListenerForSingleValueEvent(valueEventListenerContact);

        ValueEventListener valueEventListenerNearUser = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, Object> users = (HashMap<String, Object>) dataSnapshot.getValue();

                for (Map.Entry<String, Object> user : users.entrySet()) {
                    HashMap<String, Object> userDetails = (HashMap<String, Object>) user.getValue();
                    if (userDetails != null && (boolean) userDetails.get("notifsPosts") && userDetails.get("status").equals("offline")) {
                        if (userDetails.get("oneSignalUserId") != null) {
                            if (!listUserOneSignalIds.contains((String) userDetails.get("oneSignalUserId"))) {
                                listUserOneSignalIds.add("'" + userDetails.get("oneSignalUserId") + "'");
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mNearUserRef.addListenerForSingleValueEvent(valueEventListenerNearUser);
    }
}
