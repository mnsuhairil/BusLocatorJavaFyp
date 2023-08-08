package com.example.buslocatorsystem.constructor;

public class RequestPickupData {
    private String imgUrl;
    private String currentBusId;
    private String passengerId;
    private String passengerName;
    private String phoneNumber;
    private String gender;

    public RequestPickupData() {
        // Default constructor required for Firebase Realtime Database
    }

    public RequestPickupData(String currentBusId, String passengerId, String passengerName, String phoneNumber, String gender, String imgUrl) {
        this.currentBusId = currentBusId;
        this.passengerId = passengerId;
        this.passengerName = passengerName;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.imgUrl = imgUrl;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public void setCurrentBusId(String currentBusId) {
        this.currentBusId = currentBusId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setGender(String gender) {
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
