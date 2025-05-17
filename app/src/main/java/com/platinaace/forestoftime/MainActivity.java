// MainActivity.java
package com.platinaace.forestoftime;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // start_Button 찾기
        Button startButton = findViewById(R.id.start_Button);//추후에 이벤트 생성 버튼으로 수정 예정

        // 버튼 클릭 리스너 설정
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 버튼 클릭 시 토스트 메시지 출력
                Toast.makeText(MainActivity.this, "시작합니다!", Toast.LENGTH_SHORT).show();

                // 필요하다면 여기서 새로운 액티비티로 이동하는 코드 등을 추가할 수 있습니다.
                // 예시:
                // Intent intent = new Intent(MainActivity.this, NextActivity.class);
                // startActivity(intent);

                Intent intent = new Intent(MainActivity.this, CreateEventActivity.class);
                startActivity(intent); // 유저가 이벤트 생성 버튼 클릭시 캘린더 생성 화면으로 이동



            }
        });
        // MainActivity.java onCreate() 안에
        Button joinBtn = findViewById(R.id.join_Button);
        joinBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, JoinEventActivity.class));
        });

    }
}