package com.example.pillit_scaneat;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class StartActivity extends AppCompatActivity {

    private Button btnLoginStart, btnSignupStart;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // 상태바 아이콘 밝게 (메뉴 점 하얀색 포함)
        WindowInsetsController controller = getWindow().getInsetsController();
        if (controller != null) {
            controller.setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
        }

        btnLoginStart = findViewById(R.id.btnLoginStart);
        btnSignupStart = findViewById(R.id.btnSignupStart);
        btnBack = findViewById(R.id.btnBackStart);

        btnLoginStart.setOnClickListener(v ->
                startActivity(new Intent(StartActivity.this, LoginActivity.class)));

        btnSignupStart.setOnClickListener(v ->
                startActivity(new Intent(StartActivity.this, SignupActivity.class)));

        btnBack.setOnClickListener(v -> onBackPressed());
    }
}
