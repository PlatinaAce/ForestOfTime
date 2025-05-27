// TimeActivity.java
package com.platinaace.forestoftime;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class TimeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time);

        // TODO: 나중에 여기서 Intent 로 받은 EVENT_ID, LINK_CODE 로
        // 사용자/그룹의 시간 투표 결과를 불러오는 로직을 추가하세요.
    }
}
