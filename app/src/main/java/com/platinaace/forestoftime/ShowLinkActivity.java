// ShowLinkActivity.java
package com.platinaace.forestoftime;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ShowLinkActivity extends AppCompatActivity {
    /** CreateEventActivity 에서 넘겨주는 링크 코드 키 */
    public static final String EXTRA_LINK_CODE = "EXTRA_LINK_CODE";
    /** (선택) event_id 가 필요하면 추가할 키 */
    public static final String EXTRA_EVENT_ID  = "EXTRA_EVENT_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_link);

        // 1) Intent 로부터 link_code 받아오기
        String linkCode = getIntent().getStringExtra(EXTRA_LINK_CODE);
        if (linkCode == null || linkCode.isEmpty()) {
            // 안전하게 기본값 세팅
            linkCode = "------";
        }

        // 2) TextView 에 반영
        TextView tvCode = findViewById(R.id.tv_link_code);
        tvCode.setText(linkCode);

        // 3) Next → mainmenu 로 이동
        Button btnNext = findViewById(R.id.btn_link_next);
        btnNext.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);

            startActivity(intent);
            finish();
        });
    }
}
