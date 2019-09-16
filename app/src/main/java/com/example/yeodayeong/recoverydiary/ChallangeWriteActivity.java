package com.example.yeodayeong.recoverydiary;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.archit.calendardaterangepicker.customviews.DateRangeCalendarView;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static com.example.yeodayeong.recoverydiary.ChallangeItem.ETC_NUM;
import static com.example.yeodayeong.recoverydiary.ChallangeItem.ETC_STR;

public class ChallangeWriteActivity extends AppCompatActivity {
    LinearLayout layoutCWriteTitle, layoutChallangeCal, layoutTargetDetail, layoutTargetDetailEtc, layoutCWriteAll, layoutChallangeTarget;
    ImageButton ivCWriteClose, ivCWriteSave;
    TextView tvCWriteDate, tvCWriteTarget, tvCWriteRange, tvTargetUnit;
    TextView tvTargetType, tvTargetNum, tvTargetUpDown;
    EditText edtCWriteReward;
    DateRangeCalendarView challangeRangeCal;
    AlertDialog targetTypeDialog, targetNumDialog, targetDateDialog;
    SimpleDateFormat rangeFormat = new SimpleDateFormat("yyyy년 MM월 dd일");
    String startCalStr, endCalStr;
    int rangeNum = 0;
    FrameLayout frameChallangeTarget;
    EditText dlgEdtTargetNum, edtTargetInput;
    TextView dlgTvTargetUnit, dlgTvTargetUpdawn;
    int targetNum, targetTypeIndex;
    boolean fromCWriteTextSW = true;
    TextView tvTargetDetail;

    String writeMode;
    int editChallangeIndex;

    //스피너 준비
    ArrayList<String> numList;
    String[] targetTypes = {"제거행동", "제거행동 없는 식사", "제거행동 없는 날", "아침식사", "점심식사", "저녁식사", "간식", "기타"};
    //    String[] targetUnit={"번","일"};
    String[] targetUpDowns = {"이하", "이상"};
    ArrayAdapter<String> typeAdapter, unitAdapter, updownAdapter, numAdapter;
    String targetStr, targetstrType, targetstrNum, targetstrUpDown;
    int typeIndex = 0, numIndex = 0, updownIndex = 0;

   View dateRangeView ;
    AlertDialog.Builder builderTargetDate ;

    //데이터베이스 준비
    MyDBHelper myDBHelper;
    SQLiteDatabase sqlDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challange_write);
        layoutCWriteAll = (LinearLayout) findViewById(R.id.layoutCWriteAll);
//        layoutWriteTarget=(LinearLayout)findViewById(R.id.layoutWriteTarget);
        layoutCWriteTitle = (LinearLayout) findViewById(R.id.layoutCWriteTitle);
        layoutTargetDetail = (LinearLayout) findViewById(R.id.layoutTargetDetail);
        layoutTargetDetailEtc = (LinearLayout) findViewById(R.id.layoutTargetDetailEtc);
        ivCWriteClose = (ImageButton) findViewById(R.id.ivCWriteClose);
        ivCWriteSave = (ImageButton) findViewById(R.id.ivCWriteSave);
        layoutChallangeCal = (LinearLayout) findViewById(R.id.layoutChallangeCal);
        tvCWriteRange = (TextView) findViewById(R.id.tvCWriteRange);
        tvCWriteDate = (TextView) findViewById(R.id.tvCWriteDate);
        tvCWriteTarget = (TextView) findViewById(R.id.tvCWriteTarget);
        edtCWriteReward = (EditText) findViewById(R.id.edtCWriteReward);
        tvTargetType = (TextView) findViewById(R.id.tvTargetType);
        tvTargetNum = (TextView) findViewById(R.id.tvTargetNum);
        tvTargetUnit = (TextView) findViewById(R.id.tvTargetUnit);
        tvTargetUpDown = (TextView) findViewById(R.id.tvTargetUpDown);
