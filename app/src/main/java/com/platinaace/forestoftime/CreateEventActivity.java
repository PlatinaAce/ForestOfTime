package com.platinaace.forestoftime;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CreateEventActivity extends AppCompatActivity {
    private EditText    etName;
    private TableLayout calendarTable;
    private Button      btnNext;
    private Set<String> selectedDays = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        etName        = findViewById(R.id.et_event_name);
        calendarTable = findViewById(R.id.calendar_table);
        btnNext       = findViewById(R.id.btn_next);

        // 1) 날짜 셀마다 리스너 달기
        int rows = calendarTable.getChildCount();
        for (int i = 1; i < calendarTable.getChildCount(); i++) {
            TableRow row = (TableRow) calendarTable.getChildAt(i);
            for (int j = 0; j < row.getChildCount(); j++) {
                TextView cell = (TextView) row.getChildAt(j);
                if (cell.getText().toString().trim().isEmpty()) continue;

                cell.setOnClickListener(v -> {
                    v.setSelected(!v.isSelected());
                    String day = ((TextView)v).getText().toString().trim();
                    if (v.isSelected()) {
                        selectedDays.add(day);
                    } else {
                        selectedDays.remove(day);
                    }
                });
            }
        }

// 2) Next 버튼 클릭 시
        btnNext.setOnClickListener(v -> {
            String title = etName.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "이벤트 이름을 입력해주세요", Toast.LENGTH_SHORT).show();
                return;
            }
            if (selectedDays.isEmpty()) {
                Toast.makeText(this, "하나 이상의 날짜를 선택해주세요", Toast.LENGTH_SHORT).show();
                return;
            }

            // 3) 선택된 day 문자열들 → YYYY-MM-DD 포맷 리스트로 변환
            List<String> isoDates = new ArrayList<>();
            Calendar cal = Calendar.getInstance();
            int year  = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1; // Calendar.MONTH는 0~11
            for (String dayStr : selectedDays) {
                int dayInt = Integer.parseInt(dayStr);
                String mm = String.format(Locale.getDefault(), "%02d", month);
                String dd = String.format(Locale.getDefault(), "%02d", dayInt);
                isoDates.add(year + "-" + mm + "-" + dd);
            }

            // 4) 로딩 다이얼로그
            ProgressDialog pd = new ProgressDialog(this);
            pd.setMessage("이벤트 생성 중…");
            pd.setCancelable(false);
            pd.show();

            // 5) Cloud Function 파라미터 준비
            Map<String,Object> params = new HashMap<>();
            params.put("title", title);
            params.put("dates", isoDates);
            // 만약 로그인 유저가 있다면 아래처럼 추가
            // params.put("createdId", ParseUser.getCurrentUser().getObjectId());

            // 6) createEvent 호출
            ParseCloud.callFunctionInBackground(
                    "createEvent",
                    params,
                    (FunctionCallback<Map<String,Object>>) (response, e) -> {
                        pd.dismiss();
                        if (e != null) {
                            Toast.makeText(
                                    CreateEventActivity.this,
                                    "이벤트 생성 오류: " + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        // 7) 응답에서 event_id, link_code 추출
                        String eventId  = (String) response.get("event_id");
                        String linkCode = (String) response.get("link_code");

                        // 8) ShowLinkActivity 로 이동
                        Intent intent = new Intent(
                                CreateEventActivity.this,
                                ShowLinkActivity.class
                        );
                        intent.putExtra("EXTRA_EVENT_ID",  eventId);
                        intent.putExtra("EXTRA_LINK_CODE", linkCode);
                        intent.putExtra("EXTRA_TITLE",      title);
                        startActivity(intent);
                        finish();
                    }
            );
        });
    }
}
