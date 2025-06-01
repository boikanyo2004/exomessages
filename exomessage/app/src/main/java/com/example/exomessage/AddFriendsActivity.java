// AddFriendsActivity.java - Activity to add new friends
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

public class AddFriendsActivity extends AppCompatActivity {

    private LinearLayout usersContainer;
    private Button backButton;
    private Button refreshButton;
    private TextView noUsersText;

    private RequestQueue requestQueue;
    private static final String GET_AVAILABLE_USERS_URL = "https://lamp.ms.wits.ac.za/home/s2543085/get_available_users.php";
    private static final String ADD_FRIEND_URL = "https://lamp.ms.wits.ac.za/home/s2543085/add_friends.php";

    private int currentUserId;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends);

        usersContainer = findViewById(R.id.usersContainer);
        backButton = findViewById(R.id.backButton);
        refreshButton = findViewById(R.id.refreshUsersButton);
        noUsersText = findViewById(R.id.noUsersText);

        requestQueue = Volley.newRequestQueue(this);

        // Get current user ID from intent
        currentUserId = getIntent().getIntExtra("userId", 1);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back to previous activity
            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadAvailableUsers();
            }
        });

        // Load available users when activity starts
        loadAvailableUsers();
    }

    private void loadAvailableUsers() {
        // Create parameters for POST request
        Map<String, String> params = new HashMap<>();
        params.put("userId", String.valueOf(currentUserId));

        // Create JSON object from parameters
        JSONObject parameters = new JSONObject(params);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                GET_AVAILABLE_USERS_URL,
                parameters,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            String message = response.getString("message");

                            if (status.equals("success")) {
                                JSONArray users = response.getJSONArray("users");
                                displayAvailableUsers(users);

                                if (users.length() == 0) {
                                    Toast.makeText(AddFriendsActivity.this, "No users available to add as friends", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(AddFriendsActivity.this, message, Toast.LENGTH_SHORT).show();
                                showNoUsers();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(AddFriendsActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                            showNoUsers();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("AddFriendsActivity", "Error: " + error.getMessage());
                        Toast.makeText(AddFriendsActivity.this, "Failed to load users: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        showNoUsers();
                    }
                }
        );

        requestQueue.add(jsonObjectRequest);
    }

    private void displayAvailableUsers(JSONArray users) {
        usersContainer.removeAllViews();
        noUsersText.setVisibility(View.GONE);

        if (users.length() == 0) {
            showNoUsers();
            return;
        }

        try {
            for (int i = 0; i < users.length(); i++) {
                JSONObject user = users.getJSONObject(i);
                createUserView(user);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error displaying users", Toast.LENGTH_SHORT).show();
        }
    }

    private void createUserView(JSONObject user) throws JSONException {
        final int friendId = user.getInt("userId");

        // Create a linear layout for each user
        LinearLayout userLayout = new LinearLayout(this);
        userLayout.setOrientation(LinearLayout.HORIZONTAL);
        userLayout.setPadding(16, 16, 16, 16);
        userLayout.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 0, 16);
        userLayout.setLayoutParams(layoutParams);

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
        usernameText.setText(user.getString("username"));
        usernameText.setTextSize(18);
        usernameText.setTextColor(getResources().getColor(android.R.color.black));
        userInfoLayout.addView(usernameText);

        // Full name
        TextView fullNameText = new TextView(this);
        fullNameText.setText(user.getString("fullName"));
        fullNameText.setTextSize(14);
        fullNameText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        userInfoLayout.addView(fullNameText);

        // Email
        TextView emailText = new TextView(this);
        emailText.setText(user.getString("email"));
        emailText.setTextSize(12);
        emailText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        userInfoLayout.addView(emailText);

        // Add Friend button
        Button addFriendButton = new Button(this);
        addFriendButton.setText("Add Friend");
        addFriendButton.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        addFriendButton.setTextColor(getResources().getColor(android.R.color.white));

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        addFriendButton.setLayoutParams(buttonParams);

        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFriend(friendId, (Button) v);
            }
        });

        userLayout.addView(userInfoLayout);
        userLayout.addView(addFriendButton);
        usersContainer.addView(userLayout);
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
                                Toast.makeText(AddFriendsActivity.this, message, Toast.LENGTH_SHORT).show();
                                button.setText("Added!");
                                button.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                                // Refresh the list to remove the added user
                                loadAvailableUsers();
                            } else {
                                Toast.makeText(AddFriendsActivity.this, message, Toast.LENGTH_SHORT).show();
                                button.setEnabled(true);
                                button.setText("Add Friend");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(AddFriendsActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                            button.setEnabled(true);
                            button.setText("Add Friend");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("AddFriendsActivity", "Error: " + error.getMessage());
                        Toast.makeText(AddFriendsActivity.this, "Failed to add friend: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        button.setEnabled(true);
                        button.setText("Add Friend");
                    }
                }
        );

        requestQueue.add(jsonObjectRequest);
    }

    private void showNoUsers() {
        usersContainer.removeAllViews();
        noUsersText.setVisibility(View.VISIBLE);
        noUsersText.setText("No users available to add as friends.");
    }
}
