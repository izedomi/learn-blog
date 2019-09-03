package com.example.learn_firebase;

public class BlogModel {

    String title;
    String description;
    String img_url;
    String user_name;



    public BlogModel(){}

    BlogModel(String img, String title, String desc, String username){
        this.title = title;
        this.description = desc;
        this.img_url = img;
        this.user_name = username;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }
    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }
}
