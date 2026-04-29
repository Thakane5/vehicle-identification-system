package com.example.vehicleidentification.controllers;

import com.example.vehicleidentification.DataAccessObject.PoliceReportDAO;
import com.example.vehicleidentification.DataAccessObject.VehicleDAO;
import com.example.vehicleidentification.NavigationUtil;
import com.example.vehicleidentification.SessionManager;
import com.example.vehicleidentification.model.PoliceReport;
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
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
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

public class PoliceController implements Initializable {

    @FXML private VBox accessDeniedPane;
    @FXML private HBox mainContentPane;

    @FXML private ScrollPane formScrollPane;
    @FXML private VBox actionButtonsPane;

    @FXML private Label totalReportsLabel;
    @FXML private Label accidentsLabel;
    @FXML private Label theftsLabel;

    @FXML private ComboBox<Vehicle> vehicleCombo;
    @FXML private Label             vehicleInfoLabel;
    @FXML private DatePicker        reportDatePicker;
    @FXML private ComboBox<String>  reportTypeCombo;
    @FXML private TextArea          descriptionField;
    @FXML private TextField         officerField, stationField, caseNumberField, searchField;
    @FXML private Label             messageLabel;
    @FXML private Label             tableMessageLabel;
    @FXML private Button            btnDelete;

    @FXML private TableView<PoliceReport>            policeTable;
    @FXML private TableColumn<PoliceReport, Integer> colId;
    @FXML private TableColumn<PoliceReport, String>  colVehicleReg, colType, colDesc, colOfficer, colStation, colCase;
    @FXML private TableColumn<PoliceReport, LocalDate> colDate;

    @FXML private Button logoutButton;

    private final PoliceReportDAO policeDAO  = new PoliceReportDAO();
    private final VehicleDAO      vehicleDAO = new VehicleDAO();

    private ObservableList<PoliceReport> allReports = FXCollections.observableArrayList();
    private String currentRole;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentRole = SessionManager.getLoggedInUser().getRole();

        if (!currentRole.equals("POLICE") && !currentRole.equals("ADMIN")) {
            showAccessDenied();
            return;
        }

        showMainContent();
        setupTable();
        applyVisualEffects();

