package com.androidblebeaconwrapperlib.parse;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ParserListClass<T> {
    private List<DataInfoEntity> dataTypeEntities = new ArrayList<>();
    private Class<T> t;
    private List<String> rootFields;
    private static final String JSON_OBJECT = "jsonObject";
    private static final String JSON_ARRAY = "jsonArray";
    private int count = 0;
    private String jsonString;
    private FilterListener<T> filterListener;

    private ParserListClass() {
    }

    public static ParserListClass getInstance(){
        return new ParserListClass();
    }

    private void setFields() throws ParseFilterException {
        rootFields = new ArrayList<>();
        try {
            for (Field field : t.getDeclaredFields()) {
                field.setAccessible(true);
                rootFields.add(getSerializedKey(field));
            }
            rootFields.remove("serialVersionUID");
            rootFields.remove("$change");
        } catch (Exception e) {
            throw new ParseFilterException(e.getMessage());
        }
    }

    private static String getSerializedKey(Field field) {
        SerializedName annotation = field.getAnnotation(SerializedName.class);

        if (annotation == null) {
            return field.getName();
        } else {
            return annotation.value();
        }
    }

    private void parse() throws ParseFilterException {
        try {
            Object json = new JSONTokener(jsonString).nextValue();
            if (json instanceof JSONObject) {
                jsonToMap(new JSONObject(jsonString));
            } else if (json instanceof JSONArray) {
                jsonToList(new JSONArray(jsonString));
            }
        } catch (Exception e) {
            throw new ParseFilterException(e.getMessage());
        }
    }

    public void parseData(Class<T> t, String jsonString, FilterListener<T> filterListener) {
        try {
            this.t = t;
            this.filterListener = filterListener;
            this.jsonString = jsonString;

            count = 0;
            gson = new Gson();

            if (!TextUtils.isEmpty(this.jsonString) && this.t != null &&
                    this.filterListener != null) {
                setFields();
                parse();
                filterData();
                if (filterData != null && !filterData.isEmpty()) {
                    filterListener.onResponse(filterData);
                } else {
                    displayError("Empty list");
                }
            } else {
                displayError("Data must not be null");
            }

        } catch (Exception e) {
            displayError(e.getMessage());
        }

    }


    private Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
        Map<String, Object> retMap = new HashMap<String, Object>();

        if (json != JSONObject.NULL) {
            retMap = toMap(json);
        }
        return retMap;
    }

    private List<Object> jsonToList(JSONArray json) throws JSONException {
        List<Object> retList = new ArrayList<>();

        if (json != null) {
            retList = toList(json);
        }
        return retList;
    }

    private Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();

        Iterator<String> keysItr = object.keys();
        while (keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if (value instanceof JSONArray) {
                dataTypeEntities.add(new DataInfoEntity(value.toString(), JSON_ARRAY, ++count));
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                dataTypeEntities.add(new DataInfoEntity(value.toString(), JSON_OBJECT, ++count));
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    private List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<Object>();
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) {
                dataTypeEntities.add(new DataInfoEntity(value.toString(), JSON_ARRAY, ++count));
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                dataTypeEntities.add(new DataInfoEntity(value.toString(), JSON_OBJECT, ++count));
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }


    private List<T> filterData = new ArrayList<>();

    private void filterData() throws ParseFilterException {
        try {
            for (int i = 0; i < dataTypeEntities.size(); i++) {

                DataInfoEntity dataInfoEntity = dataTypeEntities.get(i);

                if (dataInfoEntity.getType().equals(JSON_ARRAY)) {
                    if (isStringType()) {
                        filterData.addAll(checkEntityList(dataInfoEntity.getData()));
                        break;
                    } else {
                        boolean isRelativeData = false;
                        for (int k = 0; k < rootFields.size(); k++) {
                            String fName = rootFields.get(k);

//                            if (dataInfoEntity.getData().toLowerCase().indexOf(fName.toLowerCase()) > -1) {
                            if (Pattern.matches(fName, dataInfoEntity.getData())) {
                                isRelativeData = true;
                                break;
                            }
                        }
                        if (isRelativeData) {
                            if (!filterData.isEmpty()) {
                                break;
                            }
                        }
                    }

                } else if (dataInfoEntity.getType().equals(JSON_OBJECT)) {
                    boolean isRelativeData = false;
                    JSONObject jsonObject = new JSONObject(dataInfoEntity.getData());
                    Iterator<String> keys = jsonObject.keys();


                    while (keys.hasNext()) {
                        String obj = (String) keys.next();
                        if (rootFields.contains(obj)) {
                            isRelativeData = true;
                            break;
                        }
                    }

                    if (!isStringType()) {
                        if (isRelativeData) {
                            filterData.add(checkEntity(dataInfoEntity.getData()));
                        } else {
                            if (!filterData.isEmpty()) {
                                break;
                            }
                        }
                    }

                }

            }
        } catch (Exception e) {
            throw new ParseFilterException(e.getMessage());
        }

    }

    private boolean isStringType() {
        if (t.equals(String.class)) {
            return true;
        } else {
            return false;
        }
    }

    private  Gson gson;

    private T checkEntity(String response) {
        try {
            T t = gson.fromJson(response, this.t);
            return t;
        } catch (Exception e) {
            return null;
        }

    }

    private List<T> checkEntityList(String response) {
        try {
            List t = gson.fromJson(response, new TypeToken<ArrayList<T>>() {
            }.getType());
            return t;
        } catch (Exception e) {
            return null;
        }

    }

    private void displayError(String msg) {
        this.filterListener.onError(msg);
    }
}
