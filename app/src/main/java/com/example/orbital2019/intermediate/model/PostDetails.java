package com.example.orbital2019.intermediate.model;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class PostDetails implements Serializable {

    // Database Keys
    public static final String postDetailsKey = "PostDetails";
    public static final String foodNameKey = "FoodName";
    public static final String descriptionKey = "Description";
    public static final String ingredientKey = "Ingredient";
    public static final String priceKey = "Price";
    public static final String sellerKey = "Seller";
    public static final String areaKey = "Area";
    public static final String imageKey = "Image";
    public static final String typeKey = "Type";
    public static final String halalKey = "Halal";
    public static final String vegetarianKey = "Vegetarian";
    public static final String chineseKey = "Chinese";
    public static final String malayKey = "Malay";
    public static final String indianKey = "Indian";
    public static final String westernKey = "Western";
    public static final String otherKey = "Other";
    public static final String inputKey = "Input";

    public static final String dateKey = "Date";


    public String foodName;
    public String description;
    public String ingredient;
    public String price;
    public String seller;
    public String area;
    public String image;
    public boolean halal;
    public boolean vegetarian;
    public boolean chinese;
    public boolean malay;
    public boolean indian;
    public boolean western;
    public boolean other;
    public long input;

    public String date;

    public PostDetails(String foodName, String description, String ingredient, String price, String seller, String area, boolean halal, boolean vegetarian,
                       boolean chinese, boolean malay, boolean indian, boolean western, boolean other, String image, long input, String date) {
        this.foodName = foodName;
        this.description = description;
        this.ingredient = ingredient;
        this.price = price;
        this.seller = seller;
        this.area = area;
        this.image = image;
        this.halal = halal;
        this.vegetarian = vegetarian;
        this.chinese = chinese;
        this.malay = malay;
        this.indian = indian;
        this.western = western;
        this.other = other;
        this.input = input;
        this.date = date;
    }

    public String getFoodName() {
        return foodName;
    }
    public String getDescription() {
        return description;
    }
    public String getIngredient() { return ingredient; }
    public String getPrice() {
        return price;
    }
    public String getSeller() {
        return seller;
    }
    public String getArea() {
        return area;
    }
    public String getImage() {
        return image;
    }
    public boolean getHalal() {
        return halal;
    }
    public boolean getVegetarian() {
        return vegetarian;
    }
    public boolean getChinese() {
        return chinese;
    }
    public boolean getMalay() {
        return malay;
    }
    public boolean getIndian() {
        return indian;
    }
    public boolean getWestern() {
        return western;
    }
    public boolean getOther() {
        return other;
    }
    public long getInput() {
        return input;
    }
    public String getDate() {return date; }

    // call to create entry In database.
    public void createEntry () {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Boolean> typeData = new HashMap<>();
        typeData.put(halalKey, halal);
        typeData.put(vegetarianKey, vegetarian);
        typeData.put(chineseKey, chinese);
        typeData.put(malayKey, malay);
        typeData.put(indianKey, indian);
        typeData.put(westernKey, western);
        typeData.put(otherKey, other);

        Map<String,Object> data = new HashMap<>();

        data.put(foodNameKey,foodName);
        data.put(descriptionKey,description);
        data.put(ingredientKey, ingredient);
        data.put(priceKey,price);
        data.put(sellerKey, seller);
        data.put(areaKey, area);
        data.put(imageKey, image);
        data.put(typeKey, typeData);
        data.put(inputKey, input);
        data.put(dateKey, date);

        db.collection(postDetailsKey).document(input + "").set(data);
    }

    public void deleteEntry() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(postDetailsKey).document(input + "").delete();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof PostDetails) {
            PostDetails post = (PostDetails) o;
            return post.getInput() == (input);
        } else {
            return false;
        }
    }

}
