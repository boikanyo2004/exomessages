package com.example.exomessage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

public class signupActivity extends AppCompatActivity {
    EditText usernameEditText, fullNameEditText, emailEditText, passwordEditText;
    Button signupButton;
    private RequestQueue requestQueue;
    private static final String SIGNUP_URL = "https://lamp.ms.wits.ac.za/home/s2543085/signup.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        usernameEditText = findViewById(R.id.username);
        fullNameEditText = findViewById(R.id.fullname);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        signupButton = findViewById(R.id.login);

        requestQueue = Volley.newRequestQueue(this);

        signupButton.setOnClickListener(view -> attemptSignup());
    }

    private void attemptSignup() {
        String username = usernameEditText.getText().toString().trim();
        String fullname = fullNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (username.isEmpty() || fullname.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Email validation
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        // Password validation
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("username", username);
            jsonBody.put("fullname", fullname);
            jsonBody.put("email", email);
            jsonBody.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Disable button during request
        signupButton.setEnabled(false);
        signupButton.setText("Creating Account...");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                SIGNUP_URL,
                jsonBody,
                response -> {
                    try {
                        String status = response.getString("status");
                        String message = response.getString("message");

                        if (status.equals("success")) {
                            // Get user data from response
                            int userId = response.getInt("userId");
                            String responseUsername = response.getString("username");
                            String responseFullName = response.getString("fullName");
                            String responseEmail = response.getString("email");

                            // Store user session data
                            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putInt("userId", userId);
                            editor.putString("username", responseUsername);
                            editor.putString("fullName", responseFullName);
                            editor.putString("email", responseEmail);
                            editor.putBoolean("isLoggedIn", true);
                            editor.apply();

                            Toast.makeText(signupActivity.this, message, Toast.LENGTH_LONG).show();

                            // Navigate to HomeActivity
                            Intent intent = new Intent(signupActivity.this, HomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(signupActivity.this, message, Toast.LENGTH_SHORT).show();
                            // Re-enable button on error
                            signupButton.setEnabled(true);
                            signupButton.setText("Sign Up");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(signupActivity.this, "Error processing response", Toast.LENGTH_SHORT).show();
                        // Re-enable button on error
                        signupButton.setEnabled(true);
                        signupButton.setText("Sign Up");
                    }
                },
                error -> {
                    Toast.makeText(signupActivity.this, "Registration failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                    // Re-enable button on error
                    signupButton.setEnabled(true);
                    signupButton.setText("Sign Up");
                }
        );

        requestQueue.add(jsonObjectRequest);
    }
}