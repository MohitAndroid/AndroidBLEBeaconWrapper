package com.androidblebeaconwrapperlib.beacon;

import java.util.List;

public class FieldTypeEntity {

    private List<String> fieldValue;
    private Class<?> fieldType;
    private String fieldKey;

    public FieldTypeEntity(List<String> fieldValue, Class<?> fieldType, String fieldKey) {
        this.fieldValue = fieldValue;
        this.fieldType = fieldType;
        this.fieldKey = fieldKey;
    }

    public List<String> getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(List<String> fieldValue) {
        this.fieldValue = fieldValue;
    }

    public Class<?> getFieldType() {
        return fieldType;
    }

    public void setFieldType(Class<?> fieldType) {
        this.fieldType = fieldType;
    }

    public String getFieldKey() {
        return fieldKey;
    }

    public void setFieldKey(String fieldKey) {
        this.fieldKey = fieldKey;
    }
}
