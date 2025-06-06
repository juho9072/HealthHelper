package com.example.myapplication;

import android.util.Log;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class FindAccountActivity extends AppCompatActivity {

    private EditText editFindId, editFindPwEmail, editFindPw, editAuthCode;
    private Button btnCheckDup, btnSendAuthPw, btnSubmitFind, btnVerifyAuth;
    private ImageButton btnTogglePw;
    private TextView tvDupResult;
    private boolean isPwVisible = false;
    private RequestQueue queue;
    private String savedAuthCode = ""; // 인증번호 저장용

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_account);

        // View 연결
        editFindId = findViewById(R.id.edit_find_id);
        editFindPwEmail = findViewById(R.id.edit_find_pw_email);
        editFindPw = findViewById(R.id.edit_find_pw);
        editAuthCode = findViewById(R.id.edit_auth_code);

        btnCheckDup = findViewById(R.id.btn_check_duplicate);
        btnSendAuthPw = findViewById(R.id.btn_send_auth_pw);
        btnSubmitFind = findViewById(R.id.btn_submit_find);
        btnTogglePw = findViewById(R.id.btn_toggle_pw);
        btnVerifyAuth = findViewById(R.id.btn_verify_auth);

        tvDupResult = findViewById(R.id.tv_duplicate_result);

        queue = Volley.newRequestQueue(this);

        // 아이디 찾기 기능
        btnCheckDup.setOnClickListener(v -> {
            String email = editFindId.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "이메일을 입력하세요", Toast.LENGTH_SHORT).show();
                return;
            }

            FindIdRequest request = new FindIdRequest(email, response -> {
                try {
                    JSONObject json = new JSONObject(response);
                    boolean success = json.getBoolean("success");

                    if (success) {
                        String userId = json.getString("userID");
                        tvDupResult.setText("당신의 아이디는: " + userId);
                        tvDupResult.setTextColor(getColor(android.R.color.holo_green_dark));
                    } else {
                        tvDupResult.setText("해당 이메일로 가입된 아이디가 없습니다.");
                        tvDupResult.setTextColor(getColor(android.R.color.holo_red_dark));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    tvDupResult.setText("서버 오류가 발생했습니다.");
                }
            });

            queue.add(request);
        });

        // 인증번호 전송 (비밀번호 찾기용)
        btnSendAuthPw.setOnClickListener(v -> {
            String email = editFindPwEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "이메일을 입력하세요", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("AUTH_DEBUG", "전송할 이메일: " + email);

            // 인증번호 요청 (에러 핸들러 포함)
            SendAuthCodeRequest request = new SendAuthCodeRequest(email, response -> {
                try {
                    JSONObject json = new JSONObject(response);
                    boolean success = json.getBoolean("success");
                    if (success) {
                        savedAuthCode = json.getString("code");
                        Log.d("AUTH_CODE", "전송된 인증번호: " + savedAuthCode);
                        Toast.makeText(this, "인증번호가 전송되었습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "등록된 이메일이 아닙니다.", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "서버 응답 오류", Toast.LENGTH_SHORT).show();
                }
            });
            queue.add(request);
        });

        // 인증번호 확인
        btnVerifyAuth.setOnClickListener(v -> {
            String inputCode = editAuthCode.getText().toString().trim();
            if (inputCode.equals(savedAuthCode)) {
                Toast.makeText(this, "인증 성공!", Toast.LENGTH_SHORT).show();
                editFindPw.setEnabled(true); // 인증 성공 시 비밀번호 입력 활성화
            } else {
                Toast.makeText(this, "인증번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        // 비밀번호 보기/숨기기 토글
        btnSubmitFind.setOnClickListener(v -> {
            String code = editAuthCode.getText().toString().trim();
            String newPw = editFindPw.getText().toString().trim();
            String email = editFindPwEmail.getText().toString().trim();  // 인증했던 이메일 사용
            Log.d("DEBUG", "입력 이메일: " + email);
            Log.d("DEBUG", "입력 코드: " + code);
            if (newPw.isEmpty()) {
                Toast.makeText(this, "새 비밀번호를 입력하세요", Toast.LENGTH_SHORT).show();
                return;
            }

            if (email.isEmpty()) {
                Toast.makeText(this, "이메일이 비어 있습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            ChangePasswordRequest request = new ChangePasswordRequest(email,newPw, code , response -> {
                try {
                    Log.d("ChangePasswordDebug", "서버 원시 응답: " + response);
                    JSONObject json = new JSONObject(response);
                    Log.d("ChangePasswordDebug", "서버 응답 전체: " + response);
                    boolean success = json.getBoolean("success");  // 변수에 저장
                    Log.d("DEBUG", "입력 이메일: " + email);
                    Log.d("DEBUG", "입력 코드: " + code);
                    Log.d("ChangePasswordDebug", "서버 success 값: " + success);
                    if (json.getBoolean("success")) {
                        Toast.makeText(this, "비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show();

                        // 비밀번호 변경 후 로그인 화면으로 이동
                        Intent intent = new Intent(FindAccountActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  // 이전 액티비티들을 모두 종료
                        startActivity(intent);
                        finish();  // 현재 액티비티 종료
                    } else {
                        Toast.makeText(this, "비밀번호 변경에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "서버 오류", Toast.LENGTH_SHORT).show();
                }
            });

            queue.add(request);
        });

    }
}