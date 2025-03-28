package com.example.boardgamerapp.library;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.boardgamerapp.database.Database;

public class UserStoryOptional {

    public static Spinner createCuisineDropdown(Context context, String groupName, String eventId) {
        Spinner cuisineDropdown = new Spinner(context);

        // Define cuisine options with a default placeholder
        String[] cuisines = {"Wähle Essensrichtung", "Italian", "Österreichisch", "Asiatisch", "Amerikanisch"};

        // Adapter for the dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, cuisines) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setTextColor(Color.WHITE);
                textView.setTextSize(16);
                return textView;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getDropDownView(position, convertView, parent);
                textView.setTextColor(Color.BLACK);
                textView.setTextSize(16);
                return textView;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cuisineDropdown.setAdapter(adapter);

        // Styling: white border
        GradientDrawable border = new GradientDrawable();
        border.setStroke(3, Color.WHITE);
        border.setCornerRadius(12);
        border.setColor(Color.TRANSPARENT);
        cuisineDropdown.setBackground(border);

        // Layout parameters
        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(20, 10, 20, 20);
        cuisineDropdown.setLayoutParams(params);

        // Handle selection changes
        cuisineDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) { // Ignore default selection
                    String selectedCuisine = cuisines[position];

                    // Update the database with the selected cuisine and eventId
                    Database dbHelper = new Database();
                    dbHelper.updateCuisineSelection(groupName, eventId, selectedCuisine);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        return cuisineDropdown;
    }
}
