package com.example.vehicleidentification.controllers;

import com.example.vehicleidentification.DataAccessObject.UserDAO;
import com.example.vehicleidentification.NavigationUtil;
import com.example.vehicleidentification.SessionManager;
import com.example.vehicleidentification.model.User;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class AdminController implements Initializable {

    @FXML private VBox accessDeniedPane;
    @FXML private VBox mainContentPane;

    @FXML private Button       logoutButton;

    @FXML private TextField        userIdField;
    @FXML private TextField        usernameField;
    @FXML private TextField        passwordField;
    @FXML private TextField        emailField;
    @FXML private TextField        phoneField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private Label            messageLabel;

    @FXML private TableView<User>           userTable;
    @FXML private TableColumn<User, String> colUserId, colUsername, colRole, colEmail;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");

    private final UserDAO userDAO = new UserDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Platform.runLater(() -> {
            try {
                Stage stage = (Stage) messageLabel.getScene().getWindow();
                if (!stage.isMaximized()) stage.setMaximized(true);
            } catch (Exception ignored) {}
        });

        if (!isAdmin()) { showAccessDenied(); return; }

        showMainContent();
        setupTable();
        loadTable();
        applyVisualEffects();

        userIdField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[a-zA-Z0-9]*"))
                userIdField.setText(newVal.replaceAll("[^a-zA-Z0-9]", ""));
        });

        usernameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[a-zA-Z ]*"))
                usernameField.setText(newVal.replaceAll("[^a-zA-Z ]", ""));
        });

        userTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, sel) -> { if (sel != null) populateForm(sel); });
    }

    private void applyVisualEffects() {
        if (logoutButton == null) return;

        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.web("#c62828"));
        dropShadow.setRadius(18);
        dropShadow.setSpread(0.45);
        dropShadow.setOffsetY(3);
        logoutButton.setEffect(dropShadow);

        FadeTransition fade = new FadeTransition(Duration.seconds(1.4), logoutButton);
        fade.setFromValue(1.0);
        fade.setToValue(0.3);
        fade.setCycleCount(FadeTransition.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();
    }

    private void showAccessDenied() {
        accessDeniedPane.setVisible(true);  accessDeniedPane.setManaged(true);
        mainContentPane.setVisible(false);  mainContentPane.setManaged(false);
    }

    private void showMainContent() {
        accessDeniedPane.setVisible(false); accessDeniedPane.setManaged(false);
        mainContentPane.setVisible(true);   mainContentPane.setManaged(true);
    }

    private void setupTable() {
        roleCombo.setItems(FXCollections.observableArrayList(
                "ADMIN", "WORKSHOP", "CUSTOMER", "POLICE", "INSURANCE"));
        colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
    }

    private void loadTable() {
        userTable.setItems(FXCollections.observableArrayList(userDAO.getAllUsers()));
    }

    private void populateForm(User u) {
        userIdField.setText(u.getUserId()     != null ? u.getUserId()   : "");
        usernameField.setText(u.getUsername() != null ? u.getUsername() : "");
        passwordField.setText(u.getPassword() != null ? u.getPassword() : "");
        emailField.setText(u.getEmail()       != null ? u.getEmail()    : "");
        phoneField.setText(u.getPhone()       != null ? u.getPhone()    : "");
        roleCombo.setValue(u.getRole());
    }

    private void clearForm() {
        userIdField.clear();
        usernameField.clear();
        passwordField.clear();
        emailField.clear();
        phoneField.clear();
        roleCombo.setValue(null);
        messageLabel.setText("");
        userTable.getSelectionModel().clearSelection();
    }

    private boolean validateUserId(String userId) {
        if (userId.isEmpty()) { warn("User ID is required."); return false; }
        if (!userId.matches("[a-zA-Z0-9]+")) { warn("User ID must contain letters and numbers only."); return false; }
        return true;
    }

    private boolean validateFullName(String name) {
        if (name.isEmpty()) { warn("Full name is required."); return false; }
        if (!name.matches("[a-zA-Z ]+")) { warn("Full name must contain letters and spaces only."); return false; }
        return true;
    }

    private boolean validatePassword(String password) {
        if (password.isEmpty()) { warn("Password is required."); return false; }
        if (password.length() < 4) { warn("Password must be at least 4 characters long."); return false; }
        return true;
    }

    private boolean validateEmail(String email, String role) {
        if (email.isEmpty()) {
            if (!role.equals("ADMIN")) { warn("Email is required for role: " + role); return false; }
            return true;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) { warn("Invalid email format. Example: user@example.com"); return false; }
        return true;
    }

    @FXML public void handleCreate() {
        if (!isAdmin()) { deny(); return; }
        String userId = userIdField.getText().trim().toUpperCase();
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String role = roleCombo.getValue();

        if (!validateUserId(userId)) return;
        if (!validateFullName(username)) return;
        if (!validatePassword(password)) return;
        if (role == null) { warn("Role is required."); return; }
        if (!validateEmail(email, role)) return;
        if (userDAO.userIdExists(userId)) { warn("User ID '" + userId + "' is already taken."); return; }

        User u = new User(userId, username, password, role, email, phone);
        if (userDAO.createUser(u)) {
            success("User '" + username + "' created successfully.\nID: " + userId + " | Role: " + role + "\nShare this ID with the user for login.");
            loadTable(); clearForm();
        } else { error("Failed to create user. ID may already exist."); }
    }

    @FXML public void handleUpdate() {
        if (!isAdmin()) { deny(); return; }
        User sel = userTable.getSelectionModel().getSelectedItem();
        if (sel == null) { warn("Select a user from the table first."); return; }
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String role = roleCombo.getValue();
        if (!validateFullName(username)) return;
        if (!validatePassword(password)) return;
        if (role == null) { warn("Role is required."); return; }
        if (!validateEmail(email, role)) return;
        sel.setUsername(username);
        sel.setPassword(password);
        sel.setRole(role);
        sel.setEmail(email);
        sel.setPhone(phone);
        if (userDAO.updateUser(sel)) { success("User updated successfully."); loadTable(); clearForm(); }
        else { error("Failed to update user."); }
    }

    @FXML public void handleDelete() {
        if (!isAdmin()) { deny(); return; }
        User sel = userTable.getSelectionModel().getSelectedItem();
        if (sel == null) { warn("Select a user from the table first."); return; }
        if (sel.getUserId().equalsIgnoreCase(SessionManager.getUserId())) { warn("You cannot delete your own account."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete user: " + sel.getUsername() + " (" + sel.getUserId() + ")");
        confirm.setContentText("This will also remove their linked records. Continue?");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                if (userDAO.deleteUser(sel.getUserId())) { success("User deleted successfully."); loadTable(); clearForm(); }
                else { error("Failed to delete user."); }
            }
        });
    }

    @FXML public void handleClearForm() { clearForm(); }

    @FXML public void handleRoleSelected() {
        String role = roleCombo.getValue();
        if (role == null) return;
        switch (role) {
            case "CUSTOMER"  -> info("Will also create a Customer record linked to this user.");
            case "POLICE"    -> info("Police user — access to police reports and rule breaches.");
            case "INSURANCE" -> info("Insurance user — access to insurance policies.");
            case "WORKSHOP"  -> info("Workshop user — access to service records.");
            case "ADMIN"     -> info("Full system access. Use with caution.");
        }
    }

    private void success(String m) { messageLabel.setStyle("-fx-text-fill:#2e7d32;-fx-font-weight:bold;"); messageLabel.setText(m); }
    private void error(String m)   { messageLabel.setStyle("-fx-text-fill:#c62828;-fx-font-weight:bold;"); messageLabel.setText(m); }
    private void warn(String m)    { messageLabel.setStyle("-fx-text-fill:#e65100;-fx-font-weight:bold;"); messageLabel.setText(m); }
    private void info(String m)    { messageLabel.setStyle("-fx-text-fill:#1565c0;"); messageLabel.setText(m); }
    private void deny()            { error("Access denied. Admins only."); }
    private boolean isAdmin()      { return "ADMIN".equals(SessionManager.getRole()); }

   private Stage resolveStage(ActionEvent e) {
        Object source = e.getSource();
        if (source instanceof Node node) {
            return (Stage) node.getScene().getWindow();
        } else if (source instanceof MenuItem mi) {
            ContextMenu cm = mi.getParentPopup();
            if (cm != null && cm.getOwnerNode() != null) {
                return (Stage) cm.getOwnerNode().getScene().getWindow();
            }
        }
        return Stage.getWindows().stream()
                .filter(w -> w instanceof Stage && w.isShowing())
                .map(w -> (Stage) w)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No active stage found"));
    }

    @FXML public void handleExit()               { System.exit(0); }
    @FXML public void goToDashboard(ActionEvent e) { NavigationUtil.navigateTo("dashboard.fxml",     resolveStage(e)); }
    @FXML public void goToVehicle(ActionEvent e)   { NavigationUtil.navigateTo("vehicle.fxml",       resolveStage(e)); }
    @FXML public void goToCustomer(ActionEvent e)  { NavigationUtil.navigateTo("customer.fxml",      resolveStage(e)); }
    @FXML public void goToWorkshop(ActionEvent e)  { NavigationUtil.navigateTo("workshop.fxml",      resolveStage(e)); }
    @FXML public void goToPolice(ActionEvent e)    { NavigationUtil.navigateTo("police.fxml",        resolveStage(e)); }
    @FXML public void goToViolation(ActionEvent e) { NavigationUtil.navigateTo("Violation.fxml",     resolveStage(e)); }
    @FXML public void goToInsurance(ActionEvent e) { NavigationUtil.navigateTo("InsuranceView.fxml", resolveStage(e)); }
    @FXML public void goToAdmin(ActionEvent e)     { NavigationUtil.navigateTo("AdminView.fxml",     resolveStage(e)); }
    @FXML public void handleLogout(ActionEvent e)  {
        SessionManager.clearSession();
        NavigationUtil.navigateTo("login.fxml", resolveStage(e));
    }
}