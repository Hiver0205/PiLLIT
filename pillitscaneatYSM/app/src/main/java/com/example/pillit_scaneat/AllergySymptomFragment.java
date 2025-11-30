package com.example.pillit_scaneat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllergySymptomFragment extends Fragment {

    private ListView listView;
    private FirebaseFirestore db;
    private String uid;

    private List<String> allergyNames = new ArrayList<>();

    // 🔥 Map<성분명, Map<mild/severe, List<String>>>
    private Map<String, Map<String, List<String>>> selectedMap = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_allergy_symptom, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listView = view.findViewById(R.id.listSymptom);
        Button btnDone = view.findViewById(R.id.btnDone);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadAllergyDataFromFirestore();

        btnDone.setOnClickListener(v -> saveAllSymptoms());
    }

    // --------------------------------------------------------
    // 🔥 Firestore에서 mild, severe, prefix, selectedIngredients 불러오기
    // --------------------------------------------------------
    private void loadAllergyDataFromFirestore() {

        db.collection("users")
                .document(uid)
                .collection("allergies")
                .document("myAllergies")
                .get()
                .addOnSuccessListener(doc -> {

                    allergyNames.clear();
                    selectedMap.clear();

                    List<String> selectedIngredients =
                            (List<String>) doc.get("selectedIngredients");

                    if (selectedIngredients == null)
                        selectedIngredients = new ArrayList<>();

                    allergyNames.addAll(selectedIngredients);

                    Map<String, Object> mildMap = (Map<String, Object>) doc.get("mild");
                    Map<String, Object> severeMap = (Map<String, Object>) doc.get("severe");

                    if (mildMap == null) mildMap = new HashMap<>();
                    if (severeMap == null) severeMap = new HashMap<>();

                    for (String ing : allergyNames) {

                        List<String> mildList = new ArrayList<>();
                        List<String> severeList = new ArrayList<>();

                        if (Boolean.TRUE.equals(mildMap.get(ing)))
                            mildList.add(ing);

                        if (Boolean.TRUE.equals(severeMap.get(ing)))
                            severeList.add(ing);

                        Map<String, List<String>> map = new HashMap<>();
                        map.put("mild", mildList);
                        map.put("severe", severeList);

                        selectedMap.put(ing, map);
                    }

                    AllergySymptomAdapter adapter =
                            new AllergySymptomAdapter(
                                    getContext(),
                                    allergyNames,
                                    selectedMap
                            );

                    listView.setAdapter(adapter);
                });
    }

    // --------------------------------------------------------
    // 🔥 완료 → Firestore에 한 번에 저장
    // --------------------------------------------------------
    private void saveAllSymptoms() {

        Map<String, Object> update = new HashMap<>();

        // prefix를 하나의 Map으로 묶기
        Map<String, Object> prefixMap = new HashMap<>();

        for (String ing : allergyNames) {

            List<String> mild = selectedMap.get(ing).get("mild");
            List<String> severe = selectedMap.get(ing).get("severe");

            boolean isMild = !mild.isEmpty();
            boolean isSevere = !severe.isEmpty();

            update.put("mild." + ing, isMild);
            update.put("severe." + ing, isSevere);

            // prefix 값 결정
            if (isSevere)
                prefixMap.put(ing, "2");
            else if (isMild)
                prefixMap.put(ing, "1");
            else
                prefixMap.put(ing, "0");
        }

        // 🔥 prefix 객체 전체 저장!
        update.put("prefix", prefixMap);

        db.collection("users")
                .document(uid)
                .collection("allergies")
                .document("myAllergies")
                .set(update, SetOptions.merge())
                .addOnSuccessListener(v -> moveToScanFragment());
    }


    private void moveToScanFragment() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new ScanFragment())
                .addToBackStack(null)
                .commit();
    }
}
