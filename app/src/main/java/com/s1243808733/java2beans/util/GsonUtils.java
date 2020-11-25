package com.s1243808733.java2beans.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class GsonUtils {
    
	public static String formatJson(String json) {
        Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
        JsonElement je = new JsonParser().parse(json);
        return gson.toJson(je);
    }

    public static String compressionJson(String json) {
        Gson gson = new GsonBuilder().serializeNulls().setLenient().create();
        JsonElement je = new JsonParser().parse(json);
        return gson.toJson(je);
    }
    
}
