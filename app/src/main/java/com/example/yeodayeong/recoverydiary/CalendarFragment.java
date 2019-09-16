package com.example.yeodayeong.recoverydiary;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.example.yeodayeong.recoverydiary.CalendarAdapter.DONE_TAG;
import static com.example.yeodayeong.recoverydiary.CalendarAdapter.FUTURE_TAG;
import static com.example.yeodayeong.recoverydiary.CalendarAdapter.OVER_DONE_TAG;
import static com.example.yeodayeong.recoverydiary.CalendarAdapter.OVER_FUTURE_TAG;
import static com.example.yeodayeong.recoverydiary.Main2Activity.myDarkGrey;
import static com.example.yeodayeong.recoverydiary.Main2Activity.myGreen;
import static com.example.yeodayeong.recoverydiary.Main2Activity.myGrey;
import static com.example.yeodayeong.recoverydiary.Main2Activity.myOrange;


public class CalendarFragment extends Fragment {
    //전용 xml에 그리드뷰를 포함. Calendar 클래스 객체를 생성후 그리드뷰용 어댑터를 생성할 때 매개로 보냄.

    Calendar calendar;
    ViewFlipper calendarSwitcher;
    Locale locale;
    CalendarAdapter calendarAdapter;
    GridView calendarGridView;
    GridView calendarDayGridView;
    LinearLayout calBackPaper;
    ImageView btnAddinCal;

    OnMyListener onMyListener;
    CalendarItem currentMonthItem;
    TextView tmp;
    CalendarItem calendarItemSelected;

    Calendar lastCalendar;
    CalendarItem[] lastdays;
    CalendarItem[] overLastDaysItems;
    boolean lastMonthOverTF = false;
    private static final int FIRST_DAY_OF_WEEK = Calendar.MONDAY;

    public interface OnMyListener {  // 프레그먼트 데이터를 메인에 전달하기 위한 인터페이스
        void onReceivedSelectedData(Object data1);
        void onReceivedCurrentData(Object data2);
        void onAddBtnClickListner();
    }

    public CalendarFragment() {  //프래그먼트객체 생성과 동시에 캘린터 객체 생성.
        calendar = Calendar.getInstance();
        locale = Locale.getDefault();
        changeLastCalender();
//        lastCalendar.set(Calendar.DAY_OF_MONTH,1);
//        refreshLastDays();
//        checkLastMonth();
    }

    void changeLastCalender() {
        if (lastCalendar == null) {
            lastCalendar = Calendar.getInstance();
        }
 /*       lastCalendar.set(Calendar.YEAR,calendar.get(Calendar.YEAR));
        lastCalendar.set(Calendar.MONTH,(calendar.get(Calendar.MONTH))-2);*/
        if (calendar.get(Calendar.MONTH) == Calendar.JANUARY) {
            lastCalendar.set((calendar.get(Calendar.YEAR)) - 1, Calendar.DECEMBER, 1);
        } else {
            lastCalendar.set((calendar.get(Calendar.YEAR)), calendar.get(Calendar.MONTH) - 1, 1);
        }
        SimpleDateFormat aaaa = new SimpleDateFormat("yyyyMMdd");
        Log.d("lasttest", "cal: " + aaaa.format(calendar.getTime()));
        Log.d("lasttest", "lastCal:" + aaaa.format(lastCalendar.getTime()));

    }

    void refreshLastDays() {  //들어온 캘린더 객체가 바뀌면 새로 세팅
        int year = lastCalendar.get(Calendar.YEAR);
        int month = lastCalendar.get(Calendar.MONTH);
        int firstDayOfMonth = lastCalendar.get(Calendar.DAY_OF_WEEK);
        int lastDayOfMonth = lastCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int lastblankies = 0;
        CalendarItem[] lastdays;

        if (firstDayOfMonth == FIRST_DAY_OF_WEEK) {  //1일 요일이 월요일이면 빈칸0개
            lastblankies = 0;
        } else if (firstDayOfMonth < FIRST_DAY_OF_WEEK) {  //1일 요일이 월요일보 작으면
            lastblankies = Calendar.SATURDAY - (FIRST_DAY_OF_WEEK - 1);
        } else {
            lastblankies = firstDayOfMonth - FIRST_DAY_OF_WEEK;  //1일 요일 - 월요일 갯수만큼 빈칸
        }
        lastdays = new CalendarItem[lastDayOfMonth + lastblankies];  //그 달의 날 수 + 앞쪽 칸 수만큼 캘린더아이템 객체 배열 생성.

        for (int day = 1, position = lastblankies; position < lastdays.length; position++) {
            lastdays[position] = new CalendarItem(year, month, day++);
        }
        this.lastdays = lastdays;

    }

