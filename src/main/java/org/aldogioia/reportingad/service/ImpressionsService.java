package org.aldogioia.reportingad.service;

import org.aldogioia.reportingad.model.GlobalHandler;
import org.aldogioia.reportingad.model.data.TimeRange;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class ImpressionsService {

    private final Random random;

    public ImpressionsService() {
        random = new Random();
    }

    public long distributeImpressionsForScreen(int weekday, Long totalWeeklyImpressions) {
        // Pesi giornalieri: lunedì ha peso maggiore, domenica ha peso minore
        double[] weights = {0.22, 0.19, 0.17, 0.14, 0.11, 0.09, 0.08};

        double base = totalWeeklyImpressions * weights[weekday - 1];
        return Math.round(base);
    }

    public Map<String, Long> distributeImpressionsForCreative(Long dailyImpressions, List<String> creatives) {
        int n = creatives.size();
        double[] weights = new double[n];
        double totalWeight = 0;

        for (int i = 0; i < n; i++) {
            weights[i] = 0.9 + random.nextDouble() * 0.2;
            totalWeight += weights[i];
        }

        double[] quotas = new double[n];
        Long[] floorValues = new Long[n];
        long allocated = 0;

        for (int i = 0; i < n; i++) {
            quotas[i] = dailyImpressions * (weights[i] / totalWeight);
            floorValues[i] = (long) Math.floor(quotas[i]);
            allocated += floorValues[i];
        }

        long remaining = dailyImpressions - allocated;

        // Calcola i residui decimali
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < n; i++) indices.add(i);

        indices.sort(Comparator.comparingDouble(i -> -(quotas[i] - floorValues[i]))); // decrescente per priorità

        for (int i = 0; i < remaining; i++) {
            floorValues[indices.get(i)] += 1;
        }


        Map<String, Long> result = new HashMap<>();
        for (int i = 0; i < n; i++) {
            result.put(creatives.get(i), floorValues[i]);
        }

        return result;
    }

    public long filterImpressionsByTimeRange(
            long dailyImpressions,
            List<TimeRange> timeRanges
    ) {
        if (timeRanges.isEmpty()) {
            return dailyImpressions;
        }

        return timeRanges.stream()
                .mapToLong(timeRange -> {
                    long impressions = 0;
                    for (LocalTime time = timeRange.getStart(); !time.isAfter(timeRange.getEnd()); time = time.plusHours(1)) {
                        impressions += Math.round(getImpressionForHour(time, dailyImpressions));
                    }
                    return impressions;
                })
                .sum();
    }

    private double getImpressionForHour(LocalTime time, long dailyImpressions) {
        if (!time.isBefore(LocalTime.of(7, 0)) && time.isBefore(LocalTime.of(10, 0))) {
            return (dailyImpressions * 0.30) / 3;
        } else if (!time.isBefore(LocalTime.of(10, 0)) && time.isBefore(LocalTime.of(13, 0))) {
            return (dailyImpressions * 0.25) / 3;
        } else if (!time.isBefore(LocalTime.of(13, 0)) && time.isBefore(LocalTime.of(15, 0))) {
            return (dailyImpressions * 0.20) / 2;
        } else if (!time.isBefore(LocalTime.of(15, 0)) && time.isBefore(LocalTime.of(21, 0))) {
            return (dailyImpressions * 0.25) / 6;
        } else {
            return 0.0;
        }
    }

    public long calculateFilteredMaxImpressions(LocalDate start, LocalDate end, List<String> selectedScreens, Map<DayOfWeek, List<TimeRange>> timeMap) {
        long total = 0L;

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            DayOfWeek day = date.getDayOfWeek();
            if (!timeMap.containsKey(day)) continue;

            List<TimeRange> timeRanges = timeMap.get(day);

            for (String screen : selectedScreens) {
                Long weeklyImpressions = GlobalHandler.getInstance().getDataScreen().getOrDefault(screen, 0L);
                long dailyImpressions = distributeImpressionsForScreen(day.getValue(), weeklyImpressions);
                long filtered = filterImpressionsByTimeRange(dailyImpressions, timeRanges);

                total += filtered;
            }
        }
        return total;
    }

}

