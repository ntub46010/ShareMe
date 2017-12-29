package com.xy.shareme_tomcat.Member;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.xy.shareme_tomcat.Product.ProductDetailActivity;
import com.xy.shareme_tomcat.R;
import com.xy.shareme_tomcat.adapter.StockListAdapter;
import com.xy.shareme_tomcat.data.Book;
import com.xy.shareme_tomcat.data.ImageObj;
import com.xy.shareme_tomcat.network_helper.GetBitmap;
import com.xy.shareme_tomcat.network_helper.MyOkHttp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.xy.shareme_tomcat.data.DataHelper.KEY_PHOTO1;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PRICE;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PRODUCT_ID;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_STATUS;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_STOCK;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_TITLE;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_USER_ID;
import static com.xy.shareme_tomcat.data.DataHelper.getNotFoundImg;
import static com.xy.shareme_tomcat.data.DataHelper.getSimpleAdapter;
import static com.xy.shareme_tomcat.data.DataHelper.loginUserId;
import static com.xy.shareme_tomcat.data.DataHelper.isStockDisplayAlive;

public class MemberStockActivity extends AppCompatActivity {
    private Context context;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView lstProduct;
    private ProgressBar prgBar;


    private ArrayList<ImageObj> books;
    private StockListAdapter stockAdapter;

    private Dialog dialog;
    private String bookId, bookTitle;

