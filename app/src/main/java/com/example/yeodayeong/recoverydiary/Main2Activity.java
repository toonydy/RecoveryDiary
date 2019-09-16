package com.example.yeodayeong.recoverydiary;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Main2Activity extends BaseActivity implements CalendarFragment.OnMyListener {
    //tmp    TextView tvDateDetail, tvMealNum, tvSnackNum, tvRemoveNum;
//    LinearLayout layoutDetailAll,layoutDetailNone,layoutDetailContent;
    TextView tvMonthText, tvYearText;
    LinearLayout layoutCalendar, layoutDetail, layoutCalFrag;
    ImageButton ivRefrexhCal;
    //    FloatingActionButton fab_main_Write;
    //레이아웃 애니메이션
    int mOriginalHeight;
    static int targetSize;
    boolean initialSizeObtained = false;
    boolean isShrink = false;
    //디바이스 사이즈
    Display display;
    static int width;
    int height;
    String dateStr;
    static boolean collaped = false;    //펼쳐진상태면 선택 날짜 체크되지 않도록
    Calendar calendarInit;
    ImageView btnAddMain;


    String pickedDateStr;
    static SimpleDateFormat pickedFormat = new SimpleDateFormat("yyyy년 MM월 dd일 ");

    String writed = "";

    final static int DAILY_REQUEST = 31;
    final static int WRITE_REQUEST = 32;
    final static int WRITE_FINISH = 33;
    final static String RESET = "RESET";

    static boolean backsw = false;

    FragmentManager fragmentManager;
    CalendarItem calendarItemSelected;
    CalendarItem calendarItemCurrentMonth;
    CalendarFragment calendarFragment;

    //데이터베이스
    MyDBHelper myDBHelper;
    SQLiteDatabase sqlDB;
    Cursor cursor;
    static int myGreen, myGrey, myLightMetal, myBeige, myBrown, myDarkGrey, myOrange;  //색 미리 준비
    static String breakfast, lunch, dinner, snack, etc;

    //디테일 리사이클러
    RecyclerView detailRecycler;
    MyDetailAdatper myDetailAdatper;

    //기기사이즈정보
    static int displayX, displayY;

    GestureDetector swipeDetectorDetail;


//tmp    ImageButton ivTmpDayPrev, ivTmpDayNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        myGreen = getResources().getColor(R.color.myGreen);
        myBeige = getResources().getColor(R.color.myLightBaige);
        myBrown = getResources().getColor(R.color.myBrown);
        myGrey = getResources().getColor(R.color.myGrey);
        myDarkGrey = getResources().getColor(R.color.myDarkGrey);
        myLightMetal = getResources().getColor(R.color.myLightMetal);
        myOrange = getResources().getColor(R.color.myOrange);
        breakfast = getResources().getString(R.string.breakfast);
        lunch = getResources().getString(R.string.lunch);
        dinner = getResources().getString(R.string.dinner);
        snack = getResources().getString(R.string.snack);
        etc = getResources().getString(R.string.etc);

        navigationView.setCheckedItem(R.id.drawer_main);
        ingDrawer = R.id.drawer_main;

//        btnAddMain=(ImageView)findViewById(R.id.btnAddMain);
        detailRecycler = (RecyclerView) findViewById(R.id.detailRecycler);

//        layoutDetailAll = (LinearLayout) findViewById(R.id.layoutDetailAll);
        ivRefrexhCal = (ImageButton) findViewById(R.id.ivRefreshCal);
//        fab_main_Write = (FloatingActionButton) findViewById(R.id.flb_main_Write);
//        tvDateDetail = (TextView) findViewById(R.id.tvDateDetail);
        layoutCalendar = (LinearLayout) findViewById(R.id.layoutCalendar);
        layoutDetail = (LinearLayout) findViewById(R.id.layoutDetail);
//        layoutDetailContent = (LinearLayout) findViewById(R.id.layoutDetailContent);
//        layoutDetailNone = (LinearLayout) findViewById(R.id.layoutDetailNone);
//        tvMealNum = (TextView) findViewById(R.id.tvMealNum);
//        tvSnackNum = (TextView) findViewById(R.id.tvSnackNum);
//        tvRemoveNum = (TextView) findViewById(R.id.tvRemoveNum);
        ivRefrexhCal.setAlpha(0f);
        tvMonthText = (TextView) toolbar.findViewById(R.id.tvMonthText);
        tvYearText = (TextView) toolbar.findViewById(R.id.tvYearText);
//        layoutCalFrag=(LinearLayout)findViewById(R.id.layoutCalFrag);

//        ivTmpDayNext = (ImageButton) findViewById(R.id.ivTmpDayNext);
//        ivTmpDayPrev = (ImageButton) findViewById(R.id.ivTmpDayPrev);

        myDBHelper = new MyDBHelper(this);

        fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.commit();

        calendarFragment = (CalendarFragment) fragmentManager.findFragmentById(R.id.fragment);


        // 디바이스 사이즈
        display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;
        Log.i("device", "width:" + width + "  height:" + height);
//        LinearLayout.LayoutParams titleParams = (LinearLayout.LayoutParams) layoutTitle.getLayoutParams();
        ViewGroup.LayoutParams barParam = toolbar.getLayoutParams();
        int barSize = barParam.height;
        barParam.height = (int) (height / 2);   //타이틀 레이아웃 펼쳐진 길이 설정
        targetSize = barSize; //줄어든 길이 설정
        LinearLayout.LayoutParams calParams = (LinearLayout.LayoutParams) layoutCalendar.getLayoutParams();

        calParams.height = height - (barParam.height);
//        layoutDetail.setVisibility(View.INVISIBLE);


        //layoutTitle 길이구하기
        toolbar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (initialSizeObtained)
                    return;
                initialSizeObtained = true;
                mOriginalHeight = toolbar.getMeasuredHeight();
            }
        });
        expandAnimation.setDuration(300);
        collapseAnimation.setDuration(300);

        //디테일 타이틀 오늘 날짜로 초기화
        calendarInit = Calendar.getInstance();
        pickedDateStr = pickedFormat.format(calendarInit.getTime());
        int dayofWeek = calendarInit.get(Calendar.DAY_OF_WEEK);
        pickedDateStr += WriteActivity.getDayofWeek(dayofWeek);
