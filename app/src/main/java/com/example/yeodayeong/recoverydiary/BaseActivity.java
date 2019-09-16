package com.example.yeodayeong.recoverydiary;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.archit.calendardaterangepicker.customviews.DateRangeCalendarView;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.example.yeodayeong.recoverydiary.Main2Activity.RESET;
import static com.example.yeodayeong.recoverydiary.PrintCalendarFragment.selectedPrintItemList;

public class BaseActivity extends AppCompatActivity implements PrintCalendarFragment.MyPrintListner {
    Toolbar toolbar;

    ActionBarDrawerToggle mDrawerToggle;
    Context context;
    NavigationView navigationView;
    DrawerLayout drawerLayout;
    ImageButton ivPrevMonth, ivNextMonth;
    TextView tvYearText, tvMonthText;

    AlertDialog printRangeDlg;
    AlertDialog printMailDlg;

    String filename = "printtestfile";
    String folderName = "printtestpdf";

    String myEmail="";
    EditText edtPrintMail;

    int ingDrawer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected boolean useToolbar() {
        return true;
    }

    @Override
    public void setContentView(int layoutResID) {
        context = this;
        DrawerLayout fullView = (DrawerLayout) getLayoutInflater().inflate(R.layout.drawer_main, null);
        //네비게이션뷰 포함한 드로어레이아웃.xml 캐스팅 (툴바포함)

        FrameLayout activityContainer = (FrameLayout) fullView.findViewById(R.id.frame);
        getLayoutInflater().inflate(layoutResID, activityContainer, true);
        //액션바 제외한 실제 레이아웃 부분 인플레이팅 시킴

        super.setContentView(fullView);  //캐스팅한 드로어레이아웃을 베이스액티비티에 셋뷰
        toolbar = (Toolbar) fullView.findViewById(R.id.tool_bar);
        ivNextMonth = (ImageButton) toolbar.findViewById(R.id.ivNextMonth);
        ivPrevMonth = (ImageButton) toolbar.findViewById(R.id.ivPreMonth);
        tvYearText = (TextView) toolbar.findViewById(R.id.tvYearText);
        tvMonthText = (TextView) toolbar.findViewById(R.id.tvMonthText);

        setSupportActionBar(toolbar);
//        getSupportActionBar().setTitle();
        toolbar.setTitle("");
        this.getSupportActionBar().setElevation(0);

//        getSupportActionBar().setLogo();
        if (useToolbar()) {
            setSupportActionBar(toolbar);
            setTitle("");
        } else {
            toolbar.setVisibility(View.GONE);
        }

        //  프린트 다이알로그 준비
        setPrintDialog();

        /*//드로어레이아웃 내 내비게이션뷰 캐스팅
        drawer_main 레이아웃 안에 NavigationView 위젯 들어있고, 여기에 (레이아옷)drawer_header와 (menu 레이아웃)drawer가 들어있음*/
        navigationView = (NavigationView) findViewById(R.id.navigationView);
        ingDrawer=navigationView.getCheckedItem().getItemId(); //현재 체크된 아이템 아이디 보관
        //네비게이션뷰 리스너(헤더)
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId()!=ingDrawer) {
                    //(menu 레이아웃)drawer 소속만
                    switch (menuItem.getItemId()) {
                        case R.id.drawer_main:
                            //어디서든 드로어 통해 메인으로 가면 날짜 리셋되고 툴바 내려오도록
                            Intent intentMain = new Intent(getApplicationContext(), Main2Activity.class);
//                        intentMain.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);  // 같은 액티비티 스택에 있으면 다시 만들지 않고 앞으로 꺼내옴. =>스택에 동일 액티비티 복수 존재 x
                            intentMain.putExtra("selectedDate", RESET);
                            startActivity(intentMain);

                            break;
                        case R.id.drawer_challange:
                            Intent intentTmp = new Intent(getApplicationContext(), ChallangeActivity.class);
                            startActivity(intentTmp);

                            break;
                        case R.id.drawer_graph:
                            Intent intentGraph = new Intent(getApplicationContext(), GraphActivity.class);
                            startActivity(intentGraph);
                            break;
                        case R.id.drawer_print:
                            printRangeDlg.show();
                            break;
                        case R.id.drawer_setting:
                            Toast.makeText(getApplicationContext(), "문의, 인포메이션 창 띄우기", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(getApplicationContext(), "??", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    drawerLayout.closeDrawers();

                }
                return true;
            }
        });

        //헤더부분은 여기서 세부 캐스팅하고 / 클릭리스너 각각 달기
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        View header = navigationView.getHeaderView(0);
        TextView headerTv1 = (TextView) header.findViewById(R.id.headerTv1);
        TextView headerTv2 = (TextView) header.findViewById(R.id.headerTv2);
        if (headerTv1 != null)
            headerTv1.setText("여다영");
        if (headerTv2 != null)
            headerTv2.setText("오늘은 넘어져도 나를 용서하는 날");

