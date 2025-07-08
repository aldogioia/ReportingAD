package org.aldogioia.reportingad.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.aldogioia.reportingad.model.GlobalHandler;
import org.aldogioia.reportingad.model.data.TimeRange;
import org.aldogioia.reportingad.model.enumerator.MessageCode;
import org.aldogioia.reportingad.service.ExcelService;
import org.aldogioia.reportingad.service.ImpressionsService;
import org.aldogioia.reportingad.service.ProjectionService;
import org.aldogioia.reportingad.utils.InputValidator;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.aldogioia.reportingad.utils.AlertHandler.showAlert;

public class ReportController {

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField txtCpm, txtImpressionsGoal, txtCampaignName, txtCreative;
    @FXML private Label impressionsLabel;
    @FXML private TilePane screenPane;
    @FXML private ListView<String> creativeListView;
    @FXML private VBox daysContainer;

    private final ObservableList<String> creativeList = FXCollections.observableArrayList();
    private final Map<String, CheckBox> screenCheckBoxes = new HashMap<>();

    private final ImpressionsService impressionsService;
    private final ProjectionService projectionService;

    public ReportController() {
        this.impressionsService = new ImpressionsService();
        this.projectionService = new ProjectionService();
    }

    @FXML
    public void initialize() {
        loadScreens();
        startDatePicker.valueProperty().addListener((_, _, _) -> updateImpressions());
        endDatePicker.valueProperty().addListener((_, _, _) -> updateImpressions());
        setupWeekdayTimePickers();
        creativeListView.setItems(creativeList);
    }

    private void loadScreens() {
        screenPane.getChildren().clear();
        GlobalHandler.getInstance().getDataScreen().forEach((name, _) -> {
            CheckBox cb = new CheckBox(name);
            cb.setSelected(true);
            cb.setOnAction(_ -> updateImpressions());
            screenCheckBoxes.put(name, cb);
            screenPane.getChildren().add(cb);
        });
    }

    public void onAddCreative() {
        String name = txtCreative.getText().trim();
        if (!name.isEmpty() && !creativeListView.getItems().contains(name)) {
            creativeList.add(name);
            txtCreative.clear();
        }
        else  {
            showAlert(Alert.AlertType.INFORMATION, "Informazione", "Inserisci un nome valido per la creatività.\nAssicurati che non sia vuoto e che non sia già presente nella lista.");
        }
    }

    public void onRemoveCreative() {
        int selectedIdx = creativeListView.getSelectionModel().getSelectedIndex();
        if (selectedIdx >= 0) {
            creativeList.remove(selectedIdx);
        }
        else {
            showAlert(Alert.AlertType.WARNING, "Attenzione", "Seleziona una creatività da rimuovere.");
        }
    }

    private void updateImpressions() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        if (start == null || end == null) {
            impressionsLabel.setText("Impressions Goal (max: )");
            return;
        }

        List<String> selectedScreens = getSelectedScreens();
        Map<DayOfWeek, List<TimeRange>> timeMap = collectDayTimeSelections();

        if (selectedScreens.isEmpty() || timeMap.isEmpty()) {
            impressionsLabel.setText("Impressions Goal (max: )");
            return;
        }

