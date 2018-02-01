package com.xy.shareme_tomcat.structure;

import com.xy.shareme_tomcat.data.Book;
import com.xy.shareme_tomcat.data.ImageObj;

public class ProductDisplayQueue extends Queue {
    private int volume;

    public ProductDisplayQueue(int volume) {
        this.volume = volume;
    }

    @Override
    protected void onEnqueue(Object obj, boolean isFromFront) {
        if (size() > volume) { //超出容量
            if (isFromFront) //若從前端加入，則從後端清除
                dequeueFromRear();
            else //從後端加入，則從前端清除
                dequeueFromFront();
        }

        if (((Book) obj).getImg() == null) //若圖片為空，則啟動自帶的下載器
            ((Book) obj).startDownloadImage();
    }

    @Override
    protected void onDequeue(Object obj) {
        ((Book) obj).cancelDownloadImage(); //取消下載
        ((Book) obj).setGetBitmap(null); //清除下載器
        ((Book) obj).setImg(null); //清除圖片
    }

    public int getSize() {
        return size();
    }

    @Override
    public void destroy() {
        for (int i = 0; i < size(); i++) {
            ((ImageObj) get(i)).cancelDownloadImage();
            ((ImageObj) get(i)).setGetBitmap(null);
            ((ImageObj) get(i)).setImg(null);
        }
        clear();
    }
}
