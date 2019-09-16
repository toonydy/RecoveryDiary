package com.example.yeodayeong.recoverydiary;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

public class PrintCalendarAdapter extends BaseAdapter {
    private static final int FIRST_DAY_OF_WEEK = Calendar.MONDAY;
    //데이터베이스
    MyDBHelper myDBHelper;
    SQLiteDatabase sqlDB;
    Cursor cursor;

    Context context;
    ArrayList<CalendarItem> printableList;
    Calendar printCal;
    CalendarItem[] printalDays;
    LayoutInflater printCalInflater;
    ViewHolder printCalHolder;


    public PrintCalendarAdapter(Context context) {
        this.context=context;
        printCal=Calendar.getInstance();
        printCal.set(Calendar.DAY_OF_MONTH,1);
        makePrintCalDays();
        setEnableList();
        printCalInflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        return printalDays.length;
    }

    @Override
    public Object getItem(int position) {
        return printalDays[position];
    }

    @Override
    public long getItemId(int position) {
        CalendarItem printItem=printalDays[position];
        if (printItem!=null){
            return printalDays[position].id;
        }
        return -1;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView=printCalInflater.inflate(R.layout.print_calendar_item,parent,false);
        printCalHolder=new ViewHolder();
        printCalHolder.ivPrintItemGridView=(ImageView)convertView.findViewById(R.id.ivPrintItemGridView);
        printCalHolder.tvPrintItemGridView=(TextView) convertView.findViewById(R.id.tvPrintItemGridView);
        CalendarItem positionItem=printalDays[position];


        if (position<42){
            if (positionItem==null){  //블랭키는 비워둠
                printCalHolder.tvPrintItemGridView.setText(null);
            }else{  //캘린더 아이템 있는 경우
                printCalHolder.tvPrintItemGridView.setText(positionItem.text);
                printCalHolder.tvPrintItemGridView.setAlpha(0.2f);
                for (int i=0;i<printableList.size();i++){
                    if (positionItem.equals(printableList.get(i))){
                        Log.d("TT","yes:"+i);
                        printCalHolder.tvPrintItemGridView.setTypeface(null,Typeface.BOLD);
                        printCalHolder.tvPrintItemGridView.setAlpha(1.0f);
                    }
                }
                for (int k=0;k<PrintCalendarFragment.selectedPrintItemList.size();k++){
                    if (positionItem.equals(PrintCalendarFragment.selectedPrintItemList.get(k))){
                        printCalHolder.ivPrintItemGridView.setVisibility(View.VISIBLE);
                        printCalHolder.tvPrintItemGridView.setTextColor(Color.WHITE);
                    }
                }

            }


        }else{
            printCalHolder.tvPrintItemGridView.setVisibility(View.GONE);
        }

        return convertView;
    }
    void setEnableList(){
        //데이터 있는 날짜 따로 클래스아이템 생성하여 리스트로 저장
        myDBHelper = new MyDBHelper(context);
        printableList = new ArrayList<>();
        sqlDB = myDBHelper.getReadableDatabase();
        cursor = sqlDB.rawQuery("select * from recoveryTBL;", null);
        while ((cursor.moveToNext())) {
            String datetxtstr = cursor.getString(1);
            String datetxtyear = datetxtstr.substring(0, 4);
            String datetxtmonth = datetxtstr.substring(6, 8);
            String datetxtday = datetxtstr.substring(10, 12);
            CalendarItem calendarItemPrintable = new CalendarItem(Integer.parseInt(datetxtyear), (Integer.parseInt(datetxtmonth))-1, Integer.parseInt(datetxtday));
            Log.d("printTest", "calendarItemPrintable: " + calendarItemPrintable.getDateString());
            printableList.add(calendarItemPrintable);
        }
        sqlDB.close();
        cursor.close();
        myDBHelper.close();
    }
    void makePrintCalDays(){
        int year = printCal.get(Calendar.YEAR);
        int month = printCal.get(Calendar.MONTH);
        int firstDayOfMonth = printCal.get(Calendar.DAY_OF_WEEK);
        int lastDayOfMonth = printCal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int printBlankies;
        CalendarItem[] printDays;

        if (firstDayOfMonth == FIRST_DAY_OF_WEEK) {  //1일 요일이 월요일이면 빈칸0개
            printBlankies = 0;
        } else if (firstDayOfMonth < FIRST_DAY_OF_WEEK) {  //1일 요일이 월요일보 작으면
            printBlankies = Calendar.SATURDAY - (FIRST_DAY_OF_WEEK - 1);
        } else {
            printBlankies= firstDayOfMonth - FIRST_DAY_OF_WEEK;  //1일 요일 - 월요일 갯수만큼 빈칸
        }
       printDays = new CalendarItem[lastDayOfMonth + printBlankies];  //그 달의 날 수 + 앞쪽 칸 수만큼 캘린더아이템 객체 배열 생성.

        for (int day = 1, position = printBlankies; position < printDays.length; position++) {
            printDays[position] = new CalendarItem(year, month, day++);
        }
        this.printalDays=printDays;
        notifyDataSetChanged();
    }

    void updatePrintCalAdapter(){
        notifyDataSetChanged();
        makePrintCalDays();
        setEnableList();
    }

    private class ViewHolder{
        FrameLayout framePrintLayoutItem;
        TextView tvPrintItemGridView;
        ImageView ivPrintItemGridView;
//        ImageView ivItemGridViewSelected;
    }
}
