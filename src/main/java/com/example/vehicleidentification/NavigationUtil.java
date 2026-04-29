package com.example.vehicleidentification;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;
import java.net.URL;

public class NavigationUtil {


    public static void navigateTo(String fxmlFile, Node sourceNode) {
        navigateTo(fxmlFile, (Stage) sourceNode.getScene().getWindow());
    }


    public static void navigateTo(String fxmlFile, Stage stage) {
        try {
            URL resource = NavigationUtil.class.getResource(
                    "/com/example/vehicleidentification/" + fxmlFile);

            if (resource == null) {
                System.err.println("FXML not found: " + fxmlFile);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Scene scene = new Scene(loader.load());

            boolean wasMaximized = stage.isMaximized();

            stage.setScene(scene);

            if (wasMaximized) {
                stage.setMaximized(false);
                stage.setMaximized(true);
            }

            stage.show();

        } catch (IOException e) {
            System.err.println("Navigation error loading " + fxmlFile
                    + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}