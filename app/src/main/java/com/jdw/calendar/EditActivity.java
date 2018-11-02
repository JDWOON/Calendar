package com.jdw.calendar;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Calendar;

public class EditActivity extends AppCompatActivity {

    public Button dateBtn, saveBtn, delBtn;
    public TextView nowEditDate;
    public EditText editSchedule;
    public Calendar focusCal;
    public int selectDate;

    public SQLiteDatabase db;
    private final String dbName = "CalendarDB";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        initDB();

        dateBtn = (Button) findViewById(R.id.dateBtn);
        saveBtn = (Button) findViewById(R.id.saveBtn);
        delBtn = (Button) findViewById(R.id.delBtn);
        nowEditDate = (TextView) findViewById(R.id.nowEditDate);
        editSchedule = (EditText) findViewById(R.id.editSchedule);

        // 날짜 지정
        dateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog dialog = new DatePickerDialog(EditActivity.this, listener,
                        focusCal.get(Calendar.YEAR), focusCal.get(Calendar.MONTH), focusCal.get(Calendar.DATE));
                dialog.show();
            }
        });

        // 스케줄 등록
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String scheduleText = editSchedule.getText().toString();

                Cursor cursor = db.rawQuery("select * from calendar where date = " + selectDate, null);

                if (cursor.getCount() > 0) { // 기존에 스케줄이 있을 경우
                    db.execSQL("update calendar " +
                            "set schedule = '" + scheduleText + "' " +
                            "where date = " + selectDate);
                } else { // 새로 작성하는 경우
                    db.execSQL("insert into calendar(date, schedule) " +
                            "values(" + selectDate + ", '" + scheduleText + "')");
                }

                finish();
            }
        });

        delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.execSQL("delete from calendar " +
                        "where date = " + selectDate);

                finish();
            }
        });

        focusCal = Calendar.getInstance();

        Intent intent = getIntent();
        int getYear = intent.getIntExtra("year", focusCal.get(Calendar.YEAR));
        int getMonth = intent.getIntExtra("month", focusCal.get(Calendar.MONTH));
        int getDate = intent.getIntExtra("date", focusCal.get(Calendar.DATE));

        focusCal.set(getYear, getMonth, getDate);

        setEdit();
    }

    public DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int date) {
            focusCal.set(year, month + 1, date);

            setEdit();
        }
    };

    public void setEdit() {
        int year = focusCal.get(Calendar.YEAR);
        int month = focusCal.get(Calendar.MONTH);
        int date = focusCal.get(Calendar.DATE);

        nowEditDate.setText(year + "년" + (month + 1) + "월" + date + "일");

        selectDate = year * 10000 * 100 + month * 100 + date;
        Cursor cursor = db.rawQuery("select schedule from calendar where date = " + selectDate, null);

        if (cursor.getCount() > 0) { // 스케줄이 있을 경우 내용을 불러옴, 삭제 활성화
            cursor.moveToFirst();
            editSchedule.setText(cursor.getString(0));
            delBtn.setVisibility(View.VISIBLE);
        } else { // 스케줄이 없을 경우 입력창을 초기화, 삭제 비활성화
            editSchedule.setText("");
            delBtn.setVisibility(View.INVISIBLE);
        }
    }

    public void initDB() {
        // create or open DB
        db = openOrCreateDatabase(dbName, MODE_WORLD_WRITEABLE, null);

        // if table is not exist, create table
        db.execSQL("create table if not exists calendar(" +
                "date int PRIMARY KEY," +
                "schedule text);");
    }
}
