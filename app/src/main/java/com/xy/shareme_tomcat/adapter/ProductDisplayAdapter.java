package com.xy.shareme_tomcat.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xy.shareme_tomcat.Product.ProductDetailActivity;
import com.xy.shareme_tomcat.R;
import com.xy.shareme_tomcat.data.Book;
import com.xy.shareme_tomcat.data.ImageObj;
import com.xy.shareme_tomcat.network_helper.GetBitmapTask;
import com.xy.shareme_tomcat.structure.ImageDownloadQueue;

import java.util.ArrayList;

import static com.xy.shareme_tomcat.data.DataHelper.KEY_ANYWAY;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PRODUCT_ID;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_TITLE;

public class ProductDisplayAdapter extends RecyclerView.Adapter<ProductDisplayAdapter.DataViewHolder> {
    private Context context;
    private Resources res;
    private ArrayList<ImageObj> books;
    private ImageDownloadQueue queue;
    private int lastPosition = -1, backgroundColor, queueVolume;

    public ProductDisplayAdapter(Resources res, Context context, ArrayList<ImageObj> books, int queueVolume){
        this.res = res;
        this.context = context;
        this.books = books;
        this.queueVolume = queueVolume;
        this.queue = new ImageDownloadQueue(queueVolume);
    }

    public class DataViewHolder extends RecyclerView.ViewHolder {
        // 連結資料的顯示物件宣告
        private int position;
        private CardView cardView;
        private LinearLayout layProductCard;
        private ImageView imgGoodsPic;
        private TextView txtGoodsTitle, txtGoodsPrice, txtBookSeller;

        DataViewHolder(View itemView) {
            super(itemView);

            // 連結資料的顯示物件取得
            cardView = (CardView) itemView.findViewById(R.id.card_view);
            layProductCard = (LinearLayout) itemView.findViewById(R.id.layProductCard);
            imgGoodsPic = (ImageView) itemView.findViewById(R.id.imgBookSummaryPic);
            txtGoodsTitle = (TextView) itemView.findViewById(R.id.txtBookSummaryTitle);
            txtGoodsPrice = (TextView) itemView.findViewById(R.id.txtBookSummaryPrice);
            txtBookSeller = (TextView) itemView.findViewById(R.id.txtSeller);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent it = new Intent(context, ProductDetailActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString(KEY_PRODUCT_ID, ((Book) books.get(position)).getId());
                    bundle.putString(KEY_TITLE, ((Book) books.get(position)).getTitle());
                    bundle.putString(KEY_ANYWAY, "0");
                    it.putExtras(bundle);
                    context.startActivity(it);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    @Override
    public ProductDisplayAdapter.DataViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        // 顯示資料物件來自 R.layout.card_book 中
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_book, viewGroup, false);
        ProductDisplayAdapter.DataViewHolder dataViewHolder = new ProductDisplayAdapter.DataViewHolder(view);
        return dataViewHolder;
    }

    public void onBindViewHolder(ProductDisplayAdapter.DataViewHolder dataViewHolder, int i) {
        // 顯示資料物件及資料項目的對應
        try {
            dataViewHolder.layProductCard.setBackgroundColor(res.getColor(backgroundColor));
        }catch (Exception e) {} //顏色資源未找到，因為未用set方法設定

        //依滑動方向檢查圖片
        if (i > lastPosition) { //往下滑
            if (books.get(i).getImg() == null) { //若發現沒圖片
                setGetBitmapTask(i, dataViewHolder); //指派下載器給該項目，放入佇列自動下載
                queue.enqueueFromRear(books.get(i));
            }
        }else { //往上滑
            if (books.get(i).getImg() == null) { //若發現沒圖片
                setGetBitmapTask(i, dataViewHolder); //指派下載器給該項目，放入佇列自動下載
                queue.enqueueFromFront(books.get(i));
            }
        }

        dataViewHolder.position = i;
        dataViewHolder.imgGoodsPic.setImageBitmap((books.get(i)).getImg()); //不用加Book，因為是父類別的方法
        dataViewHolder.txtGoodsTitle.setText(((Book) books.get(i)).getTitle());
        dataViewHolder.txtGoodsPrice.setText("$ " + ((Book) books.get(i)).getPrice());
        dataViewHolder.txtBookSeller.setText(((Book) books.get(i)).getSeller());
        lastPosition = i;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void setBackgroundColor(Resources r, int color) {
        this.backgroundColor = color;
    }

    private void setGetBitmapTask(final int i, final DataViewHolder dataViewHolder) {
        books.get(i).setGetBitmap(new GetBitmapTask(res.getString(R.string.link_image), new GetBitmapTask.TaskListener() {
            @Override
            public void onFinished() {
                dataViewHolder.imgGoodsPic.setImageBitmap(books.get(i).getImg());
                //notifyDataSetChanged(); //不可
            }
        }));
    }

    public void destroy(boolean isFully) {
        if (queue != null) {
            queue.destroy();
            if (isFully)
                queue = null;
            else
                queue = new ImageDownloadQueue(queueVolume);
        }
    }
}
