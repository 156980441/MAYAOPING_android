package com.ixp.util;

import java.io.FileOutputStream;
import java.util.Date;

/**
 * Created by ixp on 12/12/16.
 */

public class Util {

    public static String getFileName(String path) {
        String[] arr = path.split("/");
        return arr[arr.length - 1];
    }


    public static Date parseDate(String str) {
        Date date = new Date();
        String[] arr = str.split("[-: ]");
        date.setYear(Integer.parseInt(arr[0]));
        date.setMonth(Integer.parseInt(arr[1]));
        date.setDate(Integer.parseInt(arr[2]));
        date.setHours(Integer.parseInt(arr[3]));
        date.setMinutes(Integer.parseInt(arr[4]));
        date.setSeconds(0);
        return date;
    }

    public static int compareTime(String dateStr1, String dateStr2) {
        if (dateStr1 == null)
            return -1;
        if (dateStr2 == null)
            return 1;

        Date date1 = parseDate(dateStr1);
        Date date2 = parseDate(dateStr2);

        return date1.compareTo(date2);
    }

    public static void saveImage(String filepath, byte[] data, int size) {
        try {
            FileOutputStream outputStream = new FileOutputStream(filepath);
            outputStream.write(data, 0, size);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
