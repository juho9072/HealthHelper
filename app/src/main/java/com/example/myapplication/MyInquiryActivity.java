package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyInquiryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private InquiryAdapter adapter;
    private List<InquiryItem> inquiryList = new ArrayList<>();
    private String userID;
    private Button btnWrite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_inquiry);

        recyclerView = findViewById(R.id.recycler_inquiry);
        btnWrite = findViewById(R.id.btn_write_inquiry);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        SharedPreferences prefs = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        userID = prefs.getString("userID", "unknown");

        btnWrite.setOnClickListener(v -> {
            Intent intent = new Intent(MyInquiryActivity.this, InquiryActivity.class);
            startActivity(intent);
        });

        loadInquiries();
    }

    private void loadInquiries() {
        String url = "https://healthhelper.mycafe24.com/get_my_inquiries.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getBoolean("success")) {
                            JSONArray arr = obj.getJSONArray("inquiries");
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject item = arr.getJSONObject(i);
                                InquiryItem inquiry = new InquiryItem();
                                inquiry.id = item.getInt("id");
                                inquiry.title = item.getString("title");
                                inquiry.created_at = item.getString("created_at");
                                inquiry.hasAnswer = item.getBoolean("hasAnswer");
                                inquiryList.add(inquiry);
                            }
                            adapter = new InquiryAdapter(this, inquiryList);
                            recyclerView.setAdapter(adapter);
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "파싱 오류 발생", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, "서버 연결 실패", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("userID", userID);
                return map;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }
}
