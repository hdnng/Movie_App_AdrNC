package com.example.movieapp.model;

public class User {
    private String uid;
    private String email;
    private String username;

    private int role;

    public User(){}

    public User(String uid, String email, String username, int role){
        this.uid = uid;
        this.email = email;
        this.username = username;
        this.role = role;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
