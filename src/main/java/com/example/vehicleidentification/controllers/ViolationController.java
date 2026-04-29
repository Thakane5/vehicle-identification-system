package com.example.vehicleidentification.controllers;

import com.example.vehicleidentification.DataAccessObject.CustomerDAO;
import com.example.vehicleidentification.DataAccessObject.ViolationDAO;
import com.example.vehicleidentification.DataAccessObject.VehicleDAO;
import com.example.vehicleidentification.NavigationUtil;
import com.example.vehicleidentification.SessionManager;
import com.example.vehicleidentification.model.Customer;
import com.example.vehicleidentification.model.Vehicle;
import com.example.vehicleidentification.model.Violation;
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
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ViolationController implements Initializable {

    // ── Panes ──────────────────────────────────────────────────────
    @FXML private VBox accessDeniedPane;
    @FXML private VBox customerPane;
    @FXML private VBox mainContentPane;   // POLICE full access
    @FXML private VBox adminViewPane;     // ADMIN view-only

    // ── Welcome / summary (shared labels used by police pane) ──────
    @FXML private Label welcomeLabel;
    @FXML private Label totalViolationsLabel;
    @FXML private Label unpaidLabel;
    @FXML private Label paidLabel;

    // ── Admin-only summary labels (separate pane) ──────────────────
    @FXML private Label adminTotalLabel;
    @FXML private Label adminUnpaidLabel;
    @FXML private Label adminPaidLabel;

    // ── Admin view-only table ──────────────────────────────────────
    @FXML private TableView<Violation>            adminViolationTable;
    @FXML private TableColumn<Violation, Integer> aColId;
    @FXML private TableColumn<Violation, String>  aColVehicleReg, aColType,
            aColDesc, aColStatus,
            aColOfficer, aColLocation;
    @FXML private TableColumn<Violation, LocalDate> aColDate;
    @FXML private TableColumn<Violation, Double>    aColFine;
    @FXML private TextField adminSearchField;
    @FXML private Label     adminTableMessageLabel;

    // ── Police / main form ─────────────────────────────────────────
    @FXML private ComboBox<Vehicle>  vehicleCombo;
    @FXML private Label              vehicleInfoLabel;
    @FXML private DatePicker         violationDatePicker;
    @FXML private ComboBox<String>   violationTypeCombo;
    @FXML private TextArea           descriptionField;
    @FXML private TextField          fineField, officerField,
            locationField, searchField;
    @FXML private ComboBox<String>   statusCombo;
    @FXML private Label              messageLabel;
    @FXML private Label              tableMessageLabel;
    @FXML private Button             btnDelete;

    // ── Police main table ──────────────────────────────────────────
    @FXML private TableView<Violation>            violationTable;
    @FXML private TableColumn<Violation, Integer> colId;
    @FXML private TableColumn<Violation, String>  colVehicleReg, colType,
            colDesc, colStatus,
            colOfficer, colLocation;
    @FXML private TableColumn<Violation, LocalDate> colDate;
    @FXML private TableColumn<Violation, Double>    colFine;

    // ── Customer table ─────────────────────────────────────────────
    @FXML private TableView<Violation>            customerViolationTable;
    @FXML private TableColumn<Violation, LocalDate> cColDate;
    @FXML private TableColumn<Violation, String>    cColVehicle, cColType, cColStatus;
    @FXML private TableColumn<Violation, Double>    cColFine;
    @FXML private Label                             customerMessageLabel;

    @FXML private Button logoutButton;

    private final ViolationDAO violationDAO = new ViolationDAO();
    private final VehicleDAO   vehicleDAO   = new VehicleDAO();
    private final CustomerDAO  customerDAO  = new CustomerDAO();

    private ObservableList<Violation> allViolations =
            FXCollections.observableArrayList();
    // Admin keeps its own observable list so police and admin don't share state
    private ObservableList<Violation> adminAllViolations =
            FXCollections.observableArrayList();

    private String currentRole;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentRole = SessionManager.getLoggedInUser().getRole();

        welcomeLabel.setText("Logged in as: "
                + SessionManager.getLoggedInUser().getUsername()
                + " [" + currentRole + "]");

        switch (currentRole) {

            // POLICE — full access: add, update, delete, mark paid
            case "POLICE" -> {
                showMainContent();
                setupMainTable();
                loadVehicleCombo();
                loadTable();
                // Police can delete — keep btnDelete visible
                applyInputFilters();
            }

            // ADMIN — view only: separate read-only pane, no form
            case "ADMIN" -> {
                showAdminView();
                setupAdminTable();
                loadAdminTable();
            }

            // CUSTOMER — own violations only, read-only
            case "CUSTOMER" -> {
                showCustomerPane();
                setupCustomerTable();
                loadCustomerBreaches();
            }

            // WORKSHOP, INSURANCE, others — denied
            default -> showAccessDenied();
        }

        applyVisualEffects();
    }

    // ----------------------------------------------------------------
    // PANE SWITCHING
    // ----------------------------------------------------------------
    private void showAccessDenied() {
        accessDeniedPane   .setVisible(true);  accessDeniedPane   .setManaged(true);
        customerPane       .setVisible(false); customerPane       .setManaged(false);
        mainContentPane    .setVisible(false); mainContentPane    .setManaged(false);
        adminViewPane      .setVisible(false); adminViewPane      .setManaged(false);
    }

    private void showCustomerPane() {
        accessDeniedPane   .setVisible(false); accessDeniedPane   .setManaged(false);
        customerPane       .setVisible(true);  customerPane       .setManaged(true);
        mainContentPane    .setVisible(false); mainContentPane    .setManaged(false);
        adminViewPane      .setVisible(false); adminViewPane      .setManaged(false);
    }

    private void showMainContent() {
        accessDeniedPane   .setVisible(false); accessDeniedPane   .setManaged(false);
        customerPane       .setVisible(false); customerPane       .setManaged(false);
        mainContentPane    .setVisible(true);  mainContentPane    .setManaged(true);
        adminViewPane      .setVisible(false); adminViewPane      .setManaged(false);
    }

    private void showAdminView() {
        accessDeniedPane   .setVisible(false); accessDeniedPane   .setManaged(false);
        customerPane       .setVisible(false); customerPane       .setManaged(false);
        mainContentPane    .setVisible(false); mainContentPane    .setManaged(false);
        adminViewPane      .setVisible(true);  adminViewPane      .setManaged(true);
    }

    // ----------------------------------------------------------------
    // ADMIN VIEW-ONLY TABLE SETUP & LOAD
    // ----------------------------------------------------------------
    private void setupAdminTable() {
        aColId        .setCellValueFactory(new PropertyValueFactory<>("violationId"));
        aColVehicleReg.setCellValueFactory(new PropertyValueFactory<>("vehicleReg"));
        aColDate      .setCellValueFactory(new PropertyValueFactory<>("violationDate"));
        aColType      .setCellValueFactory(new PropertyValueFactory<>("violationType"));
        aColDesc      .setCellValueFactory(new PropertyValueFactory<>("description"));
        aColFine      .setCellValueFactory(new PropertyValueFactory<>("fineAmount"));
        aColStatus    .setCellValueFactory(new PropertyValueFactory<>("status"));
        aColOfficer   .setCellValueFactory(new PropertyValueFactory<>("officerName"));
        aColLocation  .setCellValueFactory(new PropertyValueFactory<>("location"));

        adminViolationTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Violation item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) setStyle("");
                else if ("Unpaid".equalsIgnoreCase(item.getStatus()))
                    setStyle("-fx-background-color: #ffebee;");
                else setStyle("-fx-background-color: #e8f5e9;");
            }
        });
    }

    private void loadAdminTable() {
        adminAllViolations = FXCollections.observableArrayList(
                violationDAO.getAllViolations());
        adminViolationTable.setItems(adminAllViolations);
        updateAdminSummary();
    }

    private void updateAdminSummary() {
        adminTotalLabel .setText(String.valueOf(adminAllViolations.size()));
        long unpaid = adminAllViolations.stream()
                .filter(v -> "Unpaid".equalsIgnoreCase(v.getStatus())).count();
        long paid = adminAllViolations.stream()
                .filter(v -> "Paid".equalsIgnoreCase(v.getStatus())).count();
        adminUnpaidLabel.setText(String.valueOf(unpaid));
        adminPaidLabel  .setText(String.valueOf(paid));
    }

    // Admin search / filter handlers (separate from police search)
    @FXML public void handleAdminSearch() {
        String kw = adminSearchField.getText().trim().toLowerCase();
        if (kw.isEmpty()) { loadAdminTable(); return; }
        List<Violation> results = adminAllViolations.stream()
                .filter(v ->
                        (v.getVehicleReg()   != null && v.getVehicleReg()  .toLowerCase().contains(kw))
                                || (v.getViolationType() != null && v.getViolationType().toLowerCase().contains(kw))
                                || (v.getOfficerName()  != null && v.getOfficerName() .toLowerCase().contains(kw))
                                || (v.getLocation()     != null && v.getLocation()    .toLowerCase().contains(kw)))
                .collect(Collectors.toList());
        adminViolationTable.setItems(FXCollections.observableArrayList(results));
        adminTableMessageLabel.setText(results.isEmpty()
                ? "No results found." : results.size() + " result(s) found.");
    }

    @FXML public void handleAdminClearSearch() {
        adminSearchField.clear();
        adminViolationTable.setItems(adminAllViolations);
        adminTableMessageLabel.setText("");
    }

    @FXML public void handleAdminShowAll() {
        adminViolationTable.setItems(adminAllViolations);
        adminTableMessageLabel.setText("");
    }

    @FXML public void handleAdminFilterUnpaid() {
        List<Violation> unpaid = adminAllViolations.stream()
                .filter(v -> "Unpaid".equalsIgnoreCase(v.getStatus()))
                .collect(Collectors.toList());
        adminViolationTable.setItems(FXCollections.observableArrayList(unpaid));
        adminTableMessageLabel.setText("Showing " + unpaid.size() + " unpaid.");
    }

    @FXML public void handleAdminFilterPaid() {
        List<Violation> paid = adminAllViolations.stream()
                .filter(v -> "Paid".equalsIgnoreCase(v.getStatus()))
                .collect(Collectors.toList());
        adminViolationTable.setItems(FXCollections.observableArrayList(paid));
        adminTableMessageLabel.setText("Showing " + paid.size() + " paid.");
    }

    // ----------------------------------------------------------------
    // INPUT FILTERS — police only (letters + spaces for officer/location)
    // ----------------------------------------------------------------
    private void applyInputFilters() {
        officerField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[a-zA-Z\\s]*"))
                officerField.setText(newVal.replaceAll("[^a-zA-Z\\s]", ""));
        });
        locationField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[a-zA-Z\\s]*"))
                locationField.setText(newVal.replaceAll("[^a-zA-Z\\s]", ""));
        });
    }

    // ----------------------------------------------------------------
    // VISUAL EFFECTS — unchanged
    // ----------------------------------------------------------------
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

    // ----------------------------------------------------------------
    // POLICE MAIN TABLE SETUP — unchanged
    // ----------------------------------------------------------------
    private void setupMainTable() {
        violationTypeCombo.setItems(FXCollections.observableArrayList(
                "Speeding", "Parking Violation", "DUI", "Reckless Driving",
                "No Insurance", "Expired License", "Running Red Light", "Other"));
        statusCombo.setItems(FXCollections.observableArrayList("Paid", "Unpaid"));

        colId        .setCellValueFactory(new PropertyValueFactory<>("violationId"));
        colVehicleReg.setCellValueFactory(new PropertyValueFactory<>("vehicleReg"));
        colDate      .setCellValueFactory(new PropertyValueFactory<>("violationDate"));
        colType      .setCellValueFactory(new PropertyValueFactory<>("violationType"));
        colDesc      .setCellValueFactory(new PropertyValueFactory<>("description"));
        colFine      .setCellValueFactory(new PropertyValueFactory<>("fineAmount"));
        colStatus    .setCellValueFactory(new PropertyValueFactory<>("status"));
        colOfficer   .setCellValueFactory(new PropertyValueFactory<>("officerName"));
        colLocation  .setCellValueFactory(new PropertyValueFactory<>("location"));

        violationTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Violation item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) setStyle("");
                else if ("Unpaid".equalsIgnoreCase(item.getStatus()))
                    setStyle("-fx-background-color: #ffebee;");
                else setStyle("-fx-background-color: #e8f5e9;");
            }
        });

        violationTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, sel) -> {
                    if (sel != null) populateForm(sel);
                });

        vehicleCombo.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, v) -> {
                    if (v != null)
                        vehicleInfoLabel.setText(v.getMake() + " " + v.getModel()
                                + " | Year: " + v.getYear()
                                + " | Owner: " + v.getOwnerId());
                    else vehicleInfoLabel.setText("");
                });
    }

    private void setupCustomerTable() {
        cColDate   .setCellValueFactory(new PropertyValueFactory<>("violationDate"));
        cColVehicle.setCellValueFactory(new PropertyValueFactory<>("vehicleReg"));
        cColType   .setCellValueFactory(new PropertyValueFactory<>("violationType"));
        cColFine   .setCellValueFactory(new PropertyValueFactory<>("fineAmount"));
        cColStatus .setCellValueFactory(new PropertyValueFactory<>("status"));

        customerViolationTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Violation item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) setStyle("");
                else if ("Unpaid".equalsIgnoreCase(item.getStatus()))
                    setStyle("-fx-background-color: #ffebee;");
                else setStyle("-fx-background-color: #e8f5e9;");
            }
        });
    }

    private void loadCustomerBreaches() {
        String username = SessionManager.getLoggedInUser().getUsername();
        Customer customer = customerDAO.getAllCustomers().stream()
                .filter(c -> c.getName().equalsIgnoreCase(username)
                        || c.getEmail().equalsIgnoreCase(username))
                .findFirst().orElse(null);

        if (customer == null) {
            customerMessageLabel.setText(
                    "No customer profile linked to this account.");
            return;
        }

        List<Integer> myVehicleIds = vehicleDAO.getAllVehicles().stream()
                .filter(v -> v.getOwnerId() == customer.getCustomerId())
                .map(Vehicle::getVehicleId)
                .collect(Collectors.toList());

        List<Violation> myBreaches = violationDAO.getAllViolations().stream()
                .filter(v -> myVehicleIds.contains(v.getVehicleId()))
                .collect(Collectors.toList());

        customerViolationTable.setItems(
                FXCollections.observableArrayList(myBreaches));
        customerMessageLabel.setText(
                "Showing " + myBreaches.size() + " rule breach(es).");
    }

    private void loadVehicleCombo() {
        vehicleCombo.setItems(
                FXCollections.observableArrayList(vehicleDAO.getAllVehicles()));
        vehicleCombo.setConverter(new StringConverter<>() {
            @Override public String toString(Vehicle v) {
                return v == null ? "" : v.getRegistrationNumber()
                        + " - " + v.getMake() + " " + v.getModel();
            }
            @Override public Vehicle fromString(String s) { return null; }
        });
    }

    private void loadTable() {
        allViolations = FXCollections.observableArrayList(
                violationDAO.getAllViolations());
        violationTable.setItems(allViolations);
        updateSummary();
    }

    private void updateSummary() {
        totalViolationsLabel.setText(String.valueOf(allViolations.size()));
        long unpaid = allViolations.stream()
                .filter(v -> "Unpaid".equalsIgnoreCase(v.getStatus())).count();
        long paid = allViolations.stream()
                .filter(v -> "Paid".equalsIgnoreCase(v.getStatus())).count();
        unpaidLabel.setText(String.valueOf(unpaid));
        paidLabel  .setText(String.valueOf(paid));
    }

    private void populateForm(Violation v) {
        vehicleCombo.getItems().stream()
                .filter(vh -> vh.getVehicleId() == v.getVehicleId())
                .findFirst().ifPresent(vehicleCombo::setValue);
        violationDatePicker.setValue(v.getViolationDate());
        violationTypeCombo .setValue(v.getViolationType());
        descriptionField   .setText(v.getDescription() != null ? v.getDescription() : "");
        fineField          .setText(String.valueOf(v.getFineAmount()));
        officerField       .setText(v.getOfficerName() != null ? v.getOfficerName() : "");
        locationField      .setText(v.getLocation()    != null ? v.getLocation()    : "");
        statusCombo        .setValue(v.getStatus());
    }

    // ----------------------------------------------------------------
    // POLICE CRUD — unchanged logic, all available to POLICE
    // ----------------------------------------------------------------
    @FXML public void handleClear() {
        vehicleCombo       .setValue(null);
        vehicleInfoLabel   .setText("");
        violationDatePicker.setValue(null);
        violationTypeCombo .setValue(null);
        descriptionField   .clear();
        fineField          .clear();
        officerField       .clear();
        locationField      .clear();
        statusCombo        .setValue(null);
        messageLabel       .setText("");
        violationTable.getSelectionModel().clearSelection();
    }

    @FXML public void handleAdd() {
        if (!validateForm()) return;
        try {
            Vehicle v = vehicleCombo.getValue();
            Violation breach = new Violation(
                    0, v.getVehicleId(), v.getRegistrationNumber(),
                    violationDatePicker.getValue(),
                    violationTypeCombo.getValue(),
                    descriptionField.getText().trim(),
                    Double.parseDouble(fineField.getText().trim()),
                    statusCombo.getValue() != null ? statusCombo.getValue() : "Unpaid",
                    officerField .getText().trim(),
                    locationField.getText().trim());
            if (violationDAO.addViolation(breach)) {
                success("Rule breach added successfully.");
                loadTable(); handleClear();
            } else error("Failed to add rule breach.");
        } catch (Exception e) { error("Error: " + e.getMessage()); }
    }

    @FXML public void handleUpdate() {
        Violation sel = violationTable.getSelectionModel().getSelectedItem();
        if (sel == null) { warn("Select a rule breach from the table first."); return; }
        if (!validateForm()) return;
        try {
            Vehicle v = vehicleCombo.getValue();
            sel.setVehicleId(v.getVehicleId());
            sel.setVehicleReg(v.getRegistrationNumber());
            sel.setViolationDate(violationDatePicker.getValue());
            sel.setViolationType(violationTypeCombo.getValue());
            sel.setDescription(descriptionField.getText().trim());
            sel.setFineAmount(Double.parseDouble(fineField.getText().trim()));
            sel.setStatus(statusCombo.getValue());
            sel.setOfficerName(officerField .getText().trim());
            sel.setLocation(locationField.getText().trim());
            if (violationDAO.updateViolation(sel)) {
                success("Rule breach updated successfully.");
                loadTable(); handleClear();
            } else error("Failed to update rule breach.");
        } catch (Exception e) { error("Error: " + e.getMessage()); }
    }

    @FXML public void handleDelete() {
        Violation sel = violationTable.getSelectionModel().getSelectedItem();
        if (sel == null) { warn("Select a rule breach to delete."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete rule breach #" + sel.getViolationId());
        confirm.setContentText("This cannot be undone. Continue?");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                if (violationDAO.deleteViolation(sel.getViolationId())) {
                    success("Rule breach deleted.");
                    loadTable(); handleClear();
                } else error("Failed to delete rule breach.");
            }
        });
    }

    @FXML public void handleMarkPaid() {
        Violation sel = violationTable.getSelectionModel().getSelectedItem();
        if (sel == null) { warn("Select a rule breach to mark as paid."); return; }
        if (violationDAO.updateStatus(sel.getViolationId(), "Paid")) {
            success("Rule breach marked as Paid.");
            loadTable();
        } else error("Failed to update status.");
    }

    private boolean validateForm() {
        if (vehicleCombo       .getValue() == null) { warn("Please select a vehicle."); return false; }
        if (violationDatePicker.getValue() == null) { warn("Date is required.");         return false; }
        if (violationTypeCombo .getValue() == null) { warn("Breach type is required.");  return false; }
        if (fineField.getText().isBlank())          { warn("Fine amount is required.");  return false; }
        try { Double.parseDouble(fineField.getText().trim()); }
        catch (NumberFormatException e) { warn("Fine amount must be a number."); return false; }
        return true;
    }

    // ----------------------------------------------------------------
    // POLICE SEARCH & FILTER — unchanged
    // ----------------------------------------------------------------
    @FXML public void handleSearch() {
        String kw = searchField.getText().trim();
        if (kw.isEmpty()) { loadTable(); return; }
        List<Violation> results = violationDAO.searchViolations(kw);
        violationTable.setItems(FXCollections.observableArrayList(results));
        tableMessageLabel.setText(results.isEmpty()
                ? "No results found." : results.size() + " result(s) found.");
    }

    @FXML public void handleClearSearch() {
        searchField.clear();
        violationTable.setItems(allViolations);
        tableMessageLabel.setText("");
    }

    @FXML public void handleShowAll() {
        if (currentRole.equals("CUSTOMER")) loadCustomerBreaches();
        else { loadTable(); tableMessageLabel.setText(""); }
    }

    @FXML public void handleFilterUnpaid() {
        if (currentRole.equals("CUSTOMER")) {
            List<Violation> unpaid = customerViolationTable.getItems().stream()
                    .filter(v -> "Unpaid".equalsIgnoreCase(v.getStatus()))
                    .collect(Collectors.toList());
            customerViolationTable.setItems(
                    FXCollections.observableArrayList(unpaid));
            customerMessageLabel.setText("Showing unpaid rule breaches only.");
        } else {
            List<Violation> unpaid = allViolations.stream()
                    .filter(v -> "Unpaid".equalsIgnoreCase(v.getStatus()))
                    .collect(Collectors.toList());
            violationTable.setItems(FXCollections.observableArrayList(unpaid));
            tableMessageLabel.setText(
                    "Showing " + unpaid.size() + " unpaid rule breach(es).");
        }
    }

    @FXML public void handleFilterPaid() {
        List<Violation> paid = allViolations.stream()
                .filter(v -> "Paid".equalsIgnoreCase(v.getStatus()))
                .collect(Collectors.toList());
        violationTable.setItems(FXCollections.observableArrayList(paid));
        tableMessageLabel.setText(
                "Showing " + paid.size() + " paid rule breach(es).");
    }

    // ----------------------------------------------------------------
    // MESSAGE HELPERS — unchanged
    // ----------------------------------------------------------------
    private void success(String msg) {
        messageLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
        messageLabel.setText(msg);
    }
    private void error(String msg) {
        messageLabel.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
        messageLabel.setText(msg);
    }
    private void warn(String msg) {
        messageLabel.setStyle("-fx-text-fill: #e65100; -fx-font-weight: bold;");
        messageLabel.setText(msg);
    }

    // ----------------------------------------------------------------
    // NAVIGATION — unchanged
    // ----------------------------------------------------------------
    private Stage resolveStage(ActionEvent e) {
        Object source = e.getSource();
        if (source instanceof Node node)
            return (Stage) node.getScene().getWindow();
        else if (source instanceof MenuItem mi) {
            ContextMenu cm = mi.getParentPopup();
            if (cm != null && cm.getOwnerNode() != null)
                return (Stage) cm.getOwnerNode().getScene().getWindow();
        }
        return Stage.getWindows().stream()
                .filter(w -> w instanceof Stage && w.isShowing())
                .map(w -> (Stage) w)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No active stage found"));
    }

    @FXML public void handleExit() { System.exit(0); }
    @FXML public void goToViolations(ActionEvent e) { NavigationUtil.navigateTo("Violation.fxml",     resolveStage(e)); }
    @FXML public void goToPolice(ActionEvent e)     { NavigationUtil.navigateTo("police.fxml",        resolveStage(e)); }
    @FXML public void goToVehicle(ActionEvent e)    { NavigationUtil.navigateTo("vehicle.fxml",       resolveStage(e)); }
    @FXML public void goToDashboard(ActionEvent e)  { NavigationUtil.navigateTo("dashboard.fxml",     resolveStage(e)); }
    @FXML public void goToCustomer(ActionEvent e)   { NavigationUtil.navigateTo("customer.fxml",      resolveStage(e)); }
    @FXML public void goToWorkshop(ActionEvent e)   { NavigationUtil.navigateTo("workshop.fxml",      resolveStage(e)); }
    @FXML public void goToInsurance(ActionEvent e)  { NavigationUtil.navigateTo("InsuranceView.fxml", resolveStage(e)); }
    @FXML public void goToAdmin(ActionEvent e)      { NavigationUtil.navigateTo("AdminView.fxml",     resolveStage(e)); }
    @FXML public void handleLogout(ActionEvent e) {
        SessionManager.clearSession();
        NavigationUtil.navigateTo("login.fxml", resolveStage(e));
    }
}