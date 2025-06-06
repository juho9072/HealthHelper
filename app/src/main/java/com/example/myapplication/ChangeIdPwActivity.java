package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ChangeIdPwActivity extends AppCompatActivity {
    EditText editId, editNewPw;
    Button btnChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_idpw);

        editId = findViewById(R.id.edit_id);
        editNewPw = findViewById(R.id.edit_new_pw);
        btnChange = findViewById(R.id.btn_change);

        btnChange.setOnClickListener(v -> {
            String inputId = editId.getText().toString().trim();
            String newPw = editNewPw.getText().toString().trim();

            if (inputId.isEmpty() || newPw.isEmpty()) {
                Toast.makeText(this, "아이디와 새 비밀번호를 모두 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            sendChangePwRequest(inputId, newPw);
        });
    }

    private void sendChangePwRequest(String userId, String newPw) {
        String url = "https://healthhelper.mycafe24.com/updatePassword.php";

        Map<String, String> params = new HashMap<>();
        params.put("userID", userId);
        params.put("userPassword", newPw);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getBoolean("success")) {
                            new AlertDialog.Builder(this)
                                    .setMessage("변경에 성공했습니다. 다시 로그인 해주세요.")
                                    .setPositiveButton("확인", (dialog, which) -> {
                                        Intent intent = new Intent(this, LoginActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    }).show();
                        } else {
                            Toast.makeText(this, "변경 실패", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "응답 오류", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "네트워크 오류", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}
