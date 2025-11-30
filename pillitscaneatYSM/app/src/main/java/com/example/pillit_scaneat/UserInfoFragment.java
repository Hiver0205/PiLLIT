package com.example.pillit_scaneat;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserInfoFragment extends Fragment {

    private TextView textName, textEmail, textAllergies;
    private Button btnEditAllergy, btnChangePassword;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_userinfo, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        textName = view.findViewById(R.id.textName);
        textEmail = view.findViewById(R.id.textEmail);
        textAllergies = view.findViewById(R.id.textAllergies);
        btnEditAllergy = view.findViewById(R.id.btnEditAllergy);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            loadUserInfo(uid);
        } else {
            Log.e("USERINFO_TEST", "currentUser == NULL !!!");
        }

        // ⭐ 알레르기 정보 수정 버튼 클릭 시 이동
        btnEditAllergy.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new AllergyFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void loadUserInfo(String uid) {

        Log.d("USERINFO_TEST", "유저 정보 불러오기 시작: uid = " + uid);

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Log.d("USERINFO_TEST", "기본 정보 = " + doc.getData());

                        textName.setText("이름: " + doc.getString("name"));
                        textEmail.setText("이메일: " + doc.getString("email"));
                    } else {
                        Log.e("USERINFO_TEST", "기본 정보 문서 없음!");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("USERINFO_TEST", "기본 정보 읽기 실패: " + e.getMessage());
                });

        db.collection("users").document(uid)
                .collection("allergies")
                .document("myAllergies")
                .get()
                .addOnSuccessListener(doc -> {

                    if (doc.exists()) {
                        Log.d("USERINFO_TEST", "myAllergies = " + doc.getData());

                        Object obj = doc.get("selectedIngredients");

                        if (obj instanceof java.util.List) {
                            java.util.List<String> list = (java.util.List<String>) obj;

                            if (list.isEmpty()) {
                                textAllergies.setText("알레르기: 없음");
                            } else {
                                textAllergies.setText("알레르기: " + String.join(", ", list));
                            }
                        }
                    } else {
                        Log.e("USERINFO_TEST", "myAllergies 문서 없음!");
                        textAllergies.setText("알레르기: 없음");
                    }

                })
                .addOnFailureListener(e -> {
                    Log.e("USERINFO_TEST", "myAllergies 읽기 실패 = " + e.getMessage());
                    textAllergies.setText("알레르기: 불러오기 실패");
                });
    }
}
