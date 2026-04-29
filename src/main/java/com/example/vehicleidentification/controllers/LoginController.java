package com.example.vehicleidentification.controllers;

import com.example.vehicleidentification.DataAccessObject.UserDAO;
import com.example.vehicleidentification.NavigationUtil;
import com.example.vehicleidentification.SessionManager;
import com.example.vehicleidentification.model.User;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField        userIdField;
    @FXML private PasswordField    passwordField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private Label            statusLabel;

    // fx:id wired from FXML for visual effects
    @FXML private Button           loginButton;
    @FXML private Button           clearButton;
    @FXML private AnchorPane       bgPane;

    private final UserDAO userDAO = new UserDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        roleCombo.setItems(FXCollections.observableArrayList(
                "ADMIN", "WORKSHOP", "CUSTOMER", "POLICE", "INSURANCE"
        ));

        // Alphanumeric only — no spaces or special characters
        userIdField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[a-zA-Z0-9]*")) {
                userIdField.setText(newVal.replaceAll("[^a-zA-Z0-9]", ""));
            }
        });

        // Apply all visual effects after FXML is fully loaded
        applyVisualEffects();
    }

    // ----------------------------------------------------------------
    // VISUAL EFFECTS
    // ----------------------------------------------------------------

    private void applyVisualEffects() {

        // ── 1. DropShadow on the background AnchorPane ───────────────
        // Gives the whole login scene a deep glow/shadow atmosphere
        DropShadow bgShadow = new DropShadow();
        bgShadow.setColor(Color.web("#0d1b2a"));
        bgShadow.setRadius(40);
        bgShadow.setSpread(0.2);
        bgShadow.setOffsetY(6);
        bgPane.setEffect(bgShadow);

        // ── 2. DropShadow on the Sign In button ──────────────────────
        // Blue glow matching the button colour
        DropShadow loginShadow = new DropShadow();
        loginShadow.setColor(Color.web("#1e3a8a"));
        loginShadow.setRadius(16);
        loginShadow.setSpread(0.4);
        loginShadow.setOffsetY(3);
        loginButton.setEffect(loginShadow);

        // ── 3. DropShadow on the Clear button ────────────────────────
        // Red glow matching the button colour
        DropShadow clearShadow = new DropShadow();
        clearShadow.setColor(Color.web("#ef4444"));
        clearShadow.setRadius(16);
        clearShadow.setSpread(0.4);
        clearShadow.setOffsetY(3);
        clearButton.setEffect(clearShadow);

        // ── 4. FadeTransition on the Sign In button ──────────────────
        // Continuously fades in and out to draw the user's attention
        FadeTransition fade = new FadeTransition(Duration.seconds(1.4), loginButton);
        fade.setFromValue(1.0);
        fade.setToValue(0.35);
        fade.setCycleCount(FadeTransition.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();
    }

    // ----------------------------------------------------------------
    // LOGIN
    // ----------------------------------------------------------------
    @FXML
    public void handleLogin() {
        String userId   = userIdField.getText().trim();
        String password = passwordField.getText().trim();
        String role     = roleCombo.getValue();

        if (userId.isEmpty()) {
            showError("⚠ Please enter your User ID.");
            return;
        }
        if (password.isEmpty()) {
            showError("⚠ Please enter your password.");
            return;
        }
        if (role == null) {
            showError("⚠ Please select your role.");
            return;
        }

        try {
            User user = userDAO.loginByCustomId(userId, password, role);

            if (user != null) {
                SessionManager.setLoggedInUser(user);
                statusLabel.setStyle("-fx-text-fill: #2e7d32;");
                statusLabel.setText("✅ Login successful. Redirecting...");
                NavigationUtil.navigateTo("dashboard.fxml", userIdField);
            } else {
                showError("Invalid User ID, password or role. Please try again.");
            }

        } catch (Exception e) {
            showError("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        statusLabel.setStyle("-fx-text-fill: #c62828;");
        statusLabel.setText(message);
    }

    @FXML
    public void handleClear() {
        userIdField.clear();
        passwordField.clear();
        roleCombo.setValue(null);
        statusLabel.setText("");
    }
}