package com.s1243808733.java2beans;
import android.os.Environment;
import java.io.File;

public class AppSettings {

    public static File getJsonOutputDir() {
        return new File(Environment.getExternalStorageDirectory(), "Json2Bean");
    }

}
