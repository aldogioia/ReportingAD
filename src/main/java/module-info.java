module org.aldogioia.reportingad {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;


    opens org.aldogioia.reportingad to javafx.fxml;
    exports org.aldogioia.reportingad;
    exports org.aldogioia.reportingad.controller;
    exports org.aldogioia.reportingad.service;
    exports org.aldogioia.reportingad.model.data;
    exports org.aldogioia.reportingad.model.enumerator;
    opens org.aldogioia.reportingad.controller to javafx.fxml;
}