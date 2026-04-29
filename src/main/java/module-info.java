module com.example.vehicleidentification {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires org.postgresql.jdbc;

    opens com.example.vehicleidentification to javafx.fxml;
    opens com.example.vehicleidentification.controllers to javafx.fxml;
    opens com.example.vehicleidentification.model to javafx.base;

    exports com.example.vehicleidentification;
}