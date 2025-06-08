package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        checkForInquiryReply();

        return view;
    }

    private void checkForInquiryReply() {
        SharedPreferences userPrefs = requireActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String userID = userPrefs.getString("userID", "unknown");

        SharedPreferences notifPrefs = requireActivity().getSharedPreferences("notification_prefs", Context.MODE_PRIVATE);
        boolean isInquiryNotificationEnabled = notifPrefs.getBoolean("inquiry_answer", true);

        if (!isInquiryNotificationEnabled) {
            return;
        }

        String url = "https://healthhelper.mycafe24.com/check_inquiry_reply.php?userID=" + userID;

        RequestQueue queue = Volley.newRequestQueue(requireContext());

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    if (response.contains("reply")) {
                        showNotification(requireContext(), "문의 답변 도착", "문의하신 내용에 대한 답변이 도착했습니다.");
                    }
                },
                error -> {
                    error.printStackTrace();
                });

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
}
