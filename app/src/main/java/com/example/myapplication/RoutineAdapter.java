package com.example.myapplication;

import android.net.Uri;
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
            routineThumbnail = itemView.findViewById(R.id.routineThumbnail); // ✅ ImageView 연결
        }

        public void bind(Routine routine) {
            routineNameText.setText(routine.getName());
            routineTagText.setText("#" + routine.getTag());
            routineDescText.setText(routine.getYoutubeLink());

            // ✅ 썸네일 URL 생성
            String videoId = extractYoutubeId(routine.getYoutubeLink());
            if (!videoId.isEmpty()) {
                String thumbnailUrl = "https://img.youtube.com/vi/" + videoId + "/0.jpg";
                Log.d("썸네일URL", thumbnailUrl);

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

        private String extractYoutubeId(String url) {
            try {
                Uri uri = Uri.parse(url);
                if (url.contains("youtu.be/")) {
                    String path = uri.getPath(); // 예: /rlegQPbe6vE
                    return path != null ? path.replace("/", "") : "";
                } else if (url.contains("youtube.com/watch")) {
                    return uri.getQueryParameter("v");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }
    }
}