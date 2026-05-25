package com.jawla.jawla;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class signup extends BasePage<User> {

    private TextField nameField, emailField, addressField, nationalIdField, phoneField;
    private PasswordField passField, confirmPassField;
    private ToggleButton empBtn, adminBtn;

    public signup() {
        this.getStylesheets().clear(); 
        this.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        this.setAlignment(Pos.CENTER);
        this.getStyleClass().add("root-pane");

        VBox signupCard = new VBox(15);
        signupCard.setAlignment(Pos.CENTER);
        signupCard.setMaxWidth(480);
        signupCard.setPadding(new Insets(25, 45, 25, 45));
        signupCard.setStyle("-fx-background-color: rgba(255, 255, 255, 0.95); " +
                           "-fx-background-radius: 25; " +
                           "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 15, 0, 0, 0);");

        VBox brandHeader = new VBox(2);
        brandHeader.setAlignment(Pos.CENTER);
        Label arTitle = new Label("جولة تورز");
        arTitle.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #003366;");
        Label enTitle = new Label("Gawla Tours");
        enTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #003366;");
        Label subTitle = new Label("Sign-Up");
        subTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #555;");
        brandHeader.getChildren().addAll(arTitle, enTitle, subTitle);

        ToggleGroup group = new ToggleGroup();
        empBtn = new ToggleButton("EMPLOYEES");
        empBtn.setToggleGroup(group);
        empBtn.setSelected(true);
        empBtn.setPrefWidth(180);
        adminBtn = new ToggleButton("ADMIN");
        adminBtn.setToggleGroup(group);
        adminBtn.setPrefWidth(180);
        applyToggleStyles();

        HBox toggleBox = new HBox(empBtn, adminBtn);
        toggleBox.setAlignment(Pos.CENTER);
        toggleBox.setPadding(new Insets(10, 0, 15, 0));

        nameField = createField("Full Name: Enter your full name");
        emailField = createField("Email Address: Enter your email address");
        addressField = createField("Home Address: Enter your home address");
        nationalIdField = createField("National ID: Enter your 14 digits");
        phoneField = createField("Phone Number: Enter your phone number");
        passField = new PasswordField(); 
        passField.setPromptText("Password: Enter your password");
        styleField(passField);
        confirmPassField = new PasswordField(); 
        confirmPassField.setPromptText("Confirm Password: Confirm your password");
        styleField(confirmPassField);

        Button signupBtn = new Button("تسجيل جديد / SIGN-UP");
        signupBtn.setMaxWidth(Double.MAX_VALUE);
        signupBtn.setPrefHeight(45);
        signupBtn.setStyle("-fx-background-color: #0056b3; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        
        signupBtn.setOnAction(e -> handleSignupAction());

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
        HBox statusBox = new HBox(8, new Circle(5, Color.web("#28a745")), new Label("Server Connected | " + today));
        statusBox.setAlignment(Pos.CENTER);
        statusBox.setStyle("-fx-font-size: 11px; -fx-text-fill: #777;");

        Hyperlink loginLink = new Hyperlink("Already have an account? Login here");
        loginLink.setOnAction(e -> this.getScene().setRoot(new login()));

        signupCard.getChildren().addAll(brandHeader, toggleBox, nameField, emailField, addressField, 
                                       nationalIdField, phoneField, passField, confirmPassField, 
                                       signupBtn, statusBox, loginLink);
        this.getChildren().add(signupCard);
    }

    private void handleSignupAction() {
    // 1. سحب البيانات من الحقول
    String name = nameField.getText();
    String email = emailField.getText();
    String address = addressField.getText();
    String nid = nationalIdField.getText();
    String phone = phoneField.getText();
    String pass = passField.getText();
    String role = adminBtn.isSelected() ? "admin" : "employee";

    // 2. التحقق من البيانات الأساسية
    if(name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
        System.out.println("الرجاء ملء جميع الحقول المطلوبة!");
        return;
    }

    runInBackgroundTask(() -> {
        // 3. الاستعلام الخاص بالإضافة
        String query = "INSERT INTO users (name, email, address, nid, phone, pass, role) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (var conn = DatabaseHandler.getConnection();
             var pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, address);
            pstmt.setString(4, nid);
            pstmt.setString(5, phone);
            pstmt.setString(6, pass);
            pstmt.setString(7, role);
            
            pstmt.executeUpdate();

            updateUI(() -> {
                // الانتقال لصفحة الـ Login بعد التسجيل بنجاح
                this.getScene().setRoot(new login());
                System.out.println("تم إنشاء الحساب بنجاح!");
            });
        } catch (Exception ex) {
            ex.printStackTrace(); // لمشاهدة أي خطأ في الـ Output
        }
    });
}

    private TextField createField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        styleField(tf);
        return tf;
    }

    private void styleField(Control c) {
        c.setPrefHeight(38);
        c.setStyle("-fx-background-radius: 8; -fx-border-color: #ddd; -fx-background-color: #fff;");
    }

    private void applyToggleStyles() {
        String active = "-fx-background-color: #0056b3; -fx-text-fill: white;";
        String inactive = "-fx-background-color: #f0f0f0; -fx-text-fill: #333;";
        empBtn.setStyle(active); adminBtn.setStyle(inactive);
        empBtn.setOnAction(e -> { empBtn.setStyle(active); adminBtn.setStyle(inactive); });
        adminBtn.setOnAction(e -> { adminBtn.setStyle(active); empBtn.setStyle(inactive); });
    }

    @Override
    public void loadData() {
        this.pageData = new User();
    }
}