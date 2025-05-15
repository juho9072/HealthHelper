package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

    private String userGender;
    private AlertDialog dialog;
    private boolean validate = false; // 오타 수정

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        final EditText idText = findViewById(R.id.idText);
        final EditText passwordText = findViewById(R.id.passwordText);
        final EditText emailText = findViewById(R.id.emailText);

        RadioGroup genderGroup = findViewById(R.id.genderGroup);
        int genderGroupID = genderGroup.getCheckedRadioButtonId();
        if (genderGroupID != -1) {
            userGender = ((RadioButton) findViewById(genderGroupID)).getText().toString();
        }

        genderGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            RadioButton genderButton = findViewById(i);
            userGender = genderButton.getText().toString();
        });

        final Button validateButton = findViewById(R.id.validateButton);
        validateButton.setOnClickListener(view -> {
            String userID = idText.getText().toString();
            if (validate) {
                return;
            }
            if (userID.equals("")) {
                dialog = new AlertDialog.Builder(RegisterActivity.this)
                        .setMessage("아이디는 빈 칸일 수 없습니다.")
                        .setPositiveButton("확인", null)
                        .create();
                dialog.show();
                return;
            }

            Response.Listener<String> responseListener = response -> {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");
                    if (success) {
                        dialog = new AlertDialog.Builder(RegisterActivity.this)
                                .setMessage("사용할 수 있는 아이디입니다.")
                                .setPositiveButton("확인", null)
                                .create();
                        dialog.show();
                        idText.setEnabled(false);
                        validate = true;
                        idText.setBackgroundColor(ContextCompat.getColor(RegisterActivity.this, R.color.colorGray));
                        validateButton.setBackgroundColor(ContextCompat.getColor(RegisterActivity.this, R.color.colorGray));
                    } else {
                        dialog = new AlertDialog.Builder(RegisterActivity.this)
                                .setMessage("사용할 수 없는 아이디입니다.")
                                .setNegativeButton("확인", null)
                                .create();
                        dialog.show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };

            ValidateRequest validateRequest = new ValidateRequest(userID, responseListener);
            RequestQueue queue = Volley.newRequestQueue(RegisterActivity.this);
            queue.add(validateRequest);
        });

        Button registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(view -> {
            String userID = idText.getText().toString();
            String userPassword = passwordText.getText().toString();
            String userEmail = emailText.getText().toString();

            if (!validate) {
                dialog = new AlertDialog.Builder(RegisterActivity.this)
                        .setMessage("먼저 중복 체크를 해주세요.")
                        .setNegativeButton("확인", null)
                        .create();
                dialog.show();
                return;
            }

            if (userID.equals("") || userPassword.equals("") || userEmail.equals("") || userGender.equals("")) {
                dialog = new AlertDialog.Builder(RegisterActivity.this)
                        .setMessage("빈 칸 없이 입력해주세요.")
                        .setNegativeButton("확인", null)
                        .create();
                dialog.show();
                return;
            }

            Response.Listener<String> responseListener = response -> {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");
                    if (success) {
                        dialog = new AlertDialog.Builder(RegisterActivity.this)
                                .setMessage("회원 등록에 성공했습니다.")
                                .setPositiveButton("확인", null)
                                .create();
                        dialog.show();
                        finish();
                    } else {
                        dialog = new AlertDialog.Builder(RegisterActivity.this)
                                .setMessage("회원 등록에 실패했습니다.")
                                .setNegativeButton("확인", null)
                                .create();
                        dialog.show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };

            RegisterRequest registerRequest = new RegisterRequest(userID, userPassword, userGender, userEmail, responseListener);
            RequestQueue queue = Volley.newRequestQueue(RegisterActivity.this);
            queue.add(registerRequest);
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }
}