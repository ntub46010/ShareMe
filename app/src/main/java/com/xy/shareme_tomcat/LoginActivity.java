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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.xy.shareme_tomcat.broadcast_helper.managers.RequestManager;
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
//import static com.xy.shareme_tomcat.data.DataHelper.conFlag;
import static com.xy.shareme_tomcat.data.DataHelper.getSpnDepCode;
import static com.xy.shareme_tomcat.data.DataHelper.loginUserId;
import static com.xy.shareme_tomcat.data.DataHelper.myAvatarUrl;
import static com.xy.shareme_tomcat.data.DataHelper.myGender;
import static com.xy.shareme_tomcat.data.DataHelper.myName;
import static com.xy.shareme_tomcat.data.DataHelper.tmpToken;
import static com.xy.shareme_tomcat.broadcast_helper.beans.custom.UserData.DATABASE_USERS;

public class LoginActivity extends Activity {
    private Context context;
    private SharedPreferences sp;

    private LinearLayout layLoginField, layRegisterField;
    private EditText edtLogAcc, edtLogPwd, edtRegAcc, edtRegPwd, edtRegPwd2, edtRegName, edtRegEmail;
    private TextView txtRegister;
    private Button btnLogin, btnRegister, btnCancel;
    private RadioGroup rgpRegGender;
    private Spinner spnRegDep;
    private ProgressBar prgBar;

    private String userId = "", pwd = "", department = "51", method = "";

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
        txtRegister = (TextView) findViewById(R.id.txtRegister);
        txtRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layLoginField.setVisibility(View.GONE);
                layRegisterField.setVisibility(View.VISIBLE);
            }
        });
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userId = edtLogAcc.getText().toString();
                pwd = edtLogPwd.getText().toString();
                if (!userId.equals("") && !pwd.equals(""))
                    registerDevice(userId, pwd);
            }
        });

        //註冊元件
        layRegisterField = (LinearLayout) findViewById(R.id.layRegisterField);
        edtRegAcc = (EditText) findViewById(R.id.edtRegAccount);
        edtRegPwd = (EditText) findViewById(R.id.edtRegPassword);
        edtRegPwd2 = (EditText) findViewById(R.id.edtRegPasswordAgain);
        edtRegName = (EditText) findViewById(R.id.edtRegName);
        edtRegEmail = (EditText) findViewById(R.id.edtRegEmail);
        rgpRegGender = (RadioGroup) findViewById(R.id.rgpRegGender);
        spnRegDep = (Spinner) findViewById(R.id.spnRegDepartment);
        btnRegister = (Button) findViewById(R.id.btnRegConfirm);
        btnCancel = (Button) findViewById(R.id.btnRegCancel);

        spnRegDep.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                department = getSpnDepCode(i);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String acc = edtRegAcc.getText().toString();
                String pwd = edtRegPwd.getText().toString();
                String pwd2 = edtRegPwd2.getText().toString();
                String name = edtRegName.getText().toString();
                String email = edtRegEmail.getText().toString();

                String gender;
                switch (rgpRegGender.getCheckedRadioButtonId()) {
                    case R.id.rdoRegMale:
                        gender = "1";
                        break;
                    case R.id.rdoRegFemale:
                        gender = "0";
                        break;
                    default:
                        gender = "";
                }

                if (isInfoValid(acc, pwd, pwd2, name, email, gender)) {
                    registerMember(acc, pwd, name, email, gender);
                }

            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layRegisterField.setVisibility(View.GONE);
                layLoginField.setVisibility(View.VISIBLE);
            }
        });

        prgBar = (ProgressBar) findViewById(R.id.prgBar);
        prgBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
    }

    private void registerDevice(final String userId, final String pwd) {
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
                                myGender = obj.getBoolean(KEY_GENDER) ? 1 : 0;
                                myAvatarUrl = getString(R.string.link_avatar) + obj.getString(KEY_AVATAR);
                            }else { //BUG，登入失敗後，之後若登入成功，會出現兩個MainActivity
                                Toast.makeText(context, "帳號或密碼錯誤", Toast.LENGTH_SHORT).show();
                                loginUserId = "failed"; //若給予空字串，會出現連線逾時
                                layLoginField.setVisibility(View.VISIBLE);
                                prgBar.setVisibility(View.GONE);
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
            reqObj.put(KEY_USER_ID, userId);
            reqObj.put(KEY_PASSWORD, pwd);
            conn.execute(getString(R.string.link_login), reqObj.toString());
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean isInfoValid (String acc, String pwd, String pwd2, String name, String email, String gender) {
        //還不能阻擋在帳密輸入中文
        String errMsg = "";
        if (acc.length() < 8 || acc.length() > 10)
            errMsg += "帳號長度錯誤\n";

        if (pwd.length() < 6 || pwd.length() > 15)
            errMsg += "密碼長度錯誤\n";
        else if (!pwd.equals(pwd2))
            errMsg += "確認密碼錯誤\n";

        if (name.length() < 1)
            errMsg += "姓名未輸入\n";

        if (!email.contains("@") || email.indexOf("@") == 0 || email.indexOf("@") == email.length() - 1)
            errMsg += "信箱格式錯誤\n";

        if (gender.equals(""))
            errMsg += "性別未選擇\n";

        if (!errMsg.equals("")){
            errMsg = "註冊資料不正確：\n" + errMsg.substring(0, errMsg.length() - 1);
            AlertDialog.Builder msgbox = new AlertDialog.Builder(this);
            msgbox.setTitle("註冊帳號")
                    .setMessage(errMsg)
                    .setPositiveButton("確定", null)
                    .show();
            return false;
        }else {
            this.userId = acc;
            this.pwd = pwd;
            return true;
        }
    }

    private void registerMember(final String userId, final String pwd, String name, String email, String gender) {
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
            reqObj.put(KEY_USER_ID, userId);
            reqObj.put(KEY_PASSWORD, pwd);
            reqObj.put(KEY_NAME, name);
            reqObj.put(KEY_DEPARTMENT, department);
            reqObj.put(KEY_GENDER, gender);
            reqObj.put(KEY_EMAIL, email);
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
                    Thread.sleep(500);
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
                        deleteOriginalToken(userId);
                        break;
                    case "registerMember":
                        Toast.makeText(context, "註冊成功", Toast.LENGTH_SHORT).show();
                        registerDevice(userId, pwd);
                        break;
                    case "":
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

    private  Handler hdrWaitDelete = new Handler() { //等待token值從預設文字被改為空
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (tmpToken.equals("")) { //確定token已刪除，開始註冊新token
                RequestManager.getInstance().insertUserPushData(userId);
                RequestManager.getInstance().getTokenById(userId);
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
                prgBar.setVisibility(View.GONE);
                loginUserId = userId;
                writeLoginRecord();

                startActivity(new Intent(context, MainActivity.class)); //真正登入
                finish();
            }else
                initTrdWaitLogin();
        }
    };

    private void writeLoginRecord () {
        sp.edit()
                .putString(getString(R.string.sp_myLoginUserId), loginUserId)
                .putString(getString(R.string.sp_myAvatar), myAvatarUrl)
                .putString(getString(R.string.sp_myName), myName)
                .putInt(getString(R.string.sp_myGender), myGender)
                .apply();
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