    void checkLastMonth() {
        if (lastdays.length > 35) {
            overLastDaysItems = new CalendarItem[lastdays.length - 35];
            for (int a = 0; a < overLastDaysItems.length; a++) {
                CalendarItem overDay = lastdays[35 + a];
                overLastDaysItems[a] = overDay;
            }
            lastMonthOverTF = true;
            calendarAdapter.setLastMonthOver(true, overLastDaysItems);
        } else {
            calendarAdapter.setLastMonthOver(false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LinearLayout calLayout = (LinearLayout) inflater.inflate(R.layout.fragment_calendar, null);
        calendarGridView = (GridView) calLayout.findViewById(R.id.calendar_grid);
        calendarDayGridView = (GridView) calLayout.findViewById(R.id.calendar_days_grid);
        calBackPaper=(LinearLayout) calLayout.findViewById(R.id.calBackPaper);
        btnAddinCal=(ImageView)calLayout.findViewById(R.id.btnAddinCal);

        //캘린더 배경 깔기위해 1. 전체 레이아웃 사이즈
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
       int  height = size.y;
        ViewGroup.LayoutParams backparams=calBackPaper.getLayoutParams();
        backparams.height=(height/2)-(height/18);



        final GestureDetector swipeDetector = new GestureDetector(getActivity(), new SwipeGesture(getActivity()) {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1.getX() - e2.getX() > swipeMinDistance && Math.abs(velocityX) > swipeThresholdVelocity) {
                    onNextMonth();
                } else if (e2.getX() - e1.getX() > swipeMinDistance && Math.abs(velocityX) > swipeThresholdVelocity) {
                    onPreviousMonth();
                }
                return false;
            }
        });
        calendarSwitcher = (ViewFlipper) calLayout.findViewById(R.id.calendar_switcher);

        calendarAdapter = new CalendarAdapter(getActivity(), calendar);
        //
        refreshLastDays();
        checkLastMonth();
        //
        updateCurrentMonth();

        calendarGridView.setOnItemClickListener(new DayItemClickListener());
        btnAddinCal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onMyListener != null) {
                    onMyListener.onAddBtnClickListner();
                }
            }
        });


        calendarGridView.setAdapter(calendarAdapter);
        calendarGridView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return swipeDetector.onTouchEvent(event);
            }
        });
        DayTxtAdapter dayTxtAdapter = new DayTxtAdapter(getActivity(), getResources().getStringArray(R.array.days_array));
//        ArrayAdapter<String> dayTxtAdapter=new ArrayAdapter<>(getActivity(),R.layout.calendar_days_item,getResources().getStringArray(R.array.days_array));
        calendarDayGridView.setAdapter(dayTxtAdapter);
