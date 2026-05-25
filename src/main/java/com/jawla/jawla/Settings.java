package com.jawla.jawla;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Settings extends BasePage<Void> {

    private String currentUserName; 
    private String currentUserRole; 
    
    private Label lblBackupDate, lblTotalUsers, lblTotalBookings;
    private TextField compName, compEmail, compPhone, compAddress;
    private ComboBox<String> curr;
    private TextField uNameField;
    private Label rVal;

    public Settings(String userName, String role) {
        // استقبال البيانات الحقيقية من الـ Login
        this.currentUserName = (userName != null) ? userName : "Guest";
        this.currentUserRole = (role != null) ? role : "User";

        this.setPadding(new Insets(30));
        this.setSpacing(25);
        this.setStyle("-fx-background-color: #f0f4f8;"); 

        setupHeader();

        HBox cards = new HBox(20);
        cards.setAlignment(Pos.TOP_LEFT);

        VBox card1 = createCard("General Settings");
        setupGeneralSettings(card1);

        VBox card2 = createCard("System Information");
        setupSystemInfo(card2);

        VBox card3 = createCard("User Information");
        setupUserInfo(card3);

        cards.getChildren().addAll(card1, card2, card3);
        this.getChildren().addAll(cards);
        
        // استدعاء ميثود التحميل عشان نحدث الواجهة بالبيانات الصح
        loadData();
    }

    private void setupHeader() {
        HBox header = new HBox();
        VBox titleArea = new VBox(5);
        Label lblTitle = new Label("Settings");
        lblTitle.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #0a1931;");
        Label lblSub = new Label("Manage system preferences and configurations");
        lblSub.setStyle("-fx-text-fill: #64748b;");
        titleArea.getChildren().addAll(lblTitle, lblSub);
        
        Region headerSpacer = new Region(); HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        Label topUser = new Label("👤 " + currentUserName + " (" + currentUserRole + ")");
        topUser.setStyle("-fx-text-fill: #64748b; -fx-font-weight: bold;");
        header.getChildren().addAll(titleArea, headerSpacer, topUser);
        this.getChildren().add(header);
    }

    private void setupGeneralSettings(VBox card) {
        GridPane g1 = new GridPane(); g1.setVgap(10); g1.setHgap(10);
        
        compName = new TextField();
        compEmail = new TextField();
        compPhone = new TextField();
        compAddress = new TextField();
        curr = new ComboBox<>(); 
        // استخدام رموز العملة فقط لتجنب خطأ Data too long في الداتابيز
        curr.getItems().addAll("EGP", "USD", "EUR", "SAR");
        curr.setMaxWidth(Double.MAX_VALUE);

        Button btnSave = new Button("Save Changes");
        btnSave.setStyle("-fx-background-color: #3f72af; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnSave.setOnAction(e -> saveCompanySettings());

        if (!"Admin".equalsIgnoreCase(currentUserRole)) btnSave.setVisible(false);

        g1.addColumn(0, new Label("Company Name"), compName, new Label("Company Email"), compEmail, 
                     new Label("Company Phone"), compPhone, new Label("Company Address"), compAddress,
                     new Label("Currency"), curr, new Label(""), btnSave);
        card.getChildren().add(g1);
    }

    private void setupSystemInfo(VBox card) {
        GridPane g2 = new GridPane(); g2.setVgap(15); g2.setHgap(40);
        lblBackupDate = new Label("-"); 
        lblTotalUsers = new Label("0");
        lblTotalBookings = new Label("0");

        g2.add(new Label("System Version"), 0, 0); g2.add(new Label("v1.0.8"), 1, 0);
        g2.add(new Label("Database"), 0, 1); g2.add(new Label("MySQL 8.0"), 1, 1);
        g2.add(new Label("Last Backup"), 0, 2); g2.add(lblBackupDate, 1, 2);
        g2.add(new Label("Total Customers"), 0, 3); g2.add(lblTotalUsers, 1, 3);
        g2.add(new Label("Total Bookings"), 0, 4); g2.add(lblTotalBookings, 1, 4);
        
        Button btnBackup = new Button("Backup Now");
        btnBackup.setStyle("-fx-background-color: #3f72af; -fx-text-fill: white; -fx-cursor: hand;");
        btnBackup.setOnAction(e -> handleBackup());
        
        card.getChildren().addAll(g2, btnBackup);
    }

    private void setupUserInfo(VBox card) {
        // تم تصفير أي نص افتراضي عشان "ريم" ما تظهرش
        uNameField = new TextField(""); 
        uNameField.setEditable(false);
        uNameField.setStyle("-fx-opacity: 1.0; -fx-text-fill: #333; -fx-background-color: #eee;");

        HBox roleBox = new HBox(10);
        roleBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 8; -fx-border-color: #eee; -fx-border-radius: 5;");
        rVal = new Label(""); 
        
        Label activeTag = new Label("Active"); 
        activeTag.setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; -fx-padding: 2 8;");
        Region rSpacer = new Region(); HBox.setHgrow(rSpacer, Priority.ALWAYS);
        roleBox.getChildren().addAll(rVal, rSpacer, activeTag);
        card.getChildren().addAll(new Label("User Name"), uNameField, new Label("Role"), roleBox);
    }

    @Override 
    public void loadData() {
        // تحديث حقول اليوزر فوراً بالقيم الحقيقية لمسح "ريم" للأبد
        uNameField.setText(currentUserName);
        rVal.setText(currentUserRole);

        try (Connection conn = DatabaseHandler.getConnection()) {
            // تحميل بيانات الشركة
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM company LIMIT 1");
            if (rs.next()) {
                compName.setText(rs.getString("company_name"));
                compEmail.setText(rs.getString("company_email"));
                compPhone.setText(rs.getString("company_phone"));
                compAddress.setText(rs.getString("company_address"));
                curr.setValue(rs.getString("currency"));
                lblBackupDate.setText(rs.getString("last_backup"));
            }

            // إحصائيات سريعة
            ResultSet rsCust = conn.createStatement().executeQuery("SELECT COUNT(*) FROM customers");
            if (rsCust.next()) lblTotalUsers.setText(String.valueOf(rsCust.getInt(1)));

            ResultSet rsBook = conn.createStatement().executeQuery("SELECT COUNT(*) FROM bookings");
            if (rsBook.next()) lblTotalBookings.setText(String.valueOf(rsBook.getInt(1)));

        } catch (SQLException e) { 
            System.out.println("Load Data Error: " + e.getMessage());
        }
    }

    private void saveCompanySettings() {
        try (Connection conn = DatabaseHandler.getConnection()) {
            ResultSet rsCheck = conn.createStatement().executeQuery("SELECT COUNT(*) FROM company");
            rsCheck.next();
            boolean exists = rsCheck.getInt(1) > 0;

            String sql = exists ? 
                "UPDATE company SET company_name=?, company_email=?, company_phone=?, company_address=?, currency=? WHERE id=1" :
                "INSERT INTO company (company_name, company_email, company_phone, company_address, currency, id) VALUES (?,?,?,?,?,1)";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, compName.getText());
                ps.setString(2, compEmail.getText());
                ps.setString(3, compPhone.getText());
                ps.setString(4, compAddress.getText());
                ps.setString(5, curr.getValue()); 
                ps.executeUpdate();
                new Alert(Alert.AlertType.INFORMATION, "Settings updated successfully!").show();
            }
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Database Error: " + e.getMessage()).show();
        }
    }

    private void handleBackup() {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a"));
        try (Connection conn = DatabaseHandler.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("UPDATE company SET last_backup=? WHERE id=1");
            ps.setString(1, now);
            ps.executeUpdate();
            lblBackupDate.setText(now);
            new Alert(Alert.AlertType.INFORMATION, "Backup completed!").show();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private VBox createCard(String title) {
        VBox card = new VBox(15);
        card.setPrefWidth(350);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        card.setEffect(new DropShadow(15, Color.rgb(0,0,0,0.08)));
        Label t = new Label(title); t.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #0a1931;");
        card.getChildren().add(t);
        return card;
    }
}