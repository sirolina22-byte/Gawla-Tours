package com.jawla.jawla;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.time.LocalDate;

public class CustomerBooking extends BasePage<BookingList.Booking> {

    private ImageView passportImageView = new ImageView();
    private Image selectedImage;
    private File selectedFile; 
    
    private TextField txtCustId = new TextField();
    private TextField txtFullName = new TextField();
    private TextField txtPassport = new TextField();
    private TextField txtEmail = new TextField();
    private TextField txtOtherNationality = new TextField();
    private ChoiceBox<String> nationality = new ChoiceBox<>();
    private DatePicker birthDate = new DatePicker();

    private TextField txtBookingId = new TextField();
    private TextField txtAmount = new TextField();
    private ChoiceBox<String> flightSelection = new ChoiceBox<>();
    private DatePicker bookingDate = new DatePicker();

    public CustomerBooking() {
        ScrollPane scrollPane = new ScrollPane();
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: #f4f7f9;");
        
        Label header = new Label("✈ Add New Customer & Booking");
        header.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        VBox customerCard = createStyledCard();
        HBox infoLayout = new HBox(30);
        GridPane customerGrid = new GridPane();
        customerGrid.setHgap(15); customerGrid.setVgap(12);
        setupCustomerFields(customerGrid);
        
        VBox imageBox = setupPassportSection(); 
        
        infoLayout.getChildren().addAll(customerGrid, imageBox);
        customerCard.getChildren().add(infoLayout);

        VBox bookingCard = createStyledCard();
        Label bookingTitle = new Label("✈ Travel Bookings");
        bookingTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        GridPane bookingGrid = new GridPane();
        bookingGrid.setHgap(15); bookingGrid.setVgap(12);
        setupBookingFields(bookingGrid);
        bookingCard.getChildren().addAll(bookingTitle, new Separator(), bookingGrid);

        HBox footer = new HBox(15);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(10, 0, 20, 0));

        Button btnSave = new Button("Save Everything");
        btnSave.setStyle("-fx-background-color: #0d2c54; -fx-text-fill: white; -fx-padding: 10 30; -fx-font-weight: bold; -fx-cursor: hand;");
        
        Button btnCancel = new Button("Cancel / Clear");
        btnCancel.setStyle("-fx-padding: 10 25; -fx-cursor: hand;");

        btnCancel.setOnAction(e -> clearFields()); 
        btnSave.setOnAction(e -> validateAndSave());

        footer.getChildren().addAll(btnCancel, btnSave);

        mainContainer.getChildren().addAll(header, customerCard, bookingCard, footer);
        scrollPane.setContent(mainContainer);
        scrollPane.setFitToWidth(true);
        this.getChildren().add(scrollPane);

