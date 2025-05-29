package com.platinaace.forestoftime;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "ForestOfTimePrefs";
    private static final String TAG = "LoginActivity";
    private EditText emailEt, nicknameEt;
    private Button   loginBtn;
    private String   eventId, eventCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        // 1) Intent 에서 이벤트 코드(eventCode)와 이벤트 ID(eventId) 받기
        //    (ShowTimeActivity에서 putExtra("event_id", …) 해두셔야 합니다)
        eventCode = getIntent().getStringExtra("event_code");
        eventId   = getIntent().getStringExtra("event_id");

        // 로그로 출력
        Log.d(TAG, "Received event_code: " + eventCode);
        Log.d(TAG, "Received event_id:   " + eventId);

        if (eventId == null || eventCode == null) {
            Toast.makeText(this, "이벤트 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2) 레이아웃 참조
        emailEt    = findViewById(R.id.email_EditText);
        nicknameEt = findViewById(R.id.nickname_EditText);
        loginBtn   = findViewById(R.id.login_Button);

        // 3) 클릭 리스너
        loginBtn.setOnClickListener(v -> {
            String email    = emailEt.getText().toString().trim();
            String nickname = nicknameEt.getText().toString().trim();
            if (email.isEmpty() || nickname.isEmpty()) {
                Toast.makeText(this, "이메일과 닉네임을 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            String prefKey = "participant_" + eventId;
            String participantId = prefs.getString(prefKey, null);

            if (participantId != null) {
                // ──────────────────────────────────────────────────────────────────────────
                // 이미 가입된 참가자: authenticateParticipant 호출
                // ──────────────────────────────────────────────────────────────────────────
                Map<String, Object> params = new HashMap<>();
                params.put("participant_id", participantId);
                params.put("password", email);
                ParseCloud.callFunctionInBackground("authenticateParticipant", params,
                        new FunctionCallback<Map<String, Object>>() {
                            @Override
                            public void done(Map<String, Object> resp, ParseException e) {
                                if (e == null && Boolean.TRUE.equals(resp.get("success"))) {
                                    // 인증 성공
                                    goToAvailabilityEditor(participantId);
                                } else {
                                    // 인증 실패 → 혹시 비밀번호(이메일) 틀렸을 때 다시 가입 시도
                                    signUp(email, nickname, prefs, prefKey);
                                }
                            }
                        }
                );

            } else {
                // ──────────────────────────────────────────────────────────────────────────
                // 신규 참가자: addParticipant 호출 (회원가입)
                // ──────────────────────────────────────────────────────────────────────────
                signUp(email, nickname, prefs, prefKey);
            }
        });
    }

    /**
     * addParticipant 호출 후 성공 시 SharedPreferences에 ID 저장하고
     * 곧바로 가용성 수정 화면으로 이동
     */
    private void signUp(String email, String nickname,
                        SharedPreferences prefs, String prefKey) {
        Map<String, Object> params = new HashMap<>();
        params.put("event_id", eventId);
        params.put("name",     nickname);
        params.put("password", email);

        ParseCloud.callFunctionInBackground(
                "addParticipant",
                params,
                new FunctionCallback<Map<String, Object>>() {
                    @Override
                    public void done(Map<String, Object> resp, ParseException e) {
                        if (e == null) {
                            // now resp is a Map, so get() works
                            // 에러 전체 스택트레이스를 Logcat에 출력
                            Log.e(TAG, "addParticipant 호출 실패", e);
                            // 추가로 Parse 에러 코드와 메시지도 찍어 두면 좋습니다
                            Log.e(TAG, "addParticipant 에러 코드: " + e.getCode());
                            Log.e(TAG, "addParticipant 에러 메시지: " + e.getMessage());
                            String newParticipantId = (String) resp.get("participant_id");
                            prefs.edit()
                                    .putString(prefKey, newParticipantId)
                                    .apply();
                            goToAvailabilityEditor(newParticipantId);
                        } else {
                            Toast.makeText(LoginActivity.this,
                                            "회원가입/로그인에 실패했습니다: " + e.getMessage(),
                                            Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                }
        );
    }
    /**
     * 실제 가용성 수정 화면(예: EditAvailabilityActivity)으로 넘어가는 메서드
     */
    private void goToAvailabilityEditor(String participantId) {
        Intent it = new Intent(this, EditTimeActivity.class);
        it.putExtra("event_id",        eventId);
        it.putExtra("participant_id",  participantId);
        it.putExtra("event_code",      eventCode);
        startActivity(it);
        finish();
    }
}
