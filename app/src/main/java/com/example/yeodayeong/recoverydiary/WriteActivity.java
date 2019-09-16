
package com.example.yeodayeong.recoverydiary;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


/*import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;*/


import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.sql.Array;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.example.yeodayeong.recoverydiary.DailyActivity.dailyActivity;
import static com.example.yeodayeong.recoverydiary.Main2Activity.WRITE_FINISH;


public class WriteActivity extends AppCompatActivity {

    ConstraintLayout layoutWriteTime, layoutWriteMenu, layoutWriteRemove, layoutWriteLocation, layoutWritePeople, layoutWriteThink, layoutWriteType;
    LinearLayout layoutWriteTitle, layoutWritePhoto, photoPicLayout, layoutAll;
    TextView tvDate, tvTime, tvLocationMap, tvPhoto, tvRemove, tvType;
    CheckBox cbRemove;
    ImageButton ivWriteClose, ivMenuDone, ivPeopleDone, ivPhotoDel, ivWriteSave;
    EditText edtMenu, edtPeople, edtRemove, edtThink, edtLocation;
    ListView listviewmenu, listViewPeople;
    ListItemAdapter menuadapter, peopleadapter;
    ArrayList<String> menuList, peopleList;
    CalendarView calView;
    TimePicker tPicker;
    FrameLayout calViewFrame;
    ImageView ivMenuPhoto;
    LinearLayout.LayoutParams menuparams;
    Bitmap menuBitmap = null;
    String editTime;
    Long edtid;
    Bitmap convertedBitmap;
    AlertDialog dateDialog, timeDialog;
    View dateDialogView, timeDialogView;

    //날짜와 시간
    SimpleDateFormat timeFormat = new SimpleDateFormat("a hh:mm");
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy년 MM월 dd일");
    String pickedDatestr;
    int dayofWeek;
    Calendar pickedcalendar;
    Date pickedDate;
    //    Spinner spnType;
    String tag;
    int diaryEditIndex;

    int yearPicked = 2019, monthPicked = 5, dayPicked = 13, hourPicked = 11, minutePicked = 30;
    String dayofweekPicked = "월", timeRange = "pm";

    private static final int PLACE_REQUEST = 1;
    private static final int GALLARY_REQUEST = 2;

    //저장할 변수
    String eLocation;

    byte[] picByteArr;
    FileOutputStream fos;

    List<Place.Field> fields;

    AutocompleteSupportFragment autocompleteSupportFragment;

    int remove;
    String removeTxt, removeTxtLoad;

    boolean entered = false;
    AlertDialog typeDialog;

    Time pTime;

    private long mLastClickTime = 0;


    //데이타배이스 준비
    MyDBHelper myDBHelper;
    SQLiteDatabase sqlDB;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        String aa = "fewfwef";
        picByteArr = (byte[]) aa.getBytes();

        layoutAll = (LinearLayout) findViewById(R.id.layoutAll);


