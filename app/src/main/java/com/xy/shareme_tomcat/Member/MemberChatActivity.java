package com.xy.shareme_tomcat.Member;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.xy.shareme_tomcat.Product.ProductDetailActivity;
import com.xy.shareme_tomcat.R;
import com.xy.shareme_tomcat.adapter.ChatAdapter;
import com.xy.shareme_tomcat.adapter.ProductSpinnerAdapter;
import com.xy.shareme_tomcat.broadcast_helper.managers.RequestManager;
import com.xy.shareme_tomcat.data.Book;
import com.xy.shareme_tomcat.data.Chat;
import com.xy.shareme_tomcat.data.ImageObj;
import com.xy.shareme_tomcat.network_helper.GetBitmapBatch;
import com.xy.shareme_tomcat.network_helper.MyOkHttp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;

import static com.xy.shareme_tomcat.data.DataHelper.KEY_ANYWAY;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_AVATAR;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_AVATAR2;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_CHAT;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_DATE;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_HAVE_TALKED;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_MEMBER_ID;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_MESSAGE;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_NAME;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PHOTO1;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PRICE;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PRODUCT;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PRODUCTS;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PRODUCT_ID;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_RECEIVER_ID;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_SENDER_ID;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_STATUS;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_TIME;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_TITLE;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_USER_ID;
import static com.xy.shareme_tomcat.data.DataHelper.canShowChatroom;
import static com.xy.shareme_tomcat.data.DataHelper.canShowProfile;
import static com.xy.shareme_tomcat.data.DataHelper.isChatroomExist;
import static com.xy.shareme_tomcat.data.DataHelper.loginUserId;
import static com.xy.shareme_tomcat.data.DataHelper.myName;
import static com.xy.shareme_tomcat.data.DataHelper.tmpToken;

public class MemberChatActivity extends AppCompatActivity implements View.OnClickListener {
    private Context context;
    private Bundle bundle;
    private ImageView btnProfile, btnProduct;
    private Spinner spnProduct;
    private ProgressBar prgProduct, prgChat;
    private RecyclerView recyChats;
    private EditText edtMsg;
    private Button btnSubmit;

    private MyOkHttp conn;
    private GetBitmapBatch gbmAvatar;
    private ArrayList<ImageObj> chats, books;
    private ImageObj avatar;
    private ChatAdapter adpChat;
    private ProductSpinnerAdapter adpProduct;
    private String memberId, productId, title, avatar1, avatar2;

    private boolean isAvatarLoaded = false, isChatShown = false, isSpinnerInitialed = false, isContinueLoadToken = false;

