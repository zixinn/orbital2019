package com.example.orbital2019.intermediate;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.orbital2019.R;
import com.example.orbital2019.intermediate.model.UserDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static com.example.orbital2019.intermediate.BuyViewDetailsActivity.setListViewHeightBasedOnChildren;
import static com.example.orbital2019.intermediate.model.UserDetails.certKey;

public class ViewUsersActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    TextView nameTV, areaTV, certTV, ratingTV, reviewTV;
    private ImageView certImageTV;
    RatingBar rateTV;
    FloatingActionButton editBtn;
    private List<String> reviewsList = new ArrayList<>();
    private ReviewsAdapter reviewsAdapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewusers);
        mAuth = FirebaseAuth.getInstance();

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("View User");

        nameTV = findViewById(R.id.name);
        areaTV = findViewById(R.id.area);
        certTV = findViewById(R.id.cert);
        certImageTV = findViewById(R.id.certImage);
        ratingTV = findViewById(R.id.rating);
        rateTV = findViewById(R.id.star);
        reviewTV = findViewById(R.id.review);
        editBtn = findViewById(R.id.edit);
        listView = findViewById(R.id.reviews_list_view);

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        final UserDetails userDetails = (UserDetails) bundle.getSerializable("userDetails");
        final String theName = userDetails.getName();

        reviewsAdapter = new ReviewsAdapter(this, reviewsList);
        listView.setAdapter(reviewsAdapter);

        // on logged in
        if(mAuth.getCurrentUser()!=null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection(UserDetails.userDetailsKey)
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    List<DocumentSnapshot> documents = task.getResult().getDocuments();
                    for (DocumentSnapshot document : documents) {
                        if (document.contains(UserDetails.idKey) && document.contains(UserDetails.nameKey)
                                && document.contains(UserDetails.areaKey) && document.contains(UserDetails.certKey)
                                && document.contains(UserDetails.numberKey) && document.contains(UserDetails.ratingKey)
                                && document.contains(UserDetails.reviewKey) && document.contains(UserDetails.favouriteKey)) {
                            String name = (String) document.get(UserDetails.nameKey);
                            if (name.equals(theName)) {
                                String area = (String) document.get(UserDetails.areaKey);
                                String cert = (String) document.get(certKey);
                                double rating = (double) document.get(UserDetails.ratingKey);
                                long number = (long) document.get(UserDetails.numberKey);
                                List<String> reviews = (List<String>) document.get(UserDetails.reviewKey);
                                nameTV.append(name);
                                areaTV.append(area);
                                if (cert.equals("")) {
                                    certTV.append("Not available");
                                    certImageTV.setVisibility(View.GONE);
                                } else {
                                    Glide.with(ViewUsersActivity.this).load(cert).into(certImageTV);
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

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

}
