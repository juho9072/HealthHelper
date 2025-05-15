package com.example.myapplication;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

// 너가 만든 Fragment들도
import com.example.myapplication.HomeFragment;
import com.example.myapplication.RoutineFragment;
import com.example.myapplication.RecordFragment;
import com.example.myapplication.MypageFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int id = item.getItemId();
            if (id == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (id == R.id.nav_routine) {
                selectedFragment = new RoutineFragment();
            } else if (id == R.id.nav_record) {
                selectedFragment = new RecordFragment();
            } else if (id == R.id.nav_mypage) {
                selectedFragment = new MypageFragment();
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();

            return true;
        });


        // 처음 실행 시 홈 화면 기본 설정
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
    }
}
