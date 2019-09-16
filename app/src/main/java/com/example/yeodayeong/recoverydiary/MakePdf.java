package com.example.yeodayeong.recoverydiary;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import static com.itextpdf.text.pdf.PdfWriter.checkPdfIsoConformance;
import static com.itextpdf.text.pdf.PdfWriter.getInstance;

public class MakePdf {
    String fileName, folderName;
    Context context;

    Document document;

    Font koreanFont;
    PdfPTable table;

    public MakePdf(Context context, String fileName, String folderName) {
        this.context = context;
        this.fileName = fileName;
        this.folderName = folderName;
        try {
            getKoreanFont();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    public void addText(String dateline) throws FileNotFoundException, DocumentException {
        document.newPage();
        document.add(new Paragraph(dateline+"\n\n"));
    }

    public void makeDoc() throws FileNotFoundException, DocumentException {
       /* Rectangle pageSize = new Rectangle(297, 420);
        document = new Document(pageSize, 24, 24, 60, 60);*/
        document = new Document();
//        document.setMargins(24,24,36,36);
        //경로 폴더 만들기
        File folderPath = new File(context.getFilesDir(), folderName);
        if (!folderPath.exists()) {
            folderPath.mkdirs();
        }
        File filePath = new File(folderPath, fileName + ".pdf");
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();
    }

    public void createTable()  {

        //테이블 만들기
        table = new PdfPTable(new float[]{2, 6, 2, 2, 2, 6, 4});  //칸 크기 비율 (첫줄 기준)>  총 3개이므로 칸은 3칸
        float docWidth=(document.getPageSize().getWidth());
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(new Paragraph("시간", koreanFont));
        table.addCell(new Paragraph("먹은 것들", koreanFont));
        table.addCell(new Paragraph("제거행동", koreanFont));
        table.addCell(new Paragraph("장소", koreanFont));
        table.addCell(new Paragraph("사람", koreanFont));
        table.addCell(new Paragraph("생각/느낌", koreanFont));
        table.addCell(new Paragraph("사진", koreanFont));
        table.setHeaderRows(1);
        PdfPCell[] cells = table.getRow(0).getCells();  //첫행 (타이틀행) 배열로 가져옴
        for (int i = 0; i < cells.length; i++) {
            cells[i].setBackgroundColor(BaseColor.GRAY);  //타이틀행에 색 넣기
        }

    }

    public void addTableRow(String time,String menu,String remove,String location, String people,String think){  //한 행 추가
        table.addCell(new Paragraph(time , koreanFont));
        table.addCell(new Paragraph(menu , koreanFont));
        table.addCell(new Paragraph(remove , koreanFont));
        table.addCell(new Paragraph(location , koreanFont));
        table.addCell(new Paragraph(people , koreanFont));
        table.addCell(new Paragraph(think, koreanFont));
        table.addCell(new Paragraph("사진 없음" , koreanFont));

    }
    public void addTableRow(String time,String menu,String remove,String location, String people,String think,byte[] menuImage){  //한 행 추가
            table.addCell(new Paragraph(time , koreanFont));
            table.addCell(new Paragraph(menu , koreanFont));
            table.addCell(new Paragraph(remove , koreanFont));
            table.addCell(new Paragraph(location , koreanFont));
            table.addCell(new Paragraph(people , koreanFont));
            table.addCell(new Paragraph(think, koreanFont));
            try {
                Image image=Image.getInstance(menuImage);
                table.addCell(image);
            } catch (BadElementException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

    }

    //만든 도큐먼트에 테이블 작성 (날짜 바뀔 때 마다 1.페이지 넘기기 2.날짜 작성 3. 테이블 작성)
    public void writeTable(String dateLine){
        document.newPage();
        Paragraph dateParagraph=new Paragraph(dateLine,koreanFont);
        dateParagraph.setAlignment(Element.ALIGN_CENTER);
        try {
            document.add(dateParagraph);
            document.add(new Paragraph("\n"));
            document.add(table);
        } catch (DocumentException e) {
            e.printStackTrace();
        }

    }


    public void getKoreanFont() throws IOException, DocumentException {
        koreanFont = FontFactory.getFont("res/font/nanumgothic.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED,6);
    }

}
