package com.youz.android.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;
import com.youz.android.R;
import com.youz.android.adapter.CodeCountryPopUpItemAdapter;
import com.youz.android.model.Country;
import com.youz.android.util.ConnectionDetector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SignUp extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.tv_toolbar)
    TextView tvToolbar;

    @BindView(R.id.switcher_signup)
    ViewSwitcher switcherSignup;

    @BindView(R.id.tv_next)
    TextView tvNext;

    @BindView(R.id.tv_code_contry)
    TextView tvCodeContry;

    @BindView(R.id.tv_header_code)
    TextView tvHeaderCode;

    @BindView(R.id.et_phone_number)
    EditText etPhoneNumber;

    @BindView(R.id.et_code_validation)
    EditText etCodeValidation;

    @BindView(R.id.rl_loading)
    RelativeLayout rlLoading;

    int currentSwitchView = 0;

    List<Country> mCountriesList = new ArrayList<Country>();
    public static String codeCountryTel;
    private AlertDialog.Builder builderCodeCountry;
    private CodeCountryPopUpItemAdapter ccPopUpItemAdapter;
    private Typeface typeFaceGras;
    Random random = new Random();
    ConnectionDetector connectionDetector;

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private String oneSignalUserId = "";

    String phoneNumber;
    String codeConfirm;
    public static String country;

    FirebaseDatabase mRoot = FirebaseDatabase.getInstance();
    DatabaseReference mUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ButterKnife.bind(this);

        connectionDetector = new ConnectionDetector(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        prefs = getSharedPreferences("com.youz.android", Context.MODE_PRIVATE);
        editor = prefs.edit();

        typeFaceGras = Typeface.createFromAsset(getAssets(), "fonts/optima_bold.ttf");
        tvToolbar.setTypeface(typeFaceGras);

        etPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (etPhoneNumber.getText().length() > 0) {
                    tvNext.setBackgroundResource(R.drawable.back_rounded_yallow);
                } else {
                    tvNext.setBackgroundResource(R.drawable.back_rounded_disable);
                }
            }
        });

        etCodeValidation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (etCodeValidation.getText().length() > 0) {
                    tvNext.setBackgroundResource(R.drawable.back_rounded_yallow);
                } else {
                    tvNext.setBackgroundResource(R.drawable.back_rounded_disable);
                }
            }
        });

        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String userId, String registrationId) {
                Log.d("debug", "User:" + userId);
                if (registrationId != null)
                    oneSignalUserId = userId;
            }
        });

        phoneNumber = prefs.getString("PhoneNumber", "");
        codeConfirm = prefs.getString("CodeConfirm", "");

        if (!codeConfirm.equals("")) {
            switcherSignup.showNext();
            currentSwitchView = 1;
            tvHeaderCode.setText("Code sent to :\n+" + phoneNumber);
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    tvNext.setBackgroundResource(R.drawable.back_rounded_disable);
                    tvToolbar.setText("Verification");
                }
            }, 300);
        }

        (new CountryAsyncTask(this)).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @OnClick(R.id.tv_next)
    public void nextClick() {
        if (connectionDetector.isConnectingToInternet()) {
            if (etPhoneNumber.getText().length() > 0 && currentSwitchView == 0) {
                switcherSignup.showNext();
                currentSwitchView = 1;

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tvNext.setBackgroundResource(R.drawable.back_rounded_disable);
                        tvToolbar.setText("Verification");
                    }
                }, 300);

                phoneNumber = codeCountryTel + " " + etPhoneNumber.getText().toString();
                if(phoneNumber.startsWith("972") && !phoneNumber.startsWith("972 0") && !phoneNumber.startsWith("9720")){
                    phoneNumber = phoneNumber.replace("972","9720");
                }
                codeConfirm = generateRandomString();

                tvHeaderCode.setText("Code sent to :\n+" + phoneNumber);

                Toast.makeText(this, codeConfirm, Toast.LENGTH_SHORT).show();
                editor.putString("PhoneNumber", phoneNumber);
                editor.putString("CodeConfirm", codeConfirm);
                editor.putString("CodeCountry", codeCountryTel);

                editor.commit();

            } else if (currentSwitchView == 1) {
                String codeEntered = etCodeValidation.getText().toString();

                if (codeConfirm.equals(codeEntered)) {
                    rlLoading.setVisibility(View.VISIBLE);
                    String[] numberParts = phoneNumber.split(" ");
                    String number = numberParts[0] + numberParts[1];
                    searchUser(number);
                } else {
                    Toast.makeText(this, getString(R.string.Incorrect_code), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, getString(R.string.conx_down), Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.rl_loading)
    public void blockClick() {

    }

    @OnClick(R.id.tv_reset_phone)
    public void resetPhone() {
        editor.remove("PhoneNumber");
        editor.remove("CodeConfirm");

        editor.commit();
        etPhoneNumber.setText("");
        etCodeValidation.setText("");

        switcherSignup.showNext();
        currentSwitchView = 0;
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                tvNext.setBackgroundResource(R.drawable.back_rounded_disable);
                tvToolbar.setText("Phone number");
            }
        }, 300);
    }

    @OnClick(R.id.tv_code_contry)
    public void chooseCodeContry() {
        builderCodeCountry = new AlertDialog.Builder(this);
        builderCodeCountry.setCancelable(true);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.layout_code_contry_popup, null);
        builderCodeCountry.setView(dialogView);

        AlertDialog alert = builderCodeCountry.create();
        alert.show();

        RecyclerView rvCc = (RecyclerView) dialogView.findViewById(R.id.rvListCc);
        ccPopUpItemAdapter = new CodeCountryPopUpItemAdapter(this, alert, mCountriesList, tvCodeContry);

        rvCc.setLayoutManager(new LinearLayoutManager(this));
        rvCc.setAdapter(ccPopUpItemAdapter);
        rvCc.setItemViewCacheSize(600);
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

    private void searchUser(final String phoneNumber) {

        mUserRef = mRoot.getReference("users").child(phoneNumber);

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    HashMap<String, Object> details = (HashMap<String, Object>) dataSnapshot.getValue();
                    final String key = dataSnapshot.getKey();

                    final boolean notifsChats = (details.get("notifsChats") != null) ? (boolean) details.get("notifsChats") : true;
                    final boolean notifsComments = (details.get("notifsComments") != null) ? (boolean) details.get("notifsComments") : true;
                    final boolean notifsLikes = (details.get("notifsLikes") != null) ? (boolean) details.get("notifsLikes") : true;
                    final boolean notifsPosts = (details.get("notifsPosts") != null) ? (boolean) details.get("notifsPosts") : true;
                    final boolean notifsShares = (details.get("notifsShares") != null) ? (boolean) details.get("notifsShares") : true;
                    final boolean enablePublicChat = (details.get("enablePublicChat") != null) ? (boolean) details.get("enablePublicChat") : true;

                    HashMap<String, Object> result = new HashMap<>();
                    result.put("status", "offline");
                    result.put("location", country);
                    result.put("oneSignalUserId", oneSignalUserId);
                    result.put("notifsChats", notifsChats);
                    result.put("notifsComments", notifsComments);
                    result.put("notifsLikes", notifsLikes);
                    result.put("notifsPosts", notifsPosts);
                    result.put("notifsShares", notifsShares);
                    result.put("enablePublicChat", enablePublicChat);

                    editor.putString("CountryName", country);
                    editor.commit();

                    mUserRef.updateChildren(result, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseReference != null) {

                                editor.putBoolean("FirstShow", false);
                                editor.putBoolean("notifsChats", notifsChats);
                                editor.putBoolean("notifsComments", notifsComments);
                                editor.putBoolean("notifsLikes", notifsLikes);
                                editor.putBoolean("notifsPosts", notifsPosts);
                                editor.putBoolean("notifsShares", notifsShares);
                                editor.putBoolean("enablePublicChat", enablePublicChat);
                                editor.putString("UserId", key);
                                editor.remove("CodeConfirm");
                                editor.commit();

                                Intent intent = new Intent(SignUp.this, MainActivity.class);
                                startActivity(intent);
                                IntroAnimation.finishActivity();
                                finish();

                            }
                        }
                    });

                } else {
                    Toast.makeText(SignUp.this, "no user", Toast.LENGTH_SHORT).show();

                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
                    format.setTimeZone(TimeZone.getDefault());
                    String date_createdAt = format.format(new Date());

                    HashMap<String, Object> result = new HashMap<>();
                    result.put("phoneNumber", phoneNumber);
                    result.put("status", "online");
                    result.put("location", country);
                    result.put("createdAt", date_createdAt);
                    result.put("oneSignalUserId", oneSignalUserId);
                    result.put("notifsChats", true);
                    result.put("notifsComments", true);
                    result.put("notifsLikes", true);
                    result.put("notifsPosts", true);
                    result.put("notifsShares", true);
                    result.put("enablePublicChat", true);

                    editor.putString("CountryName", country);
                    editor.commit();

                    Map<String, Object> childUpdates = new HashMap<>();
                    childUpdates.put("/" + phoneNumber, result);

                    mRoot.getReference("users").updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseReference != null) {

                                editor.putBoolean("FirstShow", false);
                                editor.putBoolean("notifsChats", true);
                                editor.putBoolean("notifsComments", true);
                                editor.putBoolean("notifsLikes", true);
                                editor.putBoolean("notifsPosts", true);
                                editor.putBoolean("notifsShares", true);
                                editor.putBoolean("enablePublicChat", true);
                                editor.putString("UserId", phoneNumber);
                                editor.remove("CodeConfirm");
                                editor.commit();

                                Intent intent = new Intent(SignUp.this, MainActivity.class);
                                startActivity(intent);
                                IntroAnimation.finishActivity();
                                finish();

                            }
                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mUserRef.addListenerForSingleValueEvent(valueEventListener);
    }

    public String generateRandomString() {
        char[] values = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        String out = "";

        for (int i = 0; i < 5; i++) {
            int index=random.nextInt(values.length);
            out += values[index];
        }

        return out;
    }

    public static String getLocalCountry(Context context){
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String countryCode = tm.getSimCountryIso();
        if(countryCode.equals("")){
            String networkCountry = tm.getNetworkCountryIso();
            if (networkCountry != null && networkCountry.length() == 2) {
                countryCode = networkCountry.toLowerCase(Locale.US);
            } else {
                countryCode = context.getResources().getConfiguration().locale.getCountry();
            }
        }
        return countryCode;
    }

    protected class CountryAsyncTask extends AsyncTask<Void, Void, ArrayList<Country>> {

        private Context mContext;
        String cc;

        public CountryAsyncTask(Context context) {
            mContext = context;
        }

        @Override
        protected ArrayList<Country> doInBackground(Void... params) {
            cc = getLocalCountry(mContext);

            ArrayList<Country> data = new ArrayList<Country>(233);
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(mContext.getApplicationContext().getAssets().open("countries.dat"), "UTF-8"));

                // do reading, usually loop until end of file reading
                String line;
                int i = 0;
                while ((line = reader.readLine()) != null) {
                    //process line
                    Country c = new Country(mContext, line, i);
                    mCountriesList.add(c);

                    if (c.getCountryISO().equals(cc)) {
                        codeCountryTel = c.getCountryCode() + "";
                        country = c.getName();
                    }

                    i++;
                }
            } catch (IOException e) {
                //log the exception
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        //log the exception
                    }
                }
            }

            return data;
        }

        @Override
        protected void onPostExecute(ArrayList<Country> data) {
            tvCodeContry.setText("+" + codeCountryTel);

        }
    }
}
