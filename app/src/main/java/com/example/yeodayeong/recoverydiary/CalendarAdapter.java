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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

//달에 해당하는 캘린더 객체 받아 그리드뷰에 세팅하고, 인플레이팅 시켜 뷰 형태로 반환. (그리드뷰세팅어댑터)
public class CalendarAdapter extends BaseAdapter {
    private static final int FIRST_DAY_OF_WEEK = Calendar.MONDAY;

    static String FUTURE_TAG = "futureTag"; //미래+넘어온날 아님
    static String DONE_TAG = "doneTag"; //지난 날+넘어온 날 아님
    static String OVER_DONE_TAG = "overedTag";  //지난 날+넘어온 날
    static String OVER_FUTURE_TAG="overdFutureTag";  //미래+넘어온 날
    static String NULL_TAG = "nullTag";


    Context context;
    Calendar calendar;
    CalendarItem today;
    CalendarItem selected;
    LayoutInflater inflater;
    CalendarItem[] days;
    ViewHolder viewHolder;
    boolean firstSw = false;

    boolean lastMonthOverTF=false;
    CalendarItem[] overLastDaysItems;

    //데이터베이스
    MyDBHelper myDBHelper;
    SQLiteDatabase sqlDB;
    Cursor cursor;

    ArrayList<CalendarItem> recorededCalendarItemList;


    public CalendarAdapter(Context context, Calendar calendar) {  //캘린더어댑터 객체 생성시 캘린더정보
        this.context = context;
        Log.d("refreshDays","0");
        this.calendar = calendar;
        Log.d("refreshDays","1");
        today = new CalendarItem(calendar);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
//        lastOver=lastover;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setRecorededList();
        Log.d("refreshDays","3");
//        refreshDays();
    }


    @Override
    public int getCount() {
        return days.length;
//        return 35;
    }

    @Override
    public Object getItem(int position) {
        if (lastMonthOverTF){
            if (position < overLastDaysItems.length){
                return overLastDaysItems[position];
            }
        }
        return days[position];
    }

