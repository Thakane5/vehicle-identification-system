package com.example.vehicleidentification.controllers;

import com.example.vehicleidentification.DataAccessObject.CustomerDAO;
import com.example.vehicleidentification.DataAccessObject.InsurancePolicyDAO;
import com.example.vehicleidentification.DataAccessObject.VehicleDAO;
import com.example.vehicleidentification.NavigationUtil;
import com.example.vehicleidentification.SessionManager;
import com.example.vehicleidentification.model.Customer;
import com.example.vehicleidentification.model.InsurancePolicy;
import com.example.vehicleidentification.model.Vehicle;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class InsurancePolicyController implements Initializable {

    // ── Access panes ──────────────────────────────────────────────────────
    @FXML private VBox accessDeniedPane;
    @FXML private VBox mainContentPane;

    // ── Summary labels ────────────────────────────────────────────────────
    @FXML private Label totalPoliciesLabel;
    @FXML private Label activePoliciesLabel;
    @FXML private Label expiredPoliciesLabel;

    // ── Form fields ───────────────────────────────────────────────────────
    @FXML private ComboBox<Vehicle> vehicleCombo;
    @FXML private Label             vehicleInfoLabel;
    @FXML private TextField         policyField, providerField,
            coverageTypeField, premiumField, searchField;
    @FXML private DatePicker        startDatePicker, endDatePicker;
    @FXML private ComboBox<String>  statusCombo;
    @FXML private Label             daysLeftLabel;
    @FXML private Label             messageLabel;
    @FXML private Label             tableMessageLabel;
    @FXML private Button            btnDelete;
    @FXML private Button            btnAdd;
    @FXML private Button            btnUpdate;
    @FXML private Button            btnClear;

    // ── Form panel ────────────────────────────────────────────────────────
    @FXML private VBox formPanel;

    // ── Role badge ────────────────────────────────────────────────────────
    @FXML private Label roleBadgeLabel;

    @FXML private Button logoutButton;

    // ── Table ─────────────────────────────────────────────────────────────
    @FXML private TableView<InsurancePolicy>              insuranceTable;
    @FXML private TableColumn<InsurancePolicy, Integer>   colId;
    @FXML private TableColumn<InsurancePolicy, String>    colVehicleReg, colPolicy,
            colProvider, colCoverage,
            colStatus, colDaysLeft;
    @FXML private TableColumn<InsurancePolicy, LocalDate> colStart, colEnd;
    @FXML private TableColumn<InsurancePolicy, Double>    colPremium;

    // ── DAOs ──────────────────────────────────────────────────────────────
    private final InsurancePolicyDAO insuranceDAO = new InsurancePolicyDAO();
    private final VehicleDAO         vehicleDAO   = new VehicleDAO();
    private final CustomerDAO        customerDAO  = new CustomerDAO();

    private ObservableList<InsurancePolicy> allRecords =
            FXCollections.observableArrayList();
    private String currentRole;

    // ─────────────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentRole = SessionManager.getLoggedInUser().getRole();

        // POLICE and WORKSHOP — access denied
        if (currentRole.equals("POLICE") || currentRole.equals("WORKSHOP")) {
            showAccessDenied();
            return;
        }

        showMainContent();
        setupTable();
        applyInputFilters();
        applyVisualEffects();

        insuranceTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, sel) -> {
                    if (sel != null && currentRole.equals("INSURANCE"))
                        populateForm(sel);
                });

        if (endDatePicker != null) {
            endDatePicker.valueProperty().addListener((obs, old, date) -> {
                if (date != null) showDaysLeft(date);
            });
        }

        if (vehicleCombo != null) {
            vehicleCombo.getSelectionModel().selectedItemProperty()
                    .addListener((obs, old, v) -> {
                        if (v != null)
                            vehicleInfoLabel.setText(v.getMake() + " " + v.getModel()
                                    + " | Reg: " + v.getRegistrationNumber());
                        else vehicleInfoLabel.setText("");
                    });
        }

        // Load data based on role
        switch (currentRole) {
            case "INSURANCE" -> {
                loadVehicleCombo();
                loadTable();
            }
            case "ADMIN" -> loadTable();
            case "CUSTOMER" -> loadCustomerPolicies();
        }

        applyRolePermissions();
    }

    // ── Role permissions ──────────────────────────────────────────────────
    private void applyRolePermissions() {
        switch (currentRole) {
            case "INSURANCE" -> {
                // Full access — form visible, buttons visible
                if (formPanel != null) {
                    formPanel.setVisible(true);
                    formPanel.setManaged(true);
                }
                if (btnDelete != null) {
                    btnDelete.setVisible(true);
                    btnDelete.setManaged(true);
                }
                if (btnAdd != null) {
                    btnAdd.setVisible(true);
                    btnAdd.setManaged(true);
                }
                if (btnUpdate != null) {
                    btnUpdate.setVisible(true);
                    btnUpdate.setManaged(true);
                }
                if (btnClear != null) {
                    btnClear.setVisible(true);
                    btnClear.setManaged(true);
                }
                if (roleBadgeLabel != null)
                    roleBadgeLabel.setText("Role: INSURANCE — Full Access");
            }

            case "ADMIN" -> {
                // View only — hide form panel
                if (formPanel != null) {
                    formPanel.setVisible(false);
                    formPanel.setManaged(false);
                }
                if (roleBadgeLabel != null)
                    roleBadgeLabel.setText("Role: ADMIN — View Only");
            }

            case "CUSTOMER" -> {
                // View own policies only — hide form
                if (formPanel != null) {
                    formPanel.setVisible(false);
                    formPanel.setManaged(false);
                }
                if (roleBadgeLabel != null)
                    roleBadgeLabel.setText("Role: CUSTOMER — Your Policies Only");
            }
        }
    }

    // ── Load data ─────────────────────────────────────────────────────────
    private void loadTable() {
        insuranceDAO.autoExpirePolicies();
        allRecords = FXCollections.observableArrayList(
                insuranceDAO.getAllPolicies());
        insuranceTable.setItems(allRecords);
        updateSummary();
    }

    private void loadCustomerPolicies() {
        String userId = SessionManager.getUserId();
        Customer c = customerDAO.getCustomerByUserId(userId);
        if (c == null) {
            if (tableMessageLabel != null)
                tableMessageLabel.setText("No customer profile linked to your account.");
            return;
        }
        insuranceDAO.autoExpirePolicies();
        List<InsurancePolicy> myPolicies =
                insuranceDAO.getPoliciesByCustomer(c.getCustomerId());
        allRecords = FXCollections.observableArrayList(myPolicies);
        insuranceTable.setItems(allRecords);
        updateSummary();

        if (tableMessageLabel != null)
            tableMessageLabel.setText("Showing your " + myPolicies.size()
                    + " insurance policy(ies).");
    }

    // ── Access panels ─────────────────────────────────────────────────────
    private void showAccessDenied() {
        if (accessDeniedPane != null) {
            accessDeniedPane.setVisible(true);
            accessDeniedPane.setManaged(true);
        }
        if (mainContentPane != null) {
            mainContentPane.setVisible(false);
            mainContentPane.setManaged(false);
        }
    }

    private void showMainContent() {
        if (accessDeniedPane != null) {
            accessDeniedPane.setVisible(false);
            accessDeniedPane.setManaged(false);
        }
        if (mainContentPane != null) {
            mainContentPane.setVisible(true);
            mainContentPane.setManaged(true);
        }
    }

    // ── Table setup ───────────────────────────────────────────────────────
    private void setupTable() {
        if (statusCombo != null)
            statusCombo.setItems(FXCollections.observableArrayList(
                    "Active", "Expired", "Cancelled"));

        colId        .setCellValueFactory(new PropertyValueFactory<>("policyId"));
        colVehicleReg.setCellValueFactory(new PropertyValueFactory<>("vehicleReg"));
        colPolicy    .setCellValueFactory(new PropertyValueFactory<>("policyNumber"));
        colProvider  .setCellValueFactory(new PropertyValueFactory<>("providerName"));
        colCoverage  .setCellValueFactory(new PropertyValueFactory<>("coverageType"));
        colStart     .setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colEnd       .setCellValueFactory(new PropertyValueFactory<>("endDate"));
        colStatus    .setCellValueFactory(new PropertyValueFactory<>("status"));
        colPremium   .setCellValueFactory(new PropertyValueFactory<>("premiumAmount"));
        colDaysLeft  .setCellValueFactory(new PropertyValueFactory<>("daysLeft"));

        insuranceTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(InsurancePolicy item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) { setStyle(""); return; }
                if ("Expired".equalsIgnoreCase(item.getStatus())
                        || (item.getEndDate() != null
                        && LocalDate.now().isAfter(item.getEndDate()))) {
                    setStyle("-fx-background-color: #ffebee;");
                } else if (item.getEndDate() != null) {
                    long days = ChronoUnit.DAYS.between(
                            LocalDate.now(), item.getEndDate());
                    setStyle(days <= 30
                            ? "-fx-background-color: #fff8e1;" : "");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void loadVehicleCombo() {
        if (vehicleCombo == null) return;
        vehicleCombo.setItems(
                FXCollections.observableArrayList(vehicleDAO.getAllVehicles()));
        vehicleCombo.setConverter(new StringConverter<>() {
            @Override public String toString(Vehicle v) {
                return v == null ? ""
                        : v.getRegistrationNumber()
                        + " - " + v.getMake() + " " + v.getModel();
            }
            @Override public Vehicle fromString(String s) { return null; }
        });
    }

    private void updateSummary() {
        totalPoliciesLabel.setText(String.valueOf(allRecords.size()));
        long active = allRecords.stream()
                .filter(i -> "Active".equalsIgnoreCase(i.getStatus())
                        && i.getEndDate() != null
                        && !LocalDate.now().isAfter(i.getEndDate())).count();
        long expired = allRecords.stream()
                .filter(i -> "Expired".equalsIgnoreCase(i.getStatus())
                        || (i.getEndDate() != null
                        && LocalDate.now().isAfter(i.getEndDate()))).count();
        activePoliciesLabel .setText(String.valueOf(active));
        expiredPoliciesLabel.setText(String.valueOf(expired));
    }

    private void showDaysLeft(LocalDate endDate) {
        if (daysLeftLabel == null) return;
        long days = ChronoUnit.DAYS.between(LocalDate.now(), endDate);
        if (days < 0) {
            daysLeftLabel.setStyle("-fx-text-fill: #c62828;");
            daysLeftLabel.setText("Expired " + Math.abs(days) + " days ago.");
        } else if (days <= 30) {
            daysLeftLabel.setStyle("-fx-text-fill: #e65100;");
            daysLeftLabel.setText("Expires in " + days + " days — expiring soon.");
        } else {
            daysLeftLabel.setStyle("-fx-text-fill: #2e7d32;");
            daysLeftLabel.setText(days + " days remaining.");
        }
    }

    private void populateForm(InsurancePolicy ins) {
        if (vehicleCombo == null) return;
        vehicleCombo.getItems().stream()
                .filter(v -> v.getVehicleId() == ins.getVehicleId())
                .findFirst().ifPresent(vehicleCombo::setValue);
        policyField      .setText(ins.getPolicyNumber());
        providerField    .setText(ins.getProviderName());
        coverageTypeField.setText(ins.getCoverageType() != null
                ? ins.getCoverageType() : "");
        startDatePicker  .setValue(ins.getStartDate());
        endDatePicker    .setValue(ins.getEndDate());
        statusCombo      .setValue(ins.getStatus());
        premiumField     .setText(String.valueOf(ins.getPremiumAmount()));
        if (ins.getEndDate() != null) showDaysLeft(ins.getEndDate());
    }

    @FXML public void handleClear() {
        if (vehicleCombo != null) vehicleCombo.setValue(null);
        if (vehicleInfoLabel != null) vehicleInfoLabel.setText("");
        if (policyField != null) policyField.clear();
        if (providerField != null) providerField.clear();
        if (coverageTypeField != null) coverageTypeField.clear();
        if (premiumField != null) premiumField.clear();
        if (startDatePicker != null) startDatePicker.setValue(null);
        if (endDatePicker != null) endDatePicker.setValue(null);
        if (statusCombo != null) statusCombo.setValue(null);
        if (daysLeftLabel != null) daysLeftLabel.setText("");
        if (messageLabel != null) messageLabel.setText("");
        insuranceTable.getSelectionModel().clearSelection();
    }

    // ── CRUD — INSURANCE role only ────────────────────────────────────────
    @FXML public void handleAdd() {
        if (!currentRole.equals("INSURANCE")) {
            error("Only Insurance users can add policies."); return;
        }
        if (!validateForm()) return;
        try {
            if (insuranceDAO.addPolicy(buildPolicy(0))) {
                success("Insurance policy added successfully.");
                loadTable(); handleClear();
            } else error("Failed to add policy.");
        } catch (Exception e) { error("Error: " + e.getMessage()); }
    }

    @FXML public void handleUpdate() {
        if (!currentRole.equals("INSURANCE")) {
            error("Only Insurance users can update policies."); return;
        }
        InsurancePolicy sel = insuranceTable.getSelectionModel().getSelectedItem();
        if (sel == null) { warn("Select a record first."); return; }
        if (!validateForm()) return;
        try {
            if (insuranceDAO.updatePolicy(buildPolicy(sel.getPolicyId()))) {
                success("Policy updated successfully.");
                loadTable(); handleClear();
            } else error("Failed to update policy.");
        } catch (Exception e) { error("Error: " + e.getMessage()); }
    }

    @FXML public void handleDelete() {
        if (!currentRole.equals("INSURANCE")) {
            error("Only Insurance users can delete policies."); return;
        }
        InsurancePolicy sel = insuranceTable.getSelectionModel().getSelectedItem();
        if (sel == null) { warn("Select a record to delete."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete policy: " + sel.getPolicyNumber());
        confirm.setContentText("This cannot be undone. Continue?");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                if (insuranceDAO.deletePolicy(sel.getPolicyId())) {
                    success("Policy deleted.");
                    loadTable(); handleClear();
                } else error("Failed to delete.");
            }
        });
    }

    private InsurancePolicy buildPolicy(int id) {
        Vehicle v = vehicleCombo.getValue();
        double premium = 0.0;
        try { premium = Double.parseDouble(premiumField.getText().trim()); }
        catch (NumberFormatException ignored) {}

        return new InsurancePolicy(
                id,
                v.getVehicleId(),
                v.getRegistrationNumber(),
                v.getOwnerId(),
                SessionManager.getUserId(),
                policyField      .getText().trim(),
                providerField    .getText().trim(),
                coverageTypeField.getText().trim(),
                startDatePicker  .getValue(),
                endDatePicker    .getValue(),
                premium,
                statusCombo.getValue() != null ? statusCombo.getValue() : "Active"
        );
    }

    private boolean validateForm() {
        if (vehicleCombo.getValue() == null)
        { warn("Please select a vehicle.");      return false; }
        if (policyField.getText().isBlank())
        { warn("Policy number is required.");    return false; }
        if (providerField.getText().isBlank())
        { warn("Provider name is required.");    return false; }
        if (startDatePicker.getValue() == null)
        { warn("Start date is required.");       return false; }
        if (startDatePicker.getValue().isAfter(LocalDate.now()))
        { warn("Start date cannot be in the future."); return false; }
        if (endDatePicker.getValue() == null)
        { warn("End date is required.");         return false; }
        if (endDatePicker.getValue().isBefore(startDatePicker.getValue()))
        { warn("End date cannot be before start date."); return false; }
        if (statusCombo.getValue() == null)
        { warn("Please select a status.");       return false; }
        return true;
    }

    // ── Filters & Search ──────────────────────────────────────────────────
    @FXML public void handleSearch() {
        String kw = searchField.getText().trim().toLowerCase();
        if (kw.isEmpty()) {
            if (currentRole.equals("CUSTOMER")) loadCustomerPolicies();
            else loadTable();
            return;
        }
        List<InsurancePolicy> filtered = allRecords.stream()
                .filter(i ->
                        (i.getVehicleReg()  != null
                                && i.getVehicleReg().toLowerCase().contains(kw))
                                || (i.getPolicyNumber() != null
                                && i.getPolicyNumber().toLowerCase().contains(kw))
                                || (i.getProviderName() != null
                                && i.getProviderName().toLowerCase().contains(kw)))
                .collect(Collectors.toList());
        insuranceTable.setItems(FXCollections.observableArrayList(filtered));
        if (tableMessageLabel != null)
            tableMessageLabel.setText(filtered.isEmpty()
                    ? "No matching records."
                    : filtered.size() + " record(s) found.");
    }

    @FXML public void handleClearSearch() {
        searchField.clear();
        if (currentRole.equals("CUSTOMER")) loadCustomerPolicies();
        else loadTable();
        if (tableMessageLabel != null) tableMessageLabel.setText("");
    }

    @FXML public void filterAll() {
        insuranceTable.setItems(allRecords);
        if (tableMessageLabel != null) tableMessageLabel.setText("");
    }

    @FXML public void filterActive() {
        List<InsurancePolicy> active = allRecords.stream()
                .filter(i -> "Active".equalsIgnoreCase(i.getStatus())
                        && i.getEndDate() != null
                        && !LocalDate.now().isAfter(i.getEndDate()))
                .collect(Collectors.toList());
        insuranceTable.setItems(FXCollections.observableArrayList(active));
        if (tableMessageLabel != null)
            tableMessageLabel.setText("Showing " + active.size() + " active policy(ies).");
    }

    @FXML public void filterExpired() {
        List<InsurancePolicy> expired = allRecords.stream()
                .filter(i -> "Expired".equalsIgnoreCase(i.getStatus())
                        || (i.getEndDate() != null
                        && LocalDate.now().isAfter(i.getEndDate())))
                .collect(Collectors.toList());
        insuranceTable.setItems(FXCollections.observableArrayList(expired));
        if (tableMessageLabel != null)
            tableMessageLabel.setText("Showing " + expired.size() + " expired policy(ies).");
    }

    // ── Input filters ─────────────────────────────────────────────────────
    private void applyInputFilters() {
        if (providerField != null)
            providerField.textProperty().addListener((obs, o, n) -> {
                if (!n.matches("[a-zA-Z\\s]*"))
                    providerField.setText(n.replaceAll("[^a-zA-Z\\s]", ""));
            });
        if (coverageTypeField != null)
            coverageTypeField.textProperty().addListener((obs, o, n) -> {
                if (!n.matches("[a-zA-Z\\s]*"))
                    coverageTypeField.setText(n.replaceAll("[^a-zA-Z\\s]", ""));
            });
        if (premiumField != null)
            premiumField.textProperty().addListener((obs, o, n) -> {
                if (!n.matches("\\d*\\.?\\d*")) premiumField.setText(o);
            });
    }

    // ── Visual effects ────────────────────────────────────────────────────
    private void applyVisualEffects() {
        if (logoutButton == null) return;
        DropShadow ds = new DropShadow();
        ds.setColor(Color.web("#c62828"));
        ds.setRadius(16);
        ds.setSpread(0.4);
        ds.setOffsetY(2);
        logoutButton.setEffect(ds);
        FadeTransition fade = new FadeTransition(Duration.seconds(1.4), logoutButton);
        fade.setFromValue(1.0);
        fade.setToValue(0.3);
        fade.setCycleCount(FadeTransition.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();
    }

    // ── Message helpers ───────────────────────────────────────────────────
    private void success(String m) {
        if (messageLabel == null) return;
        messageLabel.setStyle("-fx-text-fill:#2e7d32;-fx-font-weight:bold;");
        messageLabel.setText(m);
    }
    private void error(String m) {
        if (messageLabel == null) return;
        messageLabel.setStyle("-fx-text-fill:#c62828;-fx-font-weight:bold;");
        messageLabel.setText(m);
    }
    private void warn(String m) {
        if (messageLabel == null) return;
        messageLabel.setStyle("-fx-text-fill:#e65100;-fx-font-weight:bold;");
        messageLabel.setText(m);
    }

    // ── Navigation ────────────────────────────────────────────────────────
    private Stage resolveStage(ActionEvent e) {
        Object src = e.getSource();
        if (src instanceof Node node)
            return (Stage) node.getScene().getWindow();
        if (src instanceof MenuItem mi && mi.getParentPopup() != null
                && mi.getParentPopup().getOwnerNode() != null)
            return (Stage) mi.getParentPopup().getOwnerNode().getScene().getWindow();
        return Stage.getWindows().stream()
                .filter(w -> w instanceof Stage && w.isShowing())
                .map(w -> (Stage) w).findFirst()
                .orElseThrow(() -> new IllegalStateException("No active stage"));
    }

    @FXML public void handleExit()                 { System.exit(0); }
    @FXML public void goToInsurance(ActionEvent e) { NavigationUtil.navigateTo("InsuranceView.fxml", resolveStage(e)); }
    @FXML public void goToVehicle(ActionEvent e)   { NavigationUtil.navigateTo("vehicle.fxml",       resolveStage(e)); }
    @FXML public void goToDashboard(ActionEvent e) { NavigationUtil.navigateTo("dashboard.fxml",     resolveStage(e)); }
    @FXML public void goToCustomer(ActionEvent e)  { NavigationUtil.navigateTo("customer.fxml",      resolveStage(e)); }
    @FXML public void goToWorkshop(ActionEvent e)  { NavigationUtil.navigateTo("workshop.fxml",      resolveStage(e)); }
    @FXML public void goToPolice(ActionEvent e)    { NavigationUtil.navigateTo("police.fxml",        resolveStage(e)); }
    @FXML public void goToViolation(ActionEvent e) { NavigationUtil.navigateTo("Violation.fxml",     resolveStage(e)); }
    @FXML public void goToAdmin(ActionEvent e)     { NavigationUtil.navigateTo("AdminView.fxml",     resolveStage(e)); }
    @FXML public void handleLogout(ActionEvent e)  {
        SessionManager.clearSession();
        NavigationUtil.navigateTo("login.fxml", resolveStage(e));
    }
}