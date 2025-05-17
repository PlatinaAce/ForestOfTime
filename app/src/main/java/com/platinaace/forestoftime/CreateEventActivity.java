package com.platinaace.forestoftime;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashSet;
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

        // 1) 테이블의 각 셀 날짜(TextView)에 클릭 리스너 달기
        int rowCount = calendarTable.getChildCount();
        for (int i = 1; i < rowCount; i++) {
            TableRow row = (TableRow) calendarTable.getChildAt(i);
            int cellCount = row.getChildCount();
            for (int j = 0; j < cellCount; j++) {
                TextView cell = (TextView) row.getChildAt(j);
                String dayText = cell.getText().toString().trim();
                if (!dayText.isEmpty()) {
                    // 날짜 셀이 비어있지 않을 때만 토글 가능
                    cell.setOnClickListener(v -> {
                        if (selectedDays.contains(dayText)) {
                            selectedDays.remove(dayText);
                            cell.setBackgroundColor(Color.TRANSPARENT);
                        } else {
                            selectedDays.add(dayText);
                            cell.setBackgroundColor(0x8033AA33);  // 반투명 녹색
                        }
                    });
                }
            }
        }

        // 2) Next 버튼 클릭
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

            // 3) 다음 화면으로 넘어가기 (예: 링크 생성 화면)
            Intent intent = new Intent(this, ShowLinkActivity.class);
            intent.putExtra("EVENT_TITLE", title);
            intent.putStringArrayListExtra(
                    "EVENT_DATES",
                    new ArrayList<>(selectedDays)
            );
            startActivity(intent);
            // finish(); // 뒤로가기 방지 시
        });
    }
}
