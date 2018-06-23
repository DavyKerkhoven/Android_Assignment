package com.company.scott.smsapp2;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener
{

    //set default lat/long to Wellington
    double longitude = 174.77557;
    double latitude = -41.28664;
    double receivedLongitude = 0;
    double receivedLatitude = 0;
    String receivedPhoneNumber = "Unknown";

    EditText editMessage;
    EditText editPhoneNum;
    TextView receivedMessages;
    int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;

    String SENT = "SMS_SENT";
    String DELIVERED = "SMS_DELIVERED";
    PendingIntent sentIntent;
    PendingIntent deliveredIntent;
    BroadcastReceiver smsSentReceiver;
    BroadcastReceiver smsDeliveredReceiver;

    private BroadcastReceiver mMessageReceiver;
    private FusedLocationProviderClient mFusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editMessage = findViewById(R.id.editMessage);
        editPhoneNum = findViewById(R.id.editPhoneNum);
        receivedMessages = findViewById(R.id.receivedMessages);

        sentIntent = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
        deliveredIntent = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, 1);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, 1);
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        setLocation();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        smsSentReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(MainActivity.this, "SMS Sent!", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(MainActivity.this, "Generic failure", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(MainActivity.this, "No service", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(MainActivity.this, "No PDU", Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(MainActivity.this, "Radio off", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        };

        smsDeliveredReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(MainActivity.this, "SMS delivered!", Toast.LENGTH_SHORT).show();
                        break;
                    case Activity.RESULT_CANCELED:
                        Toast.makeText(MainActivity.this, "SMS not delivered", Toast.LENGTH_SHORT).show();
                        break;

                }
            }
        };

        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Get extra data included in the Intent
                String message = intent.getStringExtra("message");
                receivedMessages.setText(receivedMessages.getText() + "\n" + message);

                if (message.contains("My Location is: ") && message.contains("["))
                {
                    String[] splitMessage = message.split(":");

                    receivedPhoneNumber = splitMessage[0].split(" ")[1];
                    String[] coordinates = splitMessage[3].split("]");
                    receivedLongitude = Double.parseDouble(coordinates[1].replaceAll("[^0-9.-]", ""));
                    receivedLatitude = Double.parseDouble(coordinates[0].replaceAll("[^0-9.-]", ""));

                    EditText longText = findViewById(R.id.longText);
                    longText.setText(Double.toString(receivedLongitude));

                    EditText latText = findViewById(R.id.latText);
                    latText.setText(Double.toString(receivedLatitude));
                }
            }
        };

        // Listens for the broadcast, and when receives, it will do the onReceive method above
        registerReceiver(smsSentReceiver, new IntentFilter(SENT));
        registerReceiver(smsDeliveredReceiver, new IntentFilter(DELIVERED));
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("custom-event-name"));
    }

    @Override
    protected void onPause() {
        super.onPause();

        // when we go away from the app, turn of the registers
        unregisterReceiver(smsSentReceiver);
        unregisterReceiver(smsDeliveredReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    public void btn_SendSMS_OnClick(View v) {
        String message = editMessage.getText().toString();
        String phoneNumber = editPhoneNum.getText().toString();

        if (!TextUtils.isEmpty(phoneNumber.trim()))
        {
            if (!TextUtils.isEmpty(message.trim()))
            {
                // Checks if the permission HASN'T been granted
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
                {
                    // This will popup a system message to request access
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
                }
                else
                {
                    SmsManager sms = SmsManager.getDefault();
                    // When the send part of the message is done it will activate the sentIntent, which will Broadcast SENT. Same with Delivered
                    sms.sendTextMessage(phoneNumber, null, message, sentIntent, deliveredIntent);
                    receivedMessages.setText(receivedMessages.getText() + "\nSending to " + phoneNumber + ": " + message);
                }
            }
            else
            {
                Toast.makeText(this, "Please put something in the message box!", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Toast.makeText(this, "Please enter a phone number.", Toast.LENGTH_SHORT).show();
        }

    }

    public void btn_SendLocation_OnClick(View v) {
        String phoneNumber = editPhoneNum.getText().toString();

        //Check if phone number is not empty
        if (!TextUtils.isEmpty(phoneNumber.trim())) {
            // Checks if the permission HASN'T been granted
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)
            {
                // This will popup a system message to request access
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
            else
            {
                SmsManager sms = SmsManager.getDefault();
                // When the send part of the message is done it will activate the sentIntent, which will Broadcast SENT. Same with Delivered
                sms.sendTextMessage(phoneNumber, null, getLocation(), sentIntent, deliveredIntent);
                receivedMessages.setText(receivedMessages.getText() + "\nSending to " + phoneNumber + ": " + getLocation());
            }
        }
        else
        {
            Toast.makeText(this, "Please enter a phone number.", Toast.LENGTH_SHORT).show();
        }
    }

    private String getLocation()
    {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        //Toast.makeText(this, "Latitude: "+latitude, Toast.LENGTH_SHORT).show();
        try
        {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if ( addresses.isEmpty())
            {
                return "My Location is: No location found";
            }
            else {
                String cityName = addresses.get(0).getAddressLine(0);

                return "My Location is: " + cityName + " : [" + latitude + "][" + longitude + "]";
            }
        }
        catch (Exception e)
        {
            return "My Location is: Error finding location. Please try again.";
        }
    }

    private void setLocation()
    {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(this, "Requesting location permission..", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(this, "Failed to get permission!", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(this, "Saving current location..", Toast.LENGTH_SHORT).show();
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {

                            // Got last known location. In some rare situations this can be null.
                            if (location != null)
                            {
                                longitude = location.getLongitude();
                                latitude = location.getLatitude();
                                receivedMessages.setText(receivedMessages.getText() + "\n Lat/Long:" + latitude + ":" + longitude);
                            }
                        }
                    });
        }

    }

    @Override
    public void onLocationChanged(Location location)
    {
        setLocation();
    }

    public void viewMapActivity(View view)
    {
        //get the lat/long text fields to check if they are not empty
        EditText latText = findViewById(R.id.latText);
        EditText longText = findViewById(R.id.longText);

        String lat = latText.getText().toString().trim();
        String lng = longText.getText().toString().trim();

        if (lat.equals("") || lng.equals(""))
        {
            Toast.makeText(this, "Please enter numbers in the Lat & Long fields.", Toast.LENGTH_SHORT).show();

        }
        else
        {
            Intent myIntent = new Intent(this, MapsActivity.class);
            myIntent.putExtra("latitude", Double.parseDouble(latText.getText().toString()));
            myIntent.putExtra("longitude", Double.parseDouble(longText.getText().toString()));
            myIntent.putExtra("phoneNumber", receivedPhoneNumber);
            this.startActivity(myIntent);
        }

    }
}
