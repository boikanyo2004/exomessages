package com.example.exomessage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PostStatusActivity extends AppCompatActivity {

    private EditText statusEditText;
    private Button postButton;
    private Button viewMyStatusButton;
    private Button viewFriendsStatusButton;
    private RequestQueue requestQueue;
    private static final String POST_STATUS_URL = "https://lamp.ms.wits.ac.za/home/s2543085/post_status.php";
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_status);

        statusEditText = findViewById(R.id.statusEditText);
        postButton = findViewById(R.id.postButton);
        viewMyStatusButton = findViewById(R.id.viewMyStatusButton);
        viewFriendsStatusButton = findViewById(R.id.viewFriendsStatusButton);
        requestQueue = Volley.newRequestQueue(this);

        // Get current user ID from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("userId", 1);

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postStatus();
            }
        });

        viewMyStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PostStatusActivity.this, ViewStatusActivity.class);
                startActivity(intent);
            }
        });

        viewFriendsStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PostStatusActivity.this, StatusActivity.class);
                startActivity(intent);
            }
        });
    }

    private void postStatus() {
        String statusText = statusEditText.getText().toString().trim();

        if (statusText.isEmpty()) {
            Toast.makeText(this, "Please enter a status", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("userId", String.valueOf(currentUserId));
        params.put("statusText", statusText);

        JSONObject parameters = new JSONObject(params);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                POST_STATUS_URL,
                parameters,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            String message = response.getString("message");
                            Toast.makeText(PostStatusActivity.this, message, Toast.LENGTH_SHORT).show();

                            if (status.equals("success")) {
                                statusEditText.setText(""); // Clear the text field
                                Toast.makeText(PostStatusActivity.this, "Status posted successfully!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(PostStatusActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(PostStatusActivity.this, "Failed to post status: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );

        requestQueue.add(jsonObjectRequest);
    }
}