package com.example.orbital2019.intermediate.message;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;


import com.example.orbital2019.R;
import com.example.orbital2019.intermediate.RateReviewActivity;
import com.example.orbital2019.intermediate.model.Chat;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessageActivity extends AppCompatActivity {

    public static int HOHO = 7;
    DatabaseReference ref;
    ImageButton btn_send;
    Button btn_transact;
    EditText text_send;

    FirebaseUser user;
    String chatTo, food, seller, foodid, sellerid, chatToid;

    MessageAdapter messageAdapter;
    List<Chat> chats;

    RecyclerView recyclerView;

    ValueEventListener seenListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        chatTo = getIntent().getStringExtra("personName");
        chatToid = getIntent().getStringExtra("personID");
        food = getIntent().getStringExtra("foodName");
        foodid = getIntent().getStringExtra("foodID");
        seller = getIntent().getStringExtra("sellerName");
        sellerid = getIntent().getStringExtra("sellerID");


        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(chatTo);
        getSupportActionBar().setSubtitle(food);

        btn_transact = findViewById(R.id.theButton);
        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.text_send);
        recyclerView = findViewById(R.id.recycleView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(getApplicationContext());
        manager.setStackFromEnd(true);
        recyclerView.setLayoutManager(manager);


        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user.getUid().equals(sellerid)) {
            btn_transact.setVisibility(View.VISIBLE);
            btn_transact.setText("Transaction successful");
        }

        if (btn_transact.getTag() == null) {
            btn_transact.setTag("tst");
        }

        btn_transact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btn_transact.getTag().equals("tst")) {
                    FirebaseDatabase.getInstance().getReference().child("Transact").child(user.getUid())
                            .child(chatToid+foodid).setValue("fbk");
                    FirebaseDatabase.getInstance().getReference().child("Transact").child(chatToid)
                            .child(user.getUid()+foodid).setValue("fbk");
                    sendMessage(user.getUid(), chatToid, "TRANSACTION SUCCESSFUL!");
                } else if (btn_transact.getTag().equals("fbk")) {
                    FirebaseFirestore fs = FirebaseFirestore.getInstance();
                    fs.collection(UserDetails.userDetailsKey).whereEqualTo(UserDetails.idKey, chatToid).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            List<DocumentSnapshot> documents = task.getResult().getDocuments();
                            DocumentSnapshot document = documents.get(0);
                            String id = (String) document.get(UserDetails.idKey);
                            String name = (String) document.get(UserDetails.nameKey);
                            String area = (String) document.get(UserDetails.areaKey);
                            String cert = (String) document.get(UserDetails.certKey);
                            long number = (long) document.get(UserDetails.numberKey);
                            double rating = (double) document.get(UserDetails.ratingKey);
                            List<String> review = (List<String>) document.get(UserDetails.reviewKey);
                            List<Long> favourite = (List<Long>) document.get(UserDetails.favouriteKey);
                            UserDetails userDetails = new UserDetails(id, name, area, cert, number, rating, review, favourite);
                            Intent intent = new Intent(MessageActivity.this, RateReviewActivity.class);
                            intent.putExtra("userDetails", (Serializable) userDetails);
                            startActivityForResult(intent, HOHO);
                        }
                    });

                } else {
                    btn_transact.setVisibility(View.GONE);
                }
            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = text_send.getText().toString();
                if (!msg.equals("")) {
                    sendMessage(user.getUid(), chatToid, msg);
                } else {
                    Toast.makeText(MessageActivity.this, "You can't send empty message", Toast.LENGTH_SHORT).show();
                }
                text_send.setText("");
            }
        });

        readMessage(user.getUid(), chatToid);

        seenMessage(chatToid);
        transact(btn_transact);

    }

    private void seenMessage(final String otherUser) {
        ref = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if ((chat.getReceiver().equals(user.getUid()) && chat.getSender().equals(otherUser))
                            && chat.getFood().equals(foodid) && chat.getSeller().equals(sellerid)) {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("isseen", true);
                        snapshot.getRef().updateChildren(map);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendMessage(String sender, String receiver, String message) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> map = new HashMap<>();
        map.put("sender", sender);
        map.put("receiver", receiver);
        map.put("message", message);
        map.put("isseen", false);
        map.put("food", foodid);
        map.put("seller", sellerid);
        reference.child("Chats").push().setValue(map);

        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(user.getUid())
                .child(chatToid + foodid);
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()) {

                    chatRef.child("name").setValue(chatToid);
                    chatRef.child("food").setValue(foodid);
                    chatRef.child("seller").setValue(sellerid);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        final DatabaseReference chatRef1 = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(chatToid)
                .child(user.getUid() + foodid);
        chatRef1.child("name").setValue(user.getUid());
        chatRef1.child("food").setValue(foodid);
        chatRef1.child("seller").setValue(sellerid);


    }

    private void transact(final Button button){

        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Transact")
                .child(user.getUid());
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(chatToid+foodid).exists()){
                    String string = dataSnapshot.child(chatToid+foodid).getValue(String.class);
                    if (string != null && string.equals("tst")) {
                        button.setText("Transaction successful");
                        button.setTag("tst");
                    } else if (string != null && string.equals("fbk")) {
                        button.setText("Leave feedback");
                        button.setTag("fbk");
                        button.setVisibility(View.VISIBLE);
                    } else if (string != null && string.equals("nth")) {
                        button.setTag("nth");
                        button.setVisibility(View.GONE);
                    }
                } else if (!button.getTag().equals("nth")){
                    button.setTag("tst");
                } else {
                    button.setTag("nth");
                    button.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void readMessage(final String my, final String otheruser) {
        chats = new ArrayList<>();
        ref = FirebaseDatabase.getInstance().getReference("Chats");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chats.clear();
                for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if ((chat.getReceiver().equals(my) && chat.getSender().equals(otheruser) ||
                            chat.getReceiver().equals(otheruser) && chat.getSender().equals(my))
                            && chat.getSeller().equals(sellerid) && chat.getFood().equals(foodid)) {
                        chats.add(chat);
                    }

                    messageAdapter = new MessageAdapter(MessageActivity.this, chats);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        ref.removeEventListener(seenListener);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == HOHO && resultCode == RESULT_OK) {
            // Make sure the request was successful
            btn_transact.setVisibility(View.GONE);
            btn_transact.setTag("nth");
            FirebaseDatabase.getInstance().getReference().child("Transact").child(user.getUid())
                    .child(chatToid+foodid).setValue("nth");

        }
    }
}
