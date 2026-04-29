package com.example.vehicleidentification.controllers;

import com.example.vehicleidentification.DataAccessObject.CustomerDAO;
import com.example.vehicleidentification.DataAccessObject.UserDAO;
import com.example.vehicleidentification.DataAccessObject.VehicleDAO;
import com.example.vehicleidentification.NavigationUtil;
import com.example.vehicleidentification.SessionManager;
import com.example.vehicleidentification.model.Customer;
import com.example.vehicleidentification.model.User;
import com.example.vehicleidentification.model.Vehicle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class VehicleController implements Initializable {

    @FXML private VBox accessDeniedPane;
    @FXML private HBox mainContentPane;

    @FXML private TextField    regField, makeField, modelField, yearField,
            colorField, chassisField, searchField;
    @FXML private ComboBox<User> ownerCombo;
    @FXML private Label          ownerInfoLabel;
    @FXML private Label          messageLabel;
    @FXML private Label          tableMessageLabel;
    @FXML private Label          roleBadgeLabel;
    @FXML private Label          roleAccessLabel;

    // Admin-only buttons pane
    @FXML private VBox  adminButtonsPane;
    // Read-only notice for non-admin allowed roles
    @FXML private VBox  readOnlyNoticePane;
    @FXML private Label readOnlyLabel;

    @FXML private TableView<Vehicle>            vehicleTable;
    @FXML private TableColumn<Vehicle, Integer> colId, colYear;
    @FXML private TableColumn<Vehicle, String>  colReg, colMake, colModel,
            colColor, colChassis, colOwner;

    private final VehicleDAO  vehicleDAO  = new VehicleDAO();
    private final UserDAO     userDAO     = new UserDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();

    private String currentRole;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentRole = SessionManager.getRole();

        Platform.runLater(() -> {
            try {
                Stage stage = (Stage) roleBadgeLabel.getScene().getWindow();
                if (!stage.isMaximized()) stage.setMaximized(true);
            } catch (Exception ignored) {}
        });

        // ✅ ACCESS CONTROL
        // POLICE      → denied
        // INSURANCE   → denied
        // WORKSHOP    → denied
        // CUSTOMER    → allowed, sees own vehicles only, read-only
        // ADMIN       → full access
        switch (currentRole) {
            case "ADMIN" -> {
                showMainContent();
                setupColumns();
                loadOwnerCombo();
                loadTableByRole();
                showAdminControls();
            }
            case "CUSTOMER" -> {
                showMainContent();
                setupColumns();
                loadTableByRole();
                showReadOnly("You can view your registered vehicles.");
            }
            default -> {
                // POLICE, INSURANCE, WORKSHOP — all denied
                showAccessDenied();
                return;
            }
        }

        vehicleTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, sel) -> {
                    if (sel != null && currentRole.equals("ADMIN"))
                        populateForm(sel);
                });
    }

    // ── Panel switchers ───────────────────────────────────────────────────
    private void showAccessDenied() {
        accessDeniedPane.setVisible(true);  accessDeniedPane.setManaged(true);
        mainContentPane .setVisible(false); mainContentPane .setManaged(false);
    }

    private void showMainContent() {
        accessDeniedPane.setVisible(false); accessDeniedPane.setManaged(false);
        mainContentPane .setVisible(true);  mainContentPane .setManaged(true);
    }

    // ── Role UI controls ──────────────────────────────────────────────────
    private void showAdminControls() {
        // Show add/update/delete buttons
        if (adminButtonsPane != null) {
            adminButtonsPane.setVisible(true);
            adminButtonsPane.setManaged(true);
        }
        // Hide read-only notice
        if (readOnlyNoticePane != null) {
            readOnlyNoticePane.setVisible(false);
            readOnlyNoticePane.setManaged(false);
        }
        if (roleBadgeLabel  != null) roleBadgeLabel.setText("Viewing as: ADMIN");
        if (roleAccessLabel != null) roleAccessLabel.setText("Admin Access — Full CRUD");
    }

    private void showReadOnly(String message) {
        // Hide add/update/delete buttons
        if (adminButtonsPane != null) {
            adminButtonsPane.setVisible(false);
            adminButtonsPane.setManaged(false);
        }
        // Hide owner combo and its label — customers can't change ownership
        if (ownerCombo != null) {
            ownerCombo.setVisible(false);
            ownerCombo.setManaged(false);
        }
        if (ownerInfoLabel != null) {
            ownerInfoLabel.setVisible(false);
            ownerInfoLabel.setManaged(false);
        }
        // Show read-only notice
        if (readOnlyNoticePane != null) {
            readOnlyNoticePane.setVisible(true);
            readOnlyNoticePane.setManaged(true);
        }
        if (readOnlyLabel  != null) readOnlyLabel.setText(message);
        if (roleBadgeLabel != null) roleBadgeLabel.setText("Viewing as: CUSTOMER");
        if (roleAccessLabel!= null) roleAccessLabel.setText("View Only");
    }

    // ── Column setup ──────────────────────────────────────────────────────
    private void setupColumns() {
        colId     .setCellValueFactory(new PropertyValueFactory<>("vehicleId"));
        colReg    .setCellValueFactory(new PropertyValueFactory<>("registrationNumber"));
        colMake   .setCellValueFactory(new PropertyValueFactory<>("make"));
        colModel  .setCellValueFactory(new PropertyValueFactory<>("model"));
        colYear   .setCellValueFactory(new PropertyValueFactory<>("year"));
        colColor  .setCellValueFactory(new PropertyValueFactory<>("color"));
        colChassis.setCellValueFactory(new PropertyValueFactory<>("chassisNumber"));
        colOwner  .setCellValueFactory(new PropertyValueFactory<>("ownerId"));
    }

    // ── Owner combo — Admin only ──────────────────────────────────────────
    private void loadOwnerCombo() {
        List<User> customers = userDAO.getCustomerUsers();
        ownerCombo.setItems(FXCollections.observableArrayList(customers));
        ownerCombo.setConverter(new StringConverter<>() {
            @Override public String toString(User u) {
                return u == null ? "" : u.getUserId() + " — " + u.getUsername();
            }
            @Override public User fromString(String s) { return null; }
        });
        ownerCombo.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, u) -> {
                    if (u != null && ownerInfoLabel != null)
                        ownerInfoLabel.setText(
                                "Selected: " + u.getUsername()
                                        + " | ID: " + u.getUserId());
                });
    }

    // ── Load table by role ────────────────────────────────────────────────
    private void loadTableByRole() {
        if (currentRole.equals("ADMIN")) {
            vehicleTable.setItems(
                    FXCollections.observableArrayList(vehicleDAO.getAllVehicles()));
            if (tableMessageLabel != null) tableMessageLabel.setText("");

        } else if (currentRole.equals("CUSTOMER")) {
            // Find the customer record linked to this user
            String userId = SessionManager.getUserId();
            Customer c = customerDAO.getCustomerByUserId(userId);

            if (c == null) {
                vehicleTable.getItems().clear();
                if (tableMessageLabel != null)
                    tableMessageLabel.setText(
                            "No customer profile linked to your account.");
                return;
            }

            List<Vehicle> mine = vehicleDAO.getVehiclesByCustomerId(
                    c.getCustomerId());
            vehicleTable.setItems(FXCollections.observableArrayList(mine));

            if (tableMessageLabel != null)
                tableMessageLabel.setText(mine.isEmpty()
                        ? "No vehicles registered to your account."
                        : "Showing your " + mine.size() + " vehicle(s).");
        }
    }

    // ── Form populate ─────────────────────────────────────────────────────
    private void populateForm(Vehicle v) {
        regField    .setText(v.getRegistrationNumber());
        makeField   .setText(v.getMake());
        modelField  .setText(v.getModel());
        yearField   .setText(String.valueOf(v.getYear()));
        colorField  .setText(v.getColor()          != null ? v.getColor()          : "");
        chassisField.setText(v.getChassisNumber()  != null ? v.getChassisNumber()  : "");

        // Select owner in combo by customer_id
        ownerCombo.getItems().stream()
                .filter(u -> {
                    Customer c = customerDAO.getCustomerByUserId(u.getUserId());
                    return c != null && c.getCustomerId() == v.getOwnerId();
                })
                .findFirst()
                .ifPresent(ownerCombo::setValue);
    }

    @FXML public void handleClearForm() {
        regField    .clear();
        makeField   .clear();
        modelField  .clear();
        yearField   .clear();
        colorField  .clear();
        chassisField.clear();
        if (ownerCombo    != null) ownerCombo.setValue(null);
        if (ownerInfoLabel!= null) ownerInfoLabel.setText("");
        if (messageLabel  != null) messageLabel.setText("");
        vehicleTable.getSelectionModel().clearSelection();
    }

    // ── CRUD — Admin only ─────────────────────────────────────────────────
    @FXML public void handleAdd() {
        if (!isAdmin()) return;
        try {
            validateRequired();
            Vehicle v = buildVehicle(0);
            if (vehicleDAO.addVehicle(v)) {
                success("✅ Vehicle added successfully.");
                loadTableByRole();
                handleClearForm();
            } else {
                error("❌ Failed. Check for duplicate registration or chassis.");
            }
        } catch (IllegalArgumentException ex) { warn(ex.getMessage()); }
        catch (Exception ex) { error("❌ Error: " + ex.getMessage()); }
    }

    @FXML public void handleUpdate() {
        if (!isAdmin()) return;
        Vehicle sel = vehicleTable.getSelectionModel().getSelectedItem();
        if (sel == null) { warn("⚠ Select a vehicle first."); return; }
        try {
            validateRequired();
            Vehicle v = buildVehicle(sel.getVehicleId());
            if (vehicleDAO.updateVehicle(v)) {
                success("✅ Vehicle updated.");
                loadTableByRole();
                handleClearForm();
            } else {
                error("❌ Failed to update.");
            }
        } catch (IllegalArgumentException ex) { warn(ex.getMessage()); }
        catch (Exception ex) { error("❌ Error: " + ex.getMessage()); }
    }

    @FXML public void handleDelete() {
        if (!isAdmin()) return;
        Vehicle sel = vehicleTable.getSelectionModel().getSelectedItem();
        if (sel == null) { warn("⚠ Select a vehicle to delete."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete: " + sel.getRegistrationNumber());
        confirm.setContentText("This also removes related service records, "
                + "violations and insurance. Continue?");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                if (vehicleDAO.deleteVehicle(sel.getVehicleId())) {
                    success("✅ Vehicle deleted.");
                    loadTableByRole();
                    handleClearForm();
                } else {
                    error("❌ Failed to delete.");
                }
            }
        });
    }

    // ── Search ────────────────────────────────────────────────────────────
    @FXML public void handleSearch() {
        String kw = searchField.getText().trim().toLowerCase();
        if (kw.isEmpty()) { loadTableByRole(); return; }

        List<Vehicle> source;
        if (currentRole.equals("CUSTOMER")) {
            Customer c = customerDAO.getCustomerByUserId(SessionManager.getUserId());
            source = (c != null)
                    ? vehicleDAO.getVehiclesByCustomerId(c.getCustomerId())
                    : List.of();
        } else {
            source = vehicleDAO.getAllVehicles();
        }

        List<Vehicle> filtered = source.stream()
                .filter(v ->
                        safeContains(v.getRegistrationNumber(), kw)
                                || safeContains(v.getMake(), kw)
                                || safeContains(v.getModel(), kw))
                .collect(Collectors.toList());

        vehicleTable.setItems(FXCollections.observableArrayList(filtered));
        if (tableMessageLabel != null)
            tableMessageLabel.setText(filtered.isEmpty()
                    ? "No results found." : "");
    }

    @FXML public void handleClearSearch() {
        if (searchField != null) searchField.clear();
        loadTableByRole();
    }

    // ── Build vehicle from form ───────────────────────────────────────────
    private Vehicle buildVehicle(int id) {
        User selectedOwner = ownerCombo.getValue();
        int ownerId = 0;
        if (selectedOwner != null) {
            Customer c = customerDAO.getCustomerByUserId(
                    selectedOwner.getUserId());
            if (c != null) ownerId = c.getCustomerId();
        }
        return new Vehicle(
                id,
                regField    .getText().trim().toUpperCase(),
                makeField   .getText().trim(),
                modelField  .getText().trim(),
                Integer.parseInt(yearField.getText().trim()),
                colorField  .getText().trim(),
                chassisField.getText().trim(),
                ownerId
        );
    }

    private void validateRequired() {
        if (regField .getText().isBlank())
            throw new IllegalArgumentException("⚠ Registration number required.");
        if (makeField.getText().isBlank())
            throw new IllegalArgumentException("⚠ Make required.");
        if (modelField.getText().isBlank())
            throw new IllegalArgumentException("⚠ Model required.");
        if (yearField.getText().isBlank())
            throw new IllegalArgumentException("⚠ Year required.");
        if (ownerCombo.getValue() == null)
            throw new IllegalArgumentException("⚠ Please select an owner.");
        try { Integer.parseInt(yearField.getText().trim()); }
        catch (NumberFormatException e)
        { throw new IllegalArgumentException("⚠ Year must be a valid number."); }
    }

    private boolean isAdmin() {
        if (!"ADMIN".equals(currentRole)) {
            error("❌ Only Admin can perform this action.");
            return false;
        }
        return true;
    }

    private boolean safeContains(String f, String kw) {
        return f != null && f.toLowerCase().contains(kw);
    }

    private void success(String m) {
        messageLabel.setStyle("-fx-text-fill:#2e7d32;-fx-font-weight:bold;");
        messageLabel.setText(m);
    }
    private void error(String m) {
        messageLabel.setStyle("-fx-text-fill:#c62828;-fx-font-weight:bold;");
        messageLabel.setText(m);
    }
    private void warn(String m) {
        messageLabel.setStyle("-fx-text-fill:#e65100;-fx-font-weight:bold;");
        messageLabel.setText(m);
    }

    // ── Navigation ────────────────────────────────────────────────────────
    @FXML public void handleExit() { System.exit(0); }

    @FXML public void goToDashboard(ActionEvent e)  { NavigationUtil.navigateTo("dashboard.fxml",     (Node)e.getSource()); }
    @FXML public void goToVehicle(ActionEvent e)    { NavigationUtil.navigateTo("vehicle.fxml",       (Node)e.getSource()); }
    @FXML public void goToCustomer(ActionEvent e)   { NavigationUtil.navigateTo("customer.fxml",      (Node)e.getSource()); }
    @FXML public void goToWorkshop(ActionEvent e)   { NavigationUtil.navigateTo("workshop.fxml",      (Node)e.getSource()); }
    @FXML public void goToPolice(ActionEvent e)     { NavigationUtil.navigateTo("police.fxml",        (Node)e.getSource()); }
    @FXML public void goToViolation(ActionEvent e)  { NavigationUtil.navigateTo("Violation.fxml",     (Node)e.getSource()); }
    @FXML public void goToInsurance(ActionEvent e)  { NavigationUtil.navigateTo("InsuranceView.fxml", (Node)e.getSource()); }
    @FXML public void goToAdmin(ActionEvent e)      { NavigationUtil.navigateTo("AdminView.fxml",     (Node)e.getSource()); }
    @FXML public void handleLogout(ActionEvent e)   {
        SessionManager.clearSession();
        NavigationUtil.navigateTo("login.fxml", (Node)e.getSource());
    }
}