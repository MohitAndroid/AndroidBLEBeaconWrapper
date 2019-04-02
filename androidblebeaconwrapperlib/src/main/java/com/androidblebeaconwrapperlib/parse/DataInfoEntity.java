package com.androidblebeaconwrapperlib.parse;

public class DataInfoEntity {

    private String data;
    private String type;
    private int deepCount;
    private boolean isRelativeData=false;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getDeepCount() {
        return deepCount;
    }

    public void setDeepCount(int deepCount) {
        this.deepCount = deepCount;
    }

    public boolean isRelativeData() {
        return isRelativeData;
    }

    public void setRelativeData(boolean relativeData) {
        isRelativeData = relativeData;
    }

    public DataInfoEntity(String data, String type, int deepCount) {
        this.data = data;
        this.type = type;
        this.deepCount = deepCount;
    }
}
