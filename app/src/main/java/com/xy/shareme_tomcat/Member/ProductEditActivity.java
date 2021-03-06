package com.xy.shareme_tomcat.Member;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.xy.shareme_tomcat.R;
import com.xy.shareme_tomcat.adapter.ImageUploadAdapter;
import com.xy.shareme_tomcat.data.AlbumImageProvider;
import com.xy.shareme_tomcat.data.Book;
import com.xy.shareme_tomcat.data.ImageChild;
import com.xy.shareme_tomcat.data.Verifier;
import com.xy.shareme_tomcat.network_helper.GetBitmapBatch;
import com.xy.shareme_tomcat.network_helper.MyOkHttp;
import com.xy.shareme_tomcat.structure.ImageUploadQueue;

import org.json.JSONException;
import org.json.JSONObject;

import static com.xy.shareme_tomcat.data.DataHelper.KEY_ANYWAY;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_CONDITION;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_NOTE;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PHOTO1;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PHOTO2;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PHOTO3;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PHOTO4;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PHOTO5;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PRICE;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PRODUCT;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PRODUCT_ID;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_PS;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_STATUS;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_TITLE;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_TYPE;
import static com.xy.shareme_tomcat.data.DataHelper.KEY_USER_ID;
import static com.xy.shareme_tomcat.data.DataHelper.loginUserId;
import static com.xy.shareme_tomcat.data.DataHelper.showFoundStatus;

public class ProductEditActivity extends AppCompatActivity implements View.OnClickListener {
    private Context context;

    private LinearLayout layDetail;
    private ProgressBar prgBar;
    private EditText edtTitle, edtStatus, edtPrice, edtPS;
    private CheckBox chkAI, chkFN, chkFT, chkIB, chkBM, chkIM, chkAF, chkCD, chkCC, chkDM, chkGN;
    private Spinner spnNote;
    private ImageView btnPost;
    private TextView txtId;

    private Book book;
    private ImageUploadAdapter adapter;
    private RecyclerView recyclerView;
    private AlbumImageProvider provider;

    private String bookId, note = "0";
    private StringBuffer sbDep;

    private MyOkHttp conLoadDetail, conEditProduct;
    private GetBitmapBatch getBitmap;

