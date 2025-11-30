package com.example.pillit_scaneat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Set;

public class AllergyAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> items;
    private Set<String> selectedSet;
    private OnAllergyCheckListener listener;

    public interface OnAllergyCheckListener {
        void onCheckChanged(String ingredient, boolean isChecked);
    }

    public AllergyAdapter(Context context, ArrayList<String> items,
                          Set<String> selectedSet,
                          OnAllergyCheckListener listener) {
        this.context = context;
        this.items = items;
        this.selectedSet = selectedSet;
        this.listener = listener;
    }

    @Override
    public int getCount() { return items.size(); }

    @Override
    public Object getItem(int i) { return items.get(i); }

    @Override
    public long getItemId(int i) { return i; }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_allergy, parent, false);
        }

        CheckBox check = convertView.findViewById(R.id.checkAllergy);
        TextView text = convertView.findViewById(R.id.textAllergy);

        String ingredient = items.get(pos);
        text.setText(ingredient);

        check.setOnCheckedChangeListener(null);
        check.setChecked(selectedSet.contains(ingredient));

        check.setOnCheckedChangeListener((buttonView, isChecked) -> {
            listener.onCheckChanged(ingredient, isChecked);

            if (isChecked) selectedSet.add(ingredient);
            else selectedSet.remove(ingredient);
        });

        return convertView;
    }
}
