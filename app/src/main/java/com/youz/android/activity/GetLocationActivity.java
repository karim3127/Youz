package com.youz.android.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Button;

import com.youz.android.R;
import com.youz.android.service.GPSTracker;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by hp on 26/01/2017.
 */
public class GetLocationActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 22;

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("com.youz.android", Context.MODE_PRIVATE);
        editor = prefs.edit();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            getCurrentLocation();
            // Toast.makeText(getApplicationContext(),""+city+" "+locale,Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

         if (PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION == requestCode) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                getCurrentLocation();
            }
        }
    }

    void getCurrentLocation() {

        double longitude;
        double latitude;

        GPSTracker tracker = new GPSTracker(GetLocationActivity.this);
        if (!tracker.canGetLocation()) {

            showSettingsAlert();
            //Toast.makeText(getApplicationContext(), getResources().getString(R.string.repeat_after_setting), Toast.LENGTH_LONG).show();

        } else {

            String cityName = "";
            String countryName = "";

            latitude = tracker.getLatitude();
            longitude = tracker.getLongitude();

            if (latitude != 0.0) {
                editor.putString("Latitude", latitude + "");
                editor.commit();
            } else {
                try {
                    latitude = Double.parseDouble(prefs.getString("Latitude", "0.0"));
                } catch (Exception e) {
                    latitude = 0.0;
                }
            }
            if (longitude != 0.0) {
                editor.putString("Longitude", longitude + "");
                editor.commit();
            } else {
                try {
                    longitude = Double.parseDouble(prefs.getString("Longitude", "0.0"));
                } catch (Exception e) {
                    longitude = 0.0;
                }
            }

            Geocoder geocoder = new Geocoder(GetLocationActivity.this, Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);

            } catch (IOException e) {
                e.printStackTrace();
            }

            if (addresses != null && addresses.size() > 0) {

                cityName = addresses.get(0).getLocality();
                if (cityName == null)
                    cityName = "";

                try {
                    countryName = ", " + addresses.get(0).getCountryName();
                } catch (Exception e) {
                    countryName = "";
                }

                if (!TextUtils.isEmpty(cityName) && cityName != null) {
                    editor.putString("CityName", cityName);
                    editor.commit();
                } else {
                    cityName = prefs.getString("CityName", "");
                }

                if (!TextUtils.isEmpty(countryName) && countryName != null) {
                    editor.putString("CountryName", countryName);
                    editor.commit();
                } else {
                    try {
                        countryName = prefs.getString("CountryName", "");
                        if (countryName == null)
                            countryName = "";
                    } catch (Exception e) {
                        countryName = "";
                    }
                }

            } else {

                try {
                    countryName = prefs.getString("CountryName", "");
                    if (countryName == null)
                        countryName = "";
                } catch (Exception e) {
                    countryName = "";
                }


                try {
                    cityName = prefs.getString("CityName", "");
                    if (cityName == null)
                        cityName = "";
                } catch (Exception e) {
                    cityName = "";
                }

            }

            MainActivity.city = cityName;
            MainActivity.locale = countryName;
        }
    }

    private void showSettingsAlert() {

        final Context mContext = GetLocationActivity.this;

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(""+mContext.getResources().getString(R.string.gps_in_setting));
        // Setting Dialog Message
        builder.setMessage(""+mContext.getResources().getString(R.string.gps_not_work));

        builder.setPositiveButton(""+mContext.getResources().getString(R.string.Settings), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //do your work here
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });
        builder.setNegativeButton(mContext.getResources().getString(R.string.Cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        AlertDialog alert = builder.create();
        alert.show();
        alert.getWindow().getAttributes();

        Button btnNegatif = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        btnNegatif.setTextColor(mContext.getResources().getColor(R.color.colorRed));

        Button btnPositif = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        btnPositif.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
    }
}
