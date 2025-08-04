package org.aldogioia.reportingad.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.util.Objects;

public class MainController {

    @FXML private StackPane contentPane;
    @FXML private Button generaReportButton;
    @FXML private Button analizzaReportButton;
    @FXML private Button settingsButton;

    public void initialize() {
        loadReportPage();
    }

    private void setActiveButton(Button active) {
        generaReportButton.getStyleClass().remove("selected");
        analizzaReportButton.getStyleClass().remove("selected");
        settingsButton.getStyleClass().remove("selected");
        active.getStyleClass().add("selected");
    }

    public void loadReportPage() {
        setActiveButton(generaReportButton);
        loadPage("/org/aldogioia/reportingad/view/report-page.fxml");
    }

    public void loadReportAnalysisPage() {
        setActiveButton(analizzaReportButton);
        loadPage("/org/aldogioia/reportingad/view/analysis-page.fxml");
    }

    public void loadSettingsPage() {
        setActiveButton(settingsButton);
        loadPage("/org/aldogioia/reportingad/view/settings-page.fxml");
    }

    private void loadPage(String fxmlPath) {
        try {
            Node node = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxmlPath)));
            contentPane.getChildren().setAll(node);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
