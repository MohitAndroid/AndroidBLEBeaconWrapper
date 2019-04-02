package com.androidblebeaconwrapperlib.beaconwrapper;


import com.androidblebeaconwrapperlib.beacon.BeaconResultEntity;

import java.util.List;

public interface BleBeaconListener<T> {

    void onBeaconDataResult(List<BeaconResultEntity> beaconResultEntities);

    void onError(String errorMsg);

    void onShowProgress();

    void onParsableDataResult(List<T> parsableData);


}
