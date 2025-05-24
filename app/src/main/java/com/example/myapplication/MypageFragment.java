package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MypageFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_mypage, container, false);

        SharedPreferences prefs = getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String userId = prefs.getString("userID", "아이디 없음");

        TextView userIdTextView = view.findViewById(R.id.text_user_id); // xml에서 id 확인!
        userIdTextView.setText(userId);

        View settingLayout = view.findViewById(R.id.layout_settings);
        settingLayout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingActivity.class);
            startActivity(intent);
        });

        View btnChangeIdPw = view.findViewById(R.id.btn_change_idpw);
        btnChangeIdPw.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ChangeIdPwActivity.class);
            startActivity(intent);
        });

        Button btnLeave = view.findViewById(R.id.btn_leave);
        btnLeave.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(getActivity())
                    .setTitle("회원 탈퇴")
                    .setMessage("정말 탈퇴하시겠습니까?")
                    .setPositiveButton("예", (dialog, which) -> {

                        String userID = prefs.getString("userID", "");

                        new Thread(() -> {
                            try {

                                URL url = new URL("https://healthhelper.mycafe24.com/deleteUser.php");
                                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                conn.setRequestMethod("POST");
                                conn.setDoOutput(true);
                                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                                String postData = "userID=" + URLEncoder.encode(userID, "UTF-8");
                                OutputStream os = conn.getOutputStream();
                                os.write(postData.getBytes());
                                os.flush();
                                os.close();

                                InputStream is = conn.getInputStream();
                                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                                StringBuilder response = new StringBuilder();
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    response.append(line);
                                }
                                reader.close();

                                JSONObject json = new JSONObject(response.toString());
                                boolean success = json.optBoolean("success", false);

                                getActivity().runOnUiThread(() -> {
                                    if (success) {

                                        prefs.edit().clear().apply();
                                        new AlertDialog.Builder(getActivity())
                                                .setMessage("탈퇴가 완료되었습니다.")
                                                .setPositiveButton("확인", (dialog1, which1) -> {
                                                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(intent);
                                                    getActivity().finish();
                                                })
                                                .show();

                                    } else {
                                        Toast.makeText(getActivity(), "탈퇴 실패. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            } catch (Exception e) {
                                e.printStackTrace();
                                getActivity().runOnUiThread(() -> {
                                    Toast.makeText(getActivity(), "네트워크 오류", Toast.LENGTH_SHORT).show();
                                });
                            }
                        }).start();

                    })
                    .setNegativeButton("아니오", null)
                    .show();
        });

        return view;
    }
}
