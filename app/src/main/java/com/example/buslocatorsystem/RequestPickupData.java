package com.example.buslocatorsystem;

public class RequestPickupData {
    private String currentBusId;
    private String passengerId;
    private String passengerName;
    private String phoneNumber;
    private String gender;

    public RequestPickupData() {
        // Default constructor required for Firebase Realtime Database
    }

    public RequestPickupData(String currentBusId, String passengerId, String passengerName, String phoneNumber, String gender) {
        this.currentBusId = currentBusId;
        this.passengerId = passengerId;
        this.passengerName = passengerName;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
    }

    public String getCurrentBusId() {
        return currentBusId;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getGender() {
        return gender;
    }
}
