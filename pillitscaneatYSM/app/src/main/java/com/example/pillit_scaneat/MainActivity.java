package com.example.pillit_scaneat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("AUTH_TEST", "MainActivity 실행됨");

        // Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Toolbar
        Toolbar toolbar = findViewById(R.id.mainToolbar);
        setSupportActionBar(toolbar);

        // DrawerLayout & NavigationView
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);

        // 헤더 가져오기
        View headerView = navigationView.getHeaderView(0);
        TextView tvUserWelcome = headerView.findViewById(R.id.tvUserWelcome);
        TextView tvUserEmail = headerView.findViewById(R.id.tvUserEmail);

        // 로그인 사용자 정보 가져오기
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            Log.d("AUTH_TEST", "currentUser = " + user.getEmail());

            String uid = user.getUid();

            db.collection("users").document(uid)
                    .get()
                    .addOnSuccessListener(doc -> {
                        Log.d("AUTH_TEST", "Firestore user 문서 읽기 성공");

                        if (doc.exists()) {
                            Log.d("AUTH_TEST", "문서 내용 = " + doc.getData());

                            String name = doc.getString("name");
                            String email = doc.getString("email");

                            if (name != null) tvUserWelcome.setText(name + "님 안녕하세요!");
                            if (email != null) tvUserEmail.setText(email);

                        } else {
                            Log.e("AUTH_TEST", "사용자 문서가 Firestore에 없음!");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("AUTH_TEST", "Firestore ERROR = " + e.getMessage());
                    });

        } else {
            Log.e("AUTH_TEST", "currentUser == NULL !!!");
        }

        // Drawer 토글 아이콘
        ActionBarDrawerToggle toggle =
                new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                        R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // 첫 화면
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        // 메뉴 클릭 처리
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.menu_home) {
                loadFragment(new HomeFragment());
            } else if (id == R.id.menu_scan) {
                loadFragment(new ScanMenuFragment());
            } else if (id == R.id.menu_allergy) {
                loadFragment(new AllergyFragment());
            } else if (id == R.id.menu_userinfo) {
                loadFragment(new UserInfoFragment());
            } else if (id == R.id.menu_logout) {
                FirebaseAuth.getInstance().signOut();
                Intent i = new Intent(MainActivity.this, LoginActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    // 프래그먼트 로딩 함수
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
