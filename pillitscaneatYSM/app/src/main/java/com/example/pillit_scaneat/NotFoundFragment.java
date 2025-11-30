package com.example.pillit_scaneat;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class NotFoundFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_not_found, container, false);

        Button btnRetry = view.findViewById(R.id.btnRetry);

        // 다시 스캔하기 → ScanFragment 로 이동
        btnRetry.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ScanFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}
