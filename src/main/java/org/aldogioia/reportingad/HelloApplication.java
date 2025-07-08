package org.aldogioia.reportingad;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/aldogioia/reportingad/view/main-view.fxml"));
        BorderPane root = loader.load();
        Scene scene = new Scene(root, 1000, 700);
        stage.setScene(scene);
        stage.setTitle("Strumento di Reportistica");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}