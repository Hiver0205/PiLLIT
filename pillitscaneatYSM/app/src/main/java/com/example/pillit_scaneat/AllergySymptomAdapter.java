package com.example.pillit_scaneat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AllergySymptomAdapter extends BaseAdapter {

    private Context context;
    private List<String> allergyNames;
    private Map<String, Map<String, List<String>>> selectedMap;

    // 🔥 고정 증상 리스트
    private final List<String> mildSymptoms = List.of("가려움증", "국소 두드러기", "피부 발진");
    private final List<String> severeSymptoms = List.of(
            "아나필락시스", "호흡곤란", "저혈압", "의식 저하",
            "실신", "전신 두드러기", "혈관 부종", "심정지"
    );

    public AllergySymptomAdapter(Context context,
                                 List<String> allergyNames,
                                 Map<String, Map<String, List<String>>> selectedMap) {

        this.context = context;
        this.allergyNames = allergyNames;
        this.selectedMap = selectedMap;
    }

    @Override
    public int getCount() {
        return allergyNames.size();
    }

    @Override
    public Object getItem(int position) {
        return allergyNames.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_allergy_symptom, parent, false);
        }

        String ingredient = allergyNames.get(position);

        TextView tvIndex = convertView.findViewById(R.id.tvIndex);
        TextView tvName = convertView.findViewById(R.id.tvName);
        LinearLayout layoutSymptoms = convertView.findViewById(R.id.layoutSymptoms);
        LinearLayout layoutMild = convertView.findViewById(R.id.layoutMild);
        LinearLayout layoutSevere = convertView.findViewById(R.id.layoutSevere);
        View btnExpand = convertView.findViewById(R.id.btnExpand);

        tvIndex.setText(String.valueOf(position + 1));
        tvName.setText(ingredient);

        // ▼ 펼치기 기능
        btnExpand.setOnClickListener(v -> {
            if (layoutSymptoms.getVisibility() == View.VISIBLE)
                layoutSymptoms.setVisibility(View.GONE);
            else
                layoutSymptoms.setVisibility(View.VISIBLE);
        });

        // 기존 체크 상태 로드
        Map<String, List<String>> map = selectedMap.get(ingredient);
        List<String> mildList = map.get("mild");
        List<String> severeList = map.get("severe");

        layoutMild.removeAllViews();
        layoutSevere.removeAllViews();

        // 🔵 경증 증상 동적 생성
        for (String symptom : mildSymptoms) {

            CheckBox checkBox = new CheckBox(context);
            checkBox.setText(symptom);

            // 이미 체크된 경우 반영
            checkBox.setChecked(mildList.contains(symptom));

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!mildList.contains(symptom))
                        mildList.add(symptom);
                } else {
                    mildList.remove(symptom);
                }
            });

            layoutMild.addView(checkBox);
        }

        // 🔴 중증 증상 동적 생성
        for (String symptom : severeSymptoms) {

            CheckBox checkBox = new CheckBox(context);
            checkBox.setText(symptom);

            // 기존 체크 적용
            checkBox.setChecked(severeList.contains(symptom));

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!severeList.contains(symptom))
                        severeList.add(symptom);
                } else {
                    severeList.remove(symptom);
                }
            });

            layoutSevere.addView(checkBox);
        }

        return convertView;
    }
}
