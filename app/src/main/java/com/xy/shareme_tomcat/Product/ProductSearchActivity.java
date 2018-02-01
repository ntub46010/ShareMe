package com.xy.shareme_tomcat.Product;

import android.content.Context;
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
import com.xy.shareme_tomcat.network_helper.GetBitmap;
import com.xy.shareme_tomcat.network_helper.MyOkHttp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.xy.shareme_tomcat.MainActivity.board;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_KEYWORD;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PHOTO1;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PRICE;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PRODUCTS;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PRODUCT_ID;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_SELLER_NAME;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_STATUS;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_TITLE;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_TYPE;
import static com.xy.shareme_tomcat.data.DataHelper.getNotFoundImg;

public class ProductSearchActivity extends AppCompatActivity {
    private Context context;
    private ProgressBar prgBar;
    private RecyclerView recyProduct;
    private String keyword;

    private ArrayList<ImageObj> books;
    private ProductDisplayAdapter adapter;

    private MyOkHttp conn;
    private GetBitmap getBitmap;
    private boolean isShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_search);
        context = this;
        keyword = getIntent().getExtras().getString(KEY_KEYWORD);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.hint_search_result, keyword));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        recyProduct = (RecyclerView) findViewById(R.id.recyclerView);
        prgBar = (ProgressBar) findViewById(R.id.prgBar);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!isShown)
            loadData(keyword);
    }

    private void loadData(String keyword) {
        isShown = false;
        prgBar.setVisibility(View.VISIBLE);
        recyProduct.setVisibility(View.GONE);

        books = new ArrayList<>();
        conn = new MyOkHttp(ProductSearchActivity.this, new MyOkHttp.TaskListener() {
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
                                getBitmap = new GetBitmap(context, books, getString(R.string.link_image), new GetBitmap.TaskListener() {
                                    @Override
                                    public void onFinished() {
                                        showData();
                                    }
                                });
                                getBitmap.setPreLoadAmount(-1);
                                getBitmap.execute();
                            }else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, "沒有找到商品", Toast.LENGTH_SHORT).show();
                                        showFoundStatus();
                                        prgBar.setVisibility(View.GONE);
                                    }
                                });
                            }
                        }catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        //開始連線
        try {
            JSONObject reqObj = new JSONObject();
            reqObj.put(KEY_TYPE, board);
            reqObj.put(KEY_KEYWORD, keyword);
            conn.execute(getString(R.string.link_list_product), reqObj.toString());
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showData() {
        recyProduct.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyProduct.setLayoutManager(linearLayoutManager);

        adapter = new ProductDisplayAdapter(getResources(), context, books);
        adapter.setBackgroundColor(getResources(), R.color.card_product);
        recyProduct.setAdapter(adapter);
        books = null;

        prgBar.setVisibility(View.GONE);
        recyProduct.setVisibility(View.VISIBLE);
        isShown = true;
    }

    private void showFoundStatus() {
        //若未找到最愛的書，則說明沒有找到
        TextView txtNotFound = (TextView) findViewById(R.id.txtNotFound);
        ImageView imgNotFound = (ImageView) findViewById(R.id.imgNotFound);
        if (books == null || books.isEmpty()) {
            txtNotFound.setText("沒有找到商品");
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
        System.gc();
        super.onDestroy();
    }

    private void cancelConnection() {
        try {
            conn.cancel();
        }catch (NullPointerException e) {}
        try {
            getBitmap.cancel(true);
        }catch (NullPointerException e) {}
    }
}
