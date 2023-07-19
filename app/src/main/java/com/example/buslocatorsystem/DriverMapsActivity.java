package com.example.buslocatorsystem;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;

public class DriverMapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private DatabaseReference mDatabase;
    private Marker driverMarker;

    private Map<String, Marker> passengerMarkers = new HashMap<>();
    private static final int MARKER_WIDTH = 100;  // in pixels
    private static final int MARKER_HEIGHT = 100; // in pixels

    private double fixedLat = 0, fixedLng =0;
    private ProgressBar progressBar;

    // Declare the Polyline variable
    private Polyline polyline;

    // Inside your DriverMapsActivity class
    private PlacesClient placesClient;

    private String driverCurrentUid ;
    private String driverCurrentId;
    private Button selectLocationButton; // Add a Button to your layout and assign it here
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_maps);

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

    }
    private void loadProfileInfo() {
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
                        Toast.makeText(DriverMapsActivity.this, "Driver data not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(DriverMapsActivity.this, "Failed to read driver data. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }
    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu()); // Create a new menu resource file named popup_menu.xml

        loadProfileInfo();

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
            Toast.makeText(DriverMapsActivity.this,"Having Error detecting the routes",Toast.LENGTH_LONG ).show();
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
            Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_bus);
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, MARKER_WIDTH, MARKER_HEIGHT, false);
            BitmapDescriptor markerBusIcon = BitmapDescriptorFactory.fromBitmap(resizedBitmap);

            // Display the passenger's marker on the map
             driverMarker = mMap.addMarker(new MarkerOptions()
                    .position(driverlocaiton)
                    .title("Your Location")
                    .icon(markerBusIcon));

            if(driverlocaiton != null && fixedLocation!=null){

                getPolylineRoute(uid, driverlocaiton, fixedLocation);
            }


            // Show the progress bar
            progressBar.setVisibility(View.VISIBLE);

            // Move and zoom the map camera
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(driverlocaiton, 17), new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    // Hide the progress bar when the camera animation finishes
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onCancel() {
                    // Handle cancellation if needed
                }
            });

        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

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
            mMap.setMyLocationEnabled(true);

            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    currentLocation = location;
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

                }
            });
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

            // Zoom and animate the camera to the driver's location
            mMap.animateCamera(CameraUpdateFactory.newLatLng(driverLocation));
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
                    Marker passengerMarker = mMap.addMarker(new MarkerOptions()
                            .position(passengerLocation)
                            .title("Passenger")
                            .icon(markerIcon));
                    passengerMarkers.put(modifiedPassengerId, passengerMarker);
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
}
