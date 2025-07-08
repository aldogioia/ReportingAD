package org.aldogioia.reportingad.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

import java.util.List;

import static org.aldogioia.reportingad.utils.AlertHandler.showAlert;

public class InputValidator {
    public static boolean checkTextField(TextField field, String fieldName) {
        if (field.getText() == null || field.getText().isBlank()) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Il campo \"" + fieldName + "\" è obbligatorio.");
            return false;
        }
        return true;
    }

    public static boolean checkDateRange(DatePicker start, DatePicker end) {
        if (start.getValue() == null || end.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Le date di inizio e fine sono obbligatorie.");
            return false;
        }
        if (start.getValue().isAfter(end.getValue())) {
            showAlert(Alert.AlertType.ERROR, "Errore", "La data di inizio non può essere dopo la data di fine.");
            return false;
        }
        return true;
    }

    public static boolean checkListNotEmpty(List<?> list, String listName) {
        if (list == null || list.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Errore", "La lista \"" + listName + "\" non può essere vuota.");
            return false;
        }
        return true;
    }

    public static boolean checkPositiveNumber(String value, String fieldName) {
        try {
            double parsed = Double.parseDouble(value);
            if (parsed <= 0) {
                showAlert(Alert.AlertType.ERROR, "Errore", fieldName + " deve essere maggiore di zero.");
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Errore", "Il campo \"" + fieldName + "\" deve contenere un numero valido.");
            return false;
        }
    }

    public static boolean checkImpressionGoal(Long goal, Long estimated) {
        if (goal > estimated) {
            showAlert(Alert.AlertType.ERROR, "Errore", "L'impressions goal non può superare la capacità massima stimata.");
            return false;
        }
        return true;
    }

}
