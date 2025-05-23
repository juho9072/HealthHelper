package com.example.myapplication;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class FindIdRequest extends StringRequest {

    private static final String URL = "https://healthhelper.mycafe24.com/healthhelper/find_id.php"; // 수정 필요
    private final Map<String, String> params;

    public FindIdRequest(String email, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);
        params = new HashMap<>();
        params.put("email", email);
    }

    @Override
    protected Map<String, String> getParams() {
        return params;
    }
}
