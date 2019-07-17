package com.example.orbital2019.intermediate.model;

public class Pair {
    private String person;
    private String meal;
    private String seller;

    public Pair(String person, String meal, String seller) {
        this.person = person;
        this.meal = meal;
        this.seller = seller;

    }

    public String getPerson() {
        return person;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public String getMeal() {
        return meal;
    }

    public void setMeal(String meal) {
        this.meal = meal;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }


}
