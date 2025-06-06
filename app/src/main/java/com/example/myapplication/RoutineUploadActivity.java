package com.example.myapplication;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class RoutineUploadActivity extends AppCompatActivity {

    private EditText etTitle, etYoutubeLink;
    private Spinner spinnerLevel;
    private Button btnSelectTags;
    private ArrayList<String> selectedTags = new ArrayList<>();

    private final String[] availableTags = {
            "등", "어깨", "하체", "가슴", "전신", "무산소", "유산소", "홈트레이닝", "기초체력", "스트레칭",
            "전면어깨", "측면어깨", "후면어깨", "복근", "비복근", "전완근", "이두", "삼두", "장두", "측두", "단두", "광배근", "중앙승모근", "상부승모근", "하부승모근", "기립근", "대원근", "외복사근", "요골근",
            "대퇴사근", "대퇴이두근", "대둔근", "장요근", "내전근", "하체비복근"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.routine_uproad);

        etTitle = findViewById(R.id.etTitle);
        etYoutubeLink = findViewById(R.id.etYoutubeLink);
        spinnerLevel = findViewById(R.id.spinnerLevel);
        btnSelectTags = findViewById(R.id.btnSelectTags);
        Button btnUpload = findViewById(R.id.btnUpload);

        // 난이도 Spinner 설정
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.level_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLevel.setAdapter(adapter);

        // 태그 선택 버튼 클릭 시 검색 다중 선택 다이얼로그
        btnSelectTags.setOnClickListener(v -> showTagSelectionDialog());

        btnUpload.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String link = etYoutubeLink.getText().toString().trim();
            String level = spinnerLevel.getSelectedItem().toString();
            String tags = String.join(",", selectedTags);

            if (title.isEmpty() || link.isEmpty()) {
                Toast.makeText(this, "제목과 링크를 모두 입력하세요", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> uploadToServer(title, link, level, tags)).start();
        });
    }

    private void showTagSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("태그 검색 및 선택");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        SearchView searchView = new SearchView(this);
        ListView listView = new ListView(this);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, availableTags);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        // 기존 선택 항목 반영
        for (int i = 0; i < availableTags.length; i++) {
            if (selectedTags.contains(availableTags[i])) {
                listView.setItemChecked(i, true);
            }
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });

        layout.addView(searchView);
        layout.addView(listView);

        builder.setView(layout);

        builder.setPositiveButton("확인", (dialog, which) -> {
            selectedTags.clear();
            for (int i = 0; i < listView.getCount(); i++) {
                if (listView.isItemChecked(i)) {
                    selectedTags.add(adapter.getItem(i));
                }
            }
        });

        builder.setNegativeButton("취소", null);
        builder.show();
    }

    private void uploadToServer(String title, String link, String level, String tags) {
        try {
            URL url = new URL("https://healthhelper.mycafe24.com/uproad_routine.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            String data = "title=" + URLEncoder.encode(title, "UTF-8") +
                    "&link=" + URLEncoder.encode(link, "UTF-8") +
                    "&level=" + URLEncoder.encode(level, "UTF-8") +
                    "&tag=" + URLEncoder.encode(tags, "UTF-8");

            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
            writer.write(data);
            writer.flush();
            writer.close();

            int responseCode = conn.getResponseCode();
            runOnUiThread(() -> {
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Toast.makeText(this, "루틴 등록 완료!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "서버 오류 발생", Toast.LENGTH_SHORT).show();
                }
            });

            conn.disconnect();
        } catch (Exception e) {
            runOnUiThread(() ->
                    Toast.makeText(this, "업로드 실패: " + e.getMessage(), Toast.LENGTH_LONG).show());
            e.printStackTrace();
        }
    }
}