/*  tmp      tvDateDetail.setText(pickedDateStr);
        tvDateDetail.setText(pickedFormat.format(calendarInit.getTime()));*/

        //툴바 연월표시
        tvMonthText.setText(MonthSetting.setMonthText(calendarInit.get(Calendar.MONTH)));
        tvYearText.setText(Integer.toString(calendarInit.get(Calendar.YEAR)) + "." + (Integer.toString((calendarInit.get(Calendar.MONTH)) + 1)));

        //다음달 버튼
        ivNextMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarFragment.onNextMonth();
            }
        });
        //이전 달 버튼
        ivPrevMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarFragment.onPreviousMonth();
            }
        });


/* tmp       //디테일레이아웃 눌러 데일리액티비티 인텐트
        layoutDetailContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent dailyIntent = new Intent(getApplicationContext(), DailyActivity.class);
                dailyIntent.putExtra("pickedDateStr", calendarItemSelected.getDateString());
                startActivity(dailyIntent);
            }
        });*/
        ivRefrexhCal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarItemSelected = new CalendarItem(calendarInit);
                calendarFragment.onSelectedMonth(calendarItemSelected.year, calendarItemSelected.month);
                calendarFragment.updateCurrentMonth();
                expandTopBar(toolbar);

            }
        });


        detailRecycler.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return swipeDetectorDetail.onTouchEvent(event);
            }
        });

        if (calendarItemSelected == null)
            calendarItemSelected = new CalendarItem(Calendar.getInstance());
//        setDetailData();
    }

    //레이아웃 애니메이션 정의 클래스
    Animation collapseAnimation = new Animation() {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            ViewGroup.LayoutParams params = toolbar.getLayoutParams();
            params.height = (int) (mOriginalHeight + (((targetSize) - (mOriginalHeight)) * interpolatedTime));
            toolbar.setLayoutParams(params);
            tvYearText.setAlpha((float) (-interpolatedTime));
            ivRefrexhCal.setAlpha((float) (interpolatedTime));

            //툴바 접으면서 디테일레이아웃 해당 시간동안 딜레이시킨 후 길이 재고 세팅
            Handler delayHandler = new Handler();
            delayHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setDetailData();
                }
            }, (long) interpolatedTime);

//            ivAdd.setAlpha((float) (interpolatedTime));

