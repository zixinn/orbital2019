package com.example.orbital2019.intermediate;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
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

import static com.example.orbital2019.intermediate.model.UserDetails.certKey;

public class HomeFragment extends Fragment {

    private FirebaseAuth mAuth;
    TextView nameTV, areaTV, certTV, ratingTV, reviewTV;
    private ImageView certImageTV;
    RatingBar rateTV;
    FloatingActionButton editBtn;
    private List<String> reviewsList = new ArrayList<>();
    private ReviewsAdapter reviewsAdapter;
    private ListView listView;


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Home");
        mAuth = FirebaseAuth.getInstance();

        nameTV = view.findViewById(R.id.name);
        areaTV = view.findViewById(R.id.area);
        certTV = view.findViewById(R.id.cert);
        certImageTV = view.findViewById(R.id.certImage);
        ratingTV = view.findViewById(R.id.rating);
        rateTV = view.findViewById(R.id.star);
        reviewTV = view.findViewById(R.id.review);
        editBtn = view.findViewById(R.id.edit);
        listView = view.findViewById(R.id.reviews_list_view);

        reviewsAdapter = new ReviewsAdapter(getContext(), reviewsList);
        listView.setAdapter(reviewsAdapter);

        // on logged in
        if(mAuth.getCurrentUser()!=null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            final String theID = mAuth.getCurrentUser().getUid();

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
                            String id = (String) document.get(UserDetails.idKey);
                            if (id.equals(theID)) {
                                String name = (String) document.get(UserDetails.nameKey);
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
                                    Glide.with(getActivity()).load(cert).into(certImageTV);
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

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), EditProfileActivity.class);
                startActivityForResult(intent, PICK_THIS);
            }
        });

    }

    public HomeFragment() {
        // Required empty public constructor
    }

    static final int PICK_THIS = 3;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == PICK_THIS) {
            // Make sure the request was successful
            Fragment fragment = new HomeFragment();
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_home, container, false);

    }

}
