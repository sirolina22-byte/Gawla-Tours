package com.jawla.jawla;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.sql.*;

public class BookingList extends BasePage<BookingList.Booking> {

    public static final ObservableList<Booking> allBookings = FXCollections.observableArrayList();
    private TableView<Booking> table;

    public BookingList() {
        this.setSpacing(20);
        this.setPadding(new Insets(25));
        this.setStyle("-fx-background-color: #f0f2f5;");

        Label lblTitle = new Label("Booking List");
        lblTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #0d2c54;");

        VBox tableBox = new VBox(10);
        tableBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 15;");
        
        table = new TableView<>(allBookings);
        setupColumns();
        
        tableBox.getChildren().addAll(new Label("All Records"), new Separator(), table);
        VBox.setVgrow(tableBox, Priority.ALWAYS);

        this.getChildren().addAll(lblTitle, tableBox);
        
        loadData();
    }

    private void setupColumns() {
        String[] titles = {"Booking ID", "Customer Name", "Flight No", "Origin", "Destination", "Date", "Total Amount"};
        String[] properties = {"id", "customerName", "flightNo", "origin", "destination", "date", "total"};

        for (int i = 0; i < titles.length; i++) {
            TableColumn<Booking, String> col = new TableColumn<>(titles[i]);
            col.setCellValueFactory(new PropertyValueFactory<>(properties[i]));
            table.getColumns().add(col);
        }

        TableColumn<Booking, Void> colActions = new TableColumn<>("Actions");
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnDel = new Button("Delete");
            private final HBox pane = new HBox(btnDel);
            {
                btnDel.setStyle("-fx-background-color: #d9534f; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 5 15;");
                pane.setAlignment(Pos.CENTER);

                btnDel.setOnAction(e -> {
                    Booking selected = getTableRow().getItem();
                    if (selected != null) {
                        deleteBookingAndCustomer(selected);
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
        
        table.getColumns().add(colActions);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    @Override 
    public void loadData() {
        allBookings.clear();
        String sql = "SELECT b.Booking_ID, c.Full_Name, b.flightNum, f.origin, f.destination, b.Booking_Date, b.Total_Amount " +
                     "FROM bookings b " +
                     "JOIN customers c ON b.Customer_ID = c.Customer_ID " +
                     "JOIN flights f ON b.flightNum = f.flightNum";

        try (Connection conn = DatabaseHandler.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                allBookings.add(new Booking(
                        rs.getString("Booking_ID"),
                        rs.getString("Full_Name"),
                        rs.getString("flightNum"),
                        rs.getString("origin"),
                        rs.getString("destination"),
                        rs.getString("Booking_Date"),
                        rs.getString("Total_Amount")
                ));
            }
            table.refresh();
        } catch (SQLException e) {
            System.err.println("Error loading bookings: " + e.getMessage());
        }
    }

    private void deleteBookingAndCustomer(Booking booking) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, 
            "Are you sure? This will delete the booking and the customer record permanently.", 
            ButtonType.YES, ButtonType.NO);
            
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try (Connection conn = DatabaseHandler.getConnection()) {
                    conn.setAutoCommit(false); // بدء المعاملة
                    
                    try {
                        // 1. الحصول على معرف العميل المرتبط بهذا الحجز
                        String customerId = "";
                        String findCust = "SELECT Customer_ID FROM bookings WHERE Booking_ID = ?";
                        try (PreparedStatement psFind = conn.prepareStatement(findCust)) {
                            psFind.setString(1, booking.getId());
                            ResultSet rs = psFind.executeQuery();
                            if (rs.next()) customerId = rs.getString("Customer_ID");
                        }

                        // 2. حذف الحجز أولاً (بسبب قيود مفتاح الربط)
                        String delBooking = "DELETE FROM bookings WHERE Booking_ID = ?";
                        try (PreparedStatement psDelB = conn.prepareStatement(delBooking)) {
                            psDelB.setString(1, booking.getId());
                            psDelB.executeUpdate();
                        }

                        // 3. حذف العميل ثانياً
                        if (customerId != null && !customerId.isEmpty()) {
                            String delCustomer = "DELETE FROM customers WHERE Customer_ID = ?";
                            try (PreparedStatement psDelC = conn.prepareStatement(delCustomer)) {
                                psDelC.setString(1, customerId);
                                psDelC.executeUpdate();
                            }
                        }

                        conn.commit(); // تثبيت الحذف للجدولين
                        allBookings.remove(booking);
                        new Alert(Alert.AlertType.INFORMATION, "All records deleted!").show();
                        
                    } catch (SQLException ex) {
                        conn.rollback(); // تراجع في حال حدوث أي خطأ
                        throw ex;
                    }
                } catch (SQLException e) {
                    new Alert(Alert.AlertType.ERROR, "Delete Failed: " + e.getMessage()).show();
                }
            }
        });
    }

    public static class Booking {
        private final String id, customerName, flightNo, origin, destination, date, total;
        public Booking(String id, String name, String f, String o, String d, String dt, String t) {
            this.id = id; this.customerName = name; this.flightNo = f; 
            this.origin = o; this.destination = d; this.date = dt; this.total = t;
        }
        public String getId() { return id; }
        public String getCustomerName() { return customerName; }
        public String getFlightNo() { return flightNo; }
        public String getOrigin() { return origin; }
        public String getDestination() { return destination; }
        public String getDate() { return date; }
        public String getTotal() { return total; }
    }
}
