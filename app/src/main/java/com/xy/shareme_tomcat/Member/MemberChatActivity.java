package com.xy.shareme_tomcat.Member;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.xy.shareme_tomcat.R;

import static com.xy.shareme_tomcat.data.DataHelper.KEY_NAME;

public class MemberChatActivity extends AppCompatActivity {
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_chat);
        context = this;
        Bundle bundle = getIntent().getExtras();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView txtBarTitle = (TextView) findViewById(R.id.txtToolbarTitle);
        txtBarTitle.setText(bundle.getString(KEY_NAME));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
