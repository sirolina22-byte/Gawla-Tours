package com.jawla.jawla;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import java.sql.*;
import com.almasb.fxgl.dsl.FXGL; 
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;

public class dashboard extends BasePage<User> {

    private BorderPane mainLayout;
    private VBox sideBar;
    private StackPane contentArea; 
    private boolean isMenuVisible = true;
    private List<Button> navButtons = new ArrayList<>();
    private VBox bookingsListContainer;
    private Label dateTimeLabel; 
    private String role; // أضفت المتغير هنا لاستخدامه في الصلاحيات

    private String userName ; 
    private String totalCustomers = "1,248";
    private String totalFlights = "86";
    private String totalBookings = "732";
    private String totalRevenue = "$128,560";

    public dashboard(String userName ,String role) {
        this.userName=userName;
        this.role = role; // حفظ الرول عند فتح الصفحة
        this.getStylesheets().clear();
        this.getStylesheets().add(getClass().getResource("/dashboardstyle.css").toExternalForm());
        loadData();
        mainLayout = new BorderPane();
        mainLayout.getStyleClass().add("dashboard-root");

        sideBar = createSideBar();
        mainLayout.setLeft(sideBar);

        VBox rightSide = new VBox();
        contentArea = new StackPane();
        VBox.setVgrow(contentArea, Priority.ALWAYS);
        
        rightSide.getChildren().addAll(createTopBar(), contentArea);
        setPage(createDashboardHome()); 
        
        mainLayout.setCenter(rightSide);
        this.getChildren().add(mainLayout);
        
        startClock(); 
        loadData();
    }

    private VBox createSideBar() {
        VBox sb = new VBox(10);
        sb.getStyleClass().add("sidebar");
        sb.setPrefSize(260, Double.MAX_VALUE); 
        sb.setPadding(new Insets(20));

        Label title = new Label("GAWLA TOURS");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");

        // الزراير اللي بتظهر للكل (موظف وأدمن)
        Button dBtn = createNavButton("🏠 Dashboard", "dash.png", true);
        Button fBtn = createNavButton("✈️ Flights", "flight.png", false);
        Button cBtn = createNavButton("🎫 Customers Booking", "cust.png", false);
        Button lBtn = createNavButton("📋 Booking List", "list.png", false);
        
       // الزرار بتاع الداش بورد
dBtn.setOnAction(e -> { 
    // 1. تحديث الأرقام (الكروت) من الداتابيز
    loadData(); 
    
    // 2. إعادة بناء الصفحة بالكامل (بما فيها الرسم البياني الجديد)
    setPage(createDashboardHome()); 
    
    setActiveButton(dBtn); 
});
        fBtn.setOnAction(e -> { setPage(new Flights()); setActiveButton(fBtn); });
        cBtn.setOnAction(e -> { setPage(new CustomerBooking()); setActiveButton(cBtn); });
        lBtn.setOnAction(e -> { setPage(new BookingList()); setActiveButton(lBtn); });

        sb.getChildren().addAll(title, new Separator(), dBtn, fBtn, cBtn, lBtn);

        // --- التعديل: صفحة الموظفين تظهر للأدمن فقط ---
        if ("admin".equalsIgnoreCase(this.role)) {
            Button eBtn = createNavButton("👥 Employees", "emp.png", false);
            eBtn.setOnAction(e -> { setPage(new Employees()); setActiveButton(eBtn); });
            sb.getChildren().add(eBtn);
        }

        // زرار السيتنج يظهر للكل في نهاية القائمة
        Button sBtn = createNavButton("⚙️Settings", "settings.png", false);
        sBtn.setOnAction(e -> { setPage(new Settings(this.userName, this.role)); setActiveButton(sBtn); });
        sb.getChildren().add(sBtn);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox userProfileBox = new VBox(8);
        userProfileBox.setAlignment(Pos.CENTER);
        try {
            ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/icons/company_logo.png")));
            logo.setFitWidth(110); logo.setPreserveRatio(true);
            ImageView userIcon = new ImageView(new Image(getClass().getResourceAsStream("/icons/user_profile.png")));
            userIcon.setFitWidth(40); userIcon.setFitHeight(40);
            Label nameLbl = new Label("👤 Hi, " + userName);
            nameLbl.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
            userProfileBox.getChildren().addAll(logo, new Separator(), userIcon, nameLbl);
        } catch(Exception e) {}

        sb.getChildren().addAll(spacer, userProfileBox);
        return sb;
    }

