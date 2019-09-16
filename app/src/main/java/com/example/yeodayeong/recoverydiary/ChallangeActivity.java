package com.example.yeodayeong.recoverydiary;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.LongDef;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.GlideContext;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static com.example.yeodayeong.recoverydiary.Main2Activity.RESET;
import static com.example.yeodayeong.recoverydiary.Main2Activity.myGreen;
import static com.example.yeodayeong.recoverydiary.Main2Activity.myGrey;
import static com.example.yeodayeong.recoverydiary.Main2Activity.myOrange;


public class ChallangeActivity extends BaseActivity {
    LinearLayout layoutChallangeAll;
    RelativeLayout toolbar_verChallange, toolbar_verMain; //상속받은 툴바도구
    TextView tvCMstatusTitle, tvCMrecyclerNone;
    ImageButton arrowCMleft, arrowCMright, ivMainCal;
    FloatingActionButton flb_addChallange;
    Calendar calendar; //오늘날짜 객체
    SimpleDateFormat dateFormatToday = new SimpleDateFormat("yyyyMMdd");
    Long todayLong;//오늘 날짜 Long(20190728)
    //데이터베이스
    MyDBHelper myDBHelper;
    SQLiteDatabase sqlDB;
    Cursor cursor;
    //
    RecyclerView recyclerViewCM;
    ChallangeAdapter nowAdapter;
    int nowStatus;
    ArrayList<ChallangeItem> nowList;

    final int ING = 11, WAIT = 22, DONE = 33;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challange);
        flb_addChallange = (FloatingActionButton) findViewById(R.id.flb_AddChallange);
        toolbar_verMain = (RelativeLayout) toolbar.findViewById(R.id.toolbar_verMain);
        toolbar_verChallange = (RelativeLayout) toolbar.findViewById(R.id.toolbar_verChallange);
        ivMainCal = (ImageButton) toolbar_verChallange.findViewById(R.id.ivChallangetoMainCal);
        recyclerViewCM = (RecyclerView) findViewById(R.id.recyclerViewCM);
        arrowCMleft = (ImageButton) findViewById(R.id.arrowCMleft);
        arrowCMright = (ImageButton) findViewById(R.id.arrowCMRight);
        tvCMrecyclerNone = (TextView) findViewById(R.id.tvCMRecyclerNone);
        tvCMstatusTitle = (TextView) findViewById(R.id.tvCMStatusTitle);
        layoutChallangeAll=(LinearLayout)findViewById(R.id.layoutChallange);

        nowStatus = ING; //맨 처음 창 열면 진행중인 챌린지 리스트 보여주기

        //툴바세팅
        toolbar_verMain.setVisibility(View.GONE);
        toolbar_verChallange.setVisibility(View.VISIBLE);
        ivMainCal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(getApplicationContext(), Main2Activity.class);
                mainIntent.putExtra("selectedDate", RESET);
                setResult(RESULT_OK, mainIntent);
                startActivity(mainIntent);
            }
        });
        //네비게이션드로어 세팅
        navigationView.setCheckedItem(R.id.drawer_challange);
        ingDrawer = R.id.drawer_challange;
        //오늘날짜 세팅
        calendar = Calendar.getInstance(); //오늘날짜 객체
        todayLong = Long.parseLong(dateFormatToday.format(calendar.getTime()));

        arrowCMleft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nowStatus == ING)
                    nowStatus = DONE;
                else if (nowStatus == DONE)
                    nowStatus = WAIT;
                else if (nowStatus == WAIT)
                    nowStatus = ING;

                resetPage(nowStatus);
            }
        });
        arrowCMright.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nowStatus == ING)
                    nowStatus = WAIT;
                else if (nowStatus == WAIT)
                    nowStatus = DONE;
                else if (nowStatus == DONE)
                    nowStatus = ING;

                resetPage(nowStatus);

            }
        });

        //작성버튼 세팅
        flb_addChallange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chalWIntent = new Intent(getApplicationContext(), ChallangeWriteActivity.class);
                chalWIntent.putExtra("challangeWriteMode", "new");
                startActivity(chalWIntent);
            }
        });

        nowList = getChallangeItenList(nowStatus);  // 현재 선택되어있는 상태(ING / WAIT / DONE) 에 해당하는 리스트 받아오기
        nowAdapter = new ChallangeAdapter(nowList, nowStatus); //현재 만들어진 데이터리스트 적용해 어댑터 생성;
        setChalAdapter(recyclerViewCM, nowAdapter, getApplicationContext());
