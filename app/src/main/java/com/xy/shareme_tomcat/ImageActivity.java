package com.xy.shareme_tomcat;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.xy.shareme_tomcat.Product.ProductDetailActivity;
import com.xy.shareme_tomcat.data.ZoomableImageView;

import java.util.ArrayList;

public class ImageActivity extends AppCompatActivity {
    private static ArrayList<Bitmap> images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        Bundle bundle = getIntent().getExtras();
        int currentIndex = bundle.getInt("CurrentIndex", 0);
        //images = (ArrayList<Bitmap>) bundle.getSerializable("Images");
        images = ProductDetailActivity.images;

        //將圖片陣列載入ViewPager
        ViewPager vpgImage = (ViewPager) findViewById(R.id.viewPager);
        vpgImage.setAdapter(new ImageAdapter(getSupportFragmentManager(), images));
        vpgImage.setCurrentItem(currentIndex); //顯示所點擊的圖片位置
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        images.clear();
        images = null;
        System.gc();
    }

    private class ImageAdapter extends FragmentPagerAdapter {
        private ArrayList<Bitmap> images;

        public ImageAdapter (FragmentManager fm, ArrayList<Bitmap> images) {
            super(fm);
            this.images = images;
        }

        @Override
        public Fragment getItem(int position) {
            return ImageFrag.newInstance(position);
        }

        @Override
        public int getCount() {
            return images.size();
        }
    }

    public static class ImageFrag extends Fragment {
        private Bitmap image;

        public ImageFrag() {

        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                //由索引取得對應圖片
                int index = getArguments().getInt("Index");
                image = images.get(index);
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
            bundle.putInt("Index", index);
            frag.setArguments(bundle);
            return frag;
        }
    }
}
