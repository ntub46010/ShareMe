package com.xy.shareme_tomcat.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.xy.shareme_tomcat.ImageActivity;
import com.xy.shareme_tomcat.Product.ProductDetailActivity;
import com.xy.shareme_tomcat.R;

import java.util.ArrayList;

public class ImageGroupAdapter extends RecyclerView.Adapter<ImageGroupAdapter.DataViewHolder> {
    private Context context;
    private ArrayList<Bitmap> pictures;
    private boolean isPhotoZoomable = false;

    public class DataViewHolder extends RecyclerView.ViewHolder {
        // 連結資料的顯示物件宣告
        private CardView cardView;
        private int position;
        private ImageView imgBookPic;

        DataViewHolder(View itemView) {
            super(itemView);

            // 連結資料的顯示物件取得
            cardView = (CardView)itemView.findViewById(R.id.card_view);
            imgBookPic = (ImageView)itemView.findViewById(R.id.imgBook);

            // 當卡片被點擊時
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isPhotoZoomable) {
                        ProductDetailActivity.indexSelectedImage = position;
                        context.startActivity(new Intent(context, ImageActivity.class));
                    }
                }
            });

        }
    }

    // 將連結的資料
    public ImageGroupAdapter(Context context, ArrayList<Bitmap> pictures, boolean isPhotoZoomable) {
        this.context = context;
        this.pictures = pictures;
        this.isPhotoZoomable = isPhotoZoomable;
    }

    @Override
    public int getItemCount() {
        return pictures.size();
    }

    @Override
    public ImageGroupAdapter.DataViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_book_img, viewGroup, false);
        ImageGroupAdapter.DataViewHolder dataViewHolder = new ImageGroupAdapter.DataViewHolder(view);
        return dataViewHolder;
    }

    @Override
    public void onBindViewHolder(ImageGroupAdapter.DataViewHolder dataViewHolder, int i) {
        // 顯示資料物件及資料項目 的對應
        Bitmap picture =  pictures.get(i);
        dataViewHolder.position = i;
        if (picture != null)
            dataViewHolder.imgBookPic.setImageBitmap(picture);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
