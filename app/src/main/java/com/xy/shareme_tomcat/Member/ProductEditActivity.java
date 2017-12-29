package com.xy.shareme_tomcat.Member;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
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
import com.xy.shareme_tomcat.adapter.ImageQueueAdapter;
import com.xy.shareme_tomcat.data.Book;
import com.xy.shareme_tomcat.data.ImageObj;
import com.xy.shareme_tomcat.data.ImageObject;
import com.xy.shareme_tomcat.network_helper.GetBitmapBatch;
import com.xy.shareme_tomcat.network_helper.ImageUploadTask;
import com.xy.shareme_tomcat.network_helper.MyOkHttp;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

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
import static com.xy.shareme_tomcat.data.DataHelper.getNotFoundImg;
import static com.xy.shareme_tomcat.data.DataHelper.loginUserId;

public class ProductEditActivity extends AppCompatActivity implements View.OnClickListener {
    private Context context;

    private LinearLayout layDetail;
    private ProgressBar prgBar;
    private EditText edtTitle, edtStatus, edtPrice, edtPS;
    private CheckBox chkAI, chkFN, chkFT, chkIB, chkBM, chkIM, chkAF, chkCD, chkCC, chkDM, chkGN;
    private Spinner spnNote;
    private ImageView btnPost;
    private TextView txtId;

    private ArrayList<ImageObj> books;
    private ImageQueueAdapter adapter;
    private RecyclerView recyclerView;
    private int itemIndex, itemAmount;

    private String bookId, title, condition, price, ps, note;
    private StringBuffer sbDep;

    private MyOkHttp conLoadDetail, conEditProduct;
    private GetBitmapBatch getBitmap;
    private ImageUploadTask imageTask = null;

    private Dialog dialog = null;
    private TextView txtUploadHint;
    private int count;
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

        btnPost = (ImageView) toolbar.findViewById(R.id.btnPost);
        btnPost.setOnClickListener(this);

