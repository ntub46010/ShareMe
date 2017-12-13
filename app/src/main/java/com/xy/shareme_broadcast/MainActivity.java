package com.xy.shareme_broadcast;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.xy.shareme_broadcast.broadcast_helper.managers.RequestManager;

public class MainActivity extends Activity implements View.OnClickListener {
    private EditText edtRegId, edtSendId;
    private Button btnRegister, btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edtRegId = (EditText) findViewById(R.id.edtRegId);
        edtSendId = (EditText) findViewById(R.id.edtSendId);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnSend = (Button) findViewById(R.id.btnSend);

        btnRegister.setOnClickListener(this);
        btnSend.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnRegister:
                String id = edtRegId.getText().toString();
                RequestManager.getInstance().insertUserPushData(id);
                break;

            case R.id.btnSend:
                String sendId = edtSendId.getText().toString(); //Firebase的ID
                String title = "BENQ"; //通知標題
                String message = "Hi，你朋友希望你買來送他喔！"; //通知內容
                String photoUrl = "http://image.yipee.cc/index/2013/12/BenQ-G2F-產品圖_1-copy.jpg"; //大圖示的URL
                RequestManager.getInstance().prepareNotification(sendId, title, message, photoUrl); //開始發送推播
                break;
        }
    }
}
