package com.example.orbital2019.intermediate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.orbital2019.R;
import com.example.orbital2019.intermediate.model.UserDetails;

import java.util.List;

public class RateReviewActivity extends AppCompatActivity {

    private TextView sellerTV;
    private RatingBar star;
    private EditText reviewEditText;
    private Button submitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ratereview);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Rate/Review User");

        sellerTV = findViewById(R.id.sellerName);
        star = findViewById(R.id.star);
        reviewEditText = findViewById(R.id.review);
        submitBtn = findViewById(R.id.submit);

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        final UserDetails userDetails = (UserDetails) bundle.getSerializable("userDetails");

        sellerTV.append(userDetails.getName());

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                userDetails.deleteEntry();

                double currRating = star.getRating();
                String currReview = reviewEditText.getText().toString();

                long numRating = userDetails.getNumber();
                double rating = userDetails.getRating();
                List<String> review = userDetails.getReview();

                rating = (rating * numRating + currRating) / (numRating + 1);

                if (!currReview.equals("")) {
                    review.add(currReview);
                }

                UserDetails user = new UserDetails(userDetails.getID(), userDetails.getName(), userDetails.getArea(), userDetails.getCert(),
                        numRating + 1, rating, review, userDetails.getFavourite());
                user.createEntry();
                Toast.makeText(getApplicationContext(), "Rating submitted successfully!", Toast.LENGTH_LONG).show();
                setResult(RESULT_OK);
                finish();

            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

}
