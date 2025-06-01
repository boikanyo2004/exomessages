// Updated FriendsActivity.java - Main friends activity with messaging capability
package com.example.exomessage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FriendsActivity extends AppCompatActivity {

    private LinearLayout friendsContainer;
    private Button addFriendsButton;
    private Button friendsOfFriendsButton;
    private Button refreshButton;
    private TextView noFriendsText;

    private RequestQueue requestQueue;
    private static final String GET_FRIENDS_URL = "https://lamp.ms.wits.ac.za/home/s2543085/get_friends.php";

    private int currentUserId;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        friendsContainer = findViewById(R.id.friendsContainer);
        addFriendsButton = findViewById(R.id.addFriendsButton);
        friendsOfFriendsButton = findViewById(R.id.friendsOfFriendsButton);
        refreshButton = findViewById(R.id.refreshButton);
        noFriendsText = findViewById(R.id.noFriendsText);
        Button viewStatusButton = findViewById(R.id.viewStatusButton);
        Button postStatusButton = findViewById(R.id.postStatusButton);

        requestQueue = Volley.newRequestQueue(this);

        // Get current user ID from SharedPreferences or Intent
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("userId", 1); // Default to 1 if not found

        addFriendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FriendsActivity.this, AddFriendsActivity.class);
                intent.putExtra("userId", currentUserId);
                startActivity(intent);
            }
        });

        // Friends of Friends button click listener
        friendsOfFriendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FriendsActivity.this, FriendsOfFriendsActivity.class);
                intent.putExtra("userId", currentUserId);
                startActivity(intent);
            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFriends();
            }
        });

        viewStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FriendsActivity.this, StatusActivity.class);
                startActivity(intent);
            }
        });

        postStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FriendsActivity.this, PostStatusActivity.class);
                startActivity(intent);
            }
        });

        // Load friends when activity starts
        loadFriends();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh friends list when returning from add friends activity
        loadFriends();
    }

    private void loadFriends() {
        // Create parameters for POST request
        Map<String, String> params = new HashMap<>();
        params.put("userId", String.valueOf(currentUserId));

        // Create JSON object from parameters
        JSONObject parameters = new JSONObject(params);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                GET_FRIENDS_URL,
                parameters,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            String message = response.getString("message");

                            if (status.equals("success")) {
                                JSONArray friends = response.getJSONArray("friends");
                                displayFriends(friends);

                                if (friends.length() == 0) {
                                    Toast.makeText(FriendsActivity.this, "No friends found. Click 'Add Friends' to start connecting!", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(FriendsActivity.this, message, Toast.LENGTH_SHORT).show();
                                showNoFriends();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(FriendsActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                            showNoFriends();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("FriendsActivity", "Error: " + error.getMessage());
                        Toast.makeText(FriendsActivity.this, "Failed to load friends: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        showNoFriends();
                    }
                }
        );

        requestQueue.add(jsonObjectRequest);
    }

    private void displayFriends(JSONArray friends) {
        friendsContainer.removeAllViews();
        noFriendsText.setVisibility(View.GONE);

        if (friends.length() == 0) {
            showNoFriends();
            return;
        }

        try {
            for (int i = 0; i < friends.length(); i++) {
                JSONObject friend = friends.getJSONObject(i);
                createFriendView(friend);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error displaying friends", Toast.LENGTH_SHORT).show();
        }
    }

    private void createFriendView(JSONObject friend) throws JSONException {
        // Create a linear layout for each friend
        LinearLayout friendLayout = new LinearLayout(this);
        friendLayout.setOrientation(LinearLayout.VERTICAL);
        friendLayout.setPadding(16, 16, 16, 16);
        friendLayout.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 0, 16);
        friendLayout.setLayoutParams(layoutParams);

        // Create horizontal layout for friend info and button
        LinearLayout infoLayout = new LinearLayout(this);
        infoLayout.setOrientation(LinearLayout.HORIZONTAL);
        infoLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        // Create vertical layout for friend details
        LinearLayout detailsLayout = new LinearLayout(this);
        detailsLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams detailsParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
        );
        detailsLayout.setLayoutParams(detailsParams);

        // Username
        TextView usernameText = new TextView(this);
        usernameText.setText(friend.getString("username"));
        usernameText.setTextSize(18);
        usernameText.setTextColor(getResources().getColor(android.R.color.black));
        detailsLayout.addView(usernameText);

        // Full name
        TextView fullNameText = new TextView(this);
        fullNameText.setText(friend.getString("fullName"));
        fullNameText.setTextSize(14);
        fullNameText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        detailsLayout.addView(fullNameText);

        // Friend since date
        TextView dateText = new TextView(this);
        dateText.setText("Friends since: " + friend.getString("createdAt").substring(0, 10));
        dateText.setTextSize(12);
        dateText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        detailsLayout.addView(dateText);

        // Message button
        Button messageButton = new Button(this);
        messageButton.setText("Message");
        messageButton.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
        messageButton.setTextColor(getResources().getColor(android.R.color.white));
        messageButton.setPadding(16, 8, 16, 8);

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        messageButton.setLayoutParams(buttonParams);

        // Set click listener for message button
        final int friendId = friend.getInt("userId");
        final String friendName = friend.getString("username");
        messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FriendsActivity.this, MessagingActivity.class);
                intent.putExtra("friendId", friendId);
                intent.putExtra("friendName", friendName);
                startActivity(intent);
            }
        });

        // Add details and button to info layout
        infoLayout.addView(detailsLayout);
        infoLayout.addView(messageButton);

        // Add info layout to friend layout
        friendLayout.addView(infoLayout);

        friendsContainer.addView(friendLayout);
    }

    private void showNoFriends() {
        friendsContainer.removeAllViews();
        noFriendsText.setVisibility(View.VISIBLE);
        noFriendsText.setText("You don't have any friends yet.\nClick 'Add Friends' to start connecting!");
    }
}