//            fab_main_Write.startAnimation();

        }
    };
    Animation expandAnimation = new Animation() {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            ViewGroup.LayoutParams params = toolbar.getLayoutParams();
            params.height = (int) ((targetSize) + (((mOriginalHeight) - (targetSize)) * interpolatedTime));
            toolbar.setLayoutParams(params);
            tvYearText.setAlpha((float) (interpolatedTime));
            ivRefrexhCal.setAlpha((float) (-interpolatedTime));
//            ivAdd.setAlpha((float) (-interpolatedTime));

        }
    };


    //레이아웃 애니메이션 메소드
    void toggleTopBar(View view) {  //열었다 닫았다
        isShrink = !isShrink;
        toolbar.clearAnimation();
        toolbar.startAnimation(isShrink ? collapseAnimation : expandAnimation);
    }

    void collapseTopBar(View view) {  //접혀있다면 펴지 않고 접힌 상태 유지 , 펼쳐져있으면 접기 (다른 날짜 눌러 디테일 확인할 때)
        if (!isShrink) {
            isShrink = true;
            toolbar.clearAnimation();
            toolbar.startAnimation(collapseAnimation);
            collaped = true;
//            layoutDetail.setVisibility(View.VISIBLE);
        } else {
            return;
        }
    }

    void expandTopBar(View view) {  //펼쳐져있다면 접지 않고 펼쳐진 상태 유지 , 접혀있다면 펴기
        if (isShrink) {
            isShrink = false;
            toolbar.clearAnimation();
            toolbar.startAnimation(expandAnimation);
            collaped = false;
//            layoutDetail.setVisibility(View.INVISIBLE);
            /*calendarItemSelected=new CalendarItem(calendarInit);
            Log.d("testSlt","testSelect: "+calendarItemSelected.getDateString());
            calendarFragment.updateCurrentMonth();
            calendarFragment.calendarAdapter.setSelected(calendarItemSelected.year,calendarItemSelected.month,calendarItemSelected.day);*/
        } else {
            return;
        }
    }


    //디테일표시메소드

    void setDetailData() {

        if (toolbar.getLayoutParams().height != targetSize) {

            //툴바 접히는동안 기다렸다가 디테일 레이아웃 세팅해야 온전한 크기로 설정됨
            Handler delayHandler = new Handler();
            delayHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    detailRecycler.setLayoutManager(new LinearLayoutManager(Main2Activity.this) {
                        @Override
                        public boolean canScrollVertically() {
                            return false;
                        }
                    });
                    myDetailAdatper = new MyDetailAdatper();
                    detailRecycler.setAdapter(myDetailAdatper);
                }
            }, (300));
        } else {
            detailRecycler.setLayoutManager(new LinearLayoutManager(Main2Activity.this) {
                @Override
                public boolean canScrollVertically() {
                    return false;
                }
            });
            myDetailAdatper = new MyDetailAdatper();
            detailRecycler.setAdapter(myDetailAdatper);
        }


