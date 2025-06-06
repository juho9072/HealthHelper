package com.example.myapplication;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class RoutineStartActivity extends AppCompatActivity {

    private ListView routineListView;
    private RoutineAdapter adapter;
    private ArrayList<RoutineItem> routineItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_start);

        routineListView = findViewById(R.id.routineListView);
        adapter = new RoutineAdapter(this, routineItems);
        routineListView.setAdapter(adapter);

        loadRoutineData();
    }

    private void loadRoutineData() {
        new Thread(() -> {
            try {
                URL url = new URL("https://healthhelper.mycafe24.com/get_routines.php"); // ← 실제 서버 주소로 교체
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) result.append(line);

                JSONArray jsonArray = new JSONArray(result.toString());

                routineItems.clear();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    String title = obj.getString("title");
                    String link = obj.getString("link");

                    // 유튜브 링크에서 video ID 추출
                    String videoId = extractYoutubeVideoId(link);
                    String thumbnailUrl = "https://img.youtube.com/vi/" + videoId + "/0.jpg";

                    routineItems.add(new RoutineItem(title, thumbnailUrl, link));
                }

                runOnUiThread(() -> adapter.notifyDataSetChanged());

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "불러오기 실패: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private String extractYoutubeVideoId(String url) {
        try {
            if (url.contains("v=")) {
                return url.substring(url.indexOf("v=") + 2).split("&")[0];
            } else if (url.contains("youtu.be/")) {
                return url.substring(url.indexOf("youtu.be/") + 9);
            }
        } catch (Exception ignored) {}
        return "";
    }
}