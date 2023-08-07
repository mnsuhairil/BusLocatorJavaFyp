package com.example.buslocatorsystem;

public class Passenger {

    private  String userType;
    private  String password;
    private  String uname;
    private String userId;
    private String name;
    private String email;
    private String phone;
    private String gender;

    public Passenger() {
        // Default constructor required for calls to DataSnapshot.getValue(Passenger.class)
    }

    public Passenger(String uname, String userId, String name, String email, String phone, String password, String userType,String gender) {

        this.uname = uname;
        this.password = password;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.userType = userType;
        this.userId = userId;
        this.gender = gender;

    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
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
