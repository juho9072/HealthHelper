package com.example.myapplication;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;


import android.util.Log;

import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.kizitonwose.calendarview.CalendarView;
import com.kizitonwose.calendarview.model.CalendarDay;
import com.kizitonwose.calendarview.model.DayOwner;
import com.kizitonwose.calendarview.ui.DayBinder;
import com.kizitonwose.calendarview.ui.ViewContainer;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.time.DayOfWeek;



public class RecordFragment extends Fragment {
    private int selectedRoutineId = -1;
    private RadarChart radarChart;
    private Spinner periodSpinner;
    private final String[] TAGS = {"이두", "삼두", "어깨", "등", "가슴", "하체", "유산소", "코어"};

    private void fetchRoutinesFromServer(LocalDate date) {
        String url = "https://healthhelper.mycafe24.com/get_routines_test.php";
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        List<Routine> routines = new ArrayList<>();
                        org.json.JSONArray jsonArray = new org.json.JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            org.json.JSONObject obj = jsonArray.getJSONObject(i);
                            int id = obj.getInt("routine_id");
                            String name = obj.getString("name");
                            String description = obj.getString("description");
                            String tag = obj.getString("tag");
                            routines.add(new Routine(id, name, description, tag));
                        }

                        // ✅ 날짜 형식 문자열로 포맷
                        String formattedDate = date.format(dateFormatter);

                        // ✅ 루틴 다이얼로그 만들고 데이터 넣기
                        RoutineDialog dialog = new RoutineDialog();
                        dialog.setRoutineList(routines);

                        // ✅ 날짜 전달
                        Bundle bundle = new Bundle();
                        bundle.putString("selectedDate", formattedDate);
                        dialog.setArguments(bundle);

                        dialog.setOnRoutineSelectedListener(new RoutineDialog.OnRoutineSelectedListener() {
                            @Override
                            public void onRoutineSelected(String selectedDate, int routineId) {
                                selectedRoutineId = routineId; // 루틴 ID 저장
                                showEditInputDialog(selectedDate);
                            }
                        });


