package com.example.orbital2019.intermediate;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.orbital2019.R;
import com.example.orbital2019.intermediate.message.MessageActivity;
import com.example.orbital2019.intermediate.model.Chat;
import com.example.orbital2019.intermediate.model.Pair;
import com.example.orbital2019.intermediate.model.PostDetails;
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

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context context;
    private List<UserDetails> users;
    private List<Pair> pairs;

    String theLastMessage;
    int unread;

    public UserAdapter(Context context, List<UserDetails> users, List<Pair> pairs) {
        this.context = context;
        this.users = users;
        this.pairs = pairs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_list_row, parent, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Pair pair = pairs.get(position);
        final ViewHolder hold = holder;
        lastMessage(pair.getPerson(), holder.last_msg, pair.getMeal(), pair.getSeller());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(UserDetails.userDetailsKey).whereEqualTo(UserDetails.idKey, pair.getPerson()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<DocumentSnapshot> docs = task.getResult().getDocuments();
                DocumentSnapshot document = docs.get(0);
                String name = (String) document.get(UserDetails.nameKey);
                hold.username.setText(name);
            }
        });
        db.collection(PostDetails.postDetailsKey).whereEqualTo(PostDetails.inputKey, Long.parseLong(pair.getMeal())).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<DocumentSnapshot> docs = task.getResult().getDocuments();
                DocumentSnapshot document = docs.get(0);
                String name = (String) document.get(PostDetails.foodNameKey);
                hold.foodname.setText(name);
            }
        });

        setupBadge(pair.getPerson(), holder.chat_badge, pair.getMeal(), pair.getSeller());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(context, MessageActivity.class);
                intent.putExtra("personName", hold.username.getText().toString());
                intent.putExtra("personID", pair.getPerson());
                intent.putExtra("foodName", hold.foodname.getText().toString());
                intent.putExtra("foodID", pair.getMeal());
                intent.putExtra("sellerID", pair.getSeller());
                FirebaseFirestore fs = FirebaseFirestore.getInstance();
                fs.collection(UserDetails.userDetailsKey).whereEqualTo(UserDetails.idKey, pair.getSeller()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<DocumentSnapshot> docs = task.getResult().getDocuments();
                        DocumentSnapshot document = docs.get(0);
                        String name = (String) document.get(UserDetails.nameKey);
                        intent.putExtra("sellerName", name);
                    }
                });
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView username;
        public TextView last_msg;
        public TextView chat_badge;
        public TextView foodname;

        public ViewHolder(View itemView) {
            super(itemView);

            foodname = itemView.findViewById(R.id.food);
            username = itemView.findViewById(R.id.username);
            last_msg = itemView.findViewById(R.id.last_msg);
            chat_badge = itemView.findViewById(R.id.chat_badge);
        }

    }

    private void lastMessage(final String otheruser, final TextView last_msg, final String food, final String sell) {
        theLastMessage = "default";
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (user != null && chat != null) {
                        if (((chat.getReceiver().equals(user.getUid()) && chat.getSender().equals(otheruser))||
                                (chat.getReceiver().equals(otheruser) && chat.getSender().equals(user.getUid())))
                                && chat.getFood().equals(food) && chat.getSeller().equals(sell)) {
                            theLastMessage = chat.getMessage();
                        }
                    }
                }

                switch (theLastMessage) {
                    case "default":
                        last_msg.setText("No Message");
                        break;

                    default:
                        last_msg.setText(theLastMessage);
                        break;
                }
                theLastMessage = "default";

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setupBadge(final String otherUser, final TextView badge, final String food, final String sell) {
        unread = 0;
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Chats");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(user.getUid()) && !chat.isIsseen() && chat.getSender().equals(otherUser)
                            && chat.getFood().equals(food) && chat.getSeller().equals(sell)) {
                        unread++;
                    }

                    if (badge != null) {
                        if (unread == 0) {
                            if (badge.getVisibility() != View.GONE) {
                                badge.setVisibility(View.GONE);

                            }
                        } else {
                            badge.setText(String.valueOf(Math.min(unread, 99)));
                            if (badge.getVisibility() != View.VISIBLE) {
                                badge.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }

                unread = 0;

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

}
