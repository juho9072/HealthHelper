package com.example.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Button goFitnessButton = findViewById(R.id.btn_go_fitness);
        goFitnessButton.setOnClickListener(v -> {
            Intent intent = new Intent(SettingActivity.this, FitnessLevelActivity.class);
            startActivity(intent);
        });

        TextView textUserId = findViewById(R.id.text_user_id);
        SharedPreferences prefs = getSharedPreferences("userInfo", MODE_PRIVATE);
        String userId = prefs.getString("userID", "");
        Log.d("설정화면_userID", userId);
        textUserId.setText(userId);

        TextView textFitnessLevel = findViewById(R.id.text_fitness_level);

        TextView textEmail = findViewById(R.id.text_user_email);
        String userEmail = getSharedPreferences("userInfo", MODE_PRIVATE).getString("userEmail", "이메일 없음");
        textEmail.setText(userEmail);

        reloadUserBodyInfo();

        View notificationLayout = findViewById(R.id.layout_notification);
        notificationLayout.setOnClickListener(v -> {
            Intent intent = new Intent(SettingActivity.this, NotificationActivity.class);
            startActivity(intent);
        });

        LinearLayout layoutBodyInfo = findViewById(R.id.layout_body_info);
        layoutBodyInfo.setOnClickListener(v -> {
            Intent intent = new Intent(SettingActivity.this, BodyInfoActivity.class);
            startActivityForResult(intent, 1001);
        });

        Button btnChangePw = findViewById(R.id.btn_change_idpw);
        btnChangePw.setOnClickListener(v -> {
            Intent intent = new Intent(SettingActivity.this, ChangeIdPwActivity.class);
            startActivity(intent);
        });

        Button btnLeave = findViewById(R.id.btn_leave);
        btnLeave.setOnClickListener(v -> {
            new AlertDialog.Builder(SettingActivity.this)
                    .setTitle("회원 탈퇴")
                    .setMessage("정말로 탈퇴하시겠습니까?\n탈퇴 시 계정 정보가 모두 삭제됩니다.")
                    .setPositiveButton("예", (dialog, which) -> {
                        deleteUserFromServer(userId);
                    })
                    .setNegativeButton("아니오", null)
                    .show();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == RESULT_OK) {
            if (data != null && data.getBooleanExtra("updated", false)) {
                reloadUserBodyInfo();
            }
        }
    }

    private void reloadUserBodyInfo() {
        TextView textBody = findViewById(R.id.text_body);
        TextView textFitnessLevel = findViewById(R.id.text_fitness_level);
        SharedPreferences prefs = getSharedPreferences("userInfo", MODE_PRIVATE);
        String userId = prefs.getString("userID", "");

        String url = "https://healthhelper.mycafe24.com/healthhelper/user_body_info.php";

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("userID확인", "보내는 userID: " + userId);
                    Log.d("서버응답", "response: " + response);

                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            String gender = json.getString("userGender");
                            int height = json.getInt("height_cm");
                            int weight = json.getInt("weight_kg");
                            String fitnessLevel = json.optString("fitness_level", "미설정");

                            String info = gender + " · " + height + "cm · " + weight + "kg";
                            textBody.setText(info);
                            textFitnessLevel.setText(fitnessLevel);
                        } else {
                            textBody.setText("정보 없음");
                            textFitnessLevel.setText("미설정");
                        }
                    } catch (Exception e) {
                        textBody.setText("파싱 오류");
                        textFitnessLevel.setText("미설정");
                        e.printStackTrace();
                    }
                },
                error -> {
                    textBody.setText("불러오기 실패");
                    textFitnessLevel.setText("미설정");
                    error.printStackTrace();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userID", userId);
                return params;
            }
        };
        queue.add(request);
    }

    private void deleteUserFromServer(String userId) {
        String url = "https://healthhelper.mycafe24.com/deleteUser.php";

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("탈퇴응답", "[" + response + "]");
                    try {
                        Gson gson = new Gson();
                        Map<String, Object> result = gson.fromJson(response, Map.class);
                        boolean isSuccess = Boolean.TRUE.equals(result.get("success"));
                        String message = result.get("message").toString();

                        if (isSuccess) {
                            SharedPreferences.Editor editor = getSharedPreferences("userInfo", MODE_PRIVATE).edit();
                            editor.clear();
                            editor.apply();
                            Intent intent = new Intent(SettingActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, "탈퇴 실패: " + message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "서버 응답 파싱 오류:\n" + response, Toast.LENGTH_LONG).show();
                        Log.e("파싱오류", "response=" + response, e);
                    }
                },
                error -> Toast.makeText(this, "서버 통신 에러", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userID", userId);
                return params;
            }
        };
        queue.add(stringRequest);
    }
}
