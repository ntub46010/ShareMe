package com.xy.shareme_tomcat.Product;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.xy.shareme_tomcat.Member.MemberProfileActivity;
import com.xy.shareme_tomcat.R;
import com.xy.shareme_tomcat.adapter.ImageGroupAdapter;
import com.xy.shareme_tomcat.data.Book;
import com.xy.shareme_tomcat.network_helper.GetBitmapBatch;
import com.xy.shareme_tomcat.network_helper.MyOkHttp;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.xy.shareme_tomcat.data.DataHelper.KEY_ANYWAY;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_CONDITION;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_EDIT_TIME;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_FAVORITE;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_IS_ADD;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_MEMBER_ID;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_NOTE;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PHOTO1;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PHOTO2;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PHOTO3;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PHOTO4;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PHOTO5;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_POST_TIME;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PRICE;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PRODUCT;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PRODUCT_ID;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PS;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_SELLER_ID;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_SELLER_NAME;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_STATUS;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_TITLE;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_TYPE;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_USER_ID;
import static com.xy.shareme_tomcat.data.DataHelper.isProfileAlive;
import static com.xy.shareme_tomcat.data.DataHelper.loginUserId;
import static com.xy.shareme_tomcat.data.DataHelper.showFoundStatus;

public class ProductDetailActivity extends AppCompatActivity {
    private Context context;
    private LinearLayout layDetail;
    private ProgressBar prgBar;
    private TextView txtId, txtTitle, txtDep, txtStatus, txtNote, txtPrice, txtPS, txtSeller, txtPost, txtEdit;
    private FloatingActionButton fabContact, fabFavorite;

    private Book book;
    public static ArrayList<Bitmap> images;
    private ImageGroupAdapter adapter;
    public static int indexSelectedImage;
    private String productId;

    private MyOkHttp conn;
    private GetBitmapBatch getBitmap;
    private boolean isShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        context = this;

        //設置Toolbar
        Bundle bundle = getIntent().getExtras();
        productId = bundle.getString(KEY_PRODUCT_ID);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(bundle.getString(KEY_TITLE));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //隱藏原畫面、顯示轉圈
        layDetail = (LinearLayout) findViewById(R.id.layDetail);
        layDetail.setVisibility(View.INVISIBLE);
        prgBar = (ProgressBar) findViewById(R.id.prgBar);