    //交談訊息>個人照片>商品清單
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_chat);
        context = this;
        bundle = getIntent().getExtras();
        memberId = bundle.getString(KEY_MEMBER_ID);
        productId = bundle.getString(KEY_PRODUCT_ID);

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
        spnProduct =  (Spinner) findViewById(R.id.spnProduct);
        prgProduct = (ProgressBar) findViewById(R.id.prgProduct);
        prgChat = (ProgressBar) findViewById(R.id.prgChat);
        recyChats = (RecyclerView) findViewById(R.id.recy_chats);
        edtMsg = (EditText) findViewById(R.id.edtMsg);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);

        btnProfile.setOnClickListener(this);
        btnProduct.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);

        spnProduct.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (isSpinnerInitialed) {
                    if (isChatShown) {
                        productId = ((Book) books.get(i)).getId();
                        loadChat();
                    }
                }else
                    isSpinnerInitialed = true;
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        RequestManager.getInstance().getTokenById(memberId);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(30000);
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
                hdrTimer.sendMessage(hdrTimer.obtainMessage());
            }
        });

        canShowChatroom = false;
        isChatroomExist = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isChatShown)
            loadChatroom();
        else if (!isAvatarLoaded)
            loadAvatar();
    }

    private void loadAvatar() {
        isAvatarLoaded = false;
        avatar = new ImageObj();
        avatar.setImgURL(avatar1);
        avatar.setImgURL2(avatar2);
        gbmAvatar = new GetBitmapBatch(avatar, getString(R.string.link_avatar), new GetBitmapBatch.TaskListener() {
            @Override
            public void onFinished() {
                isAvatarLoaded = true;
                if (adpChat != null)
                    adpChat.notifyDataSetChanged();
            }
        });
        gbmAvatar.execute();
    }

    private void loadChatroom() {
        isChatShown = false;
        recyChats.setVisibility(View.INVISIBLE);
        prgProduct.setVisibility(View.VISIBLE);
        prgChat.setVisibility(View.VISIBLE);
        btnProfile.setVisibility(View.INVISIBLE);
        btnProduct.setVisibility(View.INVISIBLE);
        btnSubmit.setEnabled(false);

        books = new ArrayList<>();
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
                                avatar1 = resObj.getString(KEY_AVATAR);
                                avatar2 = resObj.getString(KEY_AVATAR2);

                                //交談商品(文字)
                                JSONArray aryProduct = resObj.getJSONArray(KEY_PRODUCTS);
                                for (int i = 0; i < aryProduct.length(); i++) {
                                    JSONObject obj = aryProduct.getJSONObject(i);
                                    books.add(new Book(
                                            obj.getString(KEY_PRODUCT_ID),
                                            obj.getString(KEY_PHOTO1),
                                            obj.getString(KEY_TITLE),
                                            obj.getString(KEY_PRICE)
                                    ));
                                }

                                //交談訊息(文字)
                                JSONObject objChat = resObj.getJSONObject(KEY_CHAT);
                                title = objChat.getString(KEY_TITLE);
                                JSONArray aryChat = objChat.getJSONArray(KEY_MESSAGE);
                                for (int i=0; i<aryChat.length(); i++) {
                                    JSONObject obj = aryChat.getJSONObject(i);
                                    chats.add(new Chat(
                                            obj.getString(KEY_SENDER_ID),
                                            URLDecoder.decode(obj.getString(KEY_MESSAGE), "UTF-8"),
                                            obj.getString(KEY_DATE),
                                            obj.getString(KEY_TIME)
                                    ));
                                }
                                //個人照片
                                loadAvatar();

                                if (!tmpToken.equals(""))
                                    showChatroomData();
                            }else {
                                Toast.makeText(context, "伺服器發生例外", Toast.LENGTH_SHORT).show();
                                prgProduct.setVisibility(View.GONE);
                                prgChat.setVisibility(View.GONE);
                            }
                        }catch (JSONException | UnsupportedEncodingException e) {
                            prgProduct.setVisibility(View.GONE);
                            prgChat.setVisibility(View.GONE);
                            Toast.makeText(context, "處理JSON發生錯誤", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        try {
            JSONObject reqObj = new JSONObject();
            reqObj.put(KEY_USER_ID, loginUserId);
            reqObj.put(KEY_MEMBER_ID, memberId);
            reqObj.put(KEY_PRODUCT_ID, productId);
            conn.execute(getString(R.string.link_show_chatroom), reqObj.toString());
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void loadChat() {
        isChatShown = false;
        btnProfile.setVisibility(View.INVISIBLE);
        btnProduct.setVisibility(View.INVISIBLE);
        btnSubmit.setEnabled(false);
        recyChats.setVisibility(View.INVISIBLE);
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
                                JSONObject objChat = resObj.getJSONObject(KEY_CHAT);
                                title = objChat.getString(KEY_TITLE);
                                JSONArray aryChat = objChat.getJSONArray(KEY_MESSAGE);
                                for (int i = 0; i < aryChat.length(); i++) {
                                    JSONObject obj = aryChat.getJSONObject(i);
                                    chats.add(new Chat(
                                            obj.getString(KEY_SENDER_ID),
                                            URLDecoder.decode(obj.getString(KEY_MESSAGE), "UTF-8"),
                                            obj.getString(KEY_DATE),
                                            obj.getString(KEY_TIME)
                                    ));
                                }
                                showChatData();
                            }else {
                                prgProduct.setVisibility(View.GONE);
                                prgChat.setVisibility(View.GONE);
                                Toast.makeText(context, "伺服器發生例外", Toast.LENGTH_SHORT).show();
                            }
                        }catch (JSONException | UnsupportedEncodingException e) {
                            prgProduct.setVisibility(View.GONE);
                            prgChat.setVisibility(View.GONE);
                            Toast.makeText(context, "處理JSON發生錯誤", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        try {
            JSONObject reqObj = new JSONObject();
            reqObj.put(KEY_USER_ID, loginUserId);
            reqObj.put(KEY_MEMBER_ID, memberId);
            reqObj.put(KEY_PRODUCT_ID, productId);
            conn.execute(getString(R.string.link_list_message), reqObj.toString());
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showChatroomData() {
        //交談商品清單
        if (!bundle.getBoolean(KEY_HAVE_TALKED)) { //檢查有無談過這個商品，若無則在Spinner放入
            books.add((Book) bundle.getSerializable(KEY_PRODUCT));
        }

        //將正在談的商品移動到清單第一個
        for (int i = 0; i < books.size(); i++) {
            if (((Book) books.get(i)).getId().equals(productId)) {
                books.add(0, books.get(i));
                books.remove(i + 1);
            }
        }

        adpProduct = new ProductSpinnerAdapter(getResources(), context, books, R.layout.lst_stock, 10);
        spnProduct.setAdapter(adpProduct);


        //交談訊息
        showChatData();
    }

    private void showChatData() {
        recyChats.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyChats.setLayoutManager(linearLayoutManager);
        btnSubmit.setEnabled(true);
        adpChat = new ChatAdapter(chats, avatar);
        recyChats.setAdapter(adpChat);

        prgProduct.setVisibility(View.GONE);
        prgChat.setVisibility(View.GONE);
        btnProduct.setVisibility(View.VISIBLE);
        btnProfile.setVisibility(View.VISIBLE);
        recyChats.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(true);
        isChatShown = true;
    }

    private void sendMessage(final String msg) {
        conn = new MyOkHttp(MemberChatActivity.this, new MyOkHttp.TaskListener() {
            @Override
            public void onFinished(final String result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject resObj = new JSONObject(result);
                            if (resObj.getBoolean(KEY_STATUS)) {
                                //發送推播
                                RequestManager.getInstance().prepareNotification(memberId, myName, msg, getString(R.string.link_avatar) + avatar1);
                                edtMsg.setText("");
                                loadChat();
                            }else {
                                Toast.makeText(context, "傳送失敗", Toast.LENGTH_SHORT).show();
                            }
                            btnSubmit.setEnabled(true);
                        }catch (JSONException e) {
                            Toast.makeText(context, "處理JSON發生錯誤", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        try {
            JSONObject reqObj = new JSONObject();
            reqObj.put(KEY_SENDER_ID, loginUserId);
            reqObj.put(KEY_RECEIVER_ID, memberId);
            reqObj.put(KEY_PRODUCT_ID, productId);
            reqObj.put(KEY_MESSAGE, URLEncoder.encode(msg, "UTF-8"));
            conn.execute(getString(R.string.link_send_message), reqObj.toString());
        }catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        Intent it;
        Bundle bundle;
        switch (view.getId()) {
            case R.id.btnProfile:
                if (canShowProfile) {
                    it = new Intent(context, MemberProfileActivity.class);
                    bundle = new Bundle();
                    bundle.putString(KEY_MEMBER_ID, memberId);
                    it.putExtras(bundle);
                    startActivity(it);
                }else
                    Toast.makeText(context, "您先前已開啟該賣家的個人檔案，不可重複開啟", Toast.LENGTH_SHORT).show();
                break;

            case R.id.btnProduct:
                it = new Intent(context, ProductDetailActivity.class);
                bundle = new Bundle();
                bundle.putString(KEY_PRODUCT_ID, productId);
                bundle.putString(KEY_TITLE, title);
                bundle.putString(KEY_ANYWAY, "1");
                it.putExtras(bundle);
                startActivity(it);
                break;

            case R.id.btnSubmit:
                String msg = edtMsg.getText().toString();
                if (!msg.equals("")) {
                    btnSubmit.setEnabled(false);
                    sendMessage(msg);
                }
                break;
        }
    }

    @Override
    public void onPause() {
        cancelConnection();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        conn = null;
        if (adpProduct != null) {
            adpProduct.destroy(true);
            adpProduct = null;
        }

        adpChat = null;
        avatar = null;
        canShowChatroom = true;
        isChatroomExist = false;

        System.gc();
        super.onDestroy();
    }

    private void cancelConnection() {
        if (conn != null)
            conn.cancel();
        if (gbmAvatar != null)
            gbmAvatar.cancel(true);
    }

    private void initTrdWaitToken() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(250);
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
                hdrWaitToken.sendMessage(hdrWaitToken.obtainMessage());
            }
        }).start();
    }

    private Handler hdrWaitToken = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (!tmpToken.equals("")) {
                showChatroomData();
                tmpToken = "";
            }else {
                if (isContinueLoadToken)
                    initTrdWaitToken();
                else{
                    tmpToken = "";
                    prgProduct.setVisibility(View.GONE);
                    prgChat.setVisibility(View.GONE);
                    Toast.makeText(context, "無法取得對方Token，請再試一次", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    private Handler hdrTimer = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            isContinueLoadToken = false;
        }
    };
}
