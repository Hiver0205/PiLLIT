package com.example.pillit_scaneat;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AllergyFragment extends Fragment {

    private ListView listView;
    private EditText editSearch;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private ArrayList<String> ingredientList = new ArrayList<>();
    private ArrayList<String> filteredList = new ArrayList<>();

    private Set<String> selectedList = new HashSet<>();
    private AllergyAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_allergy, container, false);

        listView = view.findViewById(R.id.ingredientList);
        editSearch = view.findViewById(R.id.editSearch);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loadUserSelectedAllergy();
        loadIngredients();
        setupSearchFilter();

        Button btnDone = view.findViewById(R.id.btnDone);
        btnDone.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new AllergySymptomFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    //----------------------------------------
    // 🔥 이미 저장된 selectedIngredients 불러오기
    //----------------------------------------
    private void loadUserSelectedAllergy() {
        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .collection("allergies")
                .document("myAllergies")
                .get()
                .addOnSuccessListener(doc -> {

                    selectedList.clear();

                    if (doc.exists() && doc.get("selectedIngredients") instanceof ArrayList) {
                        selectedList.addAll((ArrayList<String>) doc.get("selectedIngredients"));
                    }
                });
    }

    //----------------------------------------
    // 🔥 drugs_v2 전체 성분 로드
    //----------------------------------------
    private void loadIngredients() {

        db.collection("drugs_v2")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    ingredientList.clear();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String ing = doc.getString("주성분");
                        if (ing != null && !ingredientList.contains(ing)) {
                            ingredientList.add(ing);
                        }
                    }

                    Collections.sort(ingredientList);

                    filteredList.clear();
                    filteredList.addAll(ingredientList);

                    adapter = new AllergyAdapter(requireContext(),
                            filteredList,
                            selectedList,
                            (ingredient, isChecked) -> {

                                if (isChecked) {
                                    saveToFirestore(ingredient);
                                } else {
                                    removeFromFirestore(ingredient);
                                }
                            });

                    listView.setAdapter(adapter);
                });
    }

    //----------------------------------------
    // 🔍 검색 필터
    //----------------------------------------
    private void setupSearchFilter() {
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }
        });
    }

    private void filterList(String query) {
        filteredList.clear();

        for (String ing : ingredientList) {
            if (ing.contains(query)) filteredList.add(ing);
        }

        adapter.notifyDataSetChanged();
    }

    //----------------------------------------
    // 🔥 선택한 성분 Firestore 저장 + prefix 자동 생성
    //----------------------------------------
    private void saveToFirestore(String ingredient) {
        String uid = auth.getCurrentUser().getUid();

        // ① selectedIngredients 저장
        Map<String, Object> update = new HashMap<>();
        update.put("selectedIngredients", FieldValue.arrayUnion(ingredient));

        db.collection("users")
                .document(uid)
                .collection("allergies")
                .document("myAllergies")
                .set(update, SetOptions.merge());

        // ② 🔥 prefix 자동 저장 (기본은 severe = 2)
        savePrefix(ingredient, "2");
    }

    //----------------------------------------
    // 🔥 prefix = { "아세틸시스테인" : "2" } 형식으로 저장
    //----------------------------------------
    private void savePrefix(String ingredient, String value) {
        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .collection("allergies")
                .document("myAllergies")
                .get()
                .addOnSuccessListener(doc -> {

                    Map<String, Object> prefixMap = (Map<String, Object>) doc.get("prefix");
                    if (prefixMap == null) prefixMap = new HashMap<>();

                    prefixMap.put(ingredient, value);

                    Map<String, Object> update = new HashMap<>();
                    update.put("prefix", prefixMap);

                    db.collection("users")
                            .document(uid)
                            .collection("allergies")
                            .document("myAllergies")
                            .set(update, SetOptions.merge());
                });
    }

    //----------------------------------------
    // 🔥 선택 해제
    //----------------------------------------
    private void removeFromFirestore(String ingredient) {
        String uid = auth.getCurrentUser().getUid();

        // selectedIngredients 제거
        Map<String, Object> update = new HashMap<>();
        update.put("selectedIngredients", FieldValue.arrayRemove(ingredient));

        db.collection("users")
                .document(uid)
                .collection("allergies")
                .document("myAllergies")
                .set(update, SetOptions.merge());
    }
}