    private MyOkHttp conLoadStock, conDropProduct;
    private GetBitmap getBitmap;
    private boolean isShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_stock);
        context = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("商品管理");
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

        lstProduct = (ListView) findViewById(R.id.lstProduct);
        lstProduct.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Book book = (Book) stockAdapter.getItem(position);
                bookId = book.getId();
                bookTitle = book.getTitle();
                TextView textView = (TextView) dialog.findViewById(R.id.txtBookTitle);
                textView.setText(bookTitle);
                dialog.show();
            }
        });

        prgBar = (ProgressBar) findViewById(R.id.prgBar);
        prepareDialog();
    }

    @Override
    public void onResume() {
        super.onResume();
        isStockDisplayAlive = true;
        if (!isShown)
            loadData();

        try {
            stockAdapter.initCheckThread(true);
        }catch (NullPointerException e) {
            //第一次開啟，adapter尚未準備好
        }
    }

    private void prepareDialog() {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dlg_stock_mgt);
        dialog.setCancelable(true);

        String[] textGroup = {"查看", "編輯", "下架"};
        int[] iconGroup = {
                R.drawable.icon_see,
                R.drawable.icon_edit,
                R.drawable.icon_delete
        };

        ListView listView = (ListView) dialog.findViewById(R.id.lstStockMgt);
        listView.setAdapter(getSimpleAdapter(
                context,
                R.layout.lst_text_with_icon_black,
                R.id.imgIcon,
                R.id.txtTitle,
                iconGroup,
                textGroup
        ));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent it = null;
                Bundle bundle = new Bundle();
                switch (position) {
                    case 0:
                        it = new Intent(context, ProductDetailActivity.class);
                        bundle.putString(KEY_PRODUCT_ID, bookId);
                        bundle.putString(KEY_TITLE, bookTitle);
                        it.putExtras(bundle);
                        startActivity(it);
                        break;
                    case 1:
                        it = new Intent(context, ProductEditActivity.class);
                        bundle.putString(KEY_PRODUCT_ID, bookId);
                        it.putExtras(bundle);
                        startActivity(it);
                        break;
                    case 2:
                        AlertDialog.Builder msgbox = new AlertDialog.Builder(context);
                        msgbox.setTitle("下架商品")
                                .setMessage(getString(R.string.hint_drop_product, bookTitle))
                                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dropProduct(bookId);
                                    }
                                })
                                .setNegativeButton("取消", null)
                                .show();
                        break;
                    case 3:
                        break;
                }
                dialog.dismiss();
            }
        });
    }

    private void loadData() {
        swipeRefreshLayout.setEnabled(false);
        prgBar.setVisibility(View.VISIBLE);
        lstProduct.setVisibility(View.GONE);

        books = new ArrayList<>();
        conLoadStock = new MyOkHttp(MemberStockActivity.this, new MyOkHttp.TaskListener() {
            @Override
            public void onFinished(final String result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result == null) {
                            Toast.makeText(context, "連線失敗", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        try {
                            JSONObject resObj = new JSONObject(result);
                            if (resObj.getBoolean(KEY_STATUS)) {
                                JSONArray ary = resObj.getJSONArray(KEY_STOCK);
                                for (int i=0; i<ary.length(); i++) {
                                    JSONObject obj = ary.getJSONObject(i);
                                    books.add(new Book(
                                            obj.getString(KEY_PRODUCT_ID),
                                            obj.getString(KEY_PHOTO1),
                                            obj.getString(KEY_TITLE),
                                            obj.getString(KEY_PRICE)
                                    ));
                                }
                                getBitmap = new GetBitmap(context, getResources(), books, new GetBitmap.TaskListener() {
                                    @Override
                                    public void onFinished() {
                                        showData();
                                    }
                                });
                                getBitmap.setPreLoadAmount(12);
                                getBitmap.execute();
                            }else {
                                Toast.makeText(context, "沒有上架的商品", Toast.LENGTH_SHORT).show();
                                prgBar.setVisibility(View.GONE);
                                showFoundStatus();
                            }
                        }catch (JSONException e) {
                            Toast.makeText(context, "伺服器發生例外", Toast.LENGTH_SHORT).show();
                            prgBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
        try {
            JSONObject reqObj = new JSONObject();
            reqObj.put(KEY_USER_ID, loginUserId);
            conLoadStock.execute(getString(R.string.link_show_stock), reqObj.toString());
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showData() {
        stockAdapter = new StockListAdapter(context, getResources(), books, R.layout.spn_chat_product);
        stockAdapter.setBackgroundColor(getResources(), R.color.lst_stock);
        lstProduct.setAdapter(stockAdapter);

        books = null;
        prgBar.setVisibility(View.GONE);
        lstProduct.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setEnabled(true);
        swipeRefreshLayout.setRefreshing(false);

        isShown = true;
        System.gc();
    }

    private void showFoundStatus() {
        //若未找到書，則說明沒有找到
        TextView txtNotFound = (TextView) findViewById(R.id.txtNotFound);
        ImageView imgNotFound = (ImageView) findViewById(R.id.imgNotFound);
        if (books == null || books.isEmpty()) {
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

    private void dropProduct(String bookId) {
        lstProduct.setVisibility(View.GONE);
        prgBar.setVisibility(View.VISIBLE);
        conDropProduct = new MyOkHttp(MemberStockActivity.this, new MyOkHttp.TaskListener() {
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
                                Toast.makeText(context, "商品已下架", Toast.LENGTH_SHORT).show();
                                loadData();
                            }else {
                                Toast.makeText(context, "伺服器發生例外", Toast.LENGTH_SHORT).show();
                                prgBar.setVisibility(View.GONE);
                            }
                        }catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        try {
            JSONObject reqObj = new JSONObject();
            reqObj.put(KEY_PRODUCT_ID, bookId);
            conDropProduct.execute(getString(R.string.link_drop_product), reqObj.toString());
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        cancelConnection();
        isStockDisplayAlive = false;
        try {
            stockAdapter.setCanCheckLoop(false);
            stockAdapter.initCheckThread(false);
        }catch (NullPointerException e) {
            //第一次開啟，adapter尚未準備好
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        System.gc();
        super.onDestroy();
    }

    private void cancelConnection() {
        try {
            conLoadStock.cancel();
        }catch (NullPointerException e) {}
        try {
            conDropProduct.cancel();
        }catch (NullPointerException e) {}
        try {
            getBitmap.cancel(true);
        }catch (NullPointerException e) {}
    }
}