        layoutWriteTitle = (LinearLayout) findViewById(R.id.layoutWriteTitle);
        myDBHelper = new MyDBHelper(this);
        sqlDB = myDBHelper.getWritableDatabase();
        layoutWriteType = (ConstraintLayout) findViewById(R.id.layoutWriteType);
        layoutWriteTime = (ConstraintLayout) findViewById(R.id.layoutWriteTime);
        layoutWriteMenu = (ConstraintLayout) findViewById(R.id.layoutWriteMenu);
        layoutWriteRemove = (ConstraintLayout) findViewById(R.id.layoutWriteRemove);
        layoutWriteLocation = (ConstraintLayout) findViewById(R.id.layoutWriteLocation);
        layoutWritePeople = (ConstraintLayout) findViewById(R.id.layoutWritePeople);
        layoutWriteThink = (ConstraintLayout) findViewById(R.id.layoutWriteThink);
        layoutWritePhoto = (LinearLayout) findViewById(R.id.layoutWritePhoto);
        photoPicLayout = (LinearLayout) findViewById(R.id.photoPicLayout);
        tvType = (TextView) findViewById(R.id.tvType);
        tvDate = (TextView) findViewById(R.id.tvDate);
        tvTime = (TextView) findViewById(R.id.tvTime);
        tvLocationMap = (TextView) findViewById(R.id.tvLocationMap);
        tvPhoto = (TextView) findViewById(R.id.tvPhoto);
        tvRemove = (TextView) findViewById(R.id.tvRemove);
        cbRemove = (CheckBox) findViewById(R.id.cbRemove);
        ivWriteClose = (ImageButton) findViewById(R.id.ivWriteClose);
        ivWriteSave = (ImageButton) findViewById(R.id.ivWriteSave);
        ivMenuDone = (ImageButton) findViewById(R.id.ivMenuDone);
        edtMenu = (EditText) findViewById(R.id.edtMenu);
        ivPeopleDone = (ImageButton) findViewById(R.id.ivPeopleDone);
        listViewPeople = (ListView) findViewById(R.id.listViewPeople);
        edtPeople = (EditText) findViewById(R.id.edtPeople);
        edtRemove = (EditText) findViewById(R.id.edtRemove);
        edtThink = (EditText) findViewById(R.id.edtThink);
        edtLocation = (EditText) findViewById(R.id.edtLocation);
//        calView = (CalendarView) findViewById(R.id.calView);
//        tPicker = (TimePicker) findViewById(R.id.tPicker);
//        calViewFrame = (FrameLayout) findViewById(R.id.calViewFreme);
        ivMenuPhoto = (ImageView) findViewById(R.id.ivMenuPhoto);
        ivPhotoDel = (ImageButton) findViewById(R.id.ivPhotoDel);
//        spnType=(Spinner)findViewById(R.id.spnType);


        getAppKeyHash();

        //타이틀바 길이 설정
        LinearLayout.LayoutParams titleParams = (LinearLayout.LayoutParams) layoutWriteTitle.getLayoutParams();
        titleParams.height = Main2Activity.targetSize;

        Intent intent = getIntent();
        tag = intent.getStringExtra("tag");
        if (tag.equals("EDIT")) {
            pickedDatestr = intent.getStringExtra("editDateStr");
            editTime = intent.getStringExtra("editTimeStr");
            diaryEditIndex = intent.getIntExtra("editIndex", 0);

        } else {
            pickedDatestr = intent.getStringExtra("pickedDateStr");
        }

        //메뉴 리스트뷰 준비
        menuList = new ArrayList<String>(); //메뉴 스트링 동적배열 생성
        menuadapter = new ListItemAdapter(this, R.layout.list_layout, menuList, new ListItemAdapter.ListBtnListner() {  // 메뉴리스트어댑터 생성(생성시 리스트내부삭제버튼틀릭에 들어갈 이벤트 구현
            @Override
            public void onListBtnCLick(int position) {
                menuList.remove(position);
                menuadapter.notifyDataSetChanged();
                setListViewHeightBasedOnChildren(listviewmenu, menuadapter);
            }
        });
        listviewmenu = (ListView) findViewById(R.id.listMenuItem);
        listviewmenu.setAdapter(menuadapter);


        //스피너 식사타입 준비
        String[] typelist = {"아침식사", "점심식사", "저녁식사", "간식", "기타"};

        //함께먹은사람 리스트뷰 준비
        peopleList = new ArrayList<String>();
        peopleadapter = new ListItemAdapter(this, R.layout.list_layout, peopleList, new ListItemAdapter.ListBtnListner() {
            @Override
            public void onListBtnCLick(int position) {
                peopleList.remove(position);
                peopleadapter.notifyDataSetChanged();
                setListViewHeightBasedOnChildren(listViewPeople, peopleadapter);
            }
        });
        listViewPeople = (ListView) findViewById(R.id.listViewPeople);
        listViewPeople.setAdapter(peopleadapter);


