package com.example.buslocatorsystem.admin;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.example.buslocatorsystem.R;
import com.example.buslocatorsystem.SignInActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class AdminPanelActivity extends AppCompatActivity {

    private LottieAnimationView animationView;
    private CircleImageView imageViewAdmin;
    private TextView textViewAdminName;
    private Uri imageUri;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        animationView = findViewById(R.id.btnLogout);

        imageViewAdmin = findViewById(R.id.imageViewAdmin);
        textViewAdminName = findViewById(R.id.textViewAdminName);

        loadProfileImage();
        imageViewAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onProfileImageClick(view);
            }
        });
        animationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(view);
            }
        });

        // Find the card views by their IDs
        View cardViewRegisterDriver = findViewById(R.id.cardViewRegisterDriver);
        View cardViewUpdateDriver = findViewById(R.id.cardViewUpdateDriver);
        View cardViewDeleteDriver = findViewById(R.id.cardViewDeleteDriver);
        View cardViewRetrieveDriver = findViewById(R.id.cardViewRetrieveDriver);

        // Set click listeners for the card views
        cardViewRegisterDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add the code to navigate to the activity/page for registering a new bus driver
                // For example:
                startActivity(new Intent(AdminPanelActivity.this, RegisterDriverActivity.class));
            }
        });

        cardViewUpdateDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add the code to navigate to the activity/page for updating driver information
                // For example:
                startActivity(new Intent(AdminPanelActivity.this, UpdateDriverActivity.class));
            }
        });

        cardViewDeleteDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add the code to navigate to the activity/page for deleting a bus driver
                // For example:
                startActivity(new Intent(AdminPanelActivity.this, DeleteDriverActivity.class));
            }
        });

        cardViewRetrieveDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add the code to navigate to the activity/page for retrieving driver information
                // For example:
                startActivity(new Intent(AdminPanelActivity.this, RetrieveDriverActivity.class));
            }
        });
    }

    private void uploadImage(Bitmap bitmap) {

        // Upload the image to Firebase Storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child("admin_images/" + "image_profile_admin" + ".jpg");

        // Convert the Bitmap to bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageData = baos.toByteArray();
        // Upload the image to Firebase Storage
        UploadTask uploadTask = imageRef.putBytes(imageData);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
                    // Image upload success
                    // Get the download URL of the uploaded image
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Store the image URL in the Firebase Realtime Database

                        // Show a Toast message to notify the user
                        Toast.makeText(this, "Image uploaded successfully.", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        // Handle any errors that occurred while retrieving the image URL
                        Toast.makeText(this, "Image upload failed.", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    // Handle any errors that occurred during the image upload
                    Toast.makeText(this, "Image upload failed.", Toast.LENGTH_SHORT).show();
                });
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(AdminPanelActivity.this, "Profile image uploaded successfully", Toast.LENGTH_SHORT).show();
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // Save the download URL to the Realtime Database under the driver's profile
                        DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference("admins").child("admin");
                        adminRef.child("imageUrl").setValue(uri.toString());
                    }
                });
                // Load and display the updated profile image
                loadProfileImage();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AdminPanelActivity.this, "Failed to upload profile image. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProfileImage() {
        // Check if the current user is authenticated

            // Get the driver's profile image file name from the Firebase Authentication user ID
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            String profileImageFileName ="admin_images/" + "image_profile_admin" + ".jpg";
            StorageReference profileImageRef = storageRef.child(profileImageFileName);

            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child("admins").child("admin").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    String adminUsername = snapshot.child("admin_user_name").getValue(String.class);
                    textViewAdminName.setText(adminUsername);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
            // Download the profile image file and display it in the ImageView
            profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    // Use an image loading library like Picasso or Glide for better performance
                    Glide.with(AdminPanelActivity.this)
                            .load(uri)
                            .placeholder(R.drawable.default_profile_image)
                            .error(R.drawable.error_image)
                            .into(imageViewAdmin);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("DriverProfileActivity", "Failed to load profile image", e);
                    Toast.makeText(AdminPanelActivity.this, "Failed to load profile image", Toast.LENGTH_SHORT).show();
                }
            });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Get the selected image Uri
            Uri imageUri = data.getData();

            try {
                // Convert the Uri to Bitmap
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

                // Upload the image to Firebase Storage and update the profile image in the database
                uploadImage(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to select image", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.logout_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle menu item clicks
                switch (item.getItemId()) {
                    case R.id.menu_update_admin_info:
                        // Handle the action for updating admin information
                        showUpdateAdminDialog();
                        return true;
                    case R.id.menu_logout:
                        // Handle the action for logging out
                        Intent intent = new Intent(AdminPanelActivity.this, SignInActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                    default:
                        return false;
                }
            }
        });

        popupMenu.show();
    }
    private void showUpdateAdminDialog() {
        // Inflate the dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_update_admin_info, null);

        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Find the views in the dialog layout
        EditText editTextAdminEmail = dialogView.findViewById(R.id.editTextAdminEmail);
        EditText editTextAdminUsername = dialogView.findViewById(R.id.editTextAdminUsername);
        EditText editTextAdminPassword = dialogView.findViewById(R.id.editTextAdminPassword);
        Button btnUpdateAdminInfo = dialogView.findViewById(R.id.btnUpdateAdminInfo);

        // Set a click listener for the "Update" button in the dialog
        btnUpdateAdminInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the updated admin information from the input fields
                String updatedEmail = editTextAdminEmail.getText().toString().trim();
                String updatedUsername = editTextAdminUsername.getText().toString().trim();
                String updatedPassword = editTextAdminPassword.getText().toString().trim();

                if(updatedEmail.isEmpty()||updatedUsername.isEmpty()||updatedPassword.isEmpty()){
                    Toast.makeText(AdminPanelActivity.this, "Fill in all requirement", Toast.LENGTH_LONG).show();
                }else{
                    // Update the admin information in the Firebase Realtime Database
                    DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference("admins").child("admin");
                    Map<String, Object> updates = new HashMap<>();

                /*adminRef.child("admin_email").setValue(updatedEmail);
                adminRef.child("admin_user_name").setValue(updatedUsername);
                adminRef.child("admin_password").setValue(updatedPassword);*/

                    updates.put("admin_email",updatedEmail);
                    updates.put("admin_user_name",updatedUsername);
                    updates.put("admin_password",updatedPassword);

                    adminRef.updateChildren(updates);

                    textViewAdminName.setText(updatedUsername);
                    // Dismiss the dialog after updating
                    dialog.dismiss();
                }

            }
        });

        // Show the dialog
        dialog.show();
    }
    public void onProfileImageClick(View view) {
        // Open the gallery to select an image
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), PICK_IMAGE_REQUEST);
    }
}
