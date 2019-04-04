package com.androidblebeaconwrapperlib.beacon;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class BeaconKeySerializer {
    List<FieldTypeEntity> fieldTypeEntities;

    private BeaconKeySerializer() {
        fieldTypeEntities = new ArrayList<>();
    }

    public static BeaconKeySerializer getInstance(){
        return new BeaconKeySerializer();
    }

    public List<FieldTypeEntity> getBeaconAnnotationDetails(Object object) throws BeaconKeySerializeException {
        try {
            if (object == null) {
                return null;
            }
            Class<?> objectClass = requireNonNull(object).getClass();
            for (Field field : objectClass.getDeclaredFields()) {
                field.setAccessible(true);
                if (field.isAnnotationPresent(BeaconKey.class)) {
                    Class<?> type = field.getType();
                    if (type.isAssignableFrom(ArrayList.class)) {
                        fieldTypeEntities.add(new FieldTypeEntity(((ArrayList) field.get(object)), field.getType(),
                                getSerializedKey(field)));
                    } else {
                        List<String> strings = new ArrayList<>();
                        strings.add((String) field.get(object));
                        fieldTypeEntities.add(new FieldTypeEntity(strings, field.getType(),
                                getSerializedKey(field)));
                    }
                } else {
                    if (!field.getType().isPrimitive())
                        getBeaconAnnotationDetails(field.get(object));
                }
            }
        } catch (Exception e) {
            throw new BeaconKeySerializeException(e.getMessage());
        }
        return fieldTypeEntities;
    }

    public FieldTypeEntity getBeaconFieldTypeEntity(Object beaconObject) throws BeaconKeySerializeException {
        try {
            fieldTypeEntities.clear();
            List<FieldTypeEntity> beaconAnnotationDetails = getBeaconAnnotationDetails(beaconObject);
            return getBeaconFieldTypeEntity(beaconAnnotationDetails);
        } catch (Exception e) {
            throw new BeaconKeySerializeException(e.getMessage());
        }

    }

    private FieldTypeEntity getBeaconFieldTypeEntity(List<FieldTypeEntity> fieldTypeEntities) throws BeaconKeySerializeException {
        try {
            return fieldTypeEntities.get(0);
        } catch (Exception e) {
            throw new BeaconKeySerializeException(e.getMessage());
        }
    }

    public boolean isBeaconFilteredData(String beaconKey, Object beaconObject) throws BeaconKeySerializeException {
        try {
            boolean isKeyMatched = false;
            List<FieldTypeEntity> beaconAnnotationDetails = getBeaconAnnotationDetails(beaconObject);
            FieldTypeEntity beaconFieldTypeEntity = getBeaconFieldTypeEntity(beaconAnnotationDetails);
            for (int i = 0; i < beaconFieldTypeEntity.getFieldValue().size(); i++) {
                if (beaconKey.equals(beaconFieldTypeEntity.getFieldValue().get(i))) {
                    isKeyMatched = true;
                }
            }
            fieldTypeEntities.clear();
            return isKeyMatched;
        } catch (Exception e) {
            throw new BeaconKeySerializeException(e.getMessage());
        }
    }


    private static String getSerializedKey(Field field) {
        return field.getName();
    }
}