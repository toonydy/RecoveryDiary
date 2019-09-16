package com.example.yeodayeong.recoverydiary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static com.example.yeodayeong.recoverydiary.Main2Activity.DAILY_REQUEST;
import static com.example.yeodayeong.recoverydiary.Main2Activity.RESET;
import static com.example.yeodayeong.recoverydiary.Main2Activity.WRITE_FINISH;
import static com.example.yeodayeong.recoverydiary.Main2Activity.WRITE_REQUEST;

public class DailyActivity extends BaseActivity {

    RelativeLayout toolbar_verDaily, toolbar_verMain;
    LinearLayout layoutDaily;
    RelativeLayout layoutDailyTitle;
    ImageButton ivNextDay, ivPrevDay, ivMainCal;
    String pickedDatestr;
    String nextDayStr, prevDayStr;
    TextView tvDailyDate;
    FloatingActionButton flbWrite;

    MyDBHelper myDBHelper;
    SQLiteDatabase sqlDB;
    Cursor cursor;

    ArrayList<DailyItem> dailyItems;


    DailyAdapter dailyAdapter;
    RecyclerView recyclerView_daily;
    LinearLayoutManager layoutManager;

    static final int DAILY_TO_WRITE = 20;
    static final int DAILY_TO_EDT = 50;
    int pos;

    ImageButton ibTest;
    Intent inIntent;

    long todayLong;
    long pickedDatelong;
    String firstRecordDate;

    //데일리-작석으로 가서 작성 마치고 다시 데일리 열 때 아래에 데일리 액티비티 살아있으면 없애주기 1.
    public static DailyActivity dailyActivity=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily);

        //데일리-작석으로 가서 작성 마치고 다시 데일리 열 때 아래에 데일리 액티비티 살아있으면 없애주기 2.
        dailyActivity=this;

        layoutDaily = (LinearLayout) findViewById(R.id.layoutDaily);
        flbWrite = (FloatingActionButton) findViewById(R.id.flb_Write);
        ivMainCal = (ImageButton) findViewById(R.id.ivDailytoMainCal);
        tvDailyDate = (TextView) findViewById(R.id.tvDailyDate);
        recyclerView_daily = (RecyclerView) findViewById(R.id.recyclerView_daily);
        layoutManager = new LinearLayoutManager(this);
        recyclerView_daily.setLayoutManager(layoutManager);
        ivNextDay = (ImageButton) findViewById(R.id.ivNextDay);
        ivPrevDay = (ImageButton) findViewById(R.id.ivPrevDay);
        toolbar_verDaily = (RelativeLayout) toolbar.findViewById(R.id.toolbar_verDaily);
        toolbar_verMain = (RelativeLayout) toolbar.findViewById(R.id.toolbar_verMain);

        toolbar_verMain.setVisibility(View.GONE);
        toolbar_verDaily.setVisibility(View.VISIBLE);


        inIntent = getIntent();
        pickedDatestr = inIntent.getStringExtra("pickedDateStr");


        checkDateFirstLast();

        tvDailyDate.setText(pickedDatestr);
        setData();

        flbWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent writeIntent = new Intent(getApplicationContext(), WriteActivity.class);
                writeIntent.putExtra("pickedDateStr", tvDailyDate.getText().toString());
                writeIntent.putExtra("tag", "NEW");
//                setResult(RESULT_OK);
//                startActivityForResult(writeIntent,DAILY_TO_WRITE);
                startActivity(writeIntent);


            }
        });


        ivMainCal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(getApplicationContext(), Main2Activity.class);
//                mainIntent.putExtra("pickedDateStr",tvDailyDate.getText().toString());
                mainIntent.putExtra("selectedDate", RESET);
                setResult(RESULT_OK, mainIntent);
                startActivity(mainIntent);
