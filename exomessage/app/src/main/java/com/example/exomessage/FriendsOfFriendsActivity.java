// FriendsOfFriendsActivity.java - Activity to view friends grouped by mutual connections
package com.example.exomessage;

import android.annotation.SuppressLint;
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

public class FriendsOfFriendsActivity extends AppCompatActivity {

    private LinearLayout friendsOfFriendsContainer;
    private Button backButton;
    private Button refreshButton;
    private TextView noFriendsOfFriendsText;
    private TextView titleText;

    private RequestQueue requestQueue;
    private static final String GET_FRIENDS_OF_FRIENDS_URL = "https://lamp.ms.wits.ac.za/home/s2543085/get_friends_of_friends.php";
    private static final String ADD_FRIEND_URL = "https://lamp.ms.wits.ac.za/home/s2543085/add_friends.php";

    private int currentUserId;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_of_friends);

        friendsOfFriendsContainer = findViewById(R.id.friendsOfFriendsContainer);
        backButton = findViewById(R.id.backButton);
        refreshButton = findViewById(R.id.refreshFriendsOfFriendsButton);
        noFriendsOfFriendsText = findViewById(R.id.noFriendsOfFriendsText);
        titleText = findViewById(R.id.titleText);

        requestQueue = Volley.newRequestQueue(this);

        // Get current user ID from intent
        currentUserId = getIntent().getIntExtra("userId", 1);

        // Set title
        titleText.setText("Friends of Friends");

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back to previous activity
            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFriendsOfFriends();
            }
        });

        // Load friends of friends when activity starts
        loadFriendsOfFriends();
    }

    private void loadFriendsOfFriends() {
        // Create parameters for POST request
        Map<String, String> params = new HashMap<>();
        params.put("userId", String.valueOf(currentUserId));

        // Create JSON object from parameters
        JSONObject parameters = new JSONObject(params);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                GET_FRIENDS_OF_FRIENDS_URL,
                parameters,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            String message = response.getString("message");

                            if (status.equals("success")) {
                                JSONArray friendsOfFriendsGrouped = response.getJSONArray("friendsOfFriendsGrouped");
                                displayFriendsOfFriendsGrouped(friendsOfFriendsGrouped);

                                if (friendsOfFriendsGrouped.length() == 0) {
                                    Toast.makeText(FriendsOfFriendsActivity.this, "No friends of friends found", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(FriendsOfFriendsActivity.this, message, Toast.LENGTH_SHORT).show();
                                showNoFriendsOfFriends();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(FriendsOfFriendsActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                            showNoFriendsOfFriends();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("FriendsOfFriendsActivity", "Error: " + error.getMessage());
                        Toast.makeText(FriendsOfFriendsActivity.this, "Failed to load friends of friends: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        showNoFriendsOfFriends();
                    }
                }
        );

        requestQueue.add(jsonObjectRequest);
    }

    private void displayFriendsOfFriendsGrouped(JSONArray friendsOfFriendsGrouped) {
        friendsOfFriendsContainer.removeAllViews();
        noFriendsOfFriendsText.setVisibility(View.GONE);

        if (friendsOfFriendsGrouped.length() == 0) {
            showNoFriendsOfFriends();
            return;
        }

        try {
            for (int i = 0; i < friendsOfFriendsGrouped.length(); i++) {
                JSONObject friendGroup = friendsOfFriendsGrouped.getJSONObject(i);
                createFriendGroupView(friendGroup);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error displaying friends of friends", Toast.LENGTH_SHORT).show();
        }
    }

    private void createFriendGroupView(JSONObject friendGroup) throws JSONException {
        JSONObject friendInfo = friendGroup.getJSONObject("friendInfo");
        JSONArray theirFriends = friendGroup.getJSONArray("theirFriends");

        // Create main container for this friend group
        LinearLayout groupContainer = new LinearLayout(this);
        groupContainer.setOrientation(LinearLayout.VERTICAL);
        groupContainer.setPadding(16, 16, 16, 16);
        groupContainer.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);

        LinearLayout.LayoutParams groupParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        groupParams.setMargins(0, 0, 0, 24);
        groupContainer.setLayoutParams(groupParams);

        // Create header for the friend whose friends we're showing
        TextView friendHeaderText = new TextView(this);
        friendHeaderText.setText("Friends of " + friendInfo.getString("username") +
                " (" + friendInfo.getString("fullName") + ")");
        friendHeaderText.setTextSize(20);
        friendHeaderText.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
        friendHeaderText.setTypeface(null, android.graphics.Typeface.BOLD);
        friendHeaderText.setPadding(0, 0, 0, 16);
        groupContainer.addView(friendHeaderText);

        // Add a separator line
        View separator = new View(this);
        separator.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        LinearLayout.LayoutParams separatorParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                2
        );
        separatorParams.setMargins(0, 0, 0, 16);
        separator.setLayoutParams(separatorParams);
        groupContainer.addView(separator);

        // Add each friend of this friend
        for (int j = 0; j < theirFriends.length(); j++) {
            JSONObject friendOfFriend = theirFriends.getJSONObject(j);
            createFriendOfFriendView(friendOfFriend, groupContainer);
        }

        friendsOfFriendsContainer.addView(groupContainer);
    }

    private void createFriendOfFriendView(JSONObject friendOfFriend, LinearLayout parentContainer) throws JSONException {
        final int friendId = friendOfFriend.getInt("userId");

        // Create a linear layout for each friend of friend
        LinearLayout friendLayout = new LinearLayout(this);
        friendLayout.setOrientation(LinearLayout.HORIZONTAL);
        friendLayout.setPadding(24, 12, 12, 12);
        friendLayout.setBackgroundResource(android.R.drawable.list_selector_background);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 0, 8);
        friendLayout.setLayoutParams(layoutParams);

        // Create user info layout
        LinearLayout userInfoLayout = new LinearLayout(this);
        userInfoLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
        );
        userInfoLayout.setLayoutParams(infoParams);

        // Username
        TextView usernameText = new TextView(this);
        usernameText.setText(friendOfFriend.getString("username"));
        usernameText.setTextSize(16);
        usernameText.setTextColor(getResources().getColor(android.R.color.black));
        userInfoLayout.addView(usernameText);

        // Full name
        TextView fullNameText = new TextView(this);
        fullNameText.setText(friendOfFriend.getString("fullName"));
        fullNameText.setTextSize(14);
        fullNameText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        userInfoLayout.addView(fullNameText);

        // Email
        TextView emailText = new TextView(this);
        emailText.setText(friendOfFriend.getString("email"));
        emailText.setTextSize(12);
        emailText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        userInfoLayout.addView(emailText);

        // Add Friend button
        Button addFriendButton = new Button(this);
        addFriendButton.setText("Add Friend");
        addFriendButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        addFriendButton.setTextColor(getResources().getColor(android.R.color.white));
        addFriendButton.setTextSize(12);

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonParams.setMargins(8, 0, 0, 0);
        addFriendButton.setLayoutParams(buttonParams);

        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFriend(friendId, (Button) v);
            }
        });

        friendLayout.addView(userInfoLayout);
        friendLayout.addView(addFriendButton);
        parentContainer.addView(friendLayout);
    }

    private void addFriend(int friendId, final Button button) {
        button.setEnabled(false);
        button.setText("Adding...");

        // Create parameters for POST request
        Map<String, String> params = new HashMap<>();
        params.put("userId", String.valueOf(currentUserId));
        params.put("friendId", String.valueOf(friendId));

        // Create JSON object from parameters
        JSONObject parameters = new JSONObject(params);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                ADD_FRIEND_URL,
                parameters,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            String message = response.getString("message");

                            if (status.equals("success")) {
                                Toast.makeText(FriendsOfFriendsActivity.this, "Friend added successfully!", Toast.LENGTH_SHORT).show();
                                button.setText("Added!");
                                button.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                                // Refresh the list to remove the added user
                                loadFriendsOfFriends();
                            } else {
                                Toast.makeText(FriendsOfFriendsActivity.this, message, Toast.LENGTH_SHORT).show();
                                button.setEnabled(true);
                                button.setText("Add Friend");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(FriendsOfFriendsActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                            button.setEnabled(true);
                            button.setText("Add Friend");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("FriendsOfFriendsActivity", "Error: " + error.getMessage());
                        Toast.makeText(FriendsOfFriendsActivity.this, "Failed to add friend: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        button.setEnabled(true);
                        button.setText("Add Friend");
                    }
                }
        );

        requestQueue.add(jsonObjectRequest);
    }

    private void showNoFriendsOfFriends() {
        friendsOfFriendsContainer.removeAllViews();
        noFriendsOfFriendsText.setVisibility(View.VISIBLE);
        noFriendsOfFriendsText.setText("No friends of friends available.\nAdd more friends to see suggestions!");
    }
}