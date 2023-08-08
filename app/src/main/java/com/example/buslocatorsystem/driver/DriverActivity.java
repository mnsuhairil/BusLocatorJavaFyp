package com.example.buslocatorsystem.driver;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.example.buslocatorsystem.constructor.Driver;
import com.example.buslocatorsystem.NotificationUtils;
import com.example.buslocatorsystem.R;
import com.example.buslocatorsystem.SignInActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;


import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;

import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class DriverActivity extends AppCompatActivity implements SensorEventListener,OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener,GoogleMap.InfoWindowAdapter {

    private Spinner spinnerBusOilStatus;
    private Button btnIncrementPassenger;
    private Button btnDecrementPassenger;
    private int totalPassengerCount = 0;
    private DatabaseReference driverRef;
    private TextView editTextTotalPassenger;
    private Button btnUpdateInfo;
    private MeowBottomNavigation bottomNavigation;
    RelativeLayout driverHome, driverMap, driverProfile;

    //driver map codes
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private DatabaseReference mDatabase;
    private Marker driverMarker;

    private Map<String, Marker> passengerMarkers = new HashMap<>();
    private static final int MARKER_WIDTH = 120;  // in pixels
    private static final int MARKER_HEIGHT = 120; // in pixels

    private double fixedLat = 0, fixedLng =0;
    private ProgressBar progressBar;

    // Declare the Polyline variable
    private Polyline polyline;

    // Inside your DriverMapsActivity class
    private PlacesClient placesClient;

    private String driverCurrentUid ;
    private String driverCurrentId;
    private LottieAnimationView selectLocationButton, selectMapTypeButton; // Add a Button to your layout and assign it here
    private LatLng selectedLocation; // Add this variable to store the selected location
    // Get the current user from FirebaseAuth
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
    private Handler handler;
    private Runnable refreshMapRunnable;

    private DatabaseReference driversRef;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location currentLocation;
    private String routes;
    private LatLng driverlocaiton,fixedLocation;

    //driver profile codes
    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView profileImageView;
    private TextView nameTextView;
    private TextView emailTextView;
    private TextView busIdTextView;
    private TextView routeTextView;
    private TextView statusTextView;
    private Button updateNameButton;
    private Button logoutButton;
    private FirebaseUser currentUser;
    private StorageReference storageRef;
    private BottomNavigationView bottomNavigationView;
    private Marker passengerMarker;
    private String imgUrls;
    private Button startNavigationButton1;

    // Declare variables for accelerometer and magnetometer data
    private float[] accelerometerData1;
    private float[] magnetometerData1;
    private boolean isNavigationMode1 = false;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private float azimuth;
    private float[] gravity;
    private float[] geomagnetic;
    private float newBearing;
    private boolean cameraBool=false;
    private int delayMillis = 5000;
    private String nameCopy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver);

        // Initialize Firebase Messaging
        FirebaseMessaging.getInstance().setAutoInitEnabled(true);

        // Check if the user has granted notification permission. If not, request permission.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager.getNotificationChannel("chat_notifications") == null) {
                NotificationChannel channel = new NotificationChannel("chat_notifications", "Chat Notifications", NotificationManager.IMPORTANCE_HIGH);
                manager.createNotificationChannel(channel);
            }
        }

        // Register the sensor listener
       sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
       accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        driverHome = findViewById(R.id.driverHome);
        driverMap = findViewById(R.id.driverMap);
        driverProfile = findViewById(R.id.driverprofile);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        bottomNavigation.show(2, true);
        bottomNavigation.add(new MeowBottomNavigation.Model(1, R.drawable.ic_home));
        bottomNavigation.add(new MeowBottomNavigation.Model(2, R.drawable.ic_map));
        bottomNavigation.add(new MeowBottomNavigation.Model(3, R.drawable.profile));

        startNavigationButton1 = findViewById(R.id.start_navigation_button);
        startNavigationButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(startNavigationButton1.getText().equals("Start Driving")){
                    isNavigationMode1 = true;

                    startNavigationButton1.setText("STOP");
                }else if (startNavigationButton1.getText().equals("STOP")){
                    isNavigationMode1 = false;
                    startNavigationButton1.setText("Start Driving");
                    delayMillis = 2000;
                }
            }
        });
        bottomNavigation.setOnClickMenuListener(new Function1<MeowBottomNavigation.Model, Unit>() {
            @Override
            public Unit invoke(MeowBottomNavigation.Model model) {
                // YOUR CODES

                switch (model.getId()) {

                case 1:
                    driverHome.setVisibility(View.VISIBLE);
                    driverMap.setVisibility(View.GONE);
                    driverProfile.setVisibility(View.GONE);
                    break;

                case 2:
                    driverHome.setVisibility(View.GONE);
                    driverMap.setVisibility(View.VISIBLE);
                    driverProfile.setVisibility(View.GONE);
                    break;

                case 3:
                    driverHome.setVisibility(View.GONE);
                    driverMap.setVisibility(View.GONE);
                    driverProfile.setVisibility(View.VISIBLE);
                    break;
            }
                return null;
        }
        });


        spinnerBusOilStatus = findViewById(R.id.spinnerBusOilStatus);
        btnIncrementPassenger = findViewById(R.id.btnIncrementPassenger);
        editTextTotalPassenger = findViewById(R.id.editTextTotalPassenger);
        btnDecrementPassenger = findViewById(R.id.btnDecrementPassenger);
        btnUpdateInfo = findViewById(R.id.btnUpdateStatus);
        ArrayAdapter<CharSequence> oilStatusAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.bus_oil_statuses,
                android.R.layout.simple_spinner_item
        );
        oilStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBusOilStatus.setAdapter(oilStatusAdapter);

        // Set click listeners for the passenger count buttons
        btnIncrementPassenger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incrementPassengerCount();
            }
        });

        btnDecrementPassenger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decrementPassengerCount();
            }
        });

        driverRef = FirebaseDatabase.getInstance().getReference("drivers");

        btnUpdateInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateStatus();
            }
        });

     //driver map codes
        progressBar = findViewById(R.id.progressBar);
        // Initialize Places SDK
        Places.initialize(getApplicationContext(), "AIzaSyCuBp-Fnefr1Xe5RxLgxMh3D2OzOQzxyaE");

        // Create a PlacesClient instance
        placesClient = Places.createClient(this);
        // Initialize Firebase Realtime Database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        handler = new Handler();
        refreshMapRunnable = new Runnable() {
            @Override
            public void run() {
                // Refresh the map
                refreshMap();

                // Schedule the next map refresh after 1 second
                handler.postDelayed(this, 1000);
            }
        };

        driverCurrentUid = firebaseUser.getUid();

        String stringModifier = firebaseUser.getEmail();
        String[] parts = stringModifier.split("@");
        String value = parts[0];
        driverCurrentId = value;

        // Initialize passengerMarkers map
        passengerMarkers = new HashMap<>();

        // Build GoogleApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        // Create LocationRequest
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000) // Update location every second (change as needed)
                .setFastestInterval(1000); // Fastest update interval

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        selectLocationButton = findViewById(R.id.selectLocationButton);
        selectLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }
        });

        selectMapTypeButton = findViewById(R.id.selectMapType);
        selectMapTypeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenuMapType(v);
            }
        });

        loadProfileInfo1();

        // driver profile codes
        profileImageView = findViewById(R.id.profileImageView);
        nameTextView = findViewById(R.id.nameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        busIdTextView = findViewById(R.id.busIdTextView);
        routeTextView = findViewById(R.id.routeTextView);
        statusTextView = findViewById(R.id.statusTextView);
        updateNameButton = findViewById(R.id.updateNameButton);
        logoutButton = findViewById(R.id.logoutButton);


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

    private void incrementPassengerCount() {
        if (totalPassengerCount<12)
        {
            totalPassengerCount++;
            updatePassengerCount();
        }else Toast.makeText(this, "Invalid passenger count. Maximum allowed is 12", Toast.LENGTH_SHORT).show();

    }

    private void decrementPassengerCount() {
        if (totalPassengerCount > 0) {
            totalPassengerCount--;
            updatePassengerCount();
        }
    }

    private void updatePassengerCount() {
            editTextTotalPassenger.setText(String.valueOf(totalPassengerCount));
    }

    private void updateStatus() {
        // Get the selected bus oil status from the spinner
        String selectedBusOilStatus = spinnerBusOilStatus.getSelectedItem().toString();
        String totalPassengerString = String.valueOf(totalPassengerCount);

        if (totalPassengerString.isEmpty() || selectedBusOilStatus.isEmpty()) {
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
            String busId = null;
            // Find the index of the '@' symbol
            int atIndex = email.indexOf('@');

            if (atIndex != -1) {
                // Extract the substring before the '@' symbol
                busId = email.substring(0, atIndex);

                // Print the extracted username
                System.out.println("Username: " + busId);
            } else {
                // Handle the case when the email does not contain an '@' symbol
                System.out.println("Invalid email format");
            }
            // Update the bus status with the total passenger count and bus oil status
            // You can modify this implementation based on your database structure
            // Here, we assume the driver has already logged in and their unique driver ID is available

            // Retrieve the existing driver object from the database
            DatabaseReference driverReference = driverRef.child(busId);
            driverReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Driver driver = dataSnapshot.getValue(Driver.class);
                        if (driver != null) {
                            // Update the driver object with the new status
                            driver.setTotalPassenger(totalPassenger);
                            driver.setBusOilStatus(selectedBusOilStatus);

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

    //driver Map Codes
    private void loadProfileInfo1() {
        // Check if the current user is authenticated
        if (firebaseUser != null) {

            // Get a reference to the "users" node in the database
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("drivers");

            // Get the driver's unique ID
            String driverId = driverCurrentId;



            // Retrieve the driver's profile information from the database
            assert driverId != null;
            DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference("drivers");

            //DatabaseReference userRefererence = usersRef.child(driverId);
            DatabaseReference driverReference = driverRef.child(driverId);
            driverReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Driver driver = dataSnapshot.getValue(Driver.class);
                        if (driver != null) {
                            // Set the driver's name, bus ID, route, and status
                            routes = driver.getRoute();
                            System.out.println(routes +"debug 1");
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
    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu()); // Create a new menu resource file named popup_menu.xml


        if(Objects.equals(routes, "Route 1")){
            popupMenu.getMenu().findItem(R.id.putatan).setVisible(true);
            popupMenu.getMenu().findItem(R.id.kota_kinabalu).setVisible(true);
            popupMenu.getMenu().findItem(R.id.kinarut).setVisible(false);
            popupMenu.getMenu().findItem(R.id.penampang).setVisible(false);
        }else if(Objects.equals(routes, "Route 2")){
            popupMenu.getMenu().findItem(R.id.putatan).setVisible(false);
            popupMenu.getMenu().findItem(R.id.kota_kinabalu).setVisible(true);
            popupMenu.getMenu().findItem(R.id.kinarut).setVisible(false);
            popupMenu.getMenu().findItem(R.id.penampang).setVisible(true);
        }else if(Objects.equals(routes, "Route 3")){
            popupMenu.getMenu().findItem(R.id.putatan).setVisible(false);
            popupMenu.getMenu().findItem(R.id.kota_kinabalu).setVisible(true);
            popupMenu.getMenu().findItem(R.id.kinarut).setVisible(true);
            popupMenu.getMenu().findItem(R.id.penampang).setVisible(false);
        }else{
            popupMenu.getMenu().findItem(R.id.putatan).setVisible(false);
            popupMenu.getMenu().findItem(R.id.kota_kinabalu).setVisible(false);
            popupMenu.getMenu().findItem(R.id.kinarut).setVisible(false);
            popupMenu.getMenu().findItem(R.id.penampang).setVisible(false);
            Toast.makeText(DriverActivity.this,"Having Error detecting the routes",Toast.LENGTH_SHORT ).show();
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.putatan:
                        // Handle Putatan selection
                        selectedLocation = new LatLng(5.8943, 116.0485);
                        break;
                    case R.id.kota_kinabalu:
                        // Handle Kota Kinabalu selection
                        selectedLocation = new LatLng(5.9763, 116.0689);
                        break;
                    case R.id.kinarut:
                        // Handle Putatan selection
                        selectedLocation = new LatLng(5.817371, 116.039113);
                        break;
                    case R.id.penampang:
                        // Handle Kota Kinabalu selection
                        selectedLocation = new LatLng(5.91144, 116.103663);
                        break;

                }

                // Remove the previous polyline (if it exists)
                if (polyline != null) {
                    polyline.remove();
                }

                // Call the getPolylineRoute() method with the selected location
                if (selectedLocation != null) {
                    getPolylineRoute(firebaseUser.getUid(), new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), selectedLocation);
                }

                return true;
            }
        });

        popupMenu.show();
    }
    private void showPopupMenuMapType(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu_map_type, popupMenu.getMenu()); // Create a new menu resource file named popup_menu.xml

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.MAP_TYPE_NORMAL:
                        // Handle Putatan selection
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        break;
                    case R.id.MAP_TYPE_TERRAIN:
                        // Handle Putatan selection
                        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        break;
                    case R.id.MAP_TYPE_SATELLITE:
                        // Handle Kota Kinabalu selection
                        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        break;
                    case R.id.MAP_TYPE_NONE:
                        // Handle Putatan selection
                        mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                        break;
                    case R.id.MAP_TYPE_HYBRID:
                        // Handle Kota Kinabalu selection
                        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        break;
                }
                return true;
            }
        });

        popupMenu.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        handler.removeCallbacks(refreshMapRunnable);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

            String uid = firebaseUser.getUid();
            // Store the current location in Firebase Realtime Database
            mDatabase.child("locations").child("drivers").child(uid).setValue(new LatLng(latitude, longitude));

            // Remove the previous marker (if it exists)
            if (driverMarker != null) {
                driverMarker.remove();
            }

            //get fixed locationn
            fixedLocation = selectedLocation;
            // Display the current location on the map
            driverlocaiton = new LatLng(latitude, longitude);

            // Modify the icon size
            Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.locationtrigger);
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 80, 80, false);
            BitmapDescriptor markerBusIcon = BitmapDescriptorFactory.fromBitmap(resizedBitmap);

            // Display the passenger's marker on the map
            driverMarker = mMap.addMarker(new MarkerOptions()
                    .position(driverlocaiton)
                    .title("Your Location").rotation(location.getBearing()).anchor(0.5f, 0.5f)
                    .icon(markerBusIcon));



            if(driverlocaiton != null && fixedLocation!=null){

                getPolylineRoute(uid, driverlocaiton, fixedLocation);
            }

           if (driverlocaiton!=null){
               if(!isNavigationMode1){
                   CameraPosition cameraPosition = new CameraPosition.Builder()
                           .target(driverlocaiton)
                           .zoom(15)
                           .tilt(0.0f)
                           .bearing(0.0f)
                           .build();

                   // Use CameraUpdateFactory.newCameraPosition with animateCamera to animate the camera smoothly
                   mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
               }else{
                   //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(driverlocaiton, 20));
               }
           }

        }
    }

    @SuppressLint("PotentialBehaviorOverride")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

        googleMap.setInfoWindowAdapter(this);

        // Iterate over the passenger markers map and update each marker
        DatabaseReference requestPickupsRef = mDatabase.child("locations").child("passengers");
        requestPickupsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                listenToRequestNode();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                listenToRequestNode();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }
    private void enableMyLocation() {
        // Check if location permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            /*fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {

                        currentLocation = location;
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                }
            });*/
        } else {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
        }
    }

    private void listenToRequestNode(){
        // Listen for changes in the requestPickups node
        DatabaseReference requestPickupsRef = mDatabase.child("requestPickups").child(driverCurrentId);
        requestPickupsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // Get the passenger ID
                String passengerId = dataSnapshot.getKey();
                String passengerNameNoti = dataSnapshot.child("passengerName").getValue(String.class);
                String passengerImgUrlNoti = dataSnapshot.child("imgUrl").getValue(String.class);
                //PushNotification(passengerId,passengerNameNoti,passengerImgUrlNoti);

                System.out.println("name and img url "+passengerNameNoti+" "+passengerImgUrlNoti);
                if (!Objects.equals(nameCopy, passengerNameNoti)){
                    nameCopy = passengerNameNoti;
                    NotificationUtils notificationUtils = new NotificationUtils(DriverActivity.this);
                    notificationUtils.sendPushNotification(passengerId, passengerNameNoti,passengerImgUrlNoti);
                }
                assert passengerId != null;
                getMarkerMultiplePassengers(passengerId);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // Get the passenger ID
                String passengerId = dataSnapshot.getKey();

                getMarkerMultiplePassengersForonChildChanged(passengerId);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                for (Marker marker : passengerMarkers.values()) {
                    marker.remove();
                }
                passengerMarkers.clear();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // Handle child moved event if needed
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error if needed
            }
        });
    }

    /*private void sendPushNotification(String passengerId,String Name, String imageUrl) {
        // Step 1: Create a custom layout for the notification
        RemoteViews customNotificationLayout = new RemoteViews(getPackageName(), R.layout.custom_notification_layout);

        // Step 2: Set the image, title, and text in the custom layout using Picasso
        try {
            Bitmap imageBitmap = Picasso.get().load(imageUrl).get(); // This will download the image synchronously, so it's better to use it in a background thread in a real application
            customNotificationLayout.setImageViewBitmap(R.id.notification_image, imageBitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        customNotificationLayout.setTextViewText(R.id.notification_title, "New Passenger Request Pickup");
        customNotificationLayout.setTextViewText(R.id.notification_text, Name); // Replace "PassengerName" with the actual passenger's name

        // Step 3: Set the custom layout to the notification builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "chat_notifications")
                .setSmallIcon(R.drawable.bus_transit)
                .setCustomContentView(customNotificationLayout) // Set the custom layout here
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Add any additional configuration to the notification as needed

        // Use a unique notification ID for each notification to avoid replacing previous notifications
        int notificationId = (int) System.currentTimeMillis();

        // Issue the notification
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(notificationId, builder.build());
    }*/
    private void refreshMap() {
        // Update the driver's location marker
        updateDriverMarker();

        // Update passenger markers
        updatePassengerMarkers();

        // Update the driver's route polyline
        updateDriverRoute();
    }
    private void updateDriverMarker() {
        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

            LatLng driverLocation = new LatLng(latitude, longitude);

            // Remove the previous marker (if it exists)
            if (driverMarker != null) {
                driverMarker.remove();
            }

            // Add the new marker
            driverMarker = mMap.addMarker(new MarkerOptions().position(driverLocation).title("Current Location"));


        }
    }
    private void updateDriverRoute() {
        // Get the driver's route polyline from Firebase Realtime Database

        // Remove the previous polyline (if it exists)
        if (polyline != null) {
            polyline.remove();
        }

        if(driverlocaiton != null && fixedLocation!=null){
            getPolylineRoute(driverCurrentUid, driverlocaiton, fixedLocation);
        }
    }
    private void updatePassengerMarkers() {
        // Iterate over the passenger markers map and update each marker
        // Remove the previous passenger markers
        for (Marker marker : passengerMarkers.values()) {
            marker.remove();
        }
        passengerMarkers.clear();
        // Listen for changes in the requestPickups node
        DatabaseReference requestPickupsRef = mDatabase.child("requestPickups").child(driverCurrentId);
        requestPickupsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // Get the passenger ID
                String passengerId = dataSnapshot.getKey();

                getMarkerMultiplePassengers(passengerId);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // Get the passenger ID
                String passengerId = dataSnapshot.getKey();

                getMarkerMultiplePassengersForonChildChanged(passengerId);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                // Get the passenger ID
                String passengerId = dataSnapshot.getKey();

                // Remove the passenger's marker from the map
                Marker passengerMarker = passengerMarkers.remove(passengerId);
                if (passengerMarker != null) {
                    passengerMarker.remove();
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // Handle child moved event if needed
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error if needed
            }
        });
    }

    private void getPolylineRoute(String driverId, LatLng driverLocation, LatLng fixedLocation) {
        Task<DirectionsResult> directionsResultTask = getDirections(driverLocation, fixedLocation);

        directionsResultTask.addOnSuccessListener(new OnSuccessListener<DirectionsResult>() {
            @Override
            public void onSuccess(DirectionsResult directionsResult) {
                // Parse the directions result and create polyline options
                if (directionsResult != null && directionsResult.routes != null && directionsResult.routes.length > 0) {
                    // Get the first route
                    com.google.maps.model.LatLng[] path = directionsResult.routes[0].overviewPolyline.decodePath().toArray(new com.google.maps.model.LatLng[0]);

                    // Create polyline options
                    PolylineOptions polylineOptions = new PolylineOptions();
                    for (com.google.maps.model.LatLng latLng : path) {
                        polylineOptions.add(new LatLng(latLng.lat, latLng.lng));
                    }
                    polylineOptions.width(10); // Set the width of the polyline
                    polylineOptions.color(Color.RED); // Set the color of the polyline

                    if (polyline!=null)polyline.remove();
                    // Add the polyline to the map
                    polyline = mMap.addPolyline(polylineOptions);

                    // Store the driver route in Firebase Realtime Database
                    DatabaseReference driverRouteRef = mDatabase.child("direction").child(driverId);
                    driverRouteRef.setValue(polylineOptions.getPoints())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("Firebase", "Driver route stored successfully");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("Firebase", "Failed to store driver route: " + e.getMessage());
                                }
                            });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle the failure case
                Log.e("Directions", "Failed to get directions: " + e.getMessage());
            }
        });
    }
    private Task<DirectionsResult> getDirections(LatLng origin, LatLng destination) {
        // Create a new GeoApiContext with your API key
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey("AIzaSyCuBp-Fnefr1Xe5RxLgxMh3D2OzOQzxyaE")
                .build();

        // Create the directions request
        DirectionsApiRequest request = DirectionsApi.newRequest(context)
                .origin(new com.google.maps.model.LatLng(origin.latitude, origin.longitude))
                .destination(new com.google.maps.model.LatLng(destination.latitude, destination.longitude))
                .mode(TravelMode.DRIVING); // You can change the travel mode if needed

        // Execute the request asynchronously
        return new Task<DirectionsResult>() {
            @Override
            public boolean isComplete() {
                return false;
            }

            @Override
            public boolean isSuccessful() {
                return false;
            }

            @Override
            public DirectionsResult getResult() {
                return null;
            }

            @Override
            public <X extends Throwable> DirectionsResult getResult(Class<X> aClass) throws X {
                return null;
            }

            @Override
            public boolean isCanceled() {
                return false;
            }

            @Override
            public Exception getException() {
                return null;
            }

            @Override
            public Task<DirectionsResult> addOnSuccessListener(OnSuccessListener<? super DirectionsResult> onSuccessListener) {
                request.setCallback(new com.google.maps.PendingResult.Callback<DirectionsResult>() {
                    @Override
                    public void onResult(DirectionsResult result) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                onSuccessListener.onSuccess(result);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        Log.e("Directions", "Failed to get directions: " + e.getMessage());
                    }
                });

                return this;
            }

            @NonNull
            @Override
            public Task<DirectionsResult> addOnSuccessListener(@NonNull Activity activity, @NonNull OnSuccessListener<? super DirectionsResult> onSuccessListener) {
                return null;
            }

            @NonNull
            @Override
            public Task<DirectionsResult> addOnSuccessListener(@NonNull Executor executor, @NonNull OnSuccessListener<? super DirectionsResult> onSuccessListener) {
                return null;
            }

            @Override
            public Task<DirectionsResult> addOnFailureListener(OnFailureListener onFailureListener) {
                // Not used in this implementation
                return null;
            }

            @NonNull
            @Override
            public Task<DirectionsResult> addOnFailureListener(@NonNull Activity activity, @NonNull OnFailureListener onFailureListener) {
                return null;
            }

            @NonNull
            @Override
            public Task<DirectionsResult> addOnFailureListener(@NonNull Executor executor, @NonNull OnFailureListener onFailureListener) {
                return null;
            }
        };
    }

    private void getMarkerMultiplePassengersForonChildChanged(String passengerId) {
        System.out.println("passenger id 2 ="+passengerId);
        //modify the uid
        String modifiedPassengerId = passengerId.substring(4);

        // Retrieve the latitude and longitude for a passenger
        DatabaseReference locationsRef = FirebaseDatabase.getInstance().getReference().child("locations");
        locationsRef.child("passengers").child(modifiedPassengerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Get the passenger's updated location
                    double latitude = dataSnapshot.child("latitude").getValue(Double.class);
                    double longitude = dataSnapshot.child("longitude").getValue(Double.class);
                    LatLng passengerLocation = new LatLng(latitude, longitude);

                    // Update the passenger's marker on the map
                    Marker passengerMarker = passengerMarkers.get(modifiedPassengerId);
                    if (passengerMarker != null) {
                        System.out.println("Im here = " +latitude+ longitude );
                        passengerMarker.setPosition(passengerLocation);
                    }

                } else {
                    // Handle the case when the passenger data does not exist
                    Log.d("PassengerData", "Passenger data does not exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle potential errors
            }
        });

    }

    private void getMarkerMultiplePassengers(String passengerId) {
        System.out.println("passenger id 1 ="+passengerId);

        //modify the uid
        String modifiedPassengerId = passengerId.substring(4);

        // Retrieve the latitude and longitude for a passenger
        DatabaseReference locationsRef = FirebaseDatabase.getInstance().getReference().child("locations");
        locationsRef.child("passengers").child(modifiedPassengerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Get the passenger's location
                    double latitude = dataSnapshot.child("latitude").getValue(Double.class);
                    double longitude = dataSnapshot.child("longitude").getValue(Double.class);

                    LatLng passengerLocation = new LatLng(latitude, longitude);

                    //modify the icon size
                    Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.person);
                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, MARKER_WIDTH, MARKER_HEIGHT, false);
                    BitmapDescriptor markerIcon = BitmapDescriptorFactory.fromBitmap(resizedBitmap);
                    // Remove the previous marker for this passenger (if it exists)
                    Marker previousMarker = passengerMarkers.get(modifiedPassengerId);
                    if (previousMarker != null) {
                        previousMarker.remove();
                    }
                    // Display the passenger's marker on the map
                    passengerMarker = mMap.addMarker(new MarkerOptions()
                            .position(passengerLocation)
                            .icon(markerIcon));
                    passengerMarker.setTag(passengerId);
                    passengerMarkers.put(modifiedPassengerId, passengerMarker);

                    // Show the info window for the passenger marker
                    assert passengerMarker != null;
                    passengerMarker.showInfoWindow();
                } else {
                    // Handle the case when the passenger data does not exist
                    Log.d("PassengerData", "Passenger data does not exist");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle potential errors
            }
        });
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null; // Return null here to use the default info window background
    }
    private View infoView;
    private String name,phone,gender;
    @Override
    public View getInfoContents(Marker marker) {
        // Create a custom view for the info window

        String passengerId = marker.getTag().toString();

        DatabaseReference requestPickupsRef = mDatabase.child("requestPickups").child(driverCurrentId).child(passengerId);
        requestPickupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                 name = snapshot.child("passengerName").getValue(String.class);
                 phone = snapshot.child("phoneNumber").getValue(String.class);
                 gender = snapshot.child("gender").getValue(String.class);
                imgUrls = snapshot.child("imgUrl").getValue(String.class);
                // Find the views in your custom layout
                System.out.println("debuging request : "+name+phone+gender+imgUrls);

                infoView = getLayoutInflater().inflate(R.layout.custom_info_window, null);
                // infoView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

                CircleImageView infoImage = infoView.findViewById(R.id.imageView);

                TextView titleName = infoView.findViewById(R.id.nameTextView);
                TextView infoPhone = infoView.findViewById(R.id.phoneTextView);
                TextView infoGender = infoView.findViewById(R.id.genderTextView);

                if (imgUrls != null && !imgUrls.isEmpty()) {
                    Glide.with(DriverActivity.this)
                            .load(imgUrls)
                            .placeholder(R.drawable.default_profile_image) // Placeholder image if the user has not uploaded their image
                            .into(infoImage);

                } else {
                    // If the URL is not available, set a default placeholder image
                    infoImage.setImageResource(R.drawable.default_profile_image);
                }
                if(name!=null){
                    titleName.setText(name); // Title of the marker
                    infoPhone.setText(phone); // Add your additional information here*/
                    infoGender.setText(gender);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        return infoView;

    }

    //driver Profile code
    private void loadProfileInfoDialog (){
        if (currentUser != null) {

            // Get the driver's profile image file name from the Firebase Authentication user ID
            String profileImageFileName = "profile_images/" + currentUser.getUid() + ".jpg";
            StorageReference profileImageRef = storageRef.child(profileImageFileName);

            // Download the profile image file and display it in the ImageView
            profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    // Use an image loading library like Picasso or Glide for better performance
                    Glide.with(DriverActivity.this)
                            .load(uri)
                            .placeholder(R.drawable.default_profile_image)
                            .error(R.drawable.error_image)
                            .into(profileImageView);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("DriverProfileActivity", "Failed to load profile image", e);
                    Toast.makeText(DriverActivity.this, "Failed to load profile image", Toast.LENGTH_SHORT).show();
                }
            });
        }
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
                    Glide.with(DriverActivity.this)
                            .load(uri)
                            .placeholder(R.drawable.default_profile_image)
                            .error(R.drawable.error_image)
                            .into(profileImageView);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("DriverProfileActivity", "Failed to load profile image", e);
                    Toast.makeText(DriverActivity.this, "Failed to load profile image", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(DriverActivity.this, "Please enter a valid name", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(DriverActivity.this, "Name updated successfully", Toast.LENGTH_SHORT).show();
                            // Update the displayed name
                            nameTextView.setText(newName);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(DriverActivity.this, "Failed to update name. Please try again.", Toast.LENGTH_SHORT).show();
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
        Intent intent = new Intent(DriverActivity.this, SignInActivity.class);
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
                    Toast.makeText(DriverActivity.this, "Profile image uploaded successfully", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(DriverActivity.this, "Failed to upload profile image. Please try again.", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(DriverActivity.this, "Failed to update profile image URL. Please try again.", Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onResume() {
        super.onResume();
        // Register the sensor listeners
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the sensor listeners to save battery
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values;
        }

        if (gravity != null && geomagnetic != null) {
            float[] rotationMatrix = new float[9];
            if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(rotationMatrix, orientation);
                azimuth = (float) Math.toDegrees(orientation[0]);
                // Update the map's camera rotation
                // Create a Handler object
                // For example, update the camera rotation
                updateCameraRotation();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for this example
    }

    private void updateCameraRotation() {
        // Get the current camera position
        CameraPosition currentCameraPosition = mMap.getCameraPosition();

        if (mMap != null&& isNavigationMode1) {
            // Get the current camera position

            // Calculate the new camera rotation (bearing) based on the azimuth
            newBearing = azimuth;
            // Smoothly animate the camera rotation using CameraUpdateFactory.newCameraPosition
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(driverlocaiton)
                    .zoom(20)
                    .tilt(90.0f)
                    .bearing(newBearing)
                    .build();

            // Set the duration for the animation in milliseconds
            int animationDuration = 12; // Adjust as needed

            // Use CameraUpdateFactory.newCameraPosition with animateCamera to animate the camera smoothly

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),animationDuration,null);

        }
    }
}