        loadFlightsFromDatabase();
    }

    private VBox setupPassportSection() {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        passportImageView.setFitWidth(220); 
        passportImageView.setFitHeight(130);
        passportImageView.setStyle("-fx-border-color: #ddd; -fx-border-style: dashed; -fx-border-width: 2;");
        
        Button uploadBtn = new Button("📤 Upload Image");
        uploadBtn.setPrefWidth(160);
        
        Button removeBtn = new Button("🗑 Remove Image");
        removeBtn.setPrefWidth(160);
        removeBtn.setStyle("-fx-text-fill: #ef4444; -fx-background-color: white; -fx-border-color: #ef4444; -fx-border-radius: 5; -fx-cursor: hand;");
        
        uploadBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
            selectedFile = fc.showOpenDialog(null);
            if (selectedFile != null) {
                selectedImage = new Image(selectedFile.toURI().toString());
                passportImageView.setImage(selectedImage);
            }
        });

        removeBtn.setOnAction(e -> {
            passportImageView.setImage(null);
            selectedImage = null;
            selectedFile = null;
        });

        box.getChildren().addAll(new Label("Passport Image*"), passportImageView, uploadBtn, removeBtn);
        return box;
    }

    private void validateAndSave() {
        // التأكد من ملء الحقول الأساسية لضمان نجاح الربط
        if (txtCustId.getText().isEmpty() || txtBookingId.getText().isEmpty() || flightSelection.getValue() == null) {
            new Alert(Alert.AlertType.WARNING, "Required fields are missing!").show();
            return;
        }

        try (Connection conn = DatabaseHandler.getConnection()) {
            conn.setAutoCommit(false); // بدء معاملة واحدة لضمان حفظ الجدولين معاً
            
            try {
                // 1. حفظ العميل في جدول customers
                String sqlCust = "INSERT INTO customers (Customer_ID, Full_Name, Passport_Number, Date_of_Birth, Nationality, email, Passport_Image) VALUES (?,?,?,?,?,?,?)";
                try (PreparedStatement psCust = conn.prepareStatement(sqlCust)) {
                    psCust.setString(1, txtCustId.getText());
                    psCust.setString(2, txtFullName.getText());
                    psCust.setString(3, txtPassport.getText());
                    psCust.setDate(4, birthDate.getValue() != null ? Date.valueOf(birthDate.getValue()) : null);
                    psCust.setString(5, nationality.getValue());
                    psCust.setString(6, txtEmail.getText());

                    if (selectedFile != null) {
                        FileInputStream fis = new FileInputStream(selectedFile);
                        psCust.setBinaryStream(7, fis, (int) selectedFile.length());
                    } else {
                        psCust.setNull(7, java.sql.Types.BLOB);
                    }
                    psCust.executeUpdate();
                }

                // 2. حفظ الحجز في جدول bookings مرتبطاً بالـ Customer_ID
                String sqlBook = "INSERT INTO bookings (Booking_ID, Booking_Date, Total_Amount, Customer_ID, flightNum) VALUES (?,?,?,?,?)";
                try (PreparedStatement psBook = conn.prepareStatement(sqlBook)) {
                    psBook.setString(1, txtBookingId.getText());
                    psBook.setDate(2, bookingDate.getValue() != null ? Date.valueOf(bookingDate.getValue()) : Date.valueOf(LocalDate.now()));
                    psBook.setDouble(3, Double.parseDouble(txtAmount.getText()));
                    psBook.setString(4, txtCustId.getText()); // الربط هنا
                    psBook.setString(5, flightSelection.getValue().split(" \\| ")[0]);
                    psBook.executeUpdate();
                }

                conn.commit(); // تنفيذ الحفظ في الجدولين معاً
                new Alert(Alert.AlertType.INFORMATION, "Customer and Booking saved successfully!").show();
                clearFields();
                
            } catch (SQLException ex) {
                conn.rollback(); // في حالة فشل أي جدول، يتم إلغاء العملية كاملة
                throw ex;
            }
            
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Database Error: " + ex.getMessage()).show();
        }
    }

    private void loadFlightsFromDatabase() {
        String query = "SELECT flightNum, origin, destination FROM flights"; 
        try (Connection conn = DatabaseHandler.getConnection(); 
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            flightSelection.getItems().clear();
            while (rs.next()) {
                flightSelection.getItems().add(rs.getString("flightNum") + " | " + rs.getString("origin") + " to " + rs.getString("destination"));
            }
        } catch (Exception e) {
            System.err.println("Flights Load Error");
        }
    }

    private void setupCustomerFields(GridPane grid) {
        nationality.getItems().addAll("Egyptian", "Saudi Arabia", "Sudanese", "Other");
        txtOtherNationality.setVisible(false);
        nationality.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> 
            txtOtherNationality.setVisible("Other".equals(newVal)));

        grid.add(new Label("Customer ID*"), 0, 0); grid.add(txtCustId, 1, 0);
        grid.add(new Label("Full Name*"), 0, 1); grid.add(txtFullName, 1, 1);
        grid.add(new Label("Passport Number*"), 0, 2); grid.add(txtPassport, 1, 2);
        grid.add(new Label("Date of Birth*"), 0, 3); grid.add(birthDate, 1, 3);
        grid.add(new Label("Email"), 0, 4); grid.add(txtEmail, 1, 4);
        grid.add(new Label("Nationality*"), 0, 5); 
        grid.add(new VBox(5, nationality, txtOtherNationality), 1, 5);
    }

    private void setupBookingFields(GridPane grid) {
        flightSelection.setPrefWidth(250);
        grid.add(new Label("Booking ID*"), 0, 0); grid.add(txtBookingId, 1, 0);
        grid.add(new Label("Flight Selection*"), 0, 1); grid.add(flightSelection, 1, 1);
        grid.add(new Label("Booking Date*"), 0, 2); grid.add(bookingDate, 1, 2);
        grid.add(new Label("Total Amount*"), 0, 3); grid.add(txtAmount, 1, 3);
    }

    private void clearFields() {
        txtCustId.clear(); txtFullName.clear(); txtPassport.clear();
        txtEmail.clear(); txtOtherNationality.clear();
        txtBookingId.clear(); txtAmount.clear();
        birthDate.setValue(null); bookingDate.setValue(null);
        nationality.getSelectionModel().clearSelection();
        flightSelection.getSelectionModel().clearSelection();
        passportImageView.setImage(null);
        selectedImage = null;
        selectedFile = null;
    }

    private VBox createStyledCard() {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #ddd; -fx-border-width: 1;");
        return card;
    }

    @Override public void loadData() {}
    @Override public BookingList.Booking getData() { return null; }
}