package com.example.myapplication;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;
public class FindPwRequest extends StringRequest {
    private static final String URL = "https://healthhelper.mycafe24.com/healthhelper/find_pw.php";
    private final Map<String, String> params;

    public FindPwRequest(String userID, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, error -> error.printStackTrace());
        params = new HashMap<>();
        params.put("userID", userID);
    }

    @Override
    protected Map<String, String> getParams() {
        return params;
    }
}
