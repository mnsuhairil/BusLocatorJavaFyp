package com.example.buslocatorsystem;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class DriverProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView profileImageView;
    private TextView nameTextView;
    private TextView emailTextView;
    private TextView busIdTextView;
    private TextView routeTextView;
    private TextView statusTextView;
    private Button updateNameButton;
    private Button logoutButton;

    private DatabaseReference driverRef;
    private FirebaseUser currentUser;
    private StorageReference storageRef;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_profile);

        profileImageView = findViewById(R.id.profileImageView);
        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        busIdTextView = findViewById(R.id.busIdTextView);
        routeTextView = findViewById(R.id.routeTextView);
        statusTextView = findViewById(R.id.statusTextView);
        updateNameButton = findViewById(R.id.updateNameButton);
        logoutButton = findViewById(R.id.logoutButton);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Set a background color for the curved shape
        bottomNavigationView.setBackgroundColor(ContextCompat.getColor(this, R.color.bottom_navigation_background));

        // Set a listener for bottom navigation item clicks
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle bottom navigation item clicks
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        // Handle Home action
                        Intent profileIntent = new Intent(DriverProfileActivity.this, DriverActivity.class);
                        startActivity(profileIntent);
                        return false;
                    case R.id.navigation_map:
                        // Handle Profile action
                        Intent mapIntent = new Intent(DriverProfileActivity.this, DriverMapsActivity.class);
                        startActivity(mapIntent);
                        return true;
                    case R.id.navigation_profile:
                        // Handle Logout action
                        return false;
                }
                return false;
            }
        });

        // Create a curved shape for the bottom navigation bar
        View curvedView = findViewById(R.id.curvedView);
        curvedView.setBackground(new CurvedBottomNavigationViewBackground(getResources().getColor(R.color.bottom_navigation_background), 150));

                // Get a reference to the "drivers" node in the Firebase Realtime Database
        driverRef = FirebaseDatabase.getInstance().getReference("drivers");
        // Get the current user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        // Get a reference to the Firebase Storage
        storageRef = FirebaseStorage.getInstance().getReference();

        // Set the current user's email
        if (currentUser != null) {
            emailTextView.setText(currentUser.getEmail());
        }

        // Load and display the driver's profile image
        loadProfileImage();

        // Load and display the driver's profile information
        loadProfileInfo();

        updateNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpdateNameDialog();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutDriver();
            }
        });
    }

    private void loadProfileImage() {
        // Check if the current user is authenticated
        if (currentUser != null) {

            // Get the driver's profile image file name from the Firebase Authentication user ID
            String profileImageFileName = "profile_images/" + currentUser.getUid() + ".jpg";
            StorageReference profileImageRef = storageRef.child(profileImageFileName);

            // Download the profile image file and display it in the ImageView
            profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    // Use an image loading library like Picasso or Glide for better performance
                    Glide.with(DriverProfileActivity.this)
                            .load(uri)
                            .placeholder(R.drawable.default_profile_image)
                            .error(R.drawable.error_image)
                            .into(profileImageView);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("DriverProfileActivity", "Failed to load profile image", e);
                    Toast.makeText(DriverProfileActivity.this, "Failed to load profile image", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadProfileInfo() {
        // Check if the current user is authenticated
        if (currentUser != null) {

            // Get a reference to the "users" node in the database
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("drivers");

            // Get the driver's unique ID
            String driverId = currentUser.getUid();

            String email = currentUser.getEmail();
            String BusId = null;
            // Find the index of the '@' symbol
            int atIndex = email.indexOf('@');

            if (atIndex != -1) {
                // Extract the substring before the '@' symbol
                BusId = email.substring(0, atIndex);

                // Print the extracted username
                System.out.println("Username: " + BusId);
            } else {
                // Handle the case when the email does not contain an '@' symbol
                System.out.println("Invalid email format");
            }

            // Retrieve the driver's profile information from the database
            assert BusId != null;

            //DatabaseReference userRefererence = usersRef.child(driverId);
            DatabaseReference driverReference = driverRef.child(BusId);
            driverReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Driver driver = dataSnapshot.getValue(Driver.class);
                        if (driver != null) {
                            // Set the driver's name, bus ID, route, and status
                            nameTextView.setText(driver.getName());
                            busIdTextView.setText(driver.getBusId());
                            routeTextView.setText(driver.getRoute());
                            statusTextView.setText(driver.isOnline() ? "Online" : "Offline");
                        }
                    } else {
                        Toast.makeText(DriverProfileActivity.this, "Driver data not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(DriverProfileActivity.this, "Failed to read driver data. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    private void showUpdateNameDialog() {
        // Create a dialog to prompt the driver to update their name
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Name");

        // Inflate the custom layout for the dialog
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_update_name, null);
        builder.setView(dialogView);

        // Get a reference to the EditText for the new name
        EditText nameEditText = dialogView.findViewById(R.id.nameEditText);

        // Set the current name as the default text in the EditText
        nameEditText.setText(nameTextView.getText());

        // Set up the buttons for the dialog
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get the new name entered by the driver
                String newName = nameEditText.getText().toString().trim();

                // Check if the new name is empty
                if (!TextUtils.isEmpty(newName)) {
                    // Update the driver's name in the database
                    updateDriverName(newName);
                } else {
                    Toast.makeText(DriverProfileActivity.this, "Please enter a valid name", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Cancel the dialog
                dialog.dismiss();
            }
        });

        // Show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateDriverName(String newName) {
        // Check if the current user is authenticated
        if (currentUser != null) {
            // Get the driver's unique ID
            String driverId = currentUser.getUid();

            // Update the driver's name in the database
            driverRef.child(driverId).child("name").setValue(newName)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(DriverProfileActivity.this, "Name updated successfully", Toast.LENGTH_SHORT).show();
                            // Update the displayed name
                            nameTextView.setText(newName);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(DriverProfileActivity.this, "Failed to update name. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }

    private void logoutDriver() {
        // Sign out the current user
        FirebaseAuth.getInstance().signOut();

        // Redirect to the LoginActivity
        Intent intent = new Intent(DriverProfileActivity.this, SignInActivity.class);
        startActivity(intent);
        finish();
    }

    // Handle the result of image selection from gallery
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
                uploadProfileImage(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to select image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadProfileImage(Bitmap bitmap) {
        // Check if the current user is authenticated
        if (currentUser != null) {
            // Get the driver's profile image file name from the Firebase Authentication user ID
            String profileImageFileName = "profile_images/" + currentUser.getUid() + ".jpg";
            StorageReference profileImageRef = storageRef.child(profileImageFileName);

            // Convert the Bitmap to bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageData = baos.toByteArray();

            // Upload the image to Firebase Storage
            UploadTask uploadTask = profileImageRef.putBytes(imageData);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(DriverProfileActivity.this, "Profile image uploaded successfully", Toast.LENGTH_SHORT).show();
                    profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // Save the download URL to the Realtime Database under the driver's profile
                            saveProfileImageUrlToDatabase(uri.toString());
                        }
                    });
                    // Load and display the updated profile image
                    loadProfileImage();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(DriverProfileActivity.this, "Failed to upload profile image. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }
    private void saveProfileImageUrlToDatabase(String profileImageUrl) {
        // Check if the current user is authenticated
        if (currentUser != null) {
            // Get the driver's unique ID
            String driverId = currentUser.getUid();
            String email = currentUser.getEmail();
            String BusId = null;
            // Find the index of the '@' symbol
            int atIndex = email.indexOf('@');

            if (atIndex != -1) {
                // Extract the substring before the '@' symbol
                BusId = email.substring(0, atIndex);

                // Print the extracted username
                System.out.println("Username: " + BusId);
            } else {
                // Handle the case when the email does not contain an '@' symbol
                System.out.println("Invalid email format");
            }

            // Update the driver's profileImageUrl in the database
            driverRef.child(BusId).child("imageUrl").setValue(profileImageUrl)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Profile image URL successfully saved in the database
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Failed to update profile image URL in the database
                            Toast.makeText(DriverProfileActivity.this, "Failed to update profile image URL. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }
    public void onProfileImageClick(View view) {
        // Open the gallery to select an image
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), PICK_IMAGE_REQUEST);
    }
}
