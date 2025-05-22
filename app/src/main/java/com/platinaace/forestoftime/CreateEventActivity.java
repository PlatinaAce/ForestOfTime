package com.platinaace.forestoftime;

import android.app.ProgressDialog;
import android.content.Intent;
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
import com.parse.ParseException;

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

        // 1) 달력 날짜 클릭 리스너
        for (int i = 1; i < calendarTable.getChildCount(); i++) {
            TableRow row = (TableRow) calendarTable.getChildAt(i);
            for (int j = 0; j < row.getChildCount(); j++) {
                TextView cell = (TextView) row.getChildAt(j);
                String dayText = cell.getText().toString().trim();
                if (dayText.isEmpty()) continue;

                cell.setOnClickListener(v -> {
                    v.setSelected(!v.isSelected());
                    if (v.isSelected()) selectedDays.add(dayText);
                    else               selectedDays.remove(dayText);
                });
            }
        }

        // 2) Next 버튼
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

            // 3) "YYYY-MM-DD" 포맷 리스트로 변환
            ArrayList<String> isoDates = new ArrayList<>();
            Calendar cal = Calendar.getInstance();
            int year  = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            for (String ds : selectedDays) {
                int d = Integer.parseInt(ds);
                String mm = String.format(Locale.getDefault(), "%02d", month);
                String dd = String.format(Locale.getDefault(), "%02d", d);
                isoDates.add(year + "-" + mm + "-" + dd);
            }

            // 4) 로딩
            ProgressDialog pd = new ProgressDialog(this);
            pd.setMessage("이벤트 생성 중…");
            pd.setCancelable(false);
            pd.show();

            // 5) 파라미터 준비 (createEvent: title:String, dates:List<String>) :contentReference[oaicite:0]{index=0}
            HashMap<String, Object> params = new HashMap<>();
            params.put("title", title);
            params.put("dates", isoDates);

            // 6) Cloud Function 호출
            ParseCloud.callFunctionInBackground(
                    "createEvent",
                    params,
                    new FunctionCallback<Map<String, Object>>() {
                        @Override
                        public void done(Map<String, Object> response, ParseException e) {
                            pd.dismiss();

                            // 네트워크/서버 오류
                            if (e != null) {
                                Toast.makeText(
                                        CreateEventActivity.this,
                                        "이벤트 생성 오류: " + e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                                return;
                            }

                            // success 필드 안전하게 꺼내기
                            Object ok = response.get("success");
                            boolean isSuccess = ok instanceof Boolean
                                    ? (Boolean) ok
                                    : Boolean.parseBoolean(String.valueOf(ok));
                            if (!isSuccess) {
                                Toast.makeText(
                                        CreateEventActivity.this,
                                        "이벤트 생성에 실패했습니다",
                                        Toast.LENGTH_LONG
                                ).show();
                                return;
                            }

                            // event_id, link_code를 문자열로 안전하게 변환
                            String eventId  = String.valueOf(response.get("event_id"));
                            String linkCode = String.valueOf(response.get("link_code"));

                            // 7) 다음 화면으로
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
                    }
            );
        });
    }
}
