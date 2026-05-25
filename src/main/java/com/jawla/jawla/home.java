package com.jawla.jawla;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * كلاس الهوم - يطبق الوراثة والـ Generics
 * تم تعديله ليرتبط بملف style.css حصرياً
 */
public class home extends BasePage<String> {

    public home() {
        // --- التعديل الجديد لربط ملف الـ CSS الأول ---
        this.getStylesheets().clear(); // مسح أي ملفات CSS عالقة من صفحات تانية
        this.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        // ------------------------------------------

        this.setAlignment(Pos.CENTER_LEFT);
        this.setPadding(new Insets(150, 50, 50, 100));
        
        // التأكد من مناداة الكلاس الصحيح من ملف الـ CSS
        this.getStyleClass().add("root-pane");

        VBox contentBox = new VBox(20); 
        contentBox.setAlignment(Pos.CENTER_LEFT);

        Label welcomeLabel = new Label("Welcome");
        welcomeLabel.getStyleClass().add("welcome-text");

        Label titleLabel = new Label("GAWLA Tours");
        titleLabel.getStyleClass().add("project-title");

        // زرار اللوج إن
        Button loginBtn = new Button("Login");
        loginBtn.getStyleClass().add("primary-button");
        loginBtn.setPrefWidth(350);
        
        loginBtn.setOnAction(e -> {
            login l = new login(); 
            this.getScene().setRoot(l);
        });

        Label signUpLabel = new Label("Don't have an account? Sign Up");
        signUpLabel.getStyleClass().add("signup-hint");

        // زرار الساين أب
        Button signUpBtn = new Button("Sign Up");
        signUpBtn.getStyleClass().add("secondary-button"); 
        signUpBtn.setPrefWidth(350);

        signUpBtn.setOnAction(e -> {
            signup s = new signup(); 
            this.getScene().setRoot(s);
        });

        contentBox.getChildren().addAll(welcomeLabel, titleLabel, loginBtn, signUpLabel, signUpBtn);
        this.getChildren().add(contentBox);
    }

    @Override
    public void loadData() {
        // ميثود الـ Polymorphism المطلوبة
        this.pageData = "Home Page Loaded";
    }
}