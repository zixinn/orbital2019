package com.example.orbital2019.intermediate;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;

import com.example.orbital2019.R;
import com.example.orbital2019.intermediate.model.Chatlist;
import com.example.orbital2019.intermediate.model.Pair;
import com.example.orbital2019.intermediate.model.UserDetails;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ShowChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UserAdapter detailsAdapter;
    private List<UserDetails> users;
    private List<Pair> pairs;
    private DatabaseReference ref;
    private List<Chatlist> userList;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_chat);
        setTitle("Chats");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Chats");

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        user = FirebaseAuth.getInstance().getCurrentUser();
        userList = new ArrayList<>();

        ref = FirebaseDatabase.getInstance().getReference("Chatlist").child(user.getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Chatlist chatlist = snapshot.getValue(Chatlist.class);
                    userList.add(chatlist);
                }

                chatlist();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void chatlist() {
        users = new ArrayList<>();
        pairs = new ArrayList<>();
        FirebaseFirestore fs = FirebaseFirestore.getInstance();
        fs.collection(UserDetails.userDetailsKey).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                List<DocumentSnapshot> documents = task.getResult().getDocuments();

                // clean up the list to prevent double copies
                users.clear();
                pairs.clear();

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

                        for (Chatlist chatlist : userList) {
                            if (id.equals(chatlist.getName())) {
                                UserDetails details = new UserDetails(id, name, area, cert, number, rating, review, favourite);
                                users.add(details);
                                pairs.add(new Pair(id, chatlist.getFood(), chatlist.getSeller()));
                            }
                        }

                    }
                }
                detailsAdapter = new UserAdapter(ShowChatActivity.this, users, pairs);
                recyclerView.setAdapter(detailsAdapter);
            }
        });
    }

    private void searchchatlist(String filter) {
        users = new ArrayList<>();
        pairs = new ArrayList<>();
        FirebaseFirestore fs = FirebaseFirestore.getInstance();
        final int textlength = filter.length();
        final String compare = filter;

        fs.collection(UserDetails.userDetailsKey).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                List<DocumentSnapshot> documents = task.getResult().getDocuments();

                // clean up the list to prevent double copies
                users.clear();
                pairs.clear();

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

                        for (Chatlist chatlist : userList) {
                            if (id.equals(chatlist.getName()) && textlength <= name.length()
                                    && name.toLowerCase().trim().contains(compare.toLowerCase().trim())) {
                                UserDetails details = new UserDetails(id, name, area, cert, number, rating, review, favourite);
                                users.add(details);
                                pairs.add(new Pair(id, chatlist.getFood(), chatlist.getSeller()));
                            }
                        }

                    }
                }
                detailsAdapter = new UserAdapter(ShowChatActivity.this, users, pairs);
                recyclerView.setAdapter(detailsAdapter);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_chat, menu);
        final MenuItem menuItem = menu.findItem(R.id.action_search);

        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setQueryHint("Search chat...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                if (!newText.isEmpty()) {
                    ref = FirebaseDatabase.getInstance().getReference("Chatlist").child(user.getUid());
                    ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            userList.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Chatlist chatlist = snapshot.getValue(Chatlist.class);
                                userList.add(chatlist);
                            }
                            searchchatlist(newText);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else {
                    ref = FirebaseDatabase.getInstance().getReference("Chatlist").child(user.getUid());
                    ref.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            userList.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Chatlist chatlist = snapshot.getValue(Chatlist.class);
                                userList.add(chatlist);
                            }
                            chatlist();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                return false;
            }
        });
        return true;
    }
}
