package com.example.xmatenotes.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    /**
     *
     * @param timelong
     * @param format 确保格式正确，例："yyyy年MM月dd日-hh时mm分ss秒"/"yyyy-MM-dd HH:mm:ss SSS"/"yyyy/MM/dd-hh:mm:ss"
     * @return
     */
    public static String formatTimelong(long timelong, String format){
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date datetime = new Date(timelong);
        return sdf.format(datetime);
    }
}
