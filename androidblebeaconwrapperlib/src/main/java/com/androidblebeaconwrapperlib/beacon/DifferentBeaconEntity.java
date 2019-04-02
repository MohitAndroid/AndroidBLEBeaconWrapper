package com.androidblebeaconwrapperlib.beacon;

public class DifferentBeaconEntity {

    private String beaconKey;
    private String beaconName;


    public DifferentBeaconEntity(String beaconKey, String beaconName) {
        this.beaconKey = beaconKey;
        this.beaconName = beaconName;
    }

    public DifferentBeaconEntity() {
    }

    public String getBeaconKey() {
        return beaconKey;
    }

    public void setBeaconKey(String beaconKey) {
        this.beaconKey = beaconKey;
    }

    public String getBeaconName() {
        return beaconName;
    }

    public void setBeaconName(String beaconName) {
        this.beaconName = beaconName;
    }
}