//        challangeRangeCal=(DateRangeCalendarView)findViewById(R.id.challangeRangeCal);
        frameChallangeTarget = (FrameLayout) findViewById(R.id.frameChallangeTarget);
        layoutChallangeTarget = (LinearLayout) findViewById(R.id.layoutChallangeTarget);
        edtTargetInput = (EditText) findViewById(R.id.edtTargetInput);
        tvTargetDetail = (TextView) findViewById(R.id.tvTargetDetail);

        //타이틀바 길이 설정
        LinearLayout.LayoutParams titleParams = (LinearLayout.LayoutParams) layoutCWriteTitle.getLayoutParams();
        titleParams.height = Main2Activity.targetSize;

        //인텐트 받기 ("challangeWriteMode" ->   "NEW" , "EDIT")
        Intent inIntent = getIntent();
        writeMode = inIntent.getStringExtra("challangeWriteMode");
        //수정버전의 경우 수정할 챌린지 인덱스 editChallangeIndex 받기(NEW 이면 0)
        if (writeMode.equals("EDIT")) {
            editChallangeIndex = inIntent.getIntExtra("editId", 1);
            setEditData();
        }else
            editChallangeIndex = 0;

            ivCWriteClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            dateRangeView = (View) View.inflate(ChallangeWriteActivity.this, R.layout.challange_date_dialog, null);
          builderTargetDate = new AlertDialog.Builder(this);
            builderTargetDate.setTitle("도전 기간을 선택해주세요!");
            builderTargetDate.setView(dateRangeView);
            challangeRangeCal = (DateRangeCalendarView) dateRangeView.findViewById(R.id.challangeRangeCal);
            challangeRangeCal.setCalendarListener(new DateRangeCalendarView.CalendarListener() {
                @Override
                public void onFirstDateSelected(Calendar startDate) {
/*                long startCalLong=startDate.getTimeInMillis();
                Date startCal=new Date(startCalLong);
                String startCalStrTmp=rangeFormat.format(startCal);
                tvCWriteDate.setText(startCalStrTmp+"~");*/
                }

                @Override
                public void onDateRangeSelected(Calendar startDate, Calendar endDate) {
                    long startCalLong = startDate.getTimeInMillis();
                    Date startCal = new Date(startCalLong);
                    startCalStr = rangeFormat.format(startCal);
                    tvCWriteDate.setText(startCalStr + "~");

                    long endCalLong = endDate.getTimeInMillis();
                    Date endCal = new Date(endCalLong);
                    endCalStr = rangeFormat.format(endCal);
                    tvCWriteDate.setText(startCalStr + "~" + endCalStr);

                    GregorianCalendar endtmp = (GregorianCalendar) endDate;
                    GregorianCalendar startTmp = (GregorianCalendar) startDate;
                    rangeNum = ((int) (getDayCount(startCal, endCal))) + 1;
                    tvCWriteRange.setVisibility(View.VISIBLE);
                    tvCWriteRange.setText(rangeNum + " 일간");
                    if (frameChallangeTarget.getAlpha() != 1f) {
                        frameChallangeTarget.setAlpha(1.0f);
                    }

                    targetDateDialog.dismiss();
                }
            });


            tvCWriteDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    targetDateDialog.show();

                }
            });

            tvCWriteTarget.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (frameChallangeTarget.getAlpha() == 1)
                        targetTypeDialog.show();
                }
            });

            tvTargetDetail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    targetTypeDialog.show();
                }
            });


            typeAdapter = new ArrayAdapter<String>(ChallangeWriteActivity.this, android.R.layout.simple_spinner_dropdown_item, targetTypes);
