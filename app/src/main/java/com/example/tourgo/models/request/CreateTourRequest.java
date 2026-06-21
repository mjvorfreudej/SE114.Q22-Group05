package com.example.tourgo.models.request;

/**
 * JSON body sent to the backend {@code POST /api/tours} when a provider submits
 * a new tour. The server validates it, sets it in the database (with
 * {@code status = "PENDING"}) and returns the created tour. Null fields are
 * omitted by Gson so the server can apply its own defaults.
 */
public class CreateTourRequest {
    private String name;
    private String description;
    private double price;
    private String destination;
    private String region;
    private String duration;
    private String status;

    public CreateTourRequest(String name, String description, double price,
                             String destination, String region, String duration, String status) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.destination = destination;
        this.region = region;
        this.duration = duration;
        this.status = status;
    }

    public String getName() { return name; }
    public String getStatus() { return status; }
}