        ////캘린더.타임피커 보이기 , 가리기/////////////////////
       /* tvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (calView.getVisibility() == View.GONE) {
                    tvDate.requestFocus();
                } else {
                    tvDate.clearFocus();
                }
            }
        });
        tvDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    calViewFrame.setVisibility(View.VISIBLE);
                    tPicker.setVisibility(View.GONE);
                } else {
                    calViewFrame.setVisibility(View.GONE);
                    if (tPicker.hasFocus()) {
                        tPicker.setVisibility(View.VISIBLE);
                    } else {
                        tPicker.setVisibility(View.GONE);
                    }
                }
            }
        });
        tvTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tPicker.getVisibility() == View.GONE) {
                    tvTime.requestFocus();
                } else {
                    tvTime.clearFocus();
                }
            }
        });
        tvTime.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    calViewFrame.setVisibility(View.GONE);
                    tPicker.setVisibility(View.VISIBLE);
                } else {
                    tPicker.setVisibility(View.GONE);
                    if (tvDate.hasFocus()) {
                        calViewFrame.setVisibility(View.VISIBLE);
                    } else {
                        calViewFrame.setVisibility(View.GONE);
                    }
                }
            }
        });*/
        dateDialogView = (View) View.inflate(WriteActivity.this, R.layout.write_date_dialog, null);
        AlertDialog.Builder dateDialogBuilder = new AlertDialog.Builder(this);
        dateDialogBuilder.setTitle("날짜를 선택해주세요.");
        dateDialogBuilder.setView(dateDialogView);
        calView = (CalendarView) dateDialogView.findViewById(R.id.calView);
        dateDialogBuilder.setNegativeButton("취소", null);
        dateDialog = dateDialogBuilder.create();

        timeDialogView = (View) View.inflate(WriteActivity.this, R.layout.write_time_dialog, null);
        AlertDialog.Builder timeDialogBuilder = new AlertDialog.Builder(this);
        timeDialogBuilder.setTitle("시간을 선택해주세요");
        timeDialogBuilder.setView(timeDialogView);
        tPicker = (TimePicker) timeDialogView.findViewById(R.id.tPicker);
        timeDialogBuilder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setTvTime();
            }
        });
        timeDialogBuilder.setNegativeButton("취소", null);
        timeDialog = timeDialogBuilder.create();

/////캘린더,타임피커 설정
        //날짜와 시간 디폴트 세팅
        int dateTmp = Integer.parseInt(pickedDatestr.substring(10, 12));
        int monthTmp = Integer.parseInt(pickedDatestr.substring(6, 8));
        int yearTmp = Integer.parseInt(pickedDatestr.substring(0, 4));
        Calendar caltmp = Calendar.getInstance();
        caltmp.set(yearTmp, monthTmp - 1, dateTmp);
        calView.setDate(caltmp.getTimeInMillis());
        Calendar todaytmp = Calendar.getInstance();
        Long todayStrTmp = todaytmp.getTimeInMillis();
        calView.setMaxDate(todayStrTmp);
        Log.d("todayStrTmp", "todayStrTmp" + todayStrTmp);

        tvDate.setText(pickedDatestr);


        Date currentTime = Calendar.getInstance().getTime();
        tvTime.setText(timeFormat.format(currentTime));

        calView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                pickedcalendar = Calendar.getInstance();
                pickedcalendar.set(year, month, dayOfMonth);
                pickedDate = new Date(year - 1900, month, dayOfMonth);
                dateFormat.format(pickedDate);
                dayofWeek = pickedcalendar.get(Calendar.DAY_OF_WEEK);
