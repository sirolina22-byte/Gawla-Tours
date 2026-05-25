package com.jawla.jawla;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * كلاس اللوج إن - تم إضافة منطق الـ Role لبعته للداش بورد
 */
public class login extends BasePage<User> {

    private TextField userField;
    private PasswordField passField;
    private ToggleButton empBtn;
    private ToggleButton adminBtn;

    public login() {
        this.getStylesheets().clear(); 
        this.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        this.setAlignment(Pos.CENTER);
        this.getStyleClass().add("root-pane"); 

        VBox loginCard = new VBox(15);
        loginCard.setAlignment(Pos.CENTER);
        loginCard.setMaxWidth(400);
        loginCard.setPadding(new Insets(30, 40, 30, 40));
        loginCard.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9); " +
                           "-fx-background-radius: 20; " +
                           "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);");

        Label brandNameAr = new Label("جولة تورز");
        brandNameAr.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #003366;");
        Label brandNameEn = new Label("Gawla Tours");
        brandNameEn.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #003366;");

        ToggleGroup group = new ToggleGroup();
        empBtn = new ToggleButton("EMPLOYEES");
        empBtn.setToggleGroup(group);
        empBtn.setSelected(true);
        empBtn.setPrefWidth(150);

        adminBtn = new ToggleButton("ADMIN");
        adminBtn.setToggleGroup(group);
        adminBtn.setPrefWidth(150);
        
        applyToggleStyles(); 

        userField = new TextField();
        userField.setPromptText("Enter your username");
        userField.setPrefHeight(40);
        userField.setStyle("-fx-background-radius: 5; -fx-border-color: #DDE6ED;");

        passField = new PasswordField();
        passField.setPromptText("Pass");
        passField.setPrefHeight(40);
        passField.setStyle("-fx-background-radius: 5; -fx-border-color: #DDE6ED;");

        Button loginBtn = new Button("تسجيل الدخول / LOGIN");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setPrefHeight(45);
        loginBtn.setStyle("-fx-background-color: #0056b3; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        
        // الأكشن اللي بينفذ عملية الدخول
        loginBtn.setOnAction(e -> handleLoginAction());

        HBox statusBox = new HBox(5, new Circle(4, Color.GREEN), new Label("Server Connected | 08 May 2026"));
        statusBox.setAlignment(Pos.CENTER);

        HBox toggleBox = new HBox(empBtn, adminBtn);
        toggleBox.setAlignment(Pos.CENTER);

        loginCard.getChildren().addAll(brandNameAr, brandNameEn, toggleBox, userField, passField, loginBtn, statusBox);
        this.getChildren().add(loginCard);
    }

  private void handleLoginAction() {
    String userInput = userField.getText().trim(); // هنا المستخدم ممكن يكتب اسمه أو إيميله
    String pass = passField.getText();
    String selectedRole = adminBtn.isSelected() ? "admin" : "employee";

    runInBackgroundTask(() -> {
        // التعديل السحري: بنقول للداتابيز ابحثي بالاسم أول بالإيميل، مع مطابقة الباسورد والرتبة
        String query = "SELECT name, role FROM users WHERE (email = ? OR name = ?) AND pass = ? AND role = ?";
        
        try (var conn = DatabaseHandler.getConnection();
             var pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, userInput); // هيبحث لو كان إيميل
            pstmt.setString(2, userInput); // أو لو كان الاسم بالكامل
            pstmt.setString(3, pass);
            pstmt.setString(4, selectedRole);
            
            var rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String dbName = rs.getString("name"); 
                String dbRole = rs.getString("role");

                updateUI(() -> {
                    // فتح الداش بورد وإرسال الرتبة لتفعيل تابات الـ Admin
                    dashboard dash = new dashboard(dbName, dbRole);
                    this.getScene().setRoot(dash);
                    System.out.println("تم تسجيل الدخول بنجاح! مرحباً: " + dbName);
                });
            } else {
                updateUI(() -> {
                    System.out.println("خطأ: البيانات غير صحيحة، أو الرتبة غير متطابقة!");
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    });
}

    private void applyToggleStyles() {
        String sel = "-fx-background-color: #0056b3; -fx-text-fill: white;";
        String unsel = "-fx-background-color: #DDE6ED; -fx-text-fill: #526D82;";
        
        empBtn.setStyle(sel);
        adminBtn.setStyle(unsel);

        empBtn.setOnAction(e -> { empBtn.setStyle(sel); adminBtn.setStyle(unsel); });
        adminBtn.setOnAction(e -> { adminBtn.setStyle(sel); empBtn.setStyle(unsel); });
    }

    @Override
    public void loadData() {
        // تطبيق الـ Polymorphism المطلوب
    }
}