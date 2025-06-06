package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class InquiryActivity extends AppCompatActivity {

    private EditText editTitle, editContent;
    private Button btnSend;
    private String userID, userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inquiry);

        editTitle = findViewById(R.id.edit_inquiry_title);
        editContent = findViewById(R.id.edit_inquiry_content);
        btnSend = findViewById(R.id.btn_send_inquiry);

        SharedPreferences prefs = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        userID = prefs.getString("userID", "unknown");
        userEmail = prefs.getString("userEmail", "unknown");

        btnSend.setOnClickListener(v -> {
            String title = editTitle.getText().toString().trim();
            String content = editContent.getText().toString().trim();

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "제목과 내용을 모두 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            sendInquiry(title, content);
        });
    }

    private void sendInquiry(String title, String content) {
        String url = "https://healthhelper.mycafe24.com/send_inquiry.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Toast.makeText(this, "문의가 성공적으로 전송되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    Toast.makeText(this, "전송 실패: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("userID", userID);
                params.put("email", userEmail);
                params.put("title", title);
                params.put("content", content);
                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}
