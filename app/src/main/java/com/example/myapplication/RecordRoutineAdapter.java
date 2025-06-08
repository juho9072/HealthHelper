package com.example.myapplication;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecordRoutineAdapter extends RecyclerView.Adapter<RecordRoutineAdapter.ViewHolder> {

    public interface OnRoutineClickListener {
        void onRoutineClick(Set<Integer> selectedRoutineIds);
    }

    private List<Routine> routineList;
    private final OnRoutineClickListener listener;
    private final Set<Integer> selectedRoutineIds = new HashSet<>();

    public RecordRoutineAdapter(List<Routine> routines, OnRoutineClickListener listener) {
        this.routineList = routines;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_routine, parent, false);
        return new ViewHolder(view);
    }
    public void setRoutineList(List<Routine> routines) {
        this.routineList = routines;
        notifyDataSetChanged();
    }

    public void setSelectedRoutineIds(Set<Integer> selectedIds) {
        selectedRoutineIds.clear();
        selectedRoutineIds.addAll(selectedIds);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Routine routine = routineList.get(position);
        holder.routineNameText.setText(routine.getName());
        holder.routineTagText.setText(routine.getTag());
        holder.routineDescText.setText(routine.getDescription());

        // 유튜브 썸네일 URL 만들기
        String videoId = extractVideoIdFromUrl(routine.getYoutubeLink());
        String thumbnailUrl = "https://img.youtube.com/vi/" + videoId + "/0.jpg";

        Glide.with(holder.routineThumbnail.getContext())
                .load(thumbnailUrl)
                .placeholder(R.drawable.ic_data)
                .into(holder.routineThumbnail);

        // 선택된 항목 배경색 표시
        if (selectedRoutineIds.contains(routine.getId())) {
            holder.itemView.setBackgroundColor(Color.parseColor("#333333"));
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private String extractVideoIdFromUrl(String url) {
        if (url == null) return "";
        String pattern = "v=([^&]+)";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    @Override
    public int getItemCount() {
        return routineList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView routineNameText, routineTagText, routineDescText;
        ImageView routineThumbnail;

        ViewHolder(View itemView) {
            super(itemView);
            routineNameText = itemView.findViewById(R.id.routineNameText);
            routineTagText = itemView.findViewById(R.id.routineTagText);
            routineDescText = itemView.findViewById(R.id.routineDescText);
            routineThumbnail = itemView.findViewById(R.id.routineThumbnail);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    int routineId = routineList.get(position).getId();
                    if (selectedRoutineIds.contains(routineId)) {
                        selectedRoutineIds.remove(routineId);
                    } else {
                        selectedRoutineIds.add(routineId);
                    }
                    notifyItemChanged(position);
                    listener.onRoutineClick(selectedRoutineIds);
                }
            });
        }
    }


}
