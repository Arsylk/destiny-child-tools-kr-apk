package com.arsylk.mammonsmite.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Log {
    public final static String TAG_DEFAULT = "mTag";
    public final static File FILE_DEFAULT = new File(Define.BASE_DIRECTORY, "log.txt");



    public static void append(String msg) {
        Log.append(TAG_DEFAULT, msg);
    }

    public static void append(String tag, String msg) {
        if(!FILE_DEFAULT.exists()) {
            try {
                FILE_DEFAULT.createNewFile();
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
        try {
            String line  = String.format(Locale.US, "[%s] %s> %s", Log.timestamp(), tag, msg);
            System.out.println(line);

            BufferedWriter buf = new BufferedWriter(new FileWriter(FILE_DEFAULT, true));
            buf.append(line);
            buf.newLine();
            buf.close();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static String timestamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        dateFormat.setTimeZone(TimeZone.getDefault());
        Date today = Calendar.getInstance().getTime();
        return dateFormat.format(today);
    }

}
