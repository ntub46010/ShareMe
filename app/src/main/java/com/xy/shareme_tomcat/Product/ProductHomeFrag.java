package com.xy.shareme_tomcat.Product;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.xy.shareme_tomcat.R;

import static com.xy.shareme_tomcat.data.DataHelper.setBoardTitle;
import static com.xy.shareme_tomcat.MainActivity.context;

public class ProductHomeFrag extends Fragment {
    public static RecyclerView recyProduct;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar prgBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_product_home, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setBoardTitle();
        prgBar = (ProgressBar) getView().findViewById(R.id.prgBar);

        swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

            }
        });

        setFab();
    }

    private void setFab () {
        FloatingActionButton fabTop = (FloatingActionButton) getView().findViewById(R.id.fab_top);
        fabTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
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
}
