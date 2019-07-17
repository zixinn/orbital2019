package com.example.orbital2019.intermediate;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.orbital2019.R;

import java.util.List;

public class ReviewsAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final List<String> values;

    public ReviewsAdapter(Context context, List<String> values) {
        super(context, R.layout.reviews_list_row, values);
        this.context = context;
        this.values = values;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.reviews_list_row, parent, false);
        rowView.setEnabled(false);
        rowView.setOnClickListener(null);

        TextView reviewTV = rowView.findViewById(R.id.review);
        if (position%2 == 1) {
            rowView.setBackgroundColor(0xFAFAFA);
        } else {
            rowView.setBackgroundColor(Color.WHITE);
        }

        reviewTV.setText(values.get(position));

        return rowView;
    }
}
