package com.xy.shareme_tomcat.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.xy.shareme_tomcat.R;
import com.xy.shareme_tomcat.data.Chat;
import com.xy.shareme_tomcat.data.ImageObj;
import com.xy.shareme_tomcat.network_helper.GetBitmapTask;
import com.xy.shareme_tomcat.structure.ImageDownloadQueue;

import java.util.ArrayList;

public class MailListAdapter extends BaseAdapter{
    private Context context;
    private Resources res;
    private LayoutInflater layoutInflater;
    private ArrayList<ImageObj> mails;
    private ImageDownloadQueue queue;
    private int lastPosition, queueVolume;

    public MailListAdapter(Resources res, Context context, ArrayList<ImageObj> mails, int queueVolume) {
        this.res = res;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        this.mails = mails;
        this.queueVolume = queueVolume;
        this.queue = new ImageDownloadQueue(queueVolume);
    }

    @Override
    public int getCount() {
        return mails.size();
    }

    @Override
    public Object getItem(int i) {
        return mails.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null)
            view = layoutInflater.inflate(R.layout.lst_mail, viewGroup, false);

        ImageView imgAvatar = (ImageView) view.findViewById(R.id.imgAvatar);
        TextView txtName = (TextView) view.findViewById(R.id.txtName);
        TextView txtMsg = (TextView) view.findViewById(R.id.txtPreviewMsg);
        TextView txtDatetime = (TextView) view.findViewById(R.id.txtDatetime);

        //依滑動方向檢查圖片
        if (i > lastPosition) { //往下滑
            if (mails.get(i).getImg() == null) { //若發現沒圖片
                setGetBitmapTask(i, imgAvatar); //指派下載器給該項目，放入佇列自動下載
                queue.enqueueFromRear(mails.get(i));
            }
        }else { //往上滑
            if (mails.get(i).getImg() == null) { //若發現沒圖片
                setGetBitmapTask(i, imgAvatar); //指派下載器給該項目，放入佇列自動下載
                queue.enqueueFromFront(mails.get(i));
            }
        }

        txtName.setText(((Chat) mails.get(i)).getName());
        txtMsg.setText(((Chat) mails.get(i)).getMsg());
        txtDatetime.setText(((Chat) mails.get(i)).getDate() + "  " + ((Chat) mails.get(i)).getTime());
        if ((mails.get(i)).getImg() != null)
            imgAvatar.setImageBitmap((mails.get(i)).getImg());

        lastPosition = i;
        return view;
    }

    private void setGetBitmapTask(final int i, final ImageView imageView) {
        mails.get(i).setGetBitmap(new GetBitmapTask(res.getString(R.string.link_avatar), new GetBitmapTask.TaskListener() {
            @Override
            public void onFinished() {
                if (mails.get(i).getImg() != null)
                    imageView.setImageBitmap(mails.get(i).getImg());
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
