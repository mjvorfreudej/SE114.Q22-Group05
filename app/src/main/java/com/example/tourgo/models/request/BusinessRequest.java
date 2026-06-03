package com.example.tourgo.models.request;

import com.google.gson.annotations.SerializedName;

/**
 * Request body for POST /api/users/businesses/register
 * Creates a new business account linked to the authenticated user.
 * Status is set to "pending" by default on the backend.
 */
public class BusinessRequest {
    @SerializedName("name")
    private String name;

    @SerializedName("owner")
    private String owner;

    @SerializedName("tax_code")
    private String taxCode;

    @SerializedName("address")
    private String address;

    @SerializedName("phone")
    private String phone;

    @SerializedName("email")
    private String email;

    public BusinessRequest() {
    }

    public BusinessRequest(String name, String owner, String taxCode, String address, String phone, String email) {
        this.name = name;
        this.owner = owner;
        this.taxCode = taxCode;
        this.address = address;
        this.phone = phone;
        this.email = email;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getTaxCode() {
        return taxCode;
    }

    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
