package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

        public RoutineViewHolder(@NonNull View itemView) {
            super(itemView);
            routineNameText = itemView.findViewById(R.id.routineNameText);
            routineTagText = itemView.findViewById(R.id.routineTagText);
            routineDescText = itemView.findViewById(R.id.routineDescText);
        }

        public void bind(Routine routine) {
            routineNameText.setText(routine.getName());
            routineTagText.setText("#" + routine.getTag());
            routineDescText.setText(routine.getDescription());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRoutineClick(routine);
                }
            });
        }
    }
}
