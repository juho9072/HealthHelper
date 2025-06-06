package com.example.myapplication;

<<<<<<< HEAD
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.Routine;

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
=======
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class RoutineAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<RoutineItem> routines;

    public RoutineAdapter(Context context, ArrayList<RoutineItem> routines) {
        this.context = context;
        this.routines = routines;
    }

    @Override
    public int getCount() {
        return routines.size();
    }

    @Override
    public Object getItem(int position) {
        return routines.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.routine_list_item, parent, false);
        }

        TextView title = view.findViewById(R.id.title);
        ImageView thumbnail = view.findViewById(R.id.thumbnail);

        RoutineItem item = routines.get(position);
        title.setText(item.title);

        Glide.with(context).load(item.thumbnailUrl).into(thumbnail);

        // 항목 클릭 시 상세 화면으로 이동
        view.setOnClickListener(v -> {
            Intent intent = new Intent(context, RoutineDetailActivity.class);
            intent.putExtra("title", item.title);
            intent.putExtra("link", item.youtubeLink); // 유튜브 원본 링크 전달
            ((Activity) context).startActivity(intent); // context를 Activity로 캐스팅
        });

        return view;
    }
}
>>>>>>> main
