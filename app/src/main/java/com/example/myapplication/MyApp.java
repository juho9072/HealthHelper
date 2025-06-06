package com.example.myapplication;

import android.app.Application;
import com.kakao.sdk.common.KakaoSdk;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Manifest 에 넣어둔 @string/kakao_native_app_key를 읽어서 초기화
        KakaoSdk.init(this, getString(R.string.kakao_native_app_key));
    }
}
