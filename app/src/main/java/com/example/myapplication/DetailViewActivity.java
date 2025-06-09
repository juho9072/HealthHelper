package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetailViewActivity extends AppCompatActivity {
    private Button btnEdit, btnDelete;
    private TextView tvDate, tvWeight, tvMuscle, tvFat, tvScore;
    private RecyclerView rvRoutines;
    private RoutineDetailAdapter adapter;
    private List<Routine> routines = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_view);

        btnDelete = findViewById(R.id.btnDelete);
        btnEdit = findViewById(R.id.btnEdit);
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(DetailViewActivity.this, RoutineInputActivity.class);
            String selectedDate = getIntent().getStringExtra("selectedDate");
            intent.putExtra("selectedDate", selectedDate);
            startActivity(intent);
            finish();  // 필요하면 상세화면 종료
        });

        btnDelete.setOnClickListener(v -> {
            String selectedDate = getIntent().getStringExtra("selectedDate");
            if (selectedDate == null) return;

            String url = "https://healthhelper.mycafe24.com/delete_record.php";

            RequestQueue queue = Volley.newRequestQueue(DetailViewActivity.this);
            StringRequest request = new StringRequest(Request.Method.POST, url,
                    response -> {
                        // 서버 성공 응답 처리
                        Toast.makeText(DetailViewActivity.this, "삭제 완료", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish(); // 상세 화면 종료하고 이전 화면으로 복귀
                    },
                    error -> Toast.makeText(DetailViewActivity.this, "삭제 실패: " + error.toString(), Toast.LENGTH_SHORT).show()
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("date", selectedDate);
                    return params;
                }
            };

            // 삭제 전에 사용자 확인 다이얼로그 추가 (권장)
            new AlertDialog.Builder(DetailViewActivity.this)
                    .setTitle("삭제 확인")
                    .setMessage(LocalDate.parse(selectedDate)
                            .format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")) + " 기록을 삭제하시겠습니까?")
                    .setPositiveButton("삭제", (dialog, which) -> queue.add(request))
                    .setNegativeButton("취소", null)
                    .show();
        });


        tvDate = findViewById(R.id.tvDate);
        tvWeight = findViewById(R.id.tvWeight);
        tvMuscle = findViewById(R.id.tvMuscle);
        tvFat = findViewById(R.id.tvFat);
        tvScore = findViewById(R.id.tvScore);

        rvRoutines = findViewById(R.id.rvRoutines);
        rvRoutines.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RoutineDetailAdapter(routines);
        rvRoutines.setAdapter(adapter);

        String selectedDate = getIntent().getStringExtra("selectedDate");
        LocalDate date = LocalDate.parse(selectedDate);  // 문자열 → LocalDate 변환
        String formattedDate = date.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 기록"));  // 원하는 포맷 적용
        tvDate.setText(formattedDate);

        loadDetailData(selectedDate);
    }

    private void loadDetailData(String date) {
        String url = "https://healthhelper.mycafe24.com/get_full_record.php?date=" + date;

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);

                        JSONObject inbody = obj.getJSONObject("inbody");
                        tvWeight.setText("체중: " + inbody.getInt("weight") + " kg");
                        tvMuscle.setText("골격근량: " + inbody.getInt("muscle") + " kg");
                        tvFat.setText("체지방량: " + inbody.getInt("fat") + " kg");
                        tvScore.setText("점수: " + inbody.getInt("score"));

                        JSONArray routinesArr = obj.getJSONArray("routines");
                        routines.clear();
                        for (int i = 0; i < routinesArr.length(); i++) {
                            JSONObject routineObj = routinesArr.getJSONObject(i);
                            Routine routine = new Routine(
                                    routineObj.getInt("id"),
                                    routineObj.getString("title"),
                                    "",  // description 비워두기
                                    routineObj.getString("tag"),
                                    routineObj.has("link") ? routineObj.getString("link") : ""  // 유튜브 링크가 있을 경우 가져오기
                            );

                            routines.add(routine);
                        }
                        adapter.notifyDataSetChanged();

                    } catch (Exception e) {
                        Toast.makeText(this, "데이터 파싱 실패", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "데이터 불러오기 실패", Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }
}

