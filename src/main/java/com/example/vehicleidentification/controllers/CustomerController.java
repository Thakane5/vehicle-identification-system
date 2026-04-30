package com.example.vehicleidentification.controllers;

import com.example.vehicleidentification.DataAccessObject.*;
import com.example.vehicleidentification.NavigationUtil;
import com.example.vehicleidentification.SessionManager;
import com.example.vehicleidentification.model.*;
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

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CustomerController implements Initializable {

    @FXML private VBox    accessDeniedPane;
    @FXML private VBox    mainContentPane;
    @FXML private VBox    adminPane;
    @FXML private VBox    customerPane;

    @FXML private Label   welcomeLabel;
    @FXML private Label   roleLabel;
    @FXML private Label   totalVehiclesLabel;
    @FXML private Label   unpaidBreachesLabel;
    @FXML private TabPane tabPane;

    @FXML private Button logoutButton;

    @FXML private TextField  adminNameField, adminAddressField, adminPhoneField, adminEmailField;
    @FXML private TextField  adminUsernameField, adminPasswordField;
    @FXML private CheckBox   createLoginCheckbox;
    @FXML private VBox       loginFieldsPane;
    @FXML private Label      adminMessageLabel;
    @FXML private TextField  adminSearchField;

    @FXML private TableView<Customer>            adminCustomerTable;
    @FXML private TableColumn<Customer, Integer> colCustId;
    @FXML private TableColumn<Customer, String>  colCustName, colCustEmail,
            colCustPhone, colCustAddress,
            colCustLoginAccess;

    @FXML private TableView<CustomerQuery>              adminQueryTable;
    @FXML private TableColumn<CustomerQuery, Integer>   colAdminQueryId;
    @FXML private TableColumn<CustomerQuery, String>    colAdminQueryText, colAdminQueryStatus;
    @FXML private TableColumn<CustomerQuery, LocalDate> colAdminQueryDate;
    @FXML private TextArea adminResponseArea;
    @FXML private Label    adminQueryMessageLabel;

    @FXML private TableView<Vehicle>            vehicleTable;
    @FXML private TableColumn<Vehicle, String>  colReg, colMake, colModel, colColor;
    @FXML private TableColumn<Vehicle, Integer> colYear;

    @FXML private TableView<ServiceRecord>              serviceTable;
    @FXML private TableColumn<ServiceRecord, String>    colServiceType, colServiceDesc;
    @FXML private TableColumn<ServiceRecord, LocalDate> colServiceDate;
    @FXML private TableColumn<ServiceRecord, Double>    colServiceCost;

    @FXML private TableView<Violation>              violationTable;
    @FXML private TableColumn<Violation, String>    colVType, colVStatus;
    @FXML private TableColumn<Violation, LocalDate> colVDate;
    @FXML private TableColumn<Violation, Double>    colVFine;

    @FXML private TableView<InsurancePolicy>              insuranceTable;
    @FXML private TableColumn<InsurancePolicy, String>    colInsProvider, colInsStatus, colInsDaysLeft;
    @FXML private TableColumn<InsurancePolicy, LocalDate> colInsStart, colInsEnd;
    @FXML private TableColumn<InsurancePolicy, Double>    colInsPremium;

    @FXML private ComboBox<Vehicle>                     queryVehicleCombo;
    @FXML private TextArea                              queryTextArea;
    @FXML private Label                                 queryMessageLabel;
    @FXML private TableView<CustomerQuery>              queryTable;
    @FXML private TableColumn<CustomerQuery, String>    colQueryText, colQueryResponse, colQueryStatus;
    @FXML private TableColumn<CustomerQuery, LocalDate> colQueryDate;

    private final CustomerDAO        customerDAO  = new CustomerDAO();
    private final VehicleDAO         vehicleDAO   = new VehicleDAO();
    private final ServiceRecordDAO   serviceDAO   = new ServiceRecordDAO();
    private final ViolationDAO       violationDAO = new ViolationDAO();
    private final InsurancePolicyDAO insuranceDAO = new InsurancePolicyDAO();
    private final CustomerQueryDAO   queryDAO     = new CustomerQueryDAO();
    private final UserDAO            userDAO      = new UserDAO();

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");

    private Customer currentCustomer;
    private String   currentRole;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentRole = SessionManager.getRole();
        switch (currentRole) {
            case "ADMIN" -> {
                showMainContent(); showAdminPane();
                setupAdminColumns(); loadAdminData();
                applyVisualEffects();
                setupAdminLiveValidation();
            }
            case "CUSTOMER" -> {
                showMainContent(); showCustomerPane();
                setupCustomerColumns(); loadCustomerData();
                applyVisualEffects();
            }
            default -> showAccessDenied();
        }
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

    private void setupAdminLiveValidation() {
        adminNameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[a-zA-Z ]*"))
                adminNameField.setText(newVal.replaceAll("[^a-zA-Z ]", ""));
        });

        adminPhoneField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[0-9]*"))
                adminPhoneField.setText(newVal.replaceAll("[^0-9]", ""));
            if (adminPhoneField.getText().length() > 15)
                adminPhoneField.setText(adminPhoneField.getText().substring(0, 15));
        });

        adminEmailField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                adminEmailField.setStyle(adminEmailField.getStyle()
                        .replace("-fx-border-color: #c62828;", ""));
                return;
            }
            if (!EMAIL_PATTERN.matcher(newVal).matches()) {
                adminEmailField.setStyle("-fx-border-color: #c62828; -fx-border-width: 2;");
            } else {
                adminEmailField.setStyle("-fx-border-color: #2e7d32; -fx-border-width: 2;");
            }
        });

        adminPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                adminPasswordField.setStyle("");
                return;
            }
            if (newVal.length() < 4) {
                adminPasswordField.setStyle("-fx-border-color: #c62828; -fx-border-width: 2;");
            } else {
                adminPasswordField.setStyle("-fx-border-color: #2e7d32; -fx-border-width: 2;");
            }
        });

        adminUsernameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("[a-zA-Z0-9]*"))
                adminUsernameField.setText(newVal.replaceAll("[^a-zA-Z0-9]", ""));
        });
    }

    private void showAccessDenied() {
        accessDeniedPane.setVisible(true);  accessDeniedPane.setManaged(true);
        mainContentPane .setVisible(false); mainContentPane .setManaged(false);
    }
    private void showMainContent() {
        accessDeniedPane.setVisible(false); accessDeniedPane.setManaged(false);
        mainContentPane .setVisible(true);  mainContentPane .setManaged(true);
    }
    private void showAdminPane() {
        adminPane   .setVisible(true);  adminPane   .setManaged(true);
        customerPane.setVisible(false); customerPane.setManaged(false);
        welcomeLabel.setText("Customer Management");
        roleLabel   .setText("Role: ADMIN");
    }
    private void showCustomerPane() {
        adminPane   .setVisible(false); adminPane   .setManaged(false);
        customerPane.setVisible(true);  customerPane.setManaged(true);
        welcomeLabel.setText("Welcome, " + SessionManager.getLoggedInUser().getUsername());
        roleLabel   .setText("Role: CUSTOMER");
    }

    @FXML public void handleLoginCheckboxToggle() {
        boolean checked = createLoginCheckbox.isSelected();
        loginFieldsPane.setVisible(checked);
        loginFieldsPane.setManaged(checked);
        if (!checked) {
            adminUsernameField.clear();
            adminPasswordField.clear();
        }
    }

    private void setupAdminColumns() {
        colCustId         .setCellValueFactory(new PropertyValueFactory<>("customerId"));
        colCustName       .setCellValueFactory(new PropertyValueFactory<>("name"));
        colCustEmail      .setCellValueFactory(new PropertyValueFactory<>("email"));
        colCustPhone      .setCellValueFactory(new PropertyValueFactory<>("phone"));
        colCustAddress    .setCellValueFactory(new PropertyValueFactory<>("address"));
        colCustLoginAccess.setCellValueFactory(new PropertyValueFactory<>("loginAccess"));

        colCustLoginAccess.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                setStyle(item.startsWith("✔")
                        ? "-fx-text-fill: #2e7d32; -fx-font-weight: bold;"
                        : "-fx-text-fill: #b71c1c; -fx-font-weight: bold;");
            }
        });

        colAdminQueryId    .setCellValueFactory(new PropertyValueFactory<>("queryId"));
        colAdminQueryDate  .setCellValueFactory(new PropertyValueFactory<>("queryDate"));
        colAdminQueryText  .setCellValueFactory(new PropertyValueFactory<>("queryText"));
        colAdminQueryStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        adminCustomerTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, sel) -> { if (sel != null) populateAdminForm(sel); });
        adminQueryTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, old, sel) -> {
                    if (sel != null && sel.getResponseText() != null)
                        adminResponseArea.setText(sel.getResponseText());
                });
    }

    private void loadAdminData() {
        adminCustomerTable.setItems(
                FXCollections.observableArrayList(customerDAO.getAllCustomers()));
        adminQueryTable.setItems(
                FXCollections.observableArrayList(queryDAO.getAllQueries()));
    }

    private void populateAdminForm(Customer c) {
        adminNameField   .setText(c.getName());
        adminAddressField.setText(c.getAddress());
        adminPhoneField  .setText(c.getPhone());
        adminEmailField  .setText(c.getEmail());
        createLoginCheckbox.setSelected(false);
        loginFieldsPane.setVisible(false);
        loginFieldsPane.setManaged(false);
        adminUsernameField.clear();
        adminPasswordField.clear();
        adminMessageLabel.setText("");
    }

    private void clearAdminForm() {
        adminNameField.clear(); adminAddressField.clear();
        adminPhoneField.clear(); adminEmailField.clear();
        adminUsernameField.clear(); adminPasswordField.clear();
        createLoginCheckbox.setSelected(false);
        loginFieldsPane.setVisible(false);
        loginFieldsPane.setManaged(false);
        adminMessageLabel.setText("");
        adminCustomerTable.getSelectionModel().clearSelection();
    }

    private boolean validateAdminForm(boolean withLogin) {
        String name  = adminNameField.getText().trim();
        String phone = adminPhoneField.getText().trim();
        String email = adminEmailField.getText().trim();

        if (name.isEmpty()) {
            setAdminMsg("Full name is required.", false); return false;
        }
        if (!name.matches("[a-zA-Z ]+")) {
            setAdminMsg("Full name must contain letters and spaces only.", false); return false;
        }
        if (!phone.isEmpty()) {
            if (phone.length() < 8) {
                setAdminMsg("Phone number must be at least 8 digits.", false); return false;
            }
            if (phone.length() > 15) {
                setAdminMsg("Phone number must be less than 15 digits.", false); return false;
            }
        }
        if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            setAdminMsg("Invalid email format. Example: user@example.com", false); return false;
        }
        if (withLogin) {
            String password = adminPasswordField.getText().trim();
            if (password.length() < 4) {
                setAdminMsg("Password must be at least 4 characters.", false); return false;
            }
        }
        return true;
    }

    @FXML public void handleAdminCreate() {
        boolean withLogin = createLoginCheckbox.isSelected();
        if (!validateAdminForm(withLogin)) return;

        String name    = adminNameField.getText().trim();
        String address = adminAddressField.getText().trim();
        String phone   = adminPhoneField.getText().trim();
        String email   = adminEmailField.getText().trim();

        if (withLogin) {
            String userId   = adminUsernameField.getText().trim().toUpperCase();
            String password = adminPasswordField.getText().trim();

            if (userId.isEmpty())   { setAdminMsg("User ID is required.",  false); return; }
            if (password.isEmpty()) { setAdminMsg("Password is required.", false); return; }

            if (userDAO.userIdExists(userId)) {
                setAdminMsg("User ID '" + userId + "' is already taken.", false);
                return;
            }

            User newUser = new User(userId, name, password, "CUSTOMER", email, phone);
            if (userDAO.createUser(newUser)) {
                if (!address.isBlank()) {
                    Customer created = customerDAO.getCustomerByUserId(userId);
                    if (created != null) {
                        created.setAddress(address);
                        customerDAO.updateCustomer(created);
                    }
                }
                setAdminMsg("Customer '" + name + "' created with login ID: " + userId, true);
                loadAdminData(); clearAdminForm();
            } else {
                setAdminMsg("Failed to create customer. ID may already exist.", false);
            }

        } else {
            Customer c = new Customer(0, null, name, address, phone, email);
            if (customerDAO.addCustomerNoLogin(c)) {
                setAdminMsg("Customer '" + name + "' added (no login account).", true);
                loadAdminData(); clearAdminForm();
            } else {
                setAdminMsg("Failed to add customer.", false);
            }
        }
    }

    @FXML public void handleAdminUpdate() {
        Customer sel = adminCustomerTable.getSelectionModel().getSelectedItem();
        if (sel == null) { setAdminMsg("Select a customer first.", false); return; }
        if (!validateAdminForm(false)) return;
        sel.setName(adminNameField.getText().trim());
        sel.setAddress(adminAddressField.getText().trim());
        sel.setPhone(adminPhoneField.getText().trim());
        sel.setEmail(adminEmailField.getText().trim());
        if (customerDAO.updateCustomer(sel)) {
            setAdminMsg("Customer updated successfully.", true);
            loadAdminData(); clearAdminForm();
        } else {
            setAdminMsg("Failed to update customer.", false);
        }
    }

    @FXML public void handleAdminDelete() {
        Customer sel = adminCustomerTable.getSelectionModel().getSelectedItem();
        if (sel == null) { setAdminMsg("Select a customer first.", false); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete customer: " + sel.getName());
        confirm.setContentText(
                "This will also delete all linked vehicles, queries and data. Continue?");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                if (customerDAO.deleteCustomer(sel.getCustomerId())) {
                    setAdminMsg("Customer deleted.", true);
                    loadAdminData(); clearAdminForm();
                } else {
                    setAdminMsg("Failed to delete customer.", false);
                }
            }
        });
    }

    @FXML public void handleAdminSearch() {
        String kw = adminSearchField.getText().trim();
        if (kw.isEmpty()) { loadAdminData(); return; }
        List<Customer> results = customerDAO.searchCustomers(kw);
        adminCustomerTable.setItems(FXCollections.observableArrayList(results));
        setAdminMsg(results.isEmpty()
                        ? "No customers found." : results.size() + " result(s) found.",
                !results.isEmpty());
    }

    @FXML public void handleAdminClearSearch() {
        adminSearchField.clear(); loadAdminData(); adminMessageLabel.setText("");
    }

    @FXML public void handleAdminClearForm() { clearAdminForm(); }

    @FXML public void handleRespondToQuery() {
        CustomerQuery sel = adminQueryTable.getSelectionModel().getSelectedItem();
        if (sel == null)                           { setAdminQueryMsg("Select a query first.", false); return; }
        if (adminResponseArea.getText().isBlank()) { setAdminQueryMsg("Enter a response.",    false); return; }
        if (queryDAO.respondToQuery(sel.getQueryId(), adminResponseArea.getText().trim())) {
            setAdminQueryMsg("Response saved. Query marked as Resolved.", true);
            loadAdminData(); adminResponseArea.clear();
        } else {
            setAdminQueryMsg("Failed to save response.", false);
        }
    }

    private void setAdminMsg(String msg, boolean ok) {
        adminMessageLabel.setStyle(ok
                ? "-fx-text-fill: #2e7d32; -fx-font-weight: bold;"
                : "-fx-text-fill: #c62828; -fx-font-weight: bold;");
        adminMessageLabel.setText(msg);
    }
    private void setAdminQueryMsg(String msg, boolean ok) {
        adminQueryMessageLabel.setStyle(ok
                ? "-fx-text-fill: #2e7d32; -fx-font-weight: bold;"
                : "-fx-text-fill: #c62828; -fx-font-weight: bold;");
        adminQueryMessageLabel.setText(msg);
    }

    private void setupCustomerColumns() {
        colReg  .setCellValueFactory(new PropertyValueFactory<>("registrationNumber"));
        colMake .setCellValueFactory(new PropertyValueFactory<>("make"));
        colModel.setCellValueFactory(new PropertyValueFactory<>("model"));
        colYear .setCellValueFactory(new PropertyValueFactory<>("year"));
        colColor.setCellValueFactory(new PropertyValueFactory<>("color"));

        colServiceDate.setCellValueFactory(new PropertyValueFactory<>("serviceDate"));
        colServiceType.setCellValueFactory(new PropertyValueFactory<>("serviceType"));
        colServiceDesc.setCellValueFactory(new PropertyValueFactory<>("description"));
        colServiceCost.setCellValueFactory(new PropertyValueFactory<>("cost"));

        colVDate  .setCellValueFactory(new PropertyValueFactory<>("violationDate"));
        colVType  .setCellValueFactory(new PropertyValueFactory<>("violationType"));
        colVFine  .setCellValueFactory(new PropertyValueFactory<>("fineAmount"));
        colVStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colInsStart   .setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colInsEnd     .setCellValueFactory(new PropertyValueFactory<>("endDate"));
        colInsProvider.setCellValueFactory(new PropertyValueFactory<>("providerName"));
        colInsPremium .setCellValueFactory(new PropertyValueFactory<>("premiumAmount"));
        colInsStatus  .setCellValueFactory(new PropertyValueFactory<>("status"));
        colInsDaysLeft.setCellValueFactory(new PropertyValueFactory<>("daysLeft"));

        colQueryDate    .setCellValueFactory(new PropertyValueFactory<>("queryDate"));
        colQueryText    .setCellValueFactory(new PropertyValueFactory<>("queryText"));
        colQueryResponse.setCellValueFactory(new PropertyValueFactory<>("responseText"));
        colQueryStatus  .setCellValueFactory(new PropertyValueFactory<>("status"));

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
    }

    private void loadCustomerData() {
        try {
            String loginId = SessionManager.getUserId();
            currentCustomer = customerDAO.getCustomerByUserId(loginId);

            if (currentCustomer == null) {
                totalVehiclesLabel .setText("0");
                unpaidBreachesLabel.setText("0");
                setQueryMsg("No customer profile linked to your account.", false);
                return;
            }

            int cid = currentCustomer.getCustomerId();

            List<Vehicle> vehicles = vehicleDAO.getVehiclesByCustomerId(cid);
            vehicleTable.setItems(FXCollections.observableArrayList(vehicles));
            totalVehiclesLabel.setText(String.valueOf(vehicles.size()));
            queryVehicleCombo.setItems(FXCollections.observableArrayList(vehicles));

            List<ServiceRecord> services = vehicles.stream()
                    .flatMap(v -> serviceDAO.getServiceRecordsByVehicle(v.getVehicleId()).stream())
                    .collect(Collectors.toList());
            serviceTable.setItems(FXCollections.observableArrayList(services));

            List<Violation> violations = vehicles.stream()
                    .flatMap(v -> violationDAO.getViolationsByVehicle(v.getVehicleId()).stream())
                    .collect(Collectors.toList());
            violationTable.setItems(FXCollections.observableArrayList(violations));
            long unpaid = violations.stream()
                    .filter(v -> "Unpaid".equalsIgnoreCase(v.getStatus())).count();
            unpaidBreachesLabel.setText(String.valueOf(unpaid));

            insuranceTable.setItems(FXCollections.observableArrayList(
                    insuranceDAO.getPoliciesByCustomer(cid)));
            queryTable.setItems(FXCollections.observableArrayList(
                    queryDAO.getQueriesByCustomer(cid)));

        } catch (Exception e) {
            System.err.println("Customer load error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML public void handleSubmitQuery() {
        if (currentCustomer == null) { setQueryMsg("No profile found.", false); return; }
        String  qText      = queryTextArea.getText().trim();
        Vehicle selVehicle = queryVehicleCombo.getValue();
        if (qText.isEmpty())    { setQueryMsg("Enter your query.", false);  return; }
        if (selVehicle == null) { setQueryMsg("Select a vehicle.", false);  return; }

        CustomerQuery q = new CustomerQuery(
                0, currentCustomer.getCustomerId(), selVehicle.getVehicleId(),
                LocalDate.now(), qText, "", "Pending");
        q.setUserId(SessionManager.getUserId());

        if (queryDAO.addQuery(q)) {
            setQueryMsg("Query submitted successfully.", true);
            queryTextArea.clear();
            queryVehicleCombo.setValue(null);
            loadCustomerData();
        } else {
            setQueryMsg("Failed to submit query.", false);
        }
    }

    private void setQueryMsg(String msg, boolean ok) {
        if (queryMessageLabel == null) return;
        queryMessageLabel.setStyle(ok
                ? "-fx-text-fill: #2e7d32; -fx-font-weight: bold;"
                : "-fx-text-fill: #c62828; -fx-font-weight: bold;");
        queryMessageLabel.setText(msg);
    }

    @FXML public void goToVehicles(ActionEvent e)       { tabPane.getSelectionModel().select(0); }
    @FXML public void goToServiceHistory(ActionEvent e) { tabPane.getSelectionModel().select(1); }
    @FXML public void goToViolations(ActionEvent e)     { tabPane.getSelectionModel().select(2); }
    @FXML public void goToInsurance(ActionEvent e)      { tabPane.getSelectionModel().select(3); }
    @FXML public void goToQueries(ActionEvent e)        { tabPane.getSelectionModel().select(4); }
    @FXML public void handleExit()                      { System.exit(0); }

    @FXML public void handleLogout(ActionEvent e) {
        SessionManager.clearSession();
        NavigationUtil.navigateTo("login.fxml", resolveStage(e));
    }

    // ----------------------------------------------------------------
    // NAVIGATION - handles both Node (Button) and MenuItem sources
    // ----------------------------------------------------------------
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

    @FXML public void goToDashboard(ActionEvent e) { NavigationUtil.navigateTo("dashboard.fxml",     resolveStage(e)); }
    @FXML public void goToVehicle(ActionEvent e)   { NavigationUtil.navigateTo("vehicle.fxml",       resolveStage(e)); }
    @FXML public void goToCustomer(ActionEvent e)  { NavigationUtil.navigateTo("customer.fxml",      resolveStage(e)); }
    @FXML public void goToWorkshop(ActionEvent e)  { NavigationUtil.navigateTo("workshop.fxml",      resolveStage(e)); }
    @FXML public void goToPolice(ActionEvent e)    { NavigationUtil.navigateTo("police.fxml",        resolveStage(e)); }
    @FXML public void goToViolation(ActionEvent e) { NavigationUtil.navigateTo("Violation.fxml",     resolveStage(e)); }
    @FXML public void goToAdmin(ActionEvent e)     { NavigationUtil.navigateTo("AdminView.fxml",     resolveStage(e)); }
}