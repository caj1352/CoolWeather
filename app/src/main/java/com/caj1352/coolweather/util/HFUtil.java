package com.caj1352.coolweather.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HFUtil {
    /**
     * 根据日格式出转日期字符串
     * @param pattern
     * @param dateString
     * @return
     */
    public static String formatDateString(String pattern, String dateString) {
        String result = null;
        try {
            // for example: "2020-06-30T22:00+08:00"
            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'+08:00'");
            Date date = format1.parse(dateString);
            SimpleDateFormat format2 = new SimpleDateFormat(pattern);
            result = format2.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 转成"yyyy-MM-dd HH:mm"格式的字符串
     * @param dateString
     * @return
     */
    public static String formatDateString(String dateString) {
        return formatDateString("yyyy-MM-dd HH:mm", dateString);
    }
}
