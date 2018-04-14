package com.xy.shareme_tomcat.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.xy.shareme_tomcat.R;
import com.xy.shareme_tomcat.data.Book;
import com.xy.shareme_tomcat.data.ImageObj;
import com.xy.shareme_tomcat.network_helper.GetBitmapTask;
import com.xy.shareme_tomcat.structure.ImageDownloadQueue;

import java.util.ArrayList;

public class ProductSpinnerAdapter extends BaseAdapter {
    private Resources res;
    private Context context;
    private LayoutInflater layoutInflater;
    private int layout, lastPosition, queueVolume;

    private ArrayList<ImageObj> books;
    private ImageDownloadQueue queue;

    public ProductSpinnerAdapter(Resources res, Context context, ArrayList<ImageObj> books, int layout, int queueVolume) {
        this.res = res;
        this.context = context;
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
    public Object getItem(int i) {
        return books.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        Toast.makeText(context, "i: " + String.valueOf(i), Toast.LENGTH_SHORT).show();
        if (convertView == null)
            convertView = layoutInflater.inflate(layout, parent, false);

        ImageView imgBookPic = (ImageView) convertView.findViewById(R.id.imgBookSummaryPic);
        TextView txtBookTitle = (TextView) convertView.findViewById(R.id.txtBookSummaryTitle);
        TextView txtBookPrice = (TextView) convertView.findViewById(R.id.txtBookSummaryPrice);

        try {
            txtBookTitle.setText(((Book) books.get(i)).getTitle());
            txtBookPrice.setText("$ " + ((Book) books.get(i)).getPrice());
        }catch (NullPointerException e) {
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
        }

        if (i > lastPosition) {
            if (!books.get(i).isStartDownload() && books.get(i).getImg() == null) {
                setImageDownloader(i, imgBookPic);
                queue.enqueueFromRear(books.get(i));
            }else
                imgBookPic.setImageBitmap(books.get(i).getImg());
        }else {
            if (!books.get(i).isStartDownload() && books.get(i).getImg() == null) {
                setImageDownloader(i, imgBookPic);
                queue.enqueueFromFront(books.get(i));
            }else
                imgBookPic.setImageBitmap(books.get(i).getImg());
        }

        lastPosition = i;
        return convertView;
    }

    private void setImageDownloader (final int i, final ImageView imageView) { //BUG，重新展開Spinner才會顯示下載好的圖片
        books.get(i).setGetBitmap(new GetBitmapTask(res.getString(R.string.link_image), new GetBitmapTask.TaskListener() {
            @Override
            public void onFinished() {
                imageView.setImageBitmap(books.get(i).getImg());
                //notifyDataSetChanged(); //圖片可即時顯示，但會無限執行getView
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
