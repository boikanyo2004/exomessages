package com.example.exomessage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    Button loginButton,friendsButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); // Make sure this layout exists
        loginButton = findViewById(R.id.login);
        friendsButton = findViewById(R.id.friendss);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        friendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptFriends();
            }
        });

    }

    private void attemptLogin() {
        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        startActivity(intent);
        //finish();
    }

    private void attemptFriends() {
        Intent intent = new Intent(HomeActivity.this, FriendsActivity.class);
        startActivity(intent);
        //finish();
    }

}
