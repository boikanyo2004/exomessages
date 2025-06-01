package com.example.exomessage;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

public class loginActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private Button loginButton;
    private Button signup;
    private Button forgot;

    private RequestQueue requestQueue;
    private static final String LOGIN_URL = "https://lamp.ms.wits.ac.za/home/s2543085/login.php";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Check if user is already logged in
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        //

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login);
        signup = findViewById(R.id.SignUp);
        forgot = findViewById(R.id.ForgotPassword);

        requestQueue = Volley.newRequestQueue(this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(loginActivity.this, signupActivity.class);
                startActivity(intent);
            }
        });

        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(loginActivity.this, forgotActivity.class);
                startActivity(intent);
            }
        });
    }

    private void attemptLogin() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable login button during request
        loginButton.setEnabled(false);
        loginButton.setText("Signing In...");

        // Create parameters for POST request
        Map<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);

        // Create JSON object from parameters
        JSONObject parameters = new JSONObject(params);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                LOGIN_URL,
                parameters,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            String message = response.getString("message");

                            if (status.equals("success")) {
                                // Get user data from response
                                int userId = response.getInt("userId");
                                String responseUsername = response.getString("username");
                                String fullName = response.getString("fullName");
                                String email = response.getString("email");

                                // Store user session data
                                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putInt("userId", userId);
                                editor.putString("username", responseUsername);
                                editor.putString("fullName", fullName);
                                editor.putString("email", email);
                                editor.putBoolean("isLoggedIn", true);
                                editor.apply();

                                Toast.makeText(loginActivity.this, message, Toast.LENGTH_SHORT).show();

                                // Navigate to HomeActivity
                                Intent intent = new Intent(loginActivity.this, HomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                //finish();
                            } else {
                                Toast.makeText(loginActivity.this, message, Toast.LENGTH_SHORT).show();
                                // Re-enable login button
                                loginButton.setEnabled(true);
                                loginButton.setText("Sign In");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(loginActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                            // Re-enable login button
                            loginButton.setEnabled(true);
                            loginButton.setText("Sign In");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(loginActivity.this, "Login failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        // Re-enable login button
                        loginButton.setEnabled(true);
                        loginButton.setText("Sign In");
                    }
                }
        );

        requestQueue.add(jsonObjectRequest);
    }
}