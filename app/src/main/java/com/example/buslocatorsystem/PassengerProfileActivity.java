package com.example.buslocatorsystem;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.buslocatorsystem.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class PassengerProfileActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;

    private ImageView profileImageView;
    private TextView emailTextView;
    private TextView nameTextView;
    private TextView usernameTextView;
    private TextView phoneTextView;
    private TextView passwordTextView;

    private FirebaseUser currentUser;
    private Uri selectedImageUri; // Initialize Firebase Realtime Database

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_profile);

        // Get current user from FirebaseAuth
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Initialize views
        profileImageView = findViewById(R.id.profileImageView);
        emailTextView = findViewById(R.id.emailTextView);
        nameTextView = findViewById(R.id.nameTextView);
        usernameTextView = findViewById(R.id.usernameTextView);
        phoneTextView = findViewById(R.id.phoneTextView);
        passwordTextView = findViewById(R.id.passwordTextView);

        // Set profile image and other information
        setProfileImage() ;

        handler = new Handler(Looper.getMainLooper());
        startDataUpdates();

        // Set up click listeners
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        findViewById(R.id.editButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialog();
            }
        });

        // Enable back button in the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }



    }

    private void startDataUpdates() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Generate or retrieve the updated data
                String newData = generateNewData();

                showInfo();

                // Repeat the process after 1 second
                startDataUpdates();
            }
        }, 1000); // 1 second delay
    }

    private String generateNewData() {
        // Replace this method with your own logic to generate or retrieve the updated data
        // For this example, we will return a simple timestamp
        return String.valueOf(System.currentTimeMillis());
    }

    private void showInfo(){
        DatabaseReference databaseReferenceUsername = firebaseDatabase.getReference("users");
        databaseReferenceUsername.child("passengers").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String username = snapshot.child("uname").getValue(String.class);
                    getInfoPassenger(username);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getInfoPassenger(String uname){

        DatabaseReference databaseReference = firebaseDatabase.getReference("passengers");
        // Retrieve the passenger's profile data
        databaseReference.child(uname).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve the profile data from the dataSnapshot
                    String email = dataSnapshot.child("email").getValue(String.class);
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String username = dataSnapshot.child("uname").getValue(String.class);
                    String phone = dataSnapshot.child("phone").getValue(String.class);
                    String password = dataSnapshot.child("password").getValue(String.class);

                    // Update the UI with the retrieved data
                    emailTextView.setText(email);
                    nameTextView.setText(name);
                    usernameTextView.setText(username);
                    phoneTextView.setText(phone);
                    passwordTextView.setText(password);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error
                Log.e("Database Error", databaseError.getMessage());
            }
        });
    }

    private void setProfileImage() {
        if (currentUser.getPhotoUrl() != null) {
            // Load user's profile image using FirebaseUser's photo URL
            // You can use your own method or library for loading the image
            // Here, we use Glide library as an example
            Glide.with(this)
                    .load(currentUser.getPhotoUrl())
                    .placeholder(R.drawable.default_profile_image) // Placeholder image if the user has not uploaded their image
                    .into(profileImageView);
        } else {
            // Set a placeholder image if the user has not uploaded their image
            profileImageView.setImageResource(R.drawable.default_profile_image);
        }
    }


    private void selectImage() {
        // Create an AlertDialog to let the user choose between taking a photo or selecting from gallery
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image");
        builder.setItems(new CharSequence[]{"Take Photo", "Choose from Gallery"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    takePhoto();
                } else {
                    chooseFromGallery();
                }
            }
        });
        builder.show();
    }

    private void takePhoto() {
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePhotoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePhotoIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void chooseFromGallery() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickPhotoIntent.setType("image/*");
        startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    if (imageBitmap != null) {
                        selectedImageUri = getImageUri(imageBitmap);
                        uploadProfileImage();
                    }
                }
            } else if (requestCode == REQUEST_IMAGE_PICK) {
                if (data != null) {
                    selectedImageUri = data.getData();
                    uploadProfileImage();
                }
            }
        }
    }

    private Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Profile Image", null);
        return Uri.parse(path);
    }

    private void uploadProfileImage() {
        if (selectedImageUri != null) {
            // Set the selected image URI as the profile photo for the current user
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("profile_images/" + currentUser.getUid() + ".jpg");
            UploadTask uploadTask = storageRef.putFile(selectedImageUri);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return storageRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();

                        // Update the profile image URL for the current user
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(downloadUri)
                                .build();

                        currentUser.updateProfile(profileUpdates)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            // Set the profile image
                                            setProfileImage();
                                            Toast.makeText(PassengerProfileActivity.this, "Profile image uploaded", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(PassengerProfileActivity.this, "Failed to upload profile image", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(PassengerProfileActivity.this, "Failed to upload profile image", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void showEditDialog() {
        // Create a dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Profile");

        // Inflate the dialog layout view
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_profile, null);
        builder.setView(dialogView);

        // Initialize EditText fields and set their initial values
        EditText nameEditText = dialogView.findViewById(R.id.editNameEditText);
        EditText emailEditText = dialogView.findViewById(R.id.editEmailEditText);
        EditText phoneEditText = dialogView.findViewById(R.id.editPhoneEditText);
        EditText passwordEditText = dialogView.findViewById(R.id.editPasswordEditText);

        // Set the current user information
        nameEditText.setText("");
        emailEditText.setText(currentUser.getEmail());
        phoneEditText.setText(""); // Set the phone number as desired
        passwordEditText.setText(""); // Set the password as desired

        // Disable editing for email and username
        emailEditText.setEnabled(false);
        emailEditText.setFocusable(false);
        emailEditText.setClickable(false);

        // Create a button for saving the changes
        builder.setPositiveButton("Save", (dialog, which) -> {
            // Retrieve the updated values
            String name = nameEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (!phone.equals("")){
                DatabaseReference databaseReferenceUsername = firebaseDatabase.getReference("users");
                databaseReferenceUsername.child("passengers").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            String username = snapshot.child("uname").getValue(String.class);
                            // Update the user's profile in the Realtime Database
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("passengers").child(Objects.requireNonNull(username));

                            userRef.child("phone").setValue(phone);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
            if (!password.equals("")){
                DatabaseReference databaseReferenceUsername = firebaseDatabase.getReference("users");
                databaseReferenceUsername.child("passengers").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            String username = snapshot.child("uname").getValue(String.class);
                            // Update the user's profile in the Realtime Database
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("passengers").child(Objects.requireNonNull(username));

                            userRef.child("password").setValue(password);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
            if (!name.equals("")){
                DatabaseReference databaseReferenceUsername = firebaseDatabase.getReference("users");
                databaseReferenceUsername.child("passengers").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            String username = snapshot.child("uname").getValue(String.class);
                            // Update the user's profile in the Realtime Database
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("passengers").child(Objects.requireNonNull(username));

                            userRef.child("name").setValue(name);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }


            // Show a toast message to indicate successful update
            Toast.makeText(PassengerProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
        });

        // Create a button for canceling the changes
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            // Dismiss the dialog
            dialog.dismiss();
        });

        // Show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Finish the activity when the back button in the action bar is pressed
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
