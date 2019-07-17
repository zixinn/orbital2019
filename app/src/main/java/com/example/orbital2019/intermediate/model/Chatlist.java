package com.example.orbital2019.intermediate.model;

public class Chatlist {

    public String name;
    public String food;
    public String seller;



    public Chatlist(String name, String food, String seller) {
        this.name = name;
        this.food = food;
        this.seller = seller;

    }

    public Chatlist() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFood() { return food; }

    public void setFood(String food) { this.food = food; }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

}
