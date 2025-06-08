package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class InquiryAdapter extends RecyclerView.Adapter<InquiryAdapter.ViewHolder> {

    private List<InquiryItem> inquiryList;
    private Context context;

    public InquiryAdapter(Context context, List<InquiryItem> inquiryList) {
        this.context = context;
        this.inquiryList = inquiryList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_inquiry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InquiryItem item = inquiryList.get(position);
        holder.title.setText(item.title);
        holder.date.setText(item.created_at);
        holder.status.setText(item.hasAnswer ? "답변 완료" : "답변 대기");

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, InquiryDetailActivity.class);
            intent.putExtra("inquiry_id", item.id);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return inquiryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, date, status;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.text_title);
            date = itemView.findViewById(R.id.text_date);
            status = itemView.findViewById(R.id.text_status);
        }
    }
}
