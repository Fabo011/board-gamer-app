package com.example.boardgamerapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import com.example.boardgamerapp.library.MainActivityLibrary;

public class MainActivity extends AppCompatActivity {

    private EditText etPlayerName, etGroupName, etGroupPassword;
    private Switch toggleSwitch;
    private Button btnSubmit;

    private MainActivityLibrary library;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize library
        library = new MainActivityLibrary();

        // Initialize UI elements
        etPlayerName = findViewById(R.id.etPlayerName);
        etGroupName = findViewById(R.id.etGroupName);
        etGroupPassword = findViewById(R.id.etGroupPassword);
        toggleSwitch = findViewById(R.id.toggleSwitch);
        btnSubmit = findViewById(R.id.btnSubmit);

        // Handle form submission
        btnSubmit.setOnClickListener(view -> {
            String playerName = etPlayerName.getText().toString().trim();
            String groupName = etGroupName.getText().toString().trim();
            String groupPassword = etGroupPassword.getText().toString().trim();
            boolean isCreatingGroup = toggleSwitch.isChecked();

            library.handleFormSubmission(this, playerName, groupName, groupPassword, isCreatingGroup, this::navigateToDashboard);
        });
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
    }
}

