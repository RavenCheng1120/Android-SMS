package com.example.smstestapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class MessageReceiver extends BroadcastReceiver {
    String TAG = "Receive message";
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        SmsMessage[] msgs = null;
        String message = "";
        Log.d(TAG, "true");
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle .get("pdus");
            msgs = new SmsMessage[pdus.length];

            for(int i = 0; i < msgs.length; i++) {
                msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                message = msgs[i].getMessageBody();
                //Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }

            Intent i = new Intent("sendABroadCast");
            i.putExtra("messageIntent", message);
            context.sendBroadcast(i);
        }
    }

}