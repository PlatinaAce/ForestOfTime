package com.platinaace.forestoftime;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ShowTimeActivity extends AppCompatActivity {
    private static final String TAG = "ShowTimeActivity";

    private TableLayout calendarTable;
    private Button      btnSignIn;
    private String      linkCode;
    // ★ 이벤트 ObjectId를 저장할 필드
    private String      eventId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_time);

        calendarTable = findViewById(R.id.calendar_table);
        btnSignIn     = findViewById(R.id.btn_signin);

        // 1) Intent 로 받은 이벤트 코드
        linkCode = getIntent().getStringExtra("event_code");
        if (linkCode == null) {
            Toast.makeText(this, "이벤트 코드가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2) ① getEventByCode 호출 → ② getEventResults 호출
        Map<String,Object> p1 = new HashMap<>();
        p1.put("link_code", linkCode);
        ParseCloud.callFunctionInBackground("getEventByCode", p1,
                (FunctionCallback<Map<String,Object>>) (resp1, e1) -> {
                    if (e1 != null) {
                        Log.e(TAG, "getEventByCode 실패", e1);
                        Toast.makeText(this, "유효하지 않은 이벤트 코드입니다.", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    // ① 성공: objectId(ObjectId 문자열) 꺼내기
                    eventId = (String) resp1.get("event_id");

                    // ② getEventResults 에 objectId 를 event_id 로 넘겨서 진짜 slots 정보까지 가져오기
                    Map<String,Object> p2 = new HashMap<>();
                    p2.put("event_id", eventId);
                    ParseCloud.callFunctionInBackground("getEventResults", p2,
                            (FunctionCallback<Map<String,Object>>) (resp2, e2) -> {
                                if (e2 != null) {
                                    Log.e(TAG, "getEventResults 실패", e2);
                                    Toast.makeText(this, "이벤트 정보를 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                                    finish();
                                    return;
                                }

                                // 성공: dates 리스트 꺼내서 캘린더 생성
                                @SuppressWarnings("unchecked")
                                List<Map<String,Object>> dates =
                                        (List<Map<String,Object>>) resp2.get("dates");
                                buildCalendarWithAvailability(dates);
                            }
                    );
                }
        );

        btnSignIn.setOnClickListener(v -> {
                       // LoginActivity 로 화면 전환
                              Intent intent = new Intent(ShowTimeActivity.this, LoginActivity.class);
                       // 필요하다면 event_code 도 넘겨줄 수 있습니다
                               intent.putExtra("event_code", linkCode);
                                intent.putExtra("event_id", eventId);
                       startActivity(intent);

            // TODO: 로그인/회원가입 화면으로 이동
        });
    }

    /** 캘린더를 동적으로 생성하며 availability_percentage 에 따라 셀 색 채우기 */
    private void buildCalendarWithAvailability(List<Map<String,Object>> dates) {
        calendarTable.removeAllViews();

        // ① 헤더 행: 빈칸 + 날짜/요일
        TableRow hdr = new TableRow(this);
        hdr.addView(makeCell("", 60));
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd\nEEE", Locale.getDefault());
        for (Map<String,Object> day : dates) {
            Date d = (Date) day.get("date");
            hdr.addView(makeCell(sdf.format(d), 100));
        }
        calendarTable.addView(hdr);


        // buildCalendarWithAvailability 안쪽
        Log.d(TAG, "buildCalendarWithAvailability, days=" + dates.size());
        for (Map<String,Object> day : dates) {
            Log.d(TAG, "  date=" + day.get("date") + ", slots=" + ((List<?>)day.get("slots")).size());
        }

        // ② 시간대별 16 슬롯 (9:00~17:30)
        for (int i = 0; i < 16; i++) {
            TableRow row = new TableRow(this);
            // 시간 라벨
            int hour   = 9 + (i/2);
            int minute = (i%2)*30;
            String ampm = hour < 12 ? "AM" : "PM";
            int h12     = (hour%12 == 0 ? 12 : hour%12);
            String label = String.format(Locale.getDefault(),
                    "%d:%02d %s", h12, minute, ampm);
            row.addView(makeCell(label, 60));

            // 날짜별 가용성 셀
            for (Map<String,Object> day : dates) {
                @SuppressWarnings("unchecked")
                List<Map<String,Object>> slots =
                        (List<Map<String,Object>>) day.get("slots");
                Map<String,Object> slotInfo = slots.get(i);
                int percent = ((Number)slotInfo.get("availability_percentage")).intValue();

                View cell = new View(this);
                TableRow.LayoutParams lp = new TableRow.LayoutParams(
                        dpToPx(100), dpToPx(40)
                );
                lp.setMargins(1,1,1,1);
                cell.setLayoutParams(lp);

                int alpha = 50 + percent*2;
                int color = Color.argb(alpha, 163,194,147);
                cell.setBackgroundColor(color);

                row.addView(cell);
            }
            calendarTable.addView(row);
        }
    }

    /** 공통 셀 생성 헬퍼 (헤더, 시간 라벨) */
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

    /** DP → Pixel 변환 */
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
