//main
package com.example.myapplication;

//import android.widget.Toast; // 테스트용
//import android.util.Log; // 오류확인용


import android.content.SharedPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private AlertDialog dialog; // 🔧 AlertDialog 타입으로 변경

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        TextView registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
            }
        });

        final EditText idText = findViewById(R.id.idText);
        final EditText passwordText = findViewById(R.id.passwordText);
        final Button loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userID = idText.getText().toString();
                String userPassword = passwordText.getText().toString();

                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean("success");

                            if(success) {
                                //오류확인코드
                                //Log.d("서버응답", jsonResponse.toString());

                                //String userEmail = jsonResponse.getString("userEmail");
                               //SharedPreferences prefs = getSharedPreferences("userInfo", MODE_PRIVATE);
                                //SharedPreferences.Editor editor = prefs.edit();
                                //editor.putString("userEmail", userEmail);
                                //editor.apply();

                                SharedPreferences prefs = getSharedPreferences("userInfo", MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString("userID", userID); // <- 로그인에 입력한 userID 저장
                                editor.apply();

                                dialog = new AlertDialog.Builder(LoginActivity.this)
                                        .setMessage("로그인에 성공했습니다.")
                                        .setPositiveButton("확인", null)
                                        .create();
                                dialog.show();

                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                dialog = new AlertDialog.Builder(LoginActivity.this)
                                        .setMessage("아이디 혹은 비밀번호를 다시 확인하세요")
                                        .setNegativeButton("다시 시도", null)
                                        .create();
                                dialog.show();
                            }
                        } catch (Exception e){
                            e.printStackTrace();
                            //Toast.makeText(LoginActivity.this, "이메일 파싱 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show(); // 테스트용
                        }
                    }
                };

                LoginRequest loginRequest = new LoginRequest(userID, userPassword, responseListener);
                RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
                queue.add(loginRequest);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(dialog != null) {
            dialog.dismiss(); // 🔧 dismiss()는 AlertDialog에서만 가능
            dialog = null;
        }
    }
}