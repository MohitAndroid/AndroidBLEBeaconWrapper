package com.androidblebeaconwrapperlib.beacon;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class BeaconHelper<T> {
    private BluetoothAdapter mBluetoothAdapter;
    private Activity context;
    private BluetoothManager bluetoothManager;
    private List<BeaconResultEntity> beaconResultEntities;
    private BeaconKeySerializer beaconKeySerializer;
    private List<T> data;
    private List<T> filteredData;
    private Map<String, List<T>> dataMap = new HashMap<>();
    private List<DifferentBeaconEntity> oldKeys, newKeys;
    private BeaconResultListener beaconResultListener;
    private BeaconListener beaconListener;
    private Handler mHandler;
    private Timer timer;
    private boolean isOnlyBeaconStuff;
    private List<IBeacon> iBeacons;

    private BeaconHelper(Activity context) {
        this.context = context;
    }

    public static BeaconHelper getInstance(Activity context) {
        return new BeaconHelper(context);
    }

    public void startBeaconUpdates(List<T> data, long timeInterval, BeaconResultListener beaconResultListener) {
        this.isOnlyBeaconStuff = false;
        this.beaconResultListener = beaconResultListener;
        this.data = data;

        beaconKeySerializer =  BeaconKeySerializer.getInstance();
        beaconResultEntities = new ArrayList<>();
        bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        filteredData = new ArrayList<>();
        oldKeys = new ArrayList<>();
        newKeys = new ArrayList<>();
        mHandler = new Handler();
        timer = new Timer();

        try {
            String validationErrorMsg = checkValidation();
            if (!TextUtils.isEmpty(validationErrorMsg)) {
                displayError(validationErrorMsg);
                return;
            }
            if (!isStringTypeData()) {
                setMapFromList();
            }
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (beaconResultEntities != null
                                    && !beaconResultEntities.isEmpty() &&
                                    isDifferent(oldKeys, newKeys)) {
                                setOldKeys();
                                BeaconHelper.this.beaconResultListener.onResult(beaconResultEntities);
                            }
                        }
                    });

                }
            }, 0, timeInterval);
        } catch (BeaconKeySerializeException e) {
            displayError(e.getMessage());
        }
    }

    public void startBeaconUpdates(long timeInterval, BeaconListener beaconListener) {
        this.isOnlyBeaconStuff = true;
        this.beaconListener = beaconListener;

        bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mHandler = new Handler();
        iBeacons = new ArrayList<>();
        timer = new Timer();

        try {
            String validationErrorMsg = checkValidation();
            if (!TextUtils.isEmpty(validationErrorMsg)) {
                displayBeaconError(validationErrorMsg);
                return;
            }
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (iBeacons != null
                                    && !iBeacons.isEmpty()) {
                                BeaconHelper.this.beaconListener.onResult(iBeacons);
                            }
                        }
                    });

                }
            }, 0, timeInterval);
        } catch (Exception e) {
            displayBeaconError(e.getMessage());
        }
    }

    private String checkValidation() {
        String errorMsg = "";
        if (mBluetoothAdapter == null) {
            errorMsg = "Bluetooth not supported.";
        } else if (!mBluetoothAdapter.isEnabled()) {
            errorMsg = "Bluetooth not enabled.";
        }
//        else if (PermissionChecker.checkSelfPermission(context,
//                Manifest.permission.ACCESS_FINE_LOCATION) !=
//                PackageManager.PERMISSION_GRANTED) {
//            errorMsg = "Please grant location access so this app can detect beacons in the background.";
//        }
        return errorMsg;
    }

    private void setMapFromList() throws BeaconKeySerializeException {
        try {
            for (int i = 0; i < data.size(); i++) {
                FieldTypeEntity beaconAnnotationDetails = beaconKeySerializer.getBeaconFieldTypeEntity(data.get(i));
                for (int j = 0; j < beaconAnnotationDetails.getFieldValue().size(); j++) {
                    if (dataMap.containsKey(beaconAnnotationDetails.getFieldValue().get(j))) {
                        dataMap.get(beaconAnnotationDetails.getFieldValue().get(j)).add(data.get(i));
                    } else {
                        List<T> entities = new ArrayList<>();
                        entities.add(data.get(i));
                        dataMap.put(beaconAnnotationDetails.getFieldValue().get(j), entities);
                    }
                }
            }
        } catch (Exception e) {
            throw new BeaconKeySerializeException(e.getMessage());
        }

    }

    public void stopBeaconUpdates() {
        mBluetoothAdapter.startLeScan(mLeScanCallback);
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {

                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            IBeacon iBeacon = IBeacon.fromScanData(scanRecord, rssi, device);
                            if (iBeacon != null) {
                                if (isOnlyBeaconStuff) {
                                    getOnlyBeaconData(iBeacon);
                                } else {
                                    getBeaconFilteredData(iBeacon);
                                }
                            }
                        }
                    });

                }
            };

    private void getOnlyBeaconData(IBeacon iBeacon) {
        try {
            boolean isAdd = true;
            for (int i = 0; i < iBeacons.size(); i++) {
                if (iBeacons.get(i).getBluetoothAddress().equals(iBeacon.getBluetoothAddress())) {

                    iBeacons.get(i).setAccuracy(iBeacon.getAccuracy());
                    iBeacons.get(i).setBleDataPayload(iBeacon.getBleDataPayload());
                    iBeacons.get(i).setProximityUuid(iBeacon.getProximityUuid());
                    iBeacons.get(i).setRssi(iBeacon.getRssi());
                    iBeacons.get(i).setTimezoneString(iBeacon.getTimezoneString());
                    isAdd = false;
                    break;
                }
            }
            if (isAdd) {
                iBeacons.add(iBeacon);
            }
        } catch (Exception e) {
            displayBeaconError(e.getMessage());
        }

    }


    private boolean isStringTypeData() {
        boolean isStringType = false;
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i) instanceof String) {
                isStringType = true;
                break;
            }
        }
        return isStringType;
    }

    private void getBeaconFilteredData(IBeacon iBeacon) {
        try {
            if (data == null && data.isEmpty()) {
                throw new BeaconKeySerializeException("List can not be null");
            }
            if (!dataMap.containsKey(iBeacon.getBleDataPayload())) {
                throw new BeaconKeySerializeException("Key(Payload) not found in data");
            }
            BeaconResultEntity entity = isPresentInBeaconPagerEntities(iBeacon.getBluetoothAddress());
            if (entity == null) {
                BeaconResultEntity beaconResultEntity = new BeaconResultEntity();
                beaconResultEntity.setBeaconDetail(iBeacon);
                beaconResultEntity.setKey(iBeacon.getBleDataPayload());
                filterBeaconData(iBeacon);
                beaconResultEntity.setResult(filteredData);
                beaconResultEntities.add(beaconResultEntity);
            } else {
                entity.setBeaconDetail(iBeacon);
                entity.setKey(iBeacon.getBleDataPayload());
                filterBeaconData(iBeacon);
                entity.setResult(filteredData);
            }
            sortBeaconData();
            setNewKeys();
//            if (isDifferent(oldKeys, newKeys)) {
//                setOldKeys();
//                this.beaconResultListener.onBeaconDataResult(beaconResultEntities);
//            }
        } catch (Exception e) {
            displayError(e.getMessage());
        }
    }

    private void displayError(String msg) {
        this.beaconResultListener.onError(msg);
    }

    private void displayBeaconError(String msg) {
        this.beaconListener.onError(msg);
    }

    private void filterBeaconMapData(IBeacon iBeacon) {
        filteredData = new ArrayList<>();
        filteredData.addAll(dataMap.get(iBeacon.getBleDataPayload()));
    }

    private void filterBeaconListData(IBeacon iBeacon) {
        filteredData = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).equals(iBeacon.getBleDataPayload())) {
                filteredData.add(data.get(i));
            }
        }
    }

    private void filterBeaconData(IBeacon iBeacon) {
        if (isStringTypeData()) {
            filterBeaconListData(iBeacon);
        } else {
            filterBeaconMapData(iBeacon);
        }
    }

    private void setNewKeys() {
        newKeys.clear();
        for (int i = 0; i < beaconResultEntities.size(); i++) {
            String bluetoothAddress = beaconResultEntities.get(i).getBeaconDetail().getBluetoothAddress();
            String name = beaconResultEntities.get(i).getBeaconDetail().getBleDataPayload();
            newKeys.add(new DifferentBeaconEntity(bluetoothAddress, name));
        }
    }

    private void setOldKeys() {
        oldKeys.clear();
        for (int i = 0; i < newKeys.size(); i++) {
            oldKeys.add(newKeys.get(i));
        }
    }

    private void sortBeaconData() {
        Collections.sort(beaconResultEntities,
                (o1, o2) -> Double.compare(o1.getBeaconDetail().getAccuracy(), o2.getBeaconDetail().getAccuracy()));
    }

    private boolean isDifferent(List<DifferentBeaconEntity> oldKeys,
                                List<DifferentBeaconEntity> newKeys) {
        boolean isDifferent = false;
        if (oldKeys.size() == newKeys.size()) {
            for (int i = 0; i < oldKeys.size(); i++) {
                if (!(oldKeys.get(i).getBeaconKey().equals(newKeys.get(i).getBeaconKey())) ||
                        !(oldKeys.get(i).getBeaconName().equals(newKeys.get(i).getBeaconName()))) {
                    isDifferent = true;
                    break;
                }
            }
        } else {
            isDifferent = true;
        }
        return isDifferent;
    }

    private BeaconResultEntity isPresentInBeaconPagerEntities(String macAddress) throws BeaconKeySerializeException {
        try {
            for (int i = 0; i < beaconResultEntities.size(); i++) {
                if (beaconResultEntities.get(i).getBeaconDetail().getBluetoothAddress().equals(macAddress)) {
                    return beaconResultEntities.get(i);
                }
            }
            return null;
        } catch (Exception e) {
            throw new BeaconKeySerializeException(e.getMessage());
        }
    }

}
