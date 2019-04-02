package com.androidblebeaconwrapperlib.beacon;


import java.util.List;

public class BeaconResultEntity<T> {

    private String key;
    private List<T> result;
    private IBeacon beaconDetail;


    public BeaconResultEntity(String key, List<T> result, IBeacon beaconDetail) {
        this.key = key;
        this.result = result;
        this.beaconDetail = beaconDetail;
    }

    public BeaconResultEntity() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<T> getResult() {
        return result;
    }

    public void setResult(List<T> result) {
        this.result = result;
    }

    public IBeacon getBeaconDetail() {
        return beaconDetail;
    }

    public void setBeaconDetail(IBeacon beaconDetail) {
        this.beaconDetail = beaconDetail;
    }
}
