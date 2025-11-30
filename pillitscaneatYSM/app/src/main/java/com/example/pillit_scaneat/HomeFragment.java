package com.example.pillit_scaneat;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ⭐ S22 기준 하단 내비게이션 영역 자동 패딩 적용 (잘림 방지)
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {

            int bottomInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;

            // 기존 padding 유지하면서 bottom만 추가
            v.setPadding(
                    v.getPaddingLeft(),
                    v.getPaddingTop(),
                    v.getPaddingRight(),
                    bottomInset
            );

            return insets; // 꼭 반환해야 함
        });
    }
}
