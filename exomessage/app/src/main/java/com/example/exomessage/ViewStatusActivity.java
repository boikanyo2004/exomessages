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

public class ViewStatusActivity extends AppCompatActivity {

    private LinearLayout myStatusContainer;
    private RequestQueue requestQueue;
    private static final String GET_MY_STATUS_URL = "https://lamp.ms.wits.ac.za/home/s2543085/get_my_statuses.php";
    private static final String DELETE_STATUS_URL = "https://lamp.ms.wits.ac.za/home/s2543085/delete_status.php";
    private int currentUserId;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_status);

        myStatusContainer = findViewById(R.id.myStatusContainer);
        requestQueue = Volley.newRequestQueue(this);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("userId", 1);

        loadMyStatusUpdates();
    }

    private void loadMyStatusUpdates() {
        Log.d("ViewStatus", "Loading statuses for user: " + currentUserId);

        Map<String, String> params = new HashMap<>();
        params.put("userId", String.valueOf(currentUserId));

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                GET_MY_STATUS_URL,
                new JSONObject(params),
                response -> {
                    try {
                        Log.d("ViewStatus", "Response: " + response.toString());
                        String status = response.getString("status");
                        String message = response.getString("message");

                        if (status.equals("success")) {
                            JSONArray myStatusUpdates = response.getJSONArray("myStatuses");
                            Log.d("ViewStatus", "Found " + myStatusUpdates.length() + " statuses");
                            displayMyStatusUpdates(myStatusUpdates);

                            if (myStatusUpdates.length() == 0) {
                                Toast.makeText(ViewStatusActivity.this,
                                        "You haven't posted any status updates yet",
                                        Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(ViewStatusActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("ViewStatus", "JSON Error", e);
                        Toast.makeText(ViewStatusActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("ViewStatus", "Volley Error", error);
                    if (error.networkResponse != null) {
                        try {
                            String responseBody = new String(error.networkResponse.data, "utf-8");
                            Log.e("ViewStatus", "Error response: " + responseBody);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(ViewStatusActivity.this,
                            "Failed to load status updates",
                            Toast.LENGTH_SHORT).show();
                }
        );

        requestQueue.add(jsonObjectRequest);
    }

    private void displayMyStatusUpdates(JSONArray myStatusUpdates) {
        myStatusContainer.removeAllViews();

        try {
            for (int i = 0; i < myStatusUpdates.length(); i++) {
                JSONObject status = myStatusUpdates.getJSONObject(i);
                createMyStatusView(status);
            }
        } catch (JSONException e) {
            Log.e("ViewStatus", "Display error", e);
            Toast.makeText(this, "Error displaying statuses", Toast.LENGTH_SHORT).show();
        }
    }

    private void createMyStatusView(JSONObject status) {
        try {
            View statusView = LayoutInflater.from(this).inflate(R.layout.item_my_status, myStatusContainer, false);

            TextView statusText = statusView.findViewById(R.id.myStatusText);
            TextView upvoteCountText = statusView.findViewById(R.id.myUpvoteCountText);
            TextView friendsUpvotedText = statusView.findViewById(R.id.friendsUpvotedText);
            TextView createdAtText = statusView.findViewById(R.id.myCreatedAtText);
            Button deleteButton = statusView.findViewById(R.id.deleteStatusButton);

            // Set status text
            statusText.setText(status.optString("statusText", ""));

            // Handle upvotes
            int upvotes = status.optInt("upvotes", 0);
            upvoteCountText.setText("❤️ " + upvotes + " upvotes");

            // Handle friends who upvoted
            JSONArray friendsUpvoted = status.optJSONArray("friendsUpvoted");
            if (friendsUpvoted != null && friendsUpvoted.length() > 0) {
                StringBuilder friendsList = new StringBuilder("Upvoted by: ");
                for (int i = 0; i < friendsUpvoted.length(); i++) {
                    if (i > 0) friendsList.append(", ");
                    friendsList.append(friendsUpvoted.getString(i));
                }
                friendsUpvotedText.setText(friendsList.toString());
            } else {
                friendsUpvotedText.setText("No upvotes yet");
            }
            friendsUpvotedText.setVisibility(View.VISIBLE);

            // Handle date formatting
            String createdAt = status.optString("createdAt", "");
            String displayDate = createdAt.length() >= 10 ?
                    createdAt.substring(0, 10) : "Recent";
            createdAtText.setText("Posted: " + displayDate);

            // Set delete button listener
            String statusId = status.optString("statusId", "");
            deleteButton.setOnClickListener(v -> deleteStatus(statusId, statusView));

            myStatusContainer.addView(statusView);
        } catch (Exception e) {
            Log.e("ViewStatus", "Create view error", e);
            Toast.makeText(this, "Error showing status", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteStatus(String statusId, View statusView) {
        Log.d("ViewStatus", "Deleting status: " + statusId);

        Map<String, String> params = new HashMap<>();
        params.put("statusId", statusId);
        params.put("userId", String.valueOf(currentUserId));

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                DELETE_STATUS_URL,
                new JSONObject(params),
                response -> {
                    try {
                        String status = response.getString("status");
                        String message = response.getString("message");

                        if (status.equals("success")) {
                            myStatusContainer.removeView(statusView);
                            Toast.makeText(this, "Status deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("ViewStatus", "Delete error", e);
                        Toast.makeText(this, "Error deleting", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("ViewStatus", "Delete volley error", error);
                    Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show();
                }
        );

        requestQueue.add(request);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh statuses when returning to activity
        loadMyStatusUpdates();
    }
}