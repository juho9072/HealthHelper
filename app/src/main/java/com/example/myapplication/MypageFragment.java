package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MypageFragment extends Fragment {
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ImageView imageProfile;
    private RequestQueue requestQueue;
    private String userId;

    private void loadProfileImage() {
        String url = "https://healthhelper.mycafe24.com/healthhelper/profile_get.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {

                    Log.d("프로필응답", response);

                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.optBoolean("success")) {
                            String imgUrl = obj.optString("image_url");

                            Log.d("이미지URL", imgUrl);

                            Glide.with(requireContext())
                                    .load(imgUrl)
                                    .placeholder(R.drawable.ic_profile)
                                    .error(R.drawable.ic_profile)
                                    .apply(RequestOptions.circleCropTransform())
                                    .skipMemoryCache(true)
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .into(imageProfile);
                        } else {
                            imageProfile.setImageResource(R.drawable.ic_profile);
                        }
                    } catch (Exception e) {
                        imageProfile.setImageResource(R.drawable.ic_profile);
                    }
                },
                error -> {
                    imageProfile.setImageResource(R.drawable.ic_profile);
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userID", userId);
                return params;
            }
        };
        Volley.newRequestQueue(requireContext()).add(stringRequest);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_mypage, container, false);

        SharedPreferences prefs = getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        userId = prefs.getString("userID", "아이디 없음");
        TextView userIdTextView = view.findViewById(R.id.text_user_id);
        userIdTextView.setText(userId);

        View settingLayout = view.findViewById(R.id.btn_settings);
        settingLayout.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingActivity.class);
            startActivity(intent);
        });

        imageProfile = view.findViewById(R.id.image_profile);
        Button editProfileBtn = view.findViewById(R.id.btn_edit_profile);

        requestQueue = Volley.newRequestQueue(requireContext());

        loadProfileImage();

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            Glide.with(requireContext())
                                    .load(selectedImageUri)
                                    .apply(RequestOptions.circleCropTransform())
                                    .into(imageProfile);

                            uploadProfileImage(selectedImageUri);
                        }
                    }
                }
        );

        editProfileBtn.setOnClickListener(v -> openGallery());

        return view;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void uploadProfileImage(Uri imageUri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
            byte[] imageData = getBytes(inputStream);

            String fileName = getFileName(imageUri);

            String url = "https://healthhelper.mycafe24.com/uploadProfileImage.php";

            VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(
                    Request.Method.POST, url,
                    response -> {
                        Toast.makeText(getContext(), "프로필 이미지가 변경되었습니다.", Toast.LENGTH_SHORT).show();
                        loadProfileImage();
                    },
                    error -> {
                        Toast.makeText(getContext(), "업로드 실패: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("업로드에러", error.toString());
                    }
            ) {
                @Override
                public Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("userID", userId);
                    return params;
                }

                @Override
                public Map<String, VolleyMultipartRequest.DataPart> getByteData() {
                    Map<String, VolleyMultipartRequest.DataPart> params = new HashMap<>();
                    params.put("profile_image", new VolleyMultipartRequest.DataPart(fileName, imageData, "image/jpeg"));
                    return params;
                }
            };

            requestQueue.add(multipartRequest);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "이미지 처리 오류", Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }
}
