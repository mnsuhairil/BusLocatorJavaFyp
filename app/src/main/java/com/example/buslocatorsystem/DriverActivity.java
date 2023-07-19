package com.example.buslocatorsystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.ValueEventListener;

public class DriverActivity extends AppCompatActivity {

    private EditText editTextTotalPassenger;
    private EditText editTextBusOilStatus;
    private Button btnUpdateStatus;
    private DatabaseReference driverRef;
    private BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

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
                        return true;
                    case R.id.navigation_map:
                        // Handle Profile action
                        Intent mapIntent = new Intent(DriverActivity.this, DriverMapsActivity.class);
                        startActivity(mapIntent);
                        return true;
                    case R.id.navigation_profile:
                        // Handle Logout action
                        Intent profileIntent = new Intent(DriverActivity.this, DriverProfileActivity.class);
                        startActivity(profileIntent);
                        return true;
                }
                return false;
            }
        });

        // Create a curved shape for the bottom navigation bar
        View curvedView = findViewById(R.id.curvedView);
        curvedView.setBackground(new CurvedBottomNavigationViewBackground(getResources().getColor(R.color.bottom_navigation_background), 150));

        editTextTotalPassenger = findViewById(R.id.editTextTotalPassenger);
        editTextBusOilStatus = findViewById(R.id.editTextBusOilStatus);
        btnUpdateStatus = findViewById(R.id.btnUpdateStatus);

        // Get a reference to the "drivers" node in the Firebase Realtime Database
        driverRef = FirebaseDatabase.getInstance().getReference("drivers");

        btnUpdateStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateStatus();
            }
        });
    }

    private void updateStatus() {
        String totalPassengerString = editTextTotalPassenger.getText().toString().trim();
        String busOilStatus = editTextBusOilStatus.getText().toString().trim();

        if (totalPassengerString.isEmpty() || busOilStatus.isEmpty()) {
            Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int totalPassenger = Integer.parseInt(totalPassengerString);
        if (totalPassenger > 12) {
            Toast.makeText(this, "Invalid passenger count. Maximum allowed is 12", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
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
            // Update the bus status with the total passenger count and bus oil status
            // You can modify this implementation based on your database structure
            // Here, we assume the driver has already logged in and their unique driver ID is available

            // Retrieve the existing driver object from the database
            DatabaseReference driverReference = driverRef.child(BusId);
            driverReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Driver driver = dataSnapshot.getValue(Driver.class);
                        if (driver != null) {
                            // Update the driver object with the new status
                            driver.setTotalPassenger(totalPassenger);
                            driver.setBusOilStatus(busOilStatus);

                            // Update the driver's status in the Firebase Realtime Database
                            driverReference.setValue(driver)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(DriverActivity.this, "Bus status updated successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(DriverActivity.this, "Failed to update bus status. Please try again.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(DriverActivity.this, "Driver data not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(DriverActivity.this, "Failed to read driver data. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }
}
