package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

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
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 알림 체크 기능 실행
        checkForInquiryReply();

        // 뷰 연결
        randomRoutineText = view.findViewById(R.id.randomRoutineText);
        levelRoutineText = view.findViewById(R.id.levelRoutineText);
        randomRoutineBtn = view.findViewById(R.id.randomRoutineBtn);
        levelRoutineBtn = view.findViewById(R.id.levelRoutineBtn);
        noticeContent = view.findViewById(R.id.noticeContent);
        goalRadioGroup = view.findViewById(R.id.goalRadioGroup);

        // 목표 선택 리스너 설정
        goalRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String goal = "";
            if (checkedId == R.id.goalWeightLoss) goal = "체중감량";
            else if (checkedId == R.id.goalBulkUp) goal = "벌크업";
            else if (checkedId == R.id.goalMaintain) goal = "체중유지";

            SharedPreferences prefs = requireActivity().getSharedPreferences("userPrefs", Context.MODE_PRIVATE);
            prefs.edit().putString("fitnessGoal", goal).apply();
        });

        // 저장된 목표 복원
        SharedPreferences prefs = requireActivity().getSharedPreferences("userPrefs", Context.MODE_PRIVATE);
        String savedGoal = prefs.getString("fitnessGoal", "");
        if (!savedGoal.isEmpty()) {
            if (savedGoal.equals("체중감량")) goalRadioGroup.check(R.id.goalWeightLoss);
            else if (savedGoal.equals("벌크업")) goalRadioGroup.check(R.id.goalBulkUp);
            else if (savedGoal.equals("체중유지")) goalRadioGroup.check(R.id.goalMaintain);
        }

        // 루틴 및 공지사항 로드
        loadRandomRoutine();
        loadLevelRoutine("중급자");
        loadNotice();

        return view;
    }

    private void checkForInquiryReply() {
        SharedPreferences userPrefs = requireActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String userID = userPrefs.getString("userID", "unknown");

        SharedPreferences notifPrefs = requireActivity().getSharedPreferences("notification_prefs", Context.MODE_PRIVATE);
        boolean isInquiryNotificationEnabled = notifPrefs.getBoolean("inquiry_answer", true);

        if (!isInquiryNotificationEnabled) return;

        String url = "https://healthhelper.mycafe24.com/check_inquiry_reply.php?userID=" + userID;

        RequestQueue queue = Volley.newRequestQueue(requireContext());

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    if (response.contains("reply")) {
                        showNotification(requireContext(), "문의 답변 도착", "문의하신 내용에 대한 답변이 도착했습니다.");
                    }
                },
                Throwable::printStackTrace
        );

        queue.add(request);
    }

    private void showNotification(Context context, String title, String message) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "inquiry_reply_channel";
        String channelName = "문의 답변 알림";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify(1, builder.build());
    }

    private void loadRandomRoutine() {
        String url = BASE_URL + "?mode=random";
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    if (response.length() > 0) {
                        JSONObject obj = response.optJSONObject(0);
                        String title = obj.optString("title");
                        String link = obj.optString("link");

                        randomRoutine = new RoutineItem(title, null, link);
                        randomRoutineText.setText(title);

                        randomRoutineBtn.setOnClickListener(v -> goToRoutineDetail(randomRoutine));
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

                        if (randomRoutine != null && randomRoutine.youtubeLink.equals(link)) {
                            loadLevelRoutine(level); // 재요청
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