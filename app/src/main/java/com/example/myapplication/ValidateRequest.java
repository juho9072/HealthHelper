package com.example.myapplication;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

public class ValidateRequest extends StringRequest {

    final static private String URL = "https://healthhelper.mycafe24.com/UserValidate.php";
    private Map<String, String> parameters;

    public ValidateRequest(String userID, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);
        parameters = new HashMap<>();
        parameters.put("userID", userID);
    }


    @Override
    public Map<String, String> getParams() {
        return parameters;
    }
}