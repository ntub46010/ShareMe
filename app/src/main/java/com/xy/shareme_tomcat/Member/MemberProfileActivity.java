package com.xy.shareme_tomcat.Member;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.xy.shareme_tomcat.Product.ProductDetailActivity;
import com.xy.shareme_tomcat.R;
import com.xy.shareme_tomcat.adapter.StockListAdapter;
import com.xy.shareme_tomcat.data.Book;
import com.xy.shareme_tomcat.data.ImageObj;
import com.xy.shareme_tomcat.data.Member;
import com.xy.shareme_tomcat.network_helper.GetBitmapTask;
import com.xy.shareme_tomcat.network_helper.MyOkHttp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.xy.shareme_tomcat.data.DataHelper.KEY_ANYWAY;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_AVATAR;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_DEPARTMENT;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_EMAIL;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_GIVER_ID;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_IS_SETTING;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_MEMBER_ID;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_NAME;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_NEGATIVE;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PHOTO1;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_POSITIVE;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PRODUCT_ID;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PROFILE;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_RECEIVER_ID;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_STATUS;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_STOCK;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_TITLE;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_USER_ID;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_VALUE;
import static com.xy.shareme_tomcat.data.DataHelper.canShowProfile;
import static com.xy.shareme_tomcat.data.DataHelper.loginUserId;

public class MemberProfileActivity extends AppCompatActivity implements View.OnClickListener{
    private Context context;
    private LinearLayout layInfo;
    private ImageView imgAvatar;
    private TextView txtName, txtDepartment, txtPositive, txtNegative;
    private GridView grdShelf;
    private ProgressBar prgBar;

    private Member member;
    private ArrayList<ImageObj> books;
    private StockListAdapter adapter;
    private MyOkHttp conn;

