package com.androidblebeaconwrapperlib.network;

public interface RequestCallBackListener {

    void beforeCallBack();

    void onResponse(String responseString);

    void onError(String errorMsg);
}
