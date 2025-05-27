package com.platinaace.forestoftime;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class JoinEventActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_event);
        // 여기에 사용자가 입력한 이벤트코드를 가지고 아직 안만들어진 가상의
        // timetable 로 이동, 구현은 간단히 join 버튼에 intent 로
        // Intent intent = new Intent(this, TimeActivity.class); 이걸 사용하면 될거같습니다.
        // 그리고 화면이동하면서 eventcode 를 같이 넘겨주면 됩니다.
        // 넘겨주기만 하면 저희가 받아서 구현할거에요.
    }
}
