package com.youz.android.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.aakira.expandablelayout.ExpandableRelativeLayout;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.youz.android.R;
import com.youz.android.adapter.NewPostPhotoItemAdapter;
import com.youz.android.util.ConnectionDetector;
import com.youz.android.util.OneSignalUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class NewPost extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.tv_post)
    TextView tvPost;

    @BindView(R.id.et_status)
    EditText etStatus;

    @BindView(R.id.img_back)
    ImageView imgBack;

    @BindView(R.id.v_back)
    View vBack;

    @BindView(R.id.img_photo)
    ImageView imgPhoto;

    @BindView(R.id.img_texte)
    ImageView imgTexte;

    @BindView(R.id.img_color)
    ImageView imgColor;

    @BindView(R.id.ll_share_to)
    LinearLayout llShareTo;

    @BindView(R.id.img_share_to)
    ImageView imgShareTo;

    @BindView(R.id.ll_texte)
    LinearLayout llTexte;

    @BindView(R.id.ll_color)
    LinearLayout llColor;

    @BindView(R.id.rl_photo)
    RelativeLayout rlPhoto;

    @BindView(R.id.rv_photo)
    RecyclerView rvPhoto;

    @BindView(R.id.v_blocage)
    View vBlocage;

    @BindView(R.id.erl_option)
    ExpandableRelativeLayout erlOption;

    @BindView(R.id.tv_style_1)
    TextView tvStyle1;

    @BindView(R.id.tv_style_2)
    TextView tvStyle2;

    @BindView(R.id.tv_style_3)
    TextView tvStyle3;

    @BindView(R.id.tv_style_4)
    TextView tvStyle4;

    @BindView(R.id.tv_style_5)
    TextView tvStyle5;

    @BindView(R.id.tv_style_6)
    TextView tvStyle6;

    @BindView(R.id.tv_style_7)
    TextView tvStyle7;

    @BindView(R.id.tv_style_8)
    TextView tvStyle8;

    @BindView(R.id.tv_style_9)
    TextView tvStyle9;

    @BindView(R.id.tv_size_1)
    TextView tvSize1;

    @BindView(R.id.tv_size_2)
    TextView tvSize2;

    @BindView(R.id.tv_size_3)
    TextView tvSize3;

    @BindView(R.id.tv_size_4)
    TextView tvSize4;

    @BindView(R.id.tv_size_5)
    TextView tvSize5;

    @BindView(R.id.tv_size_6)
    TextView tvSize6;

    @BindView(R.id.tv_size_7)
    TextView tvSize7;

    @BindView(R.id.img_color_1)
    ImageView img_color_1;

    @BindView(R.id.img_color_2)
    ImageView img_color_2;

    @BindView(R.id.img_color_3)
    ImageView img_color_3;

    @BindView(R.id.img_color_4)
    ImageView img_color_4;

    @BindView(R.id.img_color_5)
    ImageView img_color_5;

    @BindView(R.id.img_color_6)
    ImageView img_color_6;

    @BindView(R.id.img_color_7)
    ImageView img_color_7;

    @BindView(R.id.img_color_8)
    ImageView img_color_8;

    @BindView(R.id.img_color_9)
    ImageView img_color_9;

    @BindView(R.id.img_color_10)
    ImageView img_color_10;

    @BindView(R.id.img_color_11)
    ImageView img_color_11;

    @BindView(R.id.img_color_12)
    ImageView img_color_12;

    @BindView(R.id.img_color_13)
    ImageView img_color_13;

    @BindView(R.id.img_color_14)
    ImageView img_color_14;

    @BindView(R.id.img_color_15)
    ImageView img_color_15;

    @BindView(R.id.img_color_16)
    ImageView img_color_16;

    @BindView(R.id.img_color_17)
    ImageView img_color_17;

    @BindView(R.id.img_color_18)
    ImageView img_color_18;

    private SharedPreferences prefs;
    private String userId;
    ConnectionDetector connectionDetector;
    SimpleDateFormat format;
    TimeZone timeZone;
    List<String> listImages;
    Typeface typeFace, typeFace1, typeFace2, typeFace3, typeFace4, typeFace5, typeFace6, typeFace7, typeFace8, typeFace9;
    public Uri fileUri;
    public boolean hasImage = false;
    public boolean hasColor = false;
    public boolean isPublic = true;
    public String postColor = "";
    public String chosedpostColor = "";
    String postFont = "";
    long postFontSize = -1;
    boolean isOpened = false;

    FirebaseDatabase mRootRef = FirebaseDatabase.getInstance();
    DatabaseReference mPostRef;
    DatabaseReference mTagRef;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();

    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_new_post);
        ButterKnife.bind(this);

        connectionDetector = new ConnectionDetector(this);
        prefs = getSharedPreferences("com.youz.android", Context.MODE_PRIVATE);
        userId = prefs.getString("UserId", "");

        mPostRef = mRootRef.getReference("posts");
        mTagRef = mRootRef.getReference("tags");

        timeZone = TimeZone.getDefault();
        format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
        format.setTimeZone(timeZone);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        typeFace = Typeface.createFromAsset(getAssets(), "fonts/optima_bold.ttf");
        tvPost.setTypeface(typeFace);

        typeFace1 = Typeface.createFromAsset(getAssets(), "fonts/BrushScriptStd.ttf");
        typeFace2 = Typeface.createFromAsset(getAssets(), "fonts/ComicSansMS.ttf");
        typeFace3 = Typeface.createFromAsset(getAssets(), "fonts/Optima-Regular.ttf");
        typeFace4 = Typeface.createFromAsset(getAssets(), "fonts/LevenimMT.ttf");
        typeFace5 = Typeface.createFromAsset(getAssets(), "fonts/Nyala-Regular.ttf");
        typeFace6 = Typeface.createFromAsset(getAssets(), "fonts/SegoePrint.ttf");
        typeFace7 = Typeface.createFromAsset(getAssets(), "fonts/SegoePrint-Bold.ttf");
        typeFace8 = Typeface.createFromAsset(getAssets(), "fonts/TektonPro-Bold.ttf");
        typeFace9 = Typeface.createFromAsset(getAssets(), "fonts/Vijaya.ttf");

        tvStyle1.setTypeface(typeFace1);
        tvStyle2.setTypeface(typeFace2);
        tvStyle3.setTypeface(typeFace3);
        tvStyle4.setTypeface(typeFace4);
        tvStyle5.setTypeface(typeFace5);
        tvStyle6.setTypeface(typeFace6);
        tvStyle7.setTypeface(typeFace7);
        tvStyle8.setTypeface(typeFace8);
        tvStyle9.setTypeface(typeFace9);

        try {
            listImages = getAllShownImagesPath();
        } catch (Exception e) {}

        listImages.add(0, "");

        rvPhoto.setHasFixedSize(true);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        rvPhoto.setLayoutManager(gridLayoutManager);

        NewPostPhotoItemAdapter adapter = new NewPostPhotoItemAdapter(this, listImages, imgBack, vBack);
        rvPhoto.setAdapter(adapter);

        tvSize5.setSelected(true);
        tvStyle3.setSelected(true);

        img_color_1.setOnClickListener(colorClickListener);
        img_color_2.setOnClickListener(colorClickListener);
        img_color_3.setOnClickListener(colorClickListener);
        img_color_4.setOnClickListener(colorClickListener);
        img_color_5.setOnClickListener(colorClickListener);
        img_color_6.setOnClickListener(colorClickListener);
        img_color_7.setOnClickListener(colorClickListener);
        img_color_8.setOnClickListener(colorClickListener);
        img_color_9.setOnClickListener(colorClickListener);
        img_color_10.setOnClickListener(colorClickListener);
        img_color_11.setOnClickListener(colorClickListener);
        img_color_12.setOnClickListener(colorClickListener);
        img_color_13.setOnClickListener(colorClickListener);
        img_color_14.setOnClickListener(colorClickListener);
        img_color_15.setOnClickListener(colorClickListener);
        img_color_16.setOnClickListener(colorClickListener);
        img_color_17.setOnClickListener(colorClickListener);
        img_color_18.setOnClickListener(colorClickListener);

        tvStyle1.setOnClickListener(textStyleClickListener);
        tvStyle2.setOnClickListener(textStyleClickListener);
        tvStyle3.setOnClickListener(textStyleClickListener);
        tvStyle4.setOnClickListener(textStyleClickListener);
        tvStyle5.setOnClickListener(textStyleClickListener);
        tvStyle6.setOnClickListener(textStyleClickListener);
        tvStyle7.setOnClickListener(textStyleClickListener);
        tvStyle8.setOnClickListener(textStyleClickListener);
        tvStyle9.setOnClickListener(textStyleClickListener);

        tvSize1.setOnClickListener(textSizeClickListener);
        tvSize2.setOnClickListener(textSizeClickListener);
        tvSize3.setOnClickListener(textSizeClickListener);
        tvSize4.setOnClickListener(textSizeClickListener);
        tvSize5.setOnClickListener(textSizeClickListener);
        tvSize6.setOnClickListener(textSizeClickListener);
        tvSize7.setOnClickListener(textSizeClickListener);

        etStatus.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_DOWN){
                    int editTextLineCount = etStatus.getLineCount();
                    if (editTextLineCount >= 7)
                        return true;
                }
                return false;
            }
        });

        etStatus.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                int editTextLineCount = etStatus.getLineCount();
                if (editTextLineCount >= 7)
                    return;
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        setRandomBackColor();
    }

    @OnClick(R.id.v_blocage)
    public void blockclick() {
        isOpened = false;
        vBlocage.setVisibility(View.GONE);
        erlOption.toggle();

        imgPhoto.setSelected(false);
        imgTexte.setSelected(false);
        imgColor.setSelected(false);
    }

    @OnClick(R.id.img_close)
    public void closeActivity() {
        onBackPressed();
    }

    @OnClick(R.id.img_photo)
    public void choosePhoto() {
        imgPhoto.setSelected(true);
        imgTexte.setSelected(false);
        imgColor.setSelected(false);

        View viewFocus = getCurrentFocus();
        if (viewFocus != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(viewFocus.getWindowToken(), 0);
            etStatus.clearFocus();
        }

        rlPhoto.setVisibility(View.VISIBLE);
        llTexte.setVisibility(View.GONE);
        llColor.setVisibility(View.GONE);

        if (!isOpened) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    isOpened = true;
                    vBlocage.setVisibility(View.VISIBLE);
                    erlOption.toggle();
                }
            }, 200);
        }
    }

    @OnClick(R.id.img_texte)
    public void chooseTexteStyle() {
        imgPhoto.setSelected(false);
        imgTexte.setSelected(true);
        imgColor.setSelected(false);

        View viewFocus = getCurrentFocus();
        if (viewFocus != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(viewFocus.getWindowToken(), 0);
            etStatus.clearFocus();
        }

        llTexte.setVisibility(View.VISIBLE);
        llColor.setVisibility(View.GONE);
        rlPhoto.setVisibility(View.GONE);

        if (!isOpened) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    isOpened = true;
                    vBlocage.setVisibility(View.VISIBLE);
                    erlOption.toggle();
                }
            }, 200);
        }
    }

    @OnClick(R.id.img_color)
    public void chooseColor() {
        imgPhoto.setSelected(false);
        imgTexte.setSelected(false);
        imgColor.setSelected(true);

        View viewFocus = getCurrentFocus();
        if (viewFocus != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(viewFocus.getWindowToken(), 0);
            etStatus.clearFocus();
        }

        llColor.setVisibility(View.VISIBLE);
        llTexte.setVisibility(View.GONE);
        rlPhoto.setVisibility(View.GONE);

        if (!isOpened) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    isOpened = true;
                    vBlocage.setVisibility(View.VISIBLE);
                    erlOption.toggle();
                }
            }, 200);
        }
    }

    @OnClick(R.id.ll_share_to)
    public void chooseShareTo() {
        PopupMenu popup = new PopupMenu(this, llShareTo);
        //Inflating the Popup using xml file
        popup.getMenuInflater().inflate(R.menu.menu_share_to, popup.getMenu());


        popup.show();
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.action_public) {
                    imgShareTo.setImageResource(R.drawable.ic_popular_selected);
                    isPublic = true;
                } else if (id == R.id.action_friend) {
                    imgShareTo.setImageResource(R.drawable.ic_friend_selected);
                    isPublic = false;
                }
                return false;
            }
        });
    }

    @OnClick(R.id.tv_post)
    public void createNewPost() {
        if (etStatus.getText().toString().trim().equals("")) {
            Toast.makeText(this, getString(R.string.write_status), Toast.LENGTH_SHORT).show();
        } else {
            if (connectionDetector.isConnectingToInternet()) {
                progressDialog = new ProgressDialog(this);
                progressDialog.setMessage(""+getString(R.string.Savingpost));
                progressDialog.setCancelable(false);
                progressDialog.show();

                if (hasImage) {
                    StorageReference storageAvatarRef = storageRef.child(userId + "/" + new Date().getTime() + ".jpg");
                    UploadTask uploadTask = storageAvatarRef.putFile(fileUri);

                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            savePost("");
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                            final String downloadUrl = taskSnapshot.getDownloadUrl().toString();
                            savePost(downloadUrl);
                        }
                    });
                } else {
                    savePost("");
                }

            } else {
                Toast.makeText(this, getString(R.string.conx_down), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (isOpened) {
            isOpened = false;
            vBlocage.setVisibility(View.GONE);
            erlOption.toggle();

            imgPhoto.setSelected(false);
            imgTexte.setSelected(false);
            imgColor.setSelected(false);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_normal, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void savePost(String photoUrl) {
        final String key = mPostRef.push().getKey();
        final String createdAt = format.format(new Date());
        String title = etStatus.getText().toString().trim();

        HashMap<String, Object> newPost = new HashMap<>();
        newPost.put("createdAt", createdAt);
        newPost.put("public", isPublic);
        newPost.put("title", title);
        newPost.put("postOwner", userId);
        newPost.put("location", prefs.getString("CountryName", ""));
        newPost.put("city", prefs.getString("CityName", ""));
        newPost.put("note", 0);

        if (!postColor.equals("")) {
            newPost.put("color", postColor);
        }

        if (!postFont.equals("")) {
            newPost.put("font", postFont);
        }

        if (postFontSize > 0) {
            newPost.put("fontSize", postFontSize);
        }

        if (!photoUrl.equals("")) {
            newPost.put("photo", photoUrl);
        }

        if (title.contains("#")) {
            List<String> listHashTags = getHashtagFromText(title);

            HashMap<String, Object> newTag = new HashMap<>();
            newTag.put(key, true);

            for (int i = 0; i < listHashTags.size(); i++) {
                mTagRef.child(listHashTags.get(i)).updateChildren(newTag);
            }
        }

        mPostRef.child(key).updateChildren(newPost, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    if (!isPublic) {
                        OneSignalUtil.sendNewPostPush(key, userId, MainActivity.locale);
                    }

                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.dismiss();
                            setResult(RESULT_OK);
                            finish();
                        }
                    }, 1000);
                }
            }
        });
    }

    public List<String> getHashtagFromText(String text) {
        List<String> listHashtag = new ArrayList<>();

        String[] words = text.split(" ");
        for (final String word : words) {
            if (word.startsWith("#") && word.length() > 1) {
                listHashtag.add(word.substring(1));
            }
        }
        return listHashtag;
    }

    private ArrayList<String> getAllShownImagesPath() {
        Uri uri;
        Cursor cursor;
        int column_index_data;
        ArrayList<String> listOfAllImages = new ArrayList<String>();
        String absolutePathOfImage = null;
        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = { MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME };

        cursor = getContentResolver().query(uri, projection, null, null, null);

        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);
            listOfAllImages.add(absolutePathOfImage);
        }
        return listOfAllImages;
    }

    private void setRandomBackColor() {

        Random random = new Random();
        int index = random.nextInt(18);
        switch (index) {
            case 0:
                img_color_1.animate().alpha(1f).setDuration(200).start();
                vBack.setBackgroundColor(getResources().getColor(R.color.color1));
                if (hasImage) {
                    vBack.setAlpha(0.5f);
                } else {
                    vBack.setAlpha(1f);
                }
                postColor = "9912D8";
                break;
            case 1:
                img_color_2.animate().alpha(1f).setDuration(200).start();
                vBack.setBackgroundColor(getResources().getColor(R.color.color2));
                if (hasImage) {
                    vBack.setAlpha(0.5f);
                } else {
                    vBack.setAlpha(1f);
                }
                postColor = "10B9C6";
                break;
            case 2:
                img_color_3.animate().alpha(1f).setDuration(200).start();
                vBack.setBackgroundColor(getResources().getColor(R.color.color3));
                if (hasImage) {
                    vBack.setAlpha(0.5f);
                } else {
                    vBack.setAlpha(1f);
                }
                postColor = "B56CE0";
                break;
            case 3:
                img_color_4.animate().alpha(1f).setDuration(200).start();
                vBack.setBackgroundColor(getResources().getColor(R.color.color4));
                if (hasImage) {
                    vBack.setAlpha(0.5f);
                } else {
                    vBack.setAlpha(1f);
                }
                postColor = "53DCE0";
                break;
            case 4:
                img_color_5.animate().alpha(1f).setDuration(200).start();
                vBack.setBackgroundColor(getResources().getColor(R.color.color5));
                if (hasImage) {
                    vBack.setAlpha(0.5f);
                } else {
                    vBack.setAlpha(1f);
                }
                postColor = "E5BC0E";
                break;
            case 5:
                img_color_6.animate().alpha(1f).setDuration(200).start();
                vBack.setBackgroundColor(getResources().getColor(R.color.color6));
                if (hasImage) {
                    vBack.setAlpha(0.5f);
                } else {
                    vBack.setAlpha(1f);
                }
                postColor = "077762";
                break;
            case 6:
                img_color_7.animate().alpha(1f).setDuration(200).start();
                vBack.setBackgroundColor(getResources().getColor(R.color.color7));
                if (hasImage) {
                    vBack.setAlpha(0.5f);
                } else {
                    vBack.setAlpha(1f);
                }
                postColor = "0A9B72";
                break;
            case 7:
                img_color_8.animate().alpha(1f).setDuration(200).start();
                vBack.setBackgroundColor(getResources().getColor(R.color.color8));
                if (hasImage) {
                    vBack.setAlpha(0.5f);
                } else {
                    vBack.setAlpha(1f);
                }
                postColor = "10E242";
                break;
            case 8:
                img_color_9.animate().alpha(1f).setDuration(200).start();
                vBack.setBackgroundColor(getResources().getColor(R.color.color9));
                if (hasImage) {
                    vBack.setAlpha(0.5f);
                } else {
                    vBack.setAlpha(1f);
                }
                postColor = "0A726F";
                break;
            case 9:
                img_color_10.animate().alpha(1f).setDuration(200).start();
                vBack.setBackgroundColor(getResources().getColor(R.color.color10));
                if (hasImage) {
                    vBack.setAlpha(0.5f);
                } else {
                    vBack.setAlpha(1f);
                }
                postColor = "CE0B97";
                break;
            case 10:
                img_color_11.animate().alpha(1f).setDuration(200).start();
                vBack.setBackgroundColor(getResources().getColor(R.color.color11));
                if (hasImage) {
                    vBack.setAlpha(0.5f);
                } else {
                    vBack.setAlpha(1f);
                }
                postColor = "F45922";
                break;
            case 11:
                img_color_12.animate().alpha(1f).setDuration(200).start();
                vBack.setBackgroundColor(getResources().getColor(R.color.color12));
                if (hasImage) {
                    vBack.setAlpha(0.5f);
                } else {
                    vBack.setAlpha(1f);
                }
                postColor = "83D622";
                break;
            case 12:
                img_color_13.animate().alpha(1f).setDuration(200).start();
                vBack.setBackgroundColor(getResources().getColor(R.color.color13));
                if (hasImage) {
                    vBack.setAlpha(0.5f);
                } else {
                    vBack.setAlpha(1f);
                }
                postColor = "0D9E97";
                break;
            case 13:
                img_color_14.animate().alpha(1f).setDuration(200).start();
                vBack.setBackgroundColor(getResources().getColor(R.color.color14));
                if (hasImage) {
                    vBack.setAlpha(0.5f);
                } else {
                    vBack.setAlpha(1f);
                }
                postColor = "D864C2";
                break;
            case 14:
                img_color_15.animate().alpha(1f).setDuration(200).start();
                vBack.setBackgroundColor(getResources().getColor(R.color.color15));
                if (hasImage) {
                    vBack.setAlpha(0.5f);
                } else {
                    vBack.setAlpha(1f);
                }
                postColor = "A3BFBC";
                break;
            case 15:
                img_color_16.animate().alpha(1f).setDuration(200).start();
                vBack.setBackgroundColor(getResources().getColor(R.color.color16));
                if (hasImage) {
                    vBack.setAlpha(0.5f);
                } else {
                    vBack.setAlpha(1f);
                }
                postColor = "724168";
                break;
            case 16:
                img_color_17.animate().alpha(1f).setDuration(200).start();
                vBack.setBackgroundColor(getResources().getColor(R.color.color17));
                if (hasImage) {
                    vBack.setAlpha(0.5f);
                } else {
                    vBack.setAlpha(1f);
                }
                postColor = "0AEDC1";
                break;
            case 17:
                img_color_18.animate().alpha(1f).setDuration(200).start();
                vBack.setBackgroundColor(getResources().getColor(R.color.color18));
                if (hasImage) {
                    vBack.setAlpha(0.5f);
                } else {
                    vBack.setAlpha(1f);
                }
                postColor = "0BAD7F";
                break;
        }
        hasColor = true;
        chosedpostColor = postColor;
    }

    View.OnClickListener colorClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            if (view == img_color_1) {
                img_color_1.animate().alpha(1f).setDuration(200).start();
                vBack.setBackgroundColor(getResources().getColor(R.color.color1));
                if (hasImage) {
                    vBack.setAlpha(0.5f);
                } else {
                    vBack.setAlpha(1f);
                }
                postColor = "9912D8";
            } else {
                img_color_1.animate().alpha(0f).setDuration(200).start();
            }
            if (view == img_color_2) {
                img_color_2.animate().alpha(1f).setDuration(200).start();
                vBack.setBackgroundColor(getResources().getColor(R.color.color2));
                if (hasImage) {
                    vBack.setAlpha(0.5f);
                } else {
                    vBack.setAlpha(1f);
                }
                postColor = "10B9C6";
            } else {
                img_color_2.animate().alpha(0f).setDuration(200).start();
            }
            if (view == img_color_3) {
                img_color_3.animate().alpha(1f).setDuration(200).start();
                vBack.setBackgroundColor(getResources().getColor(R.color.color3));
                if (hasImage) {
                    vBack.setAlpha(0.5f);
                } else {
                    vBack.setAlpha(1f);
                }
                postColor = "B56CE0";
            } else {
                img_color_3.animate().alpha(0f).setDuration(200).start();
            }
            if (view == img_color_4) {
                img_color_4.animate().alpha(1f).setDuration(200).start();
                vBack.setBackgroundColor(getResources().getColor(R.color.color4));
                if (hasImage) {
                    vBack.setAlpha(0.5f);
                } else {
                    vBack.setAlpha(1f);
                }
                postColor = "53DCE0";
            } else {
                img_color_4.animate().alpha(0f).setDuration(200).start();
            }
            if (view == img_color_5) {
                img_color_5.animate().alpha(1f).setDuration(200).start();
                vBack.setBackgroundColor(getResources().getColor(R.color.color5));
                if (hasImage) {
                    vBack.setAlpha(0.5f);
                } else {
                    vBack.setAlpha(1f);
                }
                postColor = "E5BC0E";
            } else {
                img_color_5.animate().alpha(0f).setDuration(200).start();
            }
            if (view == img_color_6) {
                img_color_6.animate().alpha(1f).setDuration(200).start();
                vBack.setBackgroundColor(getResources().getColor(R.color.color6));
                if (hasImage) {
                    vBack.setAlpha(0.5f);
                } else {
                    vBack.setAlpha(1f);
                }
                postColor = "077762";
            } else {
                img_color_6.animate().alpha(0f).setDuration(200).start();
            }
            if (view == img_color_7) {
                img_color_7.animate().alpha(1f).setDuration(200).start();
                vBack.setBackgroundColor(getResources().getColor(R.color.color7));
                if (hasImage) {
                    vBack.setAlpha(0.5f);
                } else {
                    vBack.setAlpha(1f);
                }
                postColor = "0A9B72";
            } else {
                img_color_7.animate().alpha(0f).setDuration(200).start();
            }
            if (view == img_color_8) {
                img_color_8.animate().alpha(1f).setDuration(200).start();
                vBack.setBackgroundColor(getResources().getColor(R.color.color8));
                if (hasImage) {
                    vBack.setAlpha(0.5f);
                } else {
                    vBack.setAlpha(1f);
                }
                postColor = "10E242";
            } else {
                img_color_8.animate().alpha(0f).setDuration(200).start();
            }
            if (view == img_color_9) {
                img_color_9.animate().alpha(1f).setDuration(200).start();
                vBack.setBackgroundColor(getResources().getColor(R.color.color9));
                if (hasImage) {
                    vBack.setAlpha(0.5f);
                } else {
                    vBack.setAlpha(1f);
                }
                postColor = "0A726F";
            } else {
                img_color_9.animate().alpha(0f).setDuration(200).start();
            }
            if (view == img_color_10) {
                img_color_10.animate().alpha(1f).setDuration(200).start();
                vBack.setBackgroundColor(getResources().getColor(R.color.color10));
                if (hasImage) {
                    vBack.setAlpha(0.5f);
                } else {
                    vBack.setAlpha(1f);
                }
                postColor = "CE0B97";
            } else {
                img_color_10.animate().alpha(0f).setDuration(200).start();
            }
            if (view == img_color_11) {
                img_color_11.animate().alpha(1f).setDuration(200).start();
                vBack.setBackgroundColor(getResources().getColor(R.color.color11));
                if (hasImage) {
                    vBack.setAlpha(0.5f);
                } else {
                    vBack.setAlpha(1f);
                }
                postColor = "F45922";
            } else {
                img_color_11.animate().alpha(0f).setDuration(200).start();
            }
            if (view == img_color_12) {
                img_color_12.animate().alpha(1f).setDuration(200).start();
                vBack.setBackgroundColor(getResources().getColor(R.color.color12));
                if (hasImage) {
                    vBack.setAlpha(0.5f);
                } else {
                    vBack.setAlpha(1f);
                }
                postColor = "83D622";
            } else {
                img_color_12.animate().alpha(0f).setDuration(200).start();
            }
            if (view == img_color_13) {
                img_color_13.animate().alpha(1f).setDuration(200).start();
                vBack.setBackgroundColor(getResources().getColor(R.color.color13));
                if (hasImage) {
                    vBack.setAlpha(0.5f);
                } else {
                    vBack.setAlpha(1f);
                }
                postColor = "0D9E97";
            } else {
                img_color_13.animate().alpha(0f).setDuration(200).start();
            }
            if (view == img_color_14) {
                img_color_14.animate().alpha(1f).setDuration(200).start();
                vBack.setBackgroundColor(getResources().getColor(R.color.color14));
                if (hasImage) {
                    vBack.setAlpha(0.5f);
                } else {
                    vBack.setAlpha(1f);
                }
                postColor = "D864C2";
            } else {
                img_color_14.animate().alpha(0f).setDuration(200).start();
            }
            if (view == img_color_15) {
                img_color_15.animate().alpha(1f).setDuration(200).start();
                vBack.setBackgroundColor(getResources().getColor(R.color.color15));
                if (hasImage) {
                    vBack.setAlpha(0.5f);
                } else {
                    vBack.setAlpha(1f);
                }
                postColor = "A3BFBC";
            } else {
                img_color_15.animate().alpha(0f).setDuration(200).start();
            }
            if (view == img_color_16) {
                img_color_16.animate().alpha(1f).setDuration(200).start();
                vBack.setBackgroundColor(getResources().getColor(R.color.color16));
                if (hasImage) {
                    vBack.setAlpha(0.5f);
                } else {
                    vBack.setAlpha(1f);
                }
                postColor = "724168";
            } else {
                img_color_16.animate().alpha(0f).setDuration(200).start();
            }
            if (view == img_color_17) {
                img_color_17.animate().alpha(1f).setDuration(200).start();
                vBack.setBackgroundColor(getResources().getColor(R.color.color17));
                if (hasImage) {
                    vBack.setAlpha(0.5f);
                } else {
                    vBack.setAlpha(1f);
                }
                postColor = "0AEDC1";
            } else {
                img_color_17.animate().alpha(0f).setDuration(200).start();
            }
            if (view == img_color_18) {
                img_color_18.animate().alpha(1f).setDuration(200).start();
                vBack.setBackgroundColor(getResources().getColor(R.color.color18));
                if (hasImage) {
                    vBack.setAlpha(0.5f);
                } else {
                    vBack.setAlpha(1f);
                }
                postColor = "0BAD7F";
            } else {
                img_color_18.animate().alpha(0f).setDuration(200).start();
            }
            hasColor = true;
            chosedpostColor = postColor;
        }
    };

    View.OnClickListener textStyleClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            if (view == tvStyle1) {
                tvStyle1.setSelected(true);
                etStatus.setTypeface(typeFace1);
                postFont = "BrushScriptStd";
            } else {
                tvStyle1.setSelected(false);
            }
            if (view == tvStyle2) {
                tvStyle2.setSelected(true);
                etStatus.setTypeface(typeFace2);
                postFont = "ComicSansMS";
            } else {
                tvStyle2.setSelected(false);
            }
            if (view == tvStyle3) {
                tvStyle3.setSelected(true);
                etStatus.setTypeface(typeFace3);
                postFont = "Optima-Regular";
            } else {
                tvStyle3.setSelected(false);
            }
            if (view == tvStyle4) {
                tvStyle4.setSelected(true);
                etStatus.setTypeface(typeFace4);
                postFont = "LevenimMT";
            } else {
                tvStyle4.setSelected(false);
            }
            if (view == tvStyle5) {
                tvStyle5.setSelected(true);
                etStatus.setTypeface(typeFace5);
                postFont = "Nyala-Regular";
            } else {
                tvStyle5.setSelected(false);
            }
            if (view == tvStyle6) {
                tvStyle6.setSelected(true);
                etStatus.setTypeface(typeFace6);
                postFont = "SegoePrint";
            } else {
                tvStyle6.setSelected(false);
            }
            if (view == tvStyle7) {
                tvStyle7.setSelected(true);
                etStatus.setTypeface(typeFace7);
                postFont = "SegoePrint-Bold";
            } else {
                tvStyle7.setSelected(false);
            }
            if (view == tvStyle8) {
                tvStyle8.setSelected(true);
                etStatus.setTypeface(typeFace8);
                postFont = "TektonPro-Bold";
            } else {
                tvStyle8.setSelected(false);
            }
            if (view == tvStyle9) {
                tvStyle9.setSelected(true);
                etStatus.setTypeface(typeFace9);
                postFont = "Vijaya";
            } else {
                tvStyle9.setSelected(false);
            }
        }
    };

    View.OnClickListener textSizeClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            if (view == tvSize1) {
                tvSize1.setSelected(true);
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                int px = Math.round(4 * (displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
                etStatus.setTextSize(px);
                postFontSize = 6;
            } else {
                tvSize1.setSelected(false);
            }
            if (view == tvSize2) {
                tvSize2.setSelected(true);
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                long px = Math.round(4.5 * (displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
                etStatus.setTextSize(px);
                postFontSize = 8;
            } else {
                tvSize2.setSelected(false);
            }
            if (view == tvSize3) {
                tvSize3.setSelected(true);
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                int px = Math.round(5 * (displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
                etStatus.setTextSize(px);
                postFontSize = 10;
            } else {
                tvSize3.setSelected(false);
            }
            if (view == tvSize4) {
                tvSize4.setSelected(true);
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                long px = Math.round(5.5 * (displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
                etStatus.setTextSize(px);
                postFontSize = 12;
            } else {
                tvSize4.setSelected(false);
            }
            if (view == tvSize5) {
                tvSize5.setSelected(true);
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                int px = Math.round(6 * (displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
                etStatus.setTextSize(px);
                postFontSize = 14;
            } else {
                tvSize5.setSelected(false);
            }
            if (view == tvSize6) {
                tvSize6.setSelected(true);
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                long px = Math.round(6.5 * (displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
                etStatus.setTextSize(px);
                postFontSize = 16;
            } else {
                tvSize6.setSelected(false);
            }
            if (view == tvSize7) {
                tvSize7.setSelected(true);
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                int px = Math.round(7 * (displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
                etStatus.setTextSize(px);
                postFontSize = 18;
            } else {
                tvSize7.setSelected(false);
            }

        }
    };

}
