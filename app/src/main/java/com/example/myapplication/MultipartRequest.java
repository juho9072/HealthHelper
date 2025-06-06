package com.example.myapplication;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MultipartRequest extends Request<String> {

    private final Response.Listener<String> mListener;
    private final Response.ErrorListener mErrorListener;
    private final Map<String, String> mParams;
    private final Map<String, DataPart> mByteData;
    private final String boundary = "apiclient-" + System.currentTimeMillis();

    public MultipartRequest(String url, Map<String, String> params, Map<String, DataPart> byteData,
                            Response.Listener<String> listener,
                            Response.ErrorListener errorListener) {
        super(Method.POST, url, errorListener);
        this.mListener = listener;
        this.mErrorListener = errorListener;
        this.mParams = params;
        this.mByteData = byteData;
    }

    @Override
    public String getBodyContentType() {
        return "multipart/form-data;boundary=" + boundary;
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            if (mParams != null && mParams.size() > 0) {
                for (Map.Entry<String, String> entry : mParams.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    bos.write(("--" + boundary + "\r\n").getBytes());
                    bos.write(("Content-Disposition: form-data; name=\"" + key + "\"\r\n\r\n").getBytes());
                    bos.write((value + "\r\n").getBytes());
                }
            }
            if (mByteData != null && mByteData.size() > 0) {
                for (Map.Entry<String, DataPart> entry : mByteData.entrySet()) {
                    String key = entry.getKey();
                    DataPart dataFile = entry.getValue();
                    bos.write(("--" + boundary + "\r\n").getBytes());
                    bos.write(("Content-Disposition: form-data; name=\"" + key + "\"; filename=\"" + dataFile.getFileName() + "\"\r\n").getBytes());
                    bos.write(("Content-Type: " + dataFile.getType() + "\r\n\r\n").getBytes());
                    bos.write(dataFile.getContent());
                    bos.write("\r\n".getBytes());
                }
            }
            bos.write(("--" + boundary + "--\r\n").getBytes());
        } catch (IOException e) {
            VolleyLog.e("IOException writing to ByteArrayOutputStream");
        }
        return bos.toByteArray();
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String parsed = new String(response.data);
        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }

    public static class DataPart {
        private final String fileName;
        private final byte[] content;
        private final String type;

        public DataPart(String name, byte[] data, String type) {
            fileName = name;
            content = data;
            this.type = type;
        }
        public String getFileName() { return fileName; }
        public byte[] getContent() { return content; }
        public String getType() { return type; }
    }
}
