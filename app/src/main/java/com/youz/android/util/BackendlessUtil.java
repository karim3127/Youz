package com.youz.android.util;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.messaging.DeliveryOptions;
import com.backendless.messaging.MessageStatus;
import com.backendless.messaging.PublishOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BackendlessUtil {

    public static final String APPLICATION_ID = "CCBAB090-04DC-5B8E-FFBA-DB104641EB00";
    public static final String API_KEY = "940577FC-7024-48BC-FFDB-9BBCFBF7A600";
    public static final String SERVER_URL = "http://localhost:8080/api";

    public static void sendLikePush(HashMap<String, Object> membersDetails, String postId, String userId) {

        if ((boolean) membersDetails.get("notifsLikes") && membersDetails.get("status").equals("offline")) {

            String messagePush = "Someone liked your post";
            List<String> userIds = new ArrayList<>();
            if (membersDetails.get("BackendlessDeviceId") != null) {
                userIds.add((String) membersDetails.get("BackendlessDeviceId"));
            }

            DeliveryOptions deliveryOptions = new DeliveryOptions();
            deliveryOptions.setPushSinglecast(userIds);

            PublishOptions publishOptions = new PublishOptions();
            publishOptions.putHeader("android-ticker-text", "Youz");
            publishOptions.putHeader("android-content-title", "Youz");
            publishOptions.putHeader("android-content-text", messagePush);
            publishOptions.putHeader("ios-alert", messagePush);
            publishOptions.putHeader("ios-badge", "0");
            publishOptions.putHeader("ios-sound", "default");
            publishOptions.putHeader("type", "like");
            publishOptions.putHeader("postId", postId);
            publishOptions.putHeader("userId", userId);

            Backendless.Messaging.publish(messagePush, publishOptions, deliveryOptions, new AsyncCallback<MessageStatus>() {
                @Override
                public void handleResponse(MessageStatus messageStatus) {

                }

                @Override
                public void handleFault(BackendlessFault backendlessFault) {

                }
            });

        }
    }

    public static void sendCommentPush(HashMap<String, Object> membersDetails, String postId, String userId) {

        if ((boolean) membersDetails.get("notifsComments") && membersDetails.get("status").equals("offline")) {

            String messagePush = "Someone commented your post";
            List<String> userIds = new ArrayList<>();
            if (membersDetails.get("BackendlessDeviceId") != null) {
                userIds.add((String) membersDetails.get("BackendlessDeviceId"));
            }

            DeliveryOptions deliveryOptions = new DeliveryOptions();
            deliveryOptions.setPushSinglecast(userIds);

            PublishOptions publishOptions = new PublishOptions();
            publishOptions.putHeader("android-ticker-text", "Youz");
            publishOptions.putHeader("android-content-title", "Youz");
            publishOptions.putHeader("android-content-text", messagePush);
            publishOptions.putHeader("ios-alert", messagePush);
            publishOptions.putHeader("ios-badge", "0");
            publishOptions.putHeader("ios-sound", "default");
            publishOptions.putHeader("type", "comment");
            publishOptions.putHeader("postId", postId);
            publishOptions.putHeader("userId", userId);

            Backendless.Messaging.publish(messagePush, publishOptions, deliveryOptions, new AsyncCallback<MessageStatus>() {
                @Override
                public void handleResponse(MessageStatus messageStatus) {

                }

                @Override
                public void handleFault(BackendlessFault backendlessFault) {

                }
            });

        }
    }

    public static void sendReplyPush(HashMap<String, Object> membersDetails, String postId, String userId) {

        if ((boolean) membersDetails.get("notifsComments") && membersDetails.get("status").equals("offline")) {

            String messagePush = "Someone replied to your comment";
            List<String> userIds = new ArrayList<>();
            if (membersDetails.get("BackendlessDeviceId") != null) {
                userIds.add((String) membersDetails.get("BackendlessDeviceId"));
            }

            DeliveryOptions deliveryOptions = new DeliveryOptions();
            deliveryOptions.setPushSinglecast(userIds);

            PublishOptions publishOptions = new PublishOptions();
            publishOptions.putHeader("android-ticker-text", "Youz");
            publishOptions.putHeader("android-content-title", "Youz");
            publishOptions.putHeader("android-content-text", messagePush);
            publishOptions.putHeader("ios-alert", messagePush);
            publishOptions.putHeader("ios-badge", "0");
            publishOptions.putHeader("ios-sound", "default");
            publishOptions.putHeader("type", "comment");
            publishOptions.putHeader("postId", postId);
            publishOptions.putHeader("userId", userId);

            Backendless.Messaging.publish(messagePush, publishOptions, deliveryOptions, new AsyncCallback<MessageStatus>() {
                @Override
                public void handleResponse(MessageStatus messageStatus) {

                }

                @Override
                public void handleFault(BackendlessFault backendlessFault) {

                }
            });

        }
    }

    public static void sendSharePush(HashMap<String, Object> membersDetails, String postId, String userId) {

        if ((boolean) membersDetails.get("notifsShares") && membersDetails.get("status").equals("offline")) {

            String messagePush = "Someone reyouzed your post";
            List<String> userIds = new ArrayList<>();
            if (membersDetails.get("BackendlessDeviceId") != null) {
                userIds.add((String) membersDetails.get("BackendlessDeviceId"));
            }

            DeliveryOptions deliveryOptions = new DeliveryOptions();
            deliveryOptions.setPushSinglecast(userIds);

            PublishOptions publishOptions = new PublishOptions();
            publishOptions.putHeader("android-ticker-text", "Youz");
            publishOptions.putHeader("android-content-title", "Youz");
            publishOptions.putHeader("android-content-text", messagePush);
            publishOptions.putHeader("ios-alert", messagePush);
            publishOptions.putHeader("ios-badge", "0");
            publishOptions.putHeader("ios-sound", "default");
            publishOptions.putHeader("type", "share");
            publishOptions.putHeader("postId", postId);
            publishOptions.putHeader("userId", userId);

            Backendless.Messaging.publish(messagePush, publishOptions, deliveryOptions, new AsyncCallback<MessageStatus>() {
                @Override
                public void handleResponse(MessageStatus messageStatus) {

                }

                @Override
                public void handleFault(BackendlessFault backendlessFault) {

                }
            });

        }
    }

    public static void sendNewMessagePush(HashMap<String, Object> membersDetails, String chatId, String userId) {

        if ((boolean) membersDetails.get("notifsChats") && membersDetails.get("status").equals("offline")) {

            String messagePush = "You have new chat message";
            List<String> userIds = new ArrayList<>();
            if (membersDetails.get("BackendlessDeviceId") != null) {
                userIds.add((String) membersDetails.get("BackendlessDeviceId"));
            }

            DeliveryOptions deliveryOptions = new DeliveryOptions();
            deliveryOptions.setPushSinglecast(userIds);

            PublishOptions publishOptions = new PublishOptions();
            publishOptions.putHeader("android-ticker-text", "Youz");
            publishOptions.putHeader("android-content-title", "Youz");
            publishOptions.putHeader("android-content-text", messagePush);
            publishOptions.putHeader("ios-alert", messagePush);
            publishOptions.putHeader("ios-badge", "0");
            publishOptions.putHeader("ios-sound", "default");
            publishOptions.putHeader("type", "chat");
            publishOptions.putHeader("chatId", chatId);
            publishOptions.putHeader("userId", userId);

            Backendless.Messaging.publish(messagePush, publishOptions, deliveryOptions, new AsyncCallback<MessageStatus>() {
                @Override
                public void handleResponse(MessageStatus messageStatus) {

                }

                @Override
                public void handleFault(BackendlessFault backendlessFault) {

                }
            });

        }
    }

    public static void sendNewPostPush(final String postId, final String userId, String currentLocation) {
        final FirebaseDatabase mRootRef = FirebaseDatabase.getInstance();
        Query mContactRef = mRootRef.getReference("contacts").orderByChild(userId).equalTo(true);
        Query mNearUserRef = mRootRef.getReference("users").orderByChild("location").equalTo(currentLocation);

        final List<String> listUserId = new ArrayList<>();
        final List<String> listUserOneSignalIds = new ArrayList<>();

        ValueEventListener valueEventListenerNearUser = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, Object> users = (HashMap<String, Object>) dataSnapshot.getValue();

                for (Map.Entry<String, Object> user : users.entrySet()) {
                    HashMap<String, Object> userDetails = (HashMap<String, Object>) user.getValue();
                    if (userDetails != null && (boolean) userDetails.get("notifsPosts") && userDetails.get("status").equals("offline")) {
                        if (userDetails.get("BackendlessDeviceId") != null && !user.getKey().equals(userId)) {
                            if (!listUserOneSignalIds.contains(userDetails.get("BackendlessDeviceId"))) {
                                listUserOneSignalIds.add((String) userDetails.get("BackendlessDeviceId"));
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
                                if (userDetails.get("BackendlessDeviceId") != null && !dataSnapshot.getKey().equals(userId)) {
                                    if (!listUserOneSignalIds.contains(userDetails.get("BackendlessDeviceId"))) {
                                        listUserOneSignalIds.add((String) userDetails.get("BackendlessDeviceId"));
                                    }
                                }
                            }
                            counter[0]++;
                            if (counter[0] == contactCount) {

                                String messagePush = "Youz friend had publish new post";

                                DeliveryOptions deliveryOptions = new DeliveryOptions();
                                deliveryOptions.setPushSinglecast(listUserOneSignalIds);

                                PublishOptions publishOptions = new PublishOptions();
                                publishOptions.putHeader("android-ticker-text", "Youz");
                                publishOptions.putHeader("android-content-title", "Youz");
                                publishOptions.putHeader("android-content-text", messagePush);
                                publishOptions.putHeader("ios-alert", messagePush);
                                publishOptions.putHeader("ios-badge", "0");
                                publishOptions.putHeader("ios-sound", "default");
                                publishOptions.putHeader("type", "post");
                                publishOptions.putHeader("postId", postId);
                                publishOptions.putHeader("userId", userId);

                                Backendless.Messaging.publish(messagePush, publishOptions, deliveryOptions, new AsyncCallback<MessageStatus>() {
                                    @Override
                                    public void handleResponse(MessageStatus messageStatus) {

                                    }

                                    @Override
                                    public void handleFault(BackendlessFault backendlessFault) {

                                    }
                                });

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

    }

}