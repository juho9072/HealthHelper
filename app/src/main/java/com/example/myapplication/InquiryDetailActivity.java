package com.example.myapplication;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class InquiryDetailActivity extends AppCompatActivity {

    TextView textTitle, textDate, textContent, textAnswer;
    int inquiryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inquiry_detail);

        textTitle = findViewById(R.id.text_detail_title);
        textDate = findViewById(R.id.text_detail_date);
        textContent = findViewById(R.id.text_detail_content);
        textAnswer = findViewById(R.id.text_detail_answer);

        inquiryId = getIntent().getIntExtra("inquiry_id", -1);

        if (inquiryId != -1) {
            loadInquiryDetail();
        } else {
            Toast.makeText(this, "잘못된 접근입니다.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadInquiryDetail() {
        String url = "https://healthhelper.mycafe24.com/get_inquiry_detail.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getBoolean("success")) {
                            JSONObject inquiry = obj.getJSONObject("inquiry");

                            textTitle.setText(inquiry.getString("title"));
                            textDate.setText(inquiry.getString("created_at"));
                            textContent.setText(inquiry.getString("content"));

                            String answer = inquiry.optString("answer", "");
                            if (answer == null || answer.trim().isEmpty() || answer.equalsIgnoreCase("null")) {
                                textAnswer.setText("답변 대기 중입니다.");
                            } else {
                                textAnswer.setText(answer);
                            }

                        } else {
                            Toast.makeText(this, "문의 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "파싱 오류", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "서버 연결 실패", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("id", String.valueOf(inquiryId));
                return map;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}