        try {
            long filteredMaxImpressions = impressionsService.calculateFilteredMaxImpressions(start, end, selectedScreens, timeMap);
            System.out.println("Impressions: " + filteredMaxImpressions);
            impressionsLabel.setText("Impressions Goal (max: " + filteredMaxImpressions + ")");
        } catch (Exception ex) {
            impressionsLabel.setText("Impressions Goal (max: )");
        }
    }

    public void onGenerateReport() {
        checkInput();
    }

    private void setupWeekdayTimePickers() {
        for (DayOfWeek day : DayOfWeek.values()) {
            VBox dayBox = new VBox(8);
            CheckBox dayCheckBox = new CheckBox(day.name());
            dayCheckBox.setSelected(true);
            dayCheckBox.setOnAction(_ -> updateImpressions());

            VBox rangesBox = new VBox(8);
            rangesBox.setVisible(true);
            rangesBox.setManaged(true);

            // Range default 07:00–21:00
            HBox defaultRange = createTimeRangeRow(rangesBox, true);
            rangesBox.getChildren().add(defaultRange);

            // Pulsante per aggiungere range extra
            Button addRangeBtn = new Button("Aggiungi intervallo orario");
            addRangeBtn.getStyleClass().add("btn");
            addRangeBtn.setOnAction(_ -> rangesBox.getChildren().add(createTimeRangeRow(rangesBox, false)));

            VBox wrapper = new VBox(8, rangesBox, addRangeBtn);
            wrapper.setPadding(new Insets(0, 0, 8, 20));
            wrapper.setVisible(true);
            wrapper.setManaged(true);

            // Listener per nascondere/mostrare wrapper
            dayCheckBox.selectedProperty().addListener((_, _, newVal) -> {
                wrapper.setVisible(newVal);
                wrapper.setManaged(newVal);
            });

            dayBox.getChildren().addAll(dayCheckBox, wrapper);
            daysContainer.getChildren().add(dayBox);
        }
    }


    private List<String> getSelectedScreens() {
        return screenCheckBoxes.entrySet().stream()
                .filter(entry -> entry.getValue().isSelected())
                .map(Map.Entry::getKey)
                .toList();
    }

    private HBox createTimeRangeRow(VBox parent, boolean defaultRange) {
        List<LocalTime> availableTimes = GlobalHandler.getInstance().getTimeList();

        ComboBox<LocalTime> fromCombo = new ComboBox<>();
        ComboBox<LocalTime> toCombo = new ComboBox<>();

        if (defaultRange) {
            fromCombo.setValue(availableTimes.getFirst());
            toCombo.setValue(availableTimes.getLast());
        }

        fromCombo.getItems().addAll(availableTimes);
        fromCombo.setPromptText("Dalle");
        fromCombo.setPrefWidth(100);
        fromCombo.valueProperty().addListener((_, _, _) -> updateImpressions());

        toCombo.getItems().addAll(availableTimes);
        toCombo.setPromptText("Alle");
        toCombo.setPrefWidth(100);
        toCombo.valueProperty().addListener((_, _, _) -> updateImpressions());

        Button removeBtn = new Button("Rimuovi");
        removeBtn.getStyleClass().add("btn-delete");
        removeBtn.setOnAction(_ -> parent.getChildren().removeIf(node -> node == removeBtn.getParent()));

        return new HBox(10, fromCombo, toCombo, removeBtn);
    }

    private void checkInput() {
        if (!InputValidator.checkTextField(txtCpm, "CPM")) return;
        if (!InputValidator.checkTextField(txtImpressionsGoal, "Impressions Goal")) return;
        if (!InputValidator.checkTextField(txtCampaignName, "Campagna")) return;
        if (!InputValidator.checkDateRange(startDatePicker, endDatePicker)) return;
        if (!InputValidator.checkListNotEmpty(creativeList, "Creative")) return;
        List<String> selectedScreens = getSelectedScreens();
        if (!InputValidator.checkListNotEmpty(selectedScreens, "Schermi")) return;

        double cpm;
        try{
            cpm = Double.parseDouble(txtCpm.getText());
        }
        catch (NumberFormatException ex){
            showAlert(Alert.AlertType.ERROR, "Errore", "Devi inserire un valore numerico valido per il CPM.");
            return;
        }

        long impressionsGoal;
        try {
            impressionsGoal = Long.parseLong(txtImpressionsGoal.getText());
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Devi inserire un numero intero (senza virgole o punti) per l'impressions goal.");
            return;
        }

        long impressionsEstimated = Long.parseLong(impressionsLabel.getText().replaceAll("\\D", ""));

        if (!InputValidator.checkPositiveNumber(txtCpm.getText(), "CPM")) return;
        if (!InputValidator.checkPositiveNumber(txtImpressionsGoal.getText(), "Impressions Goal")) return;
        if (!InputValidator.checkImpressionGoal(impressionsGoal, impressionsEstimated)) return;

        Map<DayOfWeek, List<TimeRange>> dayMap = collectDayTimeSelections();
        if (dayMap.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Devi selezionare almeno un giorno con orari validi.");
            return;
        }

        generateReport(
                startDatePicker.getValue(),
                endDatePicker.getValue(),
                cpm,
                impressionsGoal,
                impressionsEstimated,
                txtCampaignName.getText().trim(),
                selectedScreens,
                dayMap
        );
    }

    private Map<DayOfWeek, List<TimeRange>> collectDayTimeSelections() {
        Map<DayOfWeek, List<TimeRange>> dayMap = new HashMap<>();

        for (Node node : daysContainer.getChildren()) {
            if (node instanceof VBox dayBox) {
                List<Node> children = dayBox.getChildren();

                if (children.isEmpty()) continue;

                // Prima riga: checkbox con giorno
                if (children.getFirst() instanceof CheckBox dayCheckBox && dayCheckBox.isSelected()) {
                    String dayName = dayCheckBox.getText().toUpperCase();
                    DayOfWeek day = DayOfWeek.valueOf(dayName);

                    List<TimeRange> ranges = new ArrayList<>();

                    // Tutto il resto sono coppie di orari
                    for (int i = 1; i < children.size(); i++) {
                        if (children.get(i) instanceof VBox wrapper) {
                            for (Node innerNode : wrapper.getChildren()) {
                                if (innerNode instanceof VBox rangesBox) {
                                    for (Node timeRowNode : rangesBox.getChildren()) {
                                        if (timeRowNode instanceof HBox timeRow) {
                                            Node startNode = timeRow.getChildren().get(0);
                                            Node endNode = timeRow.getChildren().get(1);

                                            if (startNode instanceof ComboBox<?> startCombo && endNode instanceof ComboBox<?> endCombo) {
                                                LocalTime start = (LocalTime) startCombo.getValue();
                                                LocalTime end = (LocalTime) endCombo.getValue();

                                                if (start == null || end == null || end.isBefore(start)) {
                                                    showAlert(Alert.AlertType.ERROR, "Errore", "Orari non validi per il giorno: " + dayName);
                                                    return Collections.emptyMap();
                                                }

                                                ranges.add(new TimeRange(start, end));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (!ranges.isEmpty()) {
                        dayMap.put(day, ranges);
                    }
                }
            }
        }
        return dayMap;
    }

    private void generateReport(
            LocalDate start,
            LocalDate end,
            Double cpm,
            Long impressionsGoal,
            Long impressionsEstimated,
            String campaignNameText,
            List<String> screens,
            Map<DayOfWeek, List<TimeRange>> timeRanges
    ) {
        List<String> dataLines = projectionService
                .generateData(
                        start,
                        end,
                        cpm,
                        impressionsEstimated,
                        impressionsGoal,
                        screens,
                        creativeList,
                        timeRanges
                );

        List<String> infoLines = List.of(
                "Report Generato il: ;" + LocalDateTime.now(),
                "Campagna: ;" + campaignNameText,
                "CPM: ;" + cpm,
                "Impressions Goal: ;" + impressionsGoal,
                "Creative: ;" + String.join(";", creativeList)
        );

        String headerLine = "Date;Screen;Creative;Impressions;Spend";

        MessageCode code = ExcelService.writeExcel(
                campaignNameText,
                infoLines,
                headerLine,
                dataLines
        );

        if (code != MessageCode.OK) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Errore durante la scrittura del file Excel");
        }
        else {
            showAlert(Alert.AlertType.INFORMATION, "Successo", "Report generato con successo! lo trovi nella cartella download.");
        }
    }
}
