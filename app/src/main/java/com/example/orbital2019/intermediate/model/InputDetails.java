package com.example.orbital2019.intermediate.model;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class InputDetails implements Serializable {

    public static final String inputDetailsKey = "InputDetails";
    public static final String orderKey = "Order";

    public long order;

    public InputDetails(long order) {
        this.order = order;
    }

    public long getOrder() {
        return order;
    }

    public void createEntry () {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String,Object> data = new HashMap<>();

        data.put(orderKey,order);

        db.collection(inputDetailsKey).document(orderKey).set(data);
    }

    public void deleteEntry() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(inputDetailsKey).document(orderKey).delete();
    }

}
