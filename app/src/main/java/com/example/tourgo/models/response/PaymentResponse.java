package com.example.tourgo.models.response;

import com.google.gson.annotations.SerializedName;

public class PaymentResponse {
    @SerializedName("payment_method")
    private String paymentMethod;

    @SerializedName("payment_url")
    private String paymentUrl;

    @SerializedName("transaction_code")
    private String transactionCode;

    @SerializedName("amount")
    private double amount;

    @SerializedName("bank_info")
    private BankInfo bankInfo;

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public double getAmount() {
        return amount;
    }

    public BankInfo getBankInfo() {
        return bankInfo;
    }

    public static class BankInfo {
        @SerializedName("bank_name")
        private String bankName;

        @SerializedName("account_number")
        private String accountNumber;

        @SerializedName("account_holder")
        private String accountHolder;

        @SerializedName("amount")
        private double amount;

        @SerializedName("transfer_note")
        private String transferNote;

        public String getBankName() {
            return bankName;
        }

        public String getAccountNumber() {
            return accountNumber;
        }

        public String getAccountHolder() {
            return accountHolder;
        }

        public double getAmount() {
            return amount;
        }

        public String getTransferNote() {
            return transferNote;
        }
    }
}
