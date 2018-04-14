package com.xy.shareme_tomcat.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.xy.shareme_tomcat.R;
import com.xy.shareme_tomcat.data.AlbumImageProvider;
import com.xy.shareme_tomcat.data.DataHelper;
import com.xy.shareme_tomcat.data.ImageChild;
import com.xy.shareme_tomcat.structure.ImageUploadQueue;

import java.util.Collections;

public class ImageUploadAdapter extends RecyclerView.Adapter<ImageUploadAdapter.DataViewHolder> {
    private Context context;
    private ImageUploadQueue queue;
    private int pressedPosition = 0;
    private Dialog dialog;
    private AlbumImageProvider provider;

    public ImageUploadAdapter(Resources res, Context context, AlbumImageProvider provider, String linkUpload) {
        this.context = context;
        this.provider = provider;
        queue = new ImageUploadQueue(res, context, linkUpload);
        prepareDialog();
    }

    public class DataViewHolder extends RecyclerView.ViewHolder {
        // 連結資料的顯示物件宣告
        private CardView cardView;
        private int position;
        private FrameLayout layBookPic;
        private ImageView imgBookPic;

        DataViewHolder(View itemView) {
            super(itemView);

            // 連結資料的顯示物件取得
            cardView = (CardView) itemView.findViewById(R.id.card_view);
            layBookPic = (FrameLayout) itemView.findViewById(R.id.layBookImg);
            imgBookPic = (ImageView) itemView.findViewById(R.id.imgBook);

            // 當卡片被點擊時
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (getEntityAmount() > 5) {
                        Toast.makeText(context, "最多新增五張圖片", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    pressedPosition = position;
                    provider.select();
                }
            });

            cardView.setOnLongClickListener(new View.OnLongClickListener() { //長按跳出選項清單
                @Override
                public boolean onLongClick(View v) {
                    pressedPosition = position;
                    if (getItem(position).getBitmap() != null) {
                        dialog.show();
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return queue.size(); //真圖加空白圖的數量
    }

    @Override
    public DataViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_book_img, viewGroup, false);
        DataViewHolder dataViewHolder = new DataViewHolder(view);
        return dataViewHolder;
    }

    @Override
    public void onBindViewHolder(DataViewHolder dataViewHolder, int i) {
        // 顯示資料物件及資料項目 的對應
        dataViewHolder.position = i;

        if (((ImageChild) queue.get(i)).getBitmap() != null || ((ImageChild) queue.get(i)).getFileName() != "") {
            dataViewHolder.layBookPic.setBackgroundColor(Color.parseColor("#FAFAFA"));
            dataViewHolder.imgBookPic.setScaleType(ImageView.ScaleType.FIT_CENTER);//
            dataViewHolder.imgBookPic.setImageBitmap(((ImageChild) queue.get(i)).getBitmap());
        }else { //加號圖片
            dataViewHolder.layBookPic.setBackgroundColor(Color.parseColor("#DDDDDD"));
            dataViewHolder.imgBookPic.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            dataViewHolder.imgBookPic.setImageResource(R.drawable.icon_add);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public int getPressedPosition() {
        return pressedPosition;
    }

    private void prepareDialog() {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dlg_list_options);
        dialog.setCancelable(true);

        String[] textGroup = {"左移", "右移", "移除"};
        int[] iconGroup = {
                R.drawable.icon_turn_left,
                R.drawable.icon_turn_right,
                R.drawable.icon_delete
        };

        ListView listView = (ListView) dialog.findViewById(R.id.lstOptions);
        listView.setAdapter(DataHelper.getSimpleAdapter(
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
                String itemName = ((TextView)view.findViewById(R.id.txtTitle)).getText().toString();
                switch (itemName) {
                    case "左移":
                        moveItem(pressedPosition, -1);
                        break;
                    case "右移":
                        moveItem(pressedPosition, 1);
                        break;
                    case "移除":
                        removeItem(pressedPosition);
                        break;
                }
                dialog.dismiss();
            }
        });
    }

    public ImageChild getItem(int position) {
        return (ImageChild) queue.get(position);
    }

    public void setItem(int position, ImageChild item) {
        if (((ImageChild) queue.get(position)).getBitmap() == null && getItemCount() <= 4) //剛剛點的是空白圖(新增圖片)，且放圖片前至多有3+1張圖
            addItem(new ImageChild(null, false)); //新增一張空白圖

        queue.set(position, item);
        //notifyDataSetChanged(); //會出錯
        notifyItemChanged(position);
    }

    public void addItem(ImageChild item) {
        queue.enqueueFromRear(item);
        notifyDataSetChanged();
    }

    private void removeItem(int position) {
        int amount = 0;
        //第一條件，至少有兩張圖片(含一張空白)；第二條件，點選的不是空白圖
        if (getItemCount() > 1 && (getItem(position).getBitmap() != null)) {
            queue.remove(position);
            for (int i=0; i<queue.size(); i++) {
                if (getItem(i).getBitmap() != null)
                    amount++;
            }
            if (amount == 4) {
                //若移除後剩4張真圖，要補一張空白圖
                addItem(new ImageChild(null, false));
            }
            notifyDataSetChanged();
        }else
            Toast.makeText(context, "移除失敗", Toast.LENGTH_SHORT).show();
    }

    private void moveItem(int position, int distance) {
        int destination;
        if (distance > 0)
            destination = (position + distance >= getItemCount()) ? getItemCount() : position + distance;
        else
            destination = (position + distance < 0) ? 0 : position + distance;

        if (getItem(destination).getBitmap() != null) {
            Collections.swap(queue, position, destination);
            notifyDataSetChanged();
        }
    }

    public int getEntityAmount() {
        return queue.getEntityAmount();
    }

    public void startUpload(String[] fileNames, Dialog dlgUpload, TextView txtUploadHint, ImageUploadQueue.TaskListener taskListener) {
        queue.startUpload(fileNames, dlgUpload, txtUploadHint, taskListener);
    }

    public void cancelUpload() {
        queue.cancelUpload();
    }

    public void destroyQueue(boolean isFully) {
        if (queue != null) {
            queue.destroy();
            if (isFully)
                queue = null;
        }
    }
}