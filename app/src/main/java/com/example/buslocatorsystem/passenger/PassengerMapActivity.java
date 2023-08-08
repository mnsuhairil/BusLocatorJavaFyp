package com.example.buslocatorsystem.passenger;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.example.buslocatorsystem.constructor.RequestPickupData;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.maps.GeoApiContext;


import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

import android.app.AlertDialog;


import com.example.buslocatorsystem.R;

public class PassengerMapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private Marker driverMarker;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LottieAnimationView selectMapTypeButton;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private DatabaseReference mDatabase;
    private ActionBarDrawerToggle toggle;
    private DrawerLayout drawerLayout;
    private DatabaseReference databaseRef;
    private Location currentLocation;
    private Polyline polyline; // Variable to hold the polyline
    private GeoApiContext geoApiContext; // GeoApiContext instance for Directions API
    private String currentBusId;
    private Button requestPickupButton;
    private String currentBusUID;
    private boolean x;
    private double currentBusLng,currentBusLat;
    private FusedLocationProviderClient fusedLocationProviderClient;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private FirebaseUser currentUser;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Marker passengerMarker;

    private MeowBottomNavigation bottomNavigation;
    RelativeLayout passengerHome, passengerNearbyBustop;

    //profile
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;

    private ImageView profileImageView;
    private TextView emailTextView;
    private TextView nameTextView;
    private TextView usernameTextView;
    private TextView phoneTextView;
    private TextView passwordTextView;

    private Uri selectedImageUri; // Initialize Firebase Realtime Database

    private Handler handler;
    private String mapMenu;
    private String menuMap;
    private String usernamePassenger;
    private TextView genderTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_map);

        menuMap = getIntent().getStringExtra("mapFinder");

        passengerHome = findViewById(R.id.passengerMap);
        passengerNearbyBustop = findViewById(R.id.nearbyBustop);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        bottomNavigation.show(1, true);
        bottomNavigation.add(new MeowBottomNavigation.Model(1, R.drawable.ic_map));
        bottomNavigation.add(new MeowBottomNavigation.Model(2, R.drawable.profile));

        bottomNavigation.setOnClickMenuListener(new Function1<MeowBottomNavigation.Model, Unit>() {
            @Override
            public Unit invoke(MeowBottomNavigation.Model model) {
                // YOUR CODES

                switch (model.getId()) {

                    case 1:
                        passengerHome.setVisibility(View.VISIBLE);
                        passengerNearbyBustop.setVisibility(View.GONE);

                        break;

                    case 2:
                        passengerHome.setVisibility(View.GONE);
                        passengerNearbyBustop.setVisibility(View.VISIBLE);


                        break;

                }
                return null;
            }
        });

            // Get current user from FirebaseAuth
            currentUser = FirebaseAuth.getInstance().getCurrentUser();

            // Initialize Firebase Realtime Database
            mDatabase = FirebaseDatabase.getInstance().getReference();

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

            geoApiContext = new GeoApiContext.Builder()
                    .apiKey("AIzaSyCuBp-Fnefr1Xe5RxLgxMh3D2OzOQzxyaE")
                    .build();

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.mapFragment);
            assert mapFragment != null;
            mapFragment.getMapAsync(this);


            drawerLayout = findViewById(R.id.drawerLayout);
            NavigationView navigationView = findViewById(R.id.navigationView);
            toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

            displayNavHeader();

            //getcurent user id
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = mAuth.getCurrentUser();
            String passengerUserId = currentUser.getUid();
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            // Set up the request pickup button
            requestPickupButton = findViewById(R.id.requestPickupButton);
            requestPickupButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentBusId != null) {
                        // Check if the button text is "REQUEST PICKUP"
                        if (requestPickupButton.getText().equals("REQUEST PICKUP")) {
                            // Retrieve passenger username from the database
                            DatabaseReference passengersRef = FirebaseDatabase.getInstance().getReference("users/passengers");
                            passengersRef.child(passengerUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        String uname = dataSnapshot.child("uname").getValue(String.class);
                                        System.out.println("Passenger's uname: " + uname);
                                        fetchPassengerInformationAndCreateRequestData(uname);
                                        requestPickupButton.setText("CANCEL"); // Change the button text to "CANCEL"
                                    } else {
                                        System.out.println("Passenger not found");
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    System.out.println("Error retrieving passenger's uname: " + databaseError.getMessage());
                                }
                            });
                        } else {
                            // The button text is "CANCEL", so remove the requestPickup data from the database
                            DatabaseReference requestPickupRef = FirebaseDatabase.getInstance().getReference("requestPickups")
                                    .child(currentBusId).child("REQ-" + getCurrentUserId());
                            requestPickupRef.removeValue()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            // RequestPickup data removed successfully
                                            Toast.makeText(PassengerMapActivity.this, "Request canceled", Toast.LENGTH_SHORT).show();
                                            requestPickupButton.setText("REQUEST PICKUP"); // Change the button text back to "REQUEST PICKUP"
                                        } else {
                                            // Error occurred while removing RequestPickup data
                                            Toast.makeText(PassengerMapActivity.this, "Failed to cancel request", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(PassengerMapActivity.this, "Please select a bus first.", Toast.LENGTH_SHORT).show();
                    }
                }
            });


            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();
                    if (id == R.id.menu_select_bus) {
                        if (requestPickupButton.getText()=="CANCEL"){
                            Toast.makeText(PassengerMapActivity.this,"Please cancel the request first",Toast.LENGTH_LONG).show();
                        }else showBusSelectionDialog();
                    } else if (id == R.id.menu_profile) {
                        if (requestPickupButton.getText()=="CANCEL"){
                            Toast.makeText(PassengerMapActivity.this,"Please cancel the request first",Toast.LENGTH_LONG).show();
                        }else {

                            Intent intent = new Intent(PassengerMapActivity.this, BusStopFinderActivity.class);
                            intent.putExtra("mapTypeSelected", mapMenu);
                            startActivity(intent);
                        }
                    } else if (id == R.id.menu_logout) {
                        // Implement your logout logic here
                        logout();
                    }
                    drawerLayout.closeDrawers();
                    return true;
                }
            });

            selectMapTypeButton = findViewById(R.id.selectMapType);
            selectMapTypeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopupMenuMapType(v);
                }
            });

            //profile
        // Get current user from FirebaseAuth
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Initialize views
        genderTextView = findViewById(R.id.genderTextView);
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

    private void showPopupMenuMapType(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu_map_type, popupMenu.getMenu()); // Create a new menu resource file named popup_menu.xml

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.MAP_TYPE_NORMAL:
                        // Handle Putatan selection
                        mapMenu = "MAP_TYPE_NORMAL";
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        break;
                    case R.id.MAP_TYPE_TERRAIN:
                        // Handle Putatan selection
                        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        mapMenu = "MAP_TYPE_TERRAIN";
                        break;
                    case R.id.MAP_TYPE_SATELLITE:
                        // Handle Kota Kinabalu selection
                        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        mapMenu = "MAP_TYPE_SATELLITE";
                        break;
                    case R.id.MAP_TYPE_NONE:
                        // Handle Putatan selection
                        mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                        mapMenu = "MAP_TYPE_NONE";
                        break;
                    case R.id.MAP_TYPE_HYBRID:
                        // Handle Kota Kinabalu selection
                        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        mapMenu = "MAP_TYPE_HYBRID";
                        break;
                }
                return true;
            }
        });

        popupMenu.show();
    }
    private void displayNavHeader(){
        DatabaseReference databaseReferenceUsername = firebaseDatabase.getReference("users");
        databaseReferenceUsername.child("passengers").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String username = snapshot.child("uname").getValue(String.class);
                    getInfoPassenger1(username);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getInfoPassenger1(String uname){

        DatabaseReference databaseReference = firebaseDatabase.getReference("passengers");

        NavigationView navigationView = findViewById(R.id.navigationView);
        View navHeaderView = navigationView.getHeaderView(0);
        CircleImageView navHeaderImage = navHeaderView.findViewById(R.id.userImageView);
        TextView navHeaderName = navHeaderView.findViewById(R.id.userTextViewName);


        ImageView imageView = findViewById(R.id.userImageView);
        if (currentUser.getPhotoUrl() != null) {
            // Load user's profile image using FirebaseUser's photo URL
            // You can use your own method or library for loading the image
            // Here, we use Glide library as an example
            Glide.with(this)
                    .load(currentUser.getPhotoUrl())
                    .placeholder(R.drawable.default_profile_image) // Placeholder image if the user has not uploaded their image
                    .into(navHeaderImage);
        } else {
            // Set a placeholder image if the user has not uploaded their image
            navHeaderImage.setImageResource(R.drawable.default_profile_image);
        }

        // Retrieve the passenger's profile data
        databaseReference.child(uname).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    String name = dataSnapshot.child("name").getValue(String.class);

                    // Update the UI with the retrieved data

                    navHeaderName.setText(name);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error
                Log.e("Database Error", databaseError.getMessage());
            }
        });
    }

    private void fetchPassengerInformationAndCreateRequestData(String passengerUsername) {
        // Retrieve passenger information from the database
        mDatabase.child("passengers").child(passengerUsername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve passenger's information
                    String passengerName = dataSnapshot.child("name").getValue(String.class);
                    String phoneNumber = dataSnapshot.child("phone").getValue(String.class);
                    String gender = dataSnapshot.child("gender").getValue(String.class);
                    String imgUrl = dataSnapshot.child("imgUrl").getValue(String.class);

                    // Create request pickup data
                    RequestPickupData requestPickupData = new RequestPickupData(currentBusId, getCurrentUserId(), passengerName, phoneNumber, gender,imgUrl);

                    // Save request pickup data to the real-time database
                    mDatabase.child("requestPickups").child(currentBusId).child("REQ-"+getCurrentUserId()).setValue(requestPickupData)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // Notify the passenger that the request has been delivered to the selected bus ID
                                    Toast.makeText(PassengerMapActivity.this,"Request successfully",Toast.LENGTH_LONG).show();
                                } else {
                                    // Handle the error
                                    Log.e("Database Error", task.getException().getMessage());
                                }
                            });
                } else {
                    // Handle the case when the passenger does not exist
                    Toast.makeText(PassengerMapActivity.this,"Error!! passenger does not exist",Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error
                Log.e("Database Error", databaseError.getMessage());
            }
        });
    }

    private void showBusSelectionDialog() {
        mDatabase.child("drivers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PassengerMapActivity.this);
                builder.setTitle("Select Bus");

                // Retrieve bus driver IDs sorted by route
                // Modify the following code to match your Firebase database structure
                // and the data you want to display in the dialog
                List<String> busDriverIds = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String busDriverId = snapshot.getKey();

                    busDriverIds.add(busDriverId);
                }

                // Create an array adapter with the bus driver IDs
                ArrayAdapter<String> adapter = new ArrayAdapter<>(PassengerMapActivity.this, android.R.layout.simple_list_item_1, busDriverIds);
                builder.setAdapter(adapter, (dialog, which) -> {
                    String selectedBusId = busDriverIds.get(which);
                    currentBusId = busDriverIds.get(which);
                    x=true;
                    selectBusId(selectedBusId);
                });

                builder.show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    // Simulate bus ID selection (replace with your own logic for selecting bus ID)
    private void selectBusId(String busId) {

        // Create a database reference to the "drivers" node
        DatabaseReference driversRef = FirebaseDatabase.getInstance().getReference("drivers");

        // Query the "drivers" node to retrieve the specific child with busId "001"
        Query busQuery = driversRef.orderByChild("busId").equalTo(busId);

        // Add a ValueEventListener to retrieve the data
        busQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot driverSnapshot : dataSnapshot.getChildren()) {
                    String uid = driverSnapshot.child("uid").getValue(String.class);
                    currentBusUID = uid;
                    System.out.println("UID for busId 001: " + uid);

                    // Remove previous listener if exists
                    mDatabase.child("locations").child("drivers").removeEventListener(locationListener);
                    System.out.println("test 1");
                    // Add listener to the selected bus ID for real-time updates
                    mDatabase.child("locations").child("drivers").child(uid).addValueEventListener(locationListener);
                    System.out.println("test 2");

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error
                Log.e("Database Error", databaseError.getMessage());
            }
        });


    }


    private ValueEventListener locationListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                Double latitude = dataSnapshot.child("latitude").getValue(Double.class);
                Double longitude = dataSnapshot.child("longitude").getValue(Double.class);
                currentBusLat = latitude;
                currentBusLng = longitude;
                if (latitude != null && longitude != null) {
                    // Display the current location of the driver on the map
                    //mMap.clear(); // Clear previous markers
                    getDriverMarkers();
                    // Draw a polyline path from the driver's location to a fixed location

                   // retrieveDriverRoute(latitude,longitude);

                        getDriverDirection();
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            // Handle the cancellation
            Log.e("Database Error", databaseError.getMessage());
        }
    };
