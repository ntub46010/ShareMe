package com.xy.shareme_tomcat.Member;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.xy.shareme_tomcat.R;
import com.xy.shareme_tomcat.adapter.MailListAdapter;
import com.xy.shareme_tomcat.data.Chat;
import com.xy.shareme_tomcat.data.ImageObj;
import com.xy.shareme_tomcat.network_helper.MyOkHttp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

import static com.xy.shareme_tomcat.data.DataHelper.KEY_AVATAR;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_DATE;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_HAVE_TALKED;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_MAILS;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_MEMBER_ID;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_MESSAGE;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_NAME;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PRODUCT_ID;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_SELLER_ID;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_SELLER_NAME;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_STATUS;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_TIME;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_TITLE;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_USER_ID;
import static com.xy.shareme_tomcat.data.DataHelper.canShowMailbox;
import static com.xy.shareme_tomcat.data.DataHelper.isMailboxExist;
import static com.xy.shareme_tomcat.data.DataHelper.loginUserId;
import static com.xy.shareme_tomcat.data.DataHelper.myGender;
import static com.xy.shareme_tomcat.data.DataHelper.myName;
import static com.xy.shareme_tomcat.data.DataHelper.showFoundStatus;

public class MemberMailboxActivity extends AppCompatActivity {
    private Context context;

    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar prgBar;

    private ArrayList<ImageObj> chats;
    private MailListAdapter adapter;

    private MyOkHttp conn;
    private boolean isShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_mailbox);
        context = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("信箱");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.destroy(false);
                loadData();
            }
        });

        prgBar = (ProgressBar) findViewById(R.id.prgBar);

        SharedPreferences sp = getSharedPreferences(getString(R.string.sp_fileName), MODE_PRIVATE);
        if (sp.getBoolean(getString(R.string.sp_isFromNotification), false)) {
            loginUserId = sp.getString(getString(R.string.sp_myLoginUserId), "");
            myName = sp.getString(getString(R.string.sp_myName), "");
            myGender = sp.getInt(getString(R.string.sp_myGender), -1);
            sp.edit().putBoolean(getString(R.string.sp_isFromNotification), false).apply();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isMailboxExist = true;
        canShowMailbox = false;
        if (!isShown)
            loadData();
    }

    private void loadData() {
        isShown = false;
        prgBar.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setEnabled(false);

        chats = new ArrayList<>();
        conn = new MyOkHttp(MemberMailboxActivity.this, new MyOkHttp.TaskListener() {
            @Override
            public void onFinished(final String result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result == null) {
                            Toast.makeText(context, "連線失敗", Toast.LENGTH_SHORT).show();
                            prgBar.setVisibility(View.GONE);
                            return;
                        }
                        ImageView imageView = (ImageView) findViewById(R.id.imgNotFound);
                        TextView textView = (TextView) findViewById(R.id.txtNotFound);
                        try {
                            JSONObject resObj = new JSONObject(result);
                            if (resObj.getBoolean(KEY_STATUS)) {
                                JSONArray ary = resObj.getJSONArray(KEY_MAILS);
                                for (int i=0; i<ary.length(); i++) {
                                    JSONObject obj = ary.getJSONObject(i);
                                    chats.add(new Chat(
                                            obj.getString(KEY_AVATAR),
                                            obj.getString(KEY_SELLER_NAME),
                                            URLDecoder.decode(obj.getString(KEY_MESSAGE), "UTF-8"),
                                            obj.getString(KEY_DATE),
                                            obj.getString(KEY_TIME),
                                            obj.getString(KEY_PRODUCT_ID),
                                            obj.getString(KEY_TITLE),
                                            obj.getString(KEY_SELLER_ID)
                                    ));
                                }
                                showFoundStatus(chats, imageView, textView, "");
                                showData();
                            }else {
                                prgBar.setVisibility(View.GONE);
                                showFoundStatus(chats, imageView, textView, "沒有您的訊息");
                            }
                        }catch (JSONException | UnsupportedEncodingException e) {
                            prgBar.setVisibility(View.GONE);
                            showFoundStatus(chats, imageView, textView, "伺服器發生例外");
                        }
                    }
                });
            }
        });
        try {
            JSONObject reqObj = new JSONObject();
            reqObj.put(KEY_USER_ID, loginUserId);
            conn.execute(getString(R.string.link_list_mails), reqObj.toString());
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showData() {
        adapter = new MailListAdapter(getResources(), context, chats, 10);
        ListView listView = (ListView) findViewById(R.id.lstMails);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Chat chat = (Chat) adapter.getItem(i);
                Intent it = new Intent(context, MemberChatActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(KEY_MEMBER_ID, chat.getMemberId());
                bundle.putString(KEY_NAME, chat.getName());
                bundle.putString(KEY_PRODUCT_ID, chat.getProductId());
                //bundle.putString(KEY_TITLE, chat.getTitle());
                bundle.putBoolean(KEY_HAVE_TALKED, true);
                it.putExtras(bundle);
                startActivity(it);
            }
        });

        swipeRefreshLayout.setEnabled(true);
        swipeRefreshLayout.setRefreshing(false);
        prgBar.setVisibility(View.GONE);
        chats = null;
        isShown = true;
    }

    @Override
    public void onPause() {
        cancelConnection();
        isMailboxExist = false;
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (adapter != null) {
            adapter.destroy(true);
            adapter = null;
        }
        canShowMailbox = true;
        System.gc();
        super.onDestroy();
    }

    private void cancelConnection() {
        if (conn != null)
            conn.cancel();

    }
}
