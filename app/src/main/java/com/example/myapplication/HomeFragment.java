package com.example.myapplication;
import com.android.volley.toolbox.JsonObjectRequest;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
import androidx.fragment.app.Fragment;
import android.widget.RadioGroup;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;

public class HomeFragment extends Fragment {

    private TextView randomRoutineText, levelRoutineText;
    private Button randomRoutineBtn, levelRoutineBtn;
    private RoutineItem randomRoutine, levelRoutine;
    private RadioGroup goalRadioGroup;

    private TextView noticeContent;
    private final String BASE_URL = "https://healthhelper.mycafe24.com/healthhelper/get_routines.php";

    private final String NOTICE_URL = "https://healthhelper.mycafe24.com/healthhelper/get_notice.php";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 기존 뷰 연결
        randomRoutineText = view.findViewById(R.id.randomRoutineText);
        levelRoutineText = view.findViewById(R.id.levelRoutineText);
        randomRoutineBtn = view.findViewById(R.id.randomRoutineBtn);
        levelRoutineBtn = view.findViewById(R.id.levelRoutineBtn);
        noticeContent = view.findViewById(R.id.noticeContent);

        // 🔽 운동목표 선택 라디오 그룹 연결
        goalRadioGroup = view.findViewById(R.id.goalRadioGroup);

        // 🔽 리스너 설정
        goalRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String goal = "";
            if (checkedId == R.id.goalWeightLoss) goal = "체중감량";
            else if (checkedId == R.id.goalBulkUp) goal = "벌크업";
            else if (checkedId == R.id.goalMaintain) goal = "체중유지";

            SharedPreferences prefs = requireActivity().getSharedPreferences("userPrefs", Context.MODE_PRIVATE);
            prefs.edit().putString("fitnessGoal", goal).apply();


        });

        // 🔽 이전에 저장된 목표 선택값 복원
        SharedPreferences prefs = requireActivity().getSharedPreferences("userPrefs", Context.MODE_PRIVATE);
        String savedGoal = prefs.getString("fitnessGoal", "");
        if (!savedGoal.isEmpty()) {
            if (savedGoal.equals("체중감량")) goalRadioGroup.check(R.id.goalWeightLoss);
            else if (savedGoal.equals("벌크업")) goalRadioGroup.check(R.id.goalBulkUp);
            else if (savedGoal.equals("체중유지")) goalRadioGroup.check(R.id.goalMaintain);
        }

        // 🔽 기존 루틴 불러오기 호출
        loadRandomRoutine();
        loadLevelRoutine("중급자");
        loadNotice();
        return view;
    }


    private void loadRandomRoutine() {
        String url = BASE_URL + "?mode=random";
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    if (response.length() > 0) {
                        JSONObject obj = response.optJSONObject(0);
                        String title = obj.optString("title");
                        String link = obj.optString("link");

                        randomRoutine = new RoutineItem(title, null, link); // 썸네일은 null
                        randomRoutineText.setText(title);

                        randomRoutineBtn.setOnClickListener(v -> {
                            goToRoutineDetail(randomRoutine);
                        });
                    }
                },
                error -> Toast.makeText(getContext(), "랜덤 루틴 불러오기 실패", Toast.LENGTH_SHORT).show()
        );
        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void loadLevelRoutine(String level) {
        String url = BASE_URL + "?mode=random&level=" + level;
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    if (response.length() > 0) {
                        JSONObject obj = response.optJSONObject(0);
                        String title = obj.optString("title");
                        String link = obj.optString("link");

                        // ✅ 중복 검사: 랜덤 추천 루틴과 겹치면 무시하거나 재요청
                        if (randomRoutine != null && randomRoutine.youtubeLink.equals(link)) {
                            // 동일한 루틴이면 다시 요청
                            loadLevelRoutine(level); // 재귀 호출 (단, 무한 루프 방지 필요 시 조건 추가 가능)
                            return;
                        }

                        levelRoutine = new RoutineItem(title, null, link);
                        levelRoutineText.setText(title);
                        levelRoutineBtn.setOnClickListener(v -> goToRoutineDetail(levelRoutine));
                    }
                },
                error -> Toast.makeText(getContext(), "난이도 루틴 불러오기 실패", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void goToRoutineDetail(RoutineItem item) {
        Intent intent = new Intent(getContext(), RoutineDetailActivity.class);
        intent.putExtra("title", item.title);
        intent.putExtra("link", item.youtubeLink);
        startActivity(intent);
    }
    private void loadNotice() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, NOTICE_URL, null,
                response -> {
                    String content = response.optString("content");
                    noticeContent.setText(content);
                },
                error -> {
                    noticeContent.setText("공지사항을 불러오지 못했습니다.");
                }
        );
        Volley.newRequestQueue(requireContext()).add(request);
    }
}