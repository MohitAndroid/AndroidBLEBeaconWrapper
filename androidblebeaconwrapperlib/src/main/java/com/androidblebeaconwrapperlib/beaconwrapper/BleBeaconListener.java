package com.androidblebeaconwrapperlib.beaconwrapper;


import com.androidblebeaconwrapperlib.beacon.BeaconResultEntity;

import java.util.List;

public interface BleBeaconListener<T> {

    void onResult(List<BeaconResultEntity> beaconResultEntities);

    void onError(String errorMsg);

    void onShowProgress();


}
