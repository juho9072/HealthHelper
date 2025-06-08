package com.example.myapplication;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RoutineDetailAdapter extends RecyclerView.Adapter<RoutineDetailAdapter.ViewHolder> {

    private final List<Routine> routineList;

    public RoutineDetailAdapter(List<Routine> routineList) {
        this.routineList = routineList;
    }

    @NonNull
    @Override
    public RoutineDetailAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_detail_routine, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoutineDetailAdapter.ViewHolder holder, int position) {
        Routine routine = routineList.get(position);
        holder.tvRoutineTitle.setText(routine.getName());
        holder.tvRoutineTag.setText(routine.getTag());

        // 유튜브 썸네일 URL 생성
        String videoId = extractVideoIdFromUrl(routine.getYoutubeLink());
        String thumbnailUrl = "https://img.youtube.com/vi/" + videoId + "/0.jpg";

        Glide.with(holder.ivThumbnail.getContext())
                .load(thumbnailUrl)
                .placeholder(R.drawable.ic_data)
                .into(holder.ivThumbnail);


    }

    @Override
    public int getItemCount() {
        return routineList.size();
    }

    private String extractVideoIdFromUrl(String url) {
        if (url == null || url.isEmpty()) return "";

        String pattern1 = "v=([^&]+)";
        String pattern2 = "youtu\\.be/([^?&]+)";

        Pattern p1 = Pattern.compile(pattern1);
        Matcher m1 = p1.matcher(url);
        if (m1.find()) {
            String id = m1.group(1);
            Log.d("VideoID", "Found video ID (pattern1): " + id);
            return id;
        }

        Pattern p2 = Pattern.compile(pattern2);
        Matcher m2 = p2.matcher(url);
        if (m2.find()) {
            String id = m2.group(1);
            Log.d("VideoID", "Found video ID (pattern2): " + id);
            return id;
        }

        Log.d("VideoID", "No video ID found in URL: " + url);
        return "";
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumbnail;
        TextView tvRoutineTitle, tvRoutineTag;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            tvRoutineTitle = itemView.findViewById(R.id.tvRoutineName);
            tvRoutineTag = itemView.findViewById(R.id.tvRoutineTag);  // 새로 추가된 태그 텍스트뷰
        }
    }

}
