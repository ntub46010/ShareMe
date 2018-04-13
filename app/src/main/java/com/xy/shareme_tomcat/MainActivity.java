package com.xy.shareme_tomcat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.xy.shareme_tomcat.Member.MemberHomeFrag;
import com.xy.shareme_tomcat.Product.ProductHomeFrag;
import com.xy.shareme_tomcat.Product.ProductSearchActivity;
import com.xy.shareme_tomcat.Settings.SettingHomeFrag;
import com.xy.shareme_tomcat.Type.DepartmentFrag;

import java.util.ArrayList;
import java.util.List;

import static com.xy.shareme_tomcat.data.DataHelper.KEY_KEYWORD;
import static com.xy.shareme_tomcat.data.DataHelper.getBoardNickname;
import static com.xy.shareme_tomcat.data.DataHelper.setBoardTitle;

public class MainActivity extends FragmentActivity {
    public static Context context;
    public static Toolbar toolbar;
    public static TextView txtBarTitle;
    public static SearchView searchView;
    private TabLayout tabHome;
    public static ViewPager vpgHome;
    public static int lastPosition = 1;
    private ViewPagerAdapter vpgAdapter;
    public static String board = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        txtBarTitle = (TextView) toolbar.findViewById(R.id.txtToolbarTitle);

        //初次使用將開啟通知功能
        SharedPreferences sp = getSharedPreferences(getString(R.string.sp_fileName), MODE_PRIVATE);
        if (sp.getBoolean(getString(R.string.sp_isFirstUse), true)) {
            sp.edit()
                    .putBoolean(getString(R.string.sp_showNotification), true)
                    .putBoolean(getString(R.string.sp_isFirstUse), false)
                    .apply();
        }
        sp.edit().putBoolean(getString(R.string.sp_isFromNotification), false).apply();

        initSearchView();

        vpgHome = (ViewPager) findViewById(R.id.vpgHome);
        vpgHome.setOffscreenPageLimit(15);
        vpgHome.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                //設定標題
                if (position == 0 || position == 1)
                    setBoardTitle();
                else
                    txtBarTitle.setText(getString(R.string.app_name));

                //設定搜尋鈕
                if (position == 1)
                    searchView.setVisibility(View.VISIBLE);
                else {
                    searchView.setVisibility(View.GONE);
                    searchView.onActionViewCollapsed();
                }

                if (lastPosition == 0 && position == 1) //從科系移動到商品後必重新刷新商品
                    vpgAdapter.getItem(position).onResume();

                lastPosition = position;
            }
            @Override
            public void onPageScrollStateChanged(int state) {}
        });
        setupViewPager();

        tabHome = (TabLayout) findViewById(R.id.tabs);
        tabHome.setupWithViewPager(vpgHome);
        vpgHome.setCurrentItem(1); //開啟Activity第一個顯示的頁面
    }

    private void setupViewPager() {
        //加入頁面
        vpgAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        vpgAdapter.addFragment(new DepartmentFrag(), "科系");
        vpgAdapter.addFragment(new ProductHomeFrag(), "商品");
        vpgAdapter.addFragment(new MemberHomeFrag(), "會員專區");
        vpgAdapter.addFragment(new SettingHomeFrag(), "設定");
        vpgHome.setAdapter(vpgAdapter);
    }

    public void initSearchView() {
        searchView = (SearchView) toolbar.findViewById(R.id.searchview);
        searchView.setIconifiedByDefault(true);
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (!s.equals("")) {
                    Intent it = new Intent(context, ProductSearchActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString(KEY_KEYWORD, s);
                    it.putExtras(bundle);
                    startActivity(it);
                    searchView.clearFocus();
                    searchView.onActionViewCollapsed();
                }
                return true;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    txtBarTitle.setVisibility(View.GONE);
                else
                    txtBarTitle.setVisibility(View.VISIBLE);
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                searchView.onActionViewCollapsed();
                return true;
            }
        });

        //搜尋框提示字體顏色
        try {
            int id = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
            TextView textView = (TextView) searchView.findViewById(id);
            textView.setTextColor(Color.parseColor("#FFFFFF"));
            textView.setHintTextColor(Color.parseColor("#80FFFFFF"));
        }catch (Exception e) {
        }
    }

    //若改繼承FragmentStatePagerAdapter，畫面滑出便會清除，返回需重新載入
    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}