        //詳情元件
        txtId = (TextView) findViewById(R.id.txtDetailId);
        txtTitle = (TextView) findViewById(R.id.txtDetailTitle);
        txtDep = (TextView) findViewById(R.id.txtDetailDep);
        txtStatus = (TextView) findViewById(R.id.txtDetailStatus);
        txtNote = (TextView) findViewById(R.id.txtDetailNote);
        txtPrice = (TextView) findViewById(R.id.txtDetailPrice);
        txtPS = (TextView) findViewById(R.id.txtDetailPS);
        txtSeller = (TextView) findViewById(R.id.txtDetailSeller);
        txtPost = (TextView) findViewById(R.id.txtPostDate);
        txtEdit = (TextView) findViewById(R.id.txtEditDate);
        setFab();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isShown)
            loadData();
    }

    private void setFab() {
        fabContact = (FloatingActionButton) findViewById(R.id.fab_contact);
        fabContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        fabFavorite = (FloatingActionButton) findViewById(R.id.fab_favorite);
        fabFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFavorite();
            }
        });
    }

    private void loadData() {
        isShown = false;
        prgBar.setVisibility(View.VISIBLE);
        conn = new MyOkHttp(ProductDetailActivity.this, new MyOkHttp.TaskListener() {
            @Override
            public void onFinished(final String result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result == null) {
                            Toast.makeText(context, "連線失敗", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        final ImageView imageView = (ImageView) findViewById(R.id.imgNotFound);
                        final TextView textView = (TextView) findViewById(R.id.txtNotFound);
                        try {
                            JSONObject resObj = new JSONObject(result);
                            if (resObj.getBoolean(KEY_STATUS)) {
                                JSONObject obj = resObj.getJSONObject(KEY_PRODUCT);
                                book = new Book(
                                        productId,
                                        obj.getString(KEY_PHOTO1),
                                        obj.getString(KEY_PHOTO2),
                                        obj.getString(KEY_PHOTO3),
                                        obj.getString(KEY_PHOTO4),
                                        obj.getString(KEY_PHOTO5),
                                        obj.getString(KEY_TITLE),
                                        obj.getString(KEY_CONDITION),
                                        obj.getString(KEY_NOTE),
                                        obj.getString(KEY_PRICE),
                                        obj.getString(KEY_PS),
                                        obj.getString(KEY_SELLER_ID),
                                        obj.getString(KEY_SELLER_NAME),
                                        obj.getString(KEY_TYPE),
                                        obj.getString(KEY_POST_TIME),
                                        obj.getString(KEY_EDIT_TIME)
                                );

                                //若已在最愛清單，就改變愛心顏色
                                if (obj.getBoolean(KEY_FAVORITE))
                                    fabFavorite.setImageResource(R.drawable.ic_favorite_yellow);

                                // 產生物件ArrayList資料後，由圖片位址下載圖片，完成後再顯示資料.
                                getBitmap = new GetBitmapBatch(context, getResources(), book, new GetBitmapBatch.TaskListener() {
                                    // 下載圖片完成後執行的方法
                                    @Override
                                    public void onFinished() {
                                        showFoundStatus(book, imageView, textView, "");
                                        showData();
                                        prgBar.setVisibility(View.GONE);
                                    }
                                });
                                // 執行圖片下載
                                getBitmap.execute();
                            }else {
                                showFoundStatus(book, imageView, textView, "此商品不存在");
                                prgBar.setVisibility(View.GONE);
                            }
                        }catch (JSONException e) {
                            showFoundStatus(book, imageView, textView, "處理JSON發生錯誤");
                            prgBar.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
        //開始連線
        try {
            JSONObject reqObj = new JSONObject();
            reqObj.put(KEY_USER_ID, loginUserId);
            reqObj.put(KEY_PRODUCT_ID, productId);
            reqObj.put(KEY_ANYWAY, "0");
            conn.execute(getString(R.string.link_product_detail), reqObj.toString());
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showData () {
        txtId.setText(productId);
        txtTitle.setText(book.getTitle());
        txtPrice.setText("$ " + book.getPrice());
        txtStatus.setText(book.getStatus());
        txtNote.setText(book.getNote());
        txtPS.setText(book.getPs());
        txtPost.setText(book.getPostDate() + "  刊登");
        txtEdit.setText(book.getEditDate() + "  編輯");
        if (book.getEditDate().equals(""))
            txtEdit.setVisibility(View.GONE);

        final String sellerId = book.getSeller();
        String sellerName = book.getSellerName();
        txtSeller.setText(sellerName + " (" + sellerId + ")");

        if (!sellerId.equals(loginUserId) && !isProfileAlive) {
            txtSeller.setTextColor(Color.parseColor("#0000FF"));
            txtSeller.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent it = new Intent(context, MemberProfileActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString(KEY_MEMBER_ID, sellerId);
                    it.putExtras(bundle);
                    startActivity(it);
                }
            });
        }

        //顯示科系
        StringBuffer sbDep = new StringBuffer();
        if (book.getDep().contains("01")) sbDep.append("會資、");
        if (book.getDep().contains("02")) sbDep.append("財金、");
        if (book.getDep().contains("03")) sbDep.append("財稅、");
        if (book.getDep().contains("04")) sbDep.append("國商、");
        if (book.getDep().contains("05")) sbDep.append("企管、");
        if (book.getDep().contains("06")) sbDep.append("資管、");
        if (book.getDep().contains("07")) sbDep.append("應外、");
        if (book.getDep().contains("A")) sbDep.append("商設、");
        if (book.getDep().contains("B")) sbDep.append("商創、");
        if (book.getDep().contains("C")) sbDep.append("數媒、");
        if (book.getDep().contains("00")) sbDep.append("通識、");
        txtDep.setText(sbDep.substring(0, sbDep.length() - 1));

        //聯繫與喜歡按鈕
        if (!sellerId.equals(loginUserId)) {
            fabFavorite.setVisibility(View.VISIBLE);
            fabContact.setVisibility(View.VISIBLE);
        }else {
            fabFavorite.setVisibility(View.GONE);
            fabContact.setVisibility(View.GONE);
        }

        layDetail.setVisibility(View.VISIBLE);

        showImages(book);
        isShown = true;
        book = null;
    }

    private void showImages(Book book) {
        images = new ArrayList<>();
        if (!book.getImgURL().equals("")) images.add(book.getImg());
        if (!book.getImgURL2().equals("")) images.add(book.getImg2());
        if (!book.getImgURL3().equals("")) images.add(book.getImg3());
        if (!book.getImgURL4().equals("")) images.add(book.getImg4());
        if (!book.getImgURL5().equals("")) images.add(book.getImg5());

        // 產生 RecyclerView
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recy_books);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        // 產生一個Adapter物件，連結圖片資料
        adapter = new ImageGroupAdapter(context, images, true);
        recyclerView.setAdapter(adapter);
    }

    private void addFavorite() {
        MyOkHttp conn = new MyOkHttp(ProductDetailActivity.this, new MyOkHttp.TaskListener() {
            @Override
            public void onFinished(final String result) {
                if (result == null) {
                    Toast.makeText(context, "連線失敗", Toast.LENGTH_SHORT).show();
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject resObj = new JSONObject(result);
                            if (resObj.getBoolean(KEY_STATUS)) {
                                if (resObj.getBoolean(KEY_IS_ADD)) {
                                    Toast.makeText(context, "加入到我的最愛", Toast.LENGTH_SHORT).show();
                                    fabFavorite.setImageResource(R.drawable.ic_favorite_yellow);
                                }else {
                                    Toast.makeText(context, "從我的最愛移除", Toast.LENGTH_SHORT).show();
                                    fabFavorite.setImageResource(R.drawable.ic_favorite_white);
                                }
                            }else
                                Toast.makeText(context, "伺服器發生例外", Toast.LENGTH_SHORT).show();
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
            reqObj.put(KEY_USER_ID, loginUserId);
            reqObj.put(KEY_PRODUCT_ID, productId);
            conn.execute(getString(R.string.link_add_favorite), reqObj.toString());
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        cancelConnection();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        images = null;
        adapter = null;
        System.gc();
        super.onDestroy();
    }

    private void cancelConnection() {
        if (conn != null)
            conn.cancel();
        if (getBitmap != null)
            getBitmap.cancel(true);
    }

}
