package com.platinaace.forestoftime;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class JoinEventActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_event); // join_event.xml 연결

        EditText etEventCode = findViewById(R.id.et_event_code);
        Button btnJoin = findViewById(R.id.btn_join_event);

        btnJoin.setOnClickListener(v -> {
            String eventCode = etEventCode.getText().toString().trim();

            if (eventCode.length() == 6) {
                // 6자리 코드면 TimeActivity로 이동하면서 코드 전달
                Intent intent = new Intent(JoinEventActivity.this, TimeActivity.class);
                intent.putExtra("event_code", eventCode);
                startActivity(intent);
            } else {
                Toast.makeText(this, "6자리 이벤트 코드를 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