    @Override
    public long getItemId(int position) {
        if (lastMonthOverTF){
            if (position < overLastDaysItems.length){
                return overLastDaysItems[position].id;
            }
        }
        CalendarItem item = days[position];
        if (item != null) {
            return days[position].id;
        }
        return -1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = inflater.inflate(R.layout.calendar_item, parent, false);
        viewHolder = new ViewHolder();

        viewHolder.tvItemGridView = (TextView) convertView.findViewById(R.id.tvItemGridView);
        viewHolder.ivItemGridView = (ImageView) convertView.findViewById(R.id.ivItemGridView);
        viewHolder.ivItemGridViewSelected = (ImageView) convertView.findViewById(R.id.ivItemGridViewChecked);
//        viewHolder.frameLayoutItem=(FrameLayout)convertView.findViewById(R.id.frameLayoutItem);

         //currentItem 이라는 임시의 아이템에 순서대로 모두 담아가며
        CalendarItem currentItem = days[position]; //일단 기본적으로는 날짜아이템을 커런트아이템에 그냥 담음

        if (position < 35) {
            if (lastMonthOverTF){  //지난달 이월 날짜가 있다면 널로 두지 않고 받아온 이월 리스트 번호로 현재아이템 받음
                Log.d("lasttest","lastMonthOverTF:true");
                if (position < overLastDaysItems.length){
                    Log.d("lasttest","overLastDaysItems: "+overLastDaysItems.length);
                    Log.d("lasttest","overLastDaysItems["+position+"]: "+overLastDaysItems[position].text);
                    currentItem= overLastDaysItems[position];
                    convertView.setTag(OVER_DONE_TAG);

                }
            }

            if (currentItem == null) {  //날짜가 아닌 날(캘린더아이템 객체가 ㅇ없고 블랭키만 았음 + 지난달 이월 날짜도 없음
                viewHolder.tvItemGridView.setClickable(false);
                viewHolder.tvItemGridView.setFocusable(false);
                convertView.setBackgroundDrawable(null);
                viewHolder.tvItemGridView.setText(null);
//                convertView.setTag(3,NULL_TAG);

            } else {  //이번 아이템이 5줄 안에 잘 들어있고 , null 도 아니면 -> 아직 오지 않은 날인지 오늘 포함 지난 날인지 확인


                if ((today.year == currentItem.year) && (today.month <= currentItem.month)) {//아직 오지 않은 날1: 올해 중 다음달 이후이거나 / 오늘 이후 이번달 날짜 (비활성화하고 퓨처태그 세팅)
                    if ((today.day < currentItem.day) && (today.month == currentItem.month)) {
                        viewHolder.tvItemGridView.setAlpha(0.3f);
                        viewHolder.tvItemGridView.setClickable(false);
                        viewHolder.tvItemGridView.setFocusable(false);
                        if (convertView.getTag()!=null  && convertView.getTag().toString().equals(OVER_DONE_TAG)){  //미래에 해당하며 동시에 넘어온 날인 경우
                            convertView.setTag(OVER_FUTURE_TAG);
                        }else{  //미래지만 넘어온 날은 아닌 경우
                            convertView.setTag(FUTURE_TAG);
                        }

                    } else if (today.month < currentItem.month) {
                        viewHolder.tvItemGridView.setAlpha(0.3f);
                        viewHolder.tvItemGridView.setClickable(false);
                        viewHolder.tvItemGridView.setFocusable(false);
                        if (convertView.getTag()!=null  && convertView.getTag().toString().equals(OVER_DONE_TAG)){  //미래에 해당하며 동시에 넘어온 날인 경우
                            convertView.setTag(OVER_FUTURE_TAG);
                        }else{  //미래지만 넘어온 날은 아닌 경우
                            convertView.setTag(FUTURE_TAG);
                        }
                    }
                } else if (today.year < currentItem.year) { //아직 오지 않은 날2: 내년 (비활성화하고 퓨처태그 세팅)
                    viewHolder.tvItemGridView.setAlpha(0.3f);
                    viewHolder.tvItemGridView.setClickable(false);
                    viewHolder.tvItemGridView.setFocusable(false);
                    if (convertView.getTag()!=null  && convertView.getTag().toString().equals(OVER_DONE_TAG)){  //미래에 해당하며 동시에 넘어온 날인 경우
                        convertView.setTag(OVER_FUTURE_TAG);
                    }else{  //미래지만 넘어온 날은 아닌 경우
                        convertView.setTag(FUTURE_TAG);
                    }

                }

                //오늘과 이전 날들
                if (convertView.getTag() == null  || convertView.getTag().toString().equals(OVER_DONE_TAG)  || convertView.getTag().toString().equals(DONE_TAG)) {
                    if (currentItem.equals(today)) {
                        viewHolder.tvItemGridView.setTypeface(null, Typeface.BOLD);
                    }
                    if (currentItem.equals(selected)) {
                        if (Main2Activity.collaped) {
                            viewHolder.ivItemGridViewSelected.setVisibility(View.VISIBLE);
                        }
                    }
                    for (int i = 0; i < recorededCalendarItemList.size(); i++) {
                        if (currentItem.equals(recorededCalendarItemList.get(i))) {
                            viewHolder.ivItemGridView.setVisibility(View.VISIBLE);
                            viewHolder.tvItemGridView.setTextColor(Color.WHITE);
                        }
                    }

                    if ((convertView.getTag()==null) || !(convertView.getTag().toString().equals(OVER_DONE_TAG))){  //오늘 이전이지만 넘어온 날은 아닌 경우
                        convertView.setTag(DONE_TAG);
                    }

                }

                //오지 않은 날 포함 null만 아니면 모두 텍스트 표시
                if (lastMonthOverTF){
                    if (position < overLastDaysItems.length){
                        viewHolder.tvItemGridView.setText((currentItem.month+1)+"/"+currentItem.text);
                    }else {
                        viewHolder.tvItemGridView.setText(currentItem.text);
                    }
                }else {
                    viewHolder.tvItemGridView.setText(currentItem.text);
                }


            }


        } else {  //5줄 넘어가면 칸 아예 안보이게
            viewHolder.tvItemGridView.setVisibility(View.GONE);
            viewHolder.ivItemGridView.setVisibility(View.GONE);
            viewHolder.ivItemGridViewSelected.setVisibility(View.GONE);
        }
        if (convertView.getTag()==null){
            convertView.setTag(NULL_TAG);
        }

            return convertView;

    }