private void getDriverMarkers(){
    LatLng driverLatLng = new LatLng(currentBusLat, currentBusLng);

    // Load the bus icon as a Bitmap
    Bitmap busBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_bus);

    // Define the desired width and height of the icon (in pixels)
    int width = 100;
    int height = 100;

    // Resize the bitmap to the desired width and height
    Bitmap resizedBitmap = Bitmap.createScaledBitmap(busBitmap, width, height, false);

    // Create a custom marker icon from the resized bitmap
    BitmapDescriptor busIcon = BitmapDescriptorFactory.fromBitmap(resizedBitmap);

    // Add the marker with the custom icon to the map
    if (driverMarker != null) {
        driverMarker.setPosition(driverLatLng); // Update the marker's position
    } else {
        // Create a new marker and add it to the map
        busBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_bus);
        width = 100;
        height = 100;
        resizedBitmap = Bitmap.createScaledBitmap(busBitmap, width, height, false);
        busIcon = BitmapDescriptorFactory.fromBitmap(resizedBitmap);
        driverMarker = mMap.addMarker(new MarkerOptions()
                .position(driverLatLng)
                .title("Driver Location")
                .icon(busIcon));
    }

    // Set an OnMarkerClickListener for the driver marker
    driverMarker.setTag(currentBusId); // Set the driver ID as the marker's tag
    mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            String clickedDriverId = marker.getTag().toString();
            System.out.println("Im Here  " + currentBusId);
            // Call the method to show the driver information
            showDriverInformation();

            return true; // Consume the event
        }
    });

    mMap.moveCamera(CameraUpdateFactory.newLatLng(driverLatLng));
    mMap.animateCamera(CameraUpdateFactory.zoomTo(17));

}
    private void getDriverDirection() {
        //System.out.println("current driver uid : "+currentBusUID);
        DatabaseReference driverRouteRef = mDatabase.child("direction").child(currentBusUID);

            driverRouteRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        List<LatLng> routePoints = new ArrayList<>();
                        for (DataSnapshot pointSnapshot : dataSnapshot.getChildren()) {
                            double latitude = pointSnapshot.child("latitude").getValue(Double.class);
                            double longitude = pointSnapshot.child("longitude").getValue(Double.class);
                            LatLng point = new LatLng(latitude, longitude);
                            //System.out.println("im here"+latitude+longitude);
                            routePoints.add(point);
                        }
                        PolylineOptions polylineOptionsEqual = new PolylineOptions();
                        // Display the route points on the map
                        PolylineOptions polylineOptions = new PolylineOptions().addAll(routePoints);

                        //if(!Objects.equals(polylineOptions, polylineOptionsEqual)){

                            if (polyline != null) {
                                polyline.remove();
                            }
                            polyline = mMap.addPolyline(polylineOptions);

                        //}


                    }else{
                        System.out.println("error busid :"+currentBusId);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("Firebase", "Failed to retrieve driver route: " + databaseError.getMessage());
                }
            });


    }


    private void logout() {
        // Remove the requestPickup data from the database
        DatabaseReference requestPickupRef = FirebaseDatabase.getInstance().getReference("requestPickups")
                .child(currentBusId).child("REQ-" + getCurrentUserId());
        requestPickupRef.removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // RequestPickup data removed successfully
                        Toast.makeText(PassengerMapActivity.this, "RequestPickup data removed", Toast.LENGTH_SHORT).show();
                    } else {
                        // Error occurred while removing RequestPickup data
                        Toast.makeText(PassengerMapActivity.this, "Failed to remove RequestPickup data", Toast.LENGTH_SHORT).show();
                    }
                    // Perform logout operation
                    performLogout();
                });
    }

    private void performLogout() {
        // Add your logout logic here
        // For example, you can use FirebaseAuth to sign out the user
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(PassengerMapActivity.this, SignInActivity.class));
        finish();
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

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }


    }

    @Override
    public void onConnectionSuspended(int i) {
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

                //Update the current location of the passenger in Firebase Realtime Database
                mDatabase.child("locations").child("passengers").child(getCurrentUserId()).setValue(new LatLng(latitude, longitude));
                // Update the camera position to focus on the passenger's location
                //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(passengerLatLng, 15));
            }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    mMap = googleMap;

        if(Objects.equals(menuMap, "MAP_TYPE_NORMAL")){
            mMap.setMapType(googleMap.MAP_TYPE_NORMAL);
        }else if (Objects.equals(menuMap, "MAP_TYPE_TERRAIN")){
            mMap.setMapType(googleMap.MAP_TYPE_TERRAIN);
        }else if (Objects.equals(menuMap, "MAP_TYPE_SATELLITE")){
            mMap.setMapType(googleMap.MAP_TYPE_SATELLITE);
        }else if (Objects.equals(menuMap, "MAP_TYPE_NONE")){
            mMap.setMapType(googleMap.MAP_TYPE_NONE);
        }else if (Objects.equals(menuMap, "MAP_TYPE_HYBRID")){
            mMap.setMapType(googleMap.MAP_TYPE_HYBRID);
        }

    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        enableMyLocation();

    }else {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    }
    private void enableMyLocation() {
        // Check if location permission is granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    currentLocation = location;
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    if (requestPickupButton.getText()=="CANCEL"){
                        //do nothing
                    }else {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));
                        addMarkerForLocation(latLng);
                    }

                }
            });
        } else {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
        }
    }
    private void addMarkerForLocation(LatLng latLng) {
//        MarkerOptions markerOptions = new MarkerOptions()
//                .position(latLng)
//                .title("Your Location");
//
//        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
//
//        passengerMarker = mMap.addMarker(markerOptions);
    }

    // Handle location permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void showDriverInformation() {

        // Create a database reference to the "drivers" node
        DatabaseReference driversRef = FirebaseDatabase.getInstance().getReference("drivers");

        // Query the "drivers" node to retrieve the driver information based on the bus ID
        Query driverQuery = driversRef.child(currentBusId);

        // Add a ValueEventListener to retrieve the data
        driverQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve the driver's information
                    String driverName = dataSnapshot.child("name").getValue(String.class);
                    int totalPassenger = dataSnapshot.child("totalPassenger").getValue(Integer.class);
                    String oilStatus = dataSnapshot.child("busOilStatus").getValue(String.class);
                    // Display the driver's information in a dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(PassengerMapActivity.this);
                    builder.setTitle("Driver Information");
                    builder.setMessage("Driver Name: " + driverName + "\n" +
                            "Bus ID: " + currentBusId + "\n" +
                            "Total Passenger: " + totalPassenger+ "\n" +
                            "Oli Status: " + oilStatus);
                    builder.setPositiveButton("OK", null);
                    builder.show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the cancellation
                Log.e("Database Error", databaseError.getMessage());
            }
        });
    }

    private String getCurrentUserId() {
        // Implement the logic to get the current user's ID (e.g., from Firebase Authentication)
        // Get the current user from FirebaseAuth
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        } else {
            return null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //profile
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
                    usernamePassenger = dataSnapshot.child("uname").getValue(String.class);
                    String phone = dataSnapshot.child("phone").getValue(String.class);
                    String password = dataSnapshot.child("password").getValue(String.class);
                    String gender = dataSnapshot.child("gender").getValue(String.class);
                    // Update the UI with the retrieved data
                    genderTextView.setText(gender);
                    emailTextView.setText(email);
                    nameTextView.setText(name);
                    usernameTextView.setText(usernamePassenger);
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
                                            saveImageToFirebase(downloadUri);
                                            setProfileImage();
                                            Toast.makeText(PassengerMapActivity.this, "Profile image uploaded", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(PassengerMapActivity.this, "Failed to upload profile image", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(PassengerMapActivity.this, "Failed to upload profile image", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void saveImageToFirebase(Uri downloadUri) {
        DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference("passengers").child(usernamePassenger);
        adminRef.child("imgUrl").setValue(downloadUri.toString());
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
            Toast.makeText(PassengerMapActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
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

}
