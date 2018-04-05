package com.xy.shareme_tomcat.Settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.xy.shareme_tomcat.Member.MemberProfileActivity;
import com.xy.shareme_tomcat.R;
import com.xy.shareme_tomcat.data.Member;
import com.xy.shareme_tomcat.network_helper.GetBitmapTask;
import com.xy.shareme_tomcat.network_helper.MyOkHttp;

import org.json.JSONException;
import org.json.JSONObject;

import static com.xy.shareme_tomcat.data.DataHelper.KEY_AVATAR;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_DEPARTMENT;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_EMAIL;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_IS_SETTING;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_MEMBER_ID;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_NAME;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PASSWORD;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PROFILE;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_STATUS;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_USER_ID;
import static com.xy.shareme_tomcat.data.DataHelper.getSpnDepCode;
import static com.xy.shareme_tomcat.data.DataHelper.loginUserId;

public class SettingProfileActivity extends AppCompatActivity {
    private Context context;
    private LinearLayout layInfo;
    private EditText edtName, edtEmail, edtOldPwd, edtNewPwd, edtNewPwd2;
    private Spinner spnDep;
    private ImageButton btnSelectAvatar;
    private ImageView btnUpdateInfo;
    private ProgressBar prgBar;

    private Member member;
    private MyOkHttp conShowProfile, conEditProfile;
    private boolean isShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_profile);
        context = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView txtBarTitle = (TextView) toolbar.findViewById(R.id.txtToolbarTitle);
        txtBarTitle.setText("設定個人檔案");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        layInfo = (LinearLayout) findViewById(R.id.layInfo);
        edtName = (EditText) findViewById(R.id.edtName);
        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtOldPwd = (EditText) findViewById(R.id.edtOldPwd);
        edtNewPwd = (EditText) findViewById(R.id.edtNewPwd);
        edtNewPwd2 = (EditText) findViewById(R.id.edtNewPwd2);
        spnDep = (Spinner) findViewById(R.id.spnDepartment);
        btnSelectAvatar = (ImageButton) findViewById(R.id.btnSelectAvatar);
        btnUpdateInfo = (ImageView) toolbar.findViewById(R.id.btnSubmit);
        prgBar = (ProgressBar) findViewById(R.id.prgBar);

        layInfo.setVisibility(View.INVISIBLE);
        spnDep.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    member.setDepartment(getSpnDepCode(i));
                }catch (NullPointerException e) {}
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        btnUpdateInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isShown)
            loadData();
    }

    private void loadData() {
        isShown = false;
        btnUpdateInfo.setVisibility(View.GONE);

        conShowProfile = new MyOkHttp(SettingProfileActivity.this, new MyOkHttp.TaskListener() {
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
                                JSONObject obj = resObj.getJSONObject(KEY_PROFILE);
                                member = new Member(
                                        obj.getString(KEY_AVATAR),
                                        obj.getString(KEY_NAME),
                                        obj.getString(KEY_DEPARTMENT),
                                        obj.getString(KEY_EMAIL),
                                        obj.getString(KEY_PASSWORD)
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
            reqObj.put(KEY_MEMBER_ID, loginUserId);
            reqObj.put(KEY_IS_SETTING, true);
            conShowProfile.execute(getString(R.string.link_show_profile), reqObj.toString());
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showData() {
        if (member.getImg() != null)
            btnSelectAvatar.setImageBitmap(member.getImg());

        edtName.setText(member.getName());
        edtEmail.setText(member.getEmail());

        switch (member.getDepartment()) {
            case "五專會計統計科":
                spnDep.setSelection(0);
                break;
            case "五專財務金融科":
                spnDep.setSelection(1);
                break;
            case "五專財政稅務科":
                spnDep.setSelection(2);
                break;
            case "五專國際貿易科":
                spnDep.setSelection(3);
                break;
            case "五專企業管理科":
                spnDep.setSelection(4);
                break;
            case "五專資訊管理科":
                spnDep.setSelection(5);
                break;
            case "五專應用外語科":
                spnDep.setSelection(6);
                break;
            case "四技會計資訊系":
                spnDep.setSelection(7);
                break;
            case "四技財務金融系":
                spnDep.setSelection(8);
                break;
            case "四技財政稅務系":
                spnDep.setSelection(9);
                break;
            case "四技國際商務系":
                spnDep.setSelection(10);
                break;
            case "四技企業管理系":
                spnDep.setSelection(11);
                break;
            case "四技資訊管理系":
                spnDep.setSelection(12);
                break;
            case "四技應用外語系":
                spnDep.setSelection(13);
                break;
            case "四技商業設計管理系":
                spnDep.setSelection(14);
                break;
            case "四技商品創意經營系":
                spnDep.setSelection(15);
                break;
            case "四技數位多媒體設計系":
                spnDep.setSelection(16);
                break;
            case "二技會計資訊系":
                spnDep.setSelection(17);
                break;
            case "二技財務金融系":
                spnDep.setSelection(18);
                break;
            case "二技財政稅務系":
                spnDep.setSelection(19);
                break;
            case "二技國際商務系":
                spnDep.setSelection(20);
                break;
            case "二技企業管理系":
                spnDep.setSelection(21);
                break;
            case "二技資訊管理系":
                spnDep.setSelection(22);
                break;
            case "二技應用外語系":
                spnDep.setSelection(23);
                break;
        }

        layInfo.setVisibility(View.VISIBLE);
        btnUpdateInfo.setVisibility(View.VISIBLE);
        isShown = true;
    }

    private void updateProfile() {
        if (!isInfoValid(edtName.getText().toString(), edtEmail.getText().toString(), edtOldPwd.getText().toString(), edtNewPwd.getText().toString(), edtNewPwd2.getText().toString()))
            return;

        btnUpdateInfo.setEnabled(false);
        conEditProfile = new MyOkHttp(SettingProfileActivity.this, new MyOkHttp.TaskListener() {
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
                                Toast.makeText(context, "編輯成功", Toast.LENGTH_SHORT).show();

                                Intent it = new Intent(context, MemberProfileActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString(KEY_MEMBER_ID, loginUserId);
                                it.putExtras(bundle);
                                startActivity(it);
                                finish();
                            }else
                                Toast.makeText(context, "編輯失敗", Toast.LENGTH_SHORT).show();
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
            reqObj.put(KEY_USER_ID, loginUserId);
            reqObj.put(KEY_AVATAR, member.getImgURL());
            reqObj.put(KEY_NAME, member.getName());
            reqObj.put(KEY_DEPARTMENT, member.getDepartment());
            reqObj.put(KEY_EMAIL, member.getEmail());
            reqObj.put(KEY_PASSWORD, member.getPwd());
            conEditProfile.execute(getString(R.string.link_edit_profile), reqObj.toString());
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean isInfoValid(String name, String email, String oldPwd, String pwd1, String pwd2) {
        StringBuffer errMsg = new StringBuffer("");
        if (name.length() < 1)
            errMsg.append("姓名未輸入\n");

        if (!email.contains("@") || email.indexOf("@") == 0 || email.indexOf("@") == email.length() - 1)
            errMsg.append("信箱格式錯誤\n");

        if (!oldPwd.equals("")) {
            if (!oldPwd.equals(member.getPwd()))
                errMsg.append("原密碼錯誤\n");
            else {
                if (pwd1.length() < 6 || pwd1.length() > 15)
                    errMsg.append("新密碼長度錯誤\n");
                else if (!pwd1.equals(pwd2))
                    errMsg.append("確認密碼錯誤\n");
            }
        }

        if (errMsg.length() == 0) {
            member.setName(name);
            member.setEmail(email);
            if (!pwd1.equals(""))
                member.setPwd(pwd1);
            return true;
        }else {
            AlertDialog.Builder msgbox = new AlertDialog.Builder(context);
            msgbox.setTitle("編輯個人檔案")
                    .setPositiveButton("確定", null)
                    .setMessage(errMsg.substring(0, errMsg.length() - 1))
                    .show();
            return false;
        }
    }

    @Override
    public void onPause() {
        cancelConnection();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        member.setImg(null);
        member = null;
        System.gc();
        super.onDestroy();
    }

    private void cancelConnection() {
        if (conShowProfile != null)
            conShowProfile.cancel();
        if (conEditProfile != null)
            conEditProfile.cancel();
        if (member != null)
            member.cancelDownloadImage();
    }
}
