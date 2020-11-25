package com.s1243808733.library.json2bean.util;

import java.util.Locale;

public final class Utils {

    public static String getPrettyType(String type, boolean keepUnderline, boolean skipUpperCase) {
        StringBuilder sb = new StringBuilder();
        char[] array = type.toCharArray();

        //纯大写转小写
        if (!skipUpperCase) {
            int upperCaseCount = 0;
            for (int i = 0;i < array.length;i++) {
                char c = array[i];
                if (Character.isUpperCase(c)) {
                    upperCaseCount++;
                }
            }
            if (upperCaseCount == array.length) {
                return type.toLowerCase(Locale.ENGLISH);
            }
        }

        for (int i = 0;i < array.length;i++) {
            char c=array[i];
            if ((c == '_' && !keepUnderline) || c == '-') {
                if (i + 1 < array.length) {
                    if (i > 0) {
                        if (!Character.isUpperCase(array[i - 1])) {
                            array[i + 1] = Character.toUpperCase(array[i + 1]);
                        }
                    }
                }
                continue;
            }
            sb.append(c);
        }

        return sb.toString();
    }

    public static String toUpperCaseFirst(CharSequence s) {
        char[] charArray = s.toString().toCharArray();
        if (charArray.length > 0) {
            charArray[0] = Character.toUpperCase(charArray[0]);
        }
        return String.valueOf(charArray);
    }

    public static boolean isJavaKeyword(String key) {
        String[] array = {"assert", "boolean", "break", "byte", "case", "catch",
            "char", "class", "const", "continue","default", "do-while", 
            "double", "else", "enum", "extends", "final", "finally", 
            "float", "for", "goto", "if", "implements", "import",
            "instanceof", "int", "interface", "long", "native", "new",
            "package", "private", "protected", "public", "return", "short",
            "static", "strictfp", "super", "switch", "synchronized", "this", 
            "throw", "throws", "transient", "try", "void", "volatile", "while"};
        for (int i = 0;i < array.length;i++) {
            if (array[i].equals(key)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isBaseDataType(Class<?> type) {
        return  boolean.class == type
            || int.class == type
            || long.class == type
            || double.class == type;
    }

    public static boolean isBaseDataTypeWarpper(Class<?> kind) {
        return /*String.class == kind
             || */Boolean.class == kind 
            || Integer.class == kind
            || Long.class == kind 
            || Double.class == kind;
    }

    public static Class<?> unwarpper(Class<?> type) {
        if (Boolean.class == type) {
            return boolean.class;
        } else if (Integer.class == type) {
            return int.class;
        } else if (Long.class == type) {
            return long.class;
        } else if (Double.class == type) {
            return double.class;
        }
        return type;
    }

    public static <T extends Object> T getOrDefault(T obj, T defaultObj) {
        if (obj == null) return defaultObj;
        return obj;
    }

    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public static boolean isTrimEmpty(String s) {
        return s == null || s.trim().length() == 0;
    }

}
