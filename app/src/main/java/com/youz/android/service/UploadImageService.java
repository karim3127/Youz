package com.youz.android.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.onesignal.OneSignal;
import com.youz.android.R;
import com.youz.android.activity.ChatMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class UploadImageService extends Service {

    private SharedPreferences prefs;
    private int id;
    private NotificationManager nm;
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
    String chatId;
    String privateContactId;
    String userId;
    Uri fileUri;
    FirebaseDatabase mRootRef = FirebaseDatabase.getInstance();
    private DatabaseReference mMessageRef;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();


    public UploadImageService() {}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        prefs = getSharedPreferences("com.youz.android", Context.MODE_PRIVATE);
        mMessageRef = mRootRef.getReference("messages");
        format.setTimeZone(TimeZone.getDefault());
        try {
            if (intent.getExtras() != null) {

                chatId = intent.getExtras().getString("chatId");
                privateContactId = intent.getExtras().getString("privateContactId");
                userId = intent.getExtras().getString("userId");
                fileUri = Uri.parse(intent.getExtras().getString("fileUri"));
                id = intent.getIntExtra("id", -1);

                nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                final NotificationCompat.Builder builder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.ic_logo_header).setColor(getResources().getColor(R.color.colorPrimary)).setOngoing(false);
                builder.setContentTitle("Send image message");


                StorageReference storageAvatarRef = storageRef.child(userId + "/" + new Date().getTime() + ".jpg");
                UploadTask uploadTask = storageAvatarRef.putFile(fileUri);

                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads

                        builder.setContentText("Image upload failed")
                                // Removes the progress bar
                                .setProgress(0, 0, false);
                        builder.setOngoing(false).setAutoCancel(true);

                        nm.notify(id, builder.build());
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                        builder.setProgress(100, (int) progress, false);
                        nm.notify(id, builder.build());
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                        final String downloadUrl = taskSnapshot.getDownloadUrl().toString();

                        final String dateSent = format.format(new Date());

                        HashMap<String, Object> messageDetails = new HashMap<>();
                        messageDetails.put("dateSent", dateSent);
                        messageDetails.put("senderId", userId);
                        messageDetails.put("url", downloadUrl);
                        messageDetails.put("type", "image");

                        final String msgId = mMessageRef.getRef().push().getKey();

                        HashMap<String, Object> message = new HashMap<>();
                        message.put(msgId, messageDetails);

                        mMessageRef.child(chatId).updateChildren(message, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            }
                        });

                        mRootRef.getReference("chats").child(chatId).runTransaction(new Transaction.Handler() {
                            @Override
                            public Transaction.Result doTransaction(MutableData mutableData) {
                                HashMap<String, Object> chatDetails = (HashMap<String, Object>) mutableData.getValue();

                                HashMap<String, Long> newUnreadMessagesDialog = new HashMap<String, Long>();

                                HashMap<String, Long> unreadMessagesDialog = (HashMap<String, Long>) chatDetails.get("unreadMessages");

                                chatDetails.put("lastMessage", "Image message");
                                chatDetails.put("lastMessageDateSent", dateSent);
                                chatDetails.put("lastMessageUserId", userId);

                                try {
                                    for (Map.Entry<String, Long> memberId : unreadMessagesDialog.entrySet()) {
                                        if (memberId.getKey().equals(userId)) {
                                            newUnreadMessagesDialog.put(memberId.getKey(), Long.valueOf(0));
                                        } else {
                                            newUnreadMessagesDialog.put(memberId.getKey(), memberId.getValue() + 1);
                                        }
                                    }
                                    chatDetails.put("unreadMessages", newUnreadMessagesDialog);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                mutableData.setValue(chatDetails);

                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                            }
                        });

                        if (ChatMessage.dataSnapshotUser != null) {
                            HashMap<String, Object> membersDetails = (HashMap<String, Object>) ChatMessage.dataSnapshotUser.getValue();
                            if ((boolean) membersDetails.get("notifsChats") && membersDetails.get("status").equals("offline")) {
                                List<String> userIds = new ArrayList<>();
                                if (membersDetails.get("oneSignalUserId") != null) {
                                    userIds.add("'" + membersDetails.get("oneSignalUserId") + "'");
                                }

                                String userIdsList = userIds.toString();
                                String messagePush = "You have new chat message";

                                try {
                                    OneSignal.postNotification(new JSONObject("{'contents': {'en':'" + messagePush + "'}, 'ios_sound': 'Notification.mp3', 'data': {'type'='chat','chatId':'" + chatId + "','userId':'" + userId + "'},  'include_player_ids': " + userIdsList + "}"), null);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        }

                        builder.setContentText("Image upload done").setProgress(0, 0, false);
                        builder.setOngoing(false).setAutoCancel(true);
                        nm.notify(id, builder.build());

                    }
                });
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
