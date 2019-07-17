package com.example.orbital2019.intermediate.model;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDetails implements Serializable {

    // Database Keys
    public static final String userDetailsKey = "UserDetails";
    public static final String idKey = "ID";
    public static final String nameKey = "Username";
    public static final String areaKey = "AreaOfStay";
    public static final String certKey = "Cert";
    public static final String numberKey = "NumberOfRating";
    public static final String ratingKey = "Rating";
    public static final String reviewKey = "Review";
    public static final String favouriteKey = "Favourite";

    public String name;
    public String id;
    public String area;
    public String cert;
    public long number;
    public double rating;
    public List<String> review;
    public List<Long> favourite;

    public UserDetails(String id, String name, String area, String cert, long number, double rating,
                       List<String> review, List<Long> favourite) {

        this.id = id;
        this.name = name;
        this.area = area;
        this.cert = cert;
        this.number = number;
        this.rating = rating;
        this.review = review;
        this.favourite = favourite;

    }

    public String getID() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getArea() {
        return area;
    }
    public String getCert() {
        return cert;
    }
    public long getNumber() {
        return number;
    }
    public double getRating() {
        return rating;
    }
    public List<String> getReview() {
        return review;
    }
    public List<Long> getFavourite() {
        return favourite;
    }

    // call to create entry In database.
    public void createEntry () {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String,Object> data = new HashMap<>();

        data.put(idKey, id);
        data.put(nameKey,name);
        data.put(areaKey,area);
        data.put(certKey,cert);
        data.put(numberKey,number);
        data.put(ratingKey,rating);
        data.put(reviewKey,review);
        data.put(favouriteKey,favourite);

        db.collection(userDetailsKey).document(id).set(data);
    }

    public void deleteEntry() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(userDetailsKey).document(id).delete();
    }

}
