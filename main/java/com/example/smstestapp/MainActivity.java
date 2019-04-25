package com.example.smstestapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.Call;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;


public class MainActivity extends AppCompatActivity{
    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an app-defined int constant.
    // The callback method gets the result of the request.
    static final int  MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private ChatArrayAdapter chatArrayAdapter;
    private ListView chatListView;
    private EditText MessageEditText;
    private EditText receiverEditText;
    private LinearLayout QuickReplyLinearLayout;
    private Button replyBtn1;
    private Button replyBtn2;
    private Button replyBtn3;
    private boolean quickReplyHide = true;
    private boolean quickReplyHideKB = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button sendBtn = findViewById(R.id.sendBtn);
        replyBtn1 = findViewById(R.id.replyBtn1);
        replyBtn2 = findViewById(R.id.replyBtn2);
        replyBtn3 = findViewById(R.id.replyBtn3);
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

                ///////////socket///////////////
                //sendToPython sendcode = new sendToPython();
                //sendcode.execute(messageIntent);

                //initView();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String url = "http://192.168.2.39:8000/getCode/";
                            URL urltest = new URL(url);
                            //得到connection对象。
                            HttpURLConnection connection = (HttpURLConnection) urltest.openConnection();
                            //设置请求方式
                            connection.setRequestMethod("GET");
                            //连接
                            connection.connect();
                            //得到响应码
                            int responseCode = connection.getResponseCode();
                            if(responseCode == HttpURLConnection.HTTP_OK){
                                //得到响应流
                                InputStream inputStream = connection.getInputStream();
                                //将响应流转换成字符串
                                String result = is2String(inputStream);//将流转换为字符串。
                                Log.d("kwwl","result============="+result);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d("kwwl","ERRRRRRRROR!!!!");
                        }
                    }
                }).start();
            }
        };
        registerReceiver(receiver, new IntentFilter("sendABroadCast"));

        //根據edit_text focus與否，彈出或收起QuickReplyLinearLayout
        MessageEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    quickReplyHide = true;
                    QuickReplyLinearLayout.setVisibility(View.GONE);
                } else {
                    quickReplyHide = false;
                    if(quickReplyHideKB == false)
                        QuickReplyLinearLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        //根據鍵盤顯示或隱藏狀態，彈出或收起QuickReplyLinearLayout
        SoftKeyBoardListener.setListener(this, new SoftKeyBoardListener.OnSoftKeyBoardChangeListener() {
            @Override
            public void keyBoardShow(int height) {
                quickReplyHideKB = false;
                //Toast.makeText(getApplicationContext(), "键盘显示 高度" + height, Toast.LENGTH_SHORT).show();
                if(quickReplyHide == false)
                    QuickReplyLinearLayout.setVisibility(View.VISIBLE);
            }
            @Override
            public void keyBoardHide(int height) {
                quickReplyHideKB = true;
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

    ///////////socket///////////////
    //和python server做連接
//    class sendToPython extends AsyncTask<String,Void,Void>{
//        @Override
//        protected Void doInBackground(String...message){
//            try {
//                Socket s = new Socket("192.168.2.39", 8001);//注意host改成你服务器的hostname或IP地址
//                PrintWriter out = new PrintWriter(s.getOutputStream());
//                out.write(message[0]);//傳送字串
//                out.flush();
//                out.close();
//                //BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
//                //String inMsg = in.readLine();
//                //Toast.makeText(getApplicationContext(), inMsg, Toast.LENGTH_SHORT).show();
//                s.close();
//            } catch (UnknownHostException e) {
//                e.printStackTrace();
//                Toast.makeText(getApplicationContext(), "ERROR:"+e, Toast.LENGTH_SHORT).show();
//            } catch (IOException e) {
//                e.printStackTrace();
//                Toast.makeText(getApplicationContext(), "ERROR:"+e, Toast.LENGTH_SHORT).show();
//            }
//            return null;
//        }
//    }


    //receive message
    private boolean sendChatMessage(boolean side, String messageIntent) {
        chatArrayAdapter.add(new ChatMessage(side, messageIntent));
        UIUtil.hideKeyboard(this);
        return true;
    }

    //按下快速回覆按鈕，將字串複製到message edit text
    public void ReplyButtonClicked(View v){
        switch(v.getId()){
            case R.id.replyBtn1:
                MessageEditText.setText(replyBtn1.getText());
                MessageEditText.setSelection(MessageEditText.getText().length());
                break;
            case R.id.replyBtn2:
                MessageEditText.setText(replyBtn2.getText());;
                MessageEditText.setSelection(MessageEditText.getText().length());
                break;
            case R.id.replyBtn3:
                MessageEditText.setText(replyBtn3.getText());
                MessageEditText.setSelection(MessageEditText.getText().length());
                break;
        }

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


    //is2String處理
    public static String is2String(InputStream is, int bufferSize,String encoding)
            throws IOException {
        Reader reader = new InputStreamReader(is, encoding);
        char[] buffer = new char[bufferSize];
        StringBuilder out = new StringBuilder();

        int rsz;
        while ((rsz = reader.read(buffer, 0, bufferSize)) >= 0) {
            out.append(buffer, 0, rsz);
        }

        return out.toString();
    }

    public static String is2String(InputStream is) throws IOException {
        return is2String(is, 50, "UTF-8");
    }
}
