package com.xy.shareme_tomcat.Member;

import android.content.Context;
import android.content.Intent;
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

import java.util.ArrayList;

import static com.xy.shareme_tomcat.data.DataHelper.KEY_AVATAR;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_DATE;
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
import static com.xy.shareme_tomcat.data.DataHelper.getNotFoundImg;
import static com.xy.shareme_tomcat.data.DataHelper.loginUserId;

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
                loadData();
            }
        });

        prgBar = (ProgressBar) findViewById(R.id.prgBar);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isShown)
            loadData();
    }

    private void loadData() {
        isShown = false;
        swipeRefreshLayout.setEnabled(false);
        prgBar.setVisibility(View.VISIBLE);

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
                        try {
                            JSONObject resObj = new JSONObject(result);
                            if (resObj.getBoolean(KEY_STATUS)) {
                                JSONArray ary = resObj.getJSONArray(KEY_MAILS);
                                for (int i=0; i<ary.length(); i++) {
                                    JSONObject obj = ary.getJSONObject(i);
                                    chats.add(new Chat(
                                            obj.getString(KEY_AVATAR),
                                            obj.getString(KEY_SELLER_NAME),
                                            obj.getString(KEY_MESSAGE),
                                            obj.getString(KEY_DATE),
                                            obj.getString(KEY_TIME),
                                            obj.getString(KEY_PRODUCT_ID),
                                            obj.getString(KEY_SELLER_ID)
                                    ));
                                }
                                showData();
                                /*getBitmap = new GetBitmap(context, chats, getString(R.string.link_avatar), new GetBitmap.TaskListener() {
                                    @Override
                                    public void onFinished() {
                                        showData();
                                    }
                                });
                                getBitmap.execute();*/
                            }else {
                                Toast.makeText(context, "沒有您的訊息", Toast.LENGTH_SHORT).show();
                                showFoundStatus();
                                prgBar.setVisibility(View.GONE);
                            }
                        }catch (JSONException e) {
                            Toast.makeText(context, "伺服器發生例外", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
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
                bundle.putString(KEY_MEMBER_ID, chat.getMember());
                bundle.putString(KEY_NAME, chat.getName());
                bundle.putString(KEY_AVATAR, chat.getImgURL());
                bundle.putString(KEY_PRODUCT_ID, chat.getProduct());
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

    private void showFoundStatus() {
        //若未找到書，則說明沒有找到
        TextView txtNotFound = (TextView) findViewById(R.id.txtNotFound);
        ImageView imgNotFound = (ImageView) findViewById(R.id.imgNotFound);
        if (chats == null || chats.isEmpty()) {
            txtNotFound.setText("此商品已被下架");
            txtNotFound.setVisibility(View.VISIBLE);
            imgNotFound.setImageResource(getNotFoundImg());
            imgNotFound.setVisibility(View.VISIBLE);
        }else {
            txtNotFound.setText("");
            txtNotFound.setVisibility(View.GONE);
            imgNotFound.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        cancelConnection();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        adapter.destroy(true);
        adapter = null;
        System.gc();
        super.onDestroy();
    }

    private void cancelConnection() {
        try {
            conn.cancel();
        }catch (NullPointerException e) {}
    }
}