    private HBox createTopBar() {
        HBox tb = new HBox(15);
        tb.setPadding(new Insets(10)); 
        tb.setAlignment(Pos.CENTER_LEFT);
        tb.setStyle("-fx-background-color: white; -fx-border-color: #eee; -fx-border-width: 0 0 1 0;");
        
        Button menuBtn = new Button("☰");
        menuBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 20px; -fx-cursor: hand;");
        menuBtn.setOnAction(e -> toggleMenu());

        Region s = new Region(); HBox.setHgrow(s, Priority.ALWAYS);

        dateTimeLabel = new Label();
        dateTimeLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 13px; -fx-font-weight: bold;");

        Button logout = new Button("🚪 Logout");
        logout.getStyleClass().add("logout-button");
        logout.setOnAction(e -> getScene().setRoot(new login()));

        tb.getChildren().addAll(menuBtn, s, dateTimeLabel, logout);
        return tb;
    }

    private void startClock() {
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd MMM | HH:mm:ss");
            dateTimeLabel.setText(LocalDateTime.now().format(formatter));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    private ScrollPane createDashboardHome() {
        VBox body = new VBox(25);
        body.setPadding(new Insets(25));
        body.setStyle("-fx-background-color: #f8f9fa;");

        Label welcome = new Label("Welcome back, " + userName + "! 👋");
        welcome.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        GridPane cards = new GridPane();
        cards.setHgap(20);
        cards.add(createStatCard("Total Customers", totalCustomers, "👥", "+18 month ↑", "#10b981"), 0, 0);
        cards.add(createStatCard("Total Flights", totalFlights, "✈️", "+7 month ↑", "#3b82f6"), 1, 0);
        cards.add(createStatCard("Total Bookings", totalBookings, "🎫", "+23 month ↑", "#8b5cf6"), 2, 0);
        cards.add(createStatCard("Total Revenue", totalRevenue, "💰", "+12% month ↑", "#f59e0b"), 3, 0);

        HBox banner = createBanner();

        HBox bottomRow = new HBox(20);
        VBox chartBox = new VBox(15);
        chartBox.setPrefWidth(400); 
        chartBox.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");
        Label chartTitle = new Label("Revenue Analytics (FXGL) 📊");
        chartTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        
        Pane fxglChart = createFXGLChart(); 
        chartBox.getChildren().addAll(chartTitle, fxglChart);

        VBox recentBox = new VBox(15);
        recentBox.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");
        HBox.setHgrow(recentBox, Priority.ALWAYS);
        Label rbTitle = new Label("Recent Bookings 📋");
        rbTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
        bookingsListContainer = new VBox(10);
        addSampleBookings();
        recentBox.getChildren().addAll(rbTitle, bookingsListContainer);

        bottomRow.getChildren().addAll(chartBox, recentBox);
        body.getChildren().addAll(welcome, cards, banner, bottomRow);

        ScrollPane sp = new ScrollPane(body);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: #f8f9fa; -fx-border-color: transparent;");
        return sp;
    }

    private VBox createStatCard(String title, String val, String icon, String trend, String color) {
        VBox card = new VBox(10);
        card.setPrefWidth(240);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-padding: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");
        
        HBox header = new HBox();
        Label iconLabel = new Label(icon); iconLabel.setStyle("-fx-font-size: 20px;");
        Region s = new Region(); HBox.setHgrow(s, Priority.ALWAYS);
        header.getChildren().addAll(new Label(title), s, iconLabel);

        Label vLabel = new Label(val); vLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        Label trLabel = new Label(trend); trLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 11px; -fx-font-weight: bold;");
        
        card.getChildren().addAll(header, vLabel, trLabel);
        return card;
    }

   private Pane createFXGLChart() {
    // 1. إعداد المحاور
    CategoryAxis xAxis = new CategoryAxis();
    NumberAxis yAxis = new NumberAxis();
    xAxis.setLabel("Day");
    yAxis.setLabel("Revenue ($)");

    // 2. إنشاء الرسم البياني
    LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
    lineChart.setTitle("Daily Revenue Analytics 📈");
    lineChart.setLegendVisible(false);
    lineChart.setAnimated(true); // تفعيل الأنيميشن عند التحديث

    // 3. إضافة البيانات
    XYChart.Series<String, Number> series = new XYChart.Series<>();
    
    // الاستعلام هنا بيجمع الأرباح حسب اليوم (Day/Month)
    String query = "SELECT DATE_FORMAT(Booking_Date, '%d/%m') as day, SUM(Total_Amount) as total " +
                   "FROM bookings " +
                   "GROUP BY Booking_Date " +
                   "ORDER BY Booking_Date ASC " +
                   "LIMIT 10"; // عشان الرسمة متكونش زحمة، بنعرض آخر 10 أيام فيها حجوزات

    try (Connection conn = DatabaseHandler.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(query)) {
        
        boolean hasData = false;
        while (rs.next()) {
            series.getData().add(new XYChart.Data<>(rs.getString("day"), rs.getDouble("total")));
            hasData = true;
        }
        
        if (!hasData) {
            series.getData().add(new XYChart.Data<>("No Data", 0));
        }
        
    } catch (Exception e) {
        series.getData().add(new XYChart.Data<>("Sample", 100));
        System.out.println("Daily Chart Error: " + e.getMessage());
    }

    lineChart.getData().add(series);
    lineChart.setPrefHeight(250);

    StackPane chartContainer = new StackPane(lineChart);
    return chartContainer;
}

    private HBox createBanner() {
        HBox b = new HBox();
        b.setPrefHeight(180);
        b.setStyle("-fx-background-color: linear-gradient(to right, #1e3a8a, #3b82f6); -fx-background-radius: 15;");
        VBox t = new VBox(15); t.setPadding(new Insets(30));
        Label l1 = new Label("Fly Beyond Limits ✈️"); l1.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");
        t.getChildren().add(l1);
        Region s = new Region(); HBox.setHgrow(s, Priority.ALWAYS);
        try {
            ImageView iv = new ImageView(new Image(getClass().getResourceAsStream("/icons/airplane_banner.png")));
            iv.setFitHeight(160); iv.setPreserveRatio(true);
            b.getChildren().addAll(t, s, iv);
        } catch(Exception e) { b.getChildren().add(t); }
        return b;
    }

    private Button createNavButton(String text, String iconName, boolean isActive) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.getStyleClass().add("nav-button");
        if (isActive) btn.getStyleClass().add("nav-button-active");
        
        try {
            ImageView iv = new ImageView(new Image(getClass().getResourceAsStream("/icons/" + iconName)));
            iv.setFitWidth(18); iv.setFitHeight(18);
            btn.setGraphic(iv);
        } catch(Exception e) { }
        
        navButtons.add(btn);
        return btn;
    }

