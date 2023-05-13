package ru.ptrff.motiondesk.models;

public class ParameterField {
    private String name;
    private String typeName;
    private String fieldType;
    private String type;
    private Object value;
    private Object min = 3;
    private Object max = 30;

    public ParameterField() {}

    public void setName(String name) {
        this.name = name;
    }

    public void setTypeName(String type_name) {
        this.typeName = type_name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public void setMin(Object min) {
        this.min = min;
    }

    public void setMax(Object max) {
        this.max = max;
    }

    public String getFieldType() {
        return fieldType;
    }

    public Object getMin() {
        return min;
    }

    public Object getMax() {
        return max;
    }

    public String getName() {
        return name;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }
}