//        unitAdapter=new ArrayAdapter<String>(ChallangeWriteActivity.this,android.R.layout.simple_spinner_dropdown_item,targetUnit);
            updownAdapter = new ArrayAdapter<String>(ChallangeWriteActivity.this, android.R.layout.simple_spinner_dropdown_item, targetUpDowns);
            numList = new ArrayList<String>();
            for (int i = 0; i <= 50; i++) {
                numList.add(Integer.toString(i));
            }
            numAdapter = new ArrayAdapter<String>(ChallangeWriteActivity.this, android.R.layout.simple_spinner_item, numList);

            AlertDialog.Builder builderTargetType = new AlertDialog.Builder(this);
            builderTargetType.setTitle("챌린지 유형을 선택해주세요!");
            builderTargetType.setAdapter(typeAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    switch (which) {
                        case 0:  //제거행동
                            dlgTvTargetUnit.setText("번");
                            dlgTvTargetUpdawn.setText("이하");
                            targetTypeIndex = 0;
                        /*layoutTargetDetail.setAlpha(1.0f);
                        layoutTargetDetail.setClickable(true);*/
                            break;
                        case 1:  //제거행동 없는 식사
                            dlgTvTargetUnit.setText("번");
                            dlgTvTargetUpdawn.setText("이상");
                            targetTypeIndex = 1;
                        /*layoutTargetDetail.setAlpha(1.0f);
                        layoutTargetDetail.setClickable(true);*/
                            break;
                        case 2:  //제거행동 없는 날
                            dlgTvTargetUnit.setText("일");
                            dlgTvTargetUpdawn.setText("이상");
                            targetTypeIndex = 2;
                        /*layoutTargetDetail.setAlpha(1.0f);
                        layoutTargetDetail.setClickable(true);*/
                            break;
                        case 3:  //아침식사
                            dlgTvTargetUnit.setText("번");
                            dlgTvTargetUpdawn.setText("이상");
                            targetTypeIndex = 3;
                        /*layoutTargetDetail.setAlpha(1.0f);
                        layoutTargetDetail.setClickable(true);*/
                            break;
                        case 4:  //점심식사
                            dlgTvTargetUnit.setText("번");
                            dlgTvTargetUpdawn.setText("이상");
                            targetTypeIndex = 4;
                        /*layoutTargetDetail.setAlpha(1.0f);
                        layoutTargetDetail.setClickable(true);*/
                            break;
                        case 5:  //저녁식사
                            dlgTvTargetUnit.setText("번");
                            dlgTvTargetUpdawn.setText("이상");
                            targetTypeIndex = 5;
                       /* layoutTargetDetail.setAlpha(1.0f);
                        layoutTargetDetail.setClickable(true);*/
                            break;
                        case 6:  //간식
                            dlgTvTargetUnit.setText("번");
                            dlgTvTargetUpdawn.setText("이상");
                            targetTypeIndex = 6;
                        /*layoutTargetDetail.setAlpha(1.0f);
                        layoutTargetDetail.setClickable(true);*/
                            break;
                        case 7:  //기타
                            if (layoutChallangeTarget.getVisibility() == View.VISIBLE) {
                                layoutChallangeTarget.setVisibility(View.GONE);
                            }
                            if (tvCWriteTarget.getVisibility() == View.VISIBLE) {
                                tvCWriteTarget.setVisibility(View.GONE);
                            }
                            layoutTargetDetailEtc.setVisibility(View.VISIBLE);
                            targetTypeIndex = 7;

                            break;
                    }
//                tvCWriteTargetSet(which,-1);
                    if (which != 7) {
                        if (fromCWriteTextSW) {
                            targetNumDialog.show();
                        } else {
                            if (layoutTargetDetailEtc.getVisibility() == View.VISIBLE) {
                                layoutTargetDetailEtc.setVisibility(View.GONE);
                            }
                            layoutChallangeTarget.setVisibility(View.VISIBLE);
                            tvCWriteTargetSet(which, -1);
                        }
                    }

                }
            });

            final View numDialogView = (View) View.inflate(ChallangeWriteActivity.this, R.layout.challange_detail_dialog, null);
            final AlertDialog.Builder builderTargetNum = new AlertDialog.Builder(this);
            builderTargetNum.setTitle("도전 횟수를 선택해주세요!");
            builderTargetNum.setView(numDialogView);
            dlgEdtTargetNum = (EditText) numDialogView.findViewById(R.id.dlgEdtTargetNum);
            dlgTvTargetUnit = (TextView) numDialogView.findViewById(R.id.dlgTvTargetUnit);
            dlgTvTargetUpdawn = (TextView) numDialogView.findViewById(R.id.dlgTvTargetUpDown);

            builderTargetNum.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!dlgEdtTargetNum.getText().toString().trim().equals("")) {
                        targetNum = Integer.parseInt(dlgEdtTargetNum.getText().toString().trim());
                        if (fromCWriteTextSW) {
                            tvCWriteTargetSet(targetTypeIndex, targetNum);
                        } else {
                            tvCWriteTargetSet(-1, targetNum);
                        }

                        tvCWriteTarget.setVisibility(View.GONE);
                        layoutChallangeTarget.setVisibility(View.VISIBLE);
                    } else {
                        makeToast("횟수를 확인해주세요");
                    }

                }
            });


            targetNumDialog = builderTargetNum.create();
            targetTypeDialog = builderTargetType.create();
            targetDateDialog = builderTargetDate.create();


            tvTargetType.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fromCWriteTextSW = false;
                    targetTypeDialog.show();
                }
            });

            tvTargetNum.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dlgEdtTargetNum.setText(tvTargetNum.getText().toString());
                    targetNumDialog.show();
                }
            });


            ivCWriteSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String rangeFinalStr = tvCWriteRange.getText().toString();
                    String targetDateFinal = tvCWriteDate.getText().toString();
                    String rewardFinal = edtCWriteReward.getText().toString().trim();
                    rewardFinal=rewardFinal.replace("'","''");
                    String targetType;
                    int targetNum;
                    String targetUpDown = null;
                    String targetFinal;
                    String resultNum="";
                    int rangeFinal = Integer.parseInt(rangeFinalStr.replaceAll("[^0-9]", ""));

                    if (layoutTargetDetailEtc.getVisibility() == View.VISIBLE) {  //타겟타입이 기타인 경우
                        targetType = "기타";
                        targetFinal = edtTargetInput.getText().toString().trim();
                        targetFinal=targetFinal.replace("'","''");
                        targetNum = ETC_NUM;
                        targetUpDown = ETC_STR;
                        for (int i=1;i<=rangeFinal;i++){
                            resultNum+="0";
                            if (i!=rangeFinal)
                                resultNum+=",";
                        }

                    } else {

                        targetType = tvTargetType.getText().toString();
                        targetNum = Integer.parseInt(tvTargetNum.getText().toString());
                        targetFinal = targetType + " " + targetNum + tvTargetUnit.getText().toString() + " " + tvTargetUpDown.getText().toString();

                        if (tvTargetUpDown.getText().toString().equals("이상")) {
                            targetUpDown = "UP";
                        } else if (tvTargetUpDown.getText().toString().equals("이하")) {
                            targetUpDown = "DOWN";
                        }

                        resultNum="0.0";
                    }

                    long challangeId, challangeEndId;

                    if (tvCWriteDate.getText().toString().equals("도전 기간을 정해주세요.")) {
                        makeToast("도전 기간을 지정해주세요!");
                    } else if ((rangeFinalStr == null) || (targetDateFinal.length() < 15)) {
                        makeToast("도전 기간을 확인해주세요.");
                    } else if (tvCWriteTarget.getVisibility() == View.VISIBLE) {
                        makeToast("도전 목표를 선택해주세요.");
                    } else if ((targetType == "기타") && ((targetFinal).equals("") || targetFinal == null)) {
                        makeToast("도전할 내용을 직접 작성해주세요.");
                    } else if (tvTargetNum.getText().toString().equals("횟수 선택")) {
                        makeToast("횟수를 선택해주세요!");
                    } else if ((targetUpDown.equals("UP")) && (targetNum == 0)) {
                        makeToast("'" + targetFinal + "'" + "은 올바른 도전 내용이 아니에요. 다시 확인해주세요.");
                    } else if ((targetType.equals(getResources().getString(R.string.no_vomit_day))) && (targetNum > rangeFinal)) {
                        makeToast("도전 기간은 " + rangeFinal + "일 이에요.\n" + rangeFinal + "일 이하로 목표날짜를 설정해주세요");
                    } else if (rewardFinal == null || rewardFinal.equals("")) {
                        makeToast("리워드를 입력해주세요.");
                    } else {  //모두 올바르게 입력된 경우
                        String starttmp = (targetDateFinal.substring(0, 4)) + (targetDateFinal.substring(6, 8)) + (targetDateFinal.substring(10, 12));
                        challangeId = Long.parseLong(starttmp);
                        String endtmp = (targetDateFinal.substring(14, 18)) + (targetDateFinal.substring(20, 22)) + (targetDateFinal.substring(24, 26));
                        challangeEndId = Long.parseLong(endtmp);

                        myDBHelper = new MyDBHelper(getApplicationContext());
                        sqlDB = myDBHelper.getWritableDatabase();
                        String toastMsg="";
                        if (writeMode.equals("EDIT")){
                            sqlDB.execSQL("update challangeTBL set challangeid = " + challangeId + " where challangeindex=" + editChallangeIndex + ";");
                            sqlDB.execSQL("update challangeTBL set challangeendid = " + challangeEndId + " where challangeindex=" + editChallangeIndex + ";");
                            sqlDB.execSQL("update challangeTBL set targetdate = '" + targetDateFinal + "' where challangeindex=" + editChallangeIndex + ";");
                            sqlDB.execSQL("update challangeTBL set daterange = " + rangeFinal + " where challangeindex=" + editChallangeIndex + ";");
                            sqlDB.execSQL("update challangeTBL set target = '" + targetFinal + "' where challangeindex=" + editChallangeIndex + ";");
                            sqlDB.execSQL("update challangeTBL set reward = '" + rewardFinal + "' where challangeindex=" + editChallangeIndex + ";");
                            sqlDB.execSQL("update challangeTBL set targettype = '" + targetType + "' where challangeindex=" + editChallangeIndex + ";");
                            sqlDB.execSQL("update challangeTBL set targetnum = " + targetNum + " where challangeindex=" + editChallangeIndex + ";");
                            sqlDB.execSQL("update challangeTBL set targetupdown = '" + targetUpDown + "' where challangeindex=" + editChallangeIndex + ";");
                            toastMsg="도전 내용이 수정되었어요. ";

                        }else {
                            sqlDB.execSQL("insert into challangeTBL (challangeid,challangeendid,targetdate,daterange, target ,reward,targettype,targetnum,targetupdown,resultnum)" +
                                    " values (" + challangeId + ",'" + challangeEndId + "','" + targetDateFinal + "','" + rangeFinal + "','" + targetFinal + "','" + rewardFinal + "','" + targetType + "'," + targetNum + ",'" + targetUpDown + "','"+resultNum+"');");
                            toastMsg="도전 내용이 저장되었어요. 화이팅! ";
                        }
                        sqlDB.close();
                        myDBHelper.close();
                        finish();
                        makeToast(toastMsg);

                    }


                }


            });


        }


        public static long getDayCount (Date start, Date end){  //날짜 기간 구하는 메소드
            long diff = -1;
            try {
                Date dateStart = start;
                Date dateEnd = end;
                //time is always 00:00:00, so rounding should help to ignore the missing hour when going from winter to summer time, as well as the extra hour in the other direction
                diff = Math.round((dateEnd.getTime() - dateStart.getTime()) / (double) 86400000);
            } catch (Exception e) {
            }
            return diff;
        }

        //스피너 선택할때마다 그부분 텍스트만 받아서 텍스트 대체해주는 메소드
        void tvCWriteTargetSet ( int type, int num){
            if (type != -1) {
                typeIndex = type;
            }
            if (num != -1) {
                numIndex = num;
            }
            targetstrType = targetTypes[typeIndex];
            targetstrNum = Integer.toString(numIndex);
            tvTargetType.setText(targetstrType);
            tvTargetNum.setText(targetstrNum);
            tvTargetUnit.setText(dlgTvTargetUnit.getText().toString());
            tvTargetUpDown.setText(dlgTvTargetUpdawn.getText().toString());
//        targetstrUpDown=targetUpDowns[updownIndex];
       /* targetStr=targetstrType+" "+targetstrNum+" "+(dlgTvTargetUnit.getText().toString())+" "+dlgTvTargetUpdawn.getText().toString();
        tvCWriteTarget.setText(targetStr);*/
            if (layoutTargetDetailEtc.getVisibility() == View.VISIBLE) {
                layoutTargetDetailEtc.setVisibility(View.GONE);
            }
            layoutChallangeTarget.setVisibility(View.VISIBLE);

        }


        void makeToast (String msg){
            Toast.makeText(ChallangeWriteActivity.this, msg, Toast.LENGTH_SHORT).show();
        }

        void setEditData () {
            if (writeMode.equals("EDIT")){  //수정버전 맞는지 한번 더 확인
            myDBHelper=new MyDBHelper(this);
            sqlDB=myDBHelper.getReadableDatabase();
            Cursor cursor=sqlDB.rawQuery("select * from challangeTBL where challangeIndex = " + editChallangeIndex + ";", null);
            cursor.moveToFirst();
            ChallangeItem challangeItemEdt=new ChallangeItem();
                challangeItemEdt.challangeIndex=cursor.getInt(0);
                challangeItemEdt.startDateLong=(long)(cursor.getInt(1));
                challangeItemEdt.endDateLong=(long)(cursor.getInt(2));
                challangeItemEdt.dateStr=cursor.getString(3);
                challangeItemEdt.rangeInt=cursor.getInt(4);
                challangeItemEdt.targetStr=cursor.getString(5);
                challangeItemEdt.rewardStr=cursor.getString(6);
                challangeItemEdt.targetType = cursor.getString(7);
                if (!challangeItemEdt.targetType.equals(getResources().getString(R.string.etc))){
                    challangeItemEdt.targetNum = cursor.getInt(8);
                    challangeItemEdt.targetUpDown = cursor.getString(9);
                }

                frameChallangeTarget.setAlpha(1.0f); //탭 할성화
                tvCWriteDate.setText(challangeItemEdt.dateStr);
                tvCWriteRange.setText(challangeItemEdt.rangeInt+"일간");
                tvCWriteRange.setVisibility(View.VISIBLE);

                if (challangeItemEdt.targetType.equals(getResources().getString(R.string.etc))){  //'기타'챌린지를 수정하는 경우
                    if (layoutChallangeTarget.getVisibility() == View.VISIBLE) {
                        layoutChallangeTarget.setVisibility(View.GONE);
                    }
                    if (tvCWriteTarget.getVisibility() == View.VISIBLE) {
                        tvCWriteTarget.setVisibility(View.GONE);
                    }
                    layoutTargetDetailEtc.setVisibility(View.VISIBLE);
                    targetTypeIndex = 7;
                    edtTargetInput.setText(challangeItemEdt.targetStr);

                }else {  //기타가 아닌 챌린지를 수정하는 경우
                    if (layoutTargetDetailEtc.getVisibility() == View.VISIBLE) {
                        layoutTargetDetailEtc.setVisibility(View.GONE);
                    }
                    if (tvCWriteTarget.getVisibility() == View.VISIBLE) {
                        tvCWriteTarget.setVisibility(View.GONE);
                    }
                    layoutChallangeTarget.setVisibility(View.VISIBLE);
                    tvTargetType.setText(challangeItemEdt.targetType); //타겟타입 작성
                    if (challangeItemEdt.targetUpDown.equals("UP")) {//타겟 이상이하 표시
                        tvTargetUpDown.setText("이상");
                    } else{
                        tvTargetUpDown.setText("이하");
                    }

                    tvTargetNum.setText(Integer.toString(challangeItemEdt.targetNum));  //타겟횟수 작성

                }
                edtCWriteReward.setText(challangeItemEdt.rewardStr);
            }

        }

    }
