package com.example.muneeb.smssendreceive;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private EditText etPhoneNumber, etMsg ;
    private Button btnSendSMS ;

    private ListView lvMsgs ;
    ArrayList<String> listOfSms = new ArrayList<String>() ;

    ArrayAdapter arrayAdapter ;
    public static MainActivity instance ;

    private final static int REQUEST_PERMISSION_CODE_SEND = 123 ;
    private final static int REQUEST_PERMISSION_CODE_READ = 456 ;

    public static MainActivity Instance()
    {
        return instance ;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        instance = this ;

        etPhoneNumber = (EditText) findViewById(R.id.et_phoneNumber) ;
        etMsg = (EditText)findViewById(R.id.et_msg) ;

        btnSendSMS = (Button) findViewById(R.id.btn_SendSMS) ;

        btnSendSMS.setEnabled(false) ;

        //Set ListView for messages
        lvMsgs = (ListView)findViewById(R.id.lv_msgs) ;
        arrayAdapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                listOfSms) ;
        lvMsgs.setAdapter(arrayAdapter) ;

        if (checkPermission(Manifest.permission.SEND_SMS))
        {
            btnSendSMS.setEnabled(true);
        }
        else
        {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[] {(Manifest.permission.SEND_SMS)},
                    REQUEST_PERMISSION_CODE_SEND);
        }

        if (checkPermission(Manifest.permission.READ_SMS))
        {
            refreshInbox() ;
        }
        else
        {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[] {(Manifest.permission.READ_SMS)},
                    REQUEST_PERMISSION_CODE_READ);
        }

        btnSendSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String phoneNumber = etPhoneNumber.getText().toString() ;
                String message = etMsg.getText().toString() ;

                SmsManager smsManager = SmsManager.getDefault() ;
                smsManager.sendTextMessage(phoneNumber,null,
                        message, null, null) ;

                Toast.makeText(MainActivity.this,
                        "Message sent to " + phoneNumber,
                        Toast.LENGTH_LONG).show() ;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case REQUEST_PERMISSION_CODE_SEND :
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                btnSendSMS.setEnabled(true);
            }
            break ;
        }
    }

    public void readInbox(View view)
    {
        refreshInbox() ;
    }

    private void refreshInbox()
    {
        ContentResolver contentResolver = getContentResolver() ;

        Cursor cursor = contentResolver.query(Uri.parse("content://sms/inbox"),
                null, null,
                null,null
        ) ;

        int indexBody = cursor.getColumnIndex("body") ;
        int indexAddress = cursor.getColumnIndex("address") ;

        if (indexBody < 0 || !cursor.moveToFirst()) return ;

        arrayAdapter.clear() ;
        do
        {
            String result = "Message From: " +
                    cursor.getString(indexAddress) + "\n"  ;
            result += cursor.getString(indexBody) ;

            arrayAdapter.add(result) ;
        }
        while (cursor.moveToNext()) ;
    }

    private boolean checkPermission(String permission)
    {
        int checkPermission = ContextCompat.checkSelfPermission(this,
                permission) ;
        return checkPermission == PackageManager.PERMISSION_GRANTED ;
    }

    public void updateList(final String smsMsg)
    {
        arrayAdapter.insert(smsMsg, 0);
        arrayAdapter.notifyDataSetChanged();
    }
}
