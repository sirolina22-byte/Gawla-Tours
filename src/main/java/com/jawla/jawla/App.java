package com.jawla.jawla;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application implements NavigationHandler {

    private Stage primaryStage;
    
    // --- التعديل هنا ---
    // المتغيرات دي هي اللي هتشيل البيانات اللي جاية من الداتا بيس بعد اللوج إن
    // حالياً هي فيها قيم تجريبية، أول ما تربطي اللوج إن، القيم دي هي اللي هتتغير
    private String currentLoggedInUser = "Ahmed Mohamed"; // جربي تغيري الاسم هنا وشوفي النتيجة
    private String currentLoggedInRole = "Employee";      // جربي تغيريها لـ Admin وشوفي الزرار هيظهر ولا لا
    // ------------------

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        navigateTo("Home"); 
        
        stage.setTitle("Gawla Tours");
        stage.show();
    }

    @Override
    public void navigateTo(String pageName) {
        navigateTo(pageName, null);
    }

    @Override
    public void navigateTo(String pageName, Object data) {
        BasePage page = null;

        switch (pageName) {
            case "Home":
                page = new home();
                break;
            case "Bookings":
                page = new BookingList();
                break;
            case "CustomerBooking":
                page = new CustomerBooking();
                break;
            case "Settings":
                // التعديل السحري: بنبعت المتغيرات اللي فوق لصفحة السيتنج
                // كده صفحة السيتنج هتاخد "Ahmed Mohamed" و "Employee" وتعرضهم
                page = new Settings(currentLoggedInUser, currentLoggedInRole);
                break;
            case "Login":
                // navigateTo("Login") هترجعك هنا لما تعملي لوج أوت
                System.out.println("Redirecting to Login Page...");
                break;
        }

        if (page != null) {
            page.setNavigationHandler(this); 
            
            if (data != null) {
                page.setData(data);
                page.loadData();
            }

            Scene scene = new Scene(page, 1100, 750);
            primaryStage.setScene(scene);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}