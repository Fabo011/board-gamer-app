package com.example.boardgamerapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.boardgamerapp.database.Database;
import com.example.boardgamerapp.databinding.ActivityMainBinding;
import com.google.firebase.firestore.DocumentSnapshot;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private EditText etPlayerName, etGroupName, etGroupPassword;
    private Switch toggleSwitch;
    private Button btnSubmit;
    private Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize database
        database = new Database();

        // Initialize UI elements
        etPlayerName = findViewById(R.id.etPlayerName);
        etGroupName = findViewById(R.id.etGroupName);
        etGroupPassword = findViewById(R.id.etGroupPassword);
        toggleSwitch = findViewById(R.id.toggleSwitch);
        btnSubmit = findViewById(R.id.btnSubmit);

        // Handle form submission
        btnSubmit.setOnClickListener(view -> handleFormSubmission());
    }

    private void handleFormSubmission() {
        String playerName = etPlayerName.getText().toString().trim();
        String groupName = etGroupName.getText().toString().trim();
        String groupPassword = etGroupPassword.getText().toString().trim();
        boolean isCreatingGroup = toggleSwitch.isChecked();

        if (playerName.isEmpty() || groupName.isEmpty() || groupPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("DEBUG", "Submit button clicked");

        if (isCreatingGroup) {
            // Creating a new group
            Log.d("DEBUG", "Creating a new group");
            database.fetchGroup(groupName, task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Toast.makeText(this, "Group already exists!", Toast.LENGTH_SHORT).show();
                    } else {
                        // Create group with the first player
                        database.createGroup(groupName, groupPassword, playerName, "sample_fcm_token");
                        navigateToSecondFragment();
                    }
                } else {
                    Toast.makeText(this, "Error checking group", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Joining an existing group
            database.fetchGroup(groupName, task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    String storedPassword = task.getResult().getString("grouppassword");
                    if (storedPassword != null && storedPassword.equals(groupPassword)) {
                        navigateToSecondFragment();
                    } else {
                        Toast.makeText(this, "Incorrect group password!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Group not found!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void navigateToSecondFragment() {
        //NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        //navController.navigate(R.id.action_FirstFragment_to_SecondFragment);
        Toast.makeText(this, "Successfully joined the group.", Toast.LENGTH_SHORT).show();
    }
}
