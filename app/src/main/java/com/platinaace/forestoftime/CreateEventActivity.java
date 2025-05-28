// ✅ CreateEventActivity.java  (ISO-8601 날짜 → When2meet 전달 완성본)
package com.platinaace.forestoftime;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * ‣ 달력에서 날짜를 눌러 ISO-8601 형태(yyyy-MM-dd)로 선택
 * ‣ 선택 날짜를 가로 스크롤에 표시
 * ‣ Next 버튼 → Parse Cloud 호출 + TimeActivity 로 날짜 리스트 전달 가능
 */
public class CreateEventActivity extends AppCompatActivity {

    /* ───── 뷰 ───── */
    private EditText    etName;
    private TableLayout calendarTable;
    private Button      btnNext;
    private LinearLayout dateContainer;

    /* ───── 데이터 ───── */
    private final Set<String>       selectedDays = new HashSet<>(); // ISO 날짜 Set
    private final ArrayList<String> isoDates     = new ArrayList<>(); // 서버·Intent 전달용

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        etName        = findViewById(R.id.et_event_name);
        calendarTable = findViewById(R.id.calendar_table);
        btnNext       = findViewById(R.id.btn_next);
        dateContainer = findViewById(R.id.selected_date_container);

        /* ───── 달력 셀 클릭 리스너 부착 ───── */
        Calendar today = Calendar.getInstance();
        final int year  = today.get(Calendar.YEAR);
        final int month = today.get(Calendar.MONTH) + 1;

        for (int i = 1; i < calendarTable.getChildCount(); i++) {           // 0행 = 요일, 건너뜀
            TableRow row = (TableRow) calendarTable.getChildAt(i);

            for (int j = 0; j < row.getChildCount(); j++) {
                TextView cell = (TextView) row.getChildAt(j);

                /* 셀에 적힌 ‘일’ 숫자 추출 */
                String raw = cell.getText().toString().replace("\n", "").trim();
                if (raw.isEmpty()) continue;                                // 빈칸이면 패스

                /* yyyy-MM-dd ISO 문자열 생성 후 Tag 로 저장 */
                String iso = String.format(Locale.US, "%04d-%02d-%02d",
                        year, month, Integer.parseInt(raw));
                cell.setTag(iso);

                /* 클릭하면 선택/해제 + UI 갱신 */
                cell.setOnClickListener(v -> {
                    boolean now = !v.isSelected();
                    v.setSelected(now);

                    String dateIso = (String) v.getTag();
                    if (now) selectedDays.add(dateIso);
                    else     selectedDays.remove(dateIso);

                    updateSelectedDateDisplay();
                });
            }
        }

        /* ───── Next 버튼 ───── */
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

            /* 리스트 준비 */
            isoDates.clear();
            isoDates.addAll(selectedDays);     // 순서는 중요하지 않아 Set → List 변환
            Intent test = new Intent(CreateEventActivity.this, TimeActivity.class);
            test.putStringArrayListExtra("selectedDates", isoDates);
            startActivity(test);
            /* ───── Parse Cloud 호출 ───── */
            ProgressDialog pd = new ProgressDialog(this);
            pd.setMessage("이벤트 생성 중…");
            pd.setCancelable(false);
            pd.show();

            HashMap<String, Object> params = new HashMap<>();
            params.put("title", title);
            params.put("dates", isoDates);

            ParseCloud.callFunctionInBackground(
                    "createEvent",
                    params,
                    new FunctionCallback<Map<String, Object>>() {
                        @Override public void done(Map<String, Object> res, ParseException e) {
                            pd.dismiss();
                            if (e != null) {
                                Toast.makeText(CreateEventActivity.this,
                                        "이벤트 생성 오류: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                                return;
                            }

                            boolean ok = Boolean.parseBoolean(String.valueOf(res.get("success")));
                            if (!ok) {
                                Toast.makeText(CreateEventActivity.this,
                                        "이벤트 생성에 실패했습니다",
                                        Toast.LENGTH_LONG).show();
                                return;
                            }

                            /* 성공 → 링크 화면 */
                            String eventId  = String.valueOf(res.get("event_id"));
                            String linkCode = String.valueOf(res.get("link_code"));

                            Intent intent = new Intent(CreateEventActivity.this,
                                    ShowLinkActivity.class);
                            intent.putExtra("EXTRA_EVENT_ID",  eventId);
                            intent.putExtra("EXTRA_LINK_CODE", linkCode);
                            intent.putExtra("EXTRA_TITLE",     title);
                            intent.putStringArrayListExtra("selectedDates", isoDates); // ← 시간표로 넘길 때 사용
                            startActivity(intent);
                            finish();
                        }
                    });
        });
    }

    /* 선택한 날짜들을 가로 스크롤에 뿌려 줌 */
    private void updateSelectedDateDisplay() {
        dateContainer.removeAllViews();

        for (String iso : selectedDays) {
            TextView tv = new TextView(this);
            tv.setText(iso);                               // yyyy-MM-dd
            tv.setPadding(24, 16, 24, 16);
            tv.setBackgroundColor(Color.LTGRAY);
            tv.setTextColor(Color.BLACK);
            tv.setTextSize(16);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(8,0,8,0);
            tv.setLayoutParams(lp);

            dateContainer.addView(tv);
        }
    }
}
