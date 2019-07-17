package com.example.orbital2019.intermediate;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.example.orbital2019.R;
import com.example.orbital2019.intermediate.model.UserDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends Fragment {
    private List<UserDetails> userDetailsList = new ArrayList<UserDetails>();
    private ListView listView;
    private DetailsAdapter detailsAdapter;
    private EditText searchEditText;
    private FirebaseAuth mAuth;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().setTitle("View Users");
        detailsAdapter = new DetailsAdapter(getContext(), userDetailsList);

        listView = view.findViewById(R.id.details_list_view);
        searchEditText = view.findViewById(R.id.search_edit_text);

        mAuth = FirebaseAuth.getInstance();

        listView.setAdapter(detailsAdapter);

        // on click listner
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                UserDetails userDetails = (UserDetails)parent.getItemAtPosition(position);
                Intent intent = new Intent(getActivity().getApplicationContext(), ViewUsersActivity.class);
                intent.putExtra("userDetails", (Serializable) userDetails);
                startActivityForResult(intent, 1);
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (!s.toString().isEmpty())
                    searchUserData(s.toString());
                else
                    loadUserData();
            }
        });


        loadUserData();
    }


    private void loadUserData() {

        FirebaseFirestore fs = FirebaseFirestore.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();

        fs.collection(UserDetails.userDetailsKey).orderBy(UserDetails.nameKey).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                List<DocumentSnapshot> documents = task.getResult().getDocuments();

                // clean up the list to prevent double copies
                userDetailsList.removeAll(userDetailsList);

                for (DocumentSnapshot document : documents) {

                    if (document.contains(UserDetails.idKey) && document.contains(UserDetails.nameKey)
                            && document.contains(UserDetails.areaKey) && document.contains(UserDetails.certKey)
                            && document.contains(UserDetails.numberKey) && document.contains(UserDetails.ratingKey)
                            && document.contains(UserDetails.reviewKey) && document.contains(UserDetails.favouriteKey)) {

                        String id = (String) document.get(UserDetails.idKey);
                        String name = (String) document.get(UserDetails.nameKey);
                        String area = (String) document.get(UserDetails.areaKey);
                        String cert = (String) document.get(UserDetails.certKey);
                        long number = (long) document.get(UserDetails.numberKey);
                        double rating = (double) document.get(UserDetails.ratingKey);
                        List<String> review = (List<String>) document.get(UserDetails.reviewKey);
                        List<Long> favourite = (List<Long>) document.get(UserDetails.favouriteKey);

                        if (!user.getDisplayName().equals(name)) {
                            UserDetails details = new UserDetails(id, name, area, cert, number, rating, review, favourite);
                            userDetailsList.add(details);
                        }

                    }
                }

                detailsAdapter.notifyDataSetChanged();
            }
        });

    }

    private void searchUserData(String filter) {

        FirebaseFirestore fs = FirebaseFirestore.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();

        final int textlength = filter.length();
        final String compare = filter;
        fs.collection(UserDetails.userDetailsKey).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                List<DocumentSnapshot> documents = task.getResult().getDocuments();

                // clean up the list to prevent double copies
                userDetailsList.removeAll(userDetailsList);

                for (DocumentSnapshot document : documents) {

                    if (document.contains(UserDetails.idKey) && document.contains(UserDetails.nameKey)
                            && document.contains(UserDetails.areaKey) && document.contains(UserDetails.certKey)
                            && document.contains(UserDetails.numberKey) && document.contains(UserDetails.ratingKey)
                            && document.contains(UserDetails.reviewKey) && document.contains(UserDetails.favouriteKey)) {

                        String id = (String) document.get(UserDetails.idKey);
                        String name = (String) document.get(UserDetails.nameKey);
                        String area = (String) document.get(UserDetails.areaKey);
                        String cert = (String) document.get(UserDetails.certKey);
                        long number = (long) document.get(UserDetails.numberKey);
                        double rating = (double) document.get(UserDetails.ratingKey);
                        List<String> review = (List<String>) document.get(UserDetails.reviewKey);
                        List<Long> favourite = (List<Long>) document.get(UserDetails.favouriteKey);
                        if (!user.getDisplayName().equals(name) && textlength <= name.length()
                                && name.toLowerCase().trim().contains(compare.toLowerCase().trim())) {
                            UserDetails details = new UserDetails(id, name, area, cert, number, rating, review, favourite);
                            userDetailsList.add(details);
                        }
                    }
                }

                detailsAdapter.notifyDataSetChanged();
            }
        });

    }

    public UsersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_users, container, false);

    }
}
