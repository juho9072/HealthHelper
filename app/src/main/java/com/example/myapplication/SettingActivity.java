package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SettingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        TextView textUserId = findViewById(R.id.text_user_id);
        SharedPreferences prefs = getSharedPreferences("userInfo", MODE_PRIVATE);
        String userId = prefs.getString("userID", "");
        textUserId.setText(userId);

        View notificationLayout = findViewById(R.id.layout_notification);
        notificationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingActivity.this, NotificationActivity.class);
                startActivity(intent);
            }
        });

    }
}
