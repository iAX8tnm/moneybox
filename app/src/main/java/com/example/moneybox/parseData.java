package com.example.moneybox;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by yuanc on 2018/3/17.
 *
 */

public class parseData {

    public static String getTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        return simpleDateFormat.format(new Date());
    }

    /**
     *
     * @return  return date string in yyy-MM-dd format
     */
    public static String getCurrentDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyy-MM-dd");
        return simpleDateFormat.format(new Date());
    }


    /**
     *
     * @return return date string in yyy/MM/dd
     */
    public static String getTodayDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyy/M/d");
        return simpleDateFormat.format(new Date());
    }

    public static String getTomorrowDate() {
        Date date=new Date();//取时间
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(calendar.DATE,1);//把日期往后增加一天.整数往后推,负数往前移动
        date=calendar.getTime(); //这个时间就是日期往后推一天的结果

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyy-MM-dd");
        String dateString = simpleDateFormat.format(date);
        return dateString;
    }

    public static String getLastDate() {
        return "";
    }

    public static long parseStringDateToMillis(String date) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new SimpleDateFormat("yyyy/MM/dd").parse(date));
        return calendar.getTimeInMillis();
    }
}
