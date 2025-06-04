package com.example.myapplication;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import android.app.Dialog;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.widget.SearchView;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class RoutineDialog extends DialogFragment {
    private int selectedRoutineId = -1;

    private Spinner tagSpinner;
    private SearchView searchView;
    private RecyclerView routineRecyclerView;
    private RoutineAdapter adapter;
    private List<Routine> routineList = new ArrayList<>();
    private List<Routine> filteredList = new ArrayList<>();

    private String[] tags = {"전체", "이두", "삼두", "등", "가슴", "하체", "어깨", "코어", "유산소"};

    // setRoutineList는 리스트만 저장
    public void setRoutineList(List<Routine> routines) {
        this.routineList = routines;
    }

    // 루틴 선택 후 호출될 콜백 인터페이스
    public interface OnRoutineSelectedListener {
        void onRoutineSelected(String selectedDate, int routineId);
    }

    private OnRoutineSelectedListener routineSelectedListener;

    public void setOnRoutineSelectedListener(OnRoutineSelectedListener listener) {
        this.routineSelectedListener = listener;
    }


    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.routine_selection_dialog, null);

        tagSpinner = view.findViewById(R.id.tagSpinner);
        searchView = view.findViewById(R.id.searchView);
        routineRecyclerView = view.findViewById(R.id.routineRecyclerView);

        // Spinner 세팅
        ArrayAdapter<String> tagAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, tags);
        tagAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tagSpinner.setAdapter(tagAdapter);

        // RecyclerView 세팅
// 루틴 어댑터 설정 (수정됨)
        adapter = new RoutineAdapter(filteredList, new RoutineAdapter.OnRoutineClickListener() {
            @Override
            public void onRoutineClick(Routine routine) {
                selectedRoutineId = routine.getId();  // 선택된 루틴 ID 저장
                String selectedDate = getArguments().getString("selectedDate");

                // 루틴 서버에 저장
                RequestQueue queue = Volley.newRequestQueue(requireContext());
                String url = "https://healthhelper.mycafe24.com/insert_routine_test.php";

                StringRequest request = new StringRequest(Request.Method.POST, url,
                        response -> {
                            Toast.makeText(getContext(), "루틴 저장 완료!", Toast.LENGTH_SHORT).show();

                            // 루틴 선택 콜백으로 전달 (한 번만)
                            if (routineSelectedListener != null) {
                                routineSelectedListener.onRoutineSelected(selectedDate, selectedRoutineId);
                            }

                            // 캘린더에 반영
                            if (getParentFragment() instanceof RecordFragment) {
                                ((RecordFragment) getParentFragment()).addRoutineDateToCalendar(selectedDate);
                            }

                            dismiss();  // 다이얼로그 닫기
                        },
                        error -> {
                            Toast.makeText(getContext(), "저장 실패: " + error.toString(), Toast.LENGTH_SHORT).show();
                        }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("date", selectedDate);
                        params.put("routine_id", String.valueOf(selectedRoutineId));
                        return params;
                    }
                };

                queue.add(request);
            }
        });

        routineRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        routineRecyclerView.setAdapter(adapter);

        // Spinner 이벤트
        tagSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterRoutine();
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // SearchView 이벤트
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String newText) {
                filterRoutine();
                return true;
            }
        });

        builder.setView(view)
                .setTitle("루틴 선택")
                .setNegativeButton("취소", null);

        // 여기서만 filterRoutine() 호출
        filterRoutine();

        return builder.create();
    }

    private void filterRoutine() {
        if (tagSpinner == null || searchView == null) return; // 안전성 강화

        String selectedTag = tagSpinner.getSelectedItem().toString();
        String keyword = searchView.getQuery().toString().toLowerCase();

        filteredList.clear();
        for (Routine r : routineList) {
            boolean matchTag = selectedTag.equals("전체") || r.getTag().equals(selectedTag);
            boolean matchKeyword = r.getName().toLowerCase().contains(keyword);
            if (matchTag && matchKeyword) {
                filteredList.add(r);
            }
        }
        adapter.notifyDataSetChanged();
    }
    public int getSelectedRoutineId() {
        return selectedRoutineId;
    }


}
