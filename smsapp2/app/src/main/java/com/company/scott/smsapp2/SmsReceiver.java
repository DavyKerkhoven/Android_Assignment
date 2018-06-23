package com.company.scott.smsapp2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        String phoneNumber = null;
        String message = null;
        Bundle bundle = intent.getExtras();

        if (bundle != null)
        {
            Object[] pdus = (Object[]) bundle.get("pdus");

            for (int i = 0; i < pdus.length; i++)
            {
                SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdus[i]);

                phoneNumber = sms.getOriginatingAddress();
                message = sms.getDisplayMessageBody();

                //Toast.makeText(context, "From: " + phoneNumber + " Message: " + message, Toast.LENGTH_LONG).show();
                Log.d("sender", "Broadcasting message");
                Intent intent2 = new Intent("custom-event-name");

                intent2.putExtra("message", "From " + phoneNumber + ": " + message);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent2);
            }
        }


    }
}