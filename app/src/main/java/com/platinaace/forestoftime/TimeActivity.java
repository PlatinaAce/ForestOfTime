package com.platinaace.forestoftime;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * When-to-meet-style 시간표 화면
 * ─────────────────────────────
 * • 날짜(열)   : CreateEventActivity 에서 넘어온 selectedDates
 * • 시간(행)   : 09:00 - 16:00  (포함)  → 총 8칸
 * • 셀 스타일 : styles.xml 의  TimeCell  (background = selector)
 * • 드래그/클릭 시   v.setSelected(true/false)  로 색상 토글
 */
public class TimeActivity extends AppCompatActivity {

    /* 날짜 리스트 (yyyy-MM-dd) */
    private ArrayList<String> selectedDates;

    /* 시간 범위 */
    private static final int START_HOUR = 9;   // 09:00 AM
    private static final int END_HOUR   = 17;  // 05:00 PM  → 마지막 행 = 16:00-17:00
    private static final int ROW_COUNT  = END_HOUR - START_HOUR; // 8

    /* 드래그 선택용 플래그 */
    private boolean isDragging  = false;
    private boolean toggleState = true;   // true → 선택 / false → 해제

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time);

        /* ───── 1. 날짜 리스트 받기 ───── */
        selectedDates = getIntent().getStringArrayListExtra("selectedDates");
        if (selectedDates == null || selectedDates.isEmpty()) {
            Toast.makeText(this, "선택된 날짜가 없습니다", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        Log.d("TimeActivity", "selectedDates = " + selectedDates);

        /* ───── 2. 레이아웃 뷰 찾기 ───── */
        LinearLayout dateHeader = findViewById(R.id.date_header_container);
        LinearLayout timeLabels = findViewById(R.id.time_label_container);
        GridLayout   timeGrid   = findViewById(R.id.time_grid);

        /* ───── 3. 헤더(요일+날짜) 채우기 ───── */
        for (String iso : selectedDates) {
            TextView h = new TextView(this);
            h.setText(dayName(iso) + "\n" + iso.substring(5));  // Mon\n06-03
            h.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            h.setTextSize(16);
            h.setPadding(32, 24, 32, 24);
            dateHeader.addView(h);
        }

        /* ───── 4. 좌측 시간 라벨 채우기 ───── */
        for (int hour = START_HOUR; hour < END_HOUR; hour++) {
            TextView l = new TextView(this);
            l.setText(hourLabel(hour));              // “9:00 AM”
            l.setTextSize(16);
            l.setPadding(24, 60, 24, 60);
            timeLabels.addView(l);
        }

        /* ───── 5. GridLayout 크기 설정 ───── */
        int colCount = selectedDates.size();
        timeGrid.setColumnCount(colCount);
        timeGrid.setRowCount(ROW_COUNT);

        /* ───── 6. 셀 생성 & 터치핸들러 ───── */
        ContextThemeWrapper cellCtx =
                new ContextThemeWrapper(this, R.style.TimeCell); // styles.xml

        for (int row = 0; row < ROW_COUNT; row++) {
            for (int col = 0; col < colCount; col++) {
                TextView cell = new TextView(cellCtx);   // TimeCell 스타일 적용
                cell.setSelected(false);                 // 기본 미선택

                cell.setOnTouchListener((v, e) -> {
                    switch (e.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            isDragging  = true;
                            toggleState = !v.isSelected(); // 현재 상태 반전
                            v.setSelected(toggleState);
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            if (isDragging) v.setSelected(toggleState);
                            return true;
                        case MotionEvent.ACTION_UP:
                            isDragging = false;
                            return true;
                    }
                    return false;
                });

                /* 위치 지정 */
                GridLayout.LayoutParams lp =
                        new GridLayout.LayoutParams(
                                GridLayout.spec(row), GridLayout.spec(col));
                cell.setLayoutParams(lp);

                timeGrid.addView(cell);
            }
        }
    }

    /* ─────────────── Formatter ─────────────── */

    /** “9:00 AM” 형식 */
    private String hourLabel(int hour24) {
        String ampm = (hour24 < 12) ? "AM" : "PM";
        int h12 = (hour24 == 0 || hour24 == 12) ? 12 : hour24 % 12;
        return String.format(Locale.US, "%d:00 %s", h12, ampm);
    }

    /** “Mon”“Tue”… 요일 */
    private String dayName(String iso) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(iso));
            return new SimpleDateFormat("EEE", Locale.US).format(cal.getTime());
        } catch (Exception e) {
            return "";
        }
    }
}