//        calendarGridView.setSelection();
        return calLayout;
    }

    @Override
    public void onAttach(Context context) {  // 메인액티비티를 인터페이스객체회하여, 메인액티비티가 프레그먼트 내부 데이터로 작업할수있도록 프래그먼트 내에서 원격조종하게 해줌
        super.onAttach(context);
        if (context != null && context instanceof OnMyListener) {  //메인액티비티(context)가 널이 아니고,OnMyListener인터페이스를 임플리먼트 하고 있다면
            onMyListener = (OnMyListener) context;  // 1. 메인엑티비티를 onMyListener 객체로 캐스팅
        }

    }

    void updateCurrentMonth() {  //어댑터에 캘린더 정보 갱신되면 새 캘린더객체로 그리드뷰 다시 세팅하는 메소드.
        calendarAdapter.refreshDays();
        currentMonthItem = new CalendarItem(calendar);
//        Log.d("testtem","testItem"+currentMonthItem.year+" "+currentMonthItem.month+" "+currentMonthItem.day);
        if (onMyListener != null) {
            onMyListener.onReceivedCurrentData(currentMonthItem);
        }
    }

    class DayItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


            TextView dayView = (TextView) view.findViewById(R.id.tvItemGridView);
            CharSequence text = dayView.getText();

            if (text != null && !("".equals(text))) {
                if (calendarAdapter != null) {
                    if (view.getTag().toString().equals(OVER_DONE_TAG)) {
                        calendarAdapter.setSelected(calendarAdapter.overLastDaysItems[0].year, calendarAdapter.overLastDaysItems[0].month, Integer.valueOf(((String.valueOf(text)).split("/")[1])));
                        calendarItemSelected = new CalendarItem(calendarAdapter.overLastDaysItems[0].year, calendarAdapter.overLastDaysItems[0].month, Integer.valueOf(((String.valueOf(text)).split("/")[1])));
                    } else if (view.getTag().toString().equals(DONE_TAG)) {
                        calendarAdapter.setSelected(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), Integer.valueOf(String.valueOf(text)));
                        calendarItemSelected = new CalendarItem(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), Integer.valueOf(String.valueOf(text)));
                    } /* else if (view.getTag().toString().equals(OVER_FUTURE_TAG)) {
                        calendarAdapter.setSelected(calendarAdapter.overLastDaysItems[0].year, calendarAdapter.overLastDaysItems[0].month, Integer.valueOf(((String.valueOf(text)).split("/")[1])));
                        calendarItemSelected = new CalendarItem(calendarAdapter.overLastDaysItems[0].year, calendarAdapter.overLastDaysItems[0].month, Integer.valueOf(((String.valueOf(text)).split("/")[1])));
                    } else if (view.getTag().toString().equals(FUTURE_TAG)) {
                        calendarAdapter.setSelected(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), Integer.valueOf(String.valueOf(text)));
                        calendarItemSelected = new CalendarItem(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), Integer.valueOf(String.valueOf(text)));
                    }*/

                    if (onMyListener != null) {
                        if (!(view.getTag().toString().equals(FUTURE_TAG)) && !(view.getTag().toString().equals(OVER_FUTURE_TAG))) { //퓨쳐태그 달린 경우 셀렉트아이템 생성하지도 않고, 메인에 레이아웃 여닫기 및 텍스트 표시하기 명령도 하지 않음
                            onMyListener.onReceivedSelectedData(calendarItemSelected);
                            //2. onReceivedDat메소드를 가지는 onMyListener형으로 캐스팅한 메인메소드가 프래그먼트 데이터 가지고 메소드 실행하도록 원격조종.
                        }
                    }
                }

            }
        }
    }

    //날짜 옆 화살표나 디테일란 스와이프로 전날로이동
    void moveToPrevDay() {
        if (calendarAdapter != null) {
            //1일 혹은 첫날일 경우 달 이동
            if (calendarAdapter.lastMonthOverTF) {
                if ((((CalendarItem) calendarAdapter.getItem(0)).getDateString()).equals(calendarAdapter.selected.getDateString())) {
                    onPreviousMonth();
                }
            } else {
                if (calendarAdapter.selected.text.equals("1")) {
                    onPreviousMonth();
                }
            }

            Calendar foundYesterday = Calendar.getInstance();
            foundYesterday.set(calendarAdapter.selected.year, calendarAdapter.selected.month, calendarAdapter.selected.day);
            foundYesterday.add(Calendar.DATE, -1);
            calendarAdapter.setSelected(foundYesterday.get(Calendar.YEAR), foundYesterday.get(Calendar.MONTH), foundYesterday.get(Calendar.DAY_OF_MONTH));
            calendarItemSelected = new CalendarItem(foundYesterday.get(Calendar.YEAR), foundYesterday.get(Calendar.MONTH), foundYesterday.get(Calendar.DAY_OF_MONTH));
            calendarAdapter.notifyDataSetChanged();
            if (onMyListener != null) {
                onMyListener.onReceivedSelectedData(calendarItemSelected);
//                onMyListener.onReceivedLastDay(false);
            }
        }

    }

    //날짜 옆 화살표나 디테일란 스와이프로 다음날로 이동
    void moveToNextDay() {
        if (calendarAdapter != null) {
            //마지막날일 경우 달 이동
            Log.d("nextwhy", "selected:" + calendarAdapter.selected.text + "  last:" + ((CalendarItem) (calendarAdapter.getItem(calendarAdapter.days.length - 1))).text);
            if (calendarAdapter.days.length > 35) {
                if ((((CalendarItem) (calendarAdapter.getItem(34))).getDateString()).equals(calendarAdapter.selected.getDateString())) {
                    onNextMonth();
                }
            } else {
                if ((((CalendarItem) (calendarAdapter.getItem(calendarAdapter.days.length - 1))).getDateString()).equals(calendarAdapter.selected.getDateString())) {
                    Log.d("nextwhy", "같음");
                    onNextMonth();
                }
            }


            Calendar foundTommorow = Calendar.getInstance();
            foundTommorow.set(calendarAdapter.selected.year, calendarAdapter.selected.month, calendarAdapter.selected.day);
            foundTommorow.add(Calendar.DATE, 1);
            calendarAdapter.setSelected(foundTommorow.get(Calendar.YEAR), foundTommorow.get(Calendar.MONTH), foundTommorow.get(Calendar.DAY_OF_MONTH));
            calendarItemSelected = new CalendarItem(foundTommorow.get(Calendar.YEAR), foundTommorow.get(Calendar.MONTH), foundTommorow.get(Calendar.DAY_OF_MONTH));
            calendarAdapter.notifyDataSetChanged();
            if (onMyListener != null) {
                onMyListener.onReceivedSelectedData(calendarItemSelected);
            }

        }

    }

    void onNextMonth() {
        calendarSwitcher.setInAnimation(getActivity(), R.anim.in_from_left);
        calendarSwitcher.setOutAnimation(getActivity(), R.anim.out_to_left);
        calendarSwitcher.showNext();
        if (calendar.get(Calendar.MONTH) == Calendar.DECEMBER) {
            calendar.set(calendar.get(Calendar.YEAR) + 1, Calendar.JANUARY, 1);
        } else {
            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
        }
        changeLastCalender();
        refreshLastDays();
        checkLastMonth();
        updateCurrentMonth();
    }

    void onPreviousMonth() {
        calendarSwitcher.setInAnimation(getActivity(), R.anim.in_from_left);
        calendarSwitcher.setOutAnimation(getActivity(), R.anim.out_to_right);
        calendarSwitcher.showPrevious();
        if (calendar.get(Calendar.MONTH) == Calendar.JANUARY) {
            calendar.set((calendar.get(Calendar.YEAR)) - 1, Calendar.DECEMBER, 1);
        } else {
            calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
        }
        changeLastCalender();
        refreshLastDays();
        checkLastMonth();
        updateCurrentMonth();
    }

    void onSelectedMonth(int year, int mon) {
        calendar.set(year, mon, 1);
        updateCurrentMonth();
    }


    class DayTxtAdapter extends BaseAdapter {
        Context context;
        String[] strings;

        public DayTxtAdapter(Context context, String[] strings) {
            this.context = context;
            this.strings = strings;
        }

        @Override
        public int getCount() {
            return strings.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = layoutInflater.inflate(R.layout.day_txt_item, parent, false);
            }
            //f레이아웃 리소스파일인 R.layout.day_txt_item 안에있는 R.id.tvDayTxtGrid라는 텍스트뷰 이용
            TextView textView = ((TextView) view.findViewById(R.id.tvDayTxtGrid));
            textView.setText(strings[position]);
            if (position == 6) {
                textView.setTextColor(myOrange);
                textView.setAlpha(0.8f);
            } else if (position == 5) {
                textView.setTextColor(myGreen);
                textView.setAlpha(0.8f);
            } else
                textView.setTextColor(myDarkGrey);
            return view;
        }
    }
}
