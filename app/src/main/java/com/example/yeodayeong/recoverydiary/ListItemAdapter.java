package com.example.yeodayeong.recoverydiary;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

public class ListItemAdapter extends ArrayAdapter  {

    //어댑터 내에 빈 임플리먼츠 정의.어댑터 생성시 구현
    interface ListBtnListner{
        void onListBtnCLick(int position);
    }


    int resourceId;  //커스텀 리스트아이템 뷰 R.layout.list_layout 임시저장
    ListBtnListner listBtnListner;  //어댑터 내에 구현해둔 인터페이스 객체 선언.


    ArrayList<String> list;


    //어댑터 생성시에 필수로 내부버튼클릭하면발생할 이벤트 내용 ListBtnListner객체 구현하여 생성하고, 그 객체를 매개로 받도록 함.
    public ListItemAdapter(Context context, int resource, ArrayList<String> list,ListBtnListner listBtnListner) {
        super(context, resource,list);
        this.list=list;
        this.resourceId=resource;
        this.listBtnListner=listBtnListner;

    }

    //커스텀 리스트 레이아웃 위한 뷰 생성
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
       int pos=position;
       Context context=parent.getContext();

       //생성자에서 레이아웃 아이디 받아 인플레이트하여 뷰로 만ㄷㅁ
       if(convertView==null){  //인플레이트된 뷰가 없다면
           LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
           convertView= inflater.inflate(this.resourceId,parent,false);
       }

        TextView tvListItem=(TextView)convertView.findViewById(R.id.tv_list_item);  //가져온 뷰에서 텍스트뷰 캐스팅
        String listitem= list.get(position);
        tvListItem.setText(listitem);

        ImageButton ivListDel=(ImageButton)convertView.findViewById(R.id.iv_list_item);
        ivListDel.setTag(position);  //캐스팅한 내부 버튼에 인덱스 태그로 달고
        ivListDel.setOnClickListener(new View.OnClickListener() {  /*클릭리스너 구현 >> 생성하면서 구현하여 전달받은 인터페이스 객체 메소드 실행
                                                                          매개로 태그번호 보냄 > 태그번호 해당하는 아이템에 밖에서 메인에서 구현해온 이벤트 실행*/
            @Override
            public void onClick(View v) {
                if(listBtnListner != null) {
                    listBtnListner.onListBtnCLick((int)v.getTag());
                }
            }
        });

        return convertView;

    }

    @Override
    public int getCount() {
        return list.size();
    }

    //어댑터 자체가 온클릭리스너 구현 / 아이템 뷰 인플레이트 할 때 리스트내부버튼 캐스팅하고, 인덱스번호 태그로 붙임
//    @Override
//    public void onClick(View v) {
//        if (this.listBtnListner != null){  //내부버튼 눌리면 메인 액티비티의 온클릭 함수 호출
//            this.listBtnListner.onListBtnCLick((int)v.getTag());
//        }
//
//    }






}
