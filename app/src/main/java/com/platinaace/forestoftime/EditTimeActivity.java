package com.platinaace.forestoftime;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class EditTimeActivity extends AppCompatActivity {
    private static final String TAG = "EditTimeActivity";

    private TableLayout calendarTable;
    private Button btnSave;

    // 인텐트로 받은 값
    private String eventId;
    private String participantId;

    // 선택된 slot_id 집합
    private Set<String> selectedSlotIds = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_time);

        calendarTable = findViewById(R.id.calendar_table);
        btnSave       = findViewById(R.id.btn_saved);

        // 1) Intent 에서 eventId, participantId 받기
        eventId       = getIntent().getStringExtra("event_id");
        participantId = getIntent().getStringExtra("participant_id");
        if (eventId == null || participantId == null) {
            Toast.makeText(this, "이벤트 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2) 서버에서 getEventResults 호출 → 캘린더 구축
        Map<String, Object> params = new HashMap<>();
        params.put("event_id", eventId);

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Loading time slots…");
        pd.setCancelable(false);
        pd.show();

        ParseCloud.callFunctionInBackground("getEventResults", params,
                (FunctionCallback<Map<String, Object>>) (resp, e) -> {
                    pd.dismiss();
                    if (e != null) {
                        Log.e(TAG, "getEventResults 실패", e);
                        Toast.makeText(this, "시간표를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> dates =
                            (List<Map<String, Object>>) resp.get("dates");
                    buildEditableCalendar(dates);
                }
        );

        // 3) Save 클릭 → saveAvailability 호출
        btnSave.setOnClickListener(v -> {
            if (selectedSlotIds.isEmpty()) {
                Toast.makeText(this, "하나 이상의 슬롯을 선택하세요.", Toast.LENGTH_SHORT).show();
                return;
            }
            sendAvailability();
        });
    }

    private void buildEditableCalendar(List<Map<String, Object>> dates) {
        calendarTable.removeAllViews();

        // ① 헤더
        TableRow header = new TableRow(this);
        header.addView(makeCell("", 60));
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd\nEEE", Locale.getDefault());
        for (Map<String, Object> day : dates) {
            String label = sdf.format((Date)day.get("date"));
            header.addView(makeCell(label, 100));
        }
        calendarTable.addView(header);

        // ② 슬롯 행: 16 slots (9:00~17:30)
        for (int i = 0; i < 16; i++) {
            TableRow row = new TableRow(this);
            // 시간 라벨
            int hour   = 9 + (i/2);
            int minute = (i % 2) * 30;
            String ampm = hour < 12 ? "AM" : "PM";
            int h12     = hour % 12 == 0 ? 12 : hour % 12;
            String timeLabel = String.format(Locale.getDefault(), "%d:%02d %s", h12, minute, ampm);
            row.addView(makeCell(timeLabel, 60));

            // 날짜별 셀
            for (Map<String, Object> day : dates) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> slots =
                        (List<Map<String, Object>>) day.get("slots");
                Map<String, Object> slotInfo = slots.get(i);
                String slotId = (String) slotInfo.get("slot_id");

                View cell = new View(this);
                TableRow.LayoutParams lp = new TableRow.LayoutParams(
                        dpToPx(100), dpToPx(40)
                );
                lp.setMargins(1,1,1,1);
                cell.setLayoutParams(lp);
                cell.setBackgroundColor(0xFFEFEFEF);

                // 클릭 시 토글
                cell.setOnClickListener(v -> {
                    if (v.isSelected()) {
                        v.setSelected(false);
                        v.setBackgroundColor(0xFFEFEFEF);
                        selectedSlotIds.remove(slotId);
                    } else {
                        v.setSelected(true);
                        v.setBackgroundColor(0xFFA3C293);
                        selectedSlotIds.add(slotId);
                    }
                });

                row.addView(cell);
            }

            calendarTable.addView(row);
        }
    }

    /** 선택된 slot_id 리스트를 서버에 전송 */
    private void sendAvailability() {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Saving availability…");
        pd.setCancelable(false);
        pd.show();

        List<Map<String, Object>> availList = new ArrayList<>();
        for (String slotId : selectedSlotIds) {
            Map<String, Object> m = new HashMap<>();
            m.put("slot_id", slotId);
            m.put("is_avail", true);
            availList.add(m);
        }

        Map<String, Object> params = new HashMap<>();
        params.put("participant_id", participantId);
        params.put("availabilities", availList);

        ParseCloud.callFunctionInBackground("saveAvailability", params,
                (FunctionCallback<Map<String, Object>>) (resp, e) -> {
                    pd.dismiss();
                    if (e != null) {
                        Log.e(TAG, "saveAvailability 실패", e);
                        Toast.makeText(this, "저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "가용성이 저장되었습니다.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
        );
    }

    /** 공통 셀 생성 헬퍼 */
    private TextView makeCell(String text, int dpWidth) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(12);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(
                dpToPx(dpWidth),
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        lp.setMargins(1,1,1,1);
        tv.setLayoutParams(lp);
        tv.setBackgroundColor(0xFFEFEFEF);
        return tv;
    }

    /** DP→PX 변환 */
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