        prepareDialog();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isShown)
            loadData();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        //詢問是否開啟權限
        switch(requestCode) {
            case 0:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //取得權限，進行檔案存取
                    adapter.pickImageDialog();
                }else {
                    //使用者拒絕權限，停用檔案存取功能，並顯示訊息
                    Toast.makeText(context, "未給予權限，無法選擇圖片", Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK)
            return;

        Uri selectedImageUri = data.getData();
        switch (requestCode) {
            case ImageQueueAdapter.REQUEST_ALBUM:
                if (!adapter.createImageFile())
                    return;
                if (selectedImageUri != null)
                    adapter.cropImage(selectedImageUri);

                break;
            case ImageQueueAdapter.REQUEST_CROP:
                final int position = adapter.getPressedPosition();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ImageObject image = adapter.getItem(position);
                        boolean isEmptyCard = image.getBitmap() == null;
                        boolean bitmapIsNull = true;
                        do {
                            bitmapIsNull = adapter.mImageFileToBitmap(image);
                        } while (bitmapIsNull);

                        image.setEntity(true);
                        adapter.setItem(position, image);

                        if (isEmptyCard && adapter.getItemCount() < 5) {
                            Bitmap bitmap = null;
                            adapter.addItem(new ImageObject(bitmap, false)); //再新增一張空白圖
                        }
                    }
                }).start();
                recyclerView.scrollToPosition(position);
                break;
        }
    }

    private void prepareDialog() {
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dlg_uploading);
        dialog.setCancelable(false);
        txtUploadHint = (TextView) dialog.findViewById(R.id.txtHint);
        LinearLayout layUpload = (LinearLayout) dialog.findViewById(R.id.layUpload);
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
                                    imageTask = null;
                                    conEditProduct.cancel();
                                    dialog.dismiss();
                                    Toast.makeText(context, "上傳已取消", Toast.LENGTH_SHORT).show();
                                }catch (NullPointerException e) {}
                            }
                        }).show();
                return true;
            }
        });
    }

    private void loadData() {
        btnPost.setVisibility(View.GONE);

        books = new ArrayList<>();
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
                        try {
                            JSONObject resObj = new JSONObject(result);
                            if (resObj.getBoolean(KEY_STATUS)) {
                                JSONObject obj = resObj.getJSONObject(KEY_PRODUCT);

                                books.add(new Book(
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
                                ));
                                // 產生物件ArrayList資料後，由圖片位址下載圖片，完成後再顯示資料.
                                getBitmap = new GetBitmapBatch(context, getResources(), books, new GetBitmapBatch.TaskListener() {
                                    // 下載圖片完成後執行的方法
                                    @Override
                                    public void onFinished() {
                                        showData();
                                    }
                                });
                                // 執行圖片下載
                                getBitmap.execute();
                            }else {
                                Toast.makeText(context, "商品不存在", Toast.LENGTH_SHORT).show();
                                prgBar.setVisibility(View.GONE);
                                showFoundStatus();
                            }
                        }catch (JSONException e) {
                            //Toast.makeText(context, "伺服器發生例外", Toast.LENGTH_SHORT).show();
                            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                            prgBar.setVisibility(View.GONE);
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
        Book book = (Book) books.get(0);
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
                note = "1";
                break;
            case "寫在書中":
                spnNote.setSelection(2);
                note = "2";
                break;
            case "另附筆記本":
                spnNote.setSelection(3);
                note = "3";
                break;
            case "寫在書中及筆記本":
                spnNote.setSelection(4);
                note = "4";
                break;
        }

        showImages(book);

        books = null;
        prgBar.setVisibility(View.GONE);
        layDetail.setVisibility(View.VISIBLE);
        btnPost.setVisibility(View.VISIBLE);
        isShown = true;
    }

    private void showImages(Book book) {
        ArrayList<ImageObject> images = new ArrayList<>();
        if (!book.getImgURL().equals("")) {
            Bitmap bitmap = book.getImg();
            ImageObject image = new ImageObject(bitmap, false); //此圖片來自網路，並非從手機追加的實體圖片
            image.setFileName(book.getImgURL());
            images.add(image);
        }
        if (!book.getImgURL2().equals("")) {
            Bitmap bitmap = book.getImg();
            ImageObject image = new ImageObject(bitmap, false);
            image.setFileName(book.getImgURL2());
            images.add(image);
        }
        if (!book.getImgURL3().equals("")) {
            Bitmap bitmap = book.getImg();
            ImageObject image = new ImageObject(bitmap, false);
            image.setFileName(book.getImgURL3());
            images.add(image);
        }
        if (!book.getImgURL4().equals("")) {
            Bitmap bitmap = book.getImg();
            ImageObject image = new ImageObject(bitmap, false);
            image.setFileName(book.getImgURL4());
            images.add(image);
        }
        if (!book.getImgURL5().equals("")) {
            Bitmap bitmap = book.getImg();
            ImageObject image = new ImageObject(bitmap, false);
            image.setFileName(book.getImgURL5());
            images.add(image);
        }
        if (images.size() < 5) { //圖片不足5張時，添加一張空白圖
            Bitmap bitmap = null;
            images.add(new ImageObject(bitmap, false));
        }

        // 產生 RecyclerView
        recyclerView = (RecyclerView) findViewById(R.id.recy_books);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        adapter = new ImageQueueAdapter(context, images, this);
        recyclerView.setAdapter(adapter);
        images = null;
    }

    private void showFoundStatus() {
        TextView txtNotFound = (TextView) findViewById(R.id.txtNotFound);
        ImageView imgNotFound = (ImageView) findViewById(R.id.imgNotFound);
        if (books == null || books.isEmpty()) {
            txtNotFound.setText("此商品不存在");
            txtNotFound.setVisibility(View.VISIBLE);
            imgNotFound.setImageResource(getNotFoundImg());
            imgNotFound.setVisibility(View.VISIBLE);
        }else {
            txtNotFound.setText("");
            txtNotFound.setVisibility(View.GONE);
            imgNotFound.setVisibility(View.GONE);
        }
    }

    private boolean isInfoValid() {
        StringBuffer sbErrMsg = new StringBuffer();
        title = edtTitle.getText().toString();
        price = edtPrice.getText().toString();
        condition = edtStatus.getText().toString();
        ps = edtPS.getText().toString();
        sbDep = new StringBuffer();

        //書名、價格
        if (title.equals("")) sbErrMsg.append("書名\n");
        if (price.equals(""))
            sbErrMsg.append("價格\n"); //EditText已限制只能輸入>=0的整數，就算複製其他文字過來也能過濾掉
        else
            price = String.valueOf(Integer.parseInt(price)); //避免有人開頭輸入一堆0

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

        //書況、筆記
        if (condition.equals("")) sbErrMsg.append("書況\n");
        if (note.equals("0") || note.equals("")) sbErrMsg.append("筆記提供方式\n");


        if (sbErrMsg.length() != 0) {
            sbErrMsg.insert(0, "以下資料未填寫：\n");
            AlertDialog.Builder msgbox = new AlertDialog.Builder(context);
            msgbox.setTitle("刊登商品")
                    .setPositiveButton("確定", null)
                    .setMessage(sbErrMsg.substring(0, sbErrMsg.length() - 1))
                    .show();
            return false;
        }else
            return true;
    }

    private void initTrdWaitPhoto(boolean restart) {
        Thread trdWaitPhoto = new Thread(new Runnable() {
            @Override
            public void run() {
                hdrWaitPhoto.sendMessage(hdrWaitPhoto.obtainMessage());
            }
        });
        if (restart)
            trdWaitPhoto.start();
    }

    private Handler hdrWaitPhoto = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (imageTask == null)
                return;

            String fileName = imageTask.getPhotoName();
            if (fileName == null) {
                initTrdWaitPhoto(true); //還沒收到檔名，繼續監聽
            }else {
                initTrdWaitPhoto(false);
                //寫入檔名
                ImageObject image = adapter.getItem(itemIndex);
                image.setFileName(fileName);
                adapter.setItem(itemIndex, image);

                //上傳下一張
                itemIndex++;
                if (count >= itemAmount) { //圖片都上傳了
                    dialog.dismiss();
                    postProduct();
                    return;
                }
                for (int i = itemIndex; i< adapter.getItemCount(); i++) { //新增的圖片可能穿插，需要逐一確認
                    final ImageObject newImage = adapter.getItem(i);
                    if (newImage.getBitmap() != null) {
                        new Thread(new Runnable() {
                            public void run() {
                                imageTask = new ImageUploadTask(context, getString(R.string.link_upload_image));
                                imageTask.uploadFile(newImage.getBitmap());
                            }
                        }).start();
                        initTrdWaitPhoto(true);
                        count++;
                        txtUploadHint.setText(getString(R.string.hint_upload_photo, String.valueOf(count), String.valueOf(itemAmount)));
                        break;
                    }
                }
            }
        }
    };

    private void postProduct() {
        conEditProduct = new MyOkHttp(ProductEditActivity.this, new MyOkHttp.TaskListener() {
            @Override
            public void onFinished(final String result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (result == null) {
                            Toast.makeText(context, "連線失敗", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            return;
                        }
                        try {
                            JSONObject resObj = new JSONObject(result);
                            if (resObj.getBoolean(KEY_STATUS)) {
                                Toast.makeText(context, "編輯成功", Toast.LENGTH_SHORT).show();
                                finish();
                            }else
                                Toast.makeText(context, "伺服器發生例外", Toast.LENGTH_SHORT).show();
                        }catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        try {
            //修正圖檔名稱
            String[] fileName = new String[5];
            for (int i=0; i<adapter.getItemCount(); i++)
                fileName[i] = adapter.getItem(i).getFileName();
            for (int i=0; i<5; i++) {
                if (fileName[i] == null)
                    fileName[i] = "";
            }

            JSONObject reqObj = new JSONObject();
            reqObj.put(KEY_PRODUCT_ID, bookId);
            reqObj.put(KEY_TITLE, title);
            reqObj.put(KEY_PRICE, price);
            reqObj.put(KEY_TYPE, sbDep.toString());
            reqObj.put(KEY_CONDITION, condition);
            reqObj.put(KEY_NOTE, note);
            reqObj.put(KEY_PS, ps);
            reqObj.put(KEY_PHOTO1, fileName[0]);
            reqObj.put(KEY_PHOTO2, fileName[1]);
            reqObj.put(KEY_PHOTO3, fileName[2]);
            reqObj.put(KEY_PHOTO4, fileName[3]);
            reqObj.put(KEY_PHOTO5, fileName[4]);
            Toast.makeText(context, reqObj.toString(), Toast.LENGTH_LONG).show();
            //conEditProduct.execute(getString(R.string.link_edit_product), reqObj.toString());
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnPost:
                if (!isInfoValid())
                    return;

                itemIndex = 0; //卡片陣列索引
                itemAmount = 0; //新圖片數量
                count = 0; //新圖片上傳計數
                ImageObject image;

                image = adapter.getItem(0);
                if (image.getBitmap() == null) { //檢查是否至少有一張圖片
                    Toast.makeText(context, "未選擇圖片", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (int i = 0; i< adapter.getItemCount(); i++) { //計算新圖片數量
                    image = adapter.getItem(i);
                    if (image.isEntity())
                        itemAmount++;
                }
                if (itemAmount == 0) { //若無新圖片，直接上傳文字即可
                    postProduct();
                    return;
                }

                for (int i = 0; i< adapter.getItemCount(); i++) { //尋找第一張新圖片在陣列的索引
                    final ImageObject newImage = adapter.getItem(i);
                    if (newImage.isEntity()) {
                        itemIndex = i;
                        //開始上傳
                        new Thread(new Runnable() {
                            public void run() {
                                imageTask = new ImageUploadTask(context, getString(R.string.link_upload_image));
                                imageTask.uploadFile(newImage.getBitmap());
                            }
                        }).start();
                        //initTrdWaitPhoto(true);
                        count++;
                        txtUploadHint.setText(getString(R.string.hint_upload_photo, String.valueOf(count), String.valueOf(itemAmount)));
                        dialog.show();
                        break;
                    }
                }
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
        System.gc();
        super.onDestroy();
    }

    private void cancelConnection() {
        try {
            conLoadDetail.cancel();
        }catch (NullPointerException e) {}
        try {
            getBitmap.cancel(true);
        }catch (NullPointerException e) {}
        try {
            conEditProduct.cancel();
        }catch (NullPointerException e) {}
    }
}
