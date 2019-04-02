package com.androidblebeaconwrapperlib.beacon;

import java.util.List;

public interface BeaconResultListener {

    void onResult(List<BeaconResultEntity> beaconResultEntities);

    void onError(String errorMsg);
}