//        setTxts();

        //스와이프
        final GestureDetector swipeDetector = new GestureDetector(ChallangeActivity.this, new SwipeGesture(ChallangeActivity.this) {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1.getX() - e2.getX() > swipeMinDistance && Math.abs(velocityX) > swipeThresholdVelocity) {
                    arrowCMright.callOnClick();
                } else if (e2.getX() - e1.getX() > swipeMinDistance && Math.abs(velocityX) > swipeThresholdVelocity) {
                    arrowCMleft.callOnClick();
                }
                return false;
            }
        });
        layoutChallangeAll.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return swipeDetector.onTouchEvent(event);
            }
        });
        recyclerViewCM.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return swipeDetector.onTouchEvent(event);
            }
        });





    }


    public ArrayList<ChallangeItem> getChallangeItenList(int TYPE) {
        myDBHelper = new MyDBHelper(ChallangeActivity.this);
        sqlDB = myDBHelper.getReadableDatabase();
        String queryStr = "";
        if (TYPE == ING) {
            queryStr = "select * from challangeTBL where challangeid <= '" + todayLong + "' and challangeendid >= '" + todayLong + "' order by challangeid;";
        } else if (TYPE == WAIT) {
            queryStr = "select * from challangeTBL where challangeid > '" + todayLong + "' order by challangeid;";
        } else {  //TYPE==DONE
            queryStr = "select * from challangeTBL where challangeendid < '" + todayLong + "' order by challangeid;";
        }
        cursor = sqlDB.rawQuery(queryStr, null);
        ArrayList<ChallangeItem> challangeList = new ArrayList<ChallangeItem>();

        while ((cursor.moveToNext())) {
            ChallangeItem challangeItem = new ChallangeItem();
            challangeItem.challangeIndex = cursor.getInt(0);
            challangeItem.startDateLong = cursor.getLong(1);
            challangeItem.endDateLong = cursor.getLong(2);
            challangeItem.dateStr = cursor.getString(3);
            challangeItem.rangeInt = cursor.getInt(4);
            challangeItem.targetStr = cursor.getString(5);
            challangeItem.rewardStr = cursor.getString(6);
            challangeItem.targetType = cursor.getString(7);
            challangeItem.targetNum = cursor.getInt(8);
            challangeItem.targetUpDown = cursor.getString(9);
            challangeItem.resultNum = cursor.getString(10);
            challangeList.add(challangeItem);
        }
        cursor.close();
        sqlDB.close();
        myDBHelper.close();
        return challangeList;
    }

    class ChallangeAdapter extends RecyclerView.Adapter<ChallangeAdapter.ChallangeViewHolder> {

        ArrayList<ChallangeItem> challangeList;
        int TYPE;
        View view;

        public ChallangeAdapter(ArrayList<ChallangeItem> challangeList, int TYPE) {
            this.challangeList = challangeList;
            this.TYPE = TYPE;
        }

        void refreshDataList(ArrayList<ChallangeItem> challangeList) {
            this.challangeList = challangeList;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ChallangeAdapter.ChallangeViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            context = viewGroup.getContext();
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.challange_content, viewGroup, false);
            ChallangeViewHolder challangeViewHolder = new ChallangeViewHolder(view);
            return challangeViewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull final ChallangeAdapter.ChallangeViewHolder challangeViewHolder, int i) {
            challangeViewHolder.tv_chalitem_date.setText(challangeList.get(i).dateStr);
//            challangeViewHolder.tv_chalitem_range.setText(challangeList.get(i).rangeInt + "일");
            challangeViewHolder.tv_chalitem_target.setText(challangeList.get(i).targetStr);
            challangeViewHolder.tv_chalitem_reward.setText(challangeList.get(i).rewardStr);

            if (TYPE != WAIT) {

                if (challangeList.get(i).targetType.equals(getResources().getString(R.string.etc))) {

                } else {  //진행중이거나 ㅅ끝난 도전의 경우
                    //디테일 들어가기 전 챌린지 리스트 전부 (기타 챌린지 제외) 달성퍼센테이지 다시 확인하고(새 퍼센테이지 저장 x)
                    float percentTmp = 80f;
                    if (challangeList.get(i).targetUpDown.equals("UP")) {
                        percentTmp = (((float) (ChallangeDetailActivity.checkResultNum(challangeList.get(i), todayLong, getApplicationContext()).size()) / (float) challangeList.get(i).targetNum) * 100);
                    } else if (challangeList.get(i).targetUpDown.equals("DOWN")) {
                        percentTmp = 100 - (((float) (ChallangeDetailActivity.checkResultNum(challangeList.get(i), todayLong, getApplicationContext()).size()) / (float) challangeList.get(i).targetNum) * 100);
                        if (percentTmp < 0)
                            percentTmp = 0;
                    }
                    String percentStr = String.format("%.1f", percentTmp);
                    challangeViewHolder.ivChallangeUpdate.setColorFilter(myGrey);  //기본 회색 표시
                    if (Float.parseFloat(percentStr) >=100f ){
                        challangeViewHolder.ivChallangeUpdate.setColorFilter(myGreen);  //업뎃 상관없이 새로 체크해서 100 이상인경우 초록색
                    }
                    if (Float.parseFloat(percentStr) != Float.parseFloat(challangeList.get(i).resultNum)) {
                        ////새로 검사한 달성 퍼센테이지가 마지막으로 디테일창에서 확인한 퍼센테이지와 다르며,!! 기존에는 100아래였다가 100를 달성하게 변경된 경우!!!
                        if (((Float.parseFloat(percentStr)) >= 100f) && ((Float.parseFloat(challangeList.get(i).resultNum)) < 100f)) {
                            challangeViewHolder.ivChallangeUpdate.setColorFilter(myOrange);
                        }
                    }

                }





            }


        }


        @Override
        public int getItemCount() {
            return challangeList.size();
        }

        class ChallangeViewHolder extends RecyclerView.ViewHolder {
//            TextView tv_chalitem_range,tvChallangeStatus;
            TextView tv_chalitem_date, tv_chalitem_target,  tv_chalitem_reward;
            LinearLayout layoutChallangeItemBox;
            ImageView ivChallangeUpdate;

            public ChallangeViewHolder(@NonNull final View itemView) {
                super(itemView);
                tv_chalitem_date = (TextView) itemView.findViewById(R.id.tv_chalitem_date);
                tv_chalitem_reward = (TextView) itemView.findViewById(R.id.tv_chalitem_reward);
//                tv_chalitem_range = (TextView) itemView.findViewById(R.id.tv_chalitem_range);
                tv_chalitem_target = (TextView) itemView.findViewById(R.id.tv_chalitem_target);
//                tvChallangeStatus = (TextView) itemView.findViewById(R.id.tvChallangeStatus);
                layoutChallangeItemBox = (LinearLayout) itemView.findViewById(R.id.layoutChallangeItemBox);
                ivChallangeUpdate = (ImageView) itemView.findViewById(R.id.ivChallangeUpdate);

                layoutChallangeItemBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        int pos = getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            ChallangeItem selectedItem = challangeList.get(pos);
                            Intent detailIntent = new Intent(getApplicationContext(), ChallangeDetailActivity.class);
                            detailIntent.putExtra("challangeItemId", selectedItem.challangeIndex);
                            String challangeStatus = "";
                            if (nowStatus == ING)
                                challangeStatus = "ing";
                            else if (nowStatus == DONE)
                                challangeStatus = "done";
                            else if (nowStatus == WAIT)
                                challangeStatus = "wait";
                            else
                                challangeStatus = "error";
                            detailIntent.putExtra("challangeStatus", challangeStatus);
                            startActivity(detailIntent);
                        }


                    }
                });


            }

        }

    }

    void setChalAdapter(RecyclerView recyclerView, ChallangeAdapter challangeAdapter, Context context) {
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        if (recyclerView.getAdapter() == null)
            recyclerView.setAdapter(challangeAdapter);
        else {
//                recyclerView.setAdapter(challangeAdapter);
            challangeAdapter.notifyDataSetChanged();
            recyclerView.invalidate();
        }
        setTxts();
    }

    boolean checkUpdateResult(ChallangeItem challangeItem) {
        //성공여부 다시 검사
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetPage(nowStatus);
//        nowAdapter.notifyDataSetChanged();
//        pageRefresh();
//        Toast.makeText(getApplicationContext(),"RESUME",Toast.LENGTH_LONG).show();
    }

    void resetPage(int nowStatus) {
        nowList = getChallangeItenList(nowStatus);  // 현재 선택되어있는 상태(ING / WAIT / DONE) 에 해당하는 리스트 받아오기
        if (nowAdapter != null) {
            nowAdapter.refreshDataList(nowList);
            setTxts();
        } else {
            nowAdapter = new ChallangeAdapter(nowList, nowStatus); //현재 만들어진 데이터리스트 적용해 어댑터 생성;
            setChalAdapter(recyclerViewCM, nowAdapter, getApplicationContext());
        }
//        setTxts();
    }

    void setTxts() {
        if (nowStatus == ING) {
            tvCMstatusTitle.setText("진행중인 도전");
            tvCMrecyclerNone.setText("진행중인 도전이 없습니다");
        } else if (nowStatus == WAIT) {
            tvCMstatusTitle.setText("예약된 도전");
            tvCMrecyclerNone.setText("예약된 도전이 없습니다");
        } else if (nowStatus == DONE) {
            tvCMstatusTitle.setText("완료된 도전");
            tvCMrecyclerNone.setText("완료된 도전이 없습니다");
        }
        if (recyclerViewCM.getAdapter().getItemCount() > 0) {
            tvCMrecyclerNone.setVisibility(View.GONE);
            recyclerViewCM.setVisibility(View.VISIBLE);
        } else {
            tvCMrecyclerNone.setVisibility(View.VISIBLE);
            recyclerViewCM.setVisibility(View.GONE);
        }
    }


}




