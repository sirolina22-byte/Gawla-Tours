package com.jawla.jawla;

// كلاس مستقل عشان الكل يشوفه بسهولة
public class Booking {
    private final String id, customerName, flightNo, origin, destination, date, total;

    public Booking(String id, String name, String flight, String origin, String dest, String date, String total) {
        this.id = id; 
        this.customerName = name; 
        this.flightNo = flight; 
        this.origin = origin; 
        this.destination = dest; 
        this.date = date; 
        this.total = total;
    }

    // Getters
    public String getId() { return id; }
    public String getCustomerName() { return customerName; }
    public String getFlightNo() { return flightNo; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public String getDate() { return date; }
    public String getTotal() { return total; }
}