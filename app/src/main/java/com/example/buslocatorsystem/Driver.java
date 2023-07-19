package com.example.buslocatorsystem;

public class Driver {

    private boolean online;
    private String uid;
    private String name;
    private String busId;
    private String route;
    private int totalPassenger;
    private String busOilStatus;

    private String password; // Add a password field

    public Driver() {
        // Default constructor required for Firebase database operations
    }




    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public Driver(String name, String busId, String route, String password) {
        this.name = name;
        this.busId = busId;
        this.route = route;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getBusId() {
        return busId;
    }

    public String getRoute() {
        return route;
    }

    public int getTotalPassenger() {
        return totalPassenger;
    }

    public void setTotalPassenger(int totalPassenger) {
        this.totalPassenger = totalPassenger;
    }

    public String getBusOilStatus() {
        return busOilStatus;
    }

    public void setBusOilStatus(String busOilStatus) {
        this.busOilStatus = busOilStatus;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    // Override the toString() method
    @Override
    public String toString() {
        return "Bus ID: " + busId + "\nName: " + name;
    }

}
