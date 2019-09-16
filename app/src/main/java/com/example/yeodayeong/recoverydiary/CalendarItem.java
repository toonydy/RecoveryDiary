package com.example.yeodayeong.recoverydiary;


import java.util.Calendar;
import java.util.Locale;

public class CalendarItem {  //프래그먼트 내에서 현재 클릭된 아이템 정보 받아 이 클래스 인스턴스로 생성하여 메인액티비티에서 활용

    public int year;
    public int month;
    public int day;
    public String text;
    public long id;
    Locale locale;


    public CalendarItem(Calendar calendar) {
        this(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
    }

    public CalendarItem(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.text = String.valueOf(day);
        this.id = Long.valueOf(year + "" + month + "" + day);
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof CalendarItem) {
            final CalendarItem item = (CalendarItem) o;
            return item.year == year && item.month == month && item.day == day;
        }
        return false;
    }

    String getDateString() {
        String dateNumStr=Long.toString(getDateNum());
        String dateString;
        dateString=year + "년 " + MonthSetting.setMonthNumStr(month) + "월 " + dateNumStr.substring(6,8) + "일 ";
        dateString+=MonthSetting.setDayOfWeek(year,month,day);
        return dateString;
    }


    Long getDateNum(){
        String monthString;
        String dayString;

        if ((month)<10){
            monthString="0"+(month);
        }else{
            monthString=Integer.toString(month);
        }

        if (day<10){
            dayString="0"+day;
        }else{
            dayString=Integer.toString(day);
        }
        return Long.parseLong(Integer.toString(year)+monthString+dayString);
    }
}