//                dateDialogBuilder.setTitle(dateFormat.format(pickedDate) + " " + getDayofWeek(dayofWeek));
                tvDate.setText(dateFormat.format(pickedDate) + " " + getDayofWeek(dayofWeek));
                dateDialog.dismiss();
            }
        });


        tPicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                pTime = new Time(hourOfDay, minute, 0);
            }
        });

        tvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateDialog.show();
            }
        });
        tvTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tmpAMPMtext = tvTime.getText().toString().substring(0, 2);
                int tmpHtext = Integer.parseInt(tvTime.getText().toString().substring(3, 5));
                int tmpMtext = Integer.parseInt(tvTime.getText().toString().substring(6));
                if (tmpAMPMtext.equals("오후") || tmpAMPMtext.equals("PM")) {
                    tmpHtext += 12;
                }
                tPicker.setCurrentHour(tmpHtext);
                tPicker.setCurrentMinute(tmpMtext);

                timeDialog.show();
            }
        });


        //////메뉴////////////////////////////////
        edtMenu.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    menuadapter.notifyDataSetChanged();
                    edtMenu.setText("");
                    ivMenuDone.setVisibility(View.VISIBLE);
                    listviewmenu.setVisibility(View.VISIBLE);
                    Log.d("here", "now: edtMenu.setOnFocusChangeListener: true");

                } else {
                    if (!edtMenu.getText().toString().trim().equals("")) {
                        ivMenuDone.callOnClick();
                    }
                    ivMenuDone.setVisibility(View.INVISIBLE);
                    if (!menuList.isEmpty()) {
                        String menustr = "";
                        for (int i = 0; i < menuList.size(); i++) {
                            menustr += menuList.get(i);
                            if (i != (menuList.size() - 1)) {
                                menustr += ",";
                            }
                        }
                        edtMenu.setText(menustr);
                        listviewmenu.setVisibility(View.GONE);
                        Log.d("here", "now: edtMenu.setOnFocusChangeListener: false");
                    }
                }

            }
        });
        edtMenu.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    ivMenuDone.callOnClick();
                    Log.d("here", "now: edtMenu.setOnKeyListener in");
                    return true;
                }
                return false;

            }
        });

        ivMenuDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("here", "now:  ivMenuDone.setOnClickListener");
                if (!(edtMenu.getText().toString()).equals("")) {
                    menuList.add(edtMenu.getText().toString());
                    menuadapter.notifyDataSetChanged();
                    setListViewHeightBasedOnChildren(listviewmenu, menuadapter);
                    edtMenu.setText("");
                    Log.d("here", "now:  ivMenuDone.setOnClickListener");

                }
            }
        });


        //////식사타입 선택//////////////////////
        layoutWriteType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                typeDialog.show();
                hideKeyBoard();
            }
        });

        ArrayAdapter<String> typeadapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice, typelist);
//        spnType.setAdapter(typeadapter);
        AlertDialog.Builder builderType = new AlertDialog.Builder(this);
        builderType.setTitle("식사 유형을 선택해주세요");
        builderType.setAdapter(typeadapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String choice = getResources().getString(R.string.breakfast);
                switch (which) {
                    case 0:
                        choice = getResources().getString(R.string.breakfast);
                        break;
                    case 1:
                        choice = getResources().getString(R.string.lunch);
                        break;
                    case 2:
                        choice = getResources().getString(R.string.dinner);
                        break;
                    case 3:
                        choice = getResources().getString(R.string.snack);
                        break;
                    case 4:
                        choice = getResources().getString(R.string.etc);
                        break;
                }
                tvType.setText(choice);
            }
        });
        typeDialog = builderType.create();


        ////제거행동
        cbRemove.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    remove = 1;
                    if ((removeTxtLoad == null) || (removeTxtLoad.trim().equals(""))) {
                        edtRemove.setVisibility(View.VISIBLE);
                        tvRemove.setVisibility(View.GONE);
                    } else {
                        edtRemove.setText(removeTxtLoad);
                        edtRemove.setVisibility(View.VISIBLE);
                        tvRemove.setVisibility(View.GONE);
                    }
                } else {
                    remove = 0;
                    edtRemove.setVisibility(View.GONE);
                    tvRemove.setVisibility(View.VISIBLE);
                }
            }
        });
        edtRemove.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    hideKeyBoard();//엔터치면 키보드 내리기
//                    Log.d("here","now: edtMenu.setOnKeyListener in");
                    return true;
                }
                return false;

            }
        });


        /////먹은 장소////////////////////////////////

        edtLocation.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    hideKeyBoard();//엔터치면 키보드 내리기
