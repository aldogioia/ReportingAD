package org.aldogioia.reportingad.model;

import javafx.scene.control.Alert;
import lombok.Getter;
import lombok.Setter;
import org.aldogioia.reportingad.utils.AlertHandler;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalTime;
import java.util.*;

@Getter
@Setter
public class GlobalHandler {
    private static GlobalHandler instance;

    private final Map<String, Long> dataScreen;  // nome → impression
    private LocalTime startTime;
    private LocalTime endTime;

    private final Path screenPath = Paths.get("files", "data_screen.csv");

    private GlobalHandler() {
        this.dataScreen = new LinkedHashMap<>();
        this.startTime = LocalTime.of(7, 0);
        this.endTime = LocalTime.of(21, 0);
        try {
            loadData();
        } catch (IOException e) {
            AlertHandler.showAlert(Alert.AlertType.ERROR,"Errore", "Errore durante il caricamento dei dati.");
        }
    }

    public static GlobalHandler getInstance() {
        if (instance == null) {
            instance = new GlobalHandler();
        }
        return instance;
    }

    public List<LocalTime> getTimeList() {
        List<LocalTime> timeList = new ArrayList<>();
        LocalTime current = startTime;
        while (!current.isAfter(endTime)) {
            timeList.add(current);
            current = current.plusHours(1);
        }
        return timeList;
    }

    public void loadData() throws IOException {
        dataScreen.clear();

        if (Files.exists(screenPath)) {
            List<String> lines = Files.readAllLines(screenPath);
            for (String line : lines) {
                String[] parts = line.split(";");
                if (parts.length == 2) {
                    String name = parts[0].trim();
                    long impressions = Long.parseLong(parts[1].trim());
                    dataScreen.put(name, impressions);
                }
            }
        }
    }

    public void saveData() throws IOException {
        List<String> screenLines = dataScreen.entrySet().stream()
                .map(e -> e.getKey() + ";" + e.getValue())
                .toList();
        Files.write(screenPath, screenLines);
    }
}
