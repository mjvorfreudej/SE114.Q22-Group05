package com.example.tourgo.remote.api;

public class PaymentRequest {
    private String bookingId;
    private double amount;
    private String paymentMethod;

    public PaymentRequest(String bookingId, double amount, String paymentMethod) {
        this.bookingId = bookingId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }

    public String getBookingId() {
        return bookingId;
    }

    public double getAmount() {
        return amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }
}
