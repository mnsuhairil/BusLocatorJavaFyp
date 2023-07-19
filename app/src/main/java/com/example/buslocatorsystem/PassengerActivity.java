/*
package com.example.buslocatorsystem;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;


public class PassengerActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Firebase variables
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser mUser;

    // Google Maps variables
    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Marker mSelectedBusStopMarker;
    private Marker mCurrentLocationMarker;
    private Polyline mPolyline;
    private LatLng mCurrentLatLng;
    private LatLng mSelectedBusStopLatLng;
    private static final double AVERAGE_WALKING_SPEED = 1.4; // meters per minute




    // Other variables
    private String mSelectedBusId;
    private TextView textViewETA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger);

        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mUser = mAuth.getCurrentUser();

        // Find textViewETA by its ID
        textViewETA = findViewById(R.id.textViewETA);

        // Initialize Google Maps
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Code for passenger activity
        // ...


        // Example of selecting a bus
        mSelectedBusId = "bus123";
        // Example of requesting/waiting for the bus
        requestBus();

        // Example of canceling the request/wait
        cancelRequest();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Enable user's current location
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

        // Initialize location manager and listener
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                updateCurrentLocationMarker();
                // Calculate and display estimated time of arrival
                calculateETA();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };

        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    0, 0, mLocationListener);
        }

        // Add onMapClickListener for selecting bus stops
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mSelectedBusStopLatLng = latLng;
                updateSelectedBusStopMarker();
                // Calculate and display estimated time of arrival
                calculateETA();
            }
        });
    }

    private void updateCurrentLocationMarker() {
        if (mMap != null && mCurrentLatLng != null) {
            if (mCurrentLocationMarker != null)
                mCurrentLocationMarker.remove();

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(mCurrentLatLng)
                    .title("Current Location");

            mCurrentLocationMarker = mMap.addMarker(markerOptions);
        }
    }

    private void updateSelectedBusStopMarker() {
        if (mMap != null && mSelectedBusStopLatLng != null) {
            if (mSelectedBusStopMarker != null)
                mSelectedBusStopMarker.remove();

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(mSelectedBusStopLatLng)
                    .title("Selected Bus Stop");

            mSelectedBusStopMarker = mMap.addMarker(markerOptions);

            // Draw polyline between current location and selected bus stop
            drawPolyline();
        }
    }

    private void drawPolyline() {
        if (mMap != null && mCurrentLatLng != null && mSelectedBusStopLatLng != null) {
            if (mPolyline != null)
                mPolyline.remove();

            PolylineOptions polylineOptions = new PolylineOptions()
                    .add(mCurrentLatLng)
                    .add(mSelectedBusStopLatLng)
                    .color(Color.BLUE)
                    .width(5);

            mPolyline = mMap.addPolyline(polylineOptions);
        }
    }

    private void calculateETA() {
        if (mCurrentLatLng != null && mSelectedBusStopLatLng != null) {
            // Get the current time in milliseconds
            long currentTimeMillis = System.currentTimeMillis();

            // Get the distance between the current location and selected bus stop in meters
            double distance = getDistance(mCurrentLatLng.latitude, mCurrentLatLng.longitude,
                    mSelectedBusStopLatLng.latitude, mSelectedBusStopLatLng.longitude);

            // Estimate the travel time in minutes
            double estimatedTravelTimeMinutes = distance / AVERAGE_WALKING_SPEED;

            // Calculate the estimated arrival time in milliseconds
            long estimatedArrivalTimeMillis = currentTimeMillis + (long) (estimatedTravelTimeMinutes * 60 * 1000);

            // Format the estimated arrival time
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String estimatedArrivalTime = sdf.format(new Date(estimatedArrivalTimeMillis));

            // Display the estimated arrival time to the user
            textViewETA.setText("Estimated Time of Arrival (ETA): " + estimatedArrivalTime);
        }
    }
    private double getDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        return distance;
    }

    private void requestBus() {
        if (mUser != null) {
            // Example code for requesting/waiting for the bus

            // Get the current timestamp
            Date currentTime = new Date();

            // Store the request in the database
            String requestId = mDatabase.child("requests").push().getKey();
            Request request = new Request(requestId, mUser.getUid(), mSelectedBusId, currentTime);
            mDatabase.child("requests").child(requestId).setValue(request)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Request sent successfully
                                // You can perform any additional actions here

                                // For example, show a toast message
                                Toast.makeText(PassengerActivity.this, "Bus request sent!", Toast.LENGTH_SHORT).show();

                                */
/*//*
/ Or navigate to another activity
                                Intent intent = new Intent(PassengerActivity.this, WaitingActivity.class);
                                intent.putExtra("requestId", requestId);
                                startActivity(intent);

                                // Or update UI elements
                                updateUI();*//*


                                // etc.
                            } else {
                                // Request failed
                                // You can handle the failure here

                                // For example, show an error message
                                Toast.makeText(PassengerActivity.this, "Failed to send bus request.", Toast.LENGTH_SHORT).show();

                                // Or perform specific error handling
                                Exception exception = task.getException();
                                if (exception instanceof FirebaseException) {
                                    // Handle Firebase-related errors
                                    Log.e(TAG, "Firebase Error: " + exception.getMessage());
                                } else {
                                    // Handle other types of errors
                                    Log.e(TAG, "Request Error: " + exception.getMessage());
                                }
                            }
                        }
                    });

            // Notify the driver that a passenger is waiting
            notifyDriver();
        }
    }


    private void cancelRequest() {
        if (mUser != null) {
            // Check if the request exists in the database
            DatabaseReference requestRef = mDatabase.child("requests").child(mUser.getUid());
            requestRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Remove the request from the database
                        requestRef.removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Notify the driver that the request has been canceled
                                        notifyDriver();
                                        Toast.makeText(PassengerActivity.this, "Request canceled successfully", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(PassengerActivity.this, "Failed to cancel request. Please try again.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(PassengerActivity.this, "No active request to cancel", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(PassengerActivity.this, "Error occurred while canceling request. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private void notifyDriver() {
        if (mSelectedBusId != null) {
            // Get a reference to the driver's node in the database
            DatabaseReference driverRef = mDatabase.child("drivers").child(mSelectedBusId);

            // Set the notification flag to true for the driver
            driverRef.child("notification").setValue(true)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(PassengerActivity.this, "Driver notified successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(PassengerActivity.this, "Failed to notify driver. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(PassengerActivity.this, "No bus selected", Toast.LENGTH_SHORT).show();
        }
    }

    // Code for other passenger-related functionality
    // ...
    */
/*private void exampleFunctionality() {
        // Example code for other passenger-related functionality
        // ...

        // For example, you can perform additional actions or navigate to another activity based on user interactions
        // Here's an example of navigating to another activity
        Intent intent = new Intent(PassengerActivity.this, OtherActivity.class);
        startActivity(intent);
    }*//*

}
*/
