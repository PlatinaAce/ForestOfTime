package com.platinaace.forestoftime;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.util.HashMap;
import java.util.Map;

public class TimeActivity extends AppCompatActivity {
    private static final String TAG = "TimeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time);

        // 1. JoinEventActivity에서 전달받은 이벤트 코드 받기
        String eventCode = getIntent().getStringExtra("event_code");
        Log.d(TAG, "eventCode: " + eventCode);

        // 2. 서버에 이벤트 코드 전송하여 이벤트 정보 조회
        HashMap<String, Object> params = new HashMap<>();
        params.put("link_code", eventCode);

        ParseCloud.callFunctionInBackground("getEventByCode", params, new FunctionCallback<Map<String, Object>>() {
            @Override
            public void done(Map<String, Object> response, ParseException e) {
                if (e == null) {
                    // 성공: 이벤트 정보 출력
                    String eventId = (String) response.get("event_id");
                    String title = (String) response.get("title");

                    Log.d(TAG, "이벤트 조회 성공");
                    Log.d(TAG, "event_id: " + eventId);
                    Log.d(TAG, "title: " + title);

                    // TODO: 여기서 받은 이벤트 정보를 UI나 다음 처리에 사용
                } else {
                    // 실패: 토스트로 사용자에게 알림
                    Log.e(TAG, "이벤트 조회 실패: " + e.getMessage());
                    Toast.makeText(TimeActivity.this, "유효하지 않은 이벤트 코드입니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
