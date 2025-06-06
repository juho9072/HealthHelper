package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class PrivacyPolicyActivity extends AppCompatActivity {
    private WebView webView;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.privacy_policy);  // 이 레이아웃이 res/layout/activity_privacy_policy.xml 에 있어야 합니다

        // SharedPreferences 초기화
        prefs = getSharedPreferences("login_prefs", MODE_PRIVATE);

        // WebView 바인딩
        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        // JS → Android 호출 인터페이스 등록
        webView.addJavascriptInterface(new WebAppInterface(), "Android");

        // assets/privacy.html 로드
        webView.loadUrl("file:///android_asset/privacy.html");
    }

    /**
     * HTML 안에서 window.Android.closePolicy() 호출 시 실행됩니다.
     */
    private class WebAppInterface {
        @JavascriptInterface
        public void closePolicy() {
            // 동의 여부 저장
            prefs.edit()
                    .putBoolean("PRIVACY_AGREED", true)
                    .apply();

            // RESULT_OK 전달 후 Activity 종료
            setResult(RESULT_OK);
            finish();
        }
    }
}