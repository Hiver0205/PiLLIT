package com.example.pillit_scaneat;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProductSelectFragment extends Fragment {

    private TextView textIngredientName;
    private EditText editSearch;
    private RecyclerView recyclerProducts;
    private Button btnScanAgain;
    private Button btnDone;
    private ImageView imgRetry;
    private TextView textEmptyMessage;

    private FirebaseFirestore db;

    private ProductAdapter adapter;

    private String ocrIngredient = "";
    private List<String> fullProductList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_product_select, container, false);

        // UI 연결
        textIngredientName = view.findViewById(R.id.textOcrIngredient);
        editSearch = view.findViewById(R.id.editSearch);
        recyclerProducts = view.findViewById(R.id.recyclerProductList);
        btnScanAgain = view.findViewById(R.id.btnScanAgain);
        btnDone = view.findViewById(R.id.btnDone);
        imgRetry = view.findViewById(R.id.imgRetry);
        textEmptyMessage = view.findViewById(R.id.textEmptyMessage);

        recyclerProducts.setLayoutManager(new LinearLayoutManager(getContext()));

        db = FirebaseFirestore.getInstance();

        // OCR 성분명 받기
        if (getArguments() != null) {
            ocrIngredient = getArguments().getString("ingredient", "");
            textIngredientName.setText("인식한 약 성분명 : " + ocrIngredient);
        }

        // Firestore에서 제품명 로드
        loadProductsFromDB();

        // 검색 기능
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }
        });

        // 다시 스캔하기
        btnScanAgain.setOnClickListener(v -> {
            Fragment fragment = new ScanMenuFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        });

        // 선택 완료
        btnDone.setOnClickListener(v -> saveSelectedProduct());

        return view;
    }

    // -------------------------------------------------------------
    // Firestore(drugs_v2)에서 해당 성분의 제품명 찾기
    // -------------------------------------------------------------
    private void loadProductsFromDB() {

        db.collection("drugs_v2")
                .get()
                .addOnSuccessListener(q -> {

                    fullProductList.clear();
                    boolean foundIngredient = false;

                    for (QueryDocumentSnapshot doc : q) {

                        String ingredient = doc.getString("주성분");
                        String productName = doc.getString("제품명");

                        if (ingredient == null) continue;
                        if (productName == null) continue;

                        if (ingredient.equalsIgnoreCase(ocrIngredient)) {
                            foundIngredient = true;
                            fullProductList.add(productName);
                        }
                    }

                    if (!foundIngredient || fullProductList.isEmpty()) {
                        showRetryUI();
                        return;
                    }

                    showProductListUI();
                });
    }

    // -------------------------------------------------------------
    // 제품명이 있을 때 화면 구성
    // -------------------------------------------------------------
    private void showProductListUI() {

        imgRetry.setVisibility(View.GONE);
        textEmptyMessage.setVisibility(View.GONE);
        btnScanAgain.setVisibility(View.GONE);

        adapter = new ProductAdapter(getContext(), new ArrayList<>(fullProductList));
        recyclerProducts.setAdapter(adapter);
    }

    // -------------------------------------------------------------
    // 제품명 없음 → Retry 화면
    // -------------------------------------------------------------
    private void showRetryUI() {

        recyclerProducts.setVisibility(View.GONE);

        imgRetry.setVisibility(View.VISIBLE);
        textEmptyMessage.setVisibility(View.VISIBLE);
        btnScanAgain.setVisibility(View.VISIBLE);

        btnDone.setVisibility(View.GONE);
    }

    // -------------------------------------------------------------
    // 🔍 검색 필터
    // -------------------------------------------------------------
    private void filterProducts(String query) {

        List<String> filtered = new ArrayList<>();

        for (String p : fullProductList) {
            if (p.toLowerCase().contains(query.toLowerCase())) {
                filtered.add(p);
            }
        }

        adapter = new ProductAdapter(getContext(), filtered);
        recyclerProducts.setAdapter(adapter);
    }

    // -------------------------------------------------------------
    // ✔ 선택한 제품 Firestore 저장 + ResultFragment 이동
    // -------------------------------------------------------------
    private void saveSelectedProduct() {

        String selected = adapter.getSelectedProduct();

        if (selected == null) {
            Toast.makeText(getContext(), "제품을 선택해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .collection("selectedProducts")
                .document(ocrIngredient)
                .set(new SelectedProduct(selected))
                .addOnSuccessListener(v -> {

                    Toast.makeText(getContext(), "저장 완료!", Toast.LENGTH_SHORT).show();

                    // 🔥 저장 성공 → ResultFragment로 이동
                    ResultFragment fragment = new ResultFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("selectedIngredient", ocrIngredient);
                    bundle.putString("selectedProduct", selected);
                    fragment.setArguments(bundle);

                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit();
                });
    }

    // Firestore 저장용 모델
    public static class SelectedProduct {
        public String product;

        public SelectedProduct() {}
        public SelectedProduct(String product) {
            this.product = product;
        }
    }
}
