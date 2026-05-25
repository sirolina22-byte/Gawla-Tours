package com.jawla.jawla;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Employees extends BasePage<Void> {

    private final ObservableList<EmployeeModel> employeeList = FXCollections.observableArrayList();
    private Label lblTotalEmployees;
    private TableView<EmployeeModel> table;

    public Employees() {
        this.setPadding(new Insets(25));
        this.setSpacing(20);
        this.setStyle("-fx-background-color: #f0f2f5;");

        // --- Header ---
        VBox titleArea = new VBox(5);
        Label lblTitle = new Label("Employee Overview");
        lblTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #0d2c54;");
        Label lblSub = new Label("Manage employee registrations and staff counts");
        lblSub.setStyle("-fx-text-fill: #7f8c8d;");
        titleArea.getChildren().addAll(lblTitle, lblSub);

        // --- Table Container ---
        VBox tableContainer = new VBox(15);
        tableContainer.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20;");
        VBox.setVgrow(tableContainer, Priority.ALWAYS);

        Label tableTitle = new Label("Employees Information & Management 🔒");
        tableTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        table = new TableView<>(employeeList);
        setupColumns(table);

        // --- Footer ---
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(15));
        lblTotalEmployees = new Label("Total Employees: 0");
        lblTotalEmployees.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #0d2c54;");
        footer.getChildren().add(lblTotalEmployees);

        tableContainer.getChildren().addAll(tableTitle, new Separator(), table, footer);
        this.getChildren().addAll(titleArea, tableContainer);

        // استدعاء ميثود جلب البيانات الحقيقية
        loadDataFromDatabase();
    }

    private void setupColumns(TableView<EmployeeModel> table) {
        TableColumn<EmployeeModel, String> colName = new TableColumn<>("Name");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        TableColumn<EmployeeModel, String> colId = new TableColumn<>("ID / National ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        TableColumn<EmployeeModel, String> colAddress = new TableColumn<>("Address");
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        
        TableColumn<EmployeeModel, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        
        TableColumn<EmployeeModel, String> colPhone = new TableColumn<>("Phone Number");
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));

        // زرار الحذف (Delete) يمسح من الداتا بيز أيضاً
        TableColumn<EmployeeModel, Void> colDelete = new TableColumn<>("Delete");
        colDelete.setCellFactory(param -> new TableCell<>() {
            private final Button btnDel = new Button("Delete 🗑");
            {
                btnDel.setStyle("-fx-background-color: #fce4e4; -fx-text-fill: #d32f2f; -fx-border-color: #ffcdd2; -fx-cursor: hand;");
                btnDel.setOnAction(e -> {
                    EmployeeModel emp = getTableRow().getItem();
                    if (emp != null) {
                        deleteEmployeeFromDatabase(emp.getId());
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(btnDel);
                setAlignment(Pos.CENTER);
            }
        });

        table.getColumns().addAll(colName, colId, colAddress, colEmail, colPhone, colDelete);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    // --- ميثود جلب البيانات من الداتا بيز ---
    private void loadDataFromDatabase() {
        employeeList.clear(); // مسح القائمة الحالية
        
        runInBackgroundTask(() -> {
            String query = "SELECT name, nid, address, email, phone FROM users WHERE role = 'employee'";
            
            try (Connection conn = DatabaseHandler.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query);
                 ResultSet rs = pstmt.executeQuery()) {
                
                while (rs.next()) {
                    employeeList.add(new EmployeeModel(
                        rs.getString("name"),
                        rs.getString("nid"),
                        rs.getString("address"),
                        rs.getString("email"),
                        rs.getString("phone")
                    ));
                }
                
                updateUI(this::updateTotalCount);
                
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    // --- ميثود حذف الموظف من الداتا بيز ---
    private void deleteEmployeeFromDatabase(String nid) {
        runInBackgroundTask(() -> {
            String query = "DELETE FROM users WHERE nid = ?";
            try (Connection conn = DatabaseHandler.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                
                pstmt.setString(1, nid);
                pstmt.executeUpdate();
                
                // تحديث الجدول في الواجهة بعد الحذف
                updateUI(this::loadDataFromDatabase);
                
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void updateTotalCount() {
        lblTotalEmployees.setText("Total Employees: " + employeeList.size());
    }

    @Override
    public void loadData() {
        loadDataFromDatabase();
    }

    // Model Class
    public static class EmployeeModel {
        private final String name, id, address, email, phone;
        public EmployeeModel(String n, String i, String a, String e, String p) {
            this.name = n; this.id = i; this.address = a; this.email = e; this.phone = p;
        }
        public String getName() { return name; }
        public String getId() { return id; }
        public String getAddress() { return address; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
    }
}