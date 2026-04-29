package com.example.vehicleidentification.controllers;

import com.example.vehicleidentification.DataAccessObject.*;
import com.example.vehicleidentification.NavigationUtil;
import com.example.vehicleidentification.SessionManager;
import com.example.vehicleidentification.model.User;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML private Label totalVehiclesLabel;
    @FXML private Label totalCustomersLabel;
    @FXML private Label unpaidFinesLabel;
    @FXML private Label totalReportsLabel;

    @FXML private Arc vehiclesArc;
    @FXML private Arc customersArc;
    @FXML private Arc unpaidArc;
    @FXML private Arc reportsArc;

    @FXML private ProgressBar       progressBar;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label             progressLabel;
    @FXML private Label             syncLabel;

    @FXML private Label  welcomeLabel;
    @FXML private Label  roleLabel;
    @FXML private Button logoutButton;

    @FXML private HBox adminNavBox;
    @FXML private HBox workshopNavBox;
    @FXML private HBox policeNavBox;
    @FXML private HBox insuranceNavBox;
    @FXML private HBox customerNavBox;

    private final VehicleDAO      vehicleDAO   = new VehicleDAO();
    private final CustomerDAO     customerDAO  = new CustomerDAO();
    private final ViolationDAO    violationDAO = new ViolationDAO();
    private final PoliceReportDAO policeDAO    = new PoliceReportDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Platform.runLater(() -> {
            try {
                Stage stage = (Stage) welcomeLabel.getScene().getWindow();
                if (!stage.isMaximized()) stage.setMaximized(true);
            } catch (Exception ignored) {}
        });

        User u = SessionManager.getLoggedInUser();
        if (u != null) {
            welcomeLabel.setText("Welcome, " + u.getUsername());
            roleLabel.setText("Role: " + u.getRole()
                    + "  |  ID: " + u.getUserId());
        }

        // All users see all navigation now — no role-based hiding
        applyVisualEffects();
        loadStats();
    }

    private void applyVisualEffects() {
        if (logoutButton == null) return;

        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.web("#c62828"));
        dropShadow.setRadius(18);
        dropShadow.setSpread(0.45);
        dropShadow.setOffsetY(3);
        logoutButton.setEffect(dropShadow);

        FadeTransition fade = new FadeTransition(
                Duration.seconds(1.4), logoutButton);
        fade.setFromValue(1.0);
        fade.setToValue(0.3);
        fade.setCycleCount(FadeTransition.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();
    }

    private void loadStats() {
        try {
            int vehicles  = vehicleDAO.getTotalVehicles();
            int customers = customerDAO.getAllCustomers().size();
            int unpaid    = violationDAO.getUnpaidCount();
            int reports   = policeDAO.getAllReports().size();

            totalVehiclesLabel .setText(String.valueOf(vehicles));
            totalCustomersLabel.setText(String.valueOf(customers));
            unpaidFinesLabel   .setText(String.valueOf(unpaid));
            totalReportsLabel  .setText(String.valueOf(reports));

            int max = Math.max(1,
                    Math.max(vehicles,
                            Math.max(customers,
                                    Math.max(unpaid, reports))));

            animateArc(vehiclesArc,  vehicles,  max);
            animateArc(customersArc, customers, max);
            animateArc(unpaidArc,    unpaid,    max);
            animateArc(reportsArc,   reports,   max);

            int    total     = vehicles + customers + unpaid + reports;
            double barTarget = Math.min(1.0, total / 200.0);
            if (barTarget <= 0) barTarget = 0.02;

            animateProgressBar(barTarget, total);
            animateProgressIndicator(barTarget);

        } catch (Exception e) {
            System.out.println("Dashboard stats error: " + e.getMessage());
        }
    }

    private void animateArc(Arc arc, int value, int max) {
        if (arc == null) return;

        double targetDegrees;
        if (value <= 0) {
            targetDegrees = 0;
        } else {
            double proportion = Math.min(1.0, value / (double) max);
            targetDegrees = proportion * 355.0;
            if (targetDegrees < 45) targetDegrees = 45;
        }

        double target = -targetDegrees;

        Timeline tl = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(arc.lengthProperty(), 0.0)),
                new KeyFrame(Duration.millis(1000),
                        new KeyValue(arc.lengthProperty(), target))
        );
        tl.play();
    }

    private void animateProgressBar(double target, int totalItems) {
        if (progressBar == null) return;
        Timeline tl = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(progressBar.progressProperty(), 0.0)),
                new KeyFrame(Duration.millis(1000),
                        new KeyValue(progressBar.progressProperty(), target))
        );
        tl.play();
        if (progressLabel != null)
            progressLabel.setText(totalItems + " total records loaded");
    }

    private void animateProgressIndicator(double target) {
        if (progressIndicator == null) return;

        progressIndicator.setProgress(-1);

        Timeline delay = new Timeline(
                new KeyFrame(Duration.millis(800), e -> {
                    Timeline tl = new Timeline(
                            new KeyFrame(Duration.ZERO,
                                    new KeyValue(
                                            progressIndicator.progressProperty(),
                                            0.0)),
                            new KeyFrame(Duration.millis(1000),
                                    new KeyValue(
                                            progressIndicator.progressProperty(),
                                            target))
                    );
                    tl.setOnFinished(done -> {
                        if (syncLabel != null)
                            syncLabel.setText("✔ System data loaded successfully.");
                    });
                    tl.play();
                })
        );
        delay.play();
    }

    private void navigate(String fxml) {
        NavigationUtil.navigateTo(fxml, welcomeLabel);
    }

    // ----------------------------------------------------------------
    // ACTION HANDLERS — all users can access all modules
    // ----------------------------------------------------------------
    @FXML public void handleExit()                 { System.exit(0); }
    @FXML public void goToDashboard(ActionEvent e) { navigate("dashboard.fxml"); }
    @FXML public void goToVehicle(ActionEvent e)   { navigate("vehicle.fxml"); }
    @FXML public void goToCustomer(ActionEvent e)  { navigate("customer.fxml"); }
    @FXML public void goToWorkshop(ActionEvent e)  { navigate("workshop.fxml"); }
    @FXML public void goToPolice(ActionEvent e)    { navigate("police.fxml"); }
    @FXML public void goToViolation(ActionEvent e) { navigate("Violation.fxml"); }
    @FXML public void goToInsurance(ActionEvent e) { navigate("InsuranceView.fxml"); }
    @FXML public void goToAdmin(ActionEvent e)     { navigate("AdminView.fxml"); }

    @FXML public void handleLogout(ActionEvent e) {
        SessionManager.clearSession();
        navigate("login.fxml");
    }
}