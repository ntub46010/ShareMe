package com.xy.shareme_tomcat.Settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.xy.shareme_tomcat.R;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.xy.shareme_tomcat.data.DataHelper.getSimpleAdapter;
import static com.xy.shareme_tomcat.data.DataHelper.loginUserId;
import static com.xy.shareme_tomcat.MainActivity.context;
import static com.xy.shareme_tomcat.broadcast_helper.beans.custom.UserData.DATABASE_USERS;

public class SettingHomeFrag extends Fragment {
    private String[] groupFirst = {"通知"};
    private String[] groupSecond = {"帳號設定", "登出"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frag_setting_home, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        int[] iconId = new int[] {
                R.drawable.icon_setting,
                R.drawable.icon_logout
        };

        //第一個ListView(加了開關，所以要自訂Adapter)
        ListView listView1 = (ListView) getView().findViewById(R.id.lstSetHome_first);
        listView1.setAdapter(new SettingHomeAdapter(context));

        //第二個ListView(純顯示，只需使用內建Adapter)
        ListView listView2 = (ListView) getView().findViewById(R.id.lstSetHome_second);
        listView2.setAdapter(getSimpleAdapter(
                context,
                R.layout.lst_text_with_icon,
                R.id.imgIcon,
                R.id.txtTitle,
                iconId,
                groupSecond
        ));
        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showActivity(groupSecond[position]);
            }
        });
    }

    private void showActivity(String itemName) {
        switch (itemName) {
            case "帳號設定":
                //startActivity(new Intent(context, SettingProfileActivity.class));
                break;
            case "登出":
                AlertDialog.Builder msgbox = new AlertDialog.Builder(context);
                msgbox.setTitle(getString(R.string.app_name))
                        .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                logout();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .setMessage("確定要登出嗎？")
                        .show();
                break;
        }
    }

    private void logout() {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child(DATABASE_USERS).child(loginUserId).removeValue();
        getActivity().finish();
    }


    private class SettingHomeAdapter extends BaseAdapter {
        private LayoutInflater layoutInflater;
        private List<String> titleList;
        private SharedPreferences sp;
        private Switch swtNotify;

        public SettingHomeAdapter(Context context) {
            layoutInflater = LayoutInflater.from(context);

            titleList = new ArrayList<>();
            for(String item : groupFirst) {
                titleList.add(item);
            }
        }

        @Override
        public int getCount() {
            return titleList.size();
        }

        @Override
        public Object getItem(int position) {
            return titleList.get(position);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = layoutInflater.inflate(R.layout.lst_setting_home_first, parent, false);

            ImageView imgNotify = (ImageView) convertView.findViewById(R.id.imgSetHomeLst_first);
            imgNotify.setImageResource(R.drawable.icon_notification);

            TextView txtNotify = (TextView) convertView.findViewById(R.id.txtSetHomeLst_first);
            txtNotify.setText(groupFirst[0]);

            swtNotify = (Switch) convertView.findViewById(R.id.swtNotification);
            swtNotify.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    setNotify();
                }
            });
            //顯示通知開關狀態
            sp = getActivity().getSharedPreferences(getString(R.string.sp_fileName), MODE_PRIVATE);
            if (sp.getBoolean(getString(R.string.sp_showNotification), true))
                swtNotify.setChecked(true);
            else
                swtNotify.setChecked(false);

            return convertView;
        }

        private void setNotify() {
            if (swtNotify.isChecked()){
                swtNotify.setChecked(true);
                sp.edit().putBoolean(getString(R.string.sp_showNotification), true).apply();
            }else {
                swtNotify.setChecked(false);
                sp.edit().putBoolean(getString(R.string.sp_showNotification), false).apply();
            }
        }
    }

}
