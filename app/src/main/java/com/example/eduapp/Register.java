package com.example.eduapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    private static final int PICK_IMAGE_REQUEST = 1;

    EditText mFullname, mEmail, mPassword, muser;
    Button mRegisteredbtn, btnUploadPicture;
    TextView mLoginBtn;
    ImageView profilepic;

    FirebaseAuth fauth;
    FirebaseFirestore fstore;
    FirebaseStorage storage;
    StorageReference storageReference;

    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Firebase Initialization
        fauth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // UI Elements
        mFullname = findViewById(R.id.fullname);
        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        muser = findViewById(R.id.username);
        mRegisteredbtn = findViewById(R.id.register);
        mLoginBtn = findViewById(R.id.login);
        btnUploadPicture = findViewById(R.id.btn_upload_picture);
        profilepic = findViewById(R.id.iv_profile_picture);

        // Check if user is already logged in
        if (fauth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        // Redirect to Login Activity
        mLoginBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Register.this, Login.class);
            startActivity(intent);
        });

        // Upload Profile Picture Button Click
        btnUploadPicture.setOnClickListener(v -> openFileChooser());

        // Register Button Click
        mRegisteredbtn.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        final String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();
        final String fullname = mFullname.getText().toString();
        final String username = muser.getText().toString().trim();

        // Input Validation
        if (TextUtils.isEmpty(email)) {
            mEmail.setError("Email is Required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            mPassword.setError("Password is Required");
            return;
        }
        if (password.length() < 6) {
            mPassword.setError("Password must be >=6 characters");
            return;
        }

        // Firebase Authentication - Create User
        fauth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser fuser = fauth.getCurrentUser();
                if (fuser != null) {
                    fuser.sendEmailVerification().addOnSuccessListener(aVoid ->
                            Toast.makeText(Register.this, "Registration Successfull", Toast.LENGTH_SHORT).show()
                    ).addOnFailureListener(e ->
                            Log.e(TAG, "Registration failed: " + e.getMessage())
                    );

                    String userId = fuser.getUid();
                    DocumentReference documentReference = fstore.collection("users").document(userId);
                    Map<String, Object> user = new HashMap<>();
                    user.put("fname", fullname);
                    user.put("email", email);
                    user.put("username", username.isEmpty() ? "username" : username);

                    // Save user details to Firestore
                    documentReference.set(user).addOnSuccessListener(aVoid ->
                            Log.d(TAG, "User profile created for " + userId)
                    ).addOnFailureListener(e ->
                            Log.e(TAG, "Error creating user profile: " + e.toString())
                    );

                    // Upload Profile Picture if selected
                    if (imageUri != null) {
                        uploadProfilePicture(userId);
                    }

                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }
            } else {
                Toast.makeText(Register.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Open File Chooser for Profile Picture Selection
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Handle Image Selection
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profilepic.setImageURI(imageUri);
        }
    }

    // Upload Profile Picture to Firebase Storage
    private void uploadProfilePicture(String userId) {
        StorageReference fileReference = storageReference.child("profile_pictures/" + userId + ".jpg");

        fileReference.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot ->
                        fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            DocumentReference userRef = fstore.collection("users").document(userId);
                            userRef.update("profilePicUrl", uri.toString())
                                    .addOnSuccessListener(aVoid ->
                                            Log.d(TAG, "Profile picture uploaded successfully")
                                    )
                                    .addOnFailureListener(e ->
                                            Log.e(TAG, "Error updating profile picture URL: " + e.getMessage())
                                    );
                        })
                )
                .addOnFailureListener(e ->
                        Log.e(TAG, "Profile picture upload failed: " + e.getMessage())
                );
    }
}
