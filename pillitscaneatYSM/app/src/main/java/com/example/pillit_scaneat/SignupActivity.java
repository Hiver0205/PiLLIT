package com.example.pillit_scaneat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private EditText editTextName, editTextEmail, editTextPassword;
    private Button btnSignup;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        editTextName = findViewById(R.id.etIdSignup);
        editTextEmail = findViewById(R.id.etEmailSignup);
        editTextPassword = findViewById(R.id.etPwSignup);
        btnSignup = findViewById(R.id.btnCreate);

        btnSignup.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "모든 정보를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        String userId = auth.getCurrentUser().getUid();

                        Map<String, Object> user = new HashMap<>();
                        user.put("uid", userId);
                        user.put("name", name);
                        user.put("email", email);

                        firestore.collection("users").document(userId)
                                .set(user)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "회원가입 성공! 메인 화면으로 이동합니다.", Toast.LENGTH_SHORT).show();

                                    // 🔥 회원가입 후 곧바로 로그인 상태 유지 → MainActivity 이동
                                    Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "저장 실패: " + e.getMessage(), Toast.LENGTH_LONG).show()
                                );

                    } else {
                        Toast.makeText(this,
                                "회원가입 실패: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
