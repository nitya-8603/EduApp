package com.example.eduapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;


public class ProfileActivity extends AppCompatActivity {
    private DrawerLayout drawer;
    private ImageButton menu1;
    private NavigationView navigationView;
    private TextView profileName, profileEmail;
    private ImageView profileImage;
    FirebaseAuth auth;
    FirebaseFirestore db;
    Button name,password,profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        menu1 = findViewById(R.id.menuButton);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigationView);
        name=findViewById(R.id.btnUpdateName);
        password=findViewById(R.id.btnUpdatePassword);
        profile=findViewById(R.id.btnUpdateProfile);

        // Get header view to update user details
        View headerView = navigationView.getHeaderView(0);
        profileName = headerView.findViewById(R.id.name);
        profileEmail = headerView.findViewById(R.id.email);
        profileImage = headerView.findViewById(R.id.profile_image);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        loadUserData(); // Load user data into navigation drawer

        menu1.setOnClickListener(v -> {
            if (!drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.openDrawer(GravityCompat.START);
            }
        });

        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "Name Updated", Toast.LENGTH_SHORT).show();
            }
        });

        password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "Password Updated", Toast.LENGTH_SHORT).show();
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "ProfilePic Updated", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle Navigation Drawer item clicks
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemid = item.getItemId();

                if (itemid == R.id.nav_profile) {
                    Toast.makeText(ProfileActivity.this, "Profile Clicked", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ProfileActivity.this, ProfileActivity.class);
                    startActivity(intent);
                }
                if (itemid == R.id.setting) {
                    Toast.makeText(ProfileActivity.this, "Setting Clicked", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ProfileActivity.this, SettingActivity.class);
                    startActivity(intent);
                }
                if (itemid == R.id.share) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Check out this Application");
                    intent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details");
                    startActivity(Intent.createChooser(intent, "share via"));
                }
                if (itemid == R.id.logout) {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(ProfileActivity.this, Login.class);
                    startActivity(intent);
                    finish();

                }

                return false;
            }
        });
    }

    private void loadUserData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid()).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String name = document.getString("username");
                                String email = document.getString("email");
                                String profilePicUrl = document.getString("profilePicture");

                                profileName.setText(name != null ? name : "User Name");
                                profileEmail.setText(email != null ? email : "user@email.com");

                                if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
                                    Picasso.get()
                                            .load(profilePicUrl)
                                            .placeholder(R.drawable.profile)
                                            .error(R.drawable.profile)
                                            .into(profileImage);
                                }
                            }
                        }
                    });
        }
    }
}



