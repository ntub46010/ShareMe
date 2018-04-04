package com.xy.shareme_tomcat.Member;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.xy.shareme_tomcat.R;

import static com.xy.shareme_tomcat.data.DataHelper.KEY_MEMBER_ID;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_USER_ID;
import static com.xy.shareme_tomcat.data.DataHelper.loginUserId;
import static com.xy.shareme_tomcat.data.DataHelper.myGender;
import static com.xy.shareme_tomcat.data.DataHelper.getSimpleAdapter;
import static com.xy.shareme_tomcat.MainActivity.context;

public class MemberHomeFrag extends Fragment {
    private String[] title;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_member_home, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        title = new String[] {
                "個人檔案",
                "我的最愛",
                "信箱",
                "商品管理"
        };
        int[] iconId = {
                R.drawable.icon_profile_boy,
                R.drawable.icon_favorite,
                R.drawable.icon_mailbox,
                R.drawable.icon_package
        };
        if (myGender == 0)
            iconId[0] = R.drawable.icon_profile_girl;

        ListView listView = (ListView) getView().findViewById(R.id.lstMemHome);
        listView.setAdapter(getSimpleAdapter(
                context,
                R.layout.lst_text_with_icon,
                R.id.imgIcon,
                R.id.txtTitle,
                iconId,
                title
        ));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showActivity(title[position]);
            }
        });
    }

    private void showActivity(String itemName) {
        switch (itemName) {
            case "個人檔案":
                Intent it = new Intent(context, MemberProfileActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(KEY_MEMBER_ID, loginUserId);
                it.putExtras(bundle);
                startActivity(it);
                break;
            case "我的最愛":
                startActivity(new Intent(context, MemberFavoriteActivity.class));
                break;
            case "信箱":
                startActivity(new Intent(context, MemberMailboxActivity.class));
                break;
            case "商品管理":
                startActivity(new Intent(context, MemberStockActivity.class));
                break;
        }
    }
}
