package com.xy.shareme_tomcat.Member;

import android.content.Context;
import android.media.Image;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.xy.shareme_tomcat.R;
import com.xy.shareme_tomcat.adapter.ProductDisplayAdapter;
import com.xy.shareme_tomcat.data.Book;
import com.xy.shareme_tomcat.data.ImageObj;
import com.xy.shareme_tomcat.network_helper.MyOkHttp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.xy.shareme_tomcat.data.DataHelper.KEY_FAVORITE;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PHOTO1;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PRICE;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PRODUCTS;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PRODUCT_ID;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_SELLER_NAME;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_STATUS;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_TITLE;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_USER_ID;
import static com.xy.shareme_tomcat.data.DataHelper.loginUserId;
import static com.xy.shareme_tomcat.data.DataHelper.showFoundStatus;

public class MemberFavoriteActivity extends AppCompatActivity {
    private Context context;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar prgBar;

    private ArrayList<ImageObj> books;
    private ProductDisplayAdapter adapter;

    private MyOkHttp conn;
    private boolean isShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_favorite);
        context = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("我的最愛");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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

        books = new ArrayList<>();
        conn = new MyOkHttp(MemberFavoriteActivity.this, new MyOkHttp.TaskListener() {
            @Override
            public void onFinished(final String result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result == null) {
                            Toast.makeText(context, "連線失敗", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        ImageView imageView = (ImageView) findViewById(R.id.imgNotFound);
                        TextView textView = (TextView) findViewById(R.id.txtNotFound);
                        try {
                            JSONObject resObj = new JSONObject(result);
                            if (resObj.getBoolean(KEY_STATUS)) {
                                JSONArray ary = resObj.getJSONArray(KEY_PRODUCTS);
                                for (int i=0; i<ary.length(); i++) {
                                    JSONObject obj = ary.getJSONObject(i);
                                    books.add(new Book(
                                            obj.getString(KEY_PRODUCT_ID),
                                            obj.getString(KEY_PHOTO1),
                                            obj.getString(KEY_TITLE),
                                            obj.getString(KEY_PRICE),
                                            obj.getString(KEY_SELLER_NAME)
                                    ));
                                }
                                showFoundStatus(books, imageView, textView, "");
                                showData();
                                prgBar.setVisibility(View.GONE);
                            }else {
                                prgBar.setVisibility(View.GONE);
                                showFoundStatus(books, imageView, textView, "沒有最愛的商品");
                            }
                        }catch (JSONException e) {
                            prgBar.setVisibility(View.GONE);
                            showFoundStatus(books, imageView, textView, "伺服器發生例外");
                        }
                    }
                });
            }
        });
        //開始連線
        try {
            JSONObject reqObj = new JSONObject();
            reqObj.put(KEY_USER_ID, loginUserId);
            conn.execute(getString(R.string.link_list_favorite), reqObj.toString());
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showData() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);

        adapter = new ProductDisplayAdapter(getResources(), context, books, 10);
        adapter.setBackgroundColor(getResources(), R.color.card_favorite);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setEnabled(true);
        swipeRefreshLayout.setRefreshing(false);

        books = null;
        isShown = true;
    }

    @Override
    public void onPause() {
        cancelConnection();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (adapter != null) {
            adapter.destroy(true);
            adapter = null;
        }
        System.gc();
        super.onDestroy();
    }

    private void cancelConnection() {
        try {
            conn.cancel();
        }catch (NullPointerException e) {}
    }
}
