package com.example.yeodayeong.recoverydiary;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.DashPathEffect;
import android.graphics.PorterDuff;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AlertDialog;

import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.example.yeodayeong.recoverydiary.Main2Activity.myBeige;
import static com.example.yeodayeong.recoverydiary.Main2Activity.myGreen;
import static com.example.yeodayeong.recoverydiary.Main2Activity.myGrey;

public class GraphActivity extends BaseActivity {
    RelativeLayout toolbar_verMain, toolbar_verGraph;
    ImageButton ivGraphtoMainCal;
    ConstraintLayout layoutSelectGraph;  //선택하기 버튼
    TextView tvGraphYear; //지금 표시되는 년도 표시할 메인 텍스트뷰
    BarChart barChart;
    AlertDialog graphDlg;

    Calendar nowCalendar; //현재시간
    ArrayList<DailyItem> diaryList;  //전체 일기 리스트
    MinMaxDate minDate, maxDate; //첫 일기 와 오늘의 날짜정보 객체로 보관

    ArrayList<MonthDays> monthDaysList;  //기록이 있는 첫달부터 오늘까지 월별로 캘린더객체 / 날짜 / 첫날 -끝날 정보 담아 리스트
    ArrayList<BarEntry> goodMealEntry;
    ArrayList<BarEntry> vomitEntry;
    ArrayList<BarEntry> goodSnackEntry;
    ArrayList<BarEntry> goodEtcEntry;
    ArrayList<BarEntry> allEntry;
    ArrayList<String> xLabelList;  //모든 엔트리 순서대로 날짜벙보 문자로 기록(ex. 2019/8/7)
    MyValueFormatter myValueFormatter;
    MyXFormatter myXFormatter;
    int entrycount = 0; //총 엔트리 갯수 ( 첫 기록 날짜부터 오늘까지의 날 수)

    int[] monthDivideListtt;

    String[] graphDlgStrs = new String[]{"모두보기", "제거행동 횟수만 보기", "제거행동 없는 식사만 보기"};
    String[] labels;
    int[] colors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        ivGraphtoMainCal = (ImageButton) findViewById(R.id.ivGraphtoMainCal);
        layoutSelectGraph = (ConstraintLayout) findViewById(R.id.layoutSelectGraph);
        tvGraphYear = (TextView) findViewById(R.id.tvGraphYear);
        toolbar_verGraph = (RelativeLayout) toolbar.findViewById(R.id.toolbar_verGraph);
        toolbar_verMain = (RelativeLayout) toolbar.findViewById(R.id.toolbar_verMain);
        toolbar_verMain.setVisibility(View.GONE);
        toolbar_verGraph.setVisibility(View.VISIBLE);
        barChart = (BarChart) findViewById(R.id.barr);

        navigationView.setCheckedItem(R.id.drawer_graph);
        ingDrawer=R.id.drawer_graph;

        //데이터베이스 일기 전부 가져와 리스트화 ( ArrayList<DailyItem> diaryList; )
        diaryList = getDiaryList();
        if (diaryList==null || diaryList.size()==0){
            Toast.makeText(getApplicationContext(),"데이터가 없어용",Toast.LENGTH_SHORT).show();
        }else {
            //맨 처음 일기 정보 객체 & 오늘날짜 정보 객체 구하기(MinMaxDate minDate, maxDate;)
            getMinMaxObject();

            //월별로 캘린더객체 /연 /월/ 첫날(첫달만 1이 아닐 수 있음) / 끝날(현재 달만 말일이 아닐 수 있음) 클래스로 만들어 배열로 저장 =>엔트리에 들어갈 전체 날짜 정보
            monthDaysList = makeMonthDays();

            //일기 데이터 날짜 추출 => 연 월 일 비교하여 엔트리에 추가(  new barentry(191721,2)  )  날짜 / 데이터
            getEntry();

            //차트세팅 (기본:전부)
            labels = new String[]{"식사", "기타", "간식", "제거행동"};
            colors = new int[]{myGreen, myGreen, myGreen, myGrey};
            setBarChart(allEntry, labels, colors, "");
            setBarLook();
        }

