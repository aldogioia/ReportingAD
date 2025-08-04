package org.aldogioia.reportingad.service;

import org.aldogioia.reportingad.utils.GlobalHandler;
import org.aldogioia.reportingad.model.data.TimeRange;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProjectionService {
    private final ImpressionsService impressionsService;

    public ProjectionService() {
         impressionsService = new ImpressionsService();
    }

    public List<String> generateData(
            LocalDate startDate,
            LocalDate endDate,
            Double cpm,
            Long impressionsEstimated,
            Long impressionsGoal,
            List<String> screens,
            List<String> creatives,
            Map<DayOfWeek, List<TimeRange>> timeRangesPerDay
    ) {
        List<String> projectionData = new ArrayList<>();

        for (LocalDate date = startDate; date.isEqual(endDate) || date.isBefore(endDate); date = date.plusDays(1)) {
            if (timeRangesPerDay.containsKey(date.getDayOfWeek())) {
                for (String screen : screens.stream().sorted().toList()) {
                    //ottengo il numero di impressioni settimanali per lo schermo
                    Long weeklyImpressionsForScreen = GlobalHandler.getInstance().getDataScreen().get(screen);

                    //distribuisco le impressioni settimanali per il giorno della settimana
                    long dailyImpressions = impressionsService.distributeImpressionsForScreen(
                            date.getDayOfWeek().getValue(),
                            weeklyImpressionsForScreen
                    );

                    //scalo le impressions giornaliere in base alle impressions goal
                    dailyImpressions = (dailyImpressions * impressionsGoal) / impressionsEstimated;

                    //filtro le impressions giornaliere in base agli orari di visualizzazione
                    dailyImpressions = impressionsService.filterImpressionsByTimeRange(
                            dailyImpressions,
                            timeRangesPerDay.get(date.getDayOfWeek())
                    );

                    //distribuisco le impressioni giornaliere per le varie creatività
                    Map<String, Long> creativeImpressions = impressionsService
                            .distributeImpressionsForCreative(dailyImpressions, creatives);

                    for (Map.Entry<String, Long> creativeImpression : creativeImpressions.entrySet()) {
                        String creative = creativeImpression.getKey();
                        Long impressions = creativeImpression.getValue();
                        double spend = (cpm * impressions) / 1000;
                        String projectionRow = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ";" + screen + ";" + creative + ";" + impressions + ";" + spend;
                        projectionData.add(projectionRow);
                    }
                }
            }
        }
        return projectionData;
    }

}
