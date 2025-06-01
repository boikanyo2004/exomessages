package com.example.exomessage;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

public class forgotActivity extends AppCompatActivity {
    private EditText usernameEditText, emailEditText, newUsernameEditText, newPasswordEditText;
    private Button resetPasswordBtn, recoverUsernameBtn, submitBtn;
    private LinearLayout passwordResetLayout, usernameRecoverLayout;
    private RequestQueue requestQueue;
    private static final String FORGOT_URL = "https://lamp.ms.wits.ac.za/home/s2543085/forgot.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot);

        // Initialize views
        usernameEditText = findViewById(R.id.username);
        emailEditText = findViewById(R.id.email);
        newUsernameEditText = findViewById(R.id.new_username);
        newPasswordEditText = findViewById(R.id.new_password);
        resetPasswordBtn = findViewById(R.id.reset_password_btn);
        recoverUsernameBtn = findViewById(R.id.recover_username_btn);
        submitBtn = findViewById(R.id.submit_btn);
        passwordResetLayout = findViewById(R.id.password_reset_layout);
        usernameRecoverLayout = findViewById(R.id.username_recover_layout);

        requestQueue = Volley.newRequestQueue(this);

        // Setup button click listeners
        resetPasswordBtn.setOnClickListener(v -> showPasswordResetForm());
        recoverUsernameBtn.setOnClickListener(v -> showUsernameRecoverForm());
        submitBtn.setOnClickListener(v -> handleForgotRequest());
    }

    private void showPasswordResetForm() {
        passwordResetLayout.setVisibility(View.VISIBLE);
        usernameRecoverLayout.setVisibility(View.GONE);
        submitBtn.setTag("password");
    }

    private void showUsernameRecoverForm() {
        passwordResetLayout.setVisibility(View.GONE);
        usernameRecoverLayout.setVisibility(View.VISIBLE);
        submitBtn.setTag("username");
    }

    private void handleForgotRequest() {
        String type = (String) submitBtn.getTag();
        JSONObject jsonBody = new JSONObject();

        try {
            jsonBody.put("type", type);

            if (type.equals("password")) {
                String username = usernameEditText.getText().toString().trim();
                String newPassword = newPasswordEditText.getText().toString().trim();

                if (username.isEmpty() || newPassword.isEmpty()) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                jsonBody.put("username", username);
                jsonBody.put("new_password", newPassword);
            } else {
                String email = emailEditText.getText().toString().trim();
                String newUsername = newUsernameEditText.getText().toString().trim();

                if (email.isEmpty()) {
                    Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show();
                    return;
                }

                jsonBody.put("email", email);
                if (!newUsername.isEmpty()) {
                    jsonBody.put("new_username", newUsername);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                FORGOT_URL,
                jsonBody,
                response -> {
                    try {
                        String status = response.getString("status");
                        if (status.equals("success")) {
                            if (type.equals("username") && response.has("username")) {
                                String recoveredUsername = response.getString("username");
                                Toast.makeText(forgotActivity.this,
                                        "Your username is: " + recoveredUsername,
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(forgotActivity.this,
                                        "Update successful",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } else {
                            String message = response.getString("message");
                            Toast.makeText(forgotActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(forgotActivity.this,
                            "Error: " + error.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }
        );

        requestQueue.add(jsonObjectRequest);
    }
}