        switch (currentRole) {
            case "POLICE" -> {
                showFormPanel(true);
                loadVehicleCombo();
                loadTable();
                applyInputFilters();
                policeTable.getSelectionModel().selectedItemProperty()
                        .addListener((obs, old, sel) -> { if (sel != null) populateForm(sel); });
                vehicleCombo.getSelectionModel().selectedItemProperty()
                        .addListener((obs, old, v) -> {
                            if (v != null)
                                vehicleInfoLabel.setText(v.getMake() + " " + v.getModel()
                                        + " | Year: " + v.getYear() + " | Owner ID: " + v.getOwnerId());
                            else vehicleInfoLabel.setText("");
                        });
            }
            case "ADMIN" -> {
                showFormPanel(false);
                loadTable();
            }
        }
    }

    private void showFormPanel(boolean visible) {
        if (formScrollPane != null) {
            formScrollPane.setVisible(visible);
            formScrollPane.setManaged(visible);
        }
    }

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

    private void showAccessDenied() {
        accessDeniedPane.setVisible(true);  accessDeniedPane.setManaged(true);
        mainContentPane .setVisible(false); mainContentPane .setManaged(false);
    }

    private void showMainContent() {
        accessDeniedPane.setVisible(false); accessDeniedPane.setManaged(false);
        mainContentPane .setVisible(true);  mainContentPane .setManaged(true);
    }

    private void setupTable() {
        reportTypeCombo.setItems(FXCollections.observableArrayList(
                "Accident", "Theft", "Inspection", "Missing", "Other"));
        colId        .setCellValueFactory(new PropertyValueFactory<>("reportId"));
        colVehicleReg.setCellValueFactory(new PropertyValueFactory<>("vehicleReg"));
        colDate      .setCellValueFactory(new PropertyValueFactory<>("reportDate"));
        colType      .setCellValueFactory(new PropertyValueFactory<>("reportType"));
        colDesc      .setCellValueFactory(new PropertyValueFactory<>("description"));
        colOfficer   .setCellValueFactory(new PropertyValueFactory<>("officerName"));
        colStation   .setCellValueFactory(new PropertyValueFactory<>("stationName"));
        colCase      .setCellValueFactory(new PropertyValueFactory<>("caseNumber"));

        policeTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(PoliceReport item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) { setStyle(""); return; }
                switch (item.getReportType() != null ? item.getReportType() : "") {
                    case "Accident" -> setStyle("-fx-background-color: #ffebee;");
                    case "Theft"    -> setStyle("-fx-background-color: #fff3e0;");
                    case "Missing"  -> setStyle("-fx-background-color: #fce4ec;");
                    default         -> setStyle("");
                }
            }
        });
    }

    private void loadVehicleCombo() {
        vehicleCombo.setItems(FXCollections.observableArrayList(vehicleDAO.getAllVehicles()));
        vehicleCombo.setConverter(new StringConverter<>() {
            @Override public String toString(Vehicle v) {
                return v == null ? "" : v.getRegistrationNumber() + " - " + v.getMake() + " " + v.getModel();
            }
            @Override public Vehicle fromString(String s) { return null; }
        });
    }

    private void loadTable() {
        allReports = FXCollections.observableArrayList(policeDAO.getAllReports());
        policeTable.setItems(allReports);
        updateSummary();
    }

    private void updateSummary() {
        totalReportsLabel.setText(String.valueOf(allReports.size()));
        long accidents = allReports.stream().filter(r -> "Accident".equalsIgnoreCase(r.getReportType())).count();
        long thefts = allReports.stream().filter(r -> "Theft".equalsIgnoreCase(r.getReportType())).count();
        accidentsLabel.setText(String.valueOf(accidents));
        theftsLabel.setText(String.valueOf(thefts));
    }

    private void populateForm(PoliceReport r) {
        vehicleCombo.getItems().stream()
                .filter(v -> v.getVehicleId() == r.getVehicleId())
                .findFirst().ifPresent(vehicleCombo::setValue);
        reportDatePicker.setValue(r.getReportDate());
        reportTypeCombo.setValue(r.getReportType());
        descriptionField.setText(r.getDescription() != null ? r.getDescription() : "");
        officerField.setText(r.getOfficerName() != null ? r.getOfficerName() : "");
        stationField.setText(r.getStationName() != null ? r.getStationName() : "");
        caseNumberField.setText(r.getCaseNumber() != null ? r.getCaseNumber() : "");
        messageLabel.setText("");
    }

    private void applyInputFilters() {
        officerField.textProperty().addListener((obs, o, n) -> {
            if (!n.matches("[a-zA-Z\\s]*")) officerField.setText(n.replaceAll("[^a-zA-Z\\s]", ""));
        });
        stationField.textProperty().addListener((obs, o, n) -> {
            if (!n.matches("[a-zA-Z\\s]*")) stationField.setText(n.replaceAll("[^a-zA-Z\\s]", ""));
        });
    }

    @FXML public void handleClear() {
        vehicleCombo.setValue(null); vehicleInfoLabel.setText("");
        reportDatePicker.setValue(null); reportTypeCombo.setValue(null);
        descriptionField.clear(); officerField.clear(); stationField.clear(); caseNumberField.clear();
        messageLabel.setText(""); policeTable.getSelectionModel().clearSelection();
    }

    @FXML public void handleAdd() {
        if (!validateForm()) return;
        try {
            if (policeDAO.addReport(buildReport(0))) { success("Report filed successfully."); loadTable(); handleClear(); }
            else error("Failed to file report.");
        } catch (Exception e) { error("Error: " + e.getMessage()); }
    }

    @FXML public void handleUpdate() {
        PoliceReport sel = policeTable.getSelectionModel().getSelectedItem();
        if (sel == null) { warn("Select a report from the table first."); return; }
        if (!validateForm()) return;
        try {
            if (policeDAO.updateReport(buildReport(sel.getReportId()))) { success("Report updated successfully."); loadTable(); handleClear(); }
            else error("Failed to update report.");
        } catch (Exception e) { error("Error: " + e.getMessage()); }
    }

    @FXML public void handleDelete() {
        PoliceReport sel = policeTable.getSelectionModel().getSelectedItem();
        if (sel == null) { warn("Select a report to delete."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete report #" + sel.getReportId());
        confirm.setContentText("This cannot be undone. Continue?");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                if (policeDAO.deleteReport(sel.getReportId())) { success("Report deleted."); loadTable(); handleClear(); }
                else error("Failed to delete.");
            }
        });
    }

    private PoliceReport buildReport(int id) {
        Vehicle v = vehicleCombo.getValue();
        return new PoliceReport(id, v.getVehicleId(), v.getRegistrationNumber(),
                SessionManager.getUserId(), reportDatePicker.getValue(), reportTypeCombo.getValue(),
                descriptionField.getText().trim(), officerField.getText().trim(),
                stationField.getText().trim(), caseNumberField.getText().trim());
    }

    private boolean validateForm() {
        if (vehicleCombo.getValue() == null)  { warn("Please select a vehicle.");   return false; }
        if (reportDatePicker.getValue() == null) { warn("Report date is required."); return false; }
        if (reportTypeCombo.getValue() == null) { warn("Report type is required.");  return false; }
        if (officerField.getText().isBlank())  { warn("Officer name is required.");  return false; }
        return true;
    }

    @FXML public void handleSearch() {
        String kw = searchField.getText().trim();
        if (kw.isEmpty()) { loadTable(); return; }
        List<PoliceReport> results = policeDAO.searchReports(kw);
        policeTable.setItems(FXCollections.observableArrayList(results));
        tableMessageLabel.setText(results.isEmpty() ? "No results found." : results.size() + " result(s) found.");
    }

    @FXML public void handleClearSearch() { searchField.clear(); policeTable.setItems(allReports); tableMessageLabel.setText(""); }
    @FXML public void filterAll()         { policeTable.setItems(allReports); tableMessageLabel.setText(""); }
    @FXML public void filterAccidents()   { filter("Accident"); }
    @FXML public void filterTheft()       { filter("Theft"); }
    @FXML public void filterInspection()  { filter("Inspection"); }

    private void filter(String type) {
        List<PoliceReport> filtered = allReports.stream()
                .filter(r -> type.equalsIgnoreCase(r.getReportType())).collect(Collectors.toList());
        policeTable.setItems(FXCollections.observableArrayList(filtered));
        tableMessageLabel.setText("Showing " + filtered.size() + " " + type + " report(s).");
    }

    private void success(String m) { messageLabel.setStyle("-fx-text-fill:#2e7d32;-fx-font-weight:bold;"); messageLabel.setText(m); }
    private void error(String m)   { messageLabel.setStyle("-fx-text-fill:#c62828;-fx-font-weight:bold;"); messageLabel.setText(m); }
    private void warn(String m)    { messageLabel.setStyle("-fx-text-fill:#e65100;-fx-font-weight:bold;"); messageLabel.setText(m); }

    private Stage resolveStage(ActionEvent e) {
        Object source = e.getSource();
        if (source instanceof Node node) return (Stage) node.getScene().getWindow();
        else if (source instanceof MenuItem mi) {
            ContextMenu cm = mi.getParentPopup();
            if (cm != null && cm.getOwnerNode() != null) return (Stage) cm.getOwnerNode().getScene().getWindow();
        }
        return Stage.getWindows().stream().filter(w -> w instanceof Stage && w.isShowing())
                .map(w -> (Stage) w).findFirst()
                .orElseThrow(() -> new IllegalStateException("No active stage found"));
    }

    @FXML public void handleExit()                 { System.exit(0); }
    @FXML public void goToDashboard(ActionEvent e) { NavigationUtil.navigateTo("dashboard.fxml",     resolveStage(e)); }
    @FXML public void goToVehicle(ActionEvent e)   { NavigationUtil.navigateTo("vehicle.fxml",       resolveStage(e)); }
    @FXML public void goToCustomer(ActionEvent e)  { NavigationUtil.navigateTo("customer.fxml",      resolveStage(e)); }
    @FXML public void goToWorkshop(ActionEvent e)  { NavigationUtil.navigateTo("workshop.fxml",      resolveStage(e)); }
    @FXML public void goToPolice(ActionEvent e)    { NavigationUtil.navigateTo("police.fxml",        resolveStage(e)); }
    @FXML public void goToViolation(ActionEvent e) { NavigationUtil.navigateTo("Violation.fxml",     resolveStage(e)); }
    @FXML public void goToInsurance(ActionEvent e) { NavigationUtil.navigateTo("InsuranceView.fxml", resolveStage(e)); }
    @FXML public void goToAdmin(ActionEvent e)     { NavigationUtil.navigateTo("AdminView.fxml",     resolveStage(e)); }

    @FXML public void handleLogout(ActionEvent e) {
        SessionManager.clearSession();
        NavigationUtil.navigateTo("login.fxml", resolveStage(e));
    }
}