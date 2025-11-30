package com.example.pillit_scaneat;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ProductDetailFragment extends Fragment {

    private String productId = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_product_detail, container, false);

        if (getArguments() != null) {
            productId = getArguments().getString("productId", "");
        }

        TextView text = view.findViewById(R.id.textProductId);
        text.setText("제품명: " + productId);

        return view;
    }
}
