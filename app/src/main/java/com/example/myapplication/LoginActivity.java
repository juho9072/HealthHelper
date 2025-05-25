package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.kakao.sdk.user.UserApiClient;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private EditText idText, passwordText;
    private ImageButton btnTogglePw, btnKakao;
    private SignInButton btnGoogle;
    private Button loginButton;
    private CheckBox checkSaveId, checkPolicyAgree;
    private TextView privacyPolicyText, registerButton, findAccountText;

    private SharedPreferences prefs;
    private GoogleSignInClient mGoogleSignInClient;

    private final ActivityResultLauncher<Intent> googleLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        Log.d(TAG, "Google Sign-In onActivityResult: code=" + result.getResultCode());
                        Intent data = result.getData();
                        if (result.getResultCode() != RESULT_OK || data == null) {
                            Toast.makeText(this, "Google 로그인 취소됨 또는 데이터 없음", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        try {
                            GoogleSignInAccount acct = task.getResult(ApiException.class);
                            Log.d(TAG, "Google 로그인 성공: uid=" + acct.getId());

                            Map<String, String> body = new HashMap<>();
                            body.put("provider", "google");
                            body.put("provider_id", acct.getId());
                            body.put("email", acct.getEmail());
                            body.put("name", acct.getDisplayName());
                            body.put("profile_url", acct.getPhotoUrl() != null ? acct.getPhotoUrl().toString() : "");
                            body.put("id_token", acct.getIdToken());

                            NetworkHelper.sendSocialLogin(
                                    this, body,
                                    response -> {
                                        if (response.optBoolean("success", false)) {
                                            String jwt = response.optString("token");
                                            prefs.edit().putString("JWT_TOKEN", jwt).apply();
                                            goMain();
                                        } else {
                                            new AlertDialog.Builder(this)
                                                    .setMessage(response.optString("error", "로그인 실패"))
                                                    .setPositiveButton("확인", null)
                                                    .show();
                                        }
                                    },
                                    error -> {
                                        Log.e(TAG, "Volley Error: " + error);
                                        new AlertDialog.Builder(this)
                                                .setMessage("서버 통신 중 오류가 발생했습니다.")
                                                .setPositiveButton("확인", null)
                                                .show();
                                    }
                            );
                        } catch (ApiException e) {
                            Log.e(TAG, "Google 로그인 실패: " + e.getStatusCode(), e);
                            Toast.makeText(this, "Google 로그인 실패: 코드=" + e.getStatusCode(), Toast.LENGTH_LONG).show();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        Log.d(TAG, "onCreate");

        prefs = getSharedPreferences("login_prefs", MODE_PRIVATE);

        idText = findViewById(R.id.idText);
        passwordText = findViewById(R.id.passwordText);
        btnTogglePw = findViewById(R.id.btn_toggle_pw);
        loginButton = findViewById(R.id.loginButton);
        checkSaveId = findViewById(R.id.check_save_id);
        checkPolicyAgree = findViewById(R.id.check_policy_agree);
        privacyPolicyText = findViewById(R.id.privacyPolicyText);
        btnGoogle = findViewById(R.id.btn_google_sign_in);
        btnKakao = findViewById(R.id.btn_login_kakao);
        registerButton = findViewById(R.id.registerButton);
        findAccountText = findViewById(R.id.findAccountText);

        // 저장된 ID/동의 상태 복원
        String savedId = prefs.getString("SAVED_ID", "");
        if (!savedId.isEmpty()) {
            idText.setText(savedId);
            checkSaveId.setChecked(true);
        }
        checkPolicyAgree.setChecked(prefs.getBoolean("PRIVACY_AGREED", false));

        // Google 로그인 설정
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.default_web_client_id))
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnGoogle.setSize(SignInButton.SIZE_WIDE);
        btnGoogle.setOnClickListener(v -> {
            mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                googleLauncher.launch(signInIntent);
            });
        });

        // Kakao 로그인
        btnKakao.setOnClickListener(v -> {
            if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(this)) {
                UserApiClient.getInstance().loginWithKakaoTalk(this, (token, err) -> {
                    if (token != null) fetchKakaoProfile(token.getAccessToken());
                    return null;
                });
            } else {
                UserApiClient.getInstance().loginWithKakaoAccount(this, (token, err) -> {
                    if (token != null) fetchKakaoProfile(token.getAccessToken());
                    return null;
                });
            }
        });

        // 비밀번호 보기 토글
        btnTogglePw.setOnClickListener(v -> {
            boolean visible = (passwordText.getInputType() ==
                    (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD));
            passwordText.setInputType(visible
                    ? (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                    : (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD));
            btnTogglePw.setImageResource(visible ? R.drawable.login_eye : R.drawable.login_eye_off);
            passwordText.setSelection(passwordText.length());
        });

        // 개인정보처리방침 링크
        privacyPolicyText.setOnClickListener(v ->
                startActivity(new Intent(this, PrivacyPolicyActivity.class))
        );

        // 🔐 일반 로그인
        loginButton.setOnClickListener(v -> {
            if (!checkPolicyAgree.isChecked()) {
                new AlertDialog.Builder(this)
                        .setMessage("개인정보처리방침에 동의해 주세요.")
                        .setPositiveButton("확인", null)
                        .show();
                return;
            }

            String uid = idText.getText().toString().trim();
            String pw = passwordText.getText().toString();
            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(new LoginRequest(uid, pw, response -> {
                try {
                    JSONObject obj = new JSONObject(response);
                    if (obj.getBoolean("success")) {
                        SharedPreferences userPrefs = getSharedPreferences("userInfo", MODE_PRIVATE);
                        userPrefs.edit()
                                .putString("userID", uid)
                                .putString("userEmail", obj.getString("userEmail")) //이메일 받아오기
                                .apply();

                        if (checkSaveId.isChecked()) {
                            prefs.edit().putString("SAVED_ID", uid).apply();
                        } else {
                            prefs.edit().remove("SAVED_ID").apply();
                        }
                        goMain();
                    } else {
                        new AlertDialog.Builder(this)
                                .setMessage("로그인 실패")
                                .setPositiveButton("확인", null)
                                .show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "로그인 응답 처리 중 오류", e);
                }
            }));
        });

        // 회원가입/계정찾기
        registerButton.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
        findAccountText.setOnClickListener(v ->
                startActivity(new Intent(this, FindAccountActivity.class))
        );
    }

    private void fetchKakaoProfile(String accessToken) {
        UserApiClient.getInstance().me((user, meError) -> {
            if (user != null && user.getKakaoAccount() != null) {
                Map<String, String> body = new HashMap<>();
                body.put("provider", "kakao");
                body.put("provider_id", String.valueOf(user.getId()));
                body.put("email", user.getKakaoAccount().getEmail());
                body.put("name", user.getKakaoAccount().getProfile().getNickname());
                body.put("profile_url", user.getKakaoAccount().getProfile().getProfileImageUrl());
                body.put("id_token", accessToken);

                NetworkHelper.sendSocialLogin(
                        this, body,
                        response -> {
                            if (response.optBoolean("success", false)) {
                                prefs.edit().putString("JWT_TOKEN", response.optString("token")).apply();
                                goMain();
                            } else {
                                new AlertDialog.Builder(this)
                                        .setMessage(response.optString("error", "로그인 실패"))
                                        .setPositiveButton("확인", null)
                                        .show();
                            }
                        },
                        error -> {
                            Log.e("VolleyError", "통신 오류", error);
                            new AlertDialog.Builder(this)
                                    .setTitle("통신 오류")
                                    .setMessage("서버와의 통신 중 오류가 발생했습니다.")
                                    .setPositiveButton("확인", null)
                                    .show();
                        }
                );
            }
            return null;
        });
    }

    private void goMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
