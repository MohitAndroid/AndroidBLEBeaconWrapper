package com.androidblebeaconwrapperlib.beaconwrapper;

import android.app.Activity;


import com.androidblebeaconwrapperlib.beacon.BeaconHelper;
import com.androidblebeaconwrapperlib.beacon.BeaconListener;
import com.androidblebeaconwrapperlib.beacon.BeaconResultEntity;
import com.androidblebeaconwrapperlib.beacon.BeaconResultListener;
import com.androidblebeaconwrapperlib.beacon.IBeacon;
import com.androidblebeaconwrapperlib.network.NetworkManager;
import com.androidblebeaconwrapperlib.network.RequestCallBackListener;
import com.androidblebeaconwrapperlib.parse.FilterListener;
import com.androidblebeaconwrapperlib.parse.ParserListClass;

import java.util.List;
import java.util.Map;

public class BLEBeaconWrapper<T> {

    private BeaconHelper<T> beaconHelper;
    private NetworkManager networkManager;
    private ParserListClass<T> parserListClass;
    private Activity context;
    private String url;
    private Map<String, String> headerData;
    private long timeInterval;
    private Class<T> t;
    private BleBeaconListener<T> tBleBeaconListener;
    private BeaconListener tBeaconListener;


    public BLEBeaconWrapper(Activity context) {
        this.context = context;
        networkManager = new NetworkManager();
        parserListClass = new ParserListClass();
        beaconHelper = new BeaconHelper(this.context);
    }

    public void getBeaconData(String url, Class<T> t, Map<String, String> headerData,
                              long timeInterval, BleBeaconListener<T> tBleBeaconListener) {

        this.url = url;
        this.t = t;
        this.headerData = headerData;
        this.timeInterval = timeInterval;
        this.tBleBeaconListener = tBleBeaconListener;
        networkOperation();

    }

    public void getBeaconData(List<T> list, long timeInterval,
                              BleBeaconListener<T> tBleBeaconListener) {
        this.timeInterval = timeInterval;
        this.tBleBeaconListener = tBleBeaconListener;
        beaconWrapperOperation(list);
    }

    public void getBeaconData(long timeInterval,
                              BeaconListener tBeaconListener) {
        this.timeInterval = timeInterval;
        this.tBeaconListener = tBeaconListener;
        BeaconOnlyWrapper();
    }

    private void networkOperation() {
        networkManager.getRequest(url,
                headerData, new RequestCallBackListener() {
                    @Override
                    public void beforeCallBack() {
                        tBleBeaconListener.onShowProgress();
                    }

                    @Override
                    public void onResponse(String responseString) {
                        parseClassFields(responseString);
                    }

                    @Override
                    public void onError(String errorMsg) {
                        tBleBeaconListener.onError(errorMsg);
                    }
                });
    }

    private void parseClassFields(String responseString) {

        parserListClass.parseData(t, responseString, new FilterListener<T>() {
            @Override
            public void onResponse(List<T> filteredData) {
                tBleBeaconListener.onParsableDataResult(filteredData);
                beaconWrapperOperation(filteredData);
            }

            @Override
            public void onError(String errorMsg) {
                tBleBeaconListener.onError(errorMsg);
            }
        });
    }

    public void stopBeaconUpdates() {
        beaconHelper.stopBeaconUpdates();
    }

    private void beaconWrapperOperation(List<T> filteredData) {
        beaconHelper.startBeaconUpdates(filteredData, timeInterval, new BeaconResultListener() {
            @Override
            public void onResult(List<BeaconResultEntity> beaconResultEntities) {
                tBleBeaconListener.onBeaconDataResult(beaconResultEntities);
            }

            @Override
            public void onError(String errorMsg) {
                tBleBeaconListener.onError(errorMsg);
            }
        });
    }

    private void BeaconOnlyWrapper() {
        beaconHelper.startBeaconUpdates( timeInterval, new BeaconListener() {
            @Override
            public void onResult(List<IBeacon> beaconResultEntities) {
                tBeaconListener.onResult(beaconResultEntities);
            }

            @Override
            public void onError(String errorMsg) {
                tBeaconListener.onError(errorMsg);
            }
        });
    }
}
