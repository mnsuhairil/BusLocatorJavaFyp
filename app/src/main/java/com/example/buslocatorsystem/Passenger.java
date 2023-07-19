package com.example.buslocatorsystem;

public class Passenger {

    private  String userType;
    private  String password;
    private  String uname;
    private String userId;
    private String name;
    private String email;
    private String phone;

    public Passenger() {
        // Default constructor required for calls to DataSnapshot.getValue(Passenger.class)
    }

    public Passenger(String uname, String userId, String name, String email, String phone, String password, String userType) {

        this.uname = uname;
        this.password = password;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.userType = userType;
        this.userId = userId;

    }


    public String getUserType() {
        return userType;
    }

    public String getPassword() {
        return password;
    }

    public String getUname() {
        return uname;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }
}
