package ru.ptrff.motiondesk.utils;

public class Validation {
    public static  boolean checkString(String... inputs) {
        for (String input: inputs) {
            int letterCount = 0;
            int specialCharCount = 0;
            for (int i = 0; i < input.length(); i++) {
                char c = input.charAt(i);
                if (Character.isLetter(c)) {
                    letterCount++;
                } else if (!Character.isDigit(c) && !Character.isWhitespace(c)) {
                    specialCharCount++;
                }
            }
            if(!(letterCount >=3 && input.length() <= 100 && specialCharCount <= 25)){
                return false;
            }
        }
        return true;
    }

    public static  boolean checkString(int min, int max, int maxSpecialChars, String... inputs) {
        for (String input: inputs) {
            int letterCount = 0;
            int specialCharCount = 0;
            for (int i = 0; i < input.length(); i++) {
                char c = input.charAt(i);
                if (Character.isLetter(c)) {
                    letterCount++;
                } else if (!Character.isDigit(c) && !Character.isWhitespace(c)) {
                    specialCharCount++;
                }
            }
            if(!(letterCount >= min && input.length() <= max && specialCharCount <= maxSpecialChars)){
                return false;
            }
        }
        return true;
    }


    public static Object checkValue(String value, String type, Object min, Object max) {
        if (type.equals("int")){
            int typedValue = Integer.parseInt(value);
            int typedMin = Integer.parseInt(min.toString());
            int typedMax = Integer.parseInt(max.toString());
            if(typedValue>=typedMin && typedValue<=typedMax)
                return value;
        }
        if (type.equals("float")){
            float typedValue = Float.parseFloat(value);
            float typedMin = Float.parseFloat(min.toString());
            float typedMax = Float.parseFloat(max.toString());
            if(typedValue>=typedMin && typedValue<=typedMax)
                return value;
        }
        if(type.equals("string")){
            int typedMin = Integer.parseInt(min.toString());
            int typedMax = Integer.parseInt(max.toString());
            if(checkString(typedMin, typedMax, typedMax-typedMin, value))
                return value;
        }

        return null;
    }
}
