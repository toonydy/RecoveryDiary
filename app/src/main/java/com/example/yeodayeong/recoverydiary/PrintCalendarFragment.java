package com.example.yeodayeong.recoverydiary;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

public class PrintCalendarFragment extends Fragment {
    //전용 xml에 그리드뷰를 포함. Calendar 클래스 객체를 생성후 그리드뷰용 어댑터를 생성할 때 매개로 보냄.

    PrintCalendarAdapter printCalendarAdapter;
    GridView printCalendarGridView;
    GridView printCalendarDayGridView;
    TextView tvPrintCalendarTxt;
    ImageButton ivPrintPrev, ivPrintNext;
    MyPrintListner myPrintListner;

    static ArrayList<CalendarItem> selectedPrintItemList;

    public PrintCalendarFragment() {  //프래그먼트객체 생성과 동시에 캘린터 객체 생성.
        selectedPrintItemList = new ArrayList<>();
    }
    interface MyPrintListner{
        void onMyPrintListner(Object data);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context != null && context instanceof PrintCalendarFragment.MyPrintListner) {  //메인액티비티(context)가 널이 아니고,OnMyListener인터페이스를 임플리먼트 하고 있다면
            myPrintListner = (MyPrintListner) context;  // 1. 메인엑티비티를 onMyListener 객체로 캐스팅
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LinearLayout printCalLayout = (LinearLayout) inflater.inflate(R.layout.print_fragment_calendar, null);
        printCalendarGridView = (GridView) printCalLayout.findViewById(R.id.print_calendar_grid);
        printCalendarDayGridView = (GridView) printCalLayout.findViewById(R.id.print_calendar_days_grid);
        tvPrintCalendarTxt = (TextView) printCalLayout.findViewById(R.id.tvPrintCalendarTxt);
        ivPrintNext = (ImageButton) printCalLayout.findViewById(R.id.ivPrintMonthNext);
        ivPrintPrev = (ImageButton) printCalLayout.findViewById(R.id.ivPrintMonthPrev);
        printCalendarAdapter = new PrintCalendarAdapter(getActivity());
        printCalendarGridView.setAdapter(printCalendarAdapter);
        ArrayAdapter<String> dayTxtAdapter = new ArrayAdapter<>(getActivity(), R.layout.calendar_days_item, getResources().getStringArray(R.array.days_array));
        printCalendarDayGridView.setAdapter(dayTxtAdapter);

        setTvPrintMonth();
        tvPrintCalendarTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNextPrintMonth();
            }
        });
        ivPrintPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPrevPrintMonth();
            }
        });
        ivPrintNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNextPrintMonth();
            }
        });

        printCalendarGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView selectedDate = (TextView) view.findViewById(R.id.tvPrintItemGridView);
                ImageView selectedChecker = (ImageView) view.findViewById(R.id.ivPrintItemGridView);

                if (selectedDate.getAlpha() == 1f) {
                    if (selectedChecker.getVisibility() != View.VISIBLE) {

                        if (selectedPrintItemList.size() < 10) { //최대 인쇄 가능 일수
                            selectedChecker.setVisibility(View.VISIBLE);
                            selectedDate.setTextColor(Color.WHITE);
                            //리스트에 넣기
                            CalendarItem selectedOne = new CalendarItem(printCalendarAdapter.printalDays[position].year, printCalendarAdapter.printalDays[position].month, printCalendarAdapter.printalDays[position].day);
                            selectedPrintItemList.add(selectedOne);
                            Log.d("listtest", "add: " + selectedPrintItemList.size());
                            for (int i = 0; i < selectedPrintItemList.size(); i++) {
                                Log.d("listtest", "add: " + selectedPrintItemList.get(i).getDateString());
                            }
                        } else {
                            Toast.makeText(getContext(), "인쇄는 한번에 10일까지만 할 수 있어요.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        selectedChecker.setVisibility(View.INVISIBLE);
                        selectedDate.setTextColor(Main2Activity.myDarkGrey);
                        //리스트 빼기
                        selectedPrintItemList.remove(new CalendarItem(printCalendarAdapter.printalDays[position].year, printCalendarAdapter.printalDays[position].month, printCalendarAdapter.printalDays[position].day));
                        Log.d("listtest", "remove: " + selectedPrintItemList.size());
                    }

                }

                if (selectedPrintItemList.size()!=0){
                    myPrintListner.onMyPrintListner(true);
                }else{
                    myPrintListner.onMyPrintListner(false);
                }


            }


        });


        return printCalLayout;
    }

    void setNextPrintMonth() {
        if (printCalendarAdapter.printCal.get(Calendar.MONTH) == Calendar.DECEMBER) {
            printCalendarAdapter.printCal.set(printCalendarAdapter.printCal.get(Calendar.YEAR) + 1, Calendar.JANUARY, 1);
        } else {
            printCalendarAdapter.printCal.set(Calendar.MONTH, printCalendarAdapter.printCal.get(Calendar.MONTH) + 1);
        }
        printCalendarAdapter.updatePrintCalAdapter();
        setTvPrintMonth();

    }

    void setPrevPrintMonth() {
        if (printCalendarAdapter.printCal.get(Calendar.MONTH) == Calendar.JANUARY) {
            printCalendarAdapter.printCal.set((printCalendarAdapter.printCal.get(Calendar.YEAR)) - 1, Calendar.DECEMBER, 1);
        } else {
            printCalendarAdapter.printCal.set(Calendar.MONTH, printCalendarAdapter.printCal.get(Calendar.MONTH) - 1);
        }
        printCalendarAdapter.updatePrintCalAdapter();
        setTvPrintMonth();
    }

    void setTvPrintMonth() {
        tvPrintCalendarTxt.setText(printCalendarAdapter.printCal.get(Calendar.YEAR) + "년 " + (printCalendarAdapter.printCal.get(Calendar.MONTH) + 1) + "월");
    }

    void resetPrintCal() {
        if (selectedPrintItemList != null) {
            selectedPrintItemList = new ArrayList<>();
        }
        if (printCalendarAdapter != null) {
            printCalendarAdapter = new PrintCalendarAdapter(getActivity());
            printCalendarGridView.setAdapter(printCalendarAdapter);
        }

    }


}