    void setRecorededList() {
        //데이터 있는 날짜 따로 클래스아이템 생성하여 리스트로 저장
        myDBHelper = new MyDBHelper(context);
        recorededCalendarItemList = new ArrayList<>();
        sqlDB = myDBHelper.getReadableDatabase();
        cursor = sqlDB.rawQuery("select * from recoveryTBL;", null);
        while ((cursor.moveToNext())) {
            String datetxtstr = cursor.getString(1);
            String datetxtyear = datetxtstr.substring(0, 4);
            String datetxtmonth = datetxtstr.substring(6, 8);
            String datetxtday = datetxtstr.substring(10, 12);
            CalendarItem calendarItemRecorded = new CalendarItem(Integer.parseInt(datetxtyear), (Integer.parseInt(datetxtmonth)) - 1, Integer.parseInt(datetxtday));
            Log.d("arrs", "calendarItemRecorded: " + calendarItemRecorded.getDateString());
            recorededCalendarItemList.add(calendarItemRecorded);
        }
        sqlDB.close();
        cursor.close();
        myDBHelper.close();
    }

    void setSelected(int year, int month, int day) {
        selected = new CalendarItem(year, month, day);
        selected.year = year;
        selected.month = month;
        selected.day = day;
        notifyDataSetChanged();
    }

    void setSelected(CalendarItem selectedCalItem) {
        selected = selectedCalItem;
        notifyDataSetChanged();
    }


    void refreshDays() {  //들어온 캘린더 객체가 바뀌면 새로 세팅
        Log.d("refreshDays","refreshDays    ");
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int firstDayOfMonth = calendar.get(Calendar.DAY_OF_WEEK);
        int lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int blankies;
        CalendarItem[] days;

        if (firstDayOfMonth == FIRST_DAY_OF_WEEK) {  //1일 요일이 월요일이면 빈칸0개
            blankies = 0;
        } else if (firstDayOfMonth < FIRST_DAY_OF_WEEK) {  //1일 요일이 월요일보 작으면
            blankies = Calendar.SATURDAY - (FIRST_DAY_OF_WEEK - 1);
        } else {
            blankies = firstDayOfMonth - FIRST_DAY_OF_WEEK;  //1일 요일 - 월요일 갯수만큼 빈칸
        }
        days = new CalendarItem[lastDayOfMonth + blankies];  //그 달의 날 수 + 앞쪽 칸 수만큼 캘린더아이템 객체 배열 생성.

        for (int day = 1, position = blankies; position < days.length; position++) {
            days[position] = new CalendarItem(year, month, day++);
        }
        this.days = days;
        notifyDataSetChanged();
    }

    private class ViewHolder {
        FrameLayout frameLayoutItem;
        TextView tvItemGridView;
        ImageView ivItemGridView;
        ImageView ivItemGridViewSelected;
    }

    void setLastMonthOver(boolean lastMonthOverTF){
        this.lastMonthOverTF=false;
        this.overLastDaysItems=new CalendarItem[0];
        notifyDataSetChanged();
    }

    void setLastMonthOver(boolean lastMonthOverTF,CalendarItem[] overLastDaysItems){
        this.lastMonthOverTF=lastMonthOverTF;
        this.overLastDaysItems=overLastDaysItems;
        notifyDataSetChanged();
    }

}