    private Dialog dlgUpload;
    private TextView txtUploadHint;
    private boolean isShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_edit);
        context = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView txtBarTitle = (TextView) toolbar.findViewById(R.id.txtToolbarTitle);
        txtBarTitle.setText("編輯商品");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Bundle bundle = getIntent().getExtras();
        bookId = bundle.getString(KEY_PRODUCT_ID);

        layDetail = (LinearLayout) findViewById(R.id.layDetail);
        layDetail.setVisibility(View.INVISIBLE);
        prgBar = (ProgressBar) findViewById(R.id.prgBar);

        txtId = (TextView) findViewById(R.id.txtId);
        edtTitle = (EditText) findViewById(R.id.edtTitle);
        edtStatus = (EditText) findViewById(R.id.edtStatus);
        edtPrice = (EditText) findViewById(R.id.edtPrice);
        edtPS = (EditText) findViewById(R.id.edtPS);
        chkGN = (CheckBox) findViewById(R.id.chkGN);
        chkAI = (CheckBox) findViewById(R.id.chkAI);
        chkFN = (CheckBox) findViewById(R.id.chkFN);
        chkFT = (CheckBox) findViewById(R.id.chkFT);
        chkIB = (CheckBox) findViewById(R.id.chkIB);
        chkBM = (CheckBox) findViewById(R.id.chkBM);
        chkIM = (CheckBox) findViewById(R.id.chkIM);
        chkAF = (CheckBox) findViewById(R.id.chkAF);
        chkCD = (CheckBox) findViewById(R.id.chkCD);
        chkCC = (CheckBox) findViewById(R.id.chkCC);
        chkDM = (CheckBox) findViewById(R.id.chkDM);

        spnNote = (Spinner) findViewById(R.id.spnNote);
        spnNote.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                note = String.valueOf(i); //0為請選擇
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        prepareDialog();

        btnPost = (ImageView) toolbar.findViewById(R.id.btnSubmit);
        btnPost.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isShown)
            loadData();
    }

    private void prepareDialog() {
        dlgUpload = new Dialog(context);
        dlgUpload.setContentView(R.layout.dlg_uploading);
        dlgUpload.setCancelable(false);
        txtUploadHint = (TextView) dlgUpload.findViewById(R.id.txtHint);
        LinearLayout layUpload = (LinearLayout) dlgUpload.findViewById(R.id.layUpload);
        layUpload.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder msgbox = new AlertDialog.Builder(context);
                msgbox.setTitle("編輯商品")
                        .setMessage("確定取消上傳嗎？")
                        .setNegativeButton("否", null)
                        .setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try {
                                    adapter.cancelUpload();
                                    conEditProduct.cancel();
                                    Toast.makeText(context, "上傳已取消", Toast.LENGTH_SHORT).show();
                                }catch (NullPointerException e) {}
                            }
                        }).show();
                return true;
            }
        });
    }

    private void loadData() {
        isShown = false;
        btnPost.setVisibility(View.GONE);
        prgBar.setVisibility(View.VISIBLE);

        conLoadDetail = new MyOkHttp(ProductEditActivity.this, new MyOkHttp.TaskListener() {
            @Override
            public void onFinished(final String result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result == null) {
                            Toast.makeText(context, "連線失敗", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        final ImageView imageView = (ImageView) findViewById(R.id.imgNotFound);
                        final TextView textView = (TextView) findViewById(R.id.txtNotFound);
                        try {
                            JSONObject resObj = new JSONObject(result);
                            if (resObj.getBoolean(KEY_STATUS)) {
                                JSONObject obj = resObj.getJSONObject(KEY_PRODUCT);
                                book = new Book(
                                        obj.getString(KEY_PRODUCT_ID),
                                        obj.getString(KEY_PHOTO1),
                                        obj.getString(KEY_PHOTO2),
                                        obj.getString(KEY_PHOTO3),
                                        obj.getString(KEY_PHOTO4),
                                        obj.getString(KEY_PHOTO5),
                                        obj.getString(KEY_TITLE),
                                        obj.getString(KEY_CONDITION),
                                        obj.getString(KEY_NOTE),
                                        obj.getString(KEY_PRICE),
                                        obj.getString(KEY_PS),
                                        obj.getString(KEY_TYPE)
                                );

                                // 產生物件ArrayList資料後，由圖片位址下載圖片，完成後再顯示資料.
                                getBitmap = new GetBitmapBatch(book, getString(R.string.link_image), new GetBitmapBatch.TaskListener() {
                                    // 下載圖片完成後執行的方法
                                    @Override
                                    public void onFinished() {
                                        showFoundStatus(book, imageView, textView, "");
                                        showData();
                                        prgBar.setVisibility(View.GONE);
                                    }
                                });
                                // 執行圖片下載
                                getBitmap.execute();
                            }else {
                                prgBar.setVisibility(View.GONE);
                                showFoundStatus(book, imageView, textView, "此商品不存在");
                            }
                        }catch (JSONException e) {
                            prgBar.setVisibility(View.GONE);
                            showFoundStatus(book, imageView, textView, "伺服器發生例外");
                        }
                    }
                });
            }
        });
        try {
            JSONObject reqObj = new JSONObject();
            reqObj.put(KEY_USER_ID, loginUserId);
            reqObj.put(KEY_PRODUCT_ID, bookId);
            reqObj.put(KEY_ANYWAY, "0");
            conLoadDetail.execute(getString(R.string.link_product_detail), reqObj.toString());
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showData() {
        txtId.setText(bookId);
        edtTitle.setText(book.getTitle());
        edtStatus.setText(book.getStatus());
        edtPrice.setText(book.getPrice());
        edtPS.setText(book.getPs());

        String bookType = book.getSeller();
        if (bookType.contains("00")) chkGN.setChecked(true);
        if (bookType.contains("01")) chkAI.setChecked(true);
        if (bookType.contains("02")) chkFN.setChecked(true);
        if (bookType.contains("03")) chkFT.setChecked(true);
        if (bookType.contains("04")) chkIB.setChecked(true);
        if (bookType.contains("05")) chkBM.setChecked(true);
        if (bookType.contains("06")) chkIM.setChecked(true);
        if (bookType.contains("07")) chkAF.setChecked(true);
        if (bookType.contains("A")) chkCD.setChecked(true);
        if (bookType.contains("B")) chkCC.setChecked(true);
        if (bookType.contains("C")) chkDM.setChecked(true);

        String bookNote = book.getNote();
        switch (bookNote) {
            case "未附筆記":
                spnNote.setSelection(1);
                book.setNote("1");
                break;
            case "寫在書中":
                spnNote.setSelection(2);
                book.setNote("2");
                break;
            case "另附筆記本":
                spnNote.setSelection(3);
                book.setNote("3");
                break;
            case "寫在書中及筆記本":
                spnNote.setSelection(4);
                book.setNote("4");
                break;
        }

        showImages(book);
        book = null;
        layDetail.setVisibility(View.VISIBLE);
        btnPost.setVisibility(View.VISIBLE);
        isShown = true;
    }

    private void showImages(Book book) {
        provider = new AlbumImageProvider(this, 3, 4, 900, 1200, new AlbumImageProvider.TaskListener() {
            @Override
            public void onFinished(Bitmap bitmap) {
                int position = adapter.getPressedPosition();
                adapter.setItem(position, new ImageChild(bitmap, true));
                recyclerView.scrollToPosition(position);
            }
        });
        adapter = new ImageUploadAdapter(getResources(), context, provider, getString(R.string.link_upload_image));

        //設置CardView上的圖片；網路下載回來的圖會有檔名(FileName)
        ImageChild image;
        if (!book.getImgURL().equals("")) {
            image = new ImageChild(book.getImg(), false); //isEntity = false代表此圖片來自網路，並非從手機追加的實體圖片
            image.setFileName(book.getImgURL()); //將伺服器圖檔名稱設定給物件
            adapter.addItem(image); //加入佇列
        }
        if (!book.getImgURL2().equals("")) {
            image = new ImageChild(book.getImg2(), false);
            image.setFileName(book.getImgURL2());
            adapter.addItem(image);
        }
        if (!book.getImgURL3().equals("")) {
            image = new ImageChild(book.getImg3(), false);
            image.setFileName(book.getImgURL3());
            adapter.addItem(image);
        }
        if (!book.getImgURL4().equals("")) {
            image = new ImageChild(book.getImg4(), false);
            image.setFileName(book.getImgURL4());
            adapter.addItem(image);
        }
        if (!book.getImgURL5().equals("")) {
            image = new ImageChild(book.getImg5(), false);
            image.setFileName(book.getImgURL5());
            adapter.addItem(image);
        }
        if (adapter.getItemCount() < 5) //圖片不足5張時，添加一張空白圖
            adapter.addItem(new ImageChild(null, false));

        // 產生 RecyclerView
        recyclerView = (RecyclerView) findViewById(R.id.recy_books);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }

    private boolean isInfoValid() {
        Verifier v = new Verifier(context);
        StringBuffer sbErrMsg = new StringBuffer();

        book.setTitle(edtTitle.getText().toString());
        book.setPrice(edtPrice.getText().toString());
        book.setCondition(edtStatus.getText().toString());
        book.setPs(edtPS.getText().toString());
        sbDep = new StringBuffer();

        //書名、價格
        sbErrMsg.append(v.chkTitle(book.getTitle()));
        sbErrMsg.append(v.chkPrice(book.getPrice()));

        //科系
        if (chkGN.isChecked()) sbDep.append("00#");
        if (chkAI.isChecked()) sbDep.append("01#");
        if (chkFN.isChecked()) sbDep.append("02#");
        if (chkFT.isChecked()) sbDep.append("03#");
        if (chkIB.isChecked()) sbDep.append("04#");
        if (chkBM.isChecked()) sbDep.append("05#");
        if (chkIM.isChecked()) sbDep.append("06#");
        if (chkAF.isChecked()) sbDep.append("07#");
        if (chkCD.isChecked()) sbDep.append("A#");
        if (chkCC.isChecked()) sbDep.append("B#");
        if (chkDM.isChecked()) sbDep.append("C#");
        if (sbDep.length() == 0)
            sbErrMsg.append("分類\n");

        //書況、筆記、備註
        sbErrMsg.append(v.chkCondition(book.getStatus()));
        if (note.equals("0")) sbErrMsg.append(getString(R.string.chkNote));
        sbErrMsg.append(v.chkPs(book.getPs()));

        if (sbErrMsg.length() != 0) {
            v.getDialog("刊登商品", sbErrMsg.substring(0, sbErrMsg.length() - 1)).show();
            return false;
        }else {
            book.setPrice(String.valueOf(Integer.parseInt(book.getPrice()))); //避免有人開頭輸入一堆0
            book.setNote(note);
            return true;
        }
    }

    private void postProduct(String[] fileNames) {
        prgBar.setVisibility(View.VISIBLE);
        layDetail.setVisibility(View.GONE);
        conEditProduct = new MyOkHttp(ProductEditActivity.this, new MyOkHttp.TaskListener() {
            @Override
            public void onFinished(final String result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result == null) {
                            Toast.makeText(context, "連線失敗", Toast.LENGTH_SHORT).show();
                            dlgUpload.dismiss();
                            return;
                        }
                        try {
                            JSONObject resObj = new JSONObject(result);
                            if (resObj.getBoolean(KEY_STATUS)) {
                                Toast.makeText(context, "編輯成功", Toast.LENGTH_SHORT).show();
                                finish();
                            }else {
                                Toast.makeText(context, "伺服器發生例外", Toast.LENGTH_SHORT).show();
                                dlgUpload.dismiss();
                            }
                        }catch (JSONException e) {
                            Toast.makeText(context, "處理JSON發生錯誤", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        try {
            JSONObject reqObj = new JSONObject();
            reqObj.put(KEY_PRODUCT_ID, bookId);
            reqObj.put(KEY_TITLE, book.getTitle());
            reqObj.put(KEY_PRICE, book.getPrice());
            reqObj.put(KEY_TYPE, sbDep.toString());
            reqObj.put(KEY_CONDITION, book.getStatus());
            reqObj.put(KEY_NOTE, book.getNote());
            reqObj.put(KEY_PS, book.getPs());
            reqObj.put(KEY_PHOTO1, fileNames[0]);
            reqObj.put(KEY_PHOTO2, fileNames[1]);
            reqObj.put(KEY_PHOTO3, fileNames[2]);
            reqObj.put(KEY_PHOTO4, fileNames[3]);
            reqObj.put(KEY_PHOTO5, fileNames[4]);
            conEditProduct.execute(getString(R.string.link_edit_product), reqObj.toString());
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSubmit:
                book = new Book();
                if (!isInfoValid())
                    return;

                //準備檔名，舊的圖會有自己的FileName，新的圖沒有，但上傳完後會被更新
                String[] fileNames = new String[5];
                for (int i = 0; i < 5; i++)
                    fileNames[i] = "";
                for (int i = 0; i < adapter.getItemCount(); i++) {
                    if (!adapter.getItem(i).isEntity())
                        fileNames[i] = adapter.getItem(i).getFileName();
                }

                if (adapter.getEntityAmount() == 0) { //queue中的圖片數量此時也計算好
                    if (adapter.getItemCount() == 1) //沒有任何圖片，只剩一張空白圖
                        Toast.makeText(context, "未選擇圖片", Toast.LENGTH_SHORT).show();
                    else //沒有新圖片，上傳文字資料即可
                        postProduct(fileNames);
                    return;
                }

                //有追加新圖片，開始上傳
                adapter.startUpload(fileNames, dlgUpload, txtUploadHint, new ImageUploadQueue.TaskListener() {
                    @Override
                    public void onFinished(String[] fileNames) {
                        postProduct(fileNames);
                    }
                });
                break;
        }
    }

    @Override
    public void onPause() {
        cancelConnection();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        adapter = null;
        System.gc();
        super.onDestroy();
    }

    private void cancelConnection() {
        if (conLoadDetail != null)
            conLoadDetail.cancel();
        if (getBitmap != null)
            getBitmap.cancel(true);
        if (conEditProduct != null)
            conEditProduct.cancel();
        if (adapter != null) {
            adapter.cancelUpload();
            adapter.destroyQueue(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        provider.onActivityResult(requestCode, resultCode, data);
    }
}
