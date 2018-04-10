package com.xy.shareme_tomcat.Member;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.xy.shareme_tomcat.R;
import com.xy.shareme_tomcat.adapter.ChatAdapter;
import com.xy.shareme_tomcat.data.Chat;
import com.xy.shareme_tomcat.data.ImageObj;
import com.xy.shareme_tomcat.network_helper.GetBitmapBatch;
import com.xy.shareme_tomcat.network_helper.MyOkHttp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

import static com.xy.shareme_tomcat.data.DataHelper.KEY_AVATAR;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_DATE;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_MESSAGE;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_NAME;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_SENDER_ID;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_STATUS;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_TIME;
import static com.xy.shareme_tomcat.data.DataHelper.myAvatarUrl;

public class MemberChatActivity extends AppCompatActivity implements View.OnClickListener {
    private Context context;
    private Bundle bundle;
    private ImageView btnProfile, btnProduct;
    private Spinner spnProduct;
    private ProgressBar prgProduct, prgChat;
    private FrameLayout layChatField;
    private RecyclerView recyChats;
    private EditText edtMsg;

    private MyOkHttp conn;
    private ArrayList<ImageObj> chats;
    private ImageObj avatar;

    private boolean isAvatarLoaded = false, isChatShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_chat);
        context = this;
        bundle = getIntent().getExtras();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView txtBarTitle = (TextView) findViewById(R.id.txtToolbarTitle);
        txtBarTitle.setText(bundle.getString(KEY_NAME));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnProfile = (ImageView) findViewById(R.id.btnProfile);
        btnProduct = (ImageView) findViewById(R.id.btnProduct);
        layChatField = (FrameLayout) findViewById(R.id.layChatField);
        spnProduct =  (Spinner) findViewById(R.id.spnProduct);
        prgProduct = (ProgressBar) findViewById(R.id.prgProduct);
        recyChats = (RecyclerView) findViewById(R.id.recy_chats);
        prgChat = (ProgressBar) findViewById(R.id.prgChat);
        edtMsg = (EditText) findViewById(R.id.edtMsg);
        Button btnSubmit = (Button) findViewById(R.id.btnSubmit);

        btnProfile.setOnClickListener(this);
        btnProduct.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isAvatarLoaded)
            loadAvatar();
        else if (!isChatShown)
            loadChatData();
    }

    private void loadAvatar() {
        avatar = new ImageObj();
        avatar.setImgURL(myAvatarUrl);
        avatar.setImgURL2(bundle.getString(KEY_AVATAR));
        GetBitmapBatch getBitmap = new GetBitmapBatch(avatar, getString(R.string.link_avatar), new GetBitmapBatch.TaskListener() {
            @Override
            public void onFinished() {
                isAvatarLoaded = true;
                loadChatData();
            }
        });
        getBitmap.execute();
    }

    private void loadChatData() {
        isChatShown = false;
        recyChats.setVisibility(View.GONE);
        prgChat.setVisibility(View.VISIBLE);
        chats = new ArrayList<>();

        conn = new MyOkHttp(MemberChatActivity.this, new MyOkHttp.TaskListener() {
            @Override
            public void onFinished(final String result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result == null) {
                            Toast.makeText(context, "連線失敗", Toast.LENGTH_SHORT).show();
                            prgChat.setVisibility(View.GONE);
                            return;
                        }
                        try {
                            JSONObject resObj = new JSONObject(result);
                            if (resObj.getBoolean(KEY_STATUS)) {
                                JSONArray aryChat = resObj.getJSONArray(KEY_MESSAGE);
                                for (int i=0; i<aryChat.length(); i++) {
                                    JSONObject obj = aryChat.getJSONObject(i);
                                    chats.add(new Chat(
                                            obj.getString(KEY_SENDER_ID),
                                            URLDecoder.decode(obj.getString(KEY_MESSAGE), "UTF-8"),
                                            obj.getString(KEY_DATE),
                                            obj.getString(KEY_DATE),
                                            obj.getString(KEY_TIME)
                                    ));
                                }
                                showChatData();
                            }else {
                                prgChat.setVisibility(View.GONE);
                            }

                        }catch (JSONException | UnsupportedEncodingException e) {
                            prgChat.setVisibility(View.GONE);
                            //showFoundStatus(chats, imageView, textView, "伺服器發生例外");
                        }
                    }
                });
            }
        });
    }

    private void showChatData() {
        recyChats.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyChats.setLayoutManager(linearLayoutManager);

        ChatAdapter adapter = new ChatAdapter(chats, avatar);
        recyChats.setAdapter(adapter);
        //adapter.notifyDataSetChanged();

        recyChats.setVisibility(View.VISIBLE);
        btnProduct.setVisibility(View.VISIBLE);
        btnProfile.setVisibility(View.VISIBLE);
        isChatShown = true;
        prgChat.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnProfile:
                break;
            case R.id.btnProduct:
                break;
            case R.id.btnSubmit:

                break;
        }
    }
}
