package com.jdw.calendar;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Calendar focusCal;
    private GridView gridView;
    private LinearLayout monthTab, weekTab, dayTab;
    private LinearLayout monthTabLine, weekTabLine, dayTabLine;
    private ImageButton prevBtn, nextBtn;
    private FloatingActionButton editBtn;
    private TextView nowTabText;
    private LinearLayout scheduleContent;

    public SQLiteDatabase db;
    private final String dbName = "CalendarDB";

    private enum tabs {
        MONTH,
        WEEK,
        DAY
    }

    private tabs nowTab; // one of {MONTH, WEEK, DAY}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initDB();

        focusCal = Calendar.getInstance();

        gridView = (GridView) findViewById(R.id.gridView);

        scheduleContent = (LinearLayout) findViewById(R.id.scheduleContent);


        nowTabText = (TextView) findViewById(R.id.nowTabText);

        monthTab = (LinearLayout) findViewById(R.id.monthTab);
        weekTab = (LinearLayout) findViewById(R.id.weekTab);
        dayTab = (LinearLayout) findViewById(R.id.dayTab);

        monthTabLine = (LinearLayout) findViewById(R.id.monthTabLine);
        weekTabLine = (LinearLayout) findViewById(R.id.weekTabLine);
        dayTabLine = (LinearLayout) findViewById(R.id.dayTabLine);

        prevBtn = (ImageButton) findViewById(R.id.prevBtn);
        nextBtn = (ImageButton) findViewById(R.id.nextBtn);

        // month tab onclick
        monthTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setMonth();
            }
        });

        // week tab onclick
        weekTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setWeek();
            }
        });

        // day tab onclick
        dayTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDay();
            }
        });

        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nowTab == tabs.MONTH) {
                    int dateCnt = focusCal.get(Calendar.DATE);
                    focusCal.set(Calendar.DATE, 1);
                    focusCal.set(Calendar.DATE, focusCal.get(Calendar.DATE) - 1);
                    dateCnt = Math.min(dateCnt, focusCal.get(Calendar.DATE));
                    focusCal.set(Calendar.DATE, dateCnt);
                    setMonth();
                } else if (nowTab == tabs.WEEK) {
                    focusCal.set(Calendar.DATE, focusCal.get(Calendar.DATE) - 7);
                    setWeek();
                } else if (nowTab == tabs.DAY) {
                    focusCal.set(Calendar.DATE, focusCal.get(Calendar.DATE) - 1);
                    setDay();
                }
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nowTab == tabs.MONTH) {
                    int dateCnt = getMaxDate(focusCal.get(Calendar.YEAR), focusCal.get(Calendar.MONTH) + 1);
                    focusCal.set(Calendar.DATE, focusCal.get(Calendar.DATE) + dateCnt);
                    setMonth();
                } else if (nowTab == tabs.WEEK) {
                    focusCal.set(Calendar.DATE, focusCal.get(Calendar.DATE) + 7);
                    setWeek();
                } else if (nowTab == tabs.DAY) {
                    focusCal.set(Calendar.DATE, focusCal.get(Calendar.DATE) + 1);
                    setDay();
                }
            }
        });

        editBtn = (FloatingActionButton) findViewById(R.id.editBtn);
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra("year", focusCal.get(Calendar.YEAR));
                intent.putExtra("month", focusCal.get(Calendar.MONTH));
                intent.putExtra("date", focusCal.get(Calendar.DATE));
                startActivity(intent);
            }
        });

        // init tab is month
        setMonth();

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (nowTab == tabs.MONTH) {
            setMonth();
        } else if (nowTab == tabs.WEEK) {
            setWeek();
        } else if (nowTab == tabs.DAY) {
            setDay();
        }
    }

    public void setMonth() {
        nowTab = tabs.MONTH;

        monthTabLine.setVisibility(View.VISIBLE);
        weekTabLine.setVisibility(View.INVISIBLE);
        dayTabLine.setVisibility(View.INVISIBLE);

        scheduleContent.removeAllViews();

        // 요일 표시
        final List<String> list = new ArrayList<String>();
        list.add("일");
        list.add("월");
        list.add("화");
        list.add("수");
        list.add("목");
        list.add("금");
        list.add("토");

        // 요일 표시 범위이므로 사용하지 않음
        List<Boolean> hasSchedule = new ArrayList<Boolean>();
        for (int i = 0; i < 7; i++) {
            hasSchedule.add(false);
        }

        Calendar tempCal = Calendar.getInstance();
        tempCal.set(focusCal.get(Calendar.YEAR), focusCal.get(Calendar.MONTH), 1);

        int firstDay = (tempCal.get(Calendar.DAY_OF_WEEK) % 7) - 1;

        // 요일을 맞추기 위한 빈 공간
        for (int i = 0; i < firstDay; i++) {
            list.add("");
            hasSchedule.add(false);
        }

        for (int i = 0; i < focusCal.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
            list.add("" + (i + 1));

            int tempDate = focusCal.get(Calendar.YEAR) * 10000 * 100 +
                    focusCal.get(Calendar.MONTH) * 100 +
                    (i + 1);
            Cursor cursor = db.rawQuery("select * from calendar where date = " + tempDate, null);
            if (cursor.getCount() > 0) {
                hasSchedule.add(true);
            } else {
                hasSchedule.add(false);
            }
        }

        MyAdapter adapter = new MyAdapter(getApplicationContext(), list, hasSchedule);
        adapter.setFocusCal(focusCal);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (position > 6 && !list.get(position).equals("")) {
                    focusCal.set(Calendar.DATE, Integer.parseInt(list.get(position)));
                    Intent intent = new Intent(MainActivity.this, EditActivity.class);
                    intent.putExtra("year", focusCal.get(Calendar.YEAR));
                    intent.putExtra("month", focusCal.get(Calendar.MONTH));
                    intent.putExtra("date", focusCal.get(Calendar.DATE));
                    startActivity(intent);
                }
            }
        });

        setDateInfo();
    }

    public void setWeek() {
        nowTab = tabs.WEEK;

        monthTabLine.setVisibility(View.INVISIBLE);
        weekTabLine.setVisibility(View.VISIBLE);
        dayTabLine.setVisibility(View.INVISIBLE);

        scheduleContent.removeAllViews();

        // 요일 표시
        final List<String> list = new ArrayList<String>();
        list.add("일");
        list.add("월");
        list.add("화");
        list.add("수");
        list.add("목");
        list.add("금");
        list.add("토");

        // 요일 표시 범위이므로 사용하지 않음
        List<Boolean> hasSchedule = new ArrayList<Boolean>();
        for (int i = 0; i < 7; i++) {
            hasSchedule.add(false);
        }

        Calendar tempCal = Calendar.getInstance();
        tempCal.set(focusCal.get(Calendar.YEAR), focusCal.get(Calendar.MONTH),
                focusCal.get(Calendar.DATE) - focusCal.get(Calendar.DAY_OF_WEEK) + 1);

        // first date of week
        Calendar firstDate = Calendar.getInstance();
        firstDate.set(tempCal.get(Calendar.YEAR), tempCal.get(Calendar.MONTH), tempCal.get(Calendar.DATE));

        // set week info
        for (int i = 0; i < 7; i++) {
            list.add("" + tempCal.get(Calendar.DATE));

            int tempDate = tempCal.get(Calendar.YEAR) * 10000 * 100 +
                    tempCal.get(Calendar.MONTH) * 100 +
                    tempCal.get(Calendar.DATE);

            Cursor cursor = db.rawQuery("select * from calendar where date = " + tempDate, null);

            if (cursor.getCount() > 0) {
                hasSchedule.add(true);
            } else {
                hasSchedule.add(false);
            }

            tempCal.set(Calendar.DATE, tempCal.get(Calendar.DATE) + 1);
        }

        MyAdapter adapter = new MyAdapter(getApplicationContext(), list, hasSchedule);
        adapter.setFocusCal(focusCal);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (position > 6) {
                    int dateCnt = position - 6 - focusCal.get(Calendar.DAY_OF_WEEK);
                    focusCal.set(Calendar.DATE, focusCal.get(Calendar.DATE) + dateCnt);
                    Intent intent = new Intent(MainActivity.this, EditActivity.class);
                    intent.putExtra("year", focusCal.get(Calendar.YEAR));
                    intent.putExtra("month", focusCal.get(Calendar.MONTH));
                    intent.putExtra("date", focusCal.get(Calendar.DATE));
                    startActivity(intent);
                }
            }
        });

        // find schedules and set week schedule view
        scheduleContent.removeAllViews();

        tempCal.set(Calendar.DATE, tempCal.get(Calendar.DATE) - 7);

        for (int i = 0; i < 7; i++) {
            TextView daySchedule = new TextView(this);
            TableRow.LayoutParams params = new TableRow.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            daySchedule.setLayoutParams(params);
            daySchedule.setGravity(Gravity.CENTER);

            int tempDate = tempCal.get(Calendar.YEAR) * 10000 * 100 +
                    tempCal.get(Calendar.MONTH) * 100 +
                    tempCal.get(Calendar.DATE);

            Cursor cursor = db.rawQuery("select schedule from calendar where date = " + tempDate, null);

            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                daySchedule.setText(cursor.getString(0));
            } else {
                daySchedule.setText("일정 없음");
            }
            scheduleContent.addView(daySchedule);

            tempCal.set(Calendar.DATE, tempCal.get(Calendar.DATE) + 1);
        }


        setDateInfo();
    }


    public void setDay() {
        nowTab = tabs.DAY;

        monthTabLine.setVisibility(View.INVISIBLE);
        weekTabLine.setVisibility(View.INVISIBLE);
        dayTabLine.setVisibility(View.VISIBLE);

        // 요일 표시
        final List<String> list = new ArrayList<String>();
        list.add("일");
        list.add("월");
        list.add("화");
        list.add("수");
        list.add("목");
        list.add("금");
        list.add("토");

        // 요일 표시 범위이므로 사용하지 않음
        List<Boolean> hasSchedule = new ArrayList<Boolean>();
        for (int i = 0; i < 7; i++) {
            hasSchedule.add(false);
        }

        Calendar tempCal = Calendar.getInstance();
        tempCal.set(focusCal.get(Calendar.YEAR), focusCal.get(Calendar.MONTH),
                focusCal.get(Calendar.DATE) - focusCal.get(Calendar.DAY_OF_WEEK) + 1);

        // set week info
        for (int i = 0; i < 7; i++) {
            list.add("" + tempCal.get(Calendar.DATE));

            int tempDate = tempCal.get(Calendar.YEAR) * 10000 * 100 +
                    tempCal.get(Calendar.MONTH) * 100 +
                    tempCal.get(Calendar.DATE);

            Cursor cursor = db.rawQuery("select * from calendar where date = " + tempDate, null);

            if (cursor.getCount() > 0) {
                hasSchedule.add(true);
            } else {
                hasSchedule.add(false);
            }

            tempCal.set(Calendar.DATE, tempCal.get(Calendar.DATE) + 1);
        }

        MyAdapter adapter = new MyAdapter(getApplicationContext(), list, hasSchedule);
        adapter.setFocusCal(focusCal);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (position > 6) {
                    int dateCnt = position - 6 - focusCal.get(Calendar.DAY_OF_WEEK);
                    if (dateCnt == 0) {
                        Intent intent = new Intent(MainActivity.this, EditActivity.class);
                        intent.putExtra("year", focusCal.get(Calendar.YEAR));
                        intent.putExtra("month", focusCal.get(Calendar.MONTH));
                        intent.putExtra("date", focusCal.get(Calendar.DATE));
                        startActivity(intent);
                    } else {
                        focusCal.set(Calendar.DATE, focusCal.get(Calendar.DATE) + dateCnt);
                        setDay();
                    }
                }
            }
        });


        // find schedule and set day schedule view
        scheduleContent.removeAllViews();

        TextView daySchedule = new TextView(this);
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        daySchedule.setLayoutParams(params);
        daySchedule.setGravity(Gravity.CENTER);

        int focusDate = focusCal.get(Calendar.YEAR) * 10000 * 100 +
                focusCal.get(Calendar.MONTH) * 100 +
                focusCal.get(Calendar.DATE);

        Cursor cursor = db.rawQuery("select schedule from calendar where date = " + focusDate, null);

        String dayScheduleString = focusCal.get(Calendar.YEAR) + "년 " +
                (focusCal.get(Calendar.MONTH) + 1) + "월 " +
                focusCal.get(Calendar.DATE) + "일\n";
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            dayScheduleString += cursor.getString(0);
        } else {
            dayScheduleString += "일정 없음";
        }

        daySchedule.setText(dayScheduleString);

        scheduleContent.addView(daySchedule);

        setDateInfo();
    }

    // 현재 활성화된 날짜 표시
    public void setDateInfo() {
        int year = focusCal.get(Calendar.YEAR);
        int month = focusCal.get(Calendar.MONTH) + 1;
        int date = focusCal.get(Calendar.DATE);
        int dayOfWeek = focusCal.get(Calendar.DAY_OF_WEEK);

        String day = new String();

        if (dayOfWeek == Calendar.SUNDAY) {
            day = "일";
        } else if (dayOfWeek == Calendar.MONDAY) {
            day = "월";
        } else if (dayOfWeek == Calendar.TUESDAY) {
            day = "화";
        } else if (dayOfWeek == Calendar.WEDNESDAY) {
            day = "수";
        } else if (dayOfWeek == Calendar.THURSDAY) {
            day = "목";
        } else if (dayOfWeek == Calendar.FRIDAY) {
            day = "금";
        } else if (dayOfWeek == Calendar.SATURDAY) {
            day = "토";
        }

        nowTabText.setText(year + "-" + month + "-" + date + "-" + day);
        //Log.d("setDateInfo", year + "년 " + month + "월 " + date + "일 " + day + "요일");
    }

    // 각 달의 일 수
    public int getMaxDate(int year, int month) {
        switch (month) {
            case 1:
                return 31;
            case 2:
                if (year % 400 == 0) {
                    return 29;
                } else if (year % 100 == 0) {
                    return 28;
                } else if (year % 4 == 0) {
                    return 29;
                } else {
                    return 28;
                }
            case 3:
                return 31;
            case 4:
                return 30;
            case 5:
                return 31;
            case 6:
                return 30;
            case 7:
                return 31;
            case 8:
                return 31;
            case 9:
                return 30;
            case 10:
                return 31;
            case 11:
                return 30;
            case 12:
                return 31;
        }

        return -1;
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
