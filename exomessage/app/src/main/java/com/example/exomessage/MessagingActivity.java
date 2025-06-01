// MessagingActivity.java - Chat interface for communicating with friends
package com.example.exomessage;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MessagingActivity extends AppCompatActivity {

    private LinearLayout messagesContainer;
    private ScrollView messagesScrollView;
    private EditText messageInput;
    private Button sendButton;
    private TextView friendNameText;

    private RequestQueue requestQueue;
    private static final String SEND_MESSAGE_URL = "https://lamp.ms.wits.ac.za/home/s2543085/send_messages.php";
    private static final String GET_MESSAGES_URL = "https://lamp.ms.wits.ac.za/home/s2543085/get_messages.php";

    private int currentUserId;
    private int friendId;
    private String friendName;
    private Handler messageHandler;
    private Runnable messageRefreshRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        // Initialize views
        messagesContainer = findViewById(R.id.messagesContainer);
        messagesScrollView = findViewById(R.id.messagesScrollView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        friendNameText = findViewById(R.id.friendNameText);

        requestQueue = Volley.newRequestQueue(this);
        messageHandler = new Handler();

        // Get user data
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("userId", 1);

        // Get friend data from intent
        friendId = getIntent().getIntExtra("friendId", -1);
        friendName = getIntent().getStringExtra("friendName");

        if (friendId == -1 || friendName == null) {
            Toast.makeText(this, "Error: Friend data not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        friendNameText.setText("Chat with " + friendName);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        // Load existing messages
        loadMessages();

        // Set up auto-refresh for new messages (every 3 seconds)
        messageRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                loadMessages();
                messageHandler.postDelayed(this, 3000);
            }
        };
        messageHandler.postDelayed(messageRefreshRunnable, 3000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (messageHandler != null && messageRefreshRunnable != null) {
            messageHandler.removeCallbacks(messageRefreshRunnable);
        }
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();

        if (messageText.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use StringRequest instead of JsonObjectRequest for better error handling
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                SEND_MESSAGE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("MessagingActivity", "Send message response: " + response);

                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");
                            String message = jsonResponse.getString("message");

                            if (status.equals("success")) {
                                messageInput.setText("");
                                loadMessages(); // Refresh messages
                                Toast.makeText(MessagingActivity.this, "Message sent!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MessagingActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("MessagingActivity", "JSON Parse Error: " + e.getMessage());
                            Log.e("MessagingActivity", "Raw response: " + response);
                            Toast.makeText(MessagingActivity.this, "Server error. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("MessagingActivity", "Send message error: " + error.toString());
                        if (error.networkResponse != null) {
                            Log.e("MessagingActivity", "Status code: " + error.networkResponse.statusCode);
                            Log.e("MessagingActivity", "Response data: " + new String(error.networkResponse.data));
                        }
                        Toast.makeText(MessagingActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("senderId", String.valueOf(currentUserId));
                params.put("receiverId", String.valueOf(friendId));
                params.put("message", messageText);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                return headers;
            }
        };

        requestQueue.add(stringRequest);
    }

    private void loadMessages() {
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                GET_MESSAGES_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("MessagingActivity", "Load messages response: " + response);

                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");

                            if (status.equals("success")) {
                                JSONArray messages = jsonResponse.getJSONArray("messages");
                                displayMessages(messages);
                            } else {
                                // No messages yet - this is okay
                                if (messagesContainer.getChildCount() == 0) {
                                    showNoMessages();
                                }
                            }
                        } catch (JSONException e) {
                            Log.e("MessagingActivity", "JSON Parse Error: " + e.getMessage());
                            Log.e("MessagingActivity", "Raw response: " + response);

                            // Check if response contains HTML (error page)
                            if (response.contains("<br") || response.contains("<html") || response.contains("<!DOCTYPE")) {
                                Log.e("MessagingActivity", "Server returned HTML instead of JSON - likely a PHP error");
                                Toast.makeText(MessagingActivity.this, "Server configuration error", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MessagingActivity.this, "Error loading messages", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("MessagingActivity", "Load messages error: " + error.toString());
                        if (error.networkResponse != null) {
                            Log.e("MessagingActivity", "Status code: " + error.networkResponse.statusCode);
                            String responseBody = new String(error.networkResponse.data);
                            Log.e("MessagingActivity", "Response data: " + responseBody);
                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userId1", String.valueOf(currentUserId));
                params.put("userId2", String.valueOf(friendId));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                return headers;
            }
        };

        requestQueue.add(stringRequest);
    }

    private void displayMessages(JSONArray messages) {
        messagesContainer.removeAllViews();

        if (messages.length() == 0) {
            showNoMessages();
            return;
        }

        try {
            for (int i = 0; i < messages.length(); i++) {
                JSONObject message = messages.getJSONObject(i);
                createMessageView(message);
            }

            // Scroll to bottom to show latest messages
            messagesScrollView.post(new Runnable() {
                @Override
                public void run() {
                    messagesScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void createMessageView(JSONObject message) throws JSONException {
        LinearLayout messageLayout = new LinearLayout(this);
        messageLayout.setOrientation(LinearLayout.VERTICAL);
        messageLayout.setPadding(16, 8, 16, 8);

        int senderId = message.getInt("senderId");
        boolean isFromCurrentUser = (senderId == currentUserId);

        // Set alignment based on sender
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(isFromCurrentUser ? 100 : 0, 4, isFromCurrentUser ? 0 : 100, 4);
        messageLayout.setLayoutParams(layoutParams);

        // Set background color
        if (isFromCurrentUser) {
            messageLayout.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
        } else {
            messageLayout.setBackgroundColor(getResources().getColor(android.R.color.background_light));
        }

        // Message text
        TextView messageText = new TextView(this);
        messageText.setText(message.getString("message"));
        messageText.setTextSize(16);
        messageText.setTextColor(getResources().getColor(android.R.color.black));
        messageText.setPadding(12, 8, 12, 4);
        messageLayout.addView(messageText);

        // Timestamp
        TextView timestampText = new TextView(this);
        String timestamp = message.getString("createdAt");
        timestampText.setText(formatTimestamp(timestamp));
        timestampText.setTextSize(12);
        timestampText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        timestampText.setPadding(12, 0, 12, 8);
        messageLayout.addView(timestampText);

        messagesContainer.addView(messageLayout);
    }

    private void showNoMessages() {
        messagesContainer.removeAllViews();

        TextView noMessagesText = new TextView(this);
        noMessagesText.setText("No messages yet. Start the conversation!");
        noMessagesText.setTextSize(16);
        noMessagesText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        noMessagesText.setPadding(16, 32, 16, 32);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        noMessagesText.setLayoutParams(params);

        messagesContainer.addView(noMessagesText);
    }

    private String formatTimestamp(String timestamp) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(timestamp);
            return outputFormat.format(date);
        } catch (Exception e) {
            return timestamp;
        }
    }
}