                        dialog.show(getParentFragmentManager(), "RoutineDialog");

                    } catch (Exception e) {
                        Toast.makeText(getContext(), "루틴 파싱 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "루틴 불러오기 실패", Toast.LENGTH_SHORT).show()
        );


        queue.add(request);
    }

    public void addRoutineDateToCalendar(String dateString) {
        LocalDate date = LocalDate.parse(dateString, dateFormatter);
        recordedDates.add(date);
        calendarView.notifyDateChanged(date);
    }



    private void checkRoutineForDate(LocalDate date) {
        String dateStr = date.format(dateFormatter);
        String url = "https://healthhelper.mycafe24.com/get_routine_by_date_test.php?date=" + dateStr;

        RequestQueue queue = Volley.newRequestQueue(requireContext());

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        if (response.equals("null")) {
                            // ✅ 데이터 없음 → 선택 다이얼로그 띄우기
                            fetchRoutinesFromServer(date);
                        } else {
                            // ✅ 데이터 있음 → JSON 파싱해서 보기용 다이얼로그 띄우기
                            org.json.JSONObject obj = new org.json.JSONObject(response);
                            String name = obj.getString("name");
                            String description = obj.getString("description");
                            String tag = obj.getString("tag");

                            showRoutineDetailDialog(dateStr, name, description, tag);
                        }
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "루틴 확인 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "루틴 확인 실패", Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }

    private void showRoutineDetailDialog(String date, String name, String description, String tag) {
        String message = "루틴 이름: " + name + "\n" +
                "부위 태그: " + tag + "\n\n" +
                "설명:\n" + description;

        new AlertDialog.Builder(getContext())
                .setTitle(date + " 루틴 기록")
                .setMessage(message)
                .setPositiveButton("확인", null)
                .show();
    }

    public void showInputOrDetailDialog(String date) {
        LayoutInflater dialogInflater = LayoutInflater.from(getContext());
        View dialogView = dialogInflater.inflate(R.layout.inbody_input, null);

        EditText etWeight = dialogView.findViewById(R.id.et_weight);
        EditText etMuscle = dialogView.findViewById(R.id.et_muscle);
        EditText etFat = dialogView.findViewById(R.id.et_fat);
        EditText etScore = dialogView.findViewById(R.id.et_score);

        boolean hasData = hasDataForDate(LocalDate.parse(date, dateFormatter));

        if (hasData) {
            String url = "https://healthhelper.mycafe24.com/get_inbody.php?date=" + date;
            RequestQueue queue = Volley.newRequestQueue(requireContext());

            StringRequest request = new StringRequest(Request.Method.GET, url,
                    response -> {
                        try {
                            JSONObject obj = new JSONObject(response);
                            etWeight.setText(obj.getString("weight") + "kg");
                            etMuscle.setText(obj.getString("muscle") + "kg");
                            etFat.setText(obj.getString("fat") + "kg");
                            etScore.setText(obj.getString("score") + "점");


                        } catch (Exception e) {
                            Toast.makeText(getContext(), "데이터 파싱 실패", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> Toast.makeText(getContext(), "데이터 가져오기 실패", Toast.LENGTH_SHORT).show()
            );

            queue.add(request);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setTitle(date + " 인바디 기록")
                .setView(dialogView)
                .setNegativeButton("취소", null);

        if (!hasData) {
            builder.setPositiveButton("저장", (dialog, which) -> {
                String weight = etWeight.getText().toString();
                String muscle = etMuscle.getText().toString();
                String fat = etFat.getText().toString();
                String score = etScore.getText().toString();

                String url = "https://healthhelper.mycafe24.com/insert_inbody.php";
                RequestQueue queue = Volley.newRequestQueue(requireContext());

                StringRequest request = new StringRequest(Request.Method.POST, url,
                        response -> {
                            Toast.makeText(getContext(), "서버 응답: " + response, Toast.LENGTH_SHORT).show();

                            LocalDate savedDate = LocalDate.parse(date, dateFormatter);
                            recordedDates.add(savedDate);
                            calendarView.notifyDateChanged(savedDate);
                        },
                        error -> Toast.makeText(getContext(), "에러 발생: " + error.toString(), Toast.LENGTH_SHORT).show()) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();
                        params.put("date", date);
                        params.put("weight", weight);
                        params.put("muscle", muscle);
                        params.put("fat", fat);
                        params.put("score", score);
                        return params;
                    }
                };

                queue.add(request);
            });
        }

        builder.show();
    }



    private LocalDate selectedDate;
    private final Set<LocalDate> recordedDates = new HashSet<>();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private CalendarView calendarView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_record, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        TextView monthText = view.findViewById(R.id.monthText);

        // ✅ [추가] 레이더차트 및 스피너 연결
        radarChart = view.findViewById(R.id.radarChart);
        periodSpinner = view.findViewById(R.id.periodSpinner);

        // ✅ [추가] 스피너 어댑터 설정
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item,
                new String[]{"1주", "1달", "1년", "전체"});
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        periodSpinner.setAdapter(spinnerAdapter);

        // ✅ [추가] 스피너 이벤트 연결
        periodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateRadarChartForPeriod(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 선택 안 했을 때 처리 안 해도 됩니다
            }
        });



        // 캘린더 설정
        YearMonth currentMonth = YearMonth.now();
        calendarView.setup(currentMonth, currentMonth, DayOfWeek.SUNDAY);
        calendarView.scrollToMonth(currentMonth);
        fetchRecordedDates(() -> updateRadarChartForPeriod(0));

        DateTimeFormatter titleFormatter = DateTimeFormatter.ofPattern("yyyy년 M월");
        monthText.setText(titleFormatter.format(currentMonth));

        calendarView.setDayBinder(new DayBinder<DayViewContainer>() {
            @Override
            public DayViewContainer create(View view) {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(DayViewContainer container, CalendarDay day) {
                container.bind(day);
            }
        });
        periodSpinner.setSelection(0); // 1주 선택
        updateRadarChartForPeriod(0);  // 즉시 차트 업데이트
        return view;
    }

    // 날짜 뷰 컨테이너
    class DayViewContainer extends ViewContainer {
        private final TextView textView;
        private CalendarDay day;

        DayViewContainer(View view) {
            super(view);
            textView = view.findViewById(R.id.calendarDayText);

            view.setOnClickListener(v -> {
                if (day != null && day.getOwner() == DayOwner.THIS_MONTH) {
                    selectedDate = day.getDate();
                    String formattedDate = selectedDate.format(dateFormatter);

                    if (hasDataForDate(selectedDate)) {
                        // ✅ 데이터 있음 → 인바디+루틴 함께 보여주기
                        showCombinedInbodyAndRoutineDialog(formattedDate);
                    } else {
                        // ✅ 데이터 없음 → 루틴 → 인바디 입력 순서
                        fetchRoutinesFromServerThenOpenDialog(selectedDate);
                    }
                }
            });
        }

        void bind(CalendarDay day) {
            this.day = day;
            textView.setText(String.valueOf(day.getDate().getDayOfMonth()));

            if (day.getOwner() == DayOwner.THIS_MONTH) {
                textView.setVisibility(View.VISIBLE);
                textView.setTextColor(Color.BLACK);

                if (hasDataForDate(day.getDate())) {
                    textView.setBackgroundColor(Color.parseColor("#EEEEEE")); // 연한 회색
                } else {
                    textView.setBackgroundColor(Color.WHITE); // 평범한 날
                }

            } else {
                textView.setVisibility(View.INVISIBLE);
                textView.setBackground(null); // 캘린더 외부 날짜 숨김
            }
        }

        void clear() {
            this.day = null;
            textView.setVisibility(View.INVISIBLE);
        }
    }

    // 예시용 데이터 존재 여부 체크
    private boolean hasDataForDate(LocalDate date) {
        return recordedDates.contains(date);
    }

    private void fetchRecordedDates(Runnable onFinished) {
        String url = "https://healthhelper.mycafe24.com/get_routine_recorded_dates.php";

        RequestQueue queue = Volley.newRequestQueue(requireContext());

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        org.json.JSONArray jsonArray = new org.json.JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            String dateStr = jsonArray.getString(i);
                            LocalDate date = LocalDate.parse(dateStr, dateFormatter);

                            recordedDates.add(date);
                            calendarView.notifyDateChanged(date);
                        }
                        // ✅ 날짜 다 받은 뒤 실행
                        if (onFinished != null) onFinished.run();

                    } catch (Exception e) {
                        Toast.makeText(getContext(), "날짜 파싱 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "날짜 가져오기 실패", Toast.LENGTH_SHORT).show());

        queue.add(request);
    }

    private void fetchRoutinesFromServerThenOpenDialog(LocalDate date) {
        String url = "https://healthhelper.mycafe24.com/get_routines_test.php";
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        List<Routine> routines = new ArrayList<>();
                        org.json.JSONArray jsonArray = new org.json.JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            org.json.JSONObject obj = jsonArray.getJSONObject(i);
                            int id = obj.getInt("routine_id");
                            String name = obj.getString("name");
                            String description = obj.getString("description");
                            String tag = obj.getString("tag");
                            routines.add(new Routine(id, name, description, tag));
                        }

                        String formattedDate = date.format(dateFormatter);

                        RoutineDialog dialog = new RoutineDialog();
                        dialog.setRoutineList(routines);


                        Bundle bundle = new Bundle();
                        bundle.putString("selectedDate", formattedDate);
                        dialog.setArguments(bundle);


                        dialog.setOnRoutineSelectedListener(new RoutineDialog.OnRoutineSelectedListener() {
                            @Override
                            public void onRoutineSelected(String selectedDate, int routineId) {
                                selectedRoutineId = routineId;
                                showInputOrDetailDialog(selectedDate);
                            }
                        });


                        dialog.show(getParentFragmentManager(), "RoutineDialog");

                    } catch (Exception e) {
                        Toast.makeText(getContext(), "루틴 파싱 오류: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "루틴 불러오기 실패", Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }


    private void showCombinedInbodyAndRoutineDialog(String date) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_inbody_routine, null);

        // 인바디 View
        EditText etWeight = dialogView.findViewById(R.id.et_weight);
        EditText etMuscle = dialogView.findViewById(R.id.et_muscle);
        EditText etFat = dialogView.findViewById(R.id.et_fat);
        EditText etScore = dialogView.findViewById(R.id.et_score);

        // 루틴 View
        TextView tvRoutineName = dialogView.findViewById(R.id.tv_routine_name);
        TextView tvRoutineTag = dialogView.findViewById(R.id.tv_routine_tag);
        TextView tvRoutineDesc = dialogView.findViewById(R.id.tv_routine_desc);

        // 인바디 데이터 불러오기
        String inbodyUrl = "https://healthhelper.mycafe24.com/get_inbody.php?date=" + date;
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        StringRequest inbodyRequest = new StringRequest(Request.Method.GET, inbodyUrl,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        etWeight.setText(obj.getString("weight") + "kg");
                        etMuscle.setText(obj.getString("muscle") + "kg");
                        etFat.setText(obj.getString("fat") + "kg");
                        etScore.setText(obj.getString("score") + "점");

                        // 비활성화
                        etWeight.setEnabled(false);
                        etMuscle.setEnabled(false);
                        etFat.setEnabled(false);
                        etScore.setEnabled(false);
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "인바디 파싱 오류", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "인바디 가져오기 실패", Toast.LENGTH_SHORT).show()
        );

        queue.add(inbodyRequest);

        // 루틴 데이터 불러오기
        String routineUrl = "https://healthhelper.mycafe24.com/get_routine_by_date_test.php?date=" + date;
        StringRequest routineRequest = new StringRequest(Request.Method.GET, routineUrl,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        tvRoutineName.setText(obj.getString("name"));
                        tvRoutineTag.setText(obj.getString("tag"));
                        tvRoutineDesc.setText(obj.getString("description"));
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "루틴 파싱 오류", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "루틴 가져오기 실패", Toast.LENGTH_SHORT).show()
        );

        queue.add(routineRequest);

        // 다이얼로그 띄우기
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(date + " 기록")
                .setView(dialogView)
                .create();

        dialog.show();