        //메인으로
        ivGraphtoMainCal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GraphActivity.this, Main2Activity.class);
                startActivity(intent);
            }
        });

        final AlertDialog.Builder graphDlgBuilder = new AlertDialog.Builder(GraphActivity.this);
        graphDlgBuilder.setSingleChoiceItems(graphDlgStrs, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        labels = new String[]{"식사", "기타", "간식", "제거행동"};
                        colors = new int[]{myGreen, myGreen, myGreen, myGrey};
                        setBarChart(allEntry, labels, colors, "");
                        graphDlg.dismiss();
                        break;
                    case 1:
                        labels = new String[]{"제거행동"};
                        colors = new int[]{myGrey};
                        setBarChart(vomitEntry, labels, colors, "제거행동");
                        graphDlg.dismiss();
                        break;
                    case 2:
                       labels = new String[]{"제거행동 없는 식사"};
                        colors = new int[]{myGreen};
                        setBarChart(goodMealEntry, labels, colors, "제거행동 없는 식사");
                        graphDlg.dismiss();
                        break;
                }
            }
        });
        graphDlgBuilder.setNegativeButton("취소", null);
        graphDlg = graphDlgBuilder.create();
        //다이알로그 열어 확인할 그래프 선택
        layoutSelectGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                graphDlg.show();
            }
        });

        setVisibleRangeText();
    }


    ArrayList<DailyItem> getDiaryList() {
        MyDBHelper myDBHelper = new MyDBHelper(GraphActivity.this);
        SQLiteDatabase sqlDB = myDBHelper.getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery("select * from recoveryTBL order by dateid;", null);
        ArrayList<DailyItem> diarys = new ArrayList<>();

        while (cursor.moveToNext()) {
            DailyItem dailyItem = new DailyItem();
            dailyItem.dateid = Long.parseLong(cursor.getString(0));
            dailyItem.datetxt = cursor.getString(1);
            dailyItem.mealtype = cursor.getString(4);
            dailyItem.remove = cursor.getInt(5);
            dailyItem.diaryindex = cursor.getInt(11);
            diarys.add(dailyItem);
        }
        cursor.close();
        sqlDB.close();
        myDBHelper.close();

        return diarys;
    }

    void getMinMaxObject() {
        //맨 처음 일기 날짜 연 / 월 / 일 구하기
        minDate = new MinMaxDate();
        minDate.mmYear = Integer.parseInt(Long.toString(diaryList.get(0).dateid).substring(0, 4));
        minDate.mmMonth = Integer.parseInt(Long.toString(diaryList.get(0).dateid).substring(4, 6));
        minDate.mmDay = Integer.parseInt(Long.toString(diaryList.get(0).dateid).substring(6, 8));
        minDate.mmCal = Calendar.getInstance();
        minDate.mmCal.set(minDate.mmYear, minDate.mmMonth, minDate.mmDay);
        Log.d("monthtestt", "mincal Month: " + minDate.mmCal.get(Calendar.MONTH) + "  /monthInt" + minDate.mmMonth);

        //현재 날짜 구하기
        nowCalendar = Calendar.getInstance();
        maxDate = new MinMaxDate();
        maxDate.mmCal = nowCalendar;
        maxDate.mmYear = nowCalendar.get(Calendar.YEAR);
        maxDate.mmMonth = (nowCalendar.get(Calendar.MONTH)) + 1;
        maxDate.mmDay = nowCalendar.get(Calendar.DAY_OF_MONTH);
        Log.d("monthtestt", "maxcal: " + maxDate.mmYear + " " + maxDate.mmMonth + " " + maxDate.mmDay);  //2019 6 21
    }

    ArrayList<MonthDays> makeMonthDays() {
        ArrayList<MonthDays> monthList = new ArrayList<>();
        for (int a = minDate.mmYear; a <= maxDate.mmYear; a++) {
            if ((a == minDate.mmYear) && (a == maxDate.mmYear)) { //1.첫 기록이 속한 해인 경우이면서 동시에 마지막 기록이 속한 해라면
                for (int b = minDate.mmMonth; b <= maxDate.mmMonth; b++) {  //첫달~마지막달
                    MonthDays monthDays = new MonthDays();
                    monthDays.mdYear = a;
                    monthDays.mdMonth = b;
                    monthDays.mdCalendar = Calendar.getInstance();
                    monthDays.mdCalendar.set(Calendar.YEAR, a);
                    monthDays.mdCalendar.set(Calendar.MONTH, b - 1);
                    if (b == minDate.mmMonth && b != maxDate.mmMonth) { //첫 기록이 있지만 마지막 기록이 있는 달은 아니라면
                        monthDays.firstDay = minDate.mmDay;
                        monthDays.lastDay = monthDays.mdCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                    } else if (b != minDate.mmMonth && b == maxDate.mmMonth) {  //첫 기록은 아니고 마지막 기록만 있다면
                        monthDays.firstDay = 1;
                        monthDays.lastDay = maxDate.mmDay;
                    } else if (b == minDate.mmMonth && b == maxDate.mmMonth) {  //첫 기록과 마지막 기록이 모두 있다면
                        monthDays.firstDay = minDate.mmDay;
                        monthDays.lastDay = maxDate.mmDay;
                    } else {//첫 기록 달도 마지막 기록 달도 아니라면
                        monthDays.firstDay = 1;
                        monthDays.lastDay = monthDays.mdCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                    }
                    monthList.add(monthDays);
                    Log.d("monthtestt", "   " + monthDays.mdYear + "  " + monthDays.mdMonth + " " + monthDays.firstDay + "  " + monthDays.lastDay);
                }


            } else if ((a == minDate.mmYear) && (a != maxDate.mmYear)) { //2.첫 기록이 속한 해이지만 마지막 기록이 속한 해는 아니라면
                for (int b = minDate.mmMonth; b <= 12; b++) {
                    MonthDays monthDays = new MonthDays();
                    monthDays.mdYear = a;
                    monthDays.mdMonth = b;
                    monthDays.mdCalendar = Calendar.getInstance();
                    monthDays.mdCalendar.set(Calendar.YEAR, a);
                    monthDays.mdCalendar.set(Calendar.MONTH, b - 1);
                    if (b == minDate.mmMonth) {  //첫 기록이 있는 달이라면
                        monthDays.firstDay = minDate.mmDay;
                        monthDays.lastDay = monthDays.mdCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                    } else {  //첫기록도 마지막기록도 없음
                        monthDays.firstDay = 1;
                        monthDays.lastDay = monthDays.mdCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                    }
                    monthList.add(monthDays);
                    Log.d("monthtestt", "   " + monthDays.mdYear + "  " + monthDays.mdMonth + " " + monthDays.firstDay + "  " + monthDays.lastDay);
                }

            } else if ((a != minDate.mmYear) && (a == maxDate.mmYear)) { //3.첫 기록이 속한 해는 지났고 마지막 기록이 속한 해라면
                for (int b = 1; b <= maxDate.mmMonth; b++) {
                    MonthDays monthDays = new MonthDays();
                    monthDays.mdYear = a;
                    monthDays.mdMonth = b;
                    monthDays.mdCalendar = Calendar.getInstance();
                    monthDays.mdCalendar.set(Calendar.YEAR, a);
                    monthDays.mdCalendar.set(Calendar.MONTH, b - 1);
                    if (b == maxDate.mmMonth) {  //마지막 기록이 속한 달이라면
                        monthDays.firstDay = 1;
                        monthDays.lastDay = maxDate.mmDay;
                    } else {  //첫기록도 마지막기록도 없음
                        monthDays.firstDay = 1;
                        monthDays.lastDay = monthDays.mdCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                    }
                    monthList.add(monthDays);
                    Log.d("monthtestt", "   " + monthDays.mdYear + "  " + monthDays.mdMonth + " " + monthDays.firstDay + "  " + monthDays.lastDay);
                }

            } else {  //4.첫기록 속한 해도 마지막 기로기 속한 해도 아님(오롯이 1~12월이 1일무터 말일까지 )
                for (int b = 1; b <= 12; b++) {
                    MonthDays monthDays = new MonthDays();
                    monthDays.mdYear = a;
                    monthDays.mdMonth = b;
                    monthDays.mdCalendar = Calendar.getInstance();
                    monthDays.mdCalendar.set(Calendar.YEAR, a);
                    monthDays.mdCalendar.set(Calendar.MONTH, b - 1);
                    monthDays.firstDay = 1;
                    monthDays.lastDay = monthDays.mdCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                    monthList.add(monthDays);
                    Log.d("monthtestt", "   " + monthDays.mdYear + "  " + monthDays.mdMonth + " " + monthDays.firstDay + "  " + monthDays.lastDay);
                }
            }
        }

        return monthList;
    }

    void getEntry() {
        goodMealEntry = new ArrayList<>();
        vomitEntry = new ArrayList<>();
        goodSnackEntry = new ArrayList<>();
        goodEtcEntry = new ArrayList<>();
        allEntry = new ArrayList<>();
        xLabelList = new ArrayList<>();  //x축에 날짜 라벨링 위해 순서대로 날짜이름 배열 (7/12)

        for (int i = 0; i < monthDaysList.size(); i++) {  //기록 첫달부터 오늘까지
            for (int k = monthDaysList.get(i).firstDay; k <= monthDaysList.get(i).lastDay; k++) {  //그 달의 첫날~마지막날
                int removeNum = 0;  //k(날짜) 바뀔때마다 제거카운트 초기화
                int goodMealNum = 0;
                int goodSnackNum = 0;
                int goodEtcNum = 0;
                for (int j = 0; j < diaryList.size(); j++) {
                    int checkYear = Integer.parseInt(diaryList.get(j).datetxt.substring(0, 4));
                    int checkMonth = Integer.parseInt(diaryList.get(j).datetxt.substring(6, 8));
                    int checkDay = Integer.parseInt(diaryList.get(j).datetxt.substring(10, 12));
                    if ((checkYear == monthDaysList.get(i).mdYear) && (checkMonth == monthDaysList.get(i).mdMonth) && (checkDay == k)) {  // 연 월 일이 일치하면
                        if (diaryList.get(j).remove == 1) {  //제거행동 있는지 체크하여 카운트
                            removeNum++;
                        } else {  //제거행동 없는 경우
                            if (diaryList.get(j).mealtype.equals(getResources().getString(R.string.snack))) {  //구토 없으면서 간식
                                goodSnackNum++;
                            } else if (diaryList.get(j).mealtype.equals(getResources().getString(R.string.etc))) {  //구토 없으면서 기타
                                goodEtcNum++;
                            } else {  //구토 없으면서 식사
                                goodMealNum++;
                            }
                        }
                    }
                }
                vomitEntry.add(new BarEntry(entrycount, removeNum));
                goodMealEntry.add(new BarEntry(entrycount, goodMealNum));
                goodSnackEntry.add(new BarEntry(entrycount, goodSnackNum));
                goodEtcEntry.add(new BarEntry(entrycount, goodEtcNum));
                allEntry.add(new BarEntry(entrycount, new float[]{goodMealNum, goodSnackNum, goodEtcNum, removeNum}));
                Log.d("monthtestt", "entry:  ");
                xLabelList.add((Integer.toString(monthDaysList.get(i).mdYear)) + "/" + (Integer.toString(monthDaysList.get(i).mdMonth)) + "/" + k);
                entrycount++;
            }
        }
    }


    void setBarChart(final ArrayList<BarEntry> entryList, String[] labels, int[] colors, String graphName) {
        BarDataSet barDataSet;
        barDataSet = new BarDataSet(entryList, graphName);
        barDataSet.setDrawIcons(false);
        barDataSet.setStackLabels(labels);
        barDataSet.setColors(colors);
        barChart.clear();
        final BarData barData;
        barData = new BarData(barDataSet);
        myValueFormatter = new MyValueFormatter();
        barData.setValueFormatter(myValueFormatter);

        barChart.setData(barData);
        barChart.setScaleMinima(1f, 1f);
//        barChart.setFitBars(true);
//       barChart.setDragOffsetX();  >> 데이터 범위 앞(-) 혹은 뒤로 더 보여줄 범위(여유 범위)

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
//        xAxis.setLabelCount(entrycount/2); // 라벨을 몇개나 표시할 것인지 / true:좁으면 겹쳐짐 false:좁아도 옆으로 나열
        xAxis.setAxisMinimum(0);  //x축 시작지점
        myXFormatter = new MyXFormatter(entryList);
        xAxis.setValueFormatter(myXFormatter);

        YAxis yAxisR = barChart.getAxisRight();
        YAxis yAxisL = barChart.getAxisLeft();
        yAxisL.setDrawAxisLine(false);  //욎쪽 와이축 기준선
        yAxisL.setDrawGridLines(false); //왼쪽 와이축 라벨마다 그려지는 가로선
//        yAxisL.setDrawZeroLine(true);
        yAxisL.setDrawLabels(false);  //왼쪽 와이축 숫자
        yAxisL.setGranularity(1f);
        yAxisL.setAxisMaximum(4f);  //y축 끝 지점
        yAxisL.setAxisMinimum(0f); //y축 시작지점
        yAxisR.setDrawLabels(false);
        yAxisR.setDrawAxisLine(false);
        yAxisR.setDrawGridLines(false);

        barChart.setOnChartGestureListener(new MyChartListner());


        //월 나누는 세로선 그리기
        ArrayList<Integer> dividerNum = new ArrayList<>();  //매월 1일에 해당하는 엔트리 번호 배열화
        ArrayList<String> dividerTag = new ArrayList<>();
        for (int i = 0; i < xLabelList.size(); i++) {
            Log.d("rangeTest", "valueList: " + i + " " + xLabelList.get(i));
            if (xLabelList.get(i).split("/")[2].equals("1")) {
                dividerNum.add(i);
                dividerTag.add(xLabelList.get(i).split("/")[1] + "월");
            }
        }

        ArrayList<LimitLine> monthLines = new ArrayList<>();
        for (int i = 0; i < dividerNum.size(); i++) {
            LimitLine lml = new LimitLine(dividerNum.get(i), dividerTag.get(i));
            lml.setLineWidth(0.2f);
            monthLines.add(lml);
        }
        for (int k = 0; k < monthLines.size(); k++) {
            xAxis.addLimitLine(monthLines.get(k));
        }
        xAxis.setDrawLimitLinesBehindData(true);
    }


    void setBarLook(){
        barChart.setScaleYEnabled(false);
        barChart.setVisibleXRange(30f, 0f); //0번째부터 30번째까지 범위 설정(moveTo와 함께 이용하여 디폴트 범위만 세팅)
        barChart.moveViewToX(entrycount); //맨 오른쪽으로 (기록 끝까지 ) 뷰 이동
        barChart.setVisibleXRangeMaximum(365f);  //실제로 조절하여 볼 수 있는 최대 범위
        barChart.setVisibleXRangeMinimum(7f);  //조절하여 볼 수 있는 최소 범위
    }


    private class MinMaxDate {
        Calendar mmCal;
        int mmMonth, mmYear, mmDay;
    }

    private class MonthDays {
        Calendar mdCalendar;
        int mdYear, mdMonth;
        //        int[] mdDays;
        int firstDay;
        int lastDay;
    }

    class MyValueFormatter extends IndexAxisValueFormatter {  //막대 위에 표시되는 글자
        @Override
        public String getFormattedValue(float value) {
            if ((int) value != 0) {
                return Integer.toString((int) value) + "회";
            } else {
                return super.getFormattedValue(value);
            }
        }
    }

    class MyXFormatter extends IndexAxisValueFormatter {
        ArrayList<BarEntry> entryList;

        public MyXFormatter(ArrayList<BarEntry> entryList) {
            this.entryList = entryList;
        }

        @Override
        public String getFormattedValue(float value) {
            return xLabelList.get((int)value).split("/")[2];

           /* if (entryList.get((int) value).getY() == 0) {
                if (xLabelList.get((int) value).split("/")[2].equals("1")) {
                    return xLabelList.get((int) value).substring(5);
                } else
                    return "";
            } else if ((xLabelList.get((int) value).split("/")[2].equals("1"))) {
                return xLabelList.get((int) value).substring(5);
            } else
                return xLabelList.get((int) value).split("/")[2];*/
        }

    }

    void setVisibleRangeText(){
        float lowest = barChart.getLowestVisibleX();
        float highest=barChart.getHighestVisibleX();
        String lowestStr=xLabelList.get((int) lowest).split("/")[0]+ "년 "+xLabelList.get((int) lowest).split("/")[1]+"월";
        String highestStr=xLabelList.get((int) highest).split("/")[0]+ "년 "+xLabelList.get((int) highest).split("/")[1]+"월";
        tvGraphYear.setText(lowestStr+"~"+highestStr);
    }


    class MyChartListner implements OnChartGestureListener{

        @Override
        public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        }
        @Override
        public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
            setVisibleRangeText();
        }

        @Override
        public void onChartLongPressed(MotionEvent me) {
        }
        @Override
        public void onChartDoubleTapped(MotionEvent me) {
        }
        @Override
        public void onChartSingleTapped(MotionEvent me) {
        }
        @Override
        public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
        }
        @Override
        public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        }
        @Override
        public void onChartTranslate(MotionEvent me, float dX, float dY) {
        }
    }


}