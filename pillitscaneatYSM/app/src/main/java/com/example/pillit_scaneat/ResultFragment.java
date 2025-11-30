package com.example.pillit_scaneat;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class ResultFragment extends Fragment {

    private TextView textDanger, textIngredient, textSideEffect, textMore, textDetail;
    private ImageView iconWarning;
    private SuperZoomLayout detailLayout;

    private FirebaseFirestore db;

    private String selectedIngredient = "";
    private String selectedProduct = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_result, container, false);

        textDanger = view.findViewById(R.id.textDanger);
        textIngredient = view.findViewById(R.id.textDrugIngredient);
        iconWarning = view.findViewById(R.id.iconWarning);
        textSideEffect = view.findViewById(R.id.textSideEffect);
        textMore = view.findViewById(R.id.textMore);
        detailLayout = view.findViewById(R.id.detailLayout);
        textDetail = view.findViewById(R.id.textDetail);

        ImageView btnBack = view.findViewById(R.id.btnBackToSymptom);
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ScanFragment())
                    .addToBackStack(null)
                    .commit();
        });



        db = FirebaseFirestore.getInstance();

        if (getArguments() != null) {
            selectedIngredient = getArguments().getString("selectedIngredient", "");
            selectedProduct = getArguments().getString("selectedProduct", "");
        }

        textIngredient.setText(selectedIngredient);

        loadUserPrefix();
        return view;
    }

    // ---------------------------------------------------------------
    // STEP 1. 사용자 prefix 가져와서 Danger/Warning/Safety 판단
    // ---------------------------------------------------------------
    private void loadUserPrefix() {

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .collection("allergies")
                .document("myAllergies")
                .get()
                .addOnSuccessListener(doc -> {

                    System.out.println("--------------------------------------------------");
                    System.out.println("🔥 [DEBUG] selectedIngredient = " + selectedIngredient);
                    String key = selectedIngredient.replace(" ", "").trim();
                    System.out.println("🔥 [DEBUG] key(without space) = " + key);
                    System.out.println("🔥 [DEBUG] prefix map = " + doc.get("prefix"));
                    System.out.println("--------------------------------------------------");

                    Object prefixObj = doc.get("prefix");

                    if (prefixObj == null) {
                        // 유저가 아무 알레르기 체크 안 해둔 상태
                        showSafety();
                        loadDrugDetail();
                        return;
                    }

                    Map<String, String> prefix = (Map<String, String>) prefixObj;

                    String fixedKey = selectedIngredient.replace(" ", "").trim();
                    String value = prefix.get(fixedKey);

                    System.out.println("🔥 [DEBUG] value from prefix[" + fixedKey + "] = " + value);

                    if (value == null) {
                        showSafety();
                        loadDrugDetail();
                        return;
                    }

                    switch (value) {
                        case "2":
                            showDanger();
                            loadDrugDetail();
                            break;
                        case "1":
                            showWarning();
                            loadDrugDetail();
                            break;
                        default:
                            showSafety();
                            loadDrugDetail();
                            break;
                    }
                });
    }

    // ---------------------------------------------------------------
    // STEP 2. 데이터베이스에서 해당 제품명 검색
    // 제품이 존재하지 않으면 → NotFoundFragment 로 이동!!
    // ---------------------------------------------------------------
    private void loadDrugDetail() {

        db.collection("drugs_v2")
                .whereEqualTo("제품명", selectedProduct)
                .get()
                .addOnSuccessListener(q -> {

                    // ❗ DB에 해당 제품이 없으면 NotFound 페이지로 이동
                    if (q.isEmpty()) {
                        moveToNotFound();
                        return;
                    }

                    // 🔥 정상적인 경우
                    processDrug(q.getDocuments().get(0));
                });
    }

    // ---------------------------------------------------------------
    // STEP 3. 제품 정보(부작용, 효능) 화면에 표시
    // ---------------------------------------------------------------
    private void processDrug(DocumentSnapshot drug) {

        String side = drug.getString("부작용");
        if (side == null) side = "부작용 정보 없음";
        textSideEffect.setText(side);

        // -------- 효능 --------
        String usage = drug.getString("효능");
        if (usage == null) usage = "효능 정보 없음";

        // -------- 주의사항 --------
        String warningSummary = drug.getString("주의사항");
        if (warningSummary == null) warningSummary = "주의사항 정보 없음";

        // ZoomLayout 안에 전체 텍스트 구성
        StringBuilder sb = new StringBuilder();
        sb.append("● 효능\n");
        sb.append(usage);
        sb.append("\n\n");
        sb.append("● 주의사항\n");
        sb.append(warningSummary);

        textDetail.setText(sb.toString());

        textMore.setOnClickListener(v -> toggleDetail());
    }




    // 상세 정보 열기/닫기
    private void toggleDetail() {

        if (detailLayout.getVisibility() == View.GONE) {
            detailLayout.setVisibility(View.VISIBLE);
            textMore.setText("접기 ▲");
        } else {
            detailLayout.setVisibility(View.GONE);
            textMore.setText("더보기 ▼");
        }
    }

    // ---------------------------------------------------------------
    // STEP 4. 데이터베이스 없음 → NotFoundFragment 로 이동
    // ---------------------------------------------------------------
    private void moveToNotFound() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new NotFoundFragment())
                .addToBackStack(null)
                .commit();
    }

    // ---------------------------------------------------------------
    // UI 표시 함수들
    // ---------------------------------------------------------------
    private void showDanger() {
        textDanger.setText("DANGER\n매우 위험합니다!! 복용하지 마세요!!");
        textDanger.setTextColor(Color.parseColor("#C82828"));
        iconWarning.setImageResource(R.drawable.pillit_danger);
    }

    private void showWarning() {
        textDanger.setText("WARNING\n복용에 주의하세요!");
        textDanger.setTextColor(Color.parseColor("#E68A00"));
        iconWarning.setImageResource(R.drawable.pillit_warning);
    }

    private void showSafety() {
        textDanger.setText("SAFETY\n안전해요!");
        textDanger.setTextColor(Color.parseColor("#2AA93B"));
        iconWarning.setImageResource(R.drawable.pillit_safety);
    }
}
