package org.aldogioia.reportingad.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.LongStringConverter;
import org.aldogioia.reportingad.model.GlobalHandler;
import org.aldogioia.reportingad.utils.AlertHandler;

import java.io.IOException;
import java.util.Map;

public class SettingsController {

    @FXML private TableView<Map.Entry<String, Long>> tableView;
    @FXML private TableColumn<Map.Entry<String, Long>, String> nameCol;
    @FXML private TableColumn<Map.Entry<String, Long>, Long> impressionsCol;

    @FXML private TextField nameField;
    @FXML private TextField numberField;

    private final ObservableList<Map.Entry<String, Long>> data = FXCollections.observableArrayList();

    @FXML public void initialize() {
        data.setAll(GlobalHandler.getInstance().getDataScreen().entrySet());

        nameCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getKey()));
        impressionsCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleLongProperty(cellData.getValue().getValue()).asObject());

        impressionsCol.setCellFactory(TextFieldTableCell.forTableColumn(new LongStringConverter()));
        impressionsCol.setOnEditCommit(event -> {
            String name = event.getRowValue().getKey();
            Long newVal = event.getNewValue();
            GlobalHandler.getInstance().getDataScreen().put(name, newVal);
            refreshTable();
        });

        tableView.setItems(data);
        tableView.setEditable(true);
    }

    @FXML private void onAddRow() {
        String name = nameField.getText().trim();
        String numStr = numberField.getText().trim();

        if (name.isEmpty() || numStr.isEmpty()) {
            AlertHandler.showAlert(Alert.AlertType.WARNING,"Attenzione", "Nome e impressions dello schermo sono obbligatori");
            return;
        }

        try {
            long num = Long.parseLong(numStr);
            GlobalHandler.getInstance().getDataScreen().put(name, num);
            refreshTable();
            nameField.clear();
            numberField.clear();
            AlertHandler.showAlert(Alert.AlertType.CONFIRMATION,"Successo", "Dati dello schermo aggiunti con successo.");
        } catch (NumberFormatException e) {
            AlertHandler.showAlert(Alert.AlertType.WARNING,"Attenzione", "Numero impressions non valido (non deve contenere punti o virgole).");
        }
    }

    @FXML private void onDeleteRow() {
        Map.Entry<String, Long> selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            GlobalHandler.getInstance().getDataScreen().remove(selected.getKey());
            refreshTable();
            AlertHandler.showAlert(Alert.AlertType.CONFIRMATION,"Successo", "Dati dello schermo rimossi con successo.");
        }
    }

    @FXML private void onSave() {
        try {
            GlobalHandler.getInstance().saveData();
            AlertHandler.showAlert(Alert.AlertType.CONFIRMATION,"Successo", "Dati dello schermo salvati con successo.");
        } catch (IOException e) {
            AlertHandler.showAlert(Alert.AlertType.ERROR,"Errore", "Errore durante il salvataggio dei dati.");
        }
    }

    private void refreshTable() {
        data.setAll(GlobalHandler.getInstance().getDataScreen().entrySet());
    }

}
