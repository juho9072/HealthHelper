package com.example.myapplication;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class ChangePasswordRequest extends StringRequest {

    private static final String URL = "https://healthhelper.mycafe24.com/healthhelper/change_pw.php";
    private final Map<String, String> params;

    public ChangePasswordRequest(String email, String newPassword, String code, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, error -> error.printStackTrace());

        params = new HashMap<>();
        params.put("email", email);
        params.put("newPassword", newPassword);
        params.put("code", code); // ← 인증번호 추가됨
    }

    @Override
    protected Map<String, String> getParams() {
        return params;
    }
}