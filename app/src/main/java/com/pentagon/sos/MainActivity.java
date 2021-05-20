package com.pentagon.sos;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private GpsTracker gpsTracker;
    private Button mBtn;
    private EditText mEdit;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 44;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permissionLocation();
        permissionSMS();
        mBtn = findViewById(R.id.btn_sos);
        mEdit = findViewById(R.id.edit);
        mBtn.setOnClickListener(view -> sos());
    }

    private void permissionLocation(){
        // Location
        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e){
            Log.d(TAG, "init: Permission Failed" + e.getMessage());
            Toast.makeText(gpsTracker, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void permissionSMS(){
        // SMS
        try {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.SEND_SMS)) {
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS},
                            MY_PERMISSIONS_REQUEST_SEND_SMS);
                }
            }
        }catch (Exception e){
            Log.d(TAG, "init: SMS exception: " + e.getMessage());
        }
    }

    private void sos(){
        hideKeyBoard();
        String phoneNum = mEdit.getText().toString().trim();
        if (phoneNum.isEmpty()) {
            Toast.makeText(this, "Enter Phone Number", Toast.LENGTH_SHORT).show();
            return;
        }
        MyLatLng myLatLng = getMyLatLang();
        String url = "http://maps.google.com/maps?q=" + myLatLng.getLatitude() + "," + myLatLng.getLongitude();
        sendSMS(phoneNum, "My Location: " + url);
    }

    private MyLatLng getMyLatLang(){
        gpsTracker = new GpsTracker(MainActivity.this);
        if(gpsTracker.canGetLocation()){
            double latitude = gpsTracker.getLatitude();
            double longitude = gpsTracker.getLongitude();
            return new MyLatLng(latitude, longitude);
        }else{
            Log.d(TAG, "getMyLatLang: Failed");
            gpsTracker.showSettingsAlert();
            return new MyLatLng(0.0, 0.0);
        }
    }

    private void sendSMS(String phone, String message){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED){
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phone, null, message, null, null);
            Toast.makeText(getApplicationContext(), "message sent successfully! ", Toast.LENGTH_LONG).show();
        }else permissionSMS();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permission granted! Try again", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "SMS failed, please try again.", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }

    }

    private void hideKeyBoard(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}