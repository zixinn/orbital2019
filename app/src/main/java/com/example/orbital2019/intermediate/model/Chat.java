package com.example.orbital2019.intermediate.model;

public class Chat {

    private String sender;
    private String receiver;
    private String message;
    private boolean isseen;
    private String food;
    private String seller;


    public Chat(String sender, String receiver, String message, boolean isseen, String food, String seller) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isseen = isseen;
        this.food = food;
        this.seller = seller;

    }

    public Chat() {

    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }

    public String getFood() { return food; }

    public void setFood(String food) { this.food = food; }

    public String getSeller() { return seller; }

    public void setSeller(String seller) { this.seller = seller; }

}
