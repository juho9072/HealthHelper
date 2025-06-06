package com.example.myapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class RoutineDetailActivity extends AppCompatActivity {

    private WebView webView;
    private float currentSpeed = 1.0f;
    private int currentRepeat = 1;
    private int currentRepeatProgress = 0;
    private long startTime = 0;
    private Handler handler = new Handler();

    private TextView tvExpectedTime, tvRepeatProgress, tvStartTime, tvEndTime;
    private ProgressBar progressBar;

    private double videoDuration = 0;

    private String extractYoutubeVideoId(String url) {
        try {
            if (url.contains("v=")) {
                return url.substring(url.indexOf("v=") + 2).split("&")[0];
            } else if (url.contains("youtu.be/")) {
                return url.substring(url.lastIndexOf("/") + 1);
            }
        } catch (Exception ignored) {}
        return "";
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_detail);

        String youtubeLink = getIntent().getStringExtra("link");
        String videoId = extractYoutubeVideoId(youtubeLink);

        tvExpectedTime = findViewById(R.id.tvExpectedTime);
        tvRepeatProgress = findViewById(R.id.tvRepeatProgress);
        tvStartTime = findViewById(R.id.tvStartTime);
        tvEndTime = findViewById(R.id.tvEndTime);
        progressBar = findViewById(R.id.progressBar);

        webView = findViewById(R.id.youtubePlayer);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new WebAppInterface(), "Android");

        String html = "<html><body style='margin:0'>" +
                "<div id='player'></div>" +
                "<script>" +
                "var tag = document.createElement('script');" +
                "tag.src = 'https://www.youtube.com/iframe_api';" +
                "var firstScriptTag = document.getElementsByTagName('script')[0];" +
                "firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);" +

                "var player;" +
                "var repeatCount = 1;" +
                "var repeatIndex = 0;" +
                "var duration = 0;" +

                "function onYouTubeIframeAPIReady() {" +
                "  player = new YT.Player('player', {" +
                "    height: '200'," +
                "    width: '100%'," +
                "    videoId: '" + videoId + "'," +
                "    events: {" +
                "      'onReady': onPlayerReady," +
                "      'onStateChange': onPlayerStateChange" +
                "    }" +
                "  });" +
                "}" +

                "function onPlayerReady(event) {" +
                "  duration = player.getDuration();" +
                "  Android.getDuration(duration);" +
                "  setInterval(function() {" +
                "    Android.updateProgress(player.getCurrentTime());" +
                "  }, 1000);" +
                "  event.target.playVideo();" +
                "}" +

                "function onPlayerStateChange(event) {" +
                "  if (event.data === YT.PlayerState.ENDED) {" +
                "    repeatIndex++;" +
                "    Android.updateRepeat(repeatIndex);" +
                "    if (repeatIndex < repeatCount) {" +
                "      player.seekTo(0);" +
                "      player.playVideo();" +
                "    }" +
                "  }" +
                "}" +

                "function setPlaybackSpeed(speed) {" +
                "  if (player && player.setPlaybackRate) player.setPlaybackRate(speed);" +
                "}" +

                "function setRepeatCount(count) {" +
                "  repeatCount = count; repeatIndex = 0;" +
                "}" +
                "</script>" +
                "</body></html>";

        webView.loadDataWithBaseURL("https://www.youtube.com", html, "text/html", "utf-8", null);

        Button btnSpeedMinus = findViewById(R.id.btnSpeedMinus);
        Button btnSpeedPlus = findViewById(R.id.btnSpeedPlus);
        Button btnRepeatMinus = findViewById(R.id.btnRepeatMinus);
        Button btnRepeatPlus = findViewById(R.id.btnRepeatPlus);
        Button btnStart = findViewById(R.id.btnStartRoutine);
        Button btnEnd = findViewById(R.id.btnEndRoutine);
        TextView tvSpeedValue = findViewById(R.id.tvSpeedValue);
        TextView tvRepeatValue = findViewById(R.id.tvRepeatValue);

        btnSpeedMinus.setOnClickListener(v -> {
            if (currentSpeed > 0.25f) {
                currentSpeed -= 0.25f;
                tvSpeedValue.setText(String.format("%.2fx", currentSpeed));
                webView.evaluateJavascript("setPlaybackSpeed(" + currentSpeed + ");", null);
                updateExpectedTime();
            }
        });

        btnSpeedPlus.setOnClickListener(v -> {
            if (currentSpeed < 3.0f) {
                currentSpeed += 0.25f;
                tvSpeedValue.setText(String.format("%.2fx", currentSpeed));
                webView.evaluateJavascript("setPlaybackSpeed(" + currentSpeed + ");", null);
                updateExpectedTime();
            }
        });

        btnRepeatMinus.setOnClickListener(v -> {
            if (currentRepeat > 1) {
                currentRepeat--;
                tvRepeatValue.setText(currentRepeat + "회");
                webView.evaluateJavascript("setRepeatCount(" + currentRepeat + ");", null);
                updateExpectedTime();
            }
        });

        btnRepeatPlus.setOnClickListener(v -> {
            if (currentRepeat < 10) {
                currentRepeat++;
                tvRepeatValue.setText(currentRepeat + "회");
                webView.evaluateJavascript("setRepeatCount(" + currentRepeat + ");", null);
                updateExpectedTime();
            }
        });

        btnStart.setOnClickListener(v -> {
            startTime = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.KOREA);
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            tvStartTime.setText("시작 시간: " + sdf.format(new Date(startTime)));
        });

        btnEnd.setOnClickListener(v -> {
            long endTime = System.currentTimeMillis();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.KOREA);
            sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
            tvEndTime.setText("종료 시간: " + sdf.format(new Date(endTime)));
        });

        tvSpeedValue.setText("1.00x");
        tvRepeatValue.setText("1회");
        tvRepeatProgress.setText("현재: 0 / 1회");
    }

    private void updateExpectedTime() {
        if (videoDuration <= 0) return;
        double adjustedDuration = (videoDuration * currentRepeat) / currentSpeed;
        int minutes = (int)(adjustedDuration / 60);
        int seconds = (int)(adjustedDuration % 60);
        tvExpectedTime.setText(String.format(Locale.KOREA, "예상 시간: %02d:%02d", minutes, seconds));
        progressBar.setMax((int)adjustedDuration);
    }

    private class WebAppInterface {
        @JavascriptInterface
        public void getDuration(double duration) {
            videoDuration = duration;
            runOnUiThread(() -> updateExpectedTime());
        }

        @JavascriptInterface
        public void updateProgress(double currentTime) {
            runOnUiThread(() -> progressBar.setProgress((int)currentTime));
        }

        @JavascriptInterface
        public void updateRepeat(int count) {
            currentRepeatProgress = count;
            runOnUiThread(() -> tvRepeatProgress.setText("현재: " + count + " / " + currentRepeat + "회"));
        }
    }
}
