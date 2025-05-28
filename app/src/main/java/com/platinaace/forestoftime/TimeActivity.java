package com.platinaace.forestoftime;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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

public class TimeActivity extends AppCompatActivity {

    private ArrayList<String> selectedDates;

    private static final int START_HOUR = 9;
    private static final int END_HOUR = 17;
    private static final int ROW_COUNT = END_HOUR - START_HOUR;

    private boolean isDragging = false;
    private boolean toggleState = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time);

        selectedDates = getIntent().getStringArrayListExtra("selectedDates");
        if (selectedDates == null || selectedDates.isEmpty()) {
            Toast.makeText(this, "선택된 날짜가 없습니다", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        Log.d("TimeActivity", "selectedDates = " + selectedDates);

        LinearLayout dateHeader = findViewById(R.id.date_header_container);
        LinearLayout timeLabels = findViewById(R.id.time_label_container);
        GridLayout timeGrid = findViewById(R.id.time_grid);

        // 1. 날짜 헤더
        for (String iso : selectedDates) {
            TextView h = new TextView(this);
            h.setText(dayName(iso) + "\n" + iso.substring(5));
            h.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            h.setTextSize(14);
            h.setPadding(dpToPx(8), dpToPx(12), dpToPx(8), dpToPx(12));
            dateHeader.addView(h);
        }

        // 2. 시간 라벨
        for (int hour = START_HOUR; hour < END_HOUR; hour++) {
            TextView l = new TextView(this);
            l.setText(hourLabel(hour));
            l.setTextSize(11);
            l.setGravity(Gravity.CENTER);
            l.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    dpToPx(40) // ✅ 고정 높이
            ));
            timeLabels.addView(l);
        }

        // 3. 셀 그리드
        int colCount = selectedDates.size();
        timeGrid.setColumnCount(colCount);
        timeGrid.setRowCount(ROW_COUNT);

        ContextThemeWrapper cellCtx = new ContextThemeWrapper(this, R.style.TimeCell);

        for (int row = 0; row < ROW_COUNT; row++) {
            for (int col = 0; col < colCount; col++) {
                TextView cell = new TextView(cellCtx);
                cell.setSelected(false);

                cell.setOnTouchListener((v, e) -> {
                    switch (e.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            isDragging = true;
                            toggleState = !v.isSelected();
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

                GridLayout.LayoutParams lp = new GridLayout.LayoutParams(
                        GridLayout.spec(row), GridLayout.spec(col));
                lp.width = dpToPx(40);   // ✅ 고정 너비
                lp.height = dpToPx(40);  // ✅ 고정 높이
                lp.setMargins(dpToPx(1), dpToPx(1), dpToPx(1), dpToPx(1));
                cell.setLayoutParams(lp);

                timeGrid.addView(cell);
            }
        }
    }

    // 시간 라벨 포맷
    private String hourLabel(int hour24) {
        String ampm = (hour24 < 12) ? "AM" : "PM";
        int h12 = (hour24 == 0 || hour24 == 12) ? 12 : hour24 % 12;
        return String.format(Locale.US, "%d:00 %s", h12, ampm);
    }

    // 요일 포맷
    private String dayName(String iso) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(iso));
            return new SimpleDateFormat("EEE", Locale.US).format(cal.getTime());
        } catch (Exception e) {
            return "";
        }
    }

    // dp 단위를 px로 변환
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
