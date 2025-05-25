package com.example.myapplication;
import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NetworkHelper {
    private static RequestQueue queue;

    public static void init(Context ctx) {
        if (queue == null) {
            queue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
    }

    public static void sendSocialLogin(
            Context ctx,
            Map<String, String> params,
            Response.Listener<JSONObject> onSuccess,
            Response.ErrorListener onError
    ) {
        init(ctx);
        String url = "https://healthhelper.mycafe24.com/social_login.php";
        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST,
                url,
                new JSONObject(params),
                onSuccess,
                error -> {
                    if (error.networkResponse != null) {
                        Log.e("Volley", "Status: " + error.networkResponse.statusCode);
                        Log.e("Volley", "Data: " + new String(error.networkResponse.data));
                    } else {
                        Log.e("Volley", "Error: " + error.toString());
                    }
                    onError.onErrorResponse(error);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };

        queue.add(req);
    }
}