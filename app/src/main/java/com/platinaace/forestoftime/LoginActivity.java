package com.platinaace.forestoftime;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

    private EditText etName, etPwd;
    private Button btnLogin;
    private String eventCode, eventObjId;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);

        // ShowTimeActivity 에서 넘어온 파라미터
        eventObjId = getIntent().getStringExtra("event_objectId");
        eventCode  = getIntent().getStringExtra("event_code");
        if (eventObjId == null || eventCode == null) {
            Toast.makeText(this, "이벤트 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        prefs    = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        etName   = findViewById(R.id.name_EditText);
        etPwd    = findViewById(R.id.password_EditText);
        btnLogin = findViewById(R.id.login_Button);

        btnLogin.setOnClickListener(v -> {
            String name     = etName.getText().toString().trim();
            String password = etPwd.getText().toString().trim();
            if (name.isEmpty() || password.isEmpty()) {
                Toast.makeText(this,
                        "Name과 Password를 모두 입력해주세요.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            String prefKey = "participant_" + eventObjId;

            // 호출 파라미터 준비
            Map<String, Object> params = new HashMap<>();
            params.put("event_objectId", eventObjId);
            params.put("name",           name);
            params.put("password",       password);

            // 단일 가입/로그인 함수 호출
            ParseCloud.callFunctionInBackground(
                    "loginOrSignUp",
                    params,
                    (FunctionCallback<Map<String, Object>>) (resp, e) -> {
                        if (e != null) {
                            Toast.makeText(this,
                                    "서버 오류: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        Boolean ok = (Boolean) resp.get("success");
                        if (!Boolean.TRUE.equals(ok)) {
                            // 비밀번호만 틀린 경우
                            String error = (String) resp.get("error");
                            if ("wrong_password".equals(error)) {
                                Toast.makeText(this,
                                        "비밀번호가 틀렸습니다.",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this,
                                        "인증/가입에 실패했습니다.",
                                        Toast.LENGTH_LONG).show();
                            }
                            return;
                        }

                        // 성공: 서버가 넘겨준 Parse ObjectId
                        String participantId = (String) resp.get("id");
                        // prefs에 저장해두면 다음엔 바로 로그인
                        prefs.edit()
                                .putString(prefKey, participantId)
                                .apply();

                        // 가용성 편집 화면으로 이동
                        Intent it = new Intent(this, EditTimeActivity.class);
                        it.putExtra("event_objectId", eventObjId);
                        it.putExtra("participant_id", participantId);
                        it.putExtra("event_code",     eventCode);
                        startActivity(it);
                        finish();
                    }
            );
        });
    }
}
