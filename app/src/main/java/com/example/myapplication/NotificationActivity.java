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

        Switch switchAll = findViewById(R.id.switch_notification_all);
        Switch switchRestEnd = findViewById(R.id.switch_rest_end);
        Switch switchWorkoutIncomplete = findViewById(R.id.switch_workout_incomplete);
        Switch switchWorkoutSchedule = findViewById(R.id.switch_workout_schedule);
        Switch switchMyPostComment = findViewById(R.id.switch_my_post_comment);
        Switch switchMyPostLike = findViewById(R.id.switch_my_post_like);
        Switch switchInquiryAnswer = findViewById(R.id.switch_inquiry_answer_alert); // 추가

        switchAll.setChecked(prefs.getBoolean("all", true));
        switchRestEnd.setChecked(prefs.getBoolean("rest_end", true));
        switchWorkoutIncomplete.setChecked(prefs.getBoolean("workout_incomplete", true));
        switchWorkoutSchedule.setChecked(prefs.getBoolean("workout_schedule", true));
        switchMyPostComment.setChecked(prefs.getBoolean("my_post_comment", true));
        switchMyPostLike.setChecked(prefs.getBoolean("my_post_like", true));
        switchInquiryAnswer.setChecked(prefs.getBoolean("inquiry_answer", true)); // 추가

        switchRestEnd.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean("rest_end", isChecked).apply()
        );
        switchWorkoutIncomplete.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean("workout_incomplete", isChecked).apply()
        );
        switchWorkoutSchedule.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean("workout_schedule", isChecked).apply()
        );
        switchMyPostComment.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean("my_post_comment", isChecked).apply()
        );
        switchMyPostLike.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean("my_post_like", isChecked).apply()
        );
        switchInquiryAnswer.setOnCheckedChangeListener((buttonView, isChecked) ->
                prefs.edit().putBoolean("inquiry_answer", isChecked).apply()
        );

        switchAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("all", isChecked).apply();

            switchRestEnd.setChecked(isChecked);
            prefs.edit().putBoolean("rest_end", isChecked).apply();

            switchWorkoutIncomplete.setChecked(isChecked);
            prefs.edit().putBoolean("workout_incomplete", isChecked).apply();

            switchWorkoutSchedule.setChecked(isChecked);
            prefs.edit().putBoolean("workout_schedule", isChecked).apply();

            switchMyPostComment.setChecked(isChecked);
            prefs.edit().putBoolean("my_post_comment", isChecked).apply();

            switchMyPostLike.setChecked(isChecked);
            prefs.edit().putBoolean("my_post_like", isChecked).apply();

            switchInquiryAnswer.setChecked(isChecked);
            prefs.edit().putBoolean("inquiry_answer", isChecked).apply();
        });
    }
}
