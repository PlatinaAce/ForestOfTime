package com.platinaace.forestoftime;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ShowLinkActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_link);

        // 1) Intent 로부터 link_code 받아오기
        String linkCode = getIntent().getStringExtra("LINK_CODE");
        if (linkCode == null) linkCode = "------";

        // 2) TextView 에 반영
        TextView tvCode = findViewById(R.id.tv_link_code);
        tvCode.setText(linkCode);

        // 3) Next 버튼 클릭 처리 (여기선 CalendarActivity 로 이동 예시)
        Button btnNext = findViewById(R.id.btn_link_next);
        btnNext.setOnClickListener(v -> {
           // Intent intent = new Intent(this, CalendarActivity.class);
            // 만들어진 캘린더 페이지로 이동, 추후에 구현 예정
          //  startActivity(intent);
            finish();
        });
    }
}
