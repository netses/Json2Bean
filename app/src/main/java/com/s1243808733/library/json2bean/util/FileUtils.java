package com.s1243808733.library.json2bean.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

public final class FileUtils {

    public static void writeFileFromString(final File file, final String content) throws IOException {
        BufferedWriter bw = null;
        try {
            File parentFile = file.getParentFile();
            if (parentFile != null) parentFile.mkdirs();
            if (file.exists()) file.delete();
            file.createNewFile();
            bw = new BufferedWriter(new FileWriter(file, false));
            bw.write(content);
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {}
                try {
                    bw.flush();
                } catch (IOException e) {}
            }
        }
    }

    public static String readFile2String(File file) throws IOException {
        return read2String(new FileReader(file));
    }

    public static String read2String(Reader reader) throws IOException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(reader);
            StringBuffer buffer = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                buffer.append(line);
            }
            return buffer.toString();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {}
            }
        }
    }

    public static void deleteAllFileInDir(File file) {
        if (file == null || !file.exists()) 
            return;
        File[] listFiles = file.listFiles();
        if (listFiles != null) {
            for (File f: listFiles) {
                if (f.isDirectory()) {
                    deleteAllFileInDir(f);
                } else {
                    f.delete();
                }
            }
        }
        file.delete();
    }

}
