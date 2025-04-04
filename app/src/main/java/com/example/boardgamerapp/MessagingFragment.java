package com.example.boardgamerapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.boardgamerapp.messaging.MessagingService;
import com.example.boardgamerapp.store.Store;

public class MessagingFragment extends Fragment {

    private EditText messageInput;
    private Button sendButton;
    private MessagingService messagingService;
    private Store store;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_messaging, container, false);

        messageInput = view.findViewById(R.id.messageInput);
        sendButton = view.findViewById(R.id.sendButton);
        messagingService = new MessagingService();
        store = new Store(getContext());
        String groupName = store.getGroupName();
        String playerName = store.getPlayerName();

        sendButton.setOnClickListener(v -> {
            String message = playerName + ": " + messageInput.getText().toString().trim();
            // Send the message to the group's topic
            messagingService.sendFCMMessage(getContext(), groupName, message, true);
            Toast.makeText(getContext(), getString(R.string.message_sent_info) + " " + groupName, Toast.LENGTH_SHORT).show();

            // Navigate back to DashboardActivity
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack(); // Go back to previous fragment
            }
        });

        return view;
    }
}