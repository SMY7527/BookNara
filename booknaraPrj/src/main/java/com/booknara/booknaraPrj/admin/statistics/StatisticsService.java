package com.booknara.booknaraPrj.admin.statistics;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // 생성자 주입
public class StatisticsService {

    private final StatisticsRepository statisticsRepository;
    private final LendRepository lendRepository;

    /**
     * 실시간 통합 통계 데이터 조회
     */
    public Map<String, Object> getRealTimeUserStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 1. 연령대 통계 조회 및 DTO 변환
        List<UserAgeStatProjection> ageProjections = statisticsRepository.findAgeGroupStatistics();
        List<StatDto> ageStats = ageProjections.stream()
                .map(p -> new StatDto(p.getLabel(), p.getCount(), p.getPercentage()))
                .collect(Collectors.toList());

        // 2. 성별 통계 조회 및 DTO 변환
        List<UserAgeStatProjection> genderProjections = statisticsRepository.findGenderStatistics();
        List<StatDto> genderStats = genderProjections.stream()
                .map(p -> new StatDto(p.getLabel().equals("M") ? "남성" : "여성",
                        p.getCount(), p.getPercentage()))
                .collect(Collectors.toList());

        stats.put("ageStats", ageStats);
        stats.put("genderStats", genderStats);

        return stats;
    }

    public Map<String, Object> getGenderAgeStatistics() {
        Map<String, Object> result = new HashMap<>();

        // 1. 남성 연령대 통계 조회
        List<UserAgeStatProjection> maleProjections = statisticsRepository.findAgeGroupStatisticsByGender("M");
        List<StatDto> maleAgeStats = maleProjections.stream()
                .map(p -> new StatDto(p.getLabel(), p.getCount(), p.getPercentage()))
                .collect(Collectors.toList());

        // 2. 여성 연령대 통계 조회
        List<UserAgeStatProjection> femaleProjections = statisticsRepository.findAgeGroupStatisticsByGender("F");
        List<StatDto> femaleAgeStats = femaleProjections.stream()
                .map(p -> new StatDto(p.getLabel(), p.getCount(), p.getPercentage()))
                .collect(Collectors.toList());

        result.put("maleAgeStats", maleAgeStats);
        result.put("femaleAgeStats", femaleAgeStats);

        return result;
    }

    public Map<String, Object> getDashboardData() {
        Map<String, Object> data = new HashMap<>();

        // [중요] 분석 기간을 2026년까지 확장 (추가한 데이터가 보이도록)
        LocalDateTime start = LocalDateTime.of(2023, 1, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 12, 31, 23, 59, 59);

        // 1. 월별 통계
        List<Map<String, Object>> monthlyStats = lendRepository.findMonthlyLendStats(start, end);
        data.put("monthlyStats", monthlyStats);

        // 2. 차트 최댓값 계산
        int maxCount = monthlyStats.stream()
                .mapToInt(m -> {
                    Object val = m.getOrDefault("count", m.get("COUNT"));
                    return (val instanceof Number) ? ((Number) val).intValue() : 0;
                })
                .max()
                .orElse(100);
        data.put("maxCount", maxCount);

        // 3. 상단 카드 4종 지표 (getOverdueStatistics 로직 통합)
        LocalDateTime now = LocalDateTime.of(2026, 12, 31, 23, 0);
        long totalCount = lendRepository.count();

        // 연체율
        long overdueCountTotal = lendRepository.countAllOverdueItems(now);
        double overdueRate = (totalCount > 0) ? (overdueCountTotal * 100.0 / totalCount) : 0.0;
        data.put("overdueRate", String.format("%.1f", overdueRate));

        // 연장 비율
        long extendedCount = lendRepository.countByExtendCntGreaterThan(0);
        double extensionRate = (totalCount > 0) ? (extendedCount * 100.0 / totalCount) : 0.0;
        data.put("extensionRate", String.format("%.1f", extensionRate));

        // 평균 대여 일수
        Double avgLendDays = lendRepository.getAvgLendDays();
        data.put("avgLendDays", (avgLendDays != null) ? String.format("%.1f", avgLendDays) : "0.0");

        // 평균 연체 일수
        Double avgOverdueDays = lendRepository.getAvgOverdueDays();
        data.put("avgOverdueDays", (avgOverdueDays != null) ? String.format("%.1f", avgOverdueDays) : "0.0");

        // 기존의 단순 카운트 (필요시 유지)
        data.put("currentLendCount", lendRepository.countByReturnDoneAtIsNull());

        return data;
    }
}