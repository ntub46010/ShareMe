package com.xy.shareme_tomcat.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xy.shareme_tomcat.R;
import com.xy.shareme_tomcat.data.Book;
import com.xy.shareme_tomcat.data.ImageObj;
import com.xy.shareme_tomcat.network_helper.GetBitmapTask;
import com.xy.shareme_tomcat.structure.ImageDownloadQueue;

import java.util.ArrayList;

public class StockListAdapter extends BaseAdapter {
    private Resources res;
    private LayoutInflater layoutInflater;
    private int layout, lastPosition, backgroundColor, queueVolume;

    private ArrayList<ImageObj> books;
    private ImageDownloadQueue queue;

    public StockListAdapter(Resources res, Context context, ArrayList<ImageObj> books, int layout, int queueVolume) {
        this.res = res;
        this.books = books;
        this.layout = layout;
        layoutInflater = LayoutInflater.from(context);
        this.queueVolume = queueVolume;
        this.queue = new ImageDownloadQueue(queueVolume);
    }

    @Override
    public int getCount() {
        return books.size();
    }

    @Override
    public Object getItem(int position) {
        return books.get(position);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = layoutInflater.inflate(layout, parent, false);

        ImageView imgBookPic = (ImageView) convertView.findViewById(R.id.imgBookSummaryPic);
        TextView txtTitle = (TextView) convertView.findViewById(R.id.txtBookSummaryTitle);

        imgBookPic.setImageBitmap(books.get(i).getImg());
        txtTitle.setText(((Book) books.get(i)).getTitle());

        //依滑動方向檢查圖片
        if (i > lastPosition) { //往下滑
            if (books.get(i).getImg() == null) { //若發現沒圖片
                setGetBitmapTask(i, imgBookPic); //指派下載器給該項目，放入佇列自動下載
                queue.enqueueFromRear(books.get(i));
            }
        }else { //往上滑
            if (books.get(i).getImg() == null) { //若發現沒圖片
                setGetBitmapTask(i, imgBookPic); //指派下載器給該項目，放入佇列自動下載
                queue.enqueueFromFront(books.get(i));
            }
        }

        if (layout == R.layout.spn_chat_product) { //商品管理：追加選單背景色、價格
            LinearLayout linearLayout = (LinearLayout) convertView.findViewById(R.id.layBookSummary);
            linearLayout.setBackgroundColor(res.getColor(backgroundColor));
            TextView txtPrice = (TextView) convertView.findViewById(R.id.txtBookSummaryPrice);
            txtPrice.setText("$ " + ((Book) books.get(i)).getPrice());
        }else if (layout == R.layout.grd_member_shelf) { //個人檔案上架：

        }

        lastPosition = i;
        return convertView;
    }

    private void setGetBitmapTask(final int i, final ImageView imageView) {
        books.get(i).setGetBitmap(new GetBitmapTask(res.getString(R.string.link_image), new GetBitmapTask.TaskListener() {
            @Override
            public void onFinished() {
                imageView.setImageBitmap(books.get(i).getImg());
                //notifyDataSetChanged(); //不可
            }
        }));
    }

    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
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
