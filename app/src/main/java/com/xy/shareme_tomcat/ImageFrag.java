package com.xy.shareme_tomcat;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xy.shareme_tomcat.Product.ProductDetailActivity;
import com.xy.shareme_tomcat.data.ZoomableImageView;

public class ImageFrag extends Fragment {
    private Bitmap image;

    public ImageFrag() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //由索引取得對應圖片
            int index = getArguments().getInt("index");
            //image = ProductDetailActivity.images.get(index);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_image_zoom, container, false);
        //取得圖片後顯示在ImageView上
        ZoomableImageView imageView = (ZoomableImageView) view.findViewById(R.id.zoomImage);
        imageView.setImageBitmap(image);
        return view;
    }

    public static ImageFrag newInstance(int index) {
        ImageFrag frag = new ImageFrag();
        Bundle bundle = new Bundle();
        bundle.putInt("index", index);
        frag.setArguments(bundle);
        return frag;
    }
}
