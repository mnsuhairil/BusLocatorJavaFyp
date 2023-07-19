package com.example.buslocatorsystem;

public class Request {
    private String requestId;
    private String passengerId;
    private String busId;

    public Request() {
        // Default constructor required for Firebase serialization
    }

    public Request(String requestId, String passengerId, String busId) {
        this.requestId = requestId;
        this.passengerId = passengerId;
        this.busId = busId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }

    public String getBusId() {
        return busId;
    }

    public void setBusId(String busId) {
        this.busId = busId;
    }
}
