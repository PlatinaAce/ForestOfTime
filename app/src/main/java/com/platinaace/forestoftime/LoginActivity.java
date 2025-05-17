package com.platinaace.forestoftime;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page); // login_page.xml과 연결

        Button login = findViewById(R.id.login_Button);
        login.setOnClickListener(v -> {
            // 나중에 여기서 이메일·닉네임 검증/DB 호출 가능
            startActivity(new Intent(this, CreateEventActivity.class));
            // finish(); // 뒤로가기 막을 땐 주석 해제
        });
    }
}
