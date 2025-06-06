package com.example.myapplication;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import java.util.HashMap;
import java.util.Map;
public class SendAuthCodeRequest extends StringRequest {
    private static final String URL = "https://healthhelper.mycafe24.com/healthhelper/send_auth_code.php";
    private final Map<String, String> params;

    public SendAuthCodeRequest(String email, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);
        params = new HashMap<>();
        params.put("email", email);
    }

    @Override
    protected Map<String, String> getParams() {
        return params;
    }
}
