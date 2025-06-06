package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class RoutineFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // fragment_routine.xml을 뷰로 연결
        View view = inflater.inflate(R.layout.fragment_routine, container, false);

        // "루틴 등록하기" 버튼 처리
        Button btnGoToUpload = view.findViewById(R.id.buttonblue);
        btnGoToUpload.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), RoutineUploadActivity.class);
            startActivity(intent);
        });

        // "루틴 시작하기" 버튼 처리
        Button btnStartRoutine = view.findViewById(R.id.buttonwhite);
        btnStartRoutine.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), RoutineStartActivity.class);
            startActivity(intent);
        });

        return view;
    }
}