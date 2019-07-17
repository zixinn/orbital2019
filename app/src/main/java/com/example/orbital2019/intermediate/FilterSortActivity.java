package com.example.orbital2019.intermediate;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.orbital2019.R;

public class FilterSortActivity extends AppCompatActivity {

    private CheckBox halalCheckBox, vegetarianCheckBox, chineseCheckBox,
            malayCheckBox, indianCheckBox, westernCheckBox, otherCheckBox, nearbyCheckBox;
    public static boolean halal, vegetarian, chinese, malay, indian, western, other, nearby;
    private RadioGroup radioSort;
    private RadioButton lowestPriceBtn, highestPriceBtn, recentBtn;
    private Button doneBtn;

    public static void reset() {
        halal = false;
        vegetarian = false;
        chinese = false;
        malay = false;
        indian = false;
        western = false;
        other = false;
        nearby = false;
        BuyFragment.sortType = 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtersort);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Filter/Sort");

        halalCheckBox = findViewById(R.id.halal);
        vegetarianCheckBox = findViewById(R.id.vegetarian);
        chineseCheckBox = findViewById(R.id.chinese);
        malayCheckBox = findViewById(R.id.malay);
        indianCheckBox = findViewById(R.id.indian);
        westernCheckBox = findViewById(R.id.western);
        otherCheckBox = findViewById(R.id.other);
        nearbyCheckBox = findViewById(R.id.nearby);

        radioSort = findViewById(R.id.radioSort);
        lowestPriceBtn = findViewById(R.id.lowestPrice);
        highestPriceBtn = findViewById(R.id.highestPrice);
        recentBtn = findViewById(R.id.recent);
        doneBtn = findViewById(R.id.doneBtn);

        halalCheckBox.setChecked(halal);
        vegetarianCheckBox.setChecked(vegetarian);
        chineseCheckBox.setChecked(chinese);
        malayCheckBox.setChecked(malay);
        indianCheckBox.setChecked(indian);
        westernCheckBox.setChecked(western);
        otherCheckBox.setChecked(other);
        nearbyCheckBox.setChecked(nearby);
        if (BuyFragment.sortType == 1) {
            lowestPriceBtn.setChecked(true);
        } else if (BuyFragment.sortType == 2) {
            highestPriceBtn.setChecked(true);
        } else {
            recentBtn.setChecked(true);
        }

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter();
                sort();
                finish();
            }
        });
    }

    private void filter() {
        halal = halalCheckBox.isChecked();
        vegetarian = vegetarianCheckBox.isChecked();
        chinese = chineseCheckBox.isChecked();
        malay = malayCheckBox.isChecked();
        indian = indianCheckBox.isChecked();
        western = westernCheckBox.isChecked();
        other = otherCheckBox.isChecked();
        nearby = nearbyCheckBox.isChecked();
    }

    private void sort() {
        int selectedId = radioSort.getCheckedRadioButtonId();
        if (selectedId == R.id.lowestPrice) {
            BuyFragment.sortType = 1;
        } else if (selectedId == R.id.highestPrice) {
            BuyFragment.sortType = 2;
        } else {
            BuyFragment.sortType = 0;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

}
