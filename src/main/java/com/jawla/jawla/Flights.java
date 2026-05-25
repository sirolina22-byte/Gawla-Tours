package com.jawla.jawla;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import java.time.format.DateTimeFormatter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;

public class Flights extends BasePage<Object> {

    private final ObservableList<FlightTrip> flightData = FXCollections.observableArrayList();
    private Label lblTotalTrips, lblTotalRevenue;
    private TextField txtFlightNum, txtPrice, txtOrigin, txtDest, txtDepTime, txtArrTime;
    private DatePicker dpDeparture, dpArriving;

    public Flights() {
        this.getStylesheets().add(getClass().getResource("/dashboardstyle.css").toExternalForm());
        
        VBox content = new VBox(25);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: #f8f9fa;");

        HBox statsArea = new HBox(20);
        lblTotalTrips = new Label("0");
        lblTotalRevenue = new Label("$0");
        statsArea.getChildren().addAll(
            createStatCard("Total Trips", lblTotalTrips, "#3b82f6"),
            createStatCard("Revenue Today", lblTotalRevenue, "#10b981")
        );

        HBox mainGrid = new HBox(20);
        VBox tableBox = new VBox(15);
        tableBox.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");
        HBox.setHgrow(tableBox, Priority.ALWAYS);
        
        Label tableTitle = new Label("Current Flight Schedule");
        tableTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        TableView<FlightTrip> table = new TableView<>(flightData);
        buildTable(table);
        tableBox.getChildren().addAll(tableTitle, table);

        VBox formBox = new VBox(12);
        formBox.setMinWidth(350);
        formBox.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 25; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");
        buildForm(formBox);

        mainGrid.getChildren().addAll(tableBox, formBox);
        content.getChildren().addAll(statsArea, mainGrid);
        this.getChildren().add(content);

        // تحميل البيانات من القاعدة فور فتح الصفحة
        loadDataFromDatabase();
    }

    private void buildForm(VBox container) {
        Label title = new Label("Add New Flight Trip");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a;");
        
        txtFlightNum = new TextField(); txtFlightNum.setPromptText("Flight Number (e.g. GT-101)");
        txtOrigin = new TextField(); txtOrigin.setPromptText("City");
        txtDest = new TextField(); txtDest.setPromptText("Destination");
        dpDeparture = new DatePicker(); dpDeparture.setPromptText("Select Date");
        txtDepTime = new TextField(); txtDepTime.setPromptText("14:00");
        dpArriving = new DatePicker(); dpArriving.setPromptText("Select Date");
        txtArrTime = new TextField(); txtArrTime.setPromptText("17:30");
        txtPrice = new TextField(); txtPrice.setPromptText("Price in USD");

        HBox actions = new HBox(10);
        Button btnCreate = new Button("Create Trip");
        btnCreate.setPrefHeight(40);
        btnCreate.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
        btnCreate.setMaxWidth(Double.MAX_VALUE); HBox.setHgrow(btnCreate, Priority.ALWAYS);
        
        Button btnCancel = new Button("Cancel");
        btnCancel.setPrefHeight(40);
        btnCancel.setStyle("-fx-background-color: transparent; -fx-border-color: #ddd; -fx-border-radius: 8; -fx-cursor: hand;");

        btnCreate.setOnAction(e -> handleCreate());
        btnCancel.setOnAction(e -> clearFields());

        container.getChildren().addAll(
            title, new Separator(),
            new Label("Flight Number"), txtFlightNum,
            new Label("Origin & Destination"), new HBox(5, txtOrigin, txtDest),
            new Label("Departure Date & Time"), new HBox(5, dpDeparture, txtDepTime),
            new Label("Arriving Date & Time"), new HBox(5, dpArriving, txtArrTime),
            new Label("Price per Seat"), txtPrice,
            new Region(), 
            actions
        );
        actions.getChildren().addAll(btnCreate, btnCancel);
    }