//                    Log.d("here","now: edtMenu.setOnKeyListener in");
                    return true;
                }
                return false;
            }
        });

        Places.initialize(getApplicationContext(), "AIzaSyDEV_ED4RbephVMoH5yk3kDg2az9YFXTRg");
        PlacesClient placesClient = Places.createClient(this);
        fields = Arrays.asList(Place.Field.ID, Place.Field.NAME);
        autocompleteSupportFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));
        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Toast.makeText(getApplicationContext(),"name: "+place.getName()+"/placeID:"+place.getId(),Toast.LENGTH_SHORT).show();
//                tvLocation.setText(place.getName().toString());
            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(getApplicationContext(), "failed: " + status, Toast.LENGTH_SHORT).show();
            }
        });

        tvLocationMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

           /*     Intent placeIntent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields).build(WriteActivity.this);
                startActivityForResult(placeIntent, PLACE_REQUEST);
*/

   /*             Intent placeIntent=new Intent(getApplicationContext(),MapTest.class);
                startActivity(placeIntent);*/

            }
        });




        //////함께 먹은 사람/////////////////////////////
        edtPeople.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    peopleadapter.notifyDataSetChanged();
                    edtPeople.setText("");
                    ivPeopleDone.setVisibility(View.VISIBLE);
                    listViewPeople.setVisibility(View.VISIBLE);
                } else {
                    if (!edtPeople.getText().toString().trim().equals("")) {
                        ivPeopleDone.callOnClick();
                    }
                    ivPeopleDone.setVisibility(View.INVISIBLE);
                    if (!peopleList.isEmpty()) {
                        String peoplestr = "";
                        for (int i = 0; i < peopleList.size(); i++) {
                            peoplestr += peopleList.get(i);
                            if (i != (peopleList.size() - 1)) {
                                peoplestr += ",";
                            }
                        }
                        edtPeople.setText(peoplestr);
                        listViewPeople.setVisibility(View.GONE);
                    }
                }

            }
        });

        edtPeople.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    ivPeopleDone.callOnClick();
                    return true;
                }
                return false;

            }
        });

        ivPeopleDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(edtPeople.getText().toString()).equals("")) {
                    peopleList.add(edtPeople.getText().toString());
                    peopleadapter.notifyDataSetChanged();
                    setListViewHeightBasedOnChildren(listViewPeople, peopleadapter);
                    edtPeople.setText("");
                }
            }
        });


        tvPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, GALLARY_REQUEST);
            }
        });

        //선택한 사진 눌러서 삭제 혹은 변경
        ivMenuPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, GALLARY_REQUEST);
            }
        });

        ivPhotoDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivMenuPhoto.setImageResource(0);
                tvPhoto.setVisibility(View.VISIBLE);
                photoPicLayout.setVisibility(View.GONE);
                menuBitmap = null;
            }
        });


        if (tag.equals("EDIT")) {
            setEditData();
        }
