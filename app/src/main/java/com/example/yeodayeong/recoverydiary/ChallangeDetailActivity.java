package com.example.yeodayeong.recoverydiary;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.example.yeodayeong.recoverydiary.Main2Activity.myDarkGrey;
import static com.example.yeodayeong.recoverydiary.Main2Activity.myGreen;
import static com.example.yeodayeong.recoverydiary.Main2Activity.myGrey;
import static com.example.yeodayeong.recoverydiary.Main2Activity.myOrange;

public class ChallangeDetailActivity extends AppCompatActivity {

    //툴바
    TextView tvChallangeDetailTitle;
    ImageButton ivChalDetailClose, ivChalDetailEdit;
    //위젯
    LinearLayout layoutChalDetailTitle, layoutDayImage;
    GridLayout layoutStepGrid;
    TextView tvCDdate, tvCDtext, tvCDpercent, tvCDStatusTitle, tvCDreward, tvCDexplain;
    RecyclerView recyclerCDexplain;
    // 사용할 변수
    String challangeStatus; //현재 챌린지아이탬 상태 ("ing" , "wait" , "done" , "error")
    boolean etcTF = false;
    String[] etcTFlist; //기타 챌린지일 경우 1 or 0 으로 켜진날 안켜진날 배열저장
    int challangeItenId; //현재 챌린지아이탬 아이디 (인덱스)
    ChallangeItem selectedChallangeItem;  //주인공 챌린지아이쳄
    Calendar calendar; //오늘날짜 객체
    SimpleDateFormat dateFormatToday = new SimpleDateFormat("yyyyMMdd");
    Long todayLong;//오늘 날짜 Long(20190728)

    //'제거행동없는날'챌린지를 체크할 때만 만들어 사용할 리스트. 도전 기간 중 제거행동 없는 날만 캘린더 객체 목록으로 보관
    ArrayList<Calendar> finalNoVomitCalendarList;  //'제거행동없는날' 의 경우만 사용 : 제거행동없는 날짜
    ArrayList<Calendar> finalNoDiaryDayinChalDate; //'제거행동없는날' 의 경우만 사용 : 도전기간 내 일기 전혀 없는 날짜

