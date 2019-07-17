package com.example.orbital2019.intermediate;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.orbital2019.R;
import com.example.orbital2019.intermediate.message.MessageActivity;
import com.example.orbital2019.intermediate.model.PostDetails;
import com.example.orbital2019.intermediate.model.UserDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static com.example.orbital2019.intermediate.model.UserDetails.certKey;

public class BuyViewDetailsActivity extends AppCompatActivity {

    TextView nameTV, descriptionTV, priceTV, typeTV, usernameTV, areaTV, certTV, ratingTV, reviewTV, ingredientTV;
    TextView dateTV;
    private ImageView imageTV, certImageTV;
    private RatingBar rateTV;
    private FloatingActionButton mat;
    private List<String> reviewsList = new ArrayList<>();
    private ReviewsAdapter reviewsAdapter;
    private ListView listView;
    private String sellername, seller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyviewdetails);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("View Post");

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        final PostDetails postDetails = (PostDetails) bundle.getSerializable("postDetails");

        nameTV = findViewById(R.id.foodName);
        descriptionTV = findViewById(R.id.foodDescription);
        priceTV = findViewById(R.id.foodPrice);
        typeTV = findViewById(R.id.foodType);
        usernameTV = findViewById(R.id.sellerName);
        areaTV = findViewById(R.id.sellerArea);
        certTV = findViewById(R.id.cert);
        ratingTV = findViewById(R.id.sellerRating);
        rateTV = findViewById(R.id.star);
        reviewTV = findViewById(R.id.sellerReview);
        imageTV = findViewById(R.id.foodImage);
        certImageTV = findViewById(R.id.certImage);
        ingredientTV = findViewById(R.id.foodIngredient);
        listView = findViewById(R.id.reviews_list_view);

        dateTV = findViewById(R.id.foodDate);
        dateTV.append(postDetails.getDate());

        reviewsAdapter = new ReviewsAdapter(getApplicationContext(), reviewsList);
        listView.setAdapter(reviewsAdapter);


        Glide.with(this).load(postDetails.getImage()).into(imageTV);
        nameTV.append(postDetails.getFoodName());
        descriptionTV.append(postDetails.getDescription());
        ingredientTV.append(postDetails.getIngredient());
        priceTV.append("$" + String.format("%.2f", Double.parseDouble(postDetails.getPrice())));

        if (postDetails.getHalal() || postDetails.getVegetarian() || postDetails.getChinese() || postDetails.getMalay() ||
                postDetails.getIndian() || postDetails.getWestern() || postDetails.getOther()) {
            if (postDetails.getHalal()) {
                typeTV.append("Halal・");
            }
            if (postDetails.getVegetarian()) {
                typeTV.append("Vegetarian・");
            }
            if (postDetails.getChinese()) {
                typeTV.append("Chinese・");
            }
            if (postDetails.getMalay()) {
                typeTV.append("Malay・");
            }
            if (postDetails.getIndian()) {
                typeTV.append("Indian・");
            }
            if (postDetails.getWestern()) {
                typeTV.append("Western・");
            }
            if (postDetails.getOther()) {
                typeTV.append("Other・");
            }
        } else {
            typeTV.append("None");
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        seller = postDetails.getSeller();
        db.collection(UserDetails.userDetailsKey).whereEqualTo(UserDetails.idKey, seller).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<DocumentSnapshot> docs = task.getResult().getDocuments();
                DocumentSnapshot document = docs.get(0);
                sellername = (String) document.get(UserDetails.nameKey);
                usernameTV.append(sellername);
            }
        });

        mat = findViewById(R.id.Material);
        mat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BuyViewDetailsActivity.this, MessageActivity.class);
                intent.putExtra("personID", seller);
                intent.putExtra("personName", sellername);
                intent.putExtra("foodName", postDetails.getFoodName());
                intent.putExtra("foodID", postDetails.getInput()+"");
                intent.putExtra("sellerName", sellername);
                intent.putExtra("sellerID", seller);
                startActivityForResult(intent, 0);
            }
        });

        db.collection(UserDetails.userDetailsKey).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
            List<DocumentSnapshot> documents = task.getResult().getDocuments();
            for (DocumentSnapshot document : documents) {
                if (document.contains(UserDetails.idKey) && document.contains(UserDetails.nameKey)
                        && document.contains(UserDetails.areaKey) && document.contains(UserDetails.certKey)
                        && document.contains(UserDetails.numberKey) && document.contains(UserDetails.ratingKey)
                        && document.contains(UserDetails.reviewKey) && document.contains(UserDetails.favouriteKey)) {
                    String name = (String) document.get(UserDetails.nameKey);
                    String id = (String) document.get(UserDetails.idKey);
                    if (id.equals(seller)) {
                        String area = (String) document.get(UserDetails.areaKey);
                        String cert = (String) document.get(certKey);
                        double rating = (double) document.get(UserDetails.ratingKey);
                        long number = (long) document.get(UserDetails.numberKey);
                        List<String> reviews = (List<String>) document.get(UserDetails.reviewKey);
                        areaTV.append(area);
                        if (cert.equals("")) {
                            certTV.append("Not available");
                            certImageTV.setVisibility(View.GONE);
                        } else {
                            Glide.with(BuyViewDetailsActivity.this).load(cert).into(certImageTV);
                            certImageTV.setVisibility(View.VISIBLE);
                        }
                        if (number == 0) {
                            ratingTV.append("Not available");
                        } else {
                            ratingTV.append(String.format("%.1f", rating));
                        }
                        rateTV.setRating((float) rating);
                        if (reviews.isEmpty()) {
                            reviewTV.append("Not available");
                        } else {
                            reviewsList.removeAll(reviewsList);
                            for (String review : reviews) {
                                reviewsList.add(review);
                            }
                        }
                        break;
                    }
                }
            }
            reviewsAdapter.notifyDataSetChanged();
            setListViewHeightBasedOnChildren(listView);
            }
        });

    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) return;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(),
                View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0) view.setLayoutParams(new
                    ViewGroup.LayoutParams(desiredWidth,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();

        params.height = totalHeight + (listView.getDividerHeight() *
                (listAdapter.getCount() - 1));

        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

}
