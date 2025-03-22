package com.example.boardgamerapp.store;

import android.content.Context;
import android.content.SharedPreferences;

public class Store {

    private static final String PREFS_NAME = "GamePrefs";
    private static final String GROUP_NAME_KEY = "groupName";
    private SharedPreferences sharedPreferences;

    public Store(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // Save group name to SharedPreferences
    public void saveGroupName(String groupName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(GROUP_NAME_KEY, groupName);
        editor.apply(); // Apply changes
    }

    // Retrieve the saved group name from SharedPreferences
    public String getGroupName() {
        return sharedPreferences.getString(GROUP_NAME_KEY, "DefaultGroup"); // Return default if not set
    }
}

