package com.example.smstestapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
//import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;


public class MainActivity extends AppCompatActivity{
    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an app-defined int constant.
    // The callback method gets the result of the request.
    static final int  MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private ChatArrayAdapter chatArrayAdapter;
    private ListView chatListView;
    private EditText MessageEditText;
    private EditText receiverEditText;
    private LinearLayout QuickReplyLinearLayout;
    private boolean quickReplyHide = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button sendBtn = findViewById(R.id.sendBtn);
        MessageEditText = findViewById(R.id.MessageEditText);
        receiverEditText = findViewById(R.id.receiverEditText);
        chatListView = findViewById(R.id.chatListView);
        QuickReplyLinearLayout = findViewById(R.id.QuickReplyLinearLayout);
        QuickReplyLinearLayout.setVisibility(View.GONE);

        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.right_self);
        chatListView.setAdapter(chatArrayAdapter);

        //Send permission and Read permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS )
                != PackageManager.PERMISSION_GRANTED | ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS )
                        != PackageManager.PERMISSION_GRANTED | ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE )
                        != PackageManager.PERMISSION_GRANTED) {
                //request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS,Manifest.permission.READ_SMS,Manifest.permission.RECEIVE_SMS,Manifest.permission.READ_PHONE_STATE},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        }

        //receive message
        MessageReceiver receiver = new MessageReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle b = intent.getExtras();
                String messageIntent = b.getString("messageIntent");
                sendChatMessage(true, messageIntent);
            }
        };
        registerReceiver(receiver, new IntentFilter("sendABroadCast"));

        //彈出或收起QuickReplyLinearLayout
        MessageEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    quickReplyHide = true;
                } else {
                    quickReplyHide = false;
                }
            }
        });

        //根據鍵盤顯示或隱藏狀態，彈出或收起QuickReplyLinearLayout
        SoftKeyBoardListener.setListener(this, new SoftKeyBoardListener.OnSoftKeyBoardChangeListener() {
            @Override
            public void keyBoardShow(int height) {
                //Toast.makeText(getApplicationContext(), "键盘显示 高度" + height, Toast.LENGTH_SHORT).show();
                if(quickReplyHide == false)
                    QuickReplyLinearLayout.setVisibility(View.VISIBLE);
            }
            @Override
            public void keyBoardHide(int height) {
                //Toast.makeText(getApplicationContext(), "键盘隐藏 高度" + height, Toast.LENGTH_SHORT).show();
                QuickReplyLinearLayout.setVisibility(View.GONE);
            }
        });

        //Send message
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNo = receiverEditText.getText().toString();
                String message = MessageEditText.getText().toString();

                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phoneNo, null, message, null, null);
                    //Toast.makeText(getApplicationContext(), "SMS sent.", Toast.LENGTH_LONG).show();
                    sendChatMessage(false, MessageEditText.getText().toString());
                    MessageEditText.setText("");
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "SMS failed, please try again.", Toast.LENGTH_LONG).show();
                }
            }
        });

        chatListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        chatListView.setAdapter(chatArrayAdapter);

        //to scroll the list view to bottom on data change
        chatArrayAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                chatListView.setSelection(chatArrayAdapter.getCount() - 1);
            }
        });
    }

    //receive message
    private boolean sendChatMessage(boolean side, String messageIntent) {
        chatArrayAdapter.add(new ChatMessage(side, messageIntent));
        UIUtil.hideKeyboard(this);
        return true;
    }

    // permission was granted
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.SEND_SMS)){
                        Toast.makeText(this, "Permission granted.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Permission denied.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

}