/* tmp       if (calendarItemSelected != null) {
            pickedDateStr = calendarItemSelected.getDateString();
            tvDateDetail.setText(pickedDateStr);
            Log.d("ddd", "dateStrtt: " + pickedDateStr);

            //디테일레이아웃에 데이터 표시
            sqlDB = myDBHelper.getReadableDatabase();
            cursor = sqlDB.rawQuery("select * from recoveryTBL where datetxt = '" + pickedDateStr + "';", null);

            int mealNum = 0;
            int snackNum = 0;
            int removeNum = 0;
            while ((cursor.moveToNext())) {
                String type = cursor.getString(4);
                Log.d("ddd", "ddd" + type);
                if (type.equals(breakfast)) {
                    mealNum++;
                } else if (type.equals(lunch)) {
                    mealNum++;
                } else if (type.equals(dinner)) {
                    mealNum++;
                } else if (type.equals(snack)) {
                    snackNum++;
                }
                int remove = cursor.getInt(5);
                switch (remove) {
                    case 0:
                        break;
                    case 1:
                        removeNum++;
                        break;
                }
            }
            tvMealNum.setText(Integer.toString(mealNum));
            tvSnackNum.setText(Integer.toString(snackNum));
            tvRemoveNum.setText(Integer.toString(removeNum));
            sqlDB.close();
            cursor.close();

            if (mealNum == 0 && snackNum == 0 && removeNum == 0) {
//            layoutDetailContent.setAlpha(0.5f);
                layoutDetailContent.setVisibility(View.GONE);
                layoutDetailNone.setVisibility(View.VISIBLE);
            } else {
                layoutDetailNone.setVisibility(View.GONE);
                layoutDetailContent.setVisibility(View.VISIBLE);
            }

        }*/


    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("lifecycle", "lifecycle: onActivityResult() ");
    }


    @Override
    public void onReceivedSelectedData(Object data1) {  //날짜 선택할때마다 수행할 작업들
        calendarItemSelected = (CalendarItem) data1;
        collapseTopBar(toolbar);
        setDetailData();
/*
        if (((CalendarItem) data1).getDateString().equals(calendarFragment.calendarAdapter.today.getDateString())) {
            ivTmpDayNext.setClickable(false);
            ivTmpDayNext.setAlpha(0.2f);
        } else {
            ivTmpDayNext.setClickable(true);
            ivTmpDayNext.setAlpha(1f);
        }*/
    }

    @Override
    public void onReceivedCurrentData(Object data2) {   //달 바뀔때마다 수행할 작업들
        calendarItemCurrentMonth = (CalendarItem) data2;
        if (tvMonthText != null) {
            tvMonthText.setText(MonthSetting.setMonthText(calendarItemCurrentMonth.month));
            tvYearText.setText(Integer.toString(calendarItemCurrentMonth.year) + "." + (Integer.toString((calendarItemCurrentMonth.month) + 1)));
        }
    }

    @Override
    public void onAddBtnClickListner() {
        Intent writeIntent = new Intent(getApplicationContext(), WriteActivity.class);
        writeIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        if (calendarItemSelected == null) {
            calendarItemSelected = new CalendarItem(calendarInit);
            collaped = false;
            calendarFragment.calendarAdapter.setSelected(calendarItemSelected.year, calendarItemSelected.month, calendarItemSelected.day);
        }
        writeIntent.putExtra("pickedDateStr", calendarItemSelected.getDateString());
        writeIntent.putExtra("tag", "NEW");
        startActivity(writeIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("lifecycle", "lifecycle: onResume() ");

        Intent toMainIntent = getIntent();
        String selectedDate = toMainIntent.getStringExtra("selectedDate");

        if (selectedDate != null) {
            if (selectedDate.equals(RESET)) {  //날짜값 대신 "RESET" 이 전달 올 경우
                Log.d("lifecycle", "lifecycle: onResume() >>" + selectedDate);
                ivRefrexhCal.callOnClick();
            } else {
                Log.d("lifecycle", "lifecycle: onResume() >>" + selectedDate);
                String tmpY = selectedDate.substring(0, 4);
                String tmpM = selectedDate.substring(6, 8);
                String tmpD = selectedDate.substring(10, 12);
                calendarItemSelected.year = Integer.parseInt(tmpY);
                calendarItemSelected.month = (Integer.parseInt(tmpM) - 1);
                calendarItemSelected.day = Integer.parseInt(tmpD);
                calendarFragment.onSelectedMonth(calendarItemSelected.year, calendarItemSelected.month);
                calendarFragment.updateCurrentMonth();
                calendarFragment.calendarAdapter.setSelected(calendarItemSelected);
                calendarFragment.calendarAdapter.setRecorededList();
                calendarFragment.calendarAdapter.notifyDataSetChanged();
                setDetailData();
            }

        } else {
            Log.d("lifecycle", "lifecycle: onResume() >>null");
            calendarFragment.updateCurrentMonth();
            calendarFragment.calendarAdapter.setSelected(calendarItemSelected);
            calendarFragment.calendarAdapter.setRecorededList();
            calendarFragment.calendarAdapter.notifyDataSetChanged();
            setDetailData();
        }


    }


    class MyDetailAdatper extends RecyclerView.Adapter<MyDetailAdatper.MyDetailHolder> {
        Context context;
        LayoutInflater layoutInflater;
        View view;

        public MyDetailAdatper() {
        }

        @NonNull
        @Override
        public MyDetailHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            context = viewGroup.getContext();
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.timeline_recycler, viewGroup, false);
            MyDetailHolder detailHolder = new MyDetailHolder(view);


            return detailHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull final MyDetailHolder myDetailHolder, int i) {
            myDetailHolder.txtChal = (TextView) view.findViewById(R.id.txtChal);
            myDetailHolder.detailNotePper = (ImageView) view.findViewById(R.id.detailNotePaper);
            myDetailHolder.btnDetailPrev = (ImageView) view.findViewById(R.id.btnDetailPrevDay);
            myDetailHolder.btnDetailNext = (ImageView) view.findViewById(R.id.btnDetailNextDay);
            myDetailHolder.tvShowDaily = (TextView) view.findViewById(R.id.tvShowDaily);
            myDetailHolder.tvDetailDate = (TextView) view.findViewById(R.id.tvDetailDate);
            myDetailHolder.mealLabelBox = (LinearLayout) view.findViewById(R.id.mealLabelBox);
            myDetailHolder.snackLabelBox = (LinearLayout) view.findViewById(R.id.snackLabelBox);
            myDetailHolder.challangeLabelBox = (LinearLayout) view.findViewById(R.id.challangeLabelBox);
            //디테일 배경 노트이미지 크기 세팅
            ViewGroup.LayoutParams noteParams = myDetailHolder.detailNotePper.getLayoutParams();
            noteParams.width = width;
            //선택된 날짜가 오늘이면 다음날 이동 불가
            if (calendarItemSelected.getDateString().equals(calendarFragment.calendarAdapter.today.getDateString())) {
                myDetailHolder.btnDetailNext.setClickable(false);
                myDetailHolder.btnDetailNext.setAlpha(0.2f);
            } else {
                myDetailHolder.btnDetailNext.setClickable(true);
                myDetailHolder.btnDetailNext.setAlpha(1f);
            }
            //버튼 리스너
            myDetailHolder.btnDetailPrev.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    calendarFragment.moveToPrevDay();
                }
            });
            myDetailHolder.btnDetailNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    calendarFragment.moveToNextDay();
                }
            });
            //디테일 레이아웃 스와이프 리스너
            swipeDetectorDetail = new GestureDetector(Main2Activity.this, new SwipeGesture(Main2Activity.this) {
                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    if (e1.getX() - e2.getX() > swipeMinDistance && Math.abs(velocityX) > swipeThresholdVelocity) {
                        if (myDetailHolder.btnDetailNext.isClickable())
                            myDetailHolder.btnDetailNext.callOnClick();
                    } else if (e2.getX() - e1.getX() > swipeMinDistance && Math.abs(velocityX) > swipeThresholdVelocity) {
                        if (myDetailHolder.btnDetailPrev.isClickable())
                            myDetailHolder.btnDetailPrev.callOnClick();
                    }
                    return false;
                }
            });
            //선택날짜 텍스트표시
            pickedDateStr = calendarItemSelected.getDateString();
            myDetailHolder.tvDetailDate.setText(pickedDateStr);
            //자세히 보기 눌러 디테일 레이아웃 인센트
            myDetailHolder.tvShowDaily.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent dailyIntent = new Intent(getApplicationContext(), DailyActivity.class);
                    dailyIntent.putExtra("pickedDateStr", calendarItemSelected.getDateString());
                    startActivity(dailyIntent);
                }
            });

            //데이터 세팅
            myDetailHolder.mealLabelBox.removeAllViews();
            myDetailHolder.snackLabelBox.removeAllViews();
            myDetailHolder.challangeLabelBox.removeAllViews();
            int mealcount = 0, snackcount = 0, chalcount = 0;

            myDBHelper = new MyDBHelper(Main2Activity.this);
            sqlDB = myDBHelper.getReadableDatabase();
            cursor = sqlDB.rawQuery("select * from recoveryTBL where datetxt = '" + pickedDateStr + "' order by dateid;", null);
            while (cursor.moveToNext()) {
                String mealtypelabel = null;
                mealtypelabel = cursor.getString(4);
                TextView mealLabelView = new TextView(getApplicationContext());
                mealLabelView.setText(mealtypelabel);
                mealLabelView.setTextColor(Color.WHITE);
                LinearLayout.LayoutParams marginParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                marginParams.setMargins(0, 0, 10, 0);
                if (cursor.getInt(5) >= 1) {
                    mealLabelView.setBackgroundResource(R.drawable.redrabelbox);
                } else {
                    mealLabelView.setBackgroundResource(R.drawable.greenrabelbox);
                }
                if (mealtypelabel.equals(getResources().getString(R.string.breakfast)) || mealtypelabel.equals(getResources().getString(R.string.lunch)) || mealtypelabel.equals(getResources().getString(R.string.dinner))) {
                    myDetailHolder.mealLabelBox.addView(mealLabelView, marginParams);
                    mealcount++;
                } else if (mealtypelabel.equals(getResources().getString(R.string.snack)) || mealtypelabel.equals(getResources().getString(R.string.etc))) {
                    myDetailHolder.snackLabelBox.addView(mealLabelView, marginParams);
                    snackcount++;
                }
            }
            //기록 없는 란 표시


            cursor.close();
            //진행중 챌린지 표시
            cursor = sqlDB.rawQuery("select * from challangeTBL order by challangeid;", null);
            while (cursor.moveToNext()) {
                Log.d("txttxttxt", "(calendarItemSelected.getDateNum(): " + (calendarItemSelected.getDateNum()));
                Log.d("txttxttxt", "(long)(cursor.getInt(1): " + (long) (cursor.getInt(1)));
                Log.d("txttxttxt", "(long)(cursor.getInt(2): " + (long) (cursor.getInt(2)));
                if (((calendarItemSelected.getDateNum() + 100) >= (long) (cursor.getInt(1)) && ((calendarItemSelected.getDateNum() + 100) <= (long) (cursor.getInt(2))))) {
                    /*String chalName=cursor.getString(5);
                    TextView chalStrView=new TextView(getApplicationContext());
                    chalStrView.setText(chalName);
                    chalStrView.setBackgroundResource(R.drawable.textboxrounded);
                    chalStrView.setSingleLine(true);
                    chalStrView.setEllipsize(TextUtils.TruncateAt.END);
                    LinearLayout.LayoutParams marginParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                    marginParams.setMargins(0,0,10,0);
                    myDetailHolder.challangeLabelBox.addView(chalStrView);
                    chalcount++;*/
                    chalcount++;
                }
            }
            cursor.close();
            sqlDB.close();
            myDBHelper.close();

            if (mealcount <= 0) {
                TextView nullNotice = new TextView(getApplicationContext());
                nullNotice.setText("해당하는 기록이 없습니다.");
                nullNotice.setTextColor(myLightMetal);
                nullNotice.setTextSize(8);
                myDetailHolder.mealLabelBox.addView(nullNotice);
            }
            if (snackcount <= 0) {
                TextView nullNotice = new TextView(getApplicationContext());
                nullNotice.setText("해당하는 기록이 없습니다.");
                nullNotice.setTextColor(myLightMetal);
                nullNotice.setTextSize(8);
                myDetailHolder.snackLabelBox.addView(nullNotice);
            }
            if (chalcount <= 0) {
                TextView nullNotice = new TextView(getApplicationContext());
                nullNotice.setText("해당하는 기록이 없습니다.");
                nullNotice.setTextColor(myLightMetal);
                nullNotice.setTextSize(8);
                myDetailHolder.challangeLabelBox.addView(nullNotice);
            } else {
                TextView chalNotice = new TextView(getApplicationContext());
                chalNotice.setText("진행 중인 도전 ");
                myDetailHolder.challangeLabelBox.addView(chalNotice);
                TextView chalNumNotice = new TextView(getApplicationContext());
                chalNumNotice.setText(chalcount + "개");
                chalNumNotice.setTextColor(Color.WHITE);
                chalNumNotice.setBackgroundResource(R.drawable.greylabelbox);
                myDetailHolder.challangeLabelBox.addView(chalNumNotice);
                ImageButton btnShowChallDetail = new ImageButton(getApplicationContext());
                btnShowChallDetail.setImageResource(R.drawable.ic_right);
                btnShowChallDetail.setBackgroundColor(00000000);
                myDetailHolder.challangeLabelBox.addView(btnShowChallDetail);
                ViewGroup.LayoutParams btnparams = btnShowChallDetail.getLayoutParams();
                btnparams.height =  btnparams.height/2;

                btnShowChallDetail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent chalIntent = new Intent(Main2Activity.this, ChallangeActivity.class);
                        startActivity(chalIntent);
                    }
                });

            }


        }

        @Override
        public int getItemCount() {
            return 1;
        }

        class MyDetailHolder extends RecyclerView.ViewHolder {
            ImageView detailNotePper, btnDetailPrev, btnDetailNext;
            TextView tvShowDaily, tvDetailDate, txtChal;
            LinearLayout mealLabelBox, snackLabelBox, challangeLabelBox;

            public MyDetailHolder(@NonNull View itemView) {
                super(itemView);
            }
        }

    }

}