/////////////////////////////////

        ivWriteClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ivWriteSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 더블클릭 방지!!! using threshold of 1000 ms  /
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();


                if (edtMenu.hasFocus() && !edtMenu.getText().toString().trim().equals("")) {  //메뉴 체크 안누르고 적어만 둔 경우 추가하고 진행 저장
                    ivMenuDone.callOnClick();
                }
                if (edtPeople.hasFocus() && !edtPeople.getText().toString().trim().equals("")) { //사람 체크 안누르고 적어만 둔 경우 추가하고 진행 저장
                    ivPeopleDone.callOnClick();
                }


                if (((menuList.size() != 0) && (remove == 0)) || ((menuList.size() != 0) && (remove == 1) && (!edtRemove.getText().toString().trim().equals("")))) {


                    //dateidLong 파이널
                    String datetxt = tvDate.getText().toString();
                    String timetxt = tvTime.getText().toString();

                    String dateidStr = datetxt.substring(0, 4) + datetxt.substring(6, 8) + datetxt.substring(10, 12);
                    if ((timetxt.substring(0, 2).equals("PM")) || (timetxt.substring(0, 2).equals("오후"))) {
                        int htmp = Integer.parseInt(timetxt.substring(3, 5)) + 12;
                        dateidStr += Integer.toString(htmp);
                        dateidStr += timetxt.substring(6);
                    } else if ((timetxt.substring(0, 2).equals("AM")) || (timetxt.substring(0, 2).equals("오전"))) {
                        dateidStr += timetxt.substring(3, 5);
                        dateidStr += timetxt.substring(6);
                    }
                    Long dateidLong = Long.parseLong(dateidStr);


                    String menu = "";
                    for (int i = 0; i < menuList.size(); i++) {
                        menu += menuList.get(i);
                        if (i != (menuList.size() - 1)) {
                            menu += ",";
                        }
                    }  //menu 파이널
                    menu=menu.replace("'","''");


//                    String type=getResources().getString(R.string.breakfast);
                    String type = tvType.getText().toString();

                    //


                    //remove 파이널
                    if (remove == 1) {
                        removeTxt = edtRemove.getText().toString().trim();
                    } else {
                        removeTxt = "";
                    }
                    removeTxt=removeTxt.replace("'","''");

                    String location = edtLocation.getText().toString();
                    if (location.equals("먹은 장소")) {
                        location = "";
                    }
                    location=location.replace("'","''");
                    //location 파이널

                    String people = "";
                    for (int i = 0; i < peopleList.size(); i++) {
                        people += peopleList.get(i);
                        if (i != (peopleList.size() - 1)) {
                            people += ",";
                        }
                    }
                    people=people.replace("'","''");
                    //people 파이널

                    String think = edtThink.getText().toString(); //think 파이널
                    think=think.replace("'","''");
//                    think=replaceTextMark(think);


                    myDBHelper = new MyDBHelper(getApplicationContext());
                    sqlDB = myDBHelper.getWritableDatabase();

                    if (tag.equals("EDIT")) {
                        sqlDB.execSQL("update recoveryTBL set datetxt='" + datetxt + "' where diaryindex=" + diaryEditIndex + ";");
                        sqlDB.execSQL("update recoveryTBL set timetxt='" + timetxt + "' where diaryindex=" + diaryEditIndex + ";");
                        sqlDB.execSQL("update recoveryTBL set menu='" + menu + "' where diaryindex=" + diaryEditIndex + ";");
                        sqlDB.execSQL("update recoveryTBL set type='" + type + "' where diaryindex=" + diaryEditIndex + ";");
                        sqlDB.execSQL("update recoveryTBL set remove='" + remove + "' where diaryindex=" + diaryEditIndex + ";");
                        if (remove == 1) {
                            sqlDB.execSQL("update recoveryTBL set removetxt='" + removeTxt + "' where diaryindex=" + diaryEditIndex + ";");
                        } else {
                            sqlDB.execSQL("update recoveryTBL set removetxt='' where diaryindex=" + diaryEditIndex + ";");
                        }
                        sqlDB.execSQL("update recoveryTBL set location='" + location + "' where diaryindex=" + diaryEditIndex + ";");
                        sqlDB.execSQL("update recoveryTBL set people='" + people + "' where diaryindex=" + diaryEditIndex + ";");
                        sqlDB.execSQL("update recoveryTBL set think='" + think + "' where diaryindex=" + diaryEditIndex + ";");
                        if (menuBitmap != null) {
                            convertedBitmap = resizeBitmap(menuBitmap, Main2Activity.width);
                            picByteArr = getByteArray(convertedBitmap);
                            sqlDB.beginTransaction();
                            SQLiteStatement p = sqlDB.compileStatement("update recoveryTBL set menuphoto =? where diaryindex=" + diaryEditIndex + ";");
                            p.bindBlob(1, picByteArr);
                            p.executeInsert();
                            sqlDB.setTransactionSuccessful();
                            sqlDB.endTransaction();
                        } else {
                            picByteArr = null;
                            sqlDB.execSQL("update recoveryTBL set menuphoto=null where diaryindex=" + diaryEditIndex + ";");
                        }
                        sqlDB.execSQL("update recoveryTBL set dateid='" + dateidLong + "' where diaryindex=" + diaryEditIndex + ";");


                    } else {
                        sqlDB.execSQL("insert into recoveryTBL (dateid,datetxt,timetxt,menu,type,remove,location,people,think)" +
                                " values ('" + dateidLong + "','" + datetxt + "','" + timetxt + "','" + menu + "','" + type + "','" + remove + "','" + location + "','" + people + "','" + think + "');");
                        if (remove == 1) {
                            sqlDB.execSQL("update recoveryTBL set removetxt='" + removeTxt + "' where dateid='" + dateidLong + "';");
                        } else {
                            sqlDB.execSQL("update recoveryTBL set removetxt='' where dateid='" + dateidLong + "';");
                        }
                        if (menuBitmap != null) {
                            convertedBitmap = resizeBitmap(menuBitmap, Main2Activity.width);
                            picByteArr = getByteArray(convertedBitmap);
                            sqlDB.beginTransaction();
                            SQLiteStatement p = sqlDB.compileStatement("update recoveryTBL set menuphoto =? where dateid='" + dateidLong + "';");
                            Log.d("sqlq", "qqwery: " + p);
                            p.bindBlob(1, picByteArr);
                            p.executeInsert();
                            sqlDB.setTransactionSuccessful();
                            sqlDB.endTransaction();

                        } else {
                            picByteArr = null;
                        }
                    }

                    sqlDB.close();
                    myDBHelper.close();

                    //데일리-작석으로 가서 작성 마치고 다시 데일리 열 때 아래에 데일리 액티비티 살아있으면 없애주기 3.
                    if (DailyActivity.dailyActivity!=null){
                        DailyActivity dailyActivity=(DailyActivity)DailyActivity.dailyActivity;
                        dailyActivity.finish();
                    }

                    Intent dailyIntent = new Intent(getApplicationContext(), DailyActivity.class);
                    dailyIntent.putExtra("pickedDateStr", tvDate.getText().toString().trim());
                    startActivity(dailyIntent);
                    finish();  //라이트액티비티는 무조건 끝나면 지우기

                } else if (menuList.size() == 0) {
                    Toast.makeText(getApplicationContext(), "메뉴는 꼭 적어주세요!", Toast.LENGTH_SHORT).show();
                    edtMenu.requestFocus();
                } else if ((remove == 1) && (edtRemove.getText().toString().trim().equals(""))) {
                    Toast.makeText(getApplicationContext(), "제거행동 유형을 적어주세요", Toast.LENGTH_SHORT).show();
                    edtRemove.requestFocus();
                }

            }
        });


    }


    void setEditData() {
        myDBHelper = new MyDBHelper(getApplicationContext());
        sqlDB = myDBHelper.getReadableDatabase();
//        Log.i("check", "date:" + pickedDatestr + "  time:" + editTime);
//        Cursor cursor = sqlDB.rawQuery("select * from recoveryTBL where datetxt = '" + pickedDatestr + "' and timetxt= '" + editTime + "' ;", null);
        Cursor cursor = sqlDB.rawQuery("select * from recoveryTBL where diaryIndex = " + diaryEditIndex + ";", null);
        cursor.moveToFirst();
        String dateidLoad = cursor.getString(0);
        String dateLoad = cursor.getString(1);
        String timeLoad = cursor.getString(2);
        String menuLoad = cursor.getString(3);
        Log.i("check", "menu:" + menuLoad);
        String typeLoad = cursor.getString(4);
        removeTxtLoad = cursor.getString(6);
        String locationLoad = cursor.getString(7);
        String peopleLoad = cursor.getString(8);
        String thinkLoad = cursor.getString(9);
        int removeLoad = cursor.getInt(5);
        byte[] menuPhotoLoad = cursor.getBlob(10);


        cursor.close();
        sqlDB.close();
        myDBHelper.close();

        tvDate.setText(dateLoad);
        tvTime.setText(timeLoad);

        if (!locationLoad.equals("")) {
            edtLocation.setText(locationLoad);  //
        }


        switch (removeLoad) {
            case 0:
                break;
            case 1:
                cbRemove.setChecked(true);
                break;
        }

        String[] menuEdtList = menuLoad.split(",");
        for (int k = 0; k < menuEdtList.length; k++) {
            menuList.add(menuEdtList[k]);
        }
        menuadapter.notifyDataSetChanged();
        setListViewHeightBasedOnChildren(listviewmenu, menuadapter);
        edtMenu.requestFocus();


        if (!peopleLoad.equals("")) {
            String[] peopleEdtList = peopleLoad.split(",");
            for (int i = 0; i < peopleEdtList.length; i++) {
                peopleList.add(peopleEdtList[i]);
            }
            peopleadapter.notifyDataSetChanged();
            edtPeople.requestFocus();
        }

        if (!thinkLoad.equals("")) {
            edtThink.setText(thinkLoad);
        }

        tvType.setText(typeLoad);

        if (menuPhotoLoad != null) {
            Bitmap imageBitmap = BitmapFactory.decodeByteArray(menuPhotoLoad, 0, menuPhotoLoad.length);
            ivMenuPhoto.setImageBitmap(imageBitmap);
            photoPicLayout.setVisibility(View.VISIBLE);
            tvPhoto.setVisibility(View.GONE);

        }
        layoutAll.requestFocus();
    }

    //키보드 내리기
    void hideKeyBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view == null) {
        } else {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            layoutAll.requestFocus();  //카보드 내릴 때 특정부분에 포커스 가있는것 없애기
        }

    }

    //리스트 추가되면 리스트 영역 확장
    public static void setListViewHeightBasedOnChildren(ListView listView, ListItemAdapter listAdapter) {

        listAdapter.notifyDataSetChanged();
        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount()));
        listView.setLayoutParams(params);
        listView.requestLayout();

    }


    /////액티비티 결과 받기
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PLACE_REQUEST) {
/*            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                edtLocation.setText(place.getName().toString());
                Toast.makeText(getApplicationContext(), place.getName().toString() + "가 선택되었습니다", Toast.LENGTH_SHORT).show();
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Toast.makeText(getApplicationContext(), "err status:" + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "장소 선택이 취소되었습니다", Toast.LENGTH_SHORT).show();

            }*/
        } else if (requestCode == GALLARY_REQUEST) {
            if (resultCode == RESULT_OK) {
                try {
                    Uri selectedImage = data.getData();
                    InputStream imageStream = getContentResolver().openInputStream(selectedImage);
                    menuBitmap = BitmapFactory.decodeStream(imageStream);
                    ivMenuPhoto.setImageBitmap(menuBitmap);
                    photoPicLayout.setVisibility(View.VISIBLE);
                    tvPhoto.setVisibility(View.GONE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    static String getDayofWeek(int dayofWeek) {
        String dayofweekStr = "";

        switch (dayofWeek) {
            case Calendar.SUNDAY:
                dayofweekStr = "일요일";
                break;
            case Calendar.MONDAY:
                dayofweekStr = "월요일";
                break;
            case Calendar.TUESDAY:
                dayofweekStr = "화요일";
                break;
            case Calendar.WEDNESDAY:
                dayofweekStr = "수요일";
                break;
            case Calendar.THURSDAY:
                dayofweekStr = "목요일";
                break;
            case Calendar.FRIDAY:
                dayofweekStr = "금요일";
                break;
            case Calendar.SATURDAY:
                dayofweekStr = "토요일";
                break;
        }
        return dayofweekStr;

    }

    Bitmap resizeBitmap(Bitmap b, int maxSize) {
        int width = b.getWidth();
        int height = b.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(b, width, height, true);
    }

    //BLOB(byte[])로 이미지 변환
    byte[] getByteArray(Bitmap b) {
        Bitmap bitmaptest = b;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmaptest.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] data = stream.toByteArray();
        return data;
    }

    private void getAppKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(), 0));
                Log.e("Hashkey", "Hash key" + something);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e("Hashkey", "Hash key fail" + e.toString());
        }
    }

    void setTvTime() {
        tvTime.setText(timeFormat.format(pTime));
    }

}
