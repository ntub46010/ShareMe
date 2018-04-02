package com.xy.shareme_tomcat.Member;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
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

import java.util.ArrayList;

import static com.xy.shareme_tomcat.data.DataHelper.KEY_PRODUCT_ID;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PRODUCT_NAME;
import static com.xy.shareme_tomcat.data.DataHelper.isProfileAlive;

public class MemberProfileActivity extends AppCompatActivity {
    private Context context;
    private Toolbar toolbar;
    private LinearLayout layInfo;
    private ImageView imgAvatar;
    private TextView txtName, txtDepartment, txtPositive, txtNegative;
    private LinearLayout layLike, layDislike;
    private ImageButton btnPositive, btnNegative;
    private GridView grdShelf;
    private ProgressBar prgBar;

    private ArrayList<ImageObj> members, books;
    private StockListAdapter adapter;

    private String email = "email";
    private boolean isShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_profile);
        context = this;
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        //Toolbar文字
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
        layLike = (LinearLayout) findViewById(R.id.layPositive);
        layDislike = (LinearLayout) findViewById(R.id.layNegative);
        btnPositive = (ImageButton) findViewById(R.id.btnPositive);
        btnNegative = (ImageButton) findViewById(R.id.btnNegative);
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
                bundle.putString(KEY_PRODUCT_NAME, book.getTitle());
                it.putExtras(bundle);
                startActivity(it);
            }
        });

        Button btnEmail = (Button) findViewById(R.id.btnEmail);
        btnEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    Intent intent = new Intent (Intent.ACTION_VIEW , Uri.parse("mailto:" + email));
                    //intent.putExtra(Intent.EXTRA_SUBJECT, "【北商二手書交易】想購買...");
                    //intent.putExtra(Intent.EXTRA_TEXT, "信件內文");
                    Toast.makeText(context, email, Toast.LENGTH_SHORT).show();
                    startActivity(intent);
                }catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        isProfileAlive = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        /*
        if (!isShown)
            loadData();
        */
    }
}
