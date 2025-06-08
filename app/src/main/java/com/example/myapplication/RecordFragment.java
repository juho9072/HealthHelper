package com.example.myapplication;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.kizitonwose.calendarview.CalendarView;
import com.kizitonwose.calendarview.model.CalendarDay;
import com.kizitonwose.calendarview.model.DayOwner;
import com.kizitonwose.calendarview.ui.DayBinder;
import com.kizitonwose.calendarview.ui.ViewContainer;


import org.json.JSONArray;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kotlin.Unit;

public class RecordFragment extends Fragment {

    private CalendarView calendarView;
    private TextView monthText;
    private final Set<LocalDate> dataDates = new HashSet<>();
    private ActivityResultLauncher<Intent> routineInputLauncher;
    private Button btn1Week, btn1Month, btn1Year, btnAll;
    private RadarChart radarChart;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_record, container, false);

        // 레이더 차트 기간 버튼 그룹
        RadioGroup periodButtonGroup = view.findViewById(R.id.periodButtonGroup);

        periodButtonGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String selectedPeriod = "";
            if (checkedId == R.id.btnPeriod1Week) {
                selectedPeriod = "1주";
            } else if (checkedId == R.id.btnPeriod1Month) {
                selectedPeriod = "1달";
            } else if (checkedId == R.id.btnPeriod1Year) {
                selectedPeriod = "1년";
            } else if (checkedId == R.id.btnPeriodAll) {
                selectedPeriod = "전체";
            }

            fetchDataForPeriod(selectedPeriod);
        });

        return view;
    }

    // 버튼 클릭 시 호출되는 메소드
    public void onPeriodButtonClick(View view) {
        // 버튼들을 초기화
        btn1Week.setSelected(false);
        btn1Month.setSelected(false);
        btn1Year.setSelected(false);
        btnAll.setSelected(false);

        // 클릭된 버튼을 선택 상태로 설정
        view.setSelected(true);

        String selectedPeriod = "";

        // 버튼에 따라 period 값 설정
        if (view.getId() == R.id.btnPeriod1Week) {
            selectedPeriod = "1주";
        } else if (view.getId() == R.id.btnPeriod1Month) {
            selectedPeriod = "1달";
        } else if (view.getId() == R.id.btnPeriod1Year) {
            selectedPeriod = "1년";
        } else if (view.getId() == R.id.btnPeriodAll) {
            selectedPeriod = "전체";
        }

        // 선택된 기간에 맞는 데이터를 가져오는 함수 호출
        fetchDataForPeriod(selectedPeriod);
    }

    private void fetchDataForPeriod(String period) {
        // 서버에서 데이터를 가져오는 예시
        String url = "https://healthhelper.mycafe24.com/get_data_for_period.php?period=" + period;

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        // 유니코드를 제대로 처리하기 위해 String을 실제 문자로 변환
                        String decodedResponse = decodeUnicode(response);  // 유니코드 디코딩 적용

                        // 파싱된 데이터를 처리
                        JSONArray jsonArray = new JSONArray(decodedResponse);
                        List<String> tags = new ArrayList<>();

                        // 여러 루틴에 대한 태그 처리
                        for (int i = 0; i < jsonArray.length(); i++) {
                            String routineTag = jsonArray.getString(i);  // 문자열 직접 가져오기
                            tags.add(routineTag);
                        }

                        // 데이터 가공 후 레이더 차트에 반영
                        updateRadarChart(tags);

                    } catch (Exception e) {
                        Log.d("API Response", response);
                        Toast.makeText(getContext(), "데이터 파싱 실패", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "서버 통신 실패", Toast.LENGTH_SHORT).show()
        );
        queue.add(request);
    }

    // 유니코드 문자열을 실제 문자로 변환하는 메소드
    private String decodeUnicode(String input) {
        StringBuilder output = new StringBuilder();
        int i = 0;
        while (i < input.length()) {
            if (input.charAt(i) == '\\' && i + 1 < input.length() && input.charAt(i + 1) == 'u') {
                String unicode = input.substring(i + 2, i + 6);  // 유니코드 부분 추출
                output.append((char) Integer.parseInt(unicode, 16)); // 유니코드 변환
                i += 6; // 유니코드 부분을 건너뛰고 계속 진행
            } else {
                output.append(input.charAt(i));  // 그냥 문자일 경우 그대로 추가
                i++;
            }
        }
        return output.toString();  // 변환된 문자열 반환
    }
    private void updateRadarChart(List<String> tags) {
        // 태그 카운트 초기화
        int shoulderCount = 0;
        int chestCount = 0;
        int backCount = 0;
        int bicepsCount = 0;
        int tricepsCount = 0;
        int lowerBodyCount = 0;
        int coreCount = 0;
        int cardioCount = 0;

        // 태그 목록을 순회하며 카운트
        for (String tag : tags) {
            // 콤마로 구분된 태그를 분리
            String[] tagArray = tag.split(","); // 콤마로 태그를 분리
            for (String individualTag : tagArray) {
                individualTag = individualTag.trim(); // 공백 제거

                // 태그에 해당하는 항목 카운트
                if (individualTag.contains("어깨")) shoulderCount++;
                if (individualTag.contains("가슴")) chestCount++;
                if (individualTag.contains("등")) backCount++;
                if (individualTag.contains("이두")) bicepsCount++;
                if (individualTag.contains("삼두")) tricepsCount++;
                if (individualTag.contains("하체")) lowerBodyCount++;
                if (individualTag.contains("코어")) coreCount++;
                if (individualTag.contains("유산소")) cardioCount++;
            }
        }

        // 레이더 차트에 데이터를 업데이트
        updateRadarChartData(shoulderCount, chestCount, backCount, bicepsCount, tricepsCount, lowerBodyCount, coreCount, cardioCount);
    }

    private void updateRadarChartData(int shoulder, int chest, int back, int biceps, int triceps, int lowerBody, int core, int cardio) {

        Log.d("ChartData", "어깨: " + shoulder + ", 가슴: " + chest + ", 등: " + back + ", 이두: " + biceps + ", 삼두: " + triceps + ", 하체: " + lowerBody + ", 코어: " + core + ", 유산소: " + cardio);

        // 레이더 차트 데이터 객체 생성
        List<RadarEntry> entries = new ArrayList<>();
        entries.add(new RadarEntry(shoulder));
        entries.add(new RadarEntry(chest));
        entries.add(new RadarEntry(back));
        entries.add(new RadarEntry(biceps));
        entries.add(new RadarEntry(triceps));
        entries.add(new RadarEntry(lowerBody));
        entries.add(new RadarEntry(core));
        entries.add(new RadarEntry(cardio));

        // 레이더 차트 데이터 셋 생성
        RadarDataSet dataSet = new RadarDataSet(entries, "운동 집중도");
        radarChart.getLegend().setTextColor(Color.WHITE);
        dataSet.setColor(Color.parseColor("#FF6F61"));
        dataSet.setDrawFilled(true);
        dataSet.setDrawValues(false);
        dataSet.setFillColor(Color.parseColor("#FF6F61"));
        dataSet.setDrawHighlightCircleEnabled(true);

        // 레이더 차트에 데이터 세팅
        RadarData data = new RadarData(dataSet);
        radarChart.setData(data);
        radarChart.invalidate();  // 차트 갱신
    }

    private void setupRadarChart() {
        radarChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(new String[]{
                "어깨", "가슴", "등", "이두", "삼두", "하체", "코어", "유산소"
        }));
        radarChart.setWebLineWidth(1f);
        radarChart.setWebColor(Color.parseColor("#FFFFFF"));
        radarChart.setWebColorInner(Color.parseColor("#FFFFFF"));
        radarChart.setWebAlpha(100);
        radarChart.getDescription().setEnabled(false);  // 설명 텍스트 비활성화
        radarChart.setTouchEnabled(false);  // 터치 이벤트 비활성화
        // ✅ 꼭짓점 텍스트 크기 키우기 + 색상 하얀색
        radarChart.getXAxis().setTextSize(14f);
        radarChart.getXAxis().setTextColor(Color.WHITE);

        // ✅ 중앙 숫자값(방사형 눈금값) 제거
        radarChart.getYAxis().setDrawLabels(false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btn1Week = view.findViewById(R.id.btnPeriod1Week);
        btn1Month = view.findViewById(R.id.btnPeriod1Month);
        btn1Year = view.findViewById(R.id.btnPeriod1Year);
        btnAll = view.findViewById(R.id.btnPeriodAll);

        // 레이더 차트 초기화
        radarChart = view.findViewById(R.id.radarChart);  // 레이더 차트 위젯을 연결

        // 레이더 차트 설정
        setupRadarChart();

        calendarView = view.findViewById(R.id.calendarView);
        monthText = view.findViewById(R.id.monthText);

        YearMonth currentMonth = YearMonth.now();
        calendarView.setup(currentMonth.minusMonths(12), currentMonth.plusMonths(12), java.time.DayOfWeek.SUNDAY);
        calendarView.scrollToMonth(currentMonth);

        routineInputLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadMarkedDates();  // 저장 후 달력 업데이트
                    }
                }
        );


        calendarView.setDayBinder(new DayBinder<DayViewContainer>() {
            @NonNull
            @Override
            public DayViewContainer create(@NonNull View view) {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(@NonNull DayViewContainer container, @NonNull CalendarDay day) {
                container.day = day;

                TextView textView = container.textView;
                textView.setText(String.valueOf(day.getDate().getDayOfMonth()));

                textView.setTextColor(Color.BLACK);

                if (day.getOwner() == DayOwner.THIS_MONTH) {
                    textView.setVisibility(View.VISIBLE);
                    if (dataDates.contains(day.getDate())) {
                        textView.setBackgroundColor(Color.parseColor("#EEEEEE"));
                    } else {
                        textView.setBackgroundColor(Color.TRANSPARENT);
                    }
                } else {
                    textView.setVisibility(View.INVISIBLE);
                }

                view.setOnClickListener(v -> {
                    if (day != null && day.getOwner() == DayOwner.THIS_MONTH) {
                        LocalDate clickedDate = day.getDate();
                        checkDataAndNavigate(clickedDate);
                    }
                });


            }
        });

        calendarView.setMonthScrollListener(month -> {
            String title = month.getYearMonth().getYear() + "년 " + month.getYearMonth().getMonthValue() + "월";
            monthText.setText(title);
            return Unit.INSTANCE;
        });

        loadMarkedDates();
        btn1Week.setSelected(true);
        fetchDataForPeriod("1주");
    }

    private void checkDataAndNavigate(LocalDate date) {
        String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String url = "https://healthhelper.mycafe24.com/check_data.php?date=" + dateStr;

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    Log.d("DataCheck", "Response: " + response);
                    if (response.trim().equals("true")) {
                        // 데이터 있음 → 상세 보기로 이동
                        Intent intent = new Intent(getContext(), DetailViewActivity.class);
                        intent.putExtra("selectedDate", dateStr);
                        routineInputLauncher.launch(intent);
                    } else {
                        // 데이터 없음 → 입력 페이지로 이동
                        Intent intent = new Intent(getContext(), RoutineInputActivity.class);
                        intent.putExtra("selectedDate", dateStr);
                        routineInputLauncher.launch(intent);
                    }
                },
                error -> Toast.makeText(getContext(), "데이터 확인 실패", Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }

    // ✅ 날짜 셀 컨테이너 정의
    private class DayViewContainer extends ViewContainer {
        final TextView textView;
        CalendarDay day;

        DayViewContainer(View view) {
            super(view);
            textView = view.findViewById(R.id.calendarDayText);

            view.setOnClickListener(v -> {
                if (day != null && day.getOwner() == DayOwner.THIS_MONTH) {
                    LocalDate clickedDate = day.getDate();
                    checkDataAndNavigate(clickedDate); // ✅ 여기서 조건 분기
                }
            });

        }
    }

    private void loadMarkedDates() {
        String url = "https://healthhelper.mycafe24.com/get_recorded_dates.php";  // 날짜 리스트 API 주소

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        // 예: ["2025-06-01", "2025-06-02", ...] 형태로 날짜 리스트가 옴
                        JSONArray jsonArray = new JSONArray(response);
                        dataDates.clear();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            String dateStr = jsonArray.getString(i);
                            LocalDate date = LocalDate.parse(dateStr);
                            dataDates.add(date);
                        }

                        calendarView.notifyCalendarChanged();  // 달력 다시 그리기
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "날짜 데이터 파싱 실패", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "날짜 데이터 불러오기 실패", Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }

}
