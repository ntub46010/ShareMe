package com.xy.shareme_tomcat.network_helper;

import android.app.Activity;
import android.content.Context;

import com.xy.shareme_tomcat.DataHelper;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyOkHttp {
    // 宣告一個接收回傳結果的程式必須實作的介面
    public interface TaskListener { void onFinished(String result); }

    private Activity activity;
    private TaskListener taskListener;

    public MyOkHttp(Activity activity, TaskListener taskListener) {
        this.activity = activity;
        this.taskListener = taskListener;
    }

    public void execute(String... sendingData) {
        Request request;
        if (sendingData.length == 1) { //POST一個帶參數的網址
            RequestBody formBody = new FormBody.Builder().build();
            request = new Request.Builder()
                    .url(sendingData[0])
                    .post(formBody)
                    .build();
        }else { //POST一組JSON字串到某網址
            RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), sendingData[1]);
            request = new Request.Builder()
                    .url(sendingData[0])
                    .post(requestBody)
                    .build();
        }

        OkHttpClient client = new OkHttpClient();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, final Response response) {
                //連線成功
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            DataHelper.conFlag = true;
                            taskListener.onFinished(response.body().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            @Override
            public void onFailure(Call call, IOException e) {
                //連線失敗
                e.printStackTrace();
            }
        });
    }

}
