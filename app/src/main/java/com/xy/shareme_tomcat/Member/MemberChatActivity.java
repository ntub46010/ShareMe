package com.xy.shareme_tomcat.Member;

import android.content.Context;
import android.content.Intent;
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

import com.xy.shareme_tomcat.Product.ProductDetailActivity;
import com.xy.shareme_tomcat.R;
import com.xy.shareme_tomcat.adapter.ChatAdapter;
import com.xy.shareme_tomcat.adapter.ProductSpinnerAdapter;
import com.xy.shareme_tomcat.data.Book;
import com.xy.shareme_tomcat.data.Chat;
import com.xy.shareme_tomcat.data.ImageObj;
import com.xy.shareme_tomcat.network_helper.GetBitmap;
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
import static com.xy.shareme_tomcat.data.DataHelper.KEY_MEMBER_ID;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_MESSAGE;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_NAME;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PHOTO1;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PRICE;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PRODUCTS;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PRODUCT_ID;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_SENDER_ID;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_STATUS;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_TIME;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_TITLE;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_USER_ID;
import static com.xy.shareme_tomcat.data.DataHelper.loginUserId;
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
    private GetBitmap gbmProduct;
    private GetBitmapBatch gbmAvatar;
    private ArrayList<ImageObj> chats, books;
    private ImageObj avatar;
    private ChatAdapter adpChat;
    private ProductSpinnerAdapter adpProduct;
    private String memberId, productId;

    private boolean isProductShown = false, isAvatarLoaded = false, isChatShown = false;

    //交談訊息>個人照片>商品清單
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

        memberId = bundle.getString(KEY_MEMBER_ID);
        productId = bundle.getString(KEY_PRODUCT_ID);
        loadChatroom();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void loadAvatar() {
        avatar = new ImageObj();
        avatar.setImgURL(myAvatarUrl);
        avatar.setImgURL2(bundle.getString(KEY_AVATAR));
        gbmAvatar = new GetBitmapBatch(avatar, getString(R.string.link_avatar), new GetBitmapBatch.TaskListener() {
            @Override
            public void onFinished() {
                isAvatarLoaded = true;
                adpChat = new ChatAdapter(chats, avatar);
                recyChats.setAdapter(adpChat);
            }
        });
        gbmAvatar.execute();
    }

    private void loadChat(JSONArray aryChat) throws JSONException, UnsupportedEncodingException{
        chats = new ArrayList<>();
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
    }

    private void loadChatroom() {
        recyChats.setVisibility(View.GONE);
        prgChat.setVisibility(View.VISIBLE);
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
                                myAvatarUrl = resObj.getString(KEY_AVATAR);

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
                                //loadChat(resObj.getJSONArray(KEY_MESSAGE));

                                //個人照片
                                loadAvatar();

                                //交談商品(圖片)


                            }else {
                                Toast.makeText(context, "STATUS FALSE", Toast.LENGTH_SHORT).show();
                                //showFoundStatus(chats, imageView, textView, "伺服器發生例外");
                            }
                        }catch (JSONException | UnsupportedEncodingException e) {
                            e.printStackTrace();
                            //prgChat.setVisibility(View.GONE);
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

    private void showChatData() {
        //交談訊息
        recyChats.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyChats.setLayoutManager(linearLayoutManager);

        adpChat = new ChatAdapter(chats, avatar);
        recyChats.setAdapter(adpChat);
        recyChats.setVisibility(View.VISIBLE);

        //交談商品
        adpProduct = new ProductSpinnerAdapter(context, books, R.layout.spn_chat_product);
        spnProduct.setAdapter(adpProduct);

        btnProduct.setVisibility(View.VISIBLE);
        btnProfile.setVisibility(View.VISIBLE);
        isChatShown = true;
        prgChat.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        Intent it;
        Bundle bundle;
        switch (view.getId()) {
            case R.id.btnProfile:
                it = new Intent(context, MemberProfileActivity.class);
                bundle = new Bundle();
                bundle.putString(KEY_MEMBER_ID, memberId);
                it.putExtras(bundle);
                startActivity(it);
                break;

            case R.id.btnProduct:
                it = new Intent(context, ProductDetailActivity.class);
                bundle = new Bundle();
                bundle.putString(KEY_PRODUCT_ID, productId);
                bundle.putString(KEY_TITLE, "標題");
                it.putExtras(bundle);
                startActivity(it);
                break;

            case R.id.btnSubmit:

                break;
        }
    }
}
