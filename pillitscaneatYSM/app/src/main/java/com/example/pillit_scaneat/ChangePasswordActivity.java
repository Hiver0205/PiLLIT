package com.example.pillit_scaneat;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText editCurrentPassword, editNewPassword;
    private Button btnApplyChange;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        editCurrentPassword = findViewById(R.id.editCurrentPassword);
        editNewPassword = findViewById(R.id.editNewPassword);
        btnApplyChange = findViewById(R.id.btnApplyChange);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        btnApplyChange.setOnClickListener(v -> {
            if (user == null) return;

            String currentPw = editCurrentPassword.getText().toString().trim();
            String newPw = editNewPassword.getText().toString().trim();

            if (currentPw.isEmpty() || newPw.isEmpty()) {
                Toast.makeText(this, "모든 칸을 입력하세요", Toast.LENGTH_SHORT).show();
                return;
            }

            // 🔒 1) 재인증
            AuthCredential credential = EmailAuthProvider.getCredential(
                    user.getEmail(),
                    currentPw
            );

            user.reauthenticate(credential)
                    .addOnSuccessListener(aVoid -> {
                        // 🔒 2) 비밀번호 변경
                        user.updatePassword(newPw)
                                .addOnSuccessListener(aVoid2 ->
                                        Toast.makeText(this, "비밀번호 변경 완료!", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "변경 실패: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "현재 비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show());
        });
    }
}
