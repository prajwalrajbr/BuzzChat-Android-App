package com.example.buzzchat;

public class Friends {
    String name, image, bio, uid;

    public Friends() {
    }

    public Friends(String name, String image, String bio, String uid) {
        this.name = name;
        this.image = image;
        this.bio = bio;
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
