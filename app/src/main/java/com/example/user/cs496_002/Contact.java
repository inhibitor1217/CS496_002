package com.example.user.cs496_002;

public class Contact {

    public String name, phone, email, facebook, profileImage;

    public Contact(String name) {
        this.name = name;
        this.phone = "";
        this.email = "";
        this.facebook = "";
        this.profileImage = "";
    }

    public Contact(String name, String phone, String email, String facebook, String profileImage) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.facebook = facebook;
        this.profileImage = profileImage;
    }

    public Contact setName(String name) {
        this.name = name;
        return this;
    }

    public Contact setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public Contact setEmail(String email) {
        this.email = email;
        return this;
    }

    public Contact setFacebook(String facebook) {
        this.facebook = facebook;
        return this;
    }

    public Contact setProfileImage(String profileImage) {
        this.profileImage = profileImage;
        return this;
    }
}
