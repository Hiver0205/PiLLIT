package com.example.pillit_scaneat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private List<String> productList;
    private int selectedPosition = -1;   // 단일 선택
    private Context context;

    public ProductAdapter(Context context, List<String> productList) {
        this.context = context;
        this.productList = productList;
    }

    public String getSelectedProduct() {
        if (selectedPosition != -1)
            return productList.get(selectedPosition);
        return null;
    }

    @NonNull
    @Override
    public ProductAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_product_checkbox, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductAdapter.ViewHolder holder, int position) {

        String name = productList.get(position);
        holder.textProductName.setText(name);

        holder.checkBox.setChecked(position == selectedPosition);

        // 항목 클릭 시 단일 선택 처리
        View.OnClickListener listener = v -> {
            selectedPosition = position;
            notifyDataSetChanged();
        };
        holder.itemView.setOnClickListener(listener);
        holder.checkBox.setOnClickListener(listener);
    }


    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textProductName;
        CheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textProductName = itemView.findViewById(R.id.textProductName);
            checkBox = itemView.findViewById(R.id.checkProduct);
        }
    }
}