    private String memberId, email;
    private int originValue;
    private boolean isShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_profile);
        context = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        //Toolbar文字
        Bundle bundle = getIntent().getExtras();
        memberId = bundle.getString(KEY_MEMBER_ID);
        toolbar.setTitle("個人檔案");

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        layInfo = (LinearLayout) findViewById(R.id.layInfo);
        layInfo.setVisibility(View.INVISIBLE);

        imgAvatar = (ImageView) findViewById(R.id.imgAvatar);
        txtName = (TextView) findViewById(R.id.txtName);
        txtDepartment = (TextView) findViewById(R.id.txtDepartment);
        txtPositive = (TextView) findViewById(R.id.txtPositive);
        txtNegative = (TextView) findViewById(R.id.txtNegative);
        grdShelf = (GridView) findViewById(R.id.grdShelf);
        prgBar = (ProgressBar) findViewById(R.id.prgBar);

        grdShelf.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Book book = (Book) adapter.getItem(i);
                Intent it = new Intent(context, ProductDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(KEY_PRODUCT_ID, book.getId());
                bundle.putString(KEY_TITLE, book.getTitle());
                bundle.putString(KEY_ANYWAY, "0");
                it.putExtras(bundle);
                startActivity(it);
            }
        });

        Button btnEmail = (Button) findViewById(R.id.btnEmail);
        btnEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    if (memberId.equals(loginUserId))
                        Toast.makeText(context, "你的電子郵件是：" + email, Toast.LENGTH_SHORT).show();
                    else {
                        Intent it = new Intent (Intent.ACTION_VIEW , Uri.parse("mailto:" + email));
                        it.putExtra(Intent.EXTRA_SUBJECT, "主旨");
                        it.putExtra(Intent.EXTRA_TEXT, "內文");
                        startActivity(it);
                    }
                }catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        canShowProfile = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isShown)
            loadData();
    }

    private void loadData() {
        isShown = false;
        prgBar.setVisibility(View.VISIBLE);
        layInfo.setVisibility(View.INVISIBLE);

        conn = new MyOkHttp(MemberProfileActivity.this, new MyOkHttp.TaskListener() {
            @Override
            public void onFinished(final String result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result == null) {
                            Toast.makeText(context, "連線失敗", Toast.LENGTH_SHORT).show();
                            prgBar.setVisibility(View.GONE);
                            return;
                        }
                        try {
                            JSONObject resObj = new JSONObject(result);
                            if (resObj.getBoolean(KEY_STATUS)) {
                                //上架商品
                                JSONArray aryStock = resObj.getJSONArray(KEY_STOCK);
                                books = new ArrayList<>(); //此陣列最後會交由adapter的佇列(由鏈結串列製作)代管
                                for (int i=0; i<aryStock.length(); i++) {
                                    JSONObject obj = aryStock.getJSONObject(i);
                                    books.add(new Book(
                                            obj.getString(KEY_PRODUCT_ID),
                                            obj.getString(KEY_PHOTO1),
                                            obj.getString(KEY_TITLE)
                                    ));
                                }

                                //個人檔案
                                JSONObject objMember = resObj.getJSONObject(KEY_PROFILE);
                                originValue = objMember.getInt(KEY_VALUE);
                                member = new Member(
                                        objMember.getString(KEY_AVATAR),
                                        objMember.getString(KEY_NAME),
                                        objMember.getString(KEY_DEPARTMENT),
                                        objMember.getString(KEY_POSITIVE),
                                        objMember.getString(KEY_NEGATIVE),
                                        objMember.getString(KEY_EMAIL)
                                );
                                member.setGetBitmap(new GetBitmapTask(getString(R.string.link_avatar), new GetBitmapTask.TaskListener() {
                                    @Override
                                    public void onFinished() {
                                        showData();
                                        prgBar.setVisibility(View.GONE);
                                    }
                                }));
                                member.startDownloadImage();
                            }else {
                                prgBar.setVisibility(View.GONE);
                                Toast.makeText(context, "伺服器發生例外", Toast.LENGTH_SHORT).show();
                            }
                        }catch (JSONException e) {
                            prgBar.setVisibility(View.GONE);
                            Toast.makeText(context, "處理JSON發生錯誤", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        //開始連線
        try {
            JSONObject reqObj = new JSONObject();
            reqObj.put(KEY_USER_ID, loginUserId);
            reqObj.put(KEY_MEMBER_ID, memberId);
            reqObj.put(KEY_IS_SETTING, false);
            conn.execute(getString(R.string.link_show_profile), reqObj.toString());
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showData() {
        //上架商品
        adapter = new StockListAdapter(getResources(), context, books, R.layout.grd_member_shelf, 15);
        grdShelf.setAdapter(adapter);

        //個人檔案
        if (member.getImg() != null)
            imgAvatar.setImageBitmap(member.getImg());
        txtName.setText(member.getName());
        txtDepartment.setText(member.getDepartment());
        txtPositive.setText(member.getPositive());
        txtNegative.setText(member.getNegative());
        email = member.getEmail();

        if (originValue == 1)
            txtPositive.setTextColor(Color.parseColor("#00B050"));
        else if (originValue == -1)
            txtNegative.setTextColor(Color.parseColor("#FF0000"));

        books = null;
        layInfo.setVisibility(View.VISIBLE);
        isShown = true;
    }

    private void giveValue(int value) {
        TextView txtPositive = (TextView) findViewById(R.id.txtPositive);
        TextView txtNegative = (TextView) findViewById(R.id.txtNegative);

        if (value == originValue) { //撤回評價
            if (value == 1)
                adjustValue(txtPositive, -1);
            else
                adjustValue(txtNegative, -1);
            value = 0;
        }else {
            if (value == 1) { //點正評
                adjustValue(txtPositive, 1);
                txtPositive.setTextColor(Color.parseColor("#00B050"));

                if (originValue != 0) //負變正
                    adjustValue(txtNegative, -1);
            }else { //點負評
                adjustValue(txtNegative, 1);
                txtNegative.setTextColor(Color.parseColor("#FF0000"));

                if (originValue != 0) //正變負
                    adjustValue(txtPositive, -1);
            }
        }

        MyOkHttp conn = new MyOkHttp(MemberProfileActivity.this, new MyOkHttp.TaskListener() {
            @Override
            public void onFinished(final String result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject obj = new JSONObject(result);
                            if (obj.getBoolean(KEY_STATUS)) {
                                Toast.makeText(context, "評價成功", Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(context, "伺服器發生例外", Toast.LENGTH_SHORT).show();
                            }
                        }catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        //開始連線
        try {
            JSONObject reqObj = new JSONObject();
            reqObj.put(KEY_GIVER_ID, loginUserId);
            reqObj.put(KEY_RECEIVER_ID, memberId);
            reqObj.put(KEY_VALUE, String.valueOf(value));
            conn.execute(getString(R.string.link_evaluate), reqObj.toString());
        }catch (JSONException e) {
            e.printStackTrace();
        }
        originValue = value;
    }

    private void adjustValue(TextView textView, int i) {
        textView.setText(String.valueOf(Integer.parseInt(textView.getText().toString()) + i));
        if (i == -1)
            textView.setTextColor(Color.parseColor("#555555"));
    }

    @Override
    public void onPause() {
        cancelConnection();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (adapter != null) {
            adapter.destroy(true);
            adapter = null;
        }
        canShowProfile = true;
        System.gc();
        super.onDestroy();
    }

    private void cancelConnection() {
        if (conn != null)
            conn.cancel();
        if (member != null)
            member.cancelDownloadImage();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.layPositive:
                giveValue(1);
                break;
            case R.id.layNegative:
                giveValue(-1);
                break;
        }
    }
}