    private void buildTable(TableView<FlightTrip> table) {
        TableColumn<FlightTrip, String> colId = new TableColumn<>("Trip ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("flightNum"));
        TableColumn<FlightTrip, String> colOrig = new TableColumn<>("Origin");
        colOrig.setCellValueFactory(new PropertyValueFactory<>("origin"));
        TableColumn<FlightTrip, String> colDest = new TableColumn<>("Destination");
        colDest.setCellValueFactory(new PropertyValueFactory<>("destination"));
        TableColumn<FlightTrip, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        TableColumn<FlightTrip, String> colDepT = new TableColumn<>("Dep Time");
        colDepT.setCellValueFactory(new PropertyValueFactory<>("depTime"));
        TableColumn<FlightTrip, String> colArrT = new TableColumn<>("Arr Time");
        colArrT.setCellValueFactory(new PropertyValueFactory<>("arrTime"));
        TableColumn<FlightTrip, Double> colPrice = new TableColumn<>("Price");
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<FlightTrip, Void> colActions = new TableColumn<>("Action");
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnDel = new Button("🗑");
            { 
                btnDel.setStyle("-fx-text-fill: #ef4444; -fx-background-color: transparent; -fx-font-size: 16px; -fx-cursor: hand;");
                btnDel.setOnAction(e -> {
                    FlightTrip trip = getTableRow().getItem();
                    if (trip != null) {
                        deleteFlightFromDatabase(trip.getFlightNum());
                    }
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnDel);
            }
        });

        table.getColumns().addAll(colId, colOrig, colDest, colDate, colDepT, colArrT, colPrice, colActions);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    // الربط مع قاعدة البيانات عند الضغط على Create
    private void handleCreate() {
        try {
            String query = "INSERT INTO flights (flightNum, origin, destination, departureDate, depTime, arrivalDate, arrTime, price) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseHandler.getConnection(); // تأكدي من اسم الكلاس عندك
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                
                pstmt.setString(1, txtFlightNum.getText());
                pstmt.setString(2, txtOrigin.getText());
                pstmt.setString(3, txtDest.getText());
                pstmt.setDate(4, Date.valueOf(dpDeparture.getValue()));
                pstmt.setString(5, txtDepTime.getText());
                pstmt.setDate(6, Date.valueOf(dpArriving.getValue()));
                pstmt.setString(7, txtArrTime.getText());
                pstmt.setDouble(8, Double.parseDouble(txtPrice.getText()));
                
                pstmt.executeUpdate();
                loadDataFromDatabase(); // إعادة تحديث الجدول
                clearFields();
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Database Error: " + e.getMessage()).show();
        }
    }

    // تحميل البيانات من MySQL للجدول
    private void loadDataFromDatabase() {
        flightData.clear();
        String query = "SELECT * FROM flights";
        try (Connection conn = DatabaseHandler.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(query)) {
            while (rs.next()) {
                flightData.add(new FlightTrip(
                    rs.getString("flightNum"),
                    rs.getString("origin"),
                    rs.getString("destination"),
                    rs.getDate("departureDate").toString(),
                    rs.getString("depTime"),
                    rs.getString("arrTime"),
                    rs.getDouble("price")
                ));
            }
            updateStats();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // حذف رحلة من MySQL
    private void deleteFlightFromDatabase(String flightNum) {
        String query = "DELETE FROM flights WHERE flightNum = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, flightNum);
            pstmt.executeUpdate();
            loadDataFromDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearFields() {
        txtFlightNum.clear(); txtOrigin.clear(); txtDest.clear(); txtPrice.clear();
        txtDepTime.clear(); txtArrTime.clear();
        dpDeparture.setValue(null); dpArriving.setValue(null);
    }

    private void updateStats() {
        lblTotalTrips.setText(String.valueOf(flightData.size()));
        double sum = flightData.stream().mapToDouble(FlightTrip::getPrice).sum();
        lblTotalRevenue.setText("$" + String.format("%,.0f", sum));
    }

    private VBox createStatCard(String t, Label v, String accentColor) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(20));
        card.setPrefWidth(280);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");
        Label title = new Label(t);
        title.setStyle("-fx-text-fill: #64748b; -fx-font-size: 14px; -fx-font-weight: bold;");
        v.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + accentColor + ";");
        card.getChildren().addAll(title, v);
        return card;
    }

    public static class FlightTrip {
        private String flightNum, origin, destination, date, depTime, arrTime;
        private double price;
        public FlightTrip(String f, String o, String d, String dt, String dtp, String art, double p) {
            this.flightNum = f; this.origin = o; this.destination = d; this.date = dt; this.depTime = dtp; this.arrTime = art; this.price = p;
        }
        public String getFlightNum() { return flightNum; }
        public String getOrigin() { return origin; }
        public String getDestination() { return destination; }
        public String getDate() { return date; }
        public String getDepTime() { return depTime; }
        public String getArrTime() { return arrTime; }
        public double getPrice() { return price; }
    }

    @Override
    public void loadData() {
        loadDataFromDatabase();
    }
}