    //데이터베이스
    MyDBHelper myDBHelper;
    SQLiteDatabase sqlDB;
    Cursor cursor;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challange_detail);
        //위젯 캐스팅
        layoutChalDetailTitle = (LinearLayout) findViewById(R.id.layoutChalDetailTitle);
        tvChallangeDetailTitle = findViewById(R.id.tvChalDetailTitle);
        ivChalDetailClose = (ImageButton) findViewById(R.id.ivChalDetailClose);
        ivChalDetailEdit = (ImageButton) findViewById(R.id.ivChalDetailEdit);
        layoutStepGrid = (GridLayout) findViewById(R.id.layoutStepGrid);
        layoutDayImage = (LinearLayout) findViewById(R.id.layoutDayImage);
        tvCDdate = (TextView) findViewById(R.id.tvCDdate);
        tvCDtext = (TextView) findViewById(R.id.tvCDtext);
        tvCDpercent = (TextView) findViewById(R.id.tvCDpercent);
        tvCDStatusTitle = (TextView) findViewById(R.id.tvCDstatusTitle);
        tvCDreward = (TextView) findViewById(R.id.tvCDreward);
        tvCDexplain = (TextView) findViewById(R.id.tvCDexplain);
        recyclerCDexplain = (RecyclerView) findViewById(R.id.recyclerCDexplain);
        //인텐트 받기
        final Intent inIntent = getIntent();
        challangeItenId = inIntent.getIntExtra("challangeItemId", 0);
        challangeStatus = inIntent.getStringExtra("challangeStatus");  // "ing" , "wait" , "done" , "error" 중 하나로 진행상태 받음
        myDBHelper = new MyDBHelper(this);
        sqlDB = myDBHelper.getReadableDatabase();
        cursor = sqlDB.rawQuery("select * from challangeTBL where challangeIndex = " + challangeItenId + ";", null);
        cursor.moveToFirst();
        //선택받은 챌린지 아이템 객체화
        selectedChallangeItem = new ChallangeItem();
        selectedChallangeItem.challangeIndex = cursor.getInt(0);
        selectedChallangeItem.startDateLong = (long) (cursor.getInt(1));
        selectedChallangeItem.endDateLong = (long) (cursor.getInt(2));
        selectedChallangeItem.dateStr = cursor.getString(3);
        selectedChallangeItem.rangeInt = cursor.getInt(4);
        selectedChallangeItem.targetStr = cursor.getString(5);
        selectedChallangeItem.rewardStr = cursor.getString(6);
        selectedChallangeItem.targetType = cursor.getString(7);
        selectedChallangeItem.targetNum = cursor.getInt(8);
        selectedChallangeItem.targetUpDown = cursor.getString(9);
        selectedChallangeItem.resultNum = cursor.getString(10);

        cursor.close();
        sqlDB.close();
        myDBHelper.close();
        if (selectedChallangeItem.targetType.equals(getResources().getString(R.string.etc)))
            etcTF = true; //'기타' 여부 체크

        //상태에 따라 레이아웃 결정
        if (challangeStatus.equals("wait")) {  //대기중인 챌린지는 진행상황 탭 다 가리고 안내멘트
            tvCDStatusTitle.setText("진행 대기");
            tvCDexplain.setVisibility(View.VISIBLE);
            tvCDexplain.setText("아직 시작되지 않았습니다.");
            tvCDpercent.setVisibility(View.GONE);
            recyclerCDexplain.setVisibility(View.GONE);
        } else {  //시작되거나 끝난 챌린지는
            tvCDpercent.setVisibility(View.VISIBLE);

            if (etcTF) {
                tvCDexplain.setText("도전 결과를 직접 체크해주세요.");
                tvCDexplain.setVisibility(View.VISIBLE);
                recyclerCDexplain.setVisibility(View.GONE);
            } else {
                if (challangeStatus.equals("ing"))
                    tvCDStatusTitle.setText("진행 상황");
                else if (challangeStatus.equals("done"))
                    tvCDStatusTitle.setText("진행 결과");
                tvCDexplain.setVisibility(View.GONE);
                recyclerCDexplain.setVisibility(View.VISIBLE);
            }
        }


        //오늘날짜 설정
        calendar = Calendar.getInstance(); //오늘날짜 객체
        todayLong = Long.parseLong(dateFormatToday.format(calendar.getTime()));
        //타이틀바 길이 설정
        layoutChalDetailTitle = (LinearLayout) findViewById(R.id.layoutChalDetailTitle);
        LinearLayout.LayoutParams titleParams = (LinearLayout.LayoutParams) layoutChalDetailTitle.getLayoutParams();
        titleParams.height = Main2Activity.targetSize;
        //타이틀바 타이틀
        tvChallangeDetailTitle.setText(challangeStatus.toUpperCase());
        //타이틀바 메뉴 리스너
        ivChalDetailClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ivChalDetailEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(getApplicationContext(), v);
                MenuInflater menuInflater = new MenuInflater(getApplicationContext());
                menuInflater.inflate(R.menu.edit_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.itemEdit:
                                if (challangeStatus.equals("wait")) {
                                    Intent editIntent = new Intent(getApplicationContext(), ChallangeWriteActivity.class);
                                    editIntent.putExtra("challangeWriteMode", "EDIT");
                                    editIntent.putExtra("editId", selectedChallangeItem.challangeIndex);
                                    startActivity(editIntent);
                                } else
                                    Toast.makeText(getApplicationContext(), "시작되지 않은 도전 내용만 수정할 수 있습니다.", Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.itemDel:
                                final android.app.AlertDialog.Builder dlg = new android.app.AlertDialog.Builder(ChallangeDetailActivity.this);
                                dlg.setTitle(selectedChallangeItem.targetStr);
                                dlg.setMessage("삭제하시겠습니까?");
                                dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        myDBHelper = new MyDBHelper(getApplicationContext());
                                        sqlDB = myDBHelper.getWritableDatabase();
                                        sqlDB.execSQL("delete from challangeTBL where challangeindex=" + selectedChallangeItem.challangeIndex + ";");
                                        sqlDB.close();
                                        myDBHelper.close();
                                        finish();
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
            }
        });

        setPage();

    }

    void setPage() {
        //기본사항 공통표시
        tvChallangeDetailTitle.setText(selectedChallangeItem.targetStr);
        tvCDtext.setText(selectedChallangeItem.targetStr);
        tvCDreward.setText(selectedChallangeItem.rewardStr);
        tvCDdate.setText(selectedChallangeItem.dateStr);
        setDayImage();  //날짜진행상황 공통적으로 표시

        String updown = "";
        if (etcTF) {  //챌ㄹ린지 타입이 기타인 경우

            int pastDaysEtc=0;
            if (challangeStatus.equals("ing")) {  //진행중인 챌린지라면
                pastDaysEtc = (int) (todayLong - selectedChallangeItem.startDateLong); //오늘이 도전2일째면 2
            }else if (challangeStatus.equals("done")){
                pastDaysEtc=selectedChallangeItem.rangeInt-1;
            }else if (challangeStatus.equals("wait")){
                pastDaysEtc=-1;
            }

            final EtcMembers etcMembers = setSelfCheckLayout();

//                selectedChallangeItem.resultNum 분해해서 전에 성공체크했던 날 표시하기
            etcTFlist = selectedChallangeItem.resultNum.split(",");  //(스트링배열로  [1] [1] [0] [1] [0]  처럼 되어있음)
            int etcOnNum = 0;
            for (int j = 0; j < etcTFlist.length; j++) {
                if (etcTFlist[j].equals("1")) {
                    etcMembers.imageViewList.get(j).setColorFilter(myGreen);
                    etcMembers.imageViewList.get(j).setTag("ON");
//                        etcMembers.linearLayoutList.get(j).setAlpha(1f);
                    etcOnNum++;  //전체 이미지뷰 돌면서 켜져있는것 갯수 세기
                } else {
                    etcMembers.imageViewList.get(j).setColorFilter(myGrey);
                    etcMembers.imageViewList.get(j).setTag("OFF");
                }
            }
            setEtcPercentage(etcOnNum);//클릭 전 처음 세팅하고 퍼센트 표시

            for (int i = 0; i <= pastDaysEtc; i++) { //이미 지난 날짜만큼 클릭리스너 달기 + 지난 날짜는 linearLayoutList 진하
                etcMembers.linearLayoutList.get(i).setAlpha(1f);
                etcMembers.imageViewList.get(i).setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        ImageView selectedImage = (ImageView) v;
                        if (selectedImage.getTag().toString().equals("OFF")) {  //활성화되어있는것중 체크 꺼져있는것 롱클릭할 경우 켜짐
                            selectedImage.setColorFilter(myGreen);
                            selectedImage.setTag("ON");
//                                Toast.makeText(getApplicationContext(),Integer.toString(etcMembers.imageViewList.indexOf(v)),Toast.LENGTH_SHORT).show();
//                                selectedImage.setAlpha(1f);
                        } else if (selectedImage.getTag().toString().equals("ON")) { //켜져있는것 롱클릭할 경우 꺼짐
                            selectedImage.setColorFilter(myGrey);
                            selectedImage.setTag("OFF");
//                                selectedImage.setAlpha(0.3f);
                        }

                        //클릭할 때 마다 퍼센테이지 업데이트
                        int etcOnNum = 0;
                        for (int k = 0; k < etcMembers.imageViewList.size(); k++) {
                            if (etcMembers.imageViewList.get(k).getTag().toString().equals("ON")) {
                                etcOnNum++;  //전체 이미지뷰 돌면서 켜져있는것 갯수 세기
                                etcTFlist[k] = "1"; //리스트 수정
                            } else {
                                etcTFlist[k] = "0";
                            }
                        }
                        setEtcPercentage(etcOnNum);
                        updateEtcTFList();

                        return false;
                    }
                });
            }

        } else {  //'기타 ' 챌린지가 아닌 경우

            //진행상황별 나눠서 설정
            if (challangeStatus.equals("wait")) {
                setPercentage(0);
                setStepImage(new ArrayList<String>());

            } else if (challangeStatus.equals("ing") || challangeStatus.equals("done")) {
                setPercentage(checkResultNum(selectedChallangeItem,todayLong,getApplicationContext()).size());
                setStepImage(checkResultNum(selectedChallangeItem,todayLong,getApplicationContext()));

            } else {
                finish(); //에러나면 디테일창 닫기
            }
        }

        //디테일 리사이클러 표시
        setRecyclerView();

    }

    void setRecyclerView(){
        recyclerCDexplain.setLayoutManager(new LinearLayoutManager(this));
        ChallangeDetailExplainAdapter cdeAdapter=new ChallangeDetailExplainAdapter(getApplicationContext());
        recyclerCDexplain.setAdapter(cdeAdapter);

    }

    public class ChallangeDetailExplainAdapter extends RecyclerView.Adapter<ChallangeDetailExplainAdapter.CDExplainViewHolder>{

        Context context;
        ArrayList<String> daysInRange;

        public ChallangeDetailExplainAdapter(Context context) {
            this.context=context;
            daysInRange=getDaysInChallangeRange(selectedChallangeItem);

        }

        @NonNull
        @Override
        public CDExplainViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater layoutInflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view=layoutInflater.inflate(R.layout.challange_detail_recycler,viewGroup,false);
            CDExplainViewHolder cdeViewHolder=new CDExplainViewHolder(view);
            return cdeViewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull CDExplainViewHolder cdExplainViewHolder, int i) {
            String date=daysInRange.get(i);
            cdExplainViewHolder.tvCDexDate.setText(date.substring(4,6)+"/"+date.substring(6));

            ArrayList<DailyItem> diaryList=getDailyItemList(selectedChallangeItem,todayLong,getApplicationContext());

       if (selectedChallangeItem.targetType.equals(getResources().getString(R.string.vomit)) ||
               selectedChallangeItem.targetType.equals(getResources().getString(R.string.no_vomit_day)) ){
           //제거없는ㄴ날 / 제거행동 챌린지일 경우

           if (Long.parseLong(date) > todayLong){
               cdExplainViewHolder.tvCDecText.setText("-");
           }else {
               int diaryinthisday=0;
               int removeinthisday=0;
               for (int a=0;a<diaryList.size();a++){ //다이어리를 전부 돌면서
                   if (  Long.toString(diaryList.get(a).dateid).substring(0,8).equals(date)   ){
                       diaryinthisday++;  //기록 날짜가 지금 리사이클러 날짜와 같으면 추가
                       if (diaryList.get(a).remove>=1){
                           removeinthisday++;  //기록날ㅉㅏ가 같으면서 제거행동이 있으면 추가
                       }
                   }
               }

               String cdexText="제거행동 "+removeinthisday+"회 / 총 기록 "+diaryinthisday+"개";
               if (diaryinthisday==0){
                   cdexText="-";
               }
               cdExplainViewHolder.tvCDecText.setText(cdexText);
           }


       }else if(selectedChallangeItem.targetType.equals(getResources().getString(R.string.no_vomit_meal))){
           if (Long.parseLong(date) > todayLong){
               cdExplainViewHolder.tvCDecText.setText("-");
           }else {
               int diaryinthisday=0;
               int removeinthisday=0;
               for (int a=0;a<diaryList.size();a++){ //다이어리를 전부 돌면서
                   if (  Long.toString(diaryList.get(a).dateid).substring(0,8).equals(date)  ){  //기록 날짜가 지금 리사이클러 날짜와 같으면서
                       if ((diaryList.get(a).mealtype.equals(getResources().getString(R.string.breakfast)))
                               || (diaryList.get(a).mealtype.equals(getResources().getString(R.string.lunch)))
                               ||(diaryList.get(a).mealtype.equals(getResources().getString(R.string.dinner))) ){ //아침/점심/저녁 중 하나이면 추가
                           diaryinthisday++;
                           if (diaryList.get(a).remove>=1){
                               removeinthisday++;  //기록날ㅉㅏ가 같으면서 제거행동이 있으면 추가
                           }
                       }
                   }
               }
               String cdexText="제거행동 "+removeinthisday+"회 / 식사 "+diaryinthisday+"회";
               if (diaryinthisday==0){
                   cdexText="-";
               }
               cdExplainViewHolder.tvCDecText.setText(cdexText);
           }

       }else if (selectedChallangeItem.targetType.equals(getResources().getString(R.string.good_breakfast)) ){

           if (Long.parseLong(date) > todayLong){
               cdExplainViewHolder.tvCDecText.setText("-");
           }else {
               int diaryinthisday=0;
               int breakfastinthisday=0;
               int removeinthismeal=0;
               for (int a=0;a<diaryList.size();a++){ //다이어리를 전부 돌면서
                   if (  Long.toString(diaryList.get(a).dateid).substring(0,8).equals(date)){
                       diaryinthisday++;  //총기록
                       if (diaryList.get(a).mealtype.equals(getResources().getString(R.string.breakfast)) ){
                           breakfastinthisday++;  //기록 날짜가 지금 리사이클러 날짜와 같으면 추가
                           if (diaryList.get(a).remove>=1){
                               removeinthismeal++;  //기록날ㅉㅏ가 같으면서 제거행동이 있으면 추가
                           }
                       }

                   }
               }
               String cdexText="제거행동 "+removeinthismeal+"회 / 아침식사 "+breakfastinthisday+"회";
               if (diaryinthisday==0){
                   cdexText="-";
               }
               cdExplainViewHolder.tvCDecText.setText(cdexText);
           }




       }else if (selectedChallangeItem.targetType.equals(getResources().getString(R.string.good_lunch)) ){

           if (Long.parseLong(date) > todayLong){
               cdExplainViewHolder.tvCDecText.setText("-");
           }else {
               int diaryinthisday=0;
               int lunchinthisday=0;
               int removeinthismeal=0;
               for (int a=0;a<diaryList.size();a++){ //다이어리를 전부 돌면서
                   if (  Long.toString(diaryList.get(a).dateid).substring(0,8).equals(date)  ){
                       diaryinthisday++;
                       if ( diaryList.get(a).mealtype.equals(getResources().getString(R.string.lunch)) ){
                           lunchinthisday++;  //기록 날짜가 지금 리사이클러 날짜와 같으면 추가
                           if (diaryList.get(a).remove>=1){
                               removeinthismeal++;  //기록날ㅉㅏ가 같으면서 제거행동이 있으면 추가
                           }
                       }
                   }
               }
               String cdexText="제거행동 "+removeinthismeal+"회 / 점심식사 "+lunchinthisday+"회";
               if (diaryinthisday==0){
                   cdexText="-";
               }
               cdExplainViewHolder.tvCDecText.setText(cdexText);
           }

       }else if (selectedChallangeItem.targetType.equals(getResources().getString(R.string.good_dinner)) ){

           if (Long.parseLong(date) > todayLong){
               cdExplainViewHolder.tvCDecText.setText("-");
           }else {
               int diaryinthisday=0;
               int dinnerinthisday=0;
               int removeinthismeal=0;
               for (int a=0;a<diaryList.size();a++){ //다이어리를 전부 돌면서
                   if (  Long.toString(diaryList.get(a).dateid).substring(0,8).equals(date)   ){
                       diaryinthisday++;
                       if (diaryList.get(a).mealtype.equals(getResources().getString(R.string.dinner)) ){
                           dinnerinthisday++;  //기록 날짜가 지금 리사이클러 날짜와 같으면 추가
                           if (diaryList.get(a).remove>=1){
                               removeinthismeal++;  //기록날ㅉㅏ가 같으면서 제거행동이 있으면 추가
                           }
                       }
                   }
               }
               String cdexText="제거행동 "+removeinthismeal+"회 / 저녁식사 "+dinnerinthisday+"회";
               if (diaryinthisday==0){
                   cdexText="-";
               }
               cdExplainViewHolder.tvCDecText.setText(cdexText);
           }


       }else if (selectedChallangeItem.targetType.equals(getResources().getString(R.string.good_snack)) ){

           if (Long.parseLong(date) > todayLong){
               cdExplainViewHolder.tvCDecText.setText("-");
           }else {
               int diaryinthisday=0;
               int snackinthisday=0;
               int removeinthismeal=0;
               for (int a=0;a<diaryList.size();a++){ //다이어리를 전부 돌면서
                   if (  Long.toString(diaryList.get(a).dateid).substring(0,8).equals(date)  ){
                       diaryinthisday++;
                       if ( diaryList.get(a).mealtype.equals(getResources().getString(R.string.snack)) ){
                           snackinthisday++;  //기록 날짜가 지금 리사이클러 날짜와 같으면 추가
                           if (diaryList.get(a).remove>=1){
                               removeinthismeal++;  //기록날ㅉㅏ가 같으면서 제거행동이 있으면 추가
                           }
                       }
                   }
               }
               String cdexText="제거행동 "+removeinthismeal+"회 / 간식 "+snackinthisday+"회";
               if (diaryinthisday==0){
                   cdexText="-";
               }
               cdExplainViewHolder.tvCDecText.setText(cdexText);
           }

       }







        }

        @Override
        public int getItemCount() {
            return daysInRange.size();
        }

        public class CDExplainViewHolder extends RecyclerView.ViewHolder{
            TextView tvCDexDate , tvCDecText;
            public CDExplainViewHolder(@NonNull View itemView) {
                super(itemView);
                tvCDexDate=(TextView)itemView.findViewById(R.id.tvCDexDate);
                tvCDecText=(TextView)itemView.findViewById(R.id.tvCDexText);
            }
        }
    }





    void setDayImage() {
        ArrayList<ImageView> dayImageList = new ArrayList<>();
        for (int i = 0; i < selectedChallangeItem.rangeInt; i++) {
            ImageView dayImage = new ImageView(this);
            dayImage.setImageResource(R.drawable.daycircle8green);
            dayImage.setAlpha(0.2f);
            dayImageList.add(dayImage);
        }
        if (challangeStatus.equals("ing")) {
            for (int i = 0; i <= (todayLong - selectedChallangeItem.startDateLong); i++)
                dayImageList.get(i).setAlpha(1f);
        } else if (challangeStatus.equals("done")) {
            for (int i = 0; i < dayImageList.size(); i++)
                dayImageList.get(i).setAlpha(1f);
        }
        layoutDayImage.removeAllViews();
        for (int i = 0; i < dayImageList.size(); i++)
            layoutDayImage.addView(dayImageList.get(i));
    }

    void setStepImage(ArrayList<String> dateStrList) {
        layoutStepGrid.removeAllViews();
        int targetStepNum = 0;
        targetStepNum = selectedChallangeItem.targetNum;

        //
        EtcMembers etcMembers = new EtcMembers();
        etcMembers.linearLayoutList = new ArrayList<>();
        etcMembers.imageViewList = new ArrayList<>();
        etcMembers.textViewList = new ArrayList<>();

        if (dateStrList.size() > targetStepNum) {   //만약 목표횟수 이상으로 달성했다면
            for (int i = 0; i < targetStepNum; i++) {  //목표횟수만큼은 기본 이미지
                ImageView stepImage = new ImageView(this);
                etcMembers.imageViewList.add(stepImage);
            }
            for (int k = 0; k < (dateStrList.size() - targetStepNum); k++) {  //초과달성한 횟수만큼 줄 변화 (흐리게 진하게 및나게 등)
                ImageView stepImage = new ImageView(this);
//                stepImage.setAlpha(0.7f);
                etcMembers.imageViewList.add(stepImage);
            }
            for (int j = 0; j < dateStrList.size(); j++) {
                //텍스트 추가
                TextView stepText = new TextView(this);
                stepText.setText(dateStrList.get(j).substring(4, 6) + "/" + dateStrList.get(j).substring(6));

                etcMembers.textViewList.add(stepText);
            }

        } else {  //목표횟수와 같거나 그 이하라면
            for (int i = 0; i < targetStepNum; i++) {
                ImageView stepImage = new ImageView(this);
                if (i >= dateStrList.size()) { //달성 말고 남은 목표 횟수 이미지는 흐린 회색 표시
                    stepImage.setAlpha(0.3f);
                    stepImage.setColorFilter(myGrey);
                } else {  //달성한 횟수까지 (텍스트 표시)
                    TextView stepText = new TextView(this);
                    stepText.setText(dateStrList.get(i).substring(4, 6) + "/" + dateStrList.get(i).substring(6));
                    etcMembers.textViewList.add(stepText);
                }
                etcMembers.imageViewList.add(i, stepImage);
            }
        }


        if (selectedChallangeItem.targetUpDown.equals("DOWN")) {
            for (int i = 0; i < etcMembers.imageViewList.size(); i++) {
                etcMembers.imageViewList.get(i).setImageResource(R.drawable.sadface36orange);
            }
        } else {
            for (int i = 0; i < etcMembers.imageViewList.size(); i++) {
                etcMembers.imageViewList.get(i).setImageResource(R.drawable.happyface36green);
            }
        }


        //만든 이미지와 텍스트뷰 추가
        for (int i = 0; i < etcMembers.imageViewList.size(); i++) {
            LinearLayout linearLayout = new LinearLayout(this);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setPadding(16, 4, 16, 4);

            linearLayout.addView(etcMembers.imageViewList.get(i));
            if (i < etcMembers.textViewList.size()) {  //모든 텍스트뷰 설정
                etcMembers.textViewList.get(i).setGravity(Gravity.CENTER_HORIZONTAL);
                etcMembers.textViewList.get(i).setTextColor(myGrey);
                etcMembers.textViewList.get(i).setTextSize(8);
                etcMembers.textViewList.get(i).setPadding(-4, 0, 0, 0);
                linearLayout.addView(etcMembers.textViewList.get(i));
            }

            etcMembers.linearLayoutList.add(linearLayout);
            layoutStepGrid.addView(etcMembers.linearLayoutList.get(i));
        }


    }


    EtcMembers setSelfCheckLayout() {
        layoutStepGrid.removeAllViews();
//        layoutStepGridETCtext.removeAllViews();
        int targetStepNum = selectedChallangeItem.rangeInt;
        EtcMembers etcMembers = new EtcMembers();
        //이미지 생성
        etcMembers.imageViewList = new ArrayList<>();
        etcMembers.linearLayoutList = new ArrayList<>();

        for (int i = 0; i < targetStepNum; i++) {  //목표횟수만큼 기본 이미지
            ImageView stepImage = new ImageView(this);
            stepImage.setImageResource(R.drawable.happyface36green);
            stepImage.setColorFilter(myGrey);
            stepImage.setAlpha(1f);
            etcMembers.imageViewList.add(stepImage);
        }
        //날짜 텍스트 생성
        ArrayList<String> daysInRange=getDaysInChallangeRange(selectedChallangeItem);
        etcMembers.textViewList = new ArrayList<>();
        for (int i=0;i<daysInRange.size();i++){
            TextView stepDate = new TextView(this);
            stepDate.setText(daysInRange.get(i).substring(4,6)+"/"+daysInRange.get(i).substring(6));
            stepDate.setTextColor(myGrey);
            stepDate.setAlpha(1f);
            etcMembers.textViewList.add(stepDate);
        }

        //만든 이미지와 텍스트뷰 추가
        for (int i = 0; i < etcMembers.imageViewList.size(); i++) {
            etcMembers.textViewList.get(i).setGravity(Gravity.CENTER_HORIZONTAL);
            etcMembers.textViewList.get(i).setTextSize(8);
            etcMembers.textViewList.get(i).setPadding(-4, 0, 0, 0);
            LinearLayout layoutStepGridMember = new LinearLayout(this);
            layoutStepGridMember.setOrientation(LinearLayout.VERTICAL);
            layoutStepGridMember.addView(etcMembers.imageViewList.get(i));
            layoutStepGridMember.addView(etcMembers.textViewList.get(i));
            layoutStepGridMember.setAlpha(0.2f); //일단은 모두 아직 안온날 표시로 흐리게
            layoutStepGridMember.setPadding(16, 4, 16, 4);
            etcMembers.linearLayoutList.add(layoutStepGridMember);
            layoutStepGrid.addView(etcMembers.linearLayoutList.get(i));
        }

        return etcMembers;
    }

    void setPercentage(int didNum) {
        float percentTmp = 80f;
        if (selectedChallangeItem.targetUpDown.equals("UP")) {
            percentTmp = (((float) didNum / (float) selectedChallangeItem.targetNum) * 100);
        } else if (selectedChallangeItem.targetUpDown.equals("DOWN")) {
            percentTmp = 100 - (((float) didNum / (float) selectedChallangeItem.targetNum) * 100);
            if (percentTmp < 0)
                percentTmp = 0;
        }
        String percentStr = String.format("%.1f", percentTmp);
        tvCDpercent.setText(percentStr + "%");

        updateDatabase(percentStr);
    }

    void setEtcPercentage(int didNum) {
        float percentTmp = 80f;
        percentTmp = (((float) didNum / (float) selectedChallangeItem.rangeInt) * 100);
        String percentStr = String.format("%.1f", percentTmp);
        tvCDpercent.setText(percentStr + "%");

    }

    void updateEtcTFList() {  //기타 챌린지는 확인할때마다가 아니라 꾹 눌러 체크할때마다 디비업뎃
        String resultArray = "";
        for (int i = 0; i < etcTFlist.length; i++) {
            resultArray += etcTFlist[i];
            if (i != etcTFlist.length - 1)
                resultArray += ",";
        }
        myDBHelper = new MyDBHelper(this);
        sqlDB = myDBHelper.getWritableDatabase();
        sqlDB.execSQL("update challangeTBL set resultnum='" + resultArray + "' where challangeIndex=" + selectedChallangeItem.challangeIndex + ";");
        sqlDB.close();
        myDBHelper.close();
    }

    void updateDatabase(String percentStr) { //시작도지 않은 챌린지 빼고 퍼센트가 표시되는 모든 경우 (기타 챌린지 제외) resultnum 에 스트링 형태의 소수점 한자리까지 표시하는 최신 성공률 저장 (디테일페이지 들어올떄마다 업뎃)
        myDBHelper = new MyDBHelper(this);
        sqlDB = myDBHelper.getWritableDatabase();
        if (!etcTF) {  //혹시 기타챌린지가 넘어온 것 아닌지 체크
            sqlDB.execSQL("update challangeTBL set resultnum=" + percentStr + " where challangeIndex=" + selectedChallangeItem.challangeIndex + ";");  //소수점 한자리까지 표시하는 최신 성공률 디비에 업데이트
        }
        sqlDB.close();
        myDBHelper.close();
    }


    static ArrayList<DailyItem> getDailyItemList(ChallangeItem selectedChallangeItem,long todayLong,Context context) {  //진행중이거나 끝난 아이템의 경우 날짜에 해당하는 일기리스트
        MyDBHelper myDBHelper = new MyDBHelper(context);
        SQLiteDatabase sqlDB = myDBHelper.getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery("select * from recoveryTBL order by dateid;", null);
        ArrayList<DailyItem> dailyItemListA = new ArrayList<>();
        while (cursor.moveToNext()) {
            String dailyItemDateidTmp = cursor.getString(1);
            dailyItemDateidTmp = dailyItemDateidTmp.substring(0, 4) + dailyItemDateidTmp.substring(6, 8) + dailyItemDateidTmp.substring(10, 12);
            int dailyItemDateIdNum = Integer.parseInt(dailyItemDateidTmp);  //다이어리아이템 날짜를 20190723 형태로
            //체크하고있는 일기아이템 날짜가 도전기간 날짜범위 안에 들어있으면 dailyItemListA에 추가
            if (dailyItemDateIdNum >= (int) (selectedChallangeItem.startDateLong) && dailyItemDateIdNum <= (int) (selectedChallangeItem.endDateLong)  && dailyItemDateIdNum<=todayLong) {
                DailyItem dailyItem = new DailyItem();
                dailyItem.dateid = Long.parseLong(cursor.getString(0));
                dailyItem.datetxt = cursor.getString(1);
                dailyItem.time = cursor.getString(2);
                dailyItem.menu = cursor.getString(3);
                dailyItem.mealtype = cursor.getString(4);
                dailyItem.remove = cursor.getInt(5);
                dailyItem.removetxt = cursor.getString(6);
                dailyItem.location = cursor.getString(7);
                dailyItem.people = cursor.getString(8);
                dailyItem.think = cursor.getString(9);
                dailyItem.menuPhoto = cursor.getBlob(10);
                dailyItem.diaryindex = cursor.getInt(11);
                dailyItemListA.add(dailyItem);
            }
        }
        cursor.close();
        sqlDB.close();
        myDBHelper.close();
        return dailyItemListA;
    }

    static ArrayList<String> checkResultNum(ChallangeItem selectedChallangeItem,Long todayLong,Context context) {
        //여기서 직접 리스트 만들고 날수 세어 작업
        ArrayList<DailyItem> diaryList = getDailyItemList(selectedChallangeItem,todayLong,context);
        String targetTypeNow = selectedChallangeItem.targetType; //다이어리 체크에 사용할 타겟 타입

        ArrayList<String> goodBreakfastList, goodLunchList, goodDinnerList, goodSnackList, noVomitMealList, noVomitDayList, vomitList;
        ArrayList<String> finalList = null;

//        int finalNum = 0;

        if (targetTypeNow.equals(context.getResources().getString(R.string.etc))) { //1. <기타> 시작

            //도전내용 기타인 경우 체크 안했으면 팝업창으로 별점긁어서 체크하게하고 / 체크했으면 선택한 별점 표시

            //1.<기타> 종료
        } else { //<기타>가 아닌 경우의 검사 시작

            //필요한것
            //달성넘버 (finalNum) - 날짜목록 갯수로 대체 가능할듯
            //달성한 날짜 목록

            if (targetTypeNow.equals(context.getResources().getString(R.string.no_vomit_day))) {// <제거행동없는날> 시작
//                ArrayList<Long> datesHasDiary = getDaysHasDiary();  //기간내 일기리스트 중 다이어리가 있는 날짜 텍스트들의 목록

                if (diaryList.size() != 0) {
                    ArrayList<String> vomitDayListAll = new ArrayList<>();


                    //1.구토 있는 날짜 리스트 모두 구하기
                    for (int i = 0; i < diaryList.size(); i++) {
                        if (diaryList.get(i).remove >= 1) {
                            String vomitDayDate = (Long.toString(diaryList.get(i).dateid)).substring(0, 8);
                            vomitDayListAll.add(vomitDayDate);
                        }
                    }

                    //2.기간 내 날짜 모두 구하기
                    ArrayList<String> daysInRangeStr=getDaysInChallangeRange(selectedChallangeItem);

                    //3.기간 내 날짜중 구토 있는 날짜 제외라고 리스트 모으기
                    //////noVomitDayList 만드는 부분 최종적으로 다시 확인!
                    noVomitDayList = new ArrayList<>();
                    for (int i = 0; i < daysInRangeStr.size(); i++) {  //챌린지 날짜 A를 하나씩 돌면서
                        String dayinrangestr = daysInRangeStr.get(i);
                        for (int k = 0; k < vomitDayListAll.size(); k++) {  // 구토기록이 있ㄴㄴ 날짜리스트 B (중목있을수있음) 순서대로 비교하면서 ㄱ다르면 아무것도 하지 않고 다음 B로 넘어가고
                            String vomitDay = vomitDayListAll.get(k);
                            if (!dayinrangestr.equals(vomitDay)) {//만약 같은 것 발견하면 아무것도 하지 않고 다음 A로 넘어감
                                if (k == vomitDayListAll.size() - 1) {
                                    noVomitDayList.add(dayinrangestr);  //B 다 돌때까지 같은것 하나도 없으면 구토 없는 날이거나 기록 없는 날이므로 리스트에 추가
                                }
                            } else {
                                break;
                            }

                        }
                    }

                    //4. 아직 오지 않은 날 빼기
                    ArrayList<String> noVomitDayListFinal=new ArrayList<>();
                    for (int i=0;i<noVomitDayList.size();i++){
                        if (  (Long.parseLong(noVomitDayList.get(i))) <= todayLong  ){
                            noVomitDayListFinal.add(noVomitDayList.get(i));
                        }
                    }

                    finalList = noVomitDayListFinal;
                }

            } else if (targetTypeNow.equals(context.getResources().getString(R.string.good_breakfast))) {  //<아침식사> 시작


                if (diaryList.size() != 0) {
                    goodBreakfastList = new ArrayList<>();
                    for (int i = 0; i < diaryList.size(); i++) {
                        if (diaryList.get(i).mealtype.equals(context.getResources().getString(R.string.breakfast))) {
                            if (diaryList.get(i).remove == 0) { //제거행동 없었다면 횟수 누적
                                String goodBreakfastDate = (Long.toString(diaryList.get(i).dateid)).substring(0, 8);   //ex. 20190725
                                goodBreakfastList.add(goodBreakfastDate);
                            }
                        }
                    }

                    finalList = goodBreakfastList;


                }
            } else if (targetTypeNow.equals(context.getResources().getString(R.string.good_lunch))) {  //<점심식사> 시작


                if (diaryList.size() != 0) {
                    goodLunchList = new ArrayList<>();
                    for (int i = 0; i < diaryList.size(); i++) {
                        if (diaryList.get(i).mealtype.equals(context.getResources().getString(R.string.lunch))) {
                            if (diaryList.get(i).remove == 0) { //제거행동 없었다면 횟수 누적
                                String goodLunchDate = (Long.toString(diaryList.get(i).dateid)).substring(0, 8);   //ex. 20190725
                                goodLunchList.add(goodLunchDate);
                            }
                        }
                    }
                    //<점심식사>종료  >goodLunchNum 활용
                    finalList = goodLunchList;

                }
            } else if (targetTypeNow.equals(context.getResources().getString(R.string.good_dinner))) {  //<저녁식사> 시작


                if (diaryList.size() != 0) {
                    goodDinnerList = new ArrayList<>();
                    for (int i = 0; i < diaryList.size(); i++) {
                        if (diaryList.get(i).mealtype.equals(context.getResources().getString(R.string.dinner))) {
                            if (diaryList.get(i).remove == 0) { //제거행동 없었다면 횟수 누적
                                String goodDinnerDate = (Long.toString(diaryList.get(i).dateid)).substring(0, 8);   //ex. 20190725
                                goodDinnerList.add(goodDinnerDate);
                            }
                        }
                    }

                    finalList = goodDinnerList;


                }
            } else if (targetTypeNow.equals(context.getResources().getString(R.string.good_snack))) {  //<간식> 시작


                if (diaryList.size() != 0) {
                    goodSnackList = new ArrayList<>();
                    for (int i = 0; i < diaryList.size(); i++) {
                        if (diaryList.get(i).mealtype.equals(context.getResources().getString(R.string.snack))) {
                            if (diaryList.get(i).remove == 0) { //제거행동 없었다면 횟수 누적
                                String goodSnackDate = (Long.toString(diaryList.get(i).dateid)).substring(0, 8);   //ex. 20190725
                                goodSnackList.add(goodSnackDate);
                            }
                        }
                    }

                    finalList = goodSnackList;

                }
            } else if (targetTypeNow.equals(context.getResources().getString(R.string.vomit))) {  //<제거행동> 시작
                if (diaryList.size() != 0) {

                    vomitList = new ArrayList<>();

                    for (int i = 0; i < diaryList.size(); i++) {
                        //리스트 기록 모두 체크하여 제거행동 횟수 세기
                        if (diaryList.get(i).remove >= 1) { //제거행동 있었다면 횟수 누적
                            String vomitDate = (Long.toString(diaryList.get(i).dateid)).substring(0, 8);
                            vomitList.add(vomitDate);
                        }
                    }
                    finalList = vomitList;

                }

            } else if (targetTypeNow.equals(context.getResources().getString(R.string.no_vomit_meal))) {  //<제거행동 없는 식사> 시작
                if (diaryList.size() != 0) {
                    noVomitMealList = new ArrayList<>();
                    for (int i = 0; i < diaryList.size(); i++) {
                        if ((diaryList.get(i).mealtype.equals(context.getResources().getString(R.string.breakfast))) || (diaryList.get(i).mealtype.equals(context.getResources().getString(R.string.lunch))) || (diaryList.get(i).mealtype.equals(context.getResources().getString(R.string.dinner)))) {
                            if (diaryList.get(i).remove == 0) { //제거행동 없었다면 횟수 누적
                                String noVomitMealDate = (Long.toString(diaryList.get(i).dateid)).substring(0, 8);
                                noVomitMealList.add(noVomitMealDate);
                            }
                        }
                    }

                    finalList = noVomitMealList;

                }

            }

        }  //<기타>가 아닌 경우의 검사 끝



        if  ( (diaryList == null || diaryList.size() == 0) || (finalList == null || finalList.size() == 0) ){//기간 내 다이어리가 아예 없는 경우 빈 리스트 리턴
            return finalList = new ArrayList<>();
        }
        return finalList;
    }

    static ArrayList<String> getDaysInChallangeRange(ChallangeItem selectedChallangeItem){

        //도전 시작 날짜 캘린더객체회
        Calendar chalStartcalendar = Calendar.getInstance();
        chalStartcalendar.set(Calendar.YEAR, Integer.parseInt(Long.toString(selectedChallangeItem.startDateLong).substring(0, 4)));
        chalStartcalendar.set(Calendar.MONTH, (Integer.parseInt(Long.toString(selectedChallangeItem.startDateLong).substring(4, 6))) - 1);
        chalStartcalendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(Long.toString(selectedChallangeItem.startDateLong).substring(6, 8)));
        //도전 끝 날짜 캘린더 객체화
        Calendar chalEndcalendar = Calendar.getInstance();
        chalEndcalendar.set(Calendar.YEAR, Integer.parseInt(Long.toString(selectedChallangeItem.endDateLong).substring(0, 4)));
        chalEndcalendar.set(Calendar.MONTH, (Integer.parseInt(Long.toString(selectedChallangeItem.endDateLong).substring(4, 6))) - 1);
        chalEndcalendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(Long.toString(selectedChallangeItem.endDateLong).substring(6, 8)));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

        //최종으로 구할 기간 내 날짜들 캘린더 리스트
        ArrayList<Calendar> daysInRange = new ArrayList<>();

        Calendar nowCal = Calendar.getInstance(); //시작날~ 끝날 훑으며 하루씩 증가시켜 날짜저장에 사용할 임시캘린더
        nowCal = chalStartcalendar;

        //최종적으로 반환할 daysInRange의 스트링버전 (20190418)
        ArrayList<String> daysInRangeStr = new ArrayList<>();

        for (int i = 0; i < selectedChallangeItem.rangeInt; i++) {
            if (Integer.parseInt(sdf.format(nowCal.getTime())) <= Integer.parseInt(sdf.format(chalEndcalendar.getTime()))) {
                daysInRange.add(chalEndcalendar);
                daysInRangeStr.add(sdf.format(nowCal.getTime()));
                nowCal.add(Calendar.DAY_OF_MONTH, 1);
            }
        }

        return daysInRangeStr;


    }


    @Override
    protected void onResume() {
        super.onResume();
        myDBHelper = new MyDBHelper(this);
        sqlDB = myDBHelper.getReadableDatabase();
        cursor = sqlDB.rawQuery("select * from challangeTBL where challangeIndex = " + challangeItenId + ";", null);
        cursor.moveToFirst();
        //선택받은 챌린지 아이템 객체화
        selectedChallangeItem = new ChallangeItem();
        selectedChallangeItem.challangeIndex = cursor.getInt(0);
        selectedChallangeItem.startDateLong = (long) (cursor.getInt(1));
        selectedChallangeItem.endDateLong = (long) (cursor.getInt(2));
        selectedChallangeItem.dateStr = cursor.getString(3);
        selectedChallangeItem.rangeInt = cursor.getInt(4);
        selectedChallangeItem.targetStr = cursor.getString(5);
        selectedChallangeItem.rewardStr = cursor.getString(6);
        selectedChallangeItem.targetType = cursor.getString(7);
        selectedChallangeItem.targetNum = cursor.getInt(8);
        selectedChallangeItem.targetUpDown = cursor.getString(9);
        selectedChallangeItem.resultNum = cursor.getString(10);

        cursor.close();
        sqlDB.close();
        myDBHelper.close();

        setPage();
    }


    class EtcMembers {
        ArrayList<TextView> textViewList;
        ArrayList<ImageView> imageViewList;
        ArrayList<LinearLayout> linearLayoutList;
    }
}
