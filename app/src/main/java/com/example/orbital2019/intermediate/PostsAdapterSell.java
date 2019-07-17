package com.example.orbital2019.intermediate;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.orbital2019.R;
import com.example.orbital2019.intermediate.model.PostDetails;

import java.io.Serializable;
import java.util.List;

public class PostsAdapterSell extends ArrayAdapter<PostDetails> {
    private final Context context;
    private final List<PostDetails> values;
    private final Fragment fragment;

    public PostsAdapterSell(Fragment fragment, Context context, List<PostDetails> values) {
        super(context, R.layout.posts_list_row_sell, values);
        this.fragment = fragment;
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.posts_list_row_sell, parent, false);

        TextView foodnameTV = rowView.findViewById(R.id.foodname);
        TextView priceTV = rowView.findViewById(R.id.pricename);
        TextView typeTV = rowView.findViewById(R.id.typename);
        ImageView imageTV = rowView.findViewById(R.id.imagename);
        ImageView deleteBtn = rowView.findViewById(R.id.deleteBtn);
        ImageView editBtn = rowView.findViewById(R.id.editBtn);

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

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PostDetails postDetails = (PostDetails) values.get(position);
                Intent intent = new Intent(fragment.getActivity().getApplicationContext(), SellUpdatePostActivity.class);
                intent.putExtra("postDetails", (Serializable) postDetails);
                fragment.startActivityForResult(intent, SellFragment.PICK_CONTACT_REQUEST);
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getContext());
                builder.setTitle("Delete post");
                builder.setMessage("Are you sure you want to delete?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        values.get(position).deleteEntry();
                        Fragment frag = new SellFragment();
                        FragmentTransaction ft = fragment.getActivity().getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.content_frame, frag);
                        ft.commit();
                        Toast.makeText(getContext(), "Post deleted successfully!", Toast.LENGTH_LONG).show();

                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        dialogInterface.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });

        return rowView;
    }


}