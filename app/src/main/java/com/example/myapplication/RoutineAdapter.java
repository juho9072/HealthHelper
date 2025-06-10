package com.example.myapplication;

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

public class RoutineAdapter extends RecyclerView.Adapter<RoutineAdapter.RoutineViewHolder> {

    public interface OnRoutineClickListener {
        void onRoutineClick(Routine routine);
    }

    private List<Routine> routineList;
    private OnRoutineClickListener listener;

    public RoutineAdapter(List<Routine> routineList, OnRoutineClickListener listener) {
        this.routineList = routineList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoutineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_routine, parent, false);
        return new RoutineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoutineViewHolder holder, int position) {
        Routine routine = routineList.get(position);
        holder.bind(routine);
    }

    @Override
    public int getItemCount() {
        return routineList.size();
    }

    class RoutineViewHolder extends RecyclerView.ViewHolder {
        TextView routineNameText, routineTagText, routineDescText;
        ImageView routineThumbnail;

        public RoutineViewHolder(@NonNull View itemView) {
            super(itemView);
            routineNameText = itemView.findViewById(R.id.routineNameText);
            routineTagText = itemView.findViewById(R.id.routineTagText);
            routineDescText = itemView.findViewById(R.id.routineDescText);
            routineThumbnail = itemView.findViewById(R.id.routineThumbnail); // 썸네일 추가
        }

        public void bind(Routine routine) {
            routineNameText.setText(routine.getName());
            routineTagText.setText("#" + routine.getTag());
            routineDescText.setText(routine.getDescription());

            // ✅ 썸네일 로딩
            String videoId = extractYoutubeVideoId(routine.getYoutubeLink());
            if (videoId != null) {
                String thumbnailUrl = "https://img.youtube.com/vi/" + videoId + "/0.jpg";
                Glide.with(itemView.getContext())
                        .load(thumbnailUrl)
                        .into(routineThumbnail);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRoutineClick(routine);
                }
            });
        }

        // ✅ 유튜브 링크에서 videoId 추출
        private String extractYoutubeVideoId(String url) {
            if (url == null) return null;
            String pattern = "(?<=watch\\?v=|youtu\\.be/|embed/)[^&\\n]+";
            Matcher matcher = Pattern.compile(pattern).matcher(url);
            if (matcher.find()) return matcher.group();
            return null;
        }
    }
}