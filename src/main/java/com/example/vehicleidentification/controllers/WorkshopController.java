package com.example.vehicleidentification.controllers;

import com.example.vehicleidentification.DataAccessObject.CustomerDAO;
import com.example.vehicleidentification.DataAccessObject.ServiceRecordDAO;
import com.example.vehicleidentification.DataAccessObject.VehicleDAO;
import com.example.vehicleidentification.NavigationUtil;
import com.example.vehicleidentification.SessionManager;
import com.example.vehicleidentification.model.Customer;
import com.example.vehicleidentification.model.ServiceRecord;
import com.example.vehicleidentification.model.Vehicle;
import javafx.animation.FadeTransition;
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
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class WorkshopController implements Initializable {

    @FXML private VBox accessDeniedPane;
    @FXML private VBox mainContentPane;
    @FXML private VBox customerPane;

    @FXML private ScrollPane formPanel;

    @FXML private ComboBox<Vehicle> vehicleCombo;
    @FXML private Label             vehicleInfoLabel;
    @FXML private DatePicker        serviceDatePicker;

    @FXML private TextField serviceTypeField, descriptionField, costField, searchField;
    @FXML private Label     messageLabel;
    @FXML private Label     tableMessageLabel;

    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;

    @FXML private Label accessInfoLabel;

    @FXML private Button logoutButton;

    @FXML private TableView<ServiceRecord>              serviceTable;
    @FXML private TableColumn<ServiceRecord, Integer>   colId, colVehicleId;
    @FXML private TableColumn<ServiceRecord, LocalDate> colDate;
    @FXML private TableColumn<ServiceRecord, String>    colType, colDesc;
    @FXML private TableColumn<ServiceRecord, Double>    colCost;

    @FXML private TableView<ServiceRecord>              customerServiceTable;
    @FXML private TableColumn<ServiceRecord, LocalDate> cColDate;
    @FXML private TableColumn<ServiceRecord, String>    cColType, cColDesc;
    @FXML private TableColumn<ServiceRecord, Double>    cColCost;
    @FXML private Label                                 customerMessageLabel;

    private final ServiceRecordDAO serviceDAO  = new ServiceRecordDAO();
    private final VehicleDAO       vehicleDAO  = new VehicleDAO();
    private final CustomerDAO      customerDAO = new CustomerDAO();
    private String currentRole;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentRole = SessionManager.getLoggedInUser().getRole();

        switch (currentRole) {
            case "WORKSHOP" -> {
                showMainContent();
                showFormPanel(true);
                setupColumns();
                loadVehicleCombo();
                loadTable();
                attachValidation();
                applyVisualEffects();
                if (accessInfoLabel != null) {
                    accessInfoLabel.setText("Workshop Access: Full access — create, update, delete.");
                    accessInfoLabel.setStyle("-fx-text-fill:#2e7d32;-fx-font-size:11px;");
                }
                serviceTable.getSelectionModel().selectedItemProperty()
                        .addListener((obs, old, sel) -> {
                            if (sel != null) populateForm(sel);
                        });
                vehicleCombo.getSelectionModel().selectedItemProperty()
                        .addListener((obs, old, v) -> {
                            if (v != null)
                                vehicleInfoLabel.setText(v.getMake() + " " + v.getModel()
                                        + " | Year: " + v.getYear()
                                        + " | Owner ID: " + v.getOwnerId());
                            else vehicleInfoLabel.setText("");
                        });
            }
            case "ADMIN" -> {
                showMainContent();
                showFormPanel(false);
                setupColumns();
                loadTable();
                applyVisualEffects();
            }
            case "CUSTOMER" -> {
                showCustomerPane();
                setupCustomerColumns();
                loadCustomerServices();
                applyVisualEffects();
            }
            default -> {
                showAccessDenied();
                applyVisualEffects();
            }
        }
    }

    private void showFormPanel(boolean visible) {
        if (formPanel != null) {
            formPanel.setVisible(visible);
            formPanel.setManaged(visible);
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
        if (customerPane != null) {
            customerPane.setVisible(false); customerPane.setManaged(false);
        }
    }

    private void showMainContent() {
        accessDeniedPane.setVisible(false); accessDeniedPane.setManaged(false);
        mainContentPane .setVisible(true);  mainContentPane .setManaged(true);
        if (customerPane != null) {
            customerPane.setVisible(false); customerPane.setManaged(false);
        }
    }

    private void showCustomerPane() {
        accessDeniedPane.setVisible(false); accessDeniedPane.setManaged(false);
        mainContentPane .setVisible(false); mainContentPane .setManaged(false);
        if (customerPane != null) {
            customerPane.setVisible(true); customerPane.setManaged(true);
        }
    }

    private void setupCustomerColumns() {
        if (cColDate == null) return;
        cColDate.setCellValueFactory(new PropertyValueFactory<>("serviceDate"));
        cColType.setCellValueFactory(new PropertyValueFactory<>("serviceType"));
        cColDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        cColCost.setCellValueFactory(new PropertyValueFactory<>("cost"));
    }

    private void loadCustomerServices() {
        String userId = SessionManager.getUserId();
        Customer customer = customerDAO.getCustomerByUserId(userId);

        if (customer == null) {
            if (customerMessageLabel != null)
                customerMessageLabel.setText("No customer profile linked to your account.");
            return;
        }

        List<Vehicle> myVehicles = vehicleDAO.getAllVehicles().stream()
                .filter(v -> v.getOwnerId() == customer.getCustomerId())
                .collect(Collectors.toList());

        List<ServiceRecord> myServices = myVehicles.stream()
                .flatMap(v -> serviceDAO.getServiceRecordsByVehicle(v.getVehicleId()).stream())
                .collect(Collectors.toList());

        if (customerServiceTable != null)
            customerServiceTable.setItems(FXCollections.observableArrayList(myServices));

        if (customerMessageLabel != null)
            customerMessageLabel.setText("Showing " + myServices.size()
                    + " service record(s) for your vehicle(s).");
    }

    private void attachValidation() {
        costField.textProperty().addListener((obs, o, n) -> {
            String cleaned = n.replaceAll("[^0-9.]", "");
            int firstDot = cleaned.indexOf('.');
            if (firstDot != -1)
                cleaned = cleaned.substring(0, firstDot + 1)
                        + cleaned.substring(firstDot + 1).replace(".", "");
            if (!n.equals(cleaned)) costField.setText(cleaned);
        });
    }

    private void setupColumns() {
        colId       .setCellValueFactory(new PropertyValueFactory<>("serviceId"));
        colVehicleId.setCellValueFactory(new PropertyValueFactory<>("vehicleId"));
        colDate     .setCellValueFactory(new PropertyValueFactory<>("serviceDate"));
        colType     .setCellValueFactory(new PropertyValueFactory<>("serviceType"));
        colDesc     .setCellValueFactory(new PropertyValueFactory<>("description"));
        colCost     .setCellValueFactory(new PropertyValueFactory<>("cost"));
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
        serviceTable.setItems(
                FXCollections.observableArrayList(serviceDAO.getAllServiceRecords()));
    }

    private void populateForm(ServiceRecord s) {
        vehicleCombo.getItems().stream()
                .filter(v -> v.getVehicleId() == s.getVehicleId())
                .findFirst()
                .ifPresent(vehicleCombo::setValue);
        serviceDatePicker.setValue(s.getServiceDate());
        serviceTypeField .setText(s.getServiceType() != null ? s.getServiceType() : "");
        descriptionField .setText(s.getDescription() != null ? s.getDescription() : "");
        costField        .setText(String.valueOf(s.getCost()));
    }

    @FXML public void handleClear() {
        vehicleCombo     .setValue(null);
        vehicleInfoLabel .setText("");
        serviceDatePicker.setValue(null);
        serviceTypeField .clear();
        descriptionField .clear();
        costField        .clear();
        messageLabel     .setText("");
        serviceTable.getSelectionModel().clearSelection();
    }

    @FXML public void handleAdd() {
        if (!currentRole.equals("WORKSHOP")) { warn("Access denied. Workshop users only."); return; }
        if (!validateForm()) return;
        try {
            Vehicle v = vehicleCombo.getValue();
            ServiceRecord record = new ServiceRecord(0, v.getVehicleId(),
                    serviceDatePicker.getValue(), serviceTypeField.getText().trim(),
                    descriptionField.getText().trim(), Double.parseDouble(costField.getText().trim()));
            if (serviceDAO.addServiceRecord(record)) {
                success("Service record added successfully.");
                loadTable(); handleClear();
            } else {
                error("Failed to add service record.");
            }
        } catch (Exception e) { error("Error: " + e.getMessage()); }
    }

    @FXML public void handleUpdate() {
        if (!currentRole.equals("WORKSHOP")) { warn("Access denied. Workshop users only."); return; }
        ServiceRecord sel = serviceTable.getSelectionModel().getSelectedItem();
        if (sel == null) { warn("Select a record from the table first."); return; }
        if (!validateForm()) return;
        try {
            Vehicle v = vehicleCombo.getValue();
            sel.setVehicleId(v.getVehicleId());
            sel.setServiceDate(serviceDatePicker.getValue());
            sel.setServiceType(serviceTypeField.getText().trim());
            sel.setDescription(descriptionField.getText().trim());
            sel.setCost(Double.parseDouble(costField.getText().trim()));
            if (serviceDAO.updateServiceRecord(sel)) {
                success("Service record updated successfully.");
                loadTable(); handleClear();
            } else { error("Failed to update record."); }
        } catch (Exception e) { error("Error: " + e.getMessage()); }
    }

    @FXML public void handleDelete() {
        if (!currentRole.equals("WORKSHOP")) { warn("Access denied. Workshop users only."); return; }
        ServiceRecord sel = serviceTable.getSelectionModel().getSelectedItem();
        if (sel == null) { warn("Select a record to delete."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete service record #" + sel.getServiceId() + "?");
        confirm.setContentText("This cannot be undone.");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                if (serviceDAO.deleteServiceRecord(sel.getServiceId())) {
                    success("Record deleted."); loadTable();
                } else { error("Failed to delete record."); }
            }
        });
    }

    @FXML public void handleSearch() {
        String kw = searchField.getText().trim().toLowerCase();
        if (kw.isEmpty()) { loadTable(); return; }
        List<ServiceRecord> filtered = serviceDAO.getAllServiceRecords().stream()
                .filter(s -> (s.getServiceType() != null
                        && s.getServiceType().toLowerCase().contains(kw))
                        || (s.getDescription() != null
                        && s.getDescription().toLowerCase().contains(kw)))
                .collect(Collectors.toList());
        serviceTable.setItems(FXCollections.observableArrayList(filtered));
        tableMessageLabel.setText(filtered.isEmpty() ? "No results found." : "");
    }

    @FXML public void handleClearSearch() {
        searchField.clear(); loadTable(); tableMessageLabel.setText("");
    }

    private boolean validateForm() {
        if (vehicleCombo.getValue()      == null) { warn("Please select a vehicle.");     return false; }
        if (serviceDatePicker.getValue() == null) { warn("Service date is required.");    return false; }
        if (serviceTypeField.getText().isBlank())  { warn("Service type is required.");   return false; }
        if (costField.getText().isBlank())         { warn("Cost is required.");            return false; }
        try { Double.parseDouble(costField.getText().trim()); }
        catch (NumberFormatException e)            { warn("Cost must be a valid number."); return false; }
        return true;
    }

    private void success(String m) { messageLabel.setStyle("-fx-text-fill:#2e7d32;-fx-font-weight:bold;"); messageLabel.setText(m); }
    private void error(String m)   { messageLabel.setStyle("-fx-text-fill:#c62828;-fx-font-weight:bold;"); messageLabel.setText(m); }
    private void warn(String m)    { messageLabel.setStyle("-fx-text-fill:#e65100;-fx-font-weight:bold;"); messageLabel.setText(m); }

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
                .map(w -> (Stage) w).findFirst()
                .orElseThrow(() -> new IllegalStateException("No active stage found"));
    }

    @FXML public void handleExit()                 { System.exit(0); }
    @FXML public void goToVehicle(ActionEvent e)   { NavigationUtil.navigateTo("vehicle.fxml",       resolveStage(e)); }
    @FXML public void goToService(ActionEvent e)   { NavigationUtil.navigateTo("workshop.fxml",      resolveStage(e)); }
    @FXML public void goToDashboard(ActionEvent e) { NavigationUtil.navigateTo("dashboard.fxml",     resolveStage(e)); }
    @FXML public void goToCustomer(ActionEvent e)  { NavigationUtil.navigateTo("customer.fxml",      resolveStage(e)); }
    @FXML public void goToWorkshop(ActionEvent e)  { NavigationUtil.navigateTo("workshop.fxml",      resolveStage(e)); }
    @FXML public void goToPolice(ActionEvent e)    { NavigationUtil.navigateTo("police.fxml",        resolveStage(e)); }
    @FXML public void goToViolation(ActionEvent e) { NavigationUtil.navigateTo("Violation.fxml",     resolveStage(e)); }
    @FXML public void goToInsurance(ActionEvent e) { NavigationUtil.navigateTo("InsuranceView.fxml", resolveStage(e)); }
    @FXML public void goToAdmin(ActionEvent e)     { NavigationUtil.navigateTo("AdminView.fxml",     resolveStage(e)); }

    @FXML public void handleLogout(ActionEvent e) {
        Stage stage = resolveStage(e);
        FadeTransition fade = new FadeTransition(Duration.millis(600), stage.getScene().getRoot());
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(ev -> {
            SessionManager.clearSession();
            NavigationUtil.navigateTo("login.fxml", stage);
        });
        fade.play();
    }
}