        headerTv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "리스너 테스트!", Toast.LENGTH_SHORT).show();
                //다이알로그 만들어 알림창으로 마이페이지 띄우기 (도전 일수 / 사진 / 이름 / 셀프문구 / +에딧버튼으로 여기서 수정 가능하게)
            }
        });

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        actionBarDrawerToggle.syncState();
    }

    void setToolbar() {

    }

    void setPrintDialog() {
        ////다이알로그로 프린트 날짜설정 열기
        AlertDialog.Builder printDlgBuilder = new AlertDialog.Builder(BaseActivity.this);
        final AlertDialog.Builder printMailBuilder = new AlertDialog.Builder(BaseActivity.this);
        final View printRangeView = (View) View.inflate(BaseActivity.this, R.layout.print_dialog, null);
        View printMailView = (View) View.inflate(BaseActivity.this, R.layout.print_mail_dialog, null);
        printDlgBuilder.setTitle("인쇄할 날짜를 선택해주세요.");
        printMailBuilder.setTitle("전송받을 메일 주소를 입력해주세요.");
        printDlgBuilder.setView(printRangeView);
        printMailBuilder.setView(printMailView);
        final FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.commit();

        final PrintCalendarFragment printCalendarFragment = (PrintCalendarFragment) fragmentManager.findFragmentById(R.id.printfragment);
        //다른 캘린더 써서 일기 있는 날만 활성화-> 여러 날짜 선택해서 보내는걸로 수정
        edtPrintMail = (EditText) printMailView.findViewById(R.id.edtPrintMail);

        printDlgBuilder.setPositiveButton("다음", null);
        printDlgBuilder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                printRangeDlg.dismiss();
                printCalendarFragment.resetPrintCal();
            }
        });
        printMailBuilder.setPositiveButton("전송",null);
    /*    빌더에 버튼 세팅과 동시에 리스너 장착하면, 그 리스너 내용 수행하고 무조건 다이알로그 닫혀버리므로,
        별도로 다이알로그객체에 리스너(printMailDlg.setOnShowListener) 달고 그 안에서 버튼 찾아 캐스팅하여 그 버튼에 온클릭리스너+다이알로그 닫기 달아줌*/

        edtPrintMail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if ((edtPrintMail.getText().toString().trim()).equals("")){
                    printMailDlg.getButton(DialogInterface.BUTTON_POSITIVE).setClickable(false);
                    printMailDlg.getButton(DialogInterface.BUTTON_POSITIVE).setAlpha(0.3f);
                }else{
                    printMailDlg.getButton(DialogInterface.BUTTON_POSITIVE).setClickable(true);
                    printMailDlg.getButton(DialogInterface.BUTTON_POSITIVE).setAlpha(1f);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        printMailBuilder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                printMailDlg.dismiss();
                printCalendarFragment.resetPrintCal();
            }
        });

        printRangeDlg = printDlgBuilder.create();
        printMailDlg = printMailBuilder.create();
        printMailDlg.setCanceledOnTouchOutside(false);
        printRangeDlg.setCanceledOnTouchOutside(false);

        printRangeDlg.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button button=((AlertDialog)printRangeDlg).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (selectedPrintItemList==null ||selectedPrintItemList.size()==0){
                            Toast.makeText(getApplicationContext(),"인쇄할 날짜를 하나 이상 선택해주세요!",Toast.LENGTH_SHORT).show();
                        }else{
                            printRangeDlg.dismiss();
                            printMailDlg.show();
                            setMailButtonDefault();
                        }
                    }
                });
            }
        });
        printMailDlg.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button button=((AlertDialog)printMailDlg).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //목록에 해당하는 일기 피디에프로 만들기
                        myEmail=edtPrintMail.getText().toString().trim();
                        if (myEmail.contains("@")&&myEmail.contains(".")){

                            MakePdf makePdf = new MakePdf(getApplicationContext(), filename, folderName);
                            try {
                                makePdf.makeDoc();
                                Log.d("why", "makeDoc");
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (DocumentException e) {
                                e.printStackTrace();
                            }

                            ArrayList<PrintItem> printDiaryList = getDiaryListForPrint(selectedPrintItemList);  //printItem 에 프린트할 내용들 끼니별로 담아 리스트로 모음
                            String dateTitlePrev = printDiaryList.get(0).dateTxt;
                            String dateTitleNow = "";
                            makePdf.createTable();  //
                            Log.d("why", "createTable");
                            for (int i = 0; i < printDiaryList.size(); i++) {
                                dateTitleNow = printDiaryList.get(i).dateTxt;  //타이틀 날짜와 요번 프린트아이템 날짜 같으면
                        /*Log.d("why","dateTitlePrev:  "+dateTitlePrev);
                        Log.d("why","dateTitleNow:  "+dateTitleNow);*/
                                if (dateTitlePrev.equals(dateTitleNow)) {
                                    //내용 한 행 넣기 코딩
                                    if (printDiaryList.get(i).menuPhoto !=null){  //사진 있는 경우 사진 바이트어레이 보냄
                                        makePdf.addTableRow(printDiaryList.get(i).timeTxt, printDiaryList.get(i).menuTxt, printDiaryList.get(i).removeTxt, printDiaryList.get(i).locationTxt, printDiaryList.get(i).peopleTxt, printDiaryList.get(i).thinkTxt,printDiaryList.get(i).menuPhoto);
                                    } else { //사진없는경우 사진빼고 매개로 보냄
                                        makePdf.addTableRow(printDiaryList.get(i).timeTxt, printDiaryList.get(i).menuTxt, printDiaryList.get(i).removeTxt, printDiaryList.get(i).locationTxt, printDiaryList.get(i).peopleTxt, printDiaryList.get(i).thinkTxt);
                                    }

                                    if (i == printDiaryList.size() - 1) {
                                        makePdf.writeTable(dateTitlePrev);
                                        Log.d("why", "writeTable");
                                    }

                                } else {
                                    makePdf.writeTable(dateTitlePrev);
                                    Log.d("why", "writeTable");
                                    makePdf.createTable();  //
                                    Log.d("why", "createTable");

                                    dateTitlePrev = dateTitleNow;
                                    i--;
                                }

                            }

                            makePdf.document.close();
                            sendPdftoEmail(myEmail);

                            printMailDlg.dismiss();
                            printCalendarFragment.resetPrintCal();

                        }else{
                            Toast.makeText(getApplicationContext(),"메일 주소를 확인해주세요",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }


    void setMailButtonDefault(){
        printMailDlg.getButton(DialogInterface.BUTTON_POSITIVE).setClickable(false);
        printMailDlg.getButton(DialogInterface.BUTTON_POSITIVE).setAlpha(0.3f);
    }
    void setRangeButtonDefault(){
        printRangeDlg.getButton(DialogInterface.BUTTON_POSITIVE).setClickable(false);
        printRangeDlg.getButton(DialogInterface.BUTTON_POSITIVE).setAlpha(0.3f);
    }

    ArrayList<PrintItem> getDiaryListForPrint(ArrayList<CalendarItem> calList) {
        MyDBHelper myDBHelper = new MyDBHelper(getApplicationContext());
        SQLiteDatabase sqlDB = myDBHelper.getReadableDatabase();
        Cursor cursor = sqlDB.rawQuery("select * from recoveryTBL order by dateid;", null);

        ArrayList<PrintItem> printDiaryList = new ArrayList<>();

        while (cursor.moveToNext()) { //데이터 0번부터 끝번까지 한줄씩 테스트
            for (int i = 0; i < calList.size(); i++) {
                String calDateTxt = calList.get(i).getDateString();  //선택한 날짜 객체 처음부터 끝까지 돌면서
                if (cursor.getString(1).equals(calDateTxt)) {  //날짜 문장이 같으면
                    PrintItem printItem = new PrintItem();
                    printItem.dateTxt = cursor.getString(1);
                    printItem.timeTxt = cursor.getString(2);
                    printItem.menuTxt = cursor.getString(3);
                    if (cursor.getInt(5) == 1) {
                        printItem.removeTF = true;
                        printItem.removeTxt = cursor.getString(6);
                    } else {
                        printItem.removeTF = false;
                        printItem.removeTxt = " ";
                    }
                    printItem.locationTxt = cursor.getString(7);
                    printItem.peopleTxt = cursor.getString(8);
                    printItem.thinkTxt = cursor.getString(9);
                    //사진 넣기
                    printItem.menuPhoto=cursor.getBlob(10);

                    printDiaryList.add(printItem);
                }
            }
        }

        return printDiaryList;
    }



    void sendPdftoEmail(String myEmail) {
        Calendar today=Calendar.getInstance();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_EMAIL,new String[]{myEmail});
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "식사 일기 ("+today.get(Calendar.YEAR)+"년 "+(today.get(Calendar.MONTH)+1)+"월 "+today.get(Calendar.DAY_OF_MONTH)+"일)");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "내용 없음");

        File shareFolderPath = new File(getApplicationContext().getFilesDir(), folderName);
        File shareFile = new File(shareFolderPath, filename + ".pdf");
        Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), "com.example.yeodayeong.recoverydiary.fileprovider", shareFile);
//        uris.add(contentUri);
        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);

        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        String msgStr = "어디로 보낼까요?";
        startActivity(Intent.createChooser(shareIntent, msgStr));

        Toast.makeText(getApplicationContext(),"pdf 임시저장 완료",Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onMyPrintListner(Object data) {
        Button button=((AlertDialog)printRangeDlg).getButton(AlertDialog.BUTTON_POSITIVE);
        boolean buttonTF=(boolean)data;
        if (buttonTF){
            button.setClickable(true);
            button.setAlpha(1f);
        }else{
            button.setAlpha(0.3f);
            button.setClickable(true);
        }
    }
}
