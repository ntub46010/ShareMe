package com.xy.shareme_tomcat.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xy.shareme_tomcat.Product.ProductDetailActivity;
import com.xy.shareme_tomcat.R;
import com.xy.shareme_tomcat.data.Book;
import com.xy.shareme_tomcat.data.ImageObj;
import com.xy.shareme_tomcat.network_helper.GetBitmap;

import java.util.ArrayList;

import static com.xy.shareme_tomcat.data.DataHelper.KEY_PRODUCT_ID;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_TITLE;
import static com.xy.shareme_tomcat.data.DataHelper.isProductDisplayAlive;

public class ProductDisplayAdapter extends RecyclerView.Adapter<ProductDisplayAdapter.DataViewHolder> {
    private Context context;
    private Resources res = null;

    private ArrayList<ImageObj> books;
    private ArrayList<GetBitmap> bitTasks;
    private int backgroundColor;
    private boolean isFirstToHead = true, canCheckLoop = true;
    private Thread trdCheckImg = null;
    private int head = 0, tail = 9, section = 8;
    private boolean[] loadLock = null;

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

            // 當卡片被點擊時
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent it = new Intent(context, ProductDetailActivity.class);
                    Bundle bundle = new Bundle();
                    Book book = (Book) books.get(position);
                    bundle.putString(KEY_PRODUCT_ID, book.getId());
                    bundle.putString(KEY_TITLE, book.getTitle());
                    it.putExtras(bundle);
                    context.startActivity(it);
                }
            });
        }
    }

    // 將連結的資料
    public ProductDisplayAdapter(Context context, Resources res, ArrayList<ImageObj> books){
        this.context = context;
        this.res = res;
        this.books = books;

        loadLock = new boolean[this.books.size()];
        for (int i=0; i<loadLock.length; i++)
            loadLock[i] = true;

        initTask();
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

    @Override
    public void onBindViewHolder(ProductDisplayAdapter.DataViewHolder dataViewHolder, int i) {
        // 顯示資料物件及資料項目 的對應
        setContent(i);
        try {
            dataViewHolder.layProductCard.setBackgroundColor(res.getColor(backgroundColor));
        }catch (NullPointerException e) {}

        Book book = (Book) books.get(i);
        dataViewHolder.position = i;
        dataViewHolder.imgGoodsPic.setImageBitmap(book.getImg());
        dataViewHolder.txtGoodsTitle.setText(book.getTitle());
        dataViewHolder.txtGoodsPrice.setText("$ " + book.getPrice());
        dataViewHolder.txtBookSeller.setText(book.getSeller());
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    private void setContent(int position) {
        head = (position - section >= 0) ? position - section : 0;
        tail = (position + section <= books.size() - 1) ? position + section : books.size() - 1;
        initTask();
        if (isFirstToHead && position == 0) {
            isFirstToHead = false; //避免重新回到第一個項目，又啟動第二個執行緒
            initCheckThread(true);
        }
    }

    private void initTask() {
        bitTasks = new ArrayList<>();
        for (int i = 0; i < books.size(); i++) {
            final int i2 = i;
            bitTasks.add(
                    new GetBitmap(context, res, books.get(i), new GetBitmap.TaskListener() {
                        @Override
                        public void onFinished() {
                            loadLock[i2] = true;
                            //notifyItemChanged(i2); //有動畫效果
                            notifyDataSetChanged();
                        }
                    })
            );
        }
    }

    private void prepareImgs() {
        //下載應出現的照片
        try {
            for (int i = head; i <= tail; i++) {
                if (loadLock[i] && books.get(i).getImg() == null) {
                    loadLock[i] = false;
                    bitTasks.get(i).execute();
                }
            }
        }catch (ArrayIndexOutOfBoundsException e) {}

        //清除應消失的照片
        for (int i = 0; i < head; i++) {
            if (!loadLock[i] || books.get(i).getImgURL() != null) {
                bitTasks.get(i).cancel(true);
                books.get(i).setImg(null);
            }
        }
        for (int i = tail+1; i<books.size(); i++) {
            if (!loadLock[i] || books.get(i).getImg() != null) {
                bitTasks.get(i).cancel(true);
                books.get(i).setImg(null);
            }
        }
    }

    public void initCheckThread (boolean restart) {
        trdCheckImg = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(200);
                    hdrCheckImg.sendMessage(hdrCheckImg.obtainMessage());
                }catch (Exception e) {}
            }
        });

        if (restart) {
            trdCheckImg.start();
        }
    }

    private Handler hdrCheckImg = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (canCheckLoop && isProductDisplayAlive) {
                //Toast.makeText(context, "prepareImgs1", Toast.LENGTH_SHORT).show();
                prepareImgs();
                initCheckThread(true);
            }else {
                //Toast.makeText(context, "prepareImgs2", Toast.LENGTH_SHORT).show();
                canCheckLoop = true; //false ？
                initCheckThread(false);
            }
        }
    };

    public void setAllImagesNull () {
        for (int i=0; i<books.size(); i++) {
            books.get(i).setImg(null);
        }
    }

    public void setBackgroundColor(Resources r, int color) {
        this.backgroundColor = color;
    }

    public void setCanCheckLoop(boolean loop) {
        this.canCheckLoop = loop;
    }

}
