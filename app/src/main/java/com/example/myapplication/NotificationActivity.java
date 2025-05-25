package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;

public class NotificationActivity extends AppCompatActivity {
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        prefs = getSharedPreferences("notification_prefs", MODE_PRIVATE);

        // 스위치 목록 (필요하면 계속 추가)
        Switch switchAll = findViewById(R.id.switch_notification_all);
        Switch switchRestEnd = findViewById(R.id.switch_rest_end);
        Switch switchWorkoutIncomplete = findViewById(R.id.switch_workout_incomplete);
        Switch switchWorkoutSchedule = findViewById(R.id.switch_workout_schedule);
        Switch switchMyPostComment = findViewById(R.id.switch_my_post_comment);
        Switch switchMyPostLike = findViewById(R.id.switch_my_post_like);


        // 1. 스위치 상태 불러와서 반영
        switchAll.setChecked(prefs.getBoolean("all", true));
        switchRestEnd.setChecked(prefs.getBoolean("rest_end", true));
        switchWorkoutIncomplete.setChecked(prefs.getBoolean("workout_incomplete", true));
        switchWorkoutSchedule.setChecked(prefs.getBoolean("workout_schedule", true));
        switchMyPostComment.setChecked(prefs.getBoolean("my_post_comment", true));
        switchMyPostLike.setChecked(prefs.getBoolean("my_post_like", true));


        // 2. 스위치 바뀌면 저장 (각 스위치별로)
        switchAll.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) ->
                prefs.edit().putBoolean("all", isChecked).apply()
        );
        switchRestEnd.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) ->
                prefs.edit().putBoolean("rest_end", isChecked).apply()
        );
        switchWorkoutIncomplete.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) ->
                prefs.edit().putBoolean("workout_incomplete", isChecked).apply()
        );
        switchWorkoutSchedule.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) ->
                prefs.edit().putBoolean("workout_schedule", isChecked).apply()
        );
        switchMyPostComment.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) ->
                prefs.edit().putBoolean("my_post_comment", isChecked).apply()
        );
        switchMyPostLike.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) ->
                prefs.edit().putBoolean("my_post_like", isChecked).apply()
        );


        // (옵션) 전체 알림 끄면 하위 스위치도 모두 OFF
        switchAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("all", isChecked).apply();
            switchRestEnd.setChecked(isChecked);
            switchWorkoutIncomplete.setChecked(isChecked);
            switchWorkoutSchedule.setChecked(isChecked);
            switchMyPostComment.setChecked(isChecked);
            switchMyPostLike.setChecked(isChecked);
        });
    }
}
