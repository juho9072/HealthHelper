package com.example.myapplication;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FitnessLevelActivity extends AppCompatActivity {

    private Spinner spinnerFitness;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fitness_level);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Color.parseColor("#232429"));
        }

        spinnerFitness = findViewById(R.id.spinner_fitness_level);
        btnSave = findViewById(R.id.btn_save_level);

        String[] levels = {"입문자","초보자", "중급자", "상급자","인플루언서"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, R.layout.spinner_item_white, levels) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((TextView) view).setTextColor(Color.WHITE);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                ((TextView) view).setTextColor(Color.WHITE);
                return view;
            }
        };

        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item_dark);
        spinnerFitness.setAdapter(adapter);

        // 저장된 fitness_level 불러와서 spinner 초기화
        SharedPreferences prefs = getSharedPreferences("userInfo", MODE_PRIVATE);
        String savedLevel = prefs.getString("fitness_level", "");
        if (!savedLevel.isEmpty()) {
            int position = adapter.getPosition(savedLevel);
            if (position >= 0) spinnerFitness.setSelection(position);
        }

        btnSave.setOnClickListener(v -> {
            String userId = prefs.getString("userID", "");
            String selectedLevel = spinnerFitness.getSelectedItem().toString();

            if (userId.isEmpty()) {
                Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            saveFitnessLevelToServer(userId, selectedLevel);
        });
    }

    private void saveFitnessLevelToServer(String userId, String fitnessLevel) {
        String url = "https://healthhelper.mycafe24.com/healthhelper/update_fitness_level.php";

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("Volley응답", response);
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            // SharedPreferences에 저장
                            SharedPreferences.Editor editor = getSharedPreferences("userInfo", MODE_PRIVATE).edit();
                            editor.putString("fitness_level", fitnessLevel);
                            editor.apply();

                            Toast.makeText(this, "운동 수준이 저장되었습니다!", Toast.LENGTH_SHORT).show();
                            finish(); // 설정 화면으로 돌아감
                        } else {
                            Toast.makeText(this, "저장 실패: " + json.optString("error", ""), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "응답 파싱 오류", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.e("VolleyError", error.toString());
                    Toast.makeText(this, "운동 수준 저장 실패", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userID", userId);
                params.put("fitness_level", fitnessLevel);
                return params;
            }
        };

        queue.add(stringRequest);
    }
}