// 버튼 클릭 이벤트 설정
        Button btnEdit = dialogView.findViewById(R.id.btnEdit);
        Button btnDelete = dialogView.findViewById(R.id.btnDelete);
        Button btnClose = dialogView.findViewById(R.id.btnClose);

        btnEdit.setOnClickListener(v -> {
            dialog.dismiss();
            startEditFlow(date);  // 수정 로직 실행
        });

        btnDelete.setOnClickListener(v -> {
            dialog.dismiss();
            showDeleteConfirmationDialog(date);  // 삭제 로직 실행
        });

        btnClose.setOnClickListener(v -> {
            dialog.dismiss();  // 닫기만
        });

    }

    private void showDeleteConfirmationDialog(String date) {
        new AlertDialog.Builder(getContext())
                .setTitle("삭제 확인")
                .setMessage(date + "의 기록을 삭제하시겠습니까?")
                .setPositiveButton("삭제", (dialog, which) -> {
                    RequestQueue queue = Volley.newRequestQueue(requireContext());

                    String deleteInbodyUrl = "https://healthhelper.mycafe24.com/delete_inbody.php?date=" + date;
                    String deleteRoutineUrl = "https://healthhelper.mycafe24.com/delete_routine_by_date.php?date=" + date;

                    Log.d("DELETE", "Inbody URL: " + deleteInbodyUrl);
                    Log.d("DELETE", "Routine URL: " + deleteRoutineUrl);

                    // ✅ 인바디 삭제 요청
                    StringRequest deleteInbodyReq = new StringRequest(Request.Method.GET, deleteInbodyUrl,
                            inbodyResp -> {
                                Log.d("DELETE", "Inbody response: " + inbodyResp);

                                if (inbodyResp.trim().equals("success")) {
                                    // ✅ 인바디 삭제 성공 → 루틴도 삭제
                                    StringRequest deleteRoutineReq = new StringRequest(Request.Method.GET, deleteRoutineUrl,
                                            routineResp -> {
                                                Log.d("DELETE", "Routine response: " + routineResp);

                                                if (routineResp.trim().equals("success")) {
                                                    Toast.makeText(getContext(), "기록 삭제 완료", Toast.LENGTH_SHORT).show();
                                                    LocalDate deletedDate = LocalDate.parse(date, dateFormatter);
                                                    recordedDates.remove(deletedDate);
                                                    calendarView.notifyDateChanged(deletedDate);
                                                } else {
                                                    Toast.makeText(getContext(), "루틴 삭제 실패", Toast.LENGTH_SHORT).show();
                                                }
                                            },
                                            err -> Toast.makeText(getContext(), "루틴 삭제 통신 오류", Toast.LENGTH_SHORT).show()
                                    );
                                    queue.add(deleteRoutineReq);
                                } else {
                                    Toast.makeText(getContext(), "인바디 삭제 실패 (응답 FAIL)", Toast.LENGTH_SHORT).show();
                                }
                            },
                            error -> Toast.makeText(getContext(), "인바디 삭제 통신 오류", Toast.LENGTH_SHORT).show()
                    );

                    queue.add(deleteInbodyReq);
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void updateRadarChartForPeriod(int position) {
        LocalDate now = LocalDate.now();
        LocalDate startDate;

        switch (position) {
            case 0: startDate = now.minusWeeks(1); break;
            case 1: startDate = now.minusMonths(1); break;
            case 2: startDate = now.minusYears(1); break;
            default: startDate = LocalDate.MIN; break;
        }

        Map<String, Integer> tagCount = new HashMap<>();
        for (String tag : TAGS) tagCount.put(tag, 0);

        List<String> targetDateStrings = new ArrayList<>();
        for (LocalDate date : recordedDates) {
            Log.d("RECORD_DATE_ALL", date.toString());
            if (!date.isBefore(startDate) && !date.isAfter(now)) {
                targetDateStrings.add(date.format(dateFormatter));
            }
        }

        if (targetDateStrings.isEmpty()) {
            drawRadarChart(tagCount);
            return;
        }

        // ✅ 비동기 fetch 완료 추적용
        int total = targetDateStrings.size();
        int[] completed = {0};

        for (String dateStr : targetDateStrings) {
            Log.d("RADAR_FETCH", "fetchTagByDate 호출: " + dateStr);
            fetchTagByDate(dateStr, tagCount, () -> {
                completed[0]++;
                if (completed[0] == total) {
                    // 모든 fetch가 끝난 후에만 차트 그리기
                    drawRadarChart(tagCount);
                }
            });
        }
    }

    private void fetchTagByDate(String date, Map<String, Integer> tagCount, Runnable onComplete) {
        String url = "https://healthhelper.mycafe24.com/get_routine_by_date_test.php?date=" + date;

        RequestQueue queue = Volley.newRequestQueue(requireContext());

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        String tag = obj.getString("tag");

                        if (tagCount.containsKey(tag)) {
                            int count = tagCount.get(tag);
                            tagCount.put(tag, count + 1);
                            Log.d("TAG_COUNT", date + " → " + tag + ": " + (count + 1));
                        }
                    } catch (Exception e) {
                        Log.d("TAG_COUNT", "tag 파싱 실패: " + date + " 응답 = " + response);
                        Log.e("TAG_PARSE", "error", e);
                    } finally {
                        onComplete.run(); // 반드시 콜백 호출
                    }
                },
                error -> {
                    Log.e("TAG_FETCH_FAIL", "통신 에러: " + error.getMessage());
                    onComplete.run(); // 실패해도 콜백 호출
                });

        queue.add(request);
    }



    // 콜백 인터페이스 정의
    interface FetchTagCallback {
        void onTagFetched();
    }

    private void drawRadarChart(Map<String, Integer> tagCount) {
        List<RadarEntry> entries = new ArrayList<>();
        for (String tag : TAGS) {
            entries.add(new RadarEntry(tagCount.getOrDefault(tag, 0)));
        }

        RadarDataSet dataSet = new RadarDataSet(entries, "운동 비율");
        dataSet.setColor(Color.BLUE);
        dataSet.setFillColor(Color.CYAN);
        dataSet.setDrawFilled(true);
        dataSet.setLineWidth(2f);

        RadarData data = new RadarData(dataSet);
        radarChart.setData(data);

        XAxis xAxis = radarChart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return TAGS[(int) value % TAGS.length];
            }
        });


        radarChart.getDescription().setEnabled(false);
        radarChart.invalidate();
    }

    private void startEditFlow(String date) {
        LocalDate localDate = LocalDate.parse(date, dateFormatter);

        // 루틴 목록 불러오기
        String url = "https://healthhelper.mycafe24.com/get_routines_test.php";
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        List<Routine> routines = new ArrayList<>();
                        org.json.JSONArray jsonArray = new org.json.JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            org.json.JSONObject obj = jsonArray.getJSONObject(i);
                            int id = obj.getInt("routine_id");
                            String name = obj.getString("name");
                            String description = obj.getString("description");
                            String tag = obj.getString("tag");
                            routines.add(new Routine(id, name, description, tag));
                        }

                        RoutineDialog dialog = new RoutineDialog();
                        dialog.setRoutineList(routines);

                        Bundle bundle = new Bundle();
                        bundle.putString("selectedDate", date);
                        dialog.setArguments(bundle);

                        dialog.setOnRoutineSelectedListener(new RoutineDialog.OnRoutineSelectedListener() {
                            @Override
                            public void onRoutineSelected(String selectedDate, int routineId) {
                                selectedRoutineId = routineId; // 루틴 ID 저장
                                showEditInputDialog(selectedDate);
                            }
                        });



                        dialog.show(getParentFragmentManager(), "RoutineDialog");

                    } catch (Exception e) {
                        Toast.makeText(getContext(), "루틴 파싱 오류", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "루틴 불러오기 실패", Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }
    private void showEditInputDialog(String date) {

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.inbody_input, null);

        EditText etWeight = dialogView.findViewById(R.id.et_weight);
        EditText etMuscle = dialogView.findViewById(R.id.et_muscle);
        EditText etFat = dialogView.findViewById(R.id.et_fat);
        EditText etScore = dialogView.findViewById(R.id.et_score);

        // 기존 인바디 데이터 불러오기
        String getUrl = "https://healthhelper.mycafe24.com/get_inbody.php?date=" + date;
        RequestQueue queue = Volley.newRequestQueue(requireContext());

        StringRequest getRequest = new StringRequest(Request.Method.GET, getUrl,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        etWeight.setText(obj.getString("weight"));
                        etMuscle.setText(obj.getString("muscle"));
                        etFat.setText(obj.getString("fat"));
                        etScore.setText(obj.getString("score"));
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "기존 인바디 불러오기 실패", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "서버 연결 실패", Toast.LENGTH_SHORT).show()
        );
        queue.add(getRequest);

        new AlertDialog.Builder(getContext())
                .setTitle(date + " 인바디 수정")
                .setView(dialogView)
                .setNegativeButton("취소", null)
                .setPositiveButton("저장", (dialog, which) -> {
                    String weight = etWeight.getText().toString().trim();
                    String muscle = etMuscle.getText().toString().trim();
                    String fat = etFat.getText().toString().trim();
                    String score = etScore.getText().toString().trim();

                    Log.d("DEBUG", "저장 시점 selectedRoutineId = " + selectedRoutineId);

                    // ✅ 저장 누른 시점에 삭제 후 insert 시작
                    String deleteInbodyUrl = "https://healthhelper.mycafe24.com/delete_inbody_by_date.php?date=" + date;
                    String deleteRoutineUrl = "https://healthhelper.mycafe24.com/delete_routine_by_date.php?date=" + date;

                    StringRequest deleteInbodyReq = new StringRequest(Request.Method.GET, deleteInbodyUrl,
                            delResp1 -> {
                                StringRequest deleteRoutineReq = new StringRequest(Request.Method.GET, deleteRoutineUrl,
                                        delResp2 -> {
                                            // 삭제 완료 후 → 인바디 insert
                                            String insertUrl = "https://healthhelper.mycafe24.com/insert_inbody.php";
                                            StringRequest insertRequest = new StringRequest(Request.Method.POST, insertUrl,
                                                    resp -> {
                                                        // 인바디 저장 완료 후 루틴도 저장
                                                        String insertRoutineUrl = "https://healthhelper.mycafe24.com/insert_routine_test.php";
                                                        StringRequest routineInsertRequest = new StringRequest(Request.Method.POST, insertRoutineUrl,
                                                                routineResp -> {
                                                                    Toast.makeText(getContext(), "수정 완료!", Toast.LENGTH_SHORT).show();
                                                                    LocalDate savedDate = LocalDate.parse(date, dateFormatter);
                                                                    recordedDates.add(savedDate);
                                                                    calendarView.notifyDateChanged(savedDate);
                                                                },
                                                                errRoutine -> Toast.makeText(getContext(), "루틴 저장 실패", Toast.LENGTH_SHORT).show()
                                                        ) {
                                                            @Override
                                                            protected Map<String, String> getParams() {
                                                                Map<String, String> params = new HashMap<>();
                                                                params.put("date", date);
                                                                params.put("routine_id", String.valueOf(selectedRoutineId));
                                                                return params;
                                                            }
                                                        };
                                                        queue.add(routineInsertRequest);
                                                    },
                                                    err -> Toast.makeText(getContext(), "인바디 저장 실패", Toast.LENGTH_SHORT).show()
                                            ) {
                                                @Override
                                                protected Map<String, String> getParams() {
                                                    Map<String, String> params = new HashMap<>();
                                                    params.put("date", date);
                                                    params.put("weight", weight);
                                                    params.put("muscle", muscle);
                                                    params.put("fat", fat);
                                                    params.put("score", score);
                                                    return params;
                                                }
                                            };
                                            queue.add(insertRequest);
                                        },
                                        err2 -> Toast.makeText(getContext(), "루틴 삭제 실패", Toast.LENGTH_SHORT).show()
                                );
                                queue.add(deleteRoutineReq);
                            },
                            err1 -> Toast.makeText(getContext(), "인바디 삭제 실패", Toast.LENGTH_SHORT).show()
                    );
                    queue.add(deleteInbodyReq);
                })
                .show();
    }

}
