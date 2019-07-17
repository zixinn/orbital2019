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
import android.widget.Button;
import android.widget.EditText;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.example.orbital2019.intermediate.FilterSortActivity.chinese;
import static com.example.orbital2019.intermediate.FilterSortActivity.halal;
import static com.example.orbital2019.intermediate.FilterSortActivity.indian;
import static com.example.orbital2019.intermediate.FilterSortActivity.malay;
import static com.example.orbital2019.intermediate.FilterSortActivity.nearby;
import static com.example.orbital2019.intermediate.FilterSortActivity.other;
import static com.example.orbital2019.intermediate.FilterSortActivity.vegetarian;
import static com.example.orbital2019.intermediate.FilterSortActivity.western;

public class BuyFragment extends Fragment {

    private List<PostDetails> postDetailsList = new ArrayList<PostDetails>();
    private GridView gridView;
    private PostsAdapterBuy postsAdapter;
    private FirebaseAuth mAuth;
    private Button filterSortBtn;
    private EditText search;
    public static int sortType = 0;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        getActivity().setTitle("View Posts");

        filterSortBtn = view.findViewById(R.id.filterSort);
        filterSortBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FilterSortActivity.class);
                startActivityForResult(intent, PICK_CONTACT_REQUEST);
            }
        });

        search = view.findViewById(R.id.search);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (!s.toString().isEmpty())
                    searchPostData(s.toString());
                else
                    loadPostData();
            }
        });

        postsAdapter = new PostsAdapterBuy(getActivity().getApplicationContext(), postDetailsList);

        gridView = getActivity().findViewById(R.id.posts_list_view);

        gridView.setAdapter(postsAdapter);


        // on click listner
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view,
                                    int position, long id) {
                PostDetails postDetails = (PostDetails) parent.getItemAtPosition(position);
                Intent intent = new Intent(getActivity().getApplicationContext(), BuyViewDetailsActivity.class);
                intent.putExtra("postDetails", (Serializable) postDetails);
                startActivityForResult(intent, PICK_CONTACT_REQUEST);
            }
        });

        loadPostData();

    }

    private void loadPostData() {

        final FirebaseFirestore fs = FirebaseFirestore.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();

        if (nearby) {
            fs.collection(UserDetails.userDetailsKey).whereEqualTo(UserDetails.nameKey, user.getDisplayName())
                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    List<DocumentSnapshot> docs = task.getResult().getDocuments();
                    DocumentSnapshot doc = docs.get(0);
                    final String userArea = (String) doc.get(UserDetails.areaKey);
                    fs.collection(PostDetails.postDetailsKey).orderBy(PostDetails.inputKey, Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                            List<DocumentSnapshot> documents = task.getResult().getDocuments();

                            // clean up the list to prevent double copies
                            postDetailsList.removeAll(postDetailsList);

                            List<DocumentSnapshot> list = new ArrayList<>();

                            outer: for (DocumentSnapshot document : documents) {

                                if (document.contains(PostDetails.foodNameKey) && document.contains(PostDetails.descriptionKey) && document.contains(PostDetails.ingredientKey)
                                        && document.contains(PostDetails.priceKey) && document.contains(PostDetails.sellerKey)
                                        && document.contains(PostDetails.areaKey) && document.contains(PostDetails.typeKey) && document.contains(PostDetails.inputKey)
                                        && document.contains(PostDetails.dateKey)) {

                                    final String area = (String) document.get(PostDetails.areaKey);
                                    if (!userArea.equalsIgnoreCase(area)) {
                                        continue;
                                    }

                                    Map type = (Map) document.get(PostDetails.typeKey);

                                    if (!((!halal && !vegetarian) || (halal && ((boolean) type.get(PostDetails.halalKey))) ||
                                            (vegetarian && ((boolean) type.get(PostDetails.vegetarianKey))))) {
                                        continue;
                                    }

                                    if (!((!chinese && !malay && !indian && !western && !other) || (chinese && ((boolean) type.get(PostDetails.chineseKey))) ||
                                            (malay && ((boolean) type.get(PostDetails.malayKey))) || (indian && ((boolean) type.get(PostDetails.indianKey))) ||
                                            (western && ((boolean) type.get(PostDetails.westernKey))) || (other && ((boolean) type.get(PostDetails.otherKey))))) {
                                        continue;
                                    }

                                    list.add(document);

                                }
                            }

                            for (DocumentSnapshot document : list) {

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

                                if (!seller.equalsIgnoreCase(user.getUid())) {
                                    PostDetails details = new PostDetails(name, description, ingredient, price, seller, area,
                                            halal, vegetarian, chinese, malay, indian, western, other, image, input, date);
                                    postDetailsList.add(details);
                                }
                            }

                            if (sortType == 1) {
                                Collections.sort(postDetailsList, new Comparator<PostDetails>() {
                                    public int compare(PostDetails pd1, PostDetails pd2) {
                                        double p1 = Double.parseDouble(pd1.price);
                                        double p2 = Double.parseDouble(pd2.price);
                                        if (p1 < p2) {
                                            return -1;
                                        }
                                        if (p1 > p2) {
                                            return 1;
                                        }
                                        return 0;
                                    }
                                });
                            }
                            if (sortType == 2) {
                                Collections.sort(postDetailsList, new Comparator<PostDetails>() {
                                    public int compare(PostDetails pd1, PostDetails pd2) {
                                        double p1 = Double.parseDouble(pd1.price);
                                        double p2 = Double.parseDouble(pd2.price);
                                        if (p1 < p2) {
                                            return 1;
                                        }
                                        if (p1 > p2) {
                                            return -1;
                                        }
                                        return 0;
                                    }
                                });
                            }

                            postsAdapter.notifyDataSetChanged();
                        }
                    });

                }
            });

        } else {

            fs.collection(PostDetails.postDetailsKey).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    List<DocumentSnapshot> documents = task.getResult().getDocuments();

                    // clean up the list to prevent double copies
                    postDetailsList.removeAll(postDetailsList);

                    List<DocumentSnapshot> list = new ArrayList<>();

                    outer:
                    for (DocumentSnapshot document : documents) {

                        if (document.contains(PostDetails.foodNameKey) && document.contains(PostDetails.descriptionKey) && document.contains(PostDetails.ingredientKey)
                                && document.contains(PostDetails.priceKey) && document.contains(PostDetails.sellerKey)
                                && document.contains(PostDetails.areaKey) && document.contains(PostDetails.typeKey) && document.contains(PostDetails.inputKey)
                                && document.contains(PostDetails.dateKey)) {

                            Map type = (Map) document.get(PostDetails.typeKey);

                            if (!((!halal && !vegetarian) || (halal && ((boolean) type.get(PostDetails.halalKey))) ||
                                    (vegetarian && ((boolean) type.get(PostDetails.vegetarianKey))))) {
                                continue;
                            }

                            if (!((!chinese && !malay && !indian && !western && !other) || (chinese && ((boolean) type.get(PostDetails.chineseKey))) ||
                                    (malay && ((boolean) type.get(PostDetails.malayKey))) || (indian && ((boolean) type.get(PostDetails.indianKey))) ||
                                    (western && ((boolean) type.get(PostDetails.westernKey))) || (other && ((boolean) type.get(PostDetails.otherKey))))) {
                                continue;
                            }

                            list.add(document);

                        }
                    }

                    for (DocumentSnapshot document : list) {

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

                        if (!seller.equalsIgnoreCase(user.getUid())) {
                            PostDetails details = new PostDetails(name, description, ingredient, price, seller, area,
                                    halal, vegetarian, chinese, malay, indian, western, other, image, input, date);
                            postDetailsList.add(details);
                        }

                    }

                    if (sortType == 0) {
                        Collections.sort(postDetailsList, new Comparator<PostDetails>() {
                            public int compare(PostDetails pd1, PostDetails pd2) {
                                return (int) (pd2.getInput() - pd1.getInput());
                            }
                        });
                    }
                    if (sortType == 1) {
                        Collections.sort(postDetailsList, new Comparator<PostDetails>() {
                            public int compare(PostDetails pd1, PostDetails pd2) {
                                double p1 = Double.parseDouble(pd1.price);
                                double p2 = Double.parseDouble(pd2.price);
                                if (p1 < p2) {
                                    return -1;
                                }
                                if (p1 > p2) {
                                    return 1;
                                }
                                return 0;
                            }
                        });
                    }
                    if (sortType == 2) {
                        Collections.sort(postDetailsList, new Comparator<PostDetails>() {
                            public int compare(PostDetails pd1, PostDetails pd2) {
                                double p1 = Double.parseDouble(pd1.price);
                                double p2 = Double.parseDouble(pd2.price);
                                if (p1 < p2) {
                                    return 1;
                                }
                                if (p1 > p2) {
                                    return -1;
                                }
                                return 0;
                            }
                        });
                    }

                    postsAdapter.notifyDataSetChanged();
                }
            });
        }

    }


    private void searchPostData(String filter) {

        final FirebaseFirestore fs = FirebaseFirestore.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();
        final int textlength = filter.length();
        final String compare = filter;

        if (nearby) {
            fs.collection(UserDetails.userDetailsKey).whereEqualTo(UserDetails.nameKey, user.getDisplayName())
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    List<DocumentSnapshot> docs = task.getResult().getDocuments();
                    DocumentSnapshot doc = docs.get(0);
                    final String userArea = (String) doc.get(UserDetails.areaKey);
                    fs.collection(PostDetails.postDetailsKey).orderBy(PostDetails.inputKey, Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                            List<DocumentSnapshot> documents = task.getResult().getDocuments();

                            // clean up the list to prevent double copies
                            postDetailsList.removeAll(postDetailsList);

                            List<DocumentSnapshot> list = new ArrayList<>();

                            outer: for (DocumentSnapshot document : documents) {

                                if (document.contains(PostDetails.foodNameKey) && document.contains(PostDetails.descriptionKey) && document.contains(PostDetails.ingredientKey)
                                        && document.contains(PostDetails.priceKey) && document.contains(PostDetails.sellerKey)
                                        && document.contains(PostDetails.areaKey) && document.contains(PostDetails.typeKey) && document.contains(PostDetails.inputKey)
                                        && document.contains(PostDetails.dateKey)) {

                                    final String area = (String) document.get(PostDetails.areaKey);
                                    if (!userArea.equalsIgnoreCase(area)) {
                                        continue;
                                    }

                                    Map type = (Map) document.get(PostDetails.typeKey);

                                    if (!((!halal && !vegetarian) || (halal && ((boolean) type.get(PostDetails.halalKey))) ||
                                            (vegetarian && ((boolean) type.get(PostDetails.vegetarianKey))))) {
                                        continue;
                                    }

                                    if (!((!chinese && !malay && !indian && !western && !other) || (chinese && ((boolean) type.get(PostDetails.chineseKey))) ||
                                            (malay && ((boolean) type.get(PostDetails.malayKey))) || (indian && ((boolean) type.get(PostDetails.indianKey))) ||
                                            (western && ((boolean) type.get(PostDetails.westernKey))) || (other && ((boolean) type.get(PostDetails.otherKey))))) {
                                        continue;
                                    }

                                    list.add(document);

                                }
                            }

                            for (DocumentSnapshot document : list) {

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

                                if (!seller.equals(user.getUid()) && textlength <= name.length()
                                        && name.toLowerCase().trim().contains(compare.toLowerCase().trim())) {
                                    PostDetails details = new PostDetails(name, description, ingredient, price, seller, area,
                                            halal, vegetarian, chinese, malay, indian, western, other, image, input, date);
                                    postDetailsList.add(details);
                                }
                            }

                            if (sortType == 1) {
                                Collections.sort(postDetailsList, new Comparator<PostDetails>() {
                                    public int compare(PostDetails pd1, PostDetails pd2) {
                                        double p1 = Double.parseDouble(pd1.price);
                                        double p2 = Double.parseDouble(pd2.price);
                                        if (p1 < p2) {
                                            return -1;
                                        }
                                        if (p1 > p2) {
                                            return 1;
                                        }
                                        return 0;
                                    }
                                });
                            }
                            if (sortType == 2) {
                                Collections.sort(postDetailsList, new Comparator<PostDetails>() {
                                    public int compare(PostDetails pd1, PostDetails pd2) {
                                        double p1 = Double.parseDouble(pd1.price);
                                        double p2 = Double.parseDouble(pd2.price);
                                        if (p1 < p2) {
                                            return 1;
                                        }
                                        if (p1 > p2) {
                                            return -1;
                                        }
                                        return 0;
                                    }
                                });
                            }

                            postsAdapter.notifyDataSetChanged();
                        }
                    });

                }
            });

        } else {

            fs.collection(PostDetails.postDetailsKey).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {

                    List<DocumentSnapshot> documents = task.getResult().getDocuments();

                    // clean up the list to prevent double copies
                    postDetailsList.removeAll(postDetailsList);

                    List<DocumentSnapshot> list = new ArrayList<>();

                    outer:
                    for (DocumentSnapshot document : documents) {

                        if (document.contains(PostDetails.foodNameKey) && document.contains(PostDetails.descriptionKey) && document.contains(PostDetails.ingredientKey)
                                && document.contains(PostDetails.priceKey) && document.contains(PostDetails.sellerKey)
                                && document.contains(PostDetails.areaKey) && document.contains(PostDetails.typeKey) && document.contains(PostDetails.inputKey)
                                && document.contains(PostDetails.dateKey)) {

                            Map type = (Map) document.get(PostDetails.typeKey);

                            if (!((!halal && !vegetarian) || (halal && ((boolean) type.get(PostDetails.halalKey))) ||
                                    (vegetarian && ((boolean) type.get(PostDetails.vegetarianKey))))) {
                                continue;
                            }

                            if (!((!chinese && !malay && !indian && !western && !other) || (chinese && ((boolean) type.get(PostDetails.chineseKey))) ||
                                    (malay && ((boolean) type.get(PostDetails.malayKey))) || (indian && ((boolean) type.get(PostDetails.indianKey))) ||
                                    (western && ((boolean) type.get(PostDetails.westernKey))) || (other && ((boolean) type.get(PostDetails.otherKey))))) {
                                continue;
                            }

                            list.add(document);

                        }
                    }

                    for (DocumentSnapshot document : list) {

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

                        if (!seller.equals(user.getUid()) && textlength <= name.length()
                                && name.toLowerCase().trim().contains(compare.toLowerCase().trim())) {
                            PostDetails details = new PostDetails(name, description, ingredient, price, seller, area,
                                    halal, vegetarian, chinese, malay, indian, western, other, image, input, date);
                            postDetailsList.add(details);
                        }

                    }

                    if (sortType == 0) {
                        Collections.sort(postDetailsList, new Comparator<PostDetails>() {
                            public int compare(PostDetails pd1, PostDetails pd2) {
                                return (int) (pd2.getInput() - pd1.getInput());
                            }
                        });
                    }
                    if (sortType == 1) {
                        Collections.sort(postDetailsList, new Comparator<PostDetails>() {
                            public int compare(PostDetails pd1, PostDetails pd2) {
                                double p1 = Double.parseDouble(pd1.price);
                                double p2 = Double.parseDouble(pd2.price);
                                if (p1 < p2) {
                                    return -1;
                                }
                                if (p1 > p2) {
                                    return 1;
                                }
                                return 0;
                            }
                        });
                    }
                    if (sortType == 2) {
                        Collections.sort(postDetailsList, new Comparator<PostDetails>() {
                            public int compare(PostDetails pd1, PostDetails pd2) {
                                double p1 = Double.parseDouble(pd1.price);
                                double p2 = Double.parseDouble(pd2.price);
                                if (p1 < p2) {
                                    return 1;
                                }
                                if (p1 > p2) {
                                    return -1;
                                }
                                return 0;
                            }
                        });
                    }

                    postsAdapter.notifyDataSetChanged();
                }
            });
        }

    }

    static final int PICK_CONTACT_REQUEST = 1;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == PICK_CONTACT_REQUEST) {
            // Make sure the request was successful
            loadPostData();

        }
    }

    public BuyFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_buy, container, false);

    }

}
