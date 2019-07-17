package com.example.orbital2019.intermediate;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.orbital2019.R;
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

public class PostsAdapterBuy extends ArrayAdapter<PostDetails> {
    private final Context context;
    private final List<PostDetails> values;

    public PostsAdapterBuy(Context context, List<PostDetails> values) {
        super(context, R.layout.posts_list_row_buy, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.posts_list_row_buy, parent, false);

        TextView foodnameTV = rowView.findViewById(R.id.foodname);
        TextView priceTV = rowView.findViewById(R.id.pricename);
        TextView typeTV = rowView.findViewById(R.id.typename);
        ImageView imageTV = rowView.findViewById(R.id.imagename);
        final ImageView favBtn = rowView.findViewById(R.id.favBtn);
        TextView dateTV = rowView.findViewById(R.id.date);
        dateTV.setText(values.get(position).getDate());
        foodnameTV.setText(values.get(position).getFoodName());

        StringBuffer result = new StringBuffer();
        if (values.get(position).getHalal()) {
            result.append("Halal・");
        }
        if (values.get(position).getVegetarian()) {
            result.append("Vegetarian・");
        }
        if (values.get(position).getChinese()) {
            result.append("Chinese・");
        }
        if (values.get(position).getMalay()) {
            result.append("Malay・");
        }
        if (values.get(position).getIndian()) {
            result.append("Indian・");
        }
        if (values.get(position).getWestern()) {
            result.append("Western・");
        }
        if (values.get(position).getOther()) {
            result.append("Other・");
        }
        typeTV.setText(result);

        priceTV.setText("$" + String.format("%.2f", Double.parseDouble(values.get(position).getPrice())));
        Glide.with(getContext()).load(values.get(position).getImage()).into(imageTV);

        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final FirebaseFirestore fs = FirebaseFirestore.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();

        final PostDetails postDetails = (PostDetails) values.get(position);
        final long input = postDetails.getInput();

        isLiked(values.get(position).getInput()+"", favBtn);
        favBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (favBtn.getTag().equals("unlike")) {
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(postDetails.getInput()+"")
                            .child(user.getUid()).setValue(true);
                    fs.collection(UserDetails.userDetailsKey).whereEqualTo(UserDetails.idKey, user.getUid())
                            .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            List<DocumentSnapshot> docs = task.getResult().getDocuments();
                            DocumentSnapshot document = docs.get(0);
                            String id = (String) document.get(UserDetails.idKey);
                            String name = (String) document.get(UserDetails.nameKey);
                            String area = (String) document.get(UserDetails.areaKey);
                            String cert = (String) document.get(UserDetails.certKey);
                            long number = (long) document.get(UserDetails.numberKey);
                            double rating = (double) document.get(UserDetails.ratingKey);
                            List<String> review = (List<String>) document.get(UserDetails.reviewKey);
                            List<Long> favourite = (List<Long>) document.get(UserDetails.favouriteKey);

                            UserDetails oldUser = new UserDetails(id, name, area, cert, number, rating, review, favourite);
                            oldUser.deleteEntry();

                            favourite.add(input);
                            UserDetails newUser = new UserDetails(id, name, area, cert, number, rating, review, favourite);
                            newUser.createEntry();

                        }
                    });

                } else {
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(postDetails.getInput()+"")
                            .child(user.getUid()).removeValue();
                    fs.collection(UserDetails.userDetailsKey).whereEqualTo(UserDetails.idKey, user.getUid())
                            .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            List<DocumentSnapshot> docs = task.getResult().getDocuments();
                            DocumentSnapshot document = docs.get(0);
                            String id = (String) document.get(UserDetails.idKey);
                            String name = (String) document.get(UserDetails.nameKey);
                            String area = (String) document.get(UserDetails.areaKey);
                            String cert = (String) document.get(UserDetails.certKey);
                            long number = (long) document.get(UserDetails.numberKey);
                            double rating = (double) document.get(UserDetails.ratingKey);
                            List<String> review = (List<String>) document.get(UserDetails.reviewKey);
                            List<Long> favourite = (List<Long>) document.get(UserDetails.favouriteKey);

                            UserDetails oldUser = new UserDetails(id, name, area, cert, number, rating, review, favourite);
                            oldUser.deleteEntry();

                            favourite.remove(input);
                            UserDetails newUser = new UserDetails(id, name, area, cert, number, rating, review, favourite);
                            newUser.createEntry();

                        }
                    });

                }

            }
        });

        return rowView;
    }

    private void isLiked(final String postid, final ImageView imageView){

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Likes").child(postid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(firebaseUser.getUid()).exists()){
                    imageView.setBackgroundResource(R.drawable.like);
                    imageView.setTag("like");
                } else{
                    imageView.setBackgroundResource(R.drawable.unlike);
                    imageView.setTag("unlike");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}