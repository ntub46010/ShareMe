package com.xy.shareme_tomcat.network_helper;

import android.app.Activity;
import android.os.NetworkOnMainThreadException;
import android.widget.Toast;

import com.xy.shareme_tomcat.data.DataHelper;

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
    private OkHttpClient client;

    public MyOkHttp(Activity activity, TaskListener taskListener) {
        this.activity = activity;
        this.taskListener = taskListener;
    }

    public void execute(String... sendingData) {
        Call call = getCall(sendingData);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, final Response response) {
                //連線成功
                try {
                    DataHelper.conFlag = true;
                    taskListener.onFinished(response.body().string());
                    DataHelper.conFlag = false; //NetworkOnMainThreadException
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NetworkOnMainThreadException e) {
                    Toast.makeText(activity, "NetworkOnMainThreadException", Toast.LENGTH_LONG).show();
                }
                /*
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            DataHelper.conFlag = true;
                            taskListener.onFinished(response.body().string());
                            DataHelper.conFlag = false; //NetworkOnMainThreadException
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (NetworkOnMainThreadException e) {
                            Toast.makeText(activity, "NetworkOnMainThreadException", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                */
            }
            @Override
            public void onFailure(Call call, IOException e) {
                //連線失敗
                e.printStackTrace();
            }
        });
    }

    public Call getCall(String... sendingData) {
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
        client = new OkHttpClient();
        return client.newCall(request);
    }

    public void cancel() {
        client.dispatcher().cancelAll();
    }

}
