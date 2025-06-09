package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RoutineStartActivity extends AppCompatActivity implements RoutineAdapter.OnRoutineClickListener {

    private RecyclerView routineRecyclerView;
    private RoutineAdapter adapter;
    private List<Routine> routineList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_start); // 여기에 RecyclerView가 있어야 함

        routineRecyclerView = findViewById(R.id.routineRecyclerView);
        routineRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RoutineAdapter(routineList, this);  // this = 클릭 리스너
        routineRecyclerView.setAdapter(adapter);

        loadRoutineData();
    }

    private void loadRoutineData() {
        new Thread(() -> {
            try {
                URL url = new URL("https://healthhelper.mycafe24.com/get_routines.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) result.append(line);

                JSONArray jsonArray = new JSONArray(result.toString());

                routineList.clear();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    String name = obj.getString("title");
                    String tag = "유튜브 루틴";
                    String link = obj.getString("link");
                    String desc = link;

                    routineList.add(new Routine(i, name, desc, tag, link));
                }

                runOnUiThread(() -> adapter.notifyDataSetChanged());

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(this, "불러오기 실패: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    @Override
    public void onRoutineClick(Routine routine) {
        // 클릭 시 동작 정의
        Intent intent = new Intent(this, RoutineDetailActivity.class);
        intent.putExtra("title", routine.getName());
        intent.putExtra("tag", routine.getTag());
        intent.putExtra("desc", routine.getDescription()); // 링크가 들어갈 수도 있음
        startActivity(intent);
    }
}