//                finish();
            }
        });


        ivPrevDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPrevDay();
            }
        });
        ivNextDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNextDay();
            }
        });

        final GestureDetector swipeDetector = new GestureDetector(DailyActivity.this, new SwipeGesture(DailyActivity.this) {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1.getX() - e2.getX() > swipeMinDistance && Math.abs(velocityX) > swipeThresholdVelocity) {
                    if (ivNextDay.isClickable())
                        ivNextDay.callOnClick();
                } else if (e2.getX() - e1.getX() > swipeMinDistance && Math.abs(velocityX) > swipeThresholdVelocity) {
                    if (ivPrevDay.isClickable())
                        ivPrevDay.callOnClick();
                }
                return false;
            }
        });

        recyclerView_daily.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return swipeDetector.onTouchEvent(event);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (recyclerView_daily.getAdapter() == null) {
            recyclerView_daily.setAdapter(dailyAdapter);
        } else {
            recyclerView_daily.setAdapter(dailyAdapter);
            dailyAdapter.notifyDataSetChanged();
            recyclerView_daily.invalidate();
//            Toast.makeText(getApplicationContext(),"datareset",Toast.LENGTH_SHORT).show();
        }

        Intent toDailyIntent = getIntent();
        String selectedDate = toDailyIntent.getStringExtra("selectedDate");
        if (selectedDate != null) {
            pickedDatestr = selectedDate;
            tvDailyDate.setText(pickedDatestr);
            setData();
        }
    }


    void setNextDay() {

        int todayDateTmp = Integer.parseInt(pickedDatestr.substring(10, 12));
        int todayMonthTmp = Integer.parseInt(pickedDatestr.substring(6, 8));
        int todayYearTmp = Integer.parseInt(pickedDatestr.substring(0, 4));
        Calendar todayCalTmp = Calendar.getInstance();
        todayCalTmp.set(todayYearTmp, todayMonthTmp - 1, todayDateTmp);
        Calendar nextCalTmp = todayCalTmp;
        nextCalTmp.add(Calendar.DATE, 1);
        nextDayStr = Main2Activity.pickedFormat.format(nextCalTmp.getTime());
        int dayofWeek = nextCalTmp.get(Calendar.DAY_OF_WEEK);
        nextDayStr += WriteActivity.getDayofWeek(dayofWeek);
        pickedDatestr = nextDayStr;
        tvDailyDate.setText(pickedDatestr);

        setData();
        checkDateFirstLast();
        Log.i("pick", "pickedDatestr" + pickedDatestr);
    }

    void setPrevDay() {
        int todayDateTmp = Integer.parseInt(pickedDatestr.substring(10, 12));
        int todayMonthTmp = Integer.parseInt(pickedDatestr.substring(6, 8));
        int todayYearTmp = Integer.parseInt(pickedDatestr.substring(0, 4));
        Calendar todayCalTmp = Calendar.getInstance();
        todayCalTmp.set(todayYearTmp, todayMonthTmp - 1, todayDateTmp);
        Calendar prevCalTmp = todayCalTmp;
        prevCalTmp.add(Calendar.DATE, -1);
        prevDayStr = Main2Activity.pickedFormat.format(prevCalTmp.getTime());
        int dayofWeek = prevCalTmp.get(Calendar.DAY_OF_WEEK);
        prevDayStr += WriteActivity.getDayofWeek(dayofWeek);
        pickedDatestr = prevDayStr;
        tvDailyDate.setText(pickedDatestr);

        setData();
        checkDateFirstLast();
        Log.i("pick", "pickedDatestr" + pickedDatestr);
    }


    void checkDateFirstLast() {
        todayLong = Long.parseLong(new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime())); //오늘날짜
        pickedDatelong = Long.parseLong(pickedDatestr.substring(0, 4) + pickedDatestr.substring(6, 8) + pickedDatestr.substring(10, 12)); //지금 페이지 표시 날짜
        myDBHelper = new MyDBHelper(this);
        sqlDB = myDBHelper.getReadableDatabase();
        cursor = sqlDB.rawQuery("select datetxt from recoveryTBL order by dateid;", null);
        cursor.moveToFirst();
        firstRecordDate = cursor.getString(0).trim();
        if (pickedDatelong >= todayLong) {
            ivNextDay.setClickable(false);
            ivNextDay.setAlpha(0.2f);
        } else {
            ivNextDay.setClickable(true);
            ivNextDay.setAlpha(1f);
        }
        if (pickedDatestr.equals(firstRecordDate)) {
            ivPrevDay.setAlpha(0.2f);
            ivPrevDay.setClickable(false);
        } else {
            ivPrevDay.setAlpha(1f);
            ivPrevDay.setClickable(true);
        }

        Log.d("logwhy", "pickedDatestr:" + pickedDatestr + "   firstRecordDate:" + firstRecordDate);


        cursor.close();
        sqlDB.close();
        myDBHelper.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            /*if (requestCode==DAILY_REQUEST){
                pickedDatestr=data.getStringExtra("pickedDateStr");
                tvDailyDate.setText(pickedDatestr);
                setData();
            }*/
            /*if (requestCode==DAILY_TO_WRITE ||requestCode==WRITE_REQUEST){  //데일리 > 라이트 가서 작성한 경우 작성창에서 최종 작성한 날짜 데일리로 들어옴 & 데이터 업데이트
                pickedDatestr=data.getStringExtra("pickedDateStr");
                tvDailyDate.setText(pickedDatestr);
                setData();
            }else if (requestCode==DAILY_TO_EDT){
//                Toast.makeText(getApplicationContext(),"edit",Toast.LENGTH_SHORT).show();
                pickedDatestr=data.getStringExtra("pickedDateStr");
                tvDailyDate.setText(pickedDatestr);
                setData();
            }*/
        }
    }


    void setData() {
        myDBHelper = new MyDBHelper(this);
        sqlDB = myDBHelper.getReadableDatabase();
        cursor = sqlDB.rawQuery("select * from recoveryTBL where datetxt = '" + pickedDatestr + "' order by dateid;", null);
        dailyItems = new ArrayList<DailyItem>();
        while ((cursor.moveToNext())) {
            DailyItem dailyItem = new DailyItem();
            dailyItem.dateid = Long.parseLong(cursor.getString(0));
            dailyItem.datetxt = cursor.getString(1);
            dailyItem.time = cursor.getString(2);
            dailyItem.menu = cursor.getString(3);
            dailyItem.removetxt = cursor.getString(6);
            dailyItem.location = cursor.getString(7);
            dailyItem.people = cursor.getString(8);
            dailyItem.think = cursor.getString(9);
            dailyItem.remove = cursor.getInt(5);
            dailyItem.menuPhoto = cursor.getBlob(10);
            dailyItem.diaryindex = cursor.getInt(11);
            dailyItem.mealtype = cursor.getString(4);
            dailyItems.add(dailyItem);
        }
        //어댑터 세팅 or 새로고침
        dailyAdapter = new DailyAdapter(dailyItems, this);
        if (recyclerView_daily.getAdapter() == null) {
            recyclerView_daily.setAdapter(dailyAdapter);
        } else {
            recyclerView_daily.setAdapter(dailyAdapter);
            dailyAdapter.notifyDataSetChanged();
            recyclerView_daily.invalidate();
        }
        cursor.close();
        sqlDB.close();
        myDBHelper.close();
    }


    //데일리 리사이클러 어댑터

    class DailyAdapter extends RecyclerView.Adapter<DailyAdapter.DailyViewHolder> {

        ArrayList<DailyItem> dailyItems;
        //디비
        MyDBHelper myDBHelper;
        SQLiteDatabase sqlDB;
        Cursor cursor;
        View view;

        Context context;

        public DailyAdapter(ArrayList<DailyItem> dailyItems, Context context) {
            this.dailyItems = dailyItems;
            this.context = context;
        }

        @NonNull
        @Override
        public DailyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.daily_item_cardview, viewGroup, false);
            DailyViewHolder dailyViewHolder = new DailyViewHolder(view);
            return dailyViewHolder;
        }

        @Override
        public void onBindViewHolder(final DailyViewHolder dailyViewHolder, int i) {

            dailyViewHolder.tvDailyTime.setText(dailyItems.get(i).time);
            dailyViewHolder.tvDailyMenu.setText(dailyItems.get(i).menu);
            dailyViewHolder.tvDailyLocation.setText(dailyItems.get(i).location);
            dailyViewHolder.tvDailyPeople.setText(dailyItems.get(i).people);
            dailyViewHolder.tvDailyThink.setText(dailyItems.get(i).think);
            dailyViewHolder.tvDailyMealType.setText(dailyItems.get(i).mealtype);
            switch (dailyItems.get(i).remove) {
                case 0:
                    dailyViewHolder.ivRemoveChecked.setVisibility(View.INVISIBLE);
                    dailyViewHolder.tvDailyRemoveTxt.setVisibility(View.GONE);
                    break;
                case 1:
                    dailyViewHolder.ivRemoveChecked.setVisibility(View.VISIBLE);
                    dailyViewHolder.tvDailyRemoveTxt.setVisibility(View.VISIBLE);
                    dailyViewHolder.tvDailyRemoveTxt.setText(dailyItems.get(i).removetxt);
                    break;
                default:
                    dailyViewHolder.ivRemoveChecked.setVisibility(View.INVISIBLE);
                    dailyViewHolder.tvDailyRemoveTxt.setVisibility(View.GONE);
            }
            if (dailyItems.get(i).menuPhoto != null) {
                Bitmap imageBitmap = BitmapFactory.decodeByteArray(dailyItems.get(i).menuPhoto, 0, (dailyItems.get(i).menuPhoto).length);
                dailyViewHolder.ivDailyPhoto.setImageBitmap(imageBitmap);
                int imageHeight = imageBitmap.getHeight();
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) dailyViewHolder.layoutDailyPhoto.getLayoutParams();
                params.height = imageHeight;
                dailyViewHolder.ivDailyPhoto.setLayoutParams(params);
            }

            dailyViewHolder.layoutDailyItemOpenClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (dailyViewHolder.layoutMore.getVisibility() == View.GONE) {
                        dailyViewHolder.layoutMore.setVisibility(View.VISIBLE);
                        dailyViewHolder.ivCardOpen.setImageResource(R.drawable.ic_arrow_drop_up_black_24dp);
                    } else {
                        dailyViewHolder.layoutMore.setVisibility(View.GONE);
                        dailyViewHolder.ivCardOpen.setImageResource(R.drawable.ic_arrow_drop_down_black_24dp);
                    }
                }
            });

            dailyViewHolder.ivDailyItemEdit.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    pos = dailyViewHolder.getAdapterPosition();  // 내부에 생긴 리스너에서는 데일리 목록 중 클릭한 아이템 인덱스 모르므로 새로 받아옴!
                    myDBHelper = new MyDBHelper(context);
                    sqlDB = myDBHelper.getWritableDatabase();
                    PopupMenu popupMenu = new PopupMenu(context, v);
                    MenuInflater menuInflater = new MenuInflater(context);
                    menuInflater.inflate(R.menu.edit_menu, popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.itemEdit:
                                    Intent editIntent = new Intent(getApplicationContext(), WriteActivity.class);
                                    editIntent.putExtra("editDateStr", tvDailyDate.getText().toString());
                                    editIntent.putExtra("editTimeStr", dailyViewHolder.tvDailyTime.getText().toString());
                                    editIntent.putExtra("editIndex", dailyItems.get(pos).diaryindex);
                                    editIntent.putExtra("tag", "EDIT");
                                    startActivityForResult(editIntent, DAILY_TO_EDT);
                                    break;
                                case R.id.itemDel:
                                    final String dateTxtDel = dailyItems.get(pos).datetxt;
                                    final String timeTxtDel = dailyItems.get(pos).time;
                                    final int delId = dailyItems.get(pos).diaryindex;
                                    String date = dateTxtDel + " " + timeTxtDel;
                                    AlertDialog.Builder dlg = new AlertDialog.Builder(context);
                                    dlg.setTitle(date);
                                    dlg.setMessage("삭제하시겠습니까?");
                                    dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            sqlDB.execSQL("delete from recoveryTBL where diaryindex=" + delId + ";");
                                            sqlDB.close();
                                            myDBHelper.close();
//                                            notifyDataSetChanged();
                                            setData();  //데일리페이지 어댑터 새로고침
                                            Toast.makeText(context, "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    dlg.setNegativeButton("취소", null);
                                    dlg.show();
                                    break;
                            }
                            return false;
                        }
                    });
                    popupMenu.show();
                    return false;
                }
            });


        }

        @Override
        public int getItemCount() {
            return dailyItems.size();
        }

        class DailyViewHolder extends RecyclerView.ViewHolder {

            //            CardView cardView;
            LinearLayout layoutMore, layoutDailyPhoto, layoutDailyItemOpenClose;
            TextView tvDailyTime, tvDailyMenu, tvDailyLocation, tvDailyPeople, tvDailyThink, tvDailyRemoveTxt, tvDailyMealType;
            ImageView ivRemoveChecked, ivDailyPhoto, ivCardOpen;
            ImageButton ivDailyItemEdit;

            public DailyViewHolder(@NonNull View itemView) {
                super(itemView);
//                cardView=(CardView)itemView.findViewById(R.id.cardView);
                layoutDailyItemOpenClose = (LinearLayout) itemView.findViewById(R.id.layoutDailyItemOpenClose);
                layoutMore = (LinearLayout) itemView.findViewById(R.id.layoutMore);
                tvDailyTime = (TextView) itemView.findViewById(R.id.tvDailyTime);
                tvDailyMenu = (TextView) itemView.findViewById(R.id.tvDailyMenu);
                tvDailyLocation = (TextView) itemView.findViewById(R.id.tvDailyLocation);
                tvDailyPeople = (TextView) itemView.findViewById(R.id.tvDailyPeople);
                tvDailyThink = (TextView) itemView.findViewById(R.id.tvDailyThink);
                ivRemoveChecked = (ImageView) itemView.findViewById(R.id.ivRemoveChecked);
                ivDailyPhoto = (ImageView) itemView.findViewById(R.id.ivDailyPhoto);
                ivCardOpen = (ImageView) itemView.findViewById(R.id.ivCardOpen);
                ivDailyItemEdit = (ImageButton) itemView.findViewById(R.id.ivDailyItemEdit);
                layoutDailyPhoto = (LinearLayout) itemView.findViewById(R.id.layoutDailyPhoto);
                tvDailyRemoveTxt = (TextView) itemView.findViewById(R.id.tvDailyRemoveTxt);
                tvDailyMealType = (TextView) itemView.findViewById(R.id.tvDailyMealType);


            }

        }
    }


}
