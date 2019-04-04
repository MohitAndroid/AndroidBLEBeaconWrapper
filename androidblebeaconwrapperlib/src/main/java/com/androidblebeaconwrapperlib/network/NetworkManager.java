package com.androidblebeaconwrapperlib.network;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class NetworkManager {


    private NetworkManager() {
    }

    public static NetworkManager getInstance(){
        return new NetworkManager();
    }

    public void getRequest(String baseUrl, Map<String, String> headerData,
                           RequestCallBackListener requestCallBackListener) {
        try {
            OkHttpClient client = new OkHttpClient();
            OkHttpClient eagerClient = client.newBuilder()
                    .readTimeout(30, TimeUnit.SECONDS)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();
            Request.Builder builder = new Request.Builder();
            builder.url(baseUrl);
            if (headerData != null && !headerData.isEmpty()) {
                for (Map.Entry<String, String> entry : headerData.entrySet()) {
                    builder.addHeader(entry.getKey(), entry.getValue());
                }
            }
            Request request = builder.build();
            requestCallBackListener.beforeCallBack();
            eagerClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    //call.cancel();
                    requestCallBackListener.onError(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    requestCallBackListener.onResponse(response.body().string());
                }
            });
        } catch (Exception e) {
            requestCallBackListener.onError(e.getMessage());
        }

    }

    public void postRequest(String baseUrl, Map<String, String> headerData,
                            Map<String, String> bodyData,
                            RequestCallBackListener requestCallBackListener) {

        try {
            MediaType MEDIA_TYPE = MediaType.parse("application/json");
            OkHttpClient client = new OkHttpClient();
            OkHttpClient eagerClient = client.newBuilder()
                    .readTimeout(30, TimeUnit.SECONDS)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            Request.Builder builder = new Request.Builder();
            builder.url(baseUrl);
            JSONObject postdata = new JSONObject();
            for (Map.Entry<String, String> entry : bodyData.entrySet()) {
                try {
                    postdata.put(entry.getKey(), entry.getValue());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            RequestBody body = RequestBody.create(MEDIA_TYPE, postdata.toString());
            builder.post(body);


            if (headerData != null && !headerData.isEmpty()) {
                for (Map.Entry<String, String> entry : headerData.entrySet()) {
                    builder.addHeader(entry.getKey(), entry.getValue());
                }
            }
            Request request = builder.build();
            requestCallBackListener.beforeCallBack();
            eagerClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    //call.cancel();
                    requestCallBackListener.onError(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    requestCallBackListener.onResponse(response.body().string());
                }
            });
        } catch (Exception e) {
            requestCallBackListener.onError(e.getMessage());
        }

    }
}
