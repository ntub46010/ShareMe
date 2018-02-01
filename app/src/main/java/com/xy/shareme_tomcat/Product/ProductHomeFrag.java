package com.xy.shareme_tomcat.Product;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import static com.xy.shareme_tomcat.data.DataHelper.isFromDepartment;
import static com.xy.shareme_tomcat.data.DataHelper.setBoardTitle;
import static com.xy.shareme_tomcat.MainActivity.context;

public class ProductHomeFrag extends Fragment {
    public static RecyclerView recyProduct;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar prgBar;

    private ArrayList<ImageObj> books;
    public static ProductDisplayAdapter adpProductHome;
    public static MyOkHttp conProductHome;
    public static GetBitmap gbmProductHome;
    private boolean isShown = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_product_home, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setBoardTitle();
        prgBar = (ProgressBar) getView().findViewById(R.id.prgBar);

        recyProduct = (RecyclerView) getView().findViewById(R.id.recyclerView); //若在執行showData前切換科系，DepartmentFrag會在setVisibility時NPE
        swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData("");
            }
        });

        setFab();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isFromDepartment) {
            isFromDepartment = false;
            loadData("");
        }else if (!isShown)
            loadData("");
    }

    private void setFab () {
        FloatingActionButton fabTop = (FloatingActionButton) getView().findViewById(R.id.fab_top);
        fabTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    adpProductHome.destroy(false);
                    recyProduct.scrollToPosition(0);
                }catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, "沒有商品，不能往上", Toast.LENGTH_SHORT).show();
                }
            }
        });

        FloatingActionButton fabPost = (FloatingActionButton) getView().findViewById(R.id.fab_add);
        fabPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, ProductPostActivity.class));
            }
        });
    }

    private void loadData(String keyword) {
        isShown = false;
        swipeRefreshLayout.setEnabled(false);
        prgBar.setVisibility(View.VISIBLE);

        books = new ArrayList<>();
        conProductHome = new MyOkHttp(getActivity(), new MyOkHttp.TaskListener() {
            @Override
            public void onFinished(String result) {
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

                        gbmProductHome = new GetBitmap(context, books, getString(R.string.link_image), new GetBitmap.TaskListener() {
                            @Override
                            public void onFinished() {
                                showData();
                            }
                        });
                        gbmProductHome.setPreLoadAmount(-1); //-1代表都不要下載
                        gbmProductHome.execute();

                    }else {
                        Toast.makeText(context, "沒有找到商品", Toast.LENGTH_SHORT).show();
                        showFoundStatus();
                        prgBar.setVisibility(View.GONE);
                    }
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        //開始連線
        try {
            JSONObject reqObj = new JSONObject();
            reqObj.put(KEY_TYPE, board);
            reqObj.put(KEY_KEYWORD, keyword);
            conProductHome.execute(getString(R.string.link_list_product), reqObj.toString());
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showData() {
        recyProduct.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyProduct.setLayoutManager(linearLayoutManager);

        adpProductHome = new ProductDisplayAdapter(getResources(), context, books);
        adpProductHome.setBackgroundColor(getResources(), R.color.card_product);
        recyProduct.setAdapter(adpProductHome);

        swipeRefreshLayout.setEnabled(true);
        swipeRefreshLayout.setRefreshing(false);
        prgBar.setVisibility(View.GONE);
        recyProduct.setVisibility(View.VISIBLE);

        books = null;
        isShown = true;
    }

    private void showFoundStatus() {
        //若未找到書，則說明沒有找到
        TextView txtNotFound = (TextView) getView().findViewById(R.id.txtNotFound);
        ImageView imgNotFound = (ImageView) getView().findViewById(R.id.imgNotFound);
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
        adpProductHome.destroy(true);
        adpProductHome = null;
        System.gc();
        super.onDestroy();
    }

    private void cancelConnection() {
        try {
            conProductHome.cancel();
        }catch (NullPointerException e) {}
        try {
            gbmProductHome.cancel(true);
        }catch (NullPointerException e) {}
    }
}

