package com.xy.shareme_tomcat;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.xy.shareme_tomcat.Product.ProductDetailActivity;

import java.util.ArrayList;

import static com.xy.shareme_tomcat.Product.ProductDetailActivity.indexSelectedImage;

public class ImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        //將圖片陣列載入ViewPager
        ViewPager vpgImage = (ViewPager) findViewById(R.id.viewPager);
        vpgImage.setAdapter(new ImageAdapter(getSupportFragmentManager(), ProductDetailActivity.images));
        vpgImage.setCurrentItem(indexSelectedImage); //顯示所點擊的圖片位置
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
}
