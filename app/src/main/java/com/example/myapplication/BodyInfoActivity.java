package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class BodyInfoActivity extends AppCompatActivity {

    private EditText editHeight, editWeight;
    private Button btnSaveBody;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_body_info);

        editHeight = findViewById(R.id.edit_height);
        editWeight = findViewById(R.id.edit_weight);
        btnSaveBody = findViewById(R.id.btn_save_body);

        btnSaveBody.setOnClickListener(v -> {
            String height = editHeight.getText().toString().trim();
            String weight = editWeight.getText().toString().trim();

            if (height.isEmpty() || weight.isEmpty()) {
                Toast.makeText(this, "키와 몸무게를 모두 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences("userInfo", MODE_PRIVATE);
            String userId = prefs.getString("userID", "");
            String fitnessLevel = prefs.getString("fitness_level", "");

            Log.d("저장화면_userID", userId);

            if (userId.isEmpty()) {
                Toast.makeText(this, "로그인 정보 없음", Toast.LENGTH_SHORT).show();
                return;
            }

            String url = "https://healthhelper.mycafe24.com/healthhelper/user_body_info.php";

            RequestQueue queue = Volley.newRequestQueue(this);
            StringRequest request = new StringRequest(Request.Method.POST, url,
                    response -> {
                        Toast.makeText(this, "서버 저장 완료!", Toast.LENGTH_SHORT).show();
                        finish();
                    },
                    error -> {
                        Toast.makeText(this, "서버 저장 실패", Toast.LENGTH_SHORT).show();
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("userID", userId);
                    params.put("height", height);
                    params.put("weight", weight);
                    params.put("fitness_level", fitnessLevel);
                    return params;
                }
            };

            queue.add(request);
        });
    }
}