    private void toggleMenu() {
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), sideBar);
        if (isMenuVisible) {
            transition.setToX(-260);
            transition.setOnFinished(e -> mainLayout.setLeft(null));
            isMenuVisible = false;
        } else {
            mainLayout.setLeft(sideBar);
            sideBar.setTranslateX(-260);
            transition.setToX(0);
            isMenuVisible = true;
        }
        transition.play();
    }

    private void setPage(Node page) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(page);
    }

    private void setActiveButton(Button b) {
        for(Button btn : navButtons) btn.getStyleClass().remove("nav-button-active");
        b.getStyleClass().add("nav-button-active");
    }

    private void addSampleBookings() {
    bookingsListContainer.getChildren().clear();
    // استعلام بيجيب اسم العميل من جدول والوجهة من جدول تاني والمبلغ من جدول تالت
    String query = "SELECT c.Full_Name, f.destination, b.Total_Amount " +
                   "FROM bookings b " +
                   "JOIN customers c ON b.Customer_ID = c.Customer_ID " +
                   "JOIN flights f ON b.flightNum = f.flightNum " +
                   "ORDER BY b.Booking_ID DESC LIMIT 3";
    
    try (Connection conn = DatabaseHandler.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(query)) {
        
        while (rs.next()) {
            bookingsListContainer.getChildren().add(
                createBookingRow(rs.getString(1), rs.getString(2), "$" + rs.getString(3))
            );
        }
    } catch (Exception e) {
        bookingsListContainer.getChildren().add(new Label("No bookings found in database."));
    }
}

    private HBox createBookingRow(String name, String trip, String price) {
        HBox row = new HBox(30, new Label(name), new Label(trip), new Label(price));
        row.setPadding(new Insets(10));
        row.setStyle("-fx-border-color: #eee; -fx-border-width: 0 0 1 0;");
        return row;
    }

    @Override 

public void loadData() {
    try (Connection conn = DatabaseHandler.getConnection()) {
        // تحديث عدد العملاء
        ResultSet rs1 = conn.createStatement().executeQuery("SELECT COUNT(*) FROM customers");
        if (rs1.next()) this.totalCustomers = String.valueOf(rs1.getInt(1)); // تحديث المتغير الأصلي

        // تحديث عدد الرحلات
        ResultSet rs2 = conn.createStatement().executeQuery("SELECT COUNT(*) FROM flights");
        if (rs2.next()) this.totalFlights = String.valueOf(rs2.getInt(1));

        // تحديث عدد الحجوزات
        ResultSet rs3 = conn.createStatement().executeQuery("SELECT COUNT(*) FROM bookings");
        if (rs3.next()) this.totalBookings = String.valueOf(rs3.getInt(1));

        // تحديث إجمالي الأرباح
        ResultSet rs4 = conn.createStatement().executeQuery("SELECT SUM(Total_Amount) FROM bookings");
        if (rs4.next()) {
            double sum = rs4.getDouble(1);
            this.totalRevenue = "$" + String.format("%,.0f", sum);
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }
}
}