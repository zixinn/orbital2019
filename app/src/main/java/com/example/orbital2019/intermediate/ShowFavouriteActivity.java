package com.example.orbital2019.intermediate;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.orbital2019.R;
import com.example.orbital2019.intermediate.model.PostDetails;
import com.example.orbital2019.intermediate.model.UserDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShowFavouriteActivity extends AppCompatActivity {

    static final int PICK_CONTACT_REQUEST = 1;
    private List<PostDetails> postDetailsList = new ArrayList<PostDetails>();
    private GridView gridView;
    private PostsAdapterFav postsAdapter;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_favourite);
        setTitle("Favourites");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Favourites");

        mAuth = FirebaseAuth.getInstance();

        postsAdapter = new PostsAdapterFav(getApplicationContext(), postDetailsList);
        gridView = findViewById(R.id.posts_list_view);
        gridView.setAdapter(postsAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                PostDetails postDetails = (PostDetails) parent.getItemAtPosition(position);
                Intent intent = new Intent(getApplicationContext(), BuyViewDetailsActivity.class);
                intent.putExtra("postDetails", (Serializable) postDetails);
                startActivityForResult(intent, PICK_CONTACT_REQUEST);
            }
        });

        loadPostData();

    }

    private void loadPostData() {

        final FirebaseFirestore fs = FirebaseFirestore.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();
        final String name = user.getDisplayName();
        final String id = user.getUid();

        fs.collection(UserDetails.userDetailsKey).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<DocumentSnapshot> documents = task.getResult().getDocuments();
                for (final DocumentSnapshot document : documents) {
                    if (document.contains(UserDetails.idKey) && document.contains(UserDetails.nameKey)
                            && document.contains(UserDetails.areaKey) && document.contains(UserDetails.certKey)
                            && document.contains(UserDetails.numberKey) && document.contains(UserDetails.ratingKey)
                            && document.contains(UserDetails.reviewKey) && document.contains(UserDetails.favouriteKey)) {

                        final String myName = (String) document.get(UserDetails.nameKey);

                        if (myName.equals(name)) {
                            final List<Long> favourite = (List<Long>) document.get(UserDetails.favouriteKey);
                            fs.collection(PostDetails.postDetailsKey).orderBy(PostDetails.inputKey, Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                    List<DocumentSnapshot> documents = task.getResult().getDocuments();

                                    postDetailsList.removeAll(postDetailsList);

                                    for (DocumentSnapshot document : documents) {

                                        if (document.contains(PostDetails.foodNameKey) && document.contains(PostDetails.descriptionKey) && document.contains(PostDetails.ingredientKey)
                                                && document.contains(PostDetails.priceKey) && document.contains(PostDetails.sellerKey)
                                                && document.contains(PostDetails.areaKey) && document.contains(PostDetails.typeKey) && document.contains(PostDetails.inputKey)
                                                && document.contains(PostDetails.dateKey)) {

                                            String seller = (String) document.get(PostDetails.sellerKey);
                                            long input = (long) document.get(PostDetails.inputKey);

                                            if (!seller.equals(id) && favourite.contains(input)) {
                                                String name = (String) document.get(PostDetails.foodNameKey);
                                                String description = (String) document.get(PostDetails.descriptionKey);
                                                String ingredient = (String) document.get(PostDetails.ingredientKey);
                                                String price = (String) document.get(PostDetails.priceKey);
                                                String area = (String) document.get(PostDetails.areaKey);

                                                Map type = (Map) document.get(PostDetails.typeKey);
                                                Boolean halal = (Boolean) type.get(PostDetails.halalKey);
                                                Boolean vegetarian = (Boolean) type.get(PostDetails.vegetarianKey);
                                                Boolean chinese = (Boolean) type.get(PostDetails.chineseKey);
                                                Boolean malay = (Boolean) type.get(PostDetails.malayKey);
                                                Boolean indian = (Boolean) type.get(PostDetails.indianKey);
                                                Boolean western = (Boolean) type.get(PostDetails.westernKey);
                                                Boolean other = (Boolean) type.get(PostDetails.otherKey);

                                                String image = (String) document.get(PostDetails.imageKey);
                                                String date = (String) document.get(PostDetails.dateKey);

                                                PostDetails details = new PostDetails(name, description, ingredient, price, seller, area,
                                                        halal, vegetarian, chinese, malay, indian, western, other, image, input, date);
                                                postDetailsList.add(details);
                                            }

                                        }
                                    }

                                    postsAdapter.notifyDataSetChanged();
                                }
                            });

                        }
                    }
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == PICK_CONTACT_REQUEST) {
            // Make sure the request was successful
            loadPostData();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

}
