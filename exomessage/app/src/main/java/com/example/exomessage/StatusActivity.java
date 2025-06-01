package com.example.exomessage;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;

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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

public class StatusActivity extends AppCompatActivity {

    private LinearLayout statusContainer;
    private RequestQueue requestQueue;
    private static final String GET_STATUS_URL = "https://lamp.ms.wits.ac.za/home/s2543085/get_friends_status.php";
    private int currentUserId;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        statusContainer = findViewById(R.id.statusContainer);
        requestQueue = Volley.newRequestQueue(this);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("userId", 1);

        loadStatusUpdates();
    }

    private void loadStatusUpdates() {
        Map<String, String> params = new HashMap<>();
        params.put("userId", String.valueOf(currentUserId));

        JSONObject parameters = new JSONObject(params);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                GET_STATUS_URL,
                parameters,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            String message = response.getString("message");

                            if (status.equals("success")) {
                                JSONArray statusUpdates = response.getJSONArray("statusUpdates");
                                displayStatusUpdates(statusUpdates);

                                if (statusUpdates.length() == 0) {
                                    Toast.makeText(StatusActivity.this, "No status updates from friends", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(StatusActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(StatusActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("StatusActivity", "Error: " + error.getMessage());
                        Toast.makeText(StatusActivity.this, "Failed to load status updates: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );

        requestQueue.add(jsonObjectRequest);
    }

    private void displayStatusUpdates(JSONArray statusUpdates) {
        statusContainer.removeAllViews();

        try {
            for (int i = 0; i < statusUpdates.length(); i++) {
                JSONObject status = statusUpdates.getJSONObject(i);
                createStatusView(status);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error displaying status updates", Toast.LENGTH_SHORT).show();
        }
    }
    private void createStatusView(JSONObject status) {
        try {
            // Inflate the status item view
            View statusView = LayoutInflater.from(this).inflate(R.layout.item_status, statusContainer, false);

            // Initialize all views with null checks
            TextView usernameText = statusView.findViewById(R.id.usernameText);
            TextView statusText = statusView.findViewById(R.id.statusText);
            TextView upvoteCountText = statusView.findViewById(R.id.upvoteCountText);
            Button upvoteButton = statusView.findViewById(R.id.upvoteButton);
            TextView createdAtText = statusView.findViewById(R.id.createdAtText);

            // Safely set text values with fallbacks
            usernameText.setText(status.optString("username", "Unknown User"));
            statusText.setText(status.optString("statusText", ""));

            // Handle upvotes
            int upvotes = status.optInt("upvotes", 0);
            upvoteCountText.setText(String.valueOf(upvotes));

            // Handle date formatting safely
            String createdAt = status.optString("createdAt", "");
            String displayDate = createdAt.length() >= 10 ? createdAt.substring(0, 10) : "Recent";
            createdAtText.setText("Posted: " + displayDate);

            // Set upvote click listener only if we have a valid statusId
            String statusId = status.optString("statusId", "");
            if (!statusId.isEmpty()) {
                upvoteButton.setOnClickListener(v -> {
                    // Optimistic UI update
                    int currentUpvotes = Integer.parseInt(upvoteCountText.getText().toString());
                    upvoteCountText.setText(String.valueOf(currentUpvotes + 1));

                    // Send to server
                    upvoteStatus(statusId, upvoteCountText);
                });
            } else {
                upvoteButton.setEnabled(false);
            }

            statusContainer.addView(statusView);
        } catch (Exception e) {
            Log.e("StatusActivity", "Error creating status view", e);
            Toast.makeText(this, "Error showing status update", Toast.LENGTH_SHORT).show();
        }
    }

    private void upvoteStatus(String statusId, TextView upvoteCountText) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("userId", 0);

        Map<String, String> params = new HashMap<>();
        params.put("statusId", statusId);
        params.put("userId", String.valueOf(userId));

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                "https://lamp.ms.wits.ac.za/home/s2543085/upvote_status.php",
                new JSONObject(params),
                response -> {
                    try {
                        if (!response.getString("status").equals("success")) {
                            // If server fails, revert the UI
                            int current = Integer.parseInt(upvoteCountText.getText().toString());
                            upvoteCountText.setText(String.valueOf(current - 1));
                            Toast.makeText(this, "Upvote failed", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    // On network error, revert the UI
                    int current = Integer.parseInt(upvoteCountText.getText().toString());
                    upvoteCountText.setText(String.valueOf(current - 1));
                    Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                }
        );

        requestQueue.add(request);
    }
}