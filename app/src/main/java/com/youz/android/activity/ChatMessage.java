package com.youz.android.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.makeramen.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.rey.material.widget.RadioButton;
import com.wang.avi.AVLoadingIndicatorView;
import com.youz.android.R;
import com.youz.android.adapter.MessageItemAdapter;
import com.youz.android.service.UploadImageService;
import com.youz.android.util.BackendlessUtil;
import com.youz.android.util.ConnectionDetector;
import com.youz.android.util.UtilUserAvatar;
import com.youz.android.view.SuperSwipeRefreshLayout;
import com.youz.android.view.paperonboarding.listeners.AnimatorEndListener;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ChatMessage extends BaseActivity {

    static final public String COPA_RESULT = "SETTING.REQUEST_RESULT";
    static final public String COPA_MESSAGE = "SETTING.COPA_MESSAGE";

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.rv_messages)
    RecyclerView rvMessages;

    @BindView(R.id.switcher)
    ViewSwitcher switcher;

    @BindView(R.id.et_message)
    EditText etMessage;

    @BindView(R.id.img_attach)
    ImageView imgAttach;

    @BindView(R.id.img_send)
    ImageView imgSend;

    @BindView(R.id.iv_user)
    ImageView ivUser;

    @BindView(R.id.avloadingIndicatorView)
    AVLoadingIndicatorView avloadingIndicatorView;

    @BindView(R.id.swipe_refresh)
    SuperSwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.view_blocage)
    View viewBlocage;

    @BindView(R.id.v_block_input)
    View vBlockInput;

    private static int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 21;
    private static final int PICK_IMAGE = 1;
    public static DisplayImageOptions options = new DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .considerExifParams(true)
            .showImageOnLoading(R.drawable.ic_white)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();

    private String chatId, privateId, chatDeletes;
    boolean hasMessage, isBlocked = false, hasBlock = false;
    ConnectionDetector connectionDetector;
    private MessageItemAdapter adapter;
    boolean isAbleToSend = false;
    SharedPreferences prefs;
    String userId;
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");

    FirebaseDatabase mRootRef = FirebaseDatabase.getInstance();
    private DatabaseReference mMessageRef;
    private DatabaseReference mUserRef;
    Query mMessageQuery, mNewMessageQuery;

    private ChildEventListener childEventListenerMessageModification;
    private ChildEventListener childEventListenerNewMessage;
    private ValueEventListener valueEventListenerUser;

    DatabaseReference mBlockingRef;
    private ValueEventListener valueEventListenerBlocking;

    private BroadcastReceiver receiver;
    public static DataSnapshot dataSnapshotUser;
    int nbMsgMore;
    String lastId = "";
    private MenuItem blockItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_message);
        ButterKnife.bind(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        chatId = getIntent().getExtras().getString("ChatId", "");
        privateId = getIntent().getExtras().getString("PrivateId", "");
        chatDeletes = getIntent().getExtras().getString("ChatDeletes", "");
        hasMessage = getIntent().getExtras().getBoolean("HasMessage", false);

        hasBlock = MainActivity.listYouzBlocks.contains(privateId);

        connectionDetector = new ConnectionDetector(this);
        prefs = getSharedPreferences("com.youz.android", Context.MODE_PRIVATE);
        userId = prefs.getString("UserId", "");
        format.setTimeZone(TimeZone.getDefault());

        if (isBlocked || hasBlock) {
            imgAttach.setEnabled(false);
            etMessage.setEnabled(false);
            imgSend.setEnabled(false);

            vBlockInput.setVisibility(View.VISIBLE);
        }

        if (!hasMessage) {
            avloadingIndicatorView.setVisibility(View.GONE);
        }
        mUserRef = mRootRef.getReference("users").child(privateId);
        mMessageRef = mRootRef.getReference("messages").child(chatId);
        mMessageQuery = mRootRef.getReference("messages").child(chatId).orderByChild("dateSent").limitToLast(20);
        mNewMessageQuery = mRootRef.getReference("messages").child(chatId).limitToLast(1);

        mBlockingRef = mRootRef.getReference("blocks/" + privateId + "/" + userId);
        checkBlockingContacts();

        int res = UtilUserAvatar.getDrawableRes(this, privateId);
        ivUser.setImageResource(res);

        etMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (etMessage.getText().length() > 0 && !isAbleToSend) {
                    isAbleToSend = true;
                    switcher.showNext();
                } else if (etMessage.getText().length() == 0){
                    isAbleToSend = false;
                    switcher.showPrevious();
                }
            }
        });

        swipeRefreshLayout.setOnPullRefreshListener(new SuperSwipeRefreshLayout.OnPullRefreshListener() {

            @Override
            public void onRefresh() {
                viewBlocage.setVisibility(View.VISIBLE);

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            loadNextPageChatHistory();
                        } catch (Exception ex) {

                        }
                    }
                }, 500);
            }

            @Override
            public void onPullDistance(int distance) {

            }

            @Override
            public void onPullEnable(boolean enable) {

            }
        });

        rvMessages.setHasFixedSize(true);
        rvMessages.setItemViewCacheSize(500);
        rvMessages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        adapter = new MessageItemAdapter(this, new ArrayList<Pair<String, HashMap<String, Object>>>(), chatId);
        rvMessages.swapAdapter(adapter, false);

        getMessageChat();
        getMemberDetail();


        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                String mMessage = intent.getExtras().getString(COPA_MESSAGE, "");

                if (mMessage.equals("Block")) {
                    hasBlock = MainActivity.listYouzBlocks.contains(privateId);

                    if (isBlocked || hasBlock) {
                        imgAttach.setEnabled(false);
                        etMessage.setEnabled(false);
                        imgSend.setEnabled(false);

                        vBlockInput.setVisibility(View.VISIBLE);

                        View viewFocus = getCurrentFocus();
                        if (viewFocus != null) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(viewFocus.getWindowToken(), 0);
                            etMessage.clearFocus();
                        }

                    } else {
                        imgAttach.setEnabled(true);
                        etMessage.setEnabled(true);
                        imgSend.setEnabled(true);

                        vBlockInput.setVisibility(View.GONE);
                    }
                }
            }
        };

    }

    @OnClick(R.id.img_attach)
    public void chooseImage() {
        if (ContextCompat.checkSelfPermission(ChatMessage.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
        } else {
            ActivityCompat.requestPermissions(ChatMessage.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
        }
    }

    @OnClick(R.id.img_send)
    public void sendText() {
        if (!etMessage.getText().toString().trim().equals("")) {
            sendTextMessage(etMessage.getText().toString().trim());
        }
    }

    @OnClick(R.id.v_block_input)
    public void vBlockInputClick() {
        String messageBlock = "";
        if (hasBlock) {
            messageBlock = getResources().getString(R.string.blockingInputMessage);
        } else {
            messageBlock = getResources().getString(R.string.blockedInputMessage);
        }
        Toast.makeText(this, messageBlock, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver), new IntentFilter(COPA_RESULT));
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        mMessageRef.removeEventListener(childEventListenerMessageModification);
        mNewMessageQuery.removeEventListener(childEventListenerNewMessage);
        mUserRef.removeEventListener(valueEventListenerUser);
        mBlockingRef.removeEventListener(valueEventListenerBlocking);

        chatId = null;
        dataSnapshotUser = null;
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        HashMap<String, Object> unreadMessages = new HashMap<>();
        unreadMessages.put(userId, 0);
        mRootRef.getReference("chats").child(chatId + "/unreadMessages").updateChildren(unreadMessages);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (hasBlock) {
            suggestDeblock();
        }
    }

    public void getMemberDetail() {
        valueEventListenerUser = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataSnapshotUser = dataSnapshot;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mUserRef.addValueEventListener(valueEventListenerUser);
    }

    public void getMessageChat() {

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    HashMap<String, Object> messages = (HashMap<String, Object>) dataSnapshot.getValue();

                    List<Pair<String, HashMap<String, Object>>> listItems = new ArrayList<Pair<String, HashMap<String, Object>>>();
                    for (Map.Entry<String, Object> msg : messages.entrySet()) {
                        Pair<String, HashMap<String, Object>> item = new Pair<>(msg.getKey(), (HashMap<String, Object>) msg.getValue());
                        listItems.add(item);
                    }
                    
                    Collections.sort(listItems, new Comparator<Pair<String, HashMap<String, Object>>>() {
                        @Override
                        public int compare(Pair<String, HashMap<String, Object>> c1, Pair<String, HashMap<String, Object>> c2) {
                            String date1 = (String) c1.second.get("dateSent");
                            String date2 = (String) c2.second.get("dateSent");

                            Date dateValue1 = null;
                            try {
                                dateValue1 = format.parse(date1);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            Date dateValue2 = null;
                            try {
                                dateValue2 = format.parse(date2);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            return dateValue1.compareTo(dateValue2);
                        }
                    });

                    for (int i = 0; i < listItems.size(); i++) {

                        String itemDate = (String) listItems.get(i).second.get("dateSent");
                        Date dateDialog = null;
                        try {
                            dateDialog = format.parse(itemDate);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        Date dateDelete = null;
                        try {
                            dateDelete = format.parse(chatDeletes);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        if (dateDelete == null || dateDelete.compareTo(dateDialog) < 0) {

                            if (lastId.equals(""))
                                lastId = listItems.get(i).first;

                            adapter.addOnBottom(listItems.get(i));
                            scrollDown();
                        }
                    }
                    avloadingIndicatorView.setVisibility(View.GONE);

                }

                if (adapter.getItemCount() < 20) {
                    swipeRefreshLayout.setEnabled(false);
                }
                addListenerLastMessage();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                if (adapter.getItemCount() < 20) {
                    swipeRefreshLayout.setEnabled(false);
                }
            }
        };
        mMessageQuery.addListenerForSingleValueEvent(valueEventListener);


        childEventListenerMessageModification = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    for (int i = adapter.getItemCount() - 1; i >= 0; i--) {
                        if (adapter.listItems.get(i).first.equals(dataSnapshot.getKey())) {

                            final View container = ((MessageItemAdapter.MessageHolder) rvMessages.findViewHolderForPosition(i)).chatItemContainer;
                            final View rlDate = ((MessageItemAdapter.MessageHolder) rvMessages.findViewHolderForPosition(i)).rlDate;

                            container.animate().alpha(0f).setDuration(300).setListener(new AnimatorEndListener() {
                                @Override
                                public void onAnimationEnd(android.animation.Animator animator) {
                                    container.setVisibility(View.GONE);
                                    rlDate.setVisibility(View.GONE);
                                }
                            }).start();
                        }
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mMessageRef.addChildEventListener(childEventListenerMessageModification);

        if (!hasMessage) {
            addListenerLastMessage();
        }
    }

    public void addListenerLastMessage() {
        childEventListenerNewMessage = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getValue() != null) {

                    Date dateDelete = null;
                    try {
                        dateDelete = format.parse(chatDeletes);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    String dateMsg = (String) ((HashMap<String, Object>) dataSnapshot.getValue()).get("dateSent");

                    Date dateValueMsg = null;
                    try {
                        dateValueMsg = format.parse(dateMsg);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    boolean exist = false;
                    for (int i = adapter.getItemCount() - 1; i >= 0; i--) {
                        if (adapter.listItems.get(i).first.equals(dataSnapshot.getKey())) {
                            exist = true;
                        }
                    }

                    if ((dateDelete == null || dateValueMsg.compareTo(dateDelete) > 0) && !exist) {
                        final Pair<String, HashMap<String, Object>> msg = new Pair<>(dataSnapshot.getKey(), (HashMap<String, Object>) dataSnapshot.getValue());
                        String lastId = (adapter.getItemCount() > 0) ? adapter.listItems.get(adapter.getItemCount() - 1).first : "";
                        if (!lastId.equals(msg.first)) {

                            Uri sound;
                            if (msg.second.get("senderId").equals(userId)) {
                                sound = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.chat_out_going);
                            } else {
                                sound = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.chat_in_coming);
                            }

                            Ringtone ringtone = RingtoneManager.getRingtone(context, sound);
                            ringtone.play();

                            adapter.addOnBottom(msg);
                            scrollDown();
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mNewMessageQuery.addChildEventListener(childEventListenerNewMessage);
    }

    private void loadNextPageChatHistory() {
        nbMsgMore = 0;
        final String idDelete = lastId;

        Query mMessageQueryLoadMore = mRootRef.getReference("messages").child(chatId).endAt(null, lastId).limitToLast(20);
        lastId = "";

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    HashMap<String, Object> messages = (HashMap<String, Object>) dataSnapshot.getValue();

                    List<Pair<String, HashMap<String, Object>>> listItems = new ArrayList<Pair<String, HashMap<String, Object>>>();
                    for (Map.Entry<String, Object> msg : messages.entrySet()) {
                        Pair<String, HashMap<String, Object>> item = new Pair<>(msg.getKey(), (HashMap<String, Object>) msg.getValue());
                        listItems.add(item);
                    }

                    Collections.sort(listItems, new Comparator<Pair<String, HashMap<String, Object>>>() {
                        @Override
                        public int compare(Pair<String, HashMap<String, Object>> c1, Pair<String, HashMap<String, Object>> c2) {
                            String date1 = (String) c1.second.get("dateSent");
                            String date2 = (String) c2.second.get("dateSent");

                            Date dateValue1 = null;
                            try {
                                dateValue1 = format.parse(date1);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            Date dateValue2 = null;
                            try {
                                dateValue2 = format.parse(date2);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            return dateValue1.compareTo(dateValue2);
                        }
                    });

                    for (int i = 0; i < listItems.size(); i++) {

                        String itemDate = (String) listItems.get(i).second.get("dateSent");
                        Date dateDialog = null;
                        try {
                            dateDialog = format.parse(itemDate);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        Date dateDelete = null;
                        try {
                            dateDelete = format.parse(chatDeletes);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        if (dateDelete == null || dateDelete.compareTo(dateDialog) < 0) {
                            if (lastId.equals(""))
                                lastId = listItems.get(i).first;
                            if (!listItems.get(i).first.equals(idDelete)) {
                                adapter.addOnPosition(listItems.get(i), nbMsgMore);
                            } else {
                                //messageItemAdapter.notifyItemChanged(nbMsgMore);
                            }

                            nbMsgMore++;
                        }
                    }
                }

                if (nbMsgMore < 20) {
                    swipeRefreshLayout.setEnabled(false);
                }

                viewBlocage.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                if (nbMsgMore < 20) {
                    swipeRefreshLayout.setEnabled(false);
                }

                viewBlocage.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);

            }
        };

        mMessageQueryLoadMore.addListenerForSingleValueEvent(valueEventListener);
    }

    private void sendTextMessage(final String messageText) {
        if (connectionDetector.isConnectingToInternet()) {

            final String dateSent = format.format(new Date());

            HashMap<String, Object> messageDetails = new HashMap<>();
            messageDetails.put("dateSent", dateSent);
            messageDetails.put("message", messageText);
            messageDetails.put("senderId", userId);
            messageDetails.put("type", "text");

            String msgId = mMessageQuery.getRef().push().getKey();

            HashMap<String, Object> message = new HashMap<>();
            message.put(msgId, messageDetails);

            mMessageQuery.getRef().updateChildren(message);

            mRootRef.getReference("chats").child(chatId).runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    HashMap<String, Object> chatDetails = (HashMap<String, Object>) mutableData.getValue();

                    HashMap<String, Long> newUnreadMessagesDialog = new HashMap<String, Long>();

                    HashMap<String, Long> unreadMessagesDialog = (HashMap<String, Long>) chatDetails.get("unreadMessages");

                    chatDetails.put("lastMessage", messageText);
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

            BackendlessUtil.sendNewMessagePush((HashMap<String, Object>) dataSnapshotUser.getValue(), chatId, userId);

            etMessage.setText("");

        } else {
            Toast.makeText(ChatMessage.this, "Connexion is down", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendImageMessage(final Uri fileUri) {
        if (connectionDetector.isConnectingToInternet()) {

            Intent intentUpload = new Intent(this, UploadImageService.class);

            intentUpload.putExtra("chatId", chatId);
            intentUpload.putExtra("userId", userId);
            intentUpload.putExtra("id", (int) (Math.random() * 1024));

            intentUpload.putExtra("privateContactId", privateId);
            intentUpload.putExtra("fileUri", fileUri.toString());

            startService(intentUpload);

        } else {
            Toast.makeText(ChatMessage.this, "Connexion is down", Toast.LENGTH_SHORT).show();
        }
    }

    private void scrollDown() {
        rvMessages.post(new Runnable() {
            @Override
            public void run() {
                rvMessages.smoothScrollToPosition(Integer.MAX_VALUE);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE == requestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {

            Uri uri = data.getData();
            showDialog(uri);

        }
    }

    private void showDialog( final  Uri uri) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(true);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.go_pro_dialog_layout, null);
        dialog.setView(dialogView);

        final AlertDialog alerteValidImage = dialog.create();
        alerteValidImage.show();

        RoundedImageView image = (RoundedImageView) dialogView.findViewById(R.id.goProDialogImage);

        ImageLoader.getInstance().displayImage(uri.toString(), image, options);

        TextView txtValide = (TextView) dialogView.findViewById(R.id.txtValide);
        txtValide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alerteValidImage.dismiss();
                try {
                    InputStream imageStream = getContentResolver().openInputStream(uri);
                    Bitmap yourSelectedImage = BitmapFactory.decodeStream(imageStream);

                    Uri fileUri = getImageUri(getApplicationContext(), yourSelectedImage);

                    sendImageMessage(fileUri);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        TextView txtCancel = (TextView) dialogView.findViewById(R.id.txtCancel);
        txtCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alerteValidImage.dismiss();
            }
        });

        try {
            dialog.show();
        }catch (Exception e){

        }

    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 80, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, new Date().getTime() + "", null);
        return Uri.parse(path);
    }

    public void deleteConversation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String positiveBtnText = "";
        String negativeBtnText = "";

        builder.setMessage("Would you like to delete ?");
        positiveBtnText = "Validate";
        negativeBtnText = "Cancel";

        builder.setPositiveButton(positiveBtnText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //do your work here
                if (connectionDetector.isConnectingToInternet()) {

                    final String dateSent = format.format(new Date());
                    HashMap<String, Object> chatDeletes = new HashMap<>();
                    chatDeletes.put(userId, dateSent);

                    mRootRef.getReference("chats").child(chatId + "/chatDeletes").updateChildren(chatDeletes);
                    Toast.makeText(context, "Conversation deleted", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(context, "Connexion is down", Toast.LENGTH_SHORT).show();
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

    public void suggestDeblock() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String positiveBtnText = "";
        String negativeBtnText = "";

        builder.setMessage("You have blocked this user,\nWould you like to deblock him ?");

        positiveBtnText = "Validate";
        negativeBtnText = "Cancel";

        builder.setPositiveButton(positiveBtnText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //do your work here
                if (connectionDetector.isConnectingToInternet()) {

                    DatabaseReference mBlocksRef = mRootRef.getReference("blocks").child(userId).child(privateId);
                    mBlocksRef.removeValue(new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                hasBlock = false;
                                Toast.makeText(context, "User deblocked", Toast.LENGTH_SHORT).show();

                                imgAttach.setEnabled(true);
                                etMessage.setEnabled(true);
                                imgSend.setEnabled(true);

                                vBlockInput.setVisibility(View.GONE);

                                blockItem.setTitle("Block user");
                            }
                        }
                    });

                } else {
                    Toast.makeText(context, "Connexion is down", Toast.LENGTH_SHORT).show();
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

    public void blockUser() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String positiveBtnText = "";
        String negativeBtnText = "";

        if (hasBlock) {
            builder.setMessage("Would you like to deblock this user ?");
        } else {
            builder.setMessage("Would you like to block this user ?");
        }
        positiveBtnText = "Validate";
        negativeBtnText = "Cancel";

        builder.setPositiveButton(positiveBtnText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //do your work here
                if (connectionDetector.isConnectingToInternet()) {

                    String toastMsg;
                    if (hasBlock) {

                        DatabaseReference mBlocksRef = mRootRef.getReference("blocks").child(userId).child(privateId);
                        mBlocksRef.removeValue();

                        toastMsg = "User deblocked";
                    } else {
                        final String dateBlocks = format.format(new Date());
                        String postId = chatId.replace(userId, "");
                        postId = postId.replace(privateId, "");

                        HashMap<String, Object> block = new HashMap<>();
                        block.put("postId", postId);
                        block.put("createdAt", dateBlocks);

                        DatabaseReference mBlocksRef = mRootRef.getReference("blocks").child(userId).child(privateId);
                        mBlocksRef.updateChildren(block);

                        toastMsg = "User blocked";

                    }
                    Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show();
                    hasBlock = !hasBlock;

                    if (hasBlock) {
                        imgAttach.setEnabled(false);
                        etMessage.setEnabled(false);
                        imgSend.setEnabled(false);

                        vBlockInput.setVisibility(View.VISIBLE);

                        View viewFocus = getCurrentFocus();
                        if (viewFocus != null) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(viewFocus.getWindowToken(), 0);
                            etMessage.clearFocus();
                        }

                        blockItem.setTitle("Deblock user");
                    } else {
                        imgAttach.setEnabled(true);
                        etMessage.setEnabled(true);
                        imgSend.setEnabled(true);

                        vBlockInput.setVisibility(View.GONE);

                        blockItem.setTitle("Block user");
                    }
                } else {
                    Toast.makeText(context, "Connexion is down", Toast.LENGTH_SHORT).show();
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

    public void reportUser() {
        View dialoglayout = getLayoutInflater().inflate(R.layout.layout_report, null);
        final RadioButton rb1 = (RadioButton) dialoglayout.findViewById(R.id.rb1);
        final RadioButton rb2 = (RadioButton) dialoglayout.findViewById(R.id.rb2);
        final RadioButton rb3 = (RadioButton) dialoglayout.findViewById(R.id.rb3);
        final RadioButton rb4 = (RadioButton) dialoglayout.findViewById(R.id.rb4);

        CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    rb1.setChecked(compoundButton == rb1);
                    rb2.setChecked(compoundButton == rb2);
                    rb3.setChecked(compoundButton == rb3);
                    rb4.setChecked(compoundButton == rb4);
                }
            }
        };

        rb1.setOnCheckedChangeListener(onCheckedChangeListener);
        rb2.setOnCheckedChangeListener(onCheckedChangeListener);
        rb3.setOnCheckedChangeListener(onCheckedChangeListener);
        rb4.setOnCheckedChangeListener(onCheckedChangeListener);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String positiveBtnText = "";
        String negativeBtnText = "";

        builder.setTitle("Abuse report");
        builder.setView(dialoglayout);

        positiveBtnText = "Validate";
        negativeBtnText = "Cancel";

        builder.setPositiveButton(positiveBtnText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //do your work here
                if (rb1.isChecked() || rb2.isChecked() || rb3.isChecked() || rb4.isChecked()) {
                    String type = "";
                    if (rb1.isChecked()) {
                        type = "Spam";
                    } else if (rb2.isChecked()) {
                        type = "Hate speech or violence";
                    } else if (rb3.isChecked()) {
                        type = "Nudity or pornography";
                    } else {
                        type = "Piracy";
                    }
                    if (connectionDetector.isConnectingToInternet()) {

                        HashMap<String, Object> chatReports = new HashMap<>();
                        chatReports.put(userId, type);

                        mRootRef.getReference("users").child(privateId + "/reports").updateChildren(chatReports);

                        Toast.makeText(context, "User reported", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(context, "Connexion is down", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                } else {
                    Toast.makeText(ChatMessage.this, "Choose type of report", Toast.LENGTH_SHORT).show();
                }
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

    public void checkBlockingContacts() {
        valueEventListenerBlocking = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                isBlocked = dataSnapshot.getValue() != null;

                if (isBlocked || hasBlock) {
                    imgAttach.setEnabled(false);
                    etMessage.setEnabled(false);
                    imgSend.setEnabled(false);

                    vBlockInput.setVisibility(View.VISIBLE);

                    View viewFocus = getCurrentFocus();
                    if (viewFocus != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(viewFocus.getWindowToken(), 0);
                        etMessage.clearFocus();
                    }

                } else {
                    imgAttach.setEnabled(true);
                    etMessage.setEnabled(true);
                    imgSend.setEnabled(true);

                    vBlockInput.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mBlockingRef.addValueEventListener(valueEventListenerBlocking);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_normal_message, menu);

        blockItem = menu.findItem(R.id.action_block);
        if (hasBlock) {
            blockItem.setTitle("Deblock user");
        } else {
            blockItem.setTitle("Block user");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_delete) {
            deleteConversation();
            return true;
        } else if (id == R.id.action_block) {
            blockUser();
            return true;
        } else if (id == R.id.action_report) {
            reportUser();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}