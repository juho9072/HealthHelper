package com.example.myapplication;

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