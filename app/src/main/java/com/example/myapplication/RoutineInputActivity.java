package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RoutineInputActivity extends AppCompatActivity {

    private String selectedDate;
    private RecyclerView routineRecyclerView;
    private List<Routine> routineList = new ArrayList<>();
    private Set<Integer> selectedRoutineIds = new HashSet<>();
    private RecordRoutineAdapter adapter;
    private Spinner tagSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_input);

        selectedDate = getIntent().getStringExtra("selectedDate");
        LocalDate date = LocalDate.parse(selectedDate);
        String formattedDate = date.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"));

        TextView tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvSelectedDate.setText(formattedDate);

        routineRecyclerView = findViewById(R.id.routineRecyclerView);
        routineRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        tagSpinner = findViewById(R.id.tagSpinner);

        String[] tags = {"전체", "등", "어깨", "가슴", "이두", "삼두", "하체", "코어", "유산소"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_item_selected,
                tags
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tagSpinner.setAdapter(adapter);


        tagSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedTag = parentView.getItemAtPosition(position).toString();
                // adapter가 null인지 체크 후 필터링
                if (adapter != null) {
                    filterRoutineListByTag(selectedTag);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                if (adapter != null) {
                    filterRoutineListByTag("전체");
                }
            }
        });

        loadRoutinesFromServer();
        loadDataForDate();

        EditText etWeight = findViewById(R.id.etWeight);
        EditText etMuscle = findViewById(R.id.etMuscle);
        EditText etFat = findViewById(R.id.etFat);
        EditText etScore = findViewById(R.id.etScore);

        findViewById(R.id.btnSave).setOnClickListener(v -> {
            if (selectedRoutineIds.isEmpty()) {
                Toast.makeText(this, "루틴을 최소 한 개 이상 선택하세요", Toast.LENGTH_SHORT).show();
                return;
            }

            String weight = etWeight.getText().toString().trim();
            String muscle = etMuscle.getText().toString().trim();
            String fat = etFat.getText().toString().trim();
            String score = etScore.getText().toString().trim();

            String routineIdsStr = android.text.TextUtils.join(",", selectedRoutineIds);

            deleteExistingRecordAndSave(selectedDate, routineIdsStr, weight, muscle, fat, score);
        });
    }

    private void loadRoutinesFromServer() {
        String url = "https://healthhelper.mycafe24.com/record_page_get_routines.php";
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        routineList.clear();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            int id = obj.getInt("routine_id");
                            String name = obj.getString("name");
                            String tag = obj.getString("tag");
                            String youtubeLink = obj.getString("link");
                            routineList.add(new Routine(id, name, "", tag, youtubeLink));
                        }
                        selectedRoutineIds.clear();

                        // 루틴 목록과 어댑터 초기화
                        adapter = new RecordRoutineAdapter(routineList, selectedIds -> {
                            selectedRoutineIds = selectedIds;
                            Toast.makeText(this, selectedRoutineIds.size() + "개 루틴 선택됨", Toast.LENGTH_SHORT).show();
                        });

                        routineRecyclerView.setAdapter(adapter);  // 어댑터를 RecyclerView에 설정

                        // 로드된 후 기본 필터링 (선택된 태그로 필터링)
                        if (tagSpinner.getSelectedItemPosition() != 0) {
                            filterRoutineListByTag((String) tagSpinner.getSelectedItem());
                        } else {
                            filterRoutineListByTag("전체");
                        }

                    } catch (Exception e) {
                        Toast.makeText(this, "루틴 파싱 실패", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "루틴 불러오기 실패", Toast.LENGTH_SHORT).show());
        queue.add(request);
    }

    // 루틴 리스트 필터링 함수
    private void filterRoutineListByTag(String tag) {
        if (adapter == null) return; // adapter가 null인 경우 함수 종료

        List<Routine> filteredList;
        if (tag.equals("전체")) {
            filteredList = routineList;
        } else {
            filteredList = new ArrayList<>();
            for (Routine routine : routineList) {
                if (routine.getTag().contains(tag)) {
                    filteredList.add(routine);
                }
            }
        }
        adapter.setRoutineList(filteredList);  // 필터링된 리스트를 어댑터에 전달
    }

    private void loadDataForDate() {
        String url = "https://healthhelper.mycafe24.com/get_full_record.php?date=" + selectedDate;

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.has("weight")) {
                            EditText etWeight = findViewById(R.id.etWeight);
                            EditText etMuscle = findViewById(R.id.etMuscle);
                            EditText etFat = findViewById(R.id.etFat);
                            EditText etScore = findViewById(R.id.etScore);



                            etWeight.setText(obj.getString("weight"));
                            etMuscle.setText(obj.getString("muscle"));
                            etFat.setText(obj.getString("fat"));
                            etScore.setText(obj.getString("score"));

                            if (obj.has("routine_ids")) {
                                JSONArray arr = obj.getJSONArray("routine_ids");
                                selectedRoutineIds.clear();
                                for (int i = 0; i < arr.length(); i++) {
                                    selectedRoutineIds.add(arr.getInt(i));
                                }
                                if (adapter != null) {
                                    adapter.setSelectedRoutineIds(selectedRoutineIds);
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "데이터 파싱 실패", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "데이터 불러오기 실패", Toast.LENGTH_SHORT).show());
        queue.add(request);
    }

    private void deleteExistingRecordAndSave(String date, String routineIdsStr, String weight, String muscle, String fat, String score) {
        String deleteUrl = "https://healthhelper.mycafe24.com/delete_record.php";

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest deleteRequest = new StringRequest(Request.Method.POST, deleteUrl,
                response -> {
                    // 삭제 성공 시 새 데이터 저장
                    saveNewRecord(date, routineIdsStr, weight, muscle, fat, score);
                },
                error -> Toast.makeText(this, "기존 데이터 삭제 실패: " + error.toString(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("date", date);
                return params;
            }
        };

        queue.add(deleteRequest);
    }

    private void saveNewRecord(String date, String routineIdsStr, String weight, String muscle, String fat, String score) {
        String saveUrl = "https://healthhelper.mycafe24.com/insert_inbody_with_routine.php";

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest saveRequest = new StringRequest(Request.Method.POST, saveUrl,
                response -> {
                    Toast.makeText(this, "저장 완료", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                },
                error -> Toast.makeText(this, "저장 실패: " + error.toString(), Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("date", date);
                params.put("routine_ids", routineIdsStr);
                params.put("weight", weight);
                params.put("muscle", muscle);
                params.put("fat", fat);
                params.put("score", score);
                return params;
            }
        };

        queue.add(saveRequest);
    }

}


