package com.example.orbital2019.intermediate;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.example.orbital2019.R;
import com.example.orbital2019.intermediate.model.PostDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SellFragment extends Fragment {

    private List<PostDetails> postDetailsList = new ArrayList<PostDetails>();
    private GridView gridView;
    private PostsAdapterSell postsAdapter;
    private FirebaseAuth mAuth;
    private FloatingActionButton createBtn;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        getActivity().setTitle("View Posts");
        createBtn = view.findViewById(R.id.floatingActionButton);
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getContext(), CreateNewPostActivity.class);

                startActivityForResult(intent, 1);

            }
        });

        postsAdapter = new PostsAdapterSell(this, getActivity().getApplicationContext(), postDetailsList);

        gridView = getActivity().findViewById(R.id.posts_list_view);

        gridView.setAdapter(postsAdapter);

        loadUserData();

    }

    private void loadUserData() {

        FirebaseFirestore fs = FirebaseFirestore.getInstance();

        final FirebaseUser user = mAuth.getCurrentUser();

        fs.collection(PostDetails.postDetailsKey).orderBy(PostDetails.inputKey, Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                List<DocumentSnapshot> documents = task.getResult().getDocuments();

                // clean up the list to prevent double copies
                postDetailsList.removeAll(postDetailsList);

                for (DocumentSnapshot document : documents) {

                    if (document.contains(PostDetails.foodNameKey) && document.contains(PostDetails.descriptionKey) && document.contains(PostDetails.ingredientKey)
                            && document.contains(PostDetails.priceKey) && document.contains(PostDetails.sellerKey)
                            && document.contains(PostDetails.areaKey) && document.contains(PostDetails.typeKey) && document.contains(PostDetails.inputKey)
                            && document.contains(PostDetails.dateKey)) {

                        String name = (String) document.get(PostDetails.foodNameKey);
                        String description = (String) document.get(PostDetails.descriptionKey);
                        String ingredient = (String) document.get(PostDetails.ingredientKey);
                        String price = (String) document.get(PostDetails.priceKey);
                        String seller = (String) document.get(PostDetails.sellerKey);
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
                        long input = (long) document.get(PostDetails.inputKey);
                        String date = (String) document.get(PostDetails.dateKey);

                        if(seller.equals(user.getUid())) {
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

    static final int PICK_CONTACT_REQUEST = 1;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == PICK_CONTACT_REQUEST) {
            // Make sure the request was successful
            loadUserData();
        }
    }

    public SellFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_sell, container, false);

    }

}
