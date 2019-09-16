package com.example.yeodayeong.recoverydiary;

import java.util.Calendar;

public class MonthSetting {

    static String monthString;
    static String monthNumString;

    static String setMonthText(int monthInt) {

        switch (monthInt) {
            case Calendar.JANUARY:
                monthString = "JANUARY";
                break;
            case Calendar.FEBRUARY:
                monthString = "FEBRUARY";
                break;
            case Calendar.MARCH:
                monthString = "MARCH";
                break;
            case Calendar.APRIL:
                monthString = "APRIL";
                break;
            case Calendar.MAY:
                monthString = "MAY";
                break;
            case Calendar.JUNE:
                monthString = "JUNE";
                break;
            case Calendar.JULY:
                monthString = "JULY";
                break;
            case Calendar.AUGUST:
                monthString = "AUGUST";
                break;
            case Calendar.SEPTEMBER:
                monthString = "SEPTEMBER";
                break;
            case Calendar.OCTOBER:
                monthString = "OCTOBER";
                break;
            case Calendar.NOVEMBER:
                monthString = "NOVEMBER";
                break;
            case Calendar.DECEMBER:
                monthString = "DECEMBER";
                break;

        }
        return monthString;
    }

    static String setMonthNumStr(int monthInt) {

        switch (monthInt) {
            case Calendar.JANUARY:
                monthNumString = "01";
                break;
            case Calendar.FEBRUARY:
                monthNumString = "02";
                break;
            case Calendar.MARCH:
                monthNumString = "03";
                break;
            case Calendar.APRIL:
                monthNumString = "04";
                break;
            case Calendar.MAY:
                monthNumString = "05";
                break;
            case Calendar.JUNE:
                monthNumString = "06";
                break;
            case Calendar.JULY:
                monthNumString = "07";
                break;
            case Calendar.AUGUST:
                monthNumString = "08";
                break;
            case Calendar.SEPTEMBER:
                monthNumString = "09";
                break;
            case Calendar.OCTOBER:
                monthNumString = "10";
                break;
            case Calendar.NOVEMBER:
                monthNumString = "11";
                break;
            case Calendar.DECEMBER:
                monthNumString = "12";
                break;

        }
        return monthNumString;
    }


    static  String setDayOfWeek(int year,int month,int day){
        Calendar calendar=Calendar.getInstance();
        calendar.set(year,month,day);
        int dow=calendar.get(Calendar.DAY_OF_WEEK);
       if(dow==Calendar.MONDAY)
           return "월요일";
       else if (dow==Calendar.TUESDAY)
           return "화요일";
       else if (dow==Calendar.WEDNESDAY)
           return "수요일";
       else if (dow==Calendar.THURSDAY)
           return "목요일";
       else if (dow==Calendar.FRIDAY)
           return "금요일";
       else if (dow==Calendar.SATURDAY)
           return "토요일";
       else if (dow==Calendar.SUNDAY)
           return "일요일";
       return "";
    }

}
