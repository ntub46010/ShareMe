package com.xy.shareme_tomcat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.xy.shareme_tomcat.broadcast_helper.managers.RequestManager;
import com.xy.shareme_tomcat.data.Member;
import com.xy.shareme_tomcat.network_helper.MyOkHttp;

import org.json.JSONException;
import org.json.JSONObject;

import static com.xy.shareme_tomcat.data.DataHelper.KEY_AVATAR;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_DEPARTMENT;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_EMAIL;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_GENDER;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_NAME;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PASSWORD;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PROFILE;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_STATUS;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_USER_ID;
import static com.xy.shareme_tomcat.data.DataHelper.getMD5;
import static com.xy.shareme_tomcat.data.DataHelper.getSpnDepCode;
import static com.xy.shareme_tomcat.data.DataHelper.loginUserId;
import static com.xy.shareme_tomcat.data.DataHelper.myGender;
import static com.xy.shareme_tomcat.data.DataHelper.myName;
import static com.xy.shareme_tomcat.data.DataHelper.tmpToken;
import static com.xy.shareme_tomcat.broadcast_helper.beans.custom.UserData.DATABASE_USERS;

public class LoginActivity extends Activity {
    private Context context;
    private SharedPreferences sp;

    private LinearLayout layLoginField;
    private ScrollView layRegisterField;
    private EditText edtLogAcc, edtLogPwd, edtRegAcc, edtRegPwd, edtRegPwd2, edtRegName, edtRegEmail;
    private RadioGroup rgpRegGender;
    private CheckBox chkAutoLogin;
    private ProgressBar prgBar;

    private Member member;
    private String method = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = this;
        sp = getSharedPreferences(getString(R.string.sp_fileName), MODE_PRIVATE);

        //登入元件
        layLoginField = (LinearLayout) findViewById(R.id.layLoginField);
        edtLogAcc = (EditText) findViewById(R.id.edtAccount);
        edtLogPwd = (EditText) findViewById(R.id.edtPassword);
        chkAutoLogin = (CheckBox)  findViewById(R.id.chkAutoLogin);
        TextView txtRegister = (TextView) findViewById(R.id.txtRegister);
        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        prgBar = (ProgressBar) findViewById(R.id.prgBar);

        txtRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layLoginField.setVisibility(View.GONE);
                layRegisterField.setVisibility(View.VISIBLE);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                member = new Member();
                member.setAcc(edtLogAcc.getText().toString());;
                member.setPwd(edtLogPwd.getText().toString());
                if (!member.getAcc().equals("") && !member.getPwd().equals(""))
                    registerDevice(member);
            }
        });

        //註冊元件
        layRegisterField = (ScrollView) findViewById(R.id.layRegisterField);
        edtRegAcc = (EditText) findViewById(R.id.edtRegAccount);
        edtRegPwd = (EditText) findViewById(R.id.edtRegPassword);
        edtRegPwd2 = (EditText) findViewById(R.id.edtRegPasswordAgain);
        edtRegName = (EditText) findViewById(R.id.edtRegName);
        edtRegEmail = (EditText) findViewById(R.id.edtRegEmail);
        rgpRegGender = (RadioGroup) findViewById(R.id.rgpRegGender);
        Spinner spnRegDep = (Spinner) findViewById(R.id.spnRegDepartment);
        Button btnRegister = (Button) findViewById(R.id.btnRegConfirm);
        Button btnCancel = (Button) findViewById(R.id.btnRegCancel);

        spnRegDep.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (member != null)
                    member.setDepartment(getSpnDepCode(i));;
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                member = new Member();
                member.setAcc(edtRegAcc.getText().toString());
                member.setPwd(edtRegPwd.getText().toString());
                member.setPwd2(edtRegPwd2.getText().toString());
                member.setName(edtRegName.getText().toString());
                member.setEmail(edtRegEmail.getText().toString());

                switch (rgpRegGender.getCheckedRadioButtonId()) {
                    case R.id.rdoRegMale:
                        member.setGender("1");
                        break;
                    case R.id.rdoRegFemale:
                        member.setGender("0");
                        break;
                    default:
                        member.setGender("");
                }

                if (isRegisterInfoValid(member))
                    registerMember(member);
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layRegisterField.setVisibility(View.GONE);
                layLoginField.setVisibility(View.VISIBLE);
            }
        });

        if (sp.getBoolean(getString(R.string.sp_isAutoLogin), false)) {
            member = new Member();
            member.setAcc(sp.getString(getString(R.string.sp_myLoginUserId), ""));
            member.setPwd(sp.getString(getString(R.string.sp_myPassword), ""));
            registerDevice(member);
        }
    }

    private void registerDevice(Member member) {
        //連線確認登入帳密，成功後一律向Firebase重新註冊裝置Token，再取回
        layLoginField.setVisibility(View.GONE);
        prgBar.setVisibility(View.VISIBLE);
        method = "registerDevice";
        initTrdTimer(); //開始計時，30秒未登入成功將會顯示逾時
        initTrdFlag();

        MyOkHttp conn = new MyOkHttp(LoginActivity.this, new MyOkHttp.TaskListener() {
            @Override
            public void onFinished(final String result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result == null) {
                            Toast.makeText(context, "連線失敗", Toast.LENGTH_SHORT).show();
                            layLoginField.setVisibility(View.VISIBLE);
                            prgBar.setVisibility(View.GONE);
                            return;
                        }
                        try {
                            JSONObject resObj = new JSONObject(result);
                            if (resObj.getBoolean(KEY_STATUS)) {
                                //帳密正確，存取所需資料
                                JSONObject obj = resObj.getJSONObject(KEY_PROFILE);
                                myName = obj.getString(KEY_NAME);
                                myGender = obj.getInt(KEY_GENDER);
                            }else { //BUG，登入失敗後，之後若登入成功，會出現兩個MainActivity
                                Toast.makeText(context, "帳號或密碼錯誤", Toast.LENGTH_SHORT).show();
                                loginUserId = "failed"; //若給予空字串，會出現連線逾時
                                layLoginField.setVisibility(View.VISIBLE);
                                prgBar.setVisibility(View.GONE);
                            }
                        }catch (JSONException e) {
                            Toast.makeText(context, "處理JSON發生錯誤", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        //開始連線
        try {
            JSONObject reqObj = new JSONObject();
            reqObj.put(KEY_USER_ID, member.getAcc());
            reqObj.put(KEY_PASSWORD, getMD5(member.getPwd()));
            conn.execute(getString(R.string.link_login), reqObj.toString());
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean isRegisterInfoValid(Member member) {
        //還不能阻擋在帳密輸入中文
        String errMsg = "";
        if (member.getAcc().length() < 8 || member.getAcc().length() > 10)
            errMsg += "帳號長度錯誤\n";

        if (member.getPwd().length() < 6 || member.getPwd().length() > 15)
            errMsg += "密碼長度錯誤\n";
        else if (!member.getPwd().equals(member.getPwd2()))
            errMsg += "確認密碼錯誤\n";

        if (member.getName().length() < 1)
            errMsg += "姓名未輸入\n";

        if (!member.getEmail().contains("@") || member.getEmail().indexOf("@") == 0 || member.getEmail().indexOf("@") == member.getEmail().length() - 1)
            errMsg += "信箱格式錯誤\n";

        if (member.getGender().equals(""))
            errMsg += "性別未選擇\n";

        if (!errMsg.equals("")){
            errMsg = "註冊資料不正確：\n" + errMsg.substring(0, errMsg.length() - 1);
            AlertDialog.Builder msgbox = new AlertDialog.Builder(this);
            msgbox.setTitle("註冊帳號")
                    .setMessage(errMsg)
                    .setPositiveButton("確定", null)
                    .show();
            return false;
        }else
            return true;
    }

    private void registerMember(final Member member) {
        layRegisterField.setVisibility(View.GONE);
        prgBar.setVisibility(View.VISIBLE);
        initTrdFlag();
        method = "registerMember";

        MyOkHttp conn = new MyOkHttp(LoginActivity.this, new MyOkHttp.TaskListener() {
            @Override
            public void onFinished(String result) {
                if (result == null) {
                    Toast.makeText(context, "連線失敗", Toast.LENGTH_SHORT).show();
                    layRegisterField.setVisibility(View.VISIBLE);
                    prgBar.setVisibility(View.GONE);
                    return;
                }
                try {
                    JSONObject resObj = new JSONObject(result);
                    if (resObj.getBoolean(KEY_STATUS)) {
                        //註冊成功
                        registerDevice(member);
                    }else {
                        method = "";
                        Toast.makeText(context, "該帳號已被使用", Toast.LENGTH_SHORT).show();
                        layRegisterField.setVisibility(View.VISIBLE);
                        prgBar.setVisibility(View.GONE);
                    }
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        //開始連線
        try {
            JSONObject reqObj = new JSONObject();
            reqObj.put(KEY_USER_ID, member.getAcc());
            reqObj.put(KEY_PASSWORD, member.getPwd());
            reqObj.put(KEY_NAME, member.getName());
            reqObj.put(KEY_EMAIL, member.getEmail());
            reqObj.put(KEY_DEPARTMENT, member.getDepartment());
            reqObj.put(KEY_GENDER, member.getGender());
            conn.execute(getString(R.string.link_register), reqObj.toString());
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initTrdTimer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(30000);
                }catch (Exception e) {
                    e.printStackTrace();
                }
                hdrTimer.sendMessage(hdrTimer.obtainMessage());
            }
        }).start();
    }

    private Handler hdrTimer = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            layLoginField.setVisibility(View.VISIBLE);
            prgBar.setVisibility(View.GONE);
            if (loginUserId.equals(""))
                Toast.makeText(context, "登入連線逾時", Toast.LENGTH_SHORT).show();
        }
    };

    private void initTrdFlag() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                hdrFlag.sendMessage(hdrFlag.obtainMessage());
            }
        }).start();
    }

    private Handler hdrFlag = new Handler() { //等待token值從預設文字被改為空
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (!myName.equals("")) {
                switch (method) {
                    case "registerDevice":
                        deleteOriginalToken(member.getAcc());
                        break;
                    case "registerMember":
                        Toast.makeText(context, "註冊成功", Toast.LENGTH_SHORT).show();
                        registerDevice(member);
                        break;
                }
            }else
                initTrdFlag();
        }
    };

    private void deleteOriginalToken(String userId) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child(DATABASE_USERS).child(userId).removeValue(); //刪除原本token
        RequestManager.getInstance().getTokenById(userId); //嘗試取得空字串Token(代表已刪除)，存到DataHelper.tmpToken
        initTrdWaitDelete();
    }

    private void initTrdWaitDelete() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                hdrWaitDelete.sendMessage(hdrWaitDelete.obtainMessage());
            }
        }).start();
    };

    private Handler hdrWaitDelete = new Handler() { //等待token值從預設文字被改為空
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (tmpToken.equals("")) { //確定token已刪除，開始註冊新token
                RequestManager.getInstance().insertUserPushData(member.getAcc());
                RequestManager.getInstance().getTokenById(member.getAcc());
                initTrdWaitLogin(); //嘗試取得新token，取得後即可真正的登入
            }else
                initTrdWaitDelete(); //繼續嘗試取得Token，存到DataHelper.tmpToken
        }
    };

    private void initTrdWaitLogin() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                hdrWaitLogin.sendMessage(hdrWaitLogin.obtainMessage());
            }
        }).start();
    }

    private Handler hdrWaitLogin = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (!tmpToken.equals("token")) { //已取得新token
                tmpToken = "token"; //恢復至尚未取得之值
                loginUserId = member.getAcc();
                writeLoginRecord();
                startActivity(new Intent(context, MainActivity.class)); //真正登入
                prgBar.setVisibility(View.GONE);
                layLoginField.setVisibility(View.INVISIBLE);
                layRegisterField.setVisibility(View.INVISIBLE);
                finish();
            }else
                initTrdWaitLogin();
        }
    };

    private void writeLoginRecord () {
        sp.edit()
                .putString(getString(R.string.sp_myLoginUserId), loginUserId)
                .putString(getString(R.string.sp_myName), myName)
                .putInt(getString(R.string.sp_myGender), myGender)
                .apply();
        if (chkAutoLogin.isChecked()) {
            sp.edit()
                    .putString(getString(R.string.sp_myPassword), member.getPwd())
                    .putBoolean(getString(R.string.sp_isAutoLogin), true)
                    .apply();
        }else {
            sp.edit()
                    .putString(getString(R.string.sp_myPassword), "")
                    .putBoolean(getString(R.string.sp_isAutoLogin), false)
                    .apply();
        }
    }

    @Override
    public void onBackPressed() {
        if (layRegisterField.getVisibility() == View.VISIBLE) {
            layRegisterField.setVisibility(View.GONE);
            layLoginField.setVisibility(View.VISIBLE);
        }else
            super.onBackPressed();
    }

}
