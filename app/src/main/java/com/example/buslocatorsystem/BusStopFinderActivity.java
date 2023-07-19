package com.example.buslocatorsystem;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;
import com.google.maps.model.TravelMode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.Timeout;

public class BusStopFinderActivity extends AppCompatActivity implements SensorEventListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private ProgressBar progressBar,progressBar1;

    private static final int PROGRESS_DELAY = 1000;

    private ProgressDialog progressDialog;

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location currentLocation;
    private OkHttpClient client;
    private Gson gson;
    private ValueAnimator arrowAnimator = null;
    private Interpolator interpolator = new LinearInterpolator();

    private static final int PROGRESS_MAX = 100;
    private static final int PROGRESS_DURATION = 3000; // Duration in milliseconds
    private static final int PROGRESS_INCREMENT = 1; // Progress increment value
    private int currentProgress = 0; // Current progress value


    private Polyline currentPolyline;
    private LatLng selectedBusStopLatLng;
    private boolean isNavigationMode = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    private LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
    private List<Marker> busStopMarkers = new ArrayList<>();
    private Marker selectedMarker;
    private int delay = 0;


    // Declare the LocationCallback and LocationRequest variables
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;

    private double currentLocationLatitude, currentLocationLongitude;
    private String currentUid;
    private DatabaseReference mDatabase;
    private String origin,destination;

    private LocationManager locationManager;
    private static final long MIN_TIME_INTERVAL = 0; // Minimum time interval for location updates (in milliseconds)
    private static final float MIN_DISTANCE = 0; // Minimum distance interval for location updates (in meters)
    private boolean updateRoute = false;
    private Button startNavigationButton;

    // Declare variables for accelerometer and magnetometer data
    private float[] accelerometerData;
    private float[] magnetometerData;
    private boolean busStopDataFetched = false;
    private boolean isNotiAriveShowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_stop_finder);

        progressBar1 = findViewById(R.id.progressBar);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Create an instance of OkHttpClient and Gson
        client = new OkHttpClient();
        gson = new Gson();

        // Initialize the LocationCallback and LocationRequest
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    currentLocation = location;
                    currentLocationLatitude = currentLocation.getLatitude();
                    currentLocationLongitude = currentLocation.getLongitude();
                    System.out.println("debug 5"+currentLocation);
                    if (busStopMarkers.isEmpty()){
                        // Display all bus stop markers
                        displayAllBusStopMarkers();

                        // Focus the camera on the current location
                        focusCameraOnCurrentLocation();
                    }

                    if (!isNavigationMode) {
                        focusCameraOnCurrentLocation();
                    }

                }
                if (isNavigationMode){
                    for (Location location : locationResult.getLocations()) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        LatLng busStopLocation = new LatLng(selectedBusStopLatLng.latitude, selectedBusStopLatLng.longitude);

                        System.out.println("debug bustop latlng "+ busStopLocation);

                        if (hasArrived(currentLocation, busStopLocation)&&!isNotiAriveShowing) {
                            showArrivalDialog();
                            stopLocationUpdates();  // Stop further location updates
                        }
                    }
                }

            }
        };

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Start the periodic location updates
        startLocationUpdates();

        startNavigationButton = findViewById(R.id.start_navigation_button);
        startNavigationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startNavigationButton.getText().equals("Start Navigation")) {
                    if (selectedMarker != null) {
                        startNavigationButton.setText("Cancel Navigation");
                        // Start the polyline arrow animation


                        startNavigation();
                    } else {
                        Toast.makeText(BusStopFinderActivity.this, "Please select a bus stop first", Toast.LENGTH_SHORT).show();
                    }
                } else if (startNavigationButton.getText().equals("Cancel Navigation")) {
                    startNavigationButton.setText("Start Navigation");
                    cancelNavigation();
                }
            }
        });

    }


    private boolean hasArrived(LatLng currentLocation, LatLng busStopLocation) {
        Location currentLoc = new Location("Current Location");
        currentLoc.setLatitude(currentLocation.latitude);
        currentLoc.setLongitude(currentLocation.longitude);

        Location busStopLoc = new Location("Bus Stop Location");
        busStopLoc.setLatitude(busStopLocation.latitude);
        busStopLoc.setLongitude(busStopLocation.longitude);

        // Define the distance threshold for considering the user has arrived (10 meters)
        float distanceThreshold = 10f;

        float distance = currentLoc.distanceTo(busStopLoc);
        return distance <= distanceThreshold;
    }

    private void showArrivalDialog() {

        isNotiAriveShowing = true;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Arrival Notification")
                .setMessage("You have arrived at the bus stop!")
                .setInverseBackgroundForced(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle OK button click, redirect to other activity
                        isNotiAriveShowing = false;
                        redirectToOtherActivity();
                    }
                })
                .setCancelable(false)
                .show();

    }

    private void redirectToOtherActivity() {
        // Redirect to other activity
        // Replace "OtherActivity.class" with the desired activity class
        Intent intent = new Intent(this, PassengerMapActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
            startLocationUpdates();

                // Register the sensor listener
                SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
                Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                Sensor magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);


             } else {
            requestLocationPermission();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        stopLocationUpdates();
        // Unregister the sensor listener
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager.unregisterListener(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;


        // Enable the "My Location" layer on the map
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);
            //mMap.setTrafficEnabled(true);
            mMap.setIndoorEnabled(true);
            mMap.setBuildingsEnabled(true);
            mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    //updateProgressDialog(10);
                    startLocationUpdates();
                    return false;
                }
            });
        } else {
            // Request the permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

        // Set the map style
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));


        // Set up marker click listener
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker.equals(selectedMarker)) {
                    return true; // Prevent default marker click behavior
                }
                if (selectedMarker != null) {
                    deselectBusStopMarker();
                }
                selectBusStopMarker(marker);
                return true; // Prevent default marker click behavior
            }
        });


        showProgressDialog("Finding nearest Bus Stops");
        // Retrieve the user's last known location and update it on the map
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentLocation = location;
                LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            }
        });
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    currentLocation = location;
                    System.out.println("debug 1"+currentLocation);
                    focusCameraOnCurrentLocation();
                }
            });
        } else {
            requestLocationPermission();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Do nothing
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Do nothing
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        currentLocation = location;
        System.out.println("debug 2" + updateRoute);
        if (isNavigationMode&&updateRoute) {

            //clearRoute();
            drawRouteToBusStopforlocationchanged(selectedBusStopLatLng); // Add a new polyline without animation
            //updatePolylineWithDirection();

        }
    }

    private void updatePolylineWithDirection() {
        LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

        // Fetch the updated polyline points with direction
        getPolylineWithDirection(currentLatLng, selectedBusStopLatLng, new PolylineWithDirectionCallback() {
            @Override
            public void onPolylineWithDirection(PolylineOptions polylineOptions) {
                // Remove the previous polyline
                currentPolyline.remove();

                // Add the updated polyline to the map
                currentPolyline = mMap.addPolyline(polylineOptions);
            }
        });
    }


    private void getPolylineWithDirection(LatLng origin, LatLng destination, PolylineWithDirectionCallback callback) {
        // Create a GeoApiContext for the Google Directions API
        GeoApiContext geoApiContext = new GeoApiContext.Builder()
                .apiKey("AIzaSyCuBp-Fnefr1Xe5RxLgxMh3D2OzOQzxyaE")
                .build();

        // Create a DirectionsApiRequest to fetch the directions
        DirectionsApiRequest directionsRequest = DirectionsApi.newRequest(geoApiContext)
                .origin(new com.google.maps.model.LatLng(origin.latitude, origin.longitude))
                .destination(new com.google.maps.model.LatLng(destination.latitude, destination.longitude))
                .mode(TravelMode.WALKING); // Adjust the travel mode as needed

        // Execute the request asynchronously
        directionsRequest.setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                if (result != null && result.routes != null && result.routes.length > 0) {
                    DirectionsRoute route = result.routes[0];
                    EncodedPolyline encodedPolyline = route.overviewPolyline;
                    List<LatLng> points = PolyUtil.decode(encodedPolyline.getEncodedPath());

                    // Create a PolylineOptions object with the updated polyline points
                    PolylineOptions polylineOptions = new PolylineOptions()
                            .addAll(points)
                            .color(Color.CYAN)
                            .width(15)
                            .clickable(false)
                            ;

                    // Invoke the callback with the polyline options
                    callback.onPolylineWithDirection(polylineOptions);
                }
            }

            @Override
            public void onFailure(Throwable e) {
                // Handle the error
            }
        });
    }

    interface PolylineWithDirectionCallback {
        void onPolylineWithDirection(PolylineOptions polylineOptions);
    }
    private void removePolyline() {
        if (currentPolyline != null) {
            currentPolyline.remove();
            currentPolyline = null;
        }
    }
    private void addNewPolylineWithoutAnimation() {
        // Replace the following line with your own polyline creation logic
        PolylineOptions polylineOptions = new PolylineOptions()
                .add(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
                .add(selectedBusStopLatLng)
                .color(Color.RED);

        currentPolyline = mMap.addPolyline(polylineOptions);
    }
    private void stopLocationUpdates() {
        locationManager.removeUpdates(this);
    }

    // Add the missing method in your activity
    @Override
    public void onProviderDisabled(String provider) {
        // Handle the provider disabled event here if needed
    }

    // Add the missing methods of the LocationListener interface in your activity
    @Override
    public void onProviderEnabled(String provider) {
        // Handle the provider enabled event here if needed
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Handle the provider status changed event here if needed
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_INTERVAL, MIN_DISTANCE, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_INTERVAL, MIN_DISTANCE, this);

        } else {
            requestLocationPermission();
        }
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }

    // Method to show the progress dialog with loading message
    // Method to show the progress dialog with loading message
    private void showProgressDialog(String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(message);
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setInverseBackgroundForced(true);
            progressDialog.setProgress(0);
            progressDialog.setMax(100);
        }

        progressDialog.show();
        progressBar = progressDialog.findViewById(android.R.id.progress);
        int progress = 0;
        for (int i = 1; i <= 100; i++) {
            progress += 1;


                // Delay before updating progress dialog
                int finalProgress = progress;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.setProgress(finalProgress);

                        if (finalProgress >= 100){
                            dismissProgressDialog();
                        }
                    }
                }, 50 * i);

        }



    }

    // Method to dismiss the progress dialog
    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {

            try {
                Thread.sleep(3000);
                progressDialog.dismiss();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }



        }
    }

    // Method to update the progress value of the ProgressDialog
    private void updateProgressDialog(int progress) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.setProgress(progress);
        }
    }


    private void focusCameraOnCurrentLocation() {
        if (currentLocation != null) {
            System.out.println("debug 3"+currentLocation);
            currentLocationLatitude = currentLocation.getLatitude();
            currentLocationLongitude = currentLocation.getLongitude();
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            ///Toast.makeText(BusStopFinderActivity.this, "Finding Nearest bustop", Toast.LENGTH_SHORT).show();



            if(progressDialog.isShowing()){
                // Retrieve bus stop data from your data source
                List<BusStop> busStops = retrieveBusStops();
                if (!busStops.isEmpty()){
                    // Sort the bus stops by distance from the current location
                    busStops.sort(new Comparator<BusStop>() {
                        @Override
                        public int compare(BusStop busStop1, BusStop busStop2) {
                            LatLng currentLatLng = new LatLng(currentLocationLatitude, currentLocationLongitude);
                            LatLng latLng1 = new LatLng(busStop1.getLatitude(), busStop1.getLongitude());
                            LatLng latLng2 = new LatLng(busStop2.getLatitude(), busStop2.getLongitude());

                            double distance1 = SphericalUtil.computeDistanceBetween(currentLatLng, latLng1);
                            double distance2 = SphericalUtil.computeDistanceBetween(currentLatLng, latLng2);

                            return Double.compare(distance1, distance2);
                        }
                    });
                    // Get the first nearest bus stop and the current location
                    LatLng firstNearestLatLng = new LatLng(busStops.get(0).getLatitude(), busStops.get(0).getLongitude());
                    LatLng currentLatLng = new LatLng(currentLocationLatitude, currentLocationLongitude);

                    // Include the first nearest bus stop and the current location in the camera bounds
                    boundsBuilder.include(firstNearestLatLng);
                    boundsBuilder.include(currentLatLng);

                    // Move the camera to show the bus stops and the current location
                    LatLngBounds bounds = boundsBuilder.build();
                    int padding = 100; // Adjust padding as needed
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);


                    mMap.moveCamera(cameraUpdate);
                }
            }



        }
    }




    private void displayAllBusStopMarkers() {
        // Clear existing bus stop markers and the list
        for (Marker marker : busStopMarkers) {
            marker.remove();
        }
        busStopMarkers.clear();

        if(progressDialog.isShowing()){
            // Retrieve bus stop data from your data source
            List<BusStop> busStops = retrieveBusStops();

            if (!busStops.isEmpty()) {
                System.out.println("debug camera");



                for (BusStop busStop : busStops) {
                    LatLng latLng = new LatLng(busStop.getLatitude(), busStop.getLongitude());
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(latLng)
                            .title(busStop.getName())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_stop))
                            .anchor(0.5f, 0.5f)  // Center the icon
                            .infoWindowAnchor(0.5f, 0.5f)  // Center the info window
                            .draggable(false)  // Disable dragging
                            .rotation(0)  // Set rotation to 0 degrees
                            .flat(true);  // Make the icon flat

                    // Set the custom icon size
                    int iconSize = 100;  // Specify the desired size (in pixels)
                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(R.drawable.bus_stop, iconSize, iconSize)));

                    Marker marker = mMap.addMarker(markerOptions);
                    marker.setTag(busStop); // Set the bus stop object as the marker's tag
                    busStopMarkers.add(marker);
                    boundsBuilder.include(latLng);
                }


            }
        }



    }


    private Bitmap resizeMapIcons(int iconResourceId, int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), iconResourceId);
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
    }

    private void fetchBusStops(double clat, double clng) {
        OkHttpClient client = new OkHttpClient();
        System.out.println(clat + " " + clng);
        // Build the request URL using Google API
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                "?location=" + clat + "," + clng +
                "&rankby=distance" +
                "&name=bus_stop" +
                "&name=bus_station" +
                "&keyword=bus_stop" +
                "&key=AIzaSyCuBp-Fnefr1Xe5RxLgxMh3D2OzOQzxyaE";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();

                    // Parse the response JSON
                    try {
                        JSONObject jsonObject = new JSONObject(responseData);
                        JSONArray resultsArray = jsonObject.getJSONArray("results");
                        progressDialog.getProgress();

                        // Display bus stops on the map
                        runOnUiThread(() -> {

                            for (int i = 0; i < resultsArray.length(); i++) {
                                try {


                                    JSONObject placeObject = resultsArray.getJSONObject(i);
                                    JSONObject locationObject = placeObject.getJSONObject("geometry").getJSONObject("location");
                                    double latitude = locationObject.getDouble("lat");
                                    double longitude = locationObject.getDouble("lng");
                                    String placeName = placeObject.getString("name");
                                    System.out.println(placeName);
                                    LatLng busStopLatLng = new LatLng(latitude, longitude);

                                    // Load the bus stop icon and resize it to 100x100 pixels
                                    Bitmap originalIcon = BitmapFactory.decodeResource(getResources(), R.drawable.busstop);
                                    Bitmap resizedIcon = Bitmap.createScaledBitmap(originalIcon, 100, 100, false);

                                    Marker marker = mMap.addMarker(new MarkerOptions()
                                            .position(busStopLatLng)
                                            .title(placeName)
                                            .icon(BitmapDescriptorFactory.fromBitmap(resizedIcon))
                                            .anchor(0.5f, 0.5f) // Center the icon on the marker
                                            .infoWindowAnchor(0.5f, 0.5f)); // Center the info window on the marker

                                    assert marker != null;
                                    marker.setTag(busStopLatLng);
                                    busStopMarkers.add(marker);


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }

                            mMap.setOnMarkerClickListener(marker -> {
                                if (selectedMarker != null) {
                                    // Clear previous selection

                                    // Load the bus stop icon and resize it to 100x100 pixels
                                    Bitmap originalIcon = BitmapFactory.decodeResource(getResources(), R.drawable.busstopselect);
                                    Bitmap resizedIcon = Bitmap.createScaledBitmap(originalIcon, 100, 100, false);
                                    selectedMarker.setIcon(BitmapDescriptorFactory.fromBitmap(resizedIcon));

                                }

                                // Select the bus stop marker
                                selectedBusStopLatLng = (LatLng) marker.getTag();
                                selectedMarker = marker;
                                // Load the bus stop icon and resize it to 100x100 pixels
                                Bitmap originalIcon = BitmapFactory.decodeResource(getResources(), R.drawable.busstopselect);
                                Bitmap resizedIcon = Bitmap.createScaledBitmap(originalIcon, 100, 100, false);
                                selectedMarker.setIcon(BitmapDescriptorFactory.fromBitmap(resizedIcon));

                                return false;
                            });

                            busStopDataFetched = true; // Set the flag to indicate that the bus stop data has been fetched
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }



    @NonNull
    private List<BusStop> retrieveBusStops() {
        // Check if bus stop data has already been fetched
        if (busStopDataFetched) {
            List<BusStop> busStops = new ArrayList<>();
            for (Marker marker : busStopMarkers) {
                LatLng busStopLatLng = (LatLng) marker.getTag();
                String placeName = marker.getTitle();
                busStops.add(new BusStop(placeName, busStopLatLng.latitude, busStopLatLng.longitude));
            }

            return busStops;
        } else {
            // Fetch bus stop data using the fetchBusStops() method
            fetchBusStops(currentLocationLatitude, currentLocationLongitude);

            // Return an empty list as a placeholder until the data is fetched
            return new ArrayList<>();
        }
    }


    private void selectBusStopMarker(Marker marker) {
        selectedMarker = marker;
        // Load the bus stop icon and resize it to 100x100 pixels
        Bitmap originalIcon = BitmapFactory.decodeResource(getResources(), R.drawable.busstop);
        Bitmap resizedIcon = Bitmap.createScaledBitmap(originalIcon, 100, 100, false);
        selectedMarker.setIcon(BitmapDescriptorFactory.fromBitmap(resizedIcon));

        // Update the selected bus stop's latitude and longitude
        BusStop selectedBusStop = (BusStop) selectedMarker.getTag();
        if (selectedBusStop != null) {
            selectedBusStopLatLng = new LatLng(selectedBusStop.getLatitude(), selectedBusStop.getLongitude());
        }

        // Draw route to the selected bus stop
        drawRouteToBusStop(selectedBusStopLatLng);

        // Update the button text
        Button startNavigationButton = findViewById(R.id.start_navigation_button);
        startNavigationButton.setText("Start Navigation");
    }

    private void deselectBusStopMarker() {
        if (selectedMarker != null) {
            // Load the bus stop icon and resize it to 100x100 pixels
            Bitmap originalIcon = BitmapFactory.decodeResource(getResources(), R.drawable.busstopselect);
            Bitmap resizedIcon = Bitmap.createScaledBitmap(originalIcon, 100, 100, false);
            selectedMarker.setIcon(BitmapDescriptorFactory.fromBitmap(resizedIcon));
            selectedMarker = null;
        }
        clearRoute();
        clearBusStopPolyline();
        Button startNavigationButton = findViewById(R.id.start_navigation_button);
        startNavigationButton.setText("Start Navigation");
    }

    private void drawRouteToBusStop(LatLng destinationLatLng) {
        if (currentLocation == null || destinationLatLng == null) {
            return;
        }

        origin = currentLocation.getLatitude() + "," + currentLocation.getLongitude();
        LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        destination = destinationLatLng.latitude + "," + destinationLatLng.longitude;
        System.out.println("debug 4" + currentLocation);
        GeoApiContext geoApiContext = new GeoApiContext.Builder()
                .apiKey("AIzaSyCuBp-Fnefr1Xe5RxLgxMh3D2OzOQzxyaE")
                .build();

        DirectionsApiRequest directionsApiRequest = DirectionsApi.newRequest(geoApiContext)
                .mode(TravelMode.WALKING)
                .origin(origin)
                .destination(destination);

        directionsApiRequest.setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                handler.post(() -> {
                    if (result.routes != null && result.routes.length > 0) {
                        DirectionsRoute route = result.routes[0];
                        if (route.legs != null && route.legs.length > 0) {
                            List<com.google.maps.model.LatLng> decodedPath = route.overviewPolyline.decodePath();
                            com.google.maps.model.LatLng[] points = decodedPath.toArray(new com.google.maps.model.LatLng[decodedPath.size()]);

                            List<LatLng> decodedPoints = new ArrayList<>();
                            for (com.google.maps.model.LatLng latLng : points) {
                                decodedPoints.add(new LatLng(latLng.lat, latLng.lng));
                            }
                            drawPolyline(decodedPoints, currentLatLng, destinationLatLng);
                        }
                    }
                });
            }

            @Override
            public void onFailure(Throwable e) {
                handler.post(() -> {
                    Toast.makeText(BusStopFinderActivity.this, "Failed to retrieve directions", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void drawPolyline(List<LatLng> points, LatLng originLatLng, LatLng destinationLatLng) {
        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(points)
                .color(Color.CYAN)
                .width(15)
                .clickable(false)
                ;



        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(originLatLng,18));

        progressBar1.setVisibility(View.VISIBLE); // Show the progress bar

        // Delay the start of the animation by a short duration (adjust as needed)
        Handler handler = new Handler();
        int delay = 2000; // 1 second delay before starting the animation
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Calculate the total distance of the polyline
                double totalDistance = 0;
                for (int i = 0; i < points.size() - 1; i++) {
                    totalDistance += SphericalUtil.computeDistanceBetween(points.get(i), points.get(i + 1));
                }
                currentPolyline = mMap.addPolyline(polylineOptions);
                // Find the nearest point on the polyline to the current location
                LatLng nearestPoint = findNearestPointOnPolyline(points, originLatLng);
                int nearestIndex = points.indexOf(nearestPoint);
                // Create a value animator for the polyline animation
                arrowAnimator = ValueAnimator.ofFloat(0, (float) totalDistance);
                arrowAnimator.setDuration(10000); // Animation duration in milliseconds (adjust as needed)
                arrowAnimator.setInterpolator(new LinearInterpolator()); // Use LinearInterpolator for smooth animation

                final List<LatLng> newPoints = new ArrayList<>();
                arrowAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float distance = (float) animation.getAnimatedValue();

                        newPoints.clear();
                        newPoints.add(originLatLng); // Add the origin point

                        double accumulatedDistance = 0;
                        for (int i = nearestIndex; i < points.size() - 1; i++) {
                            LatLng startPoint = points.get(i);
                            LatLng endPoint = points.get(i + 1);
                            double segmentDistance = SphericalUtil.computeDistanceBetween(startPoint, endPoint);

                            if (accumulatedDistance + segmentDistance >= distance) {
                                // Add intermediate points within the current segment
                                double fraction = (distance - accumulatedDistance) / segmentDistance;
                                double lat = startPoint.latitude + (endPoint.latitude - startPoint.latitude) * fraction;
                                double lng = startPoint.longitude + (endPoint.longitude - startPoint.longitude) * fraction;
                                newPoints.add(new LatLng(lat, lng));
                                break;
                            } else {
                                // Add the entire segment
                                newPoints.add(endPoint);
                                accumulatedDistance += segmentDistance;
                            }
                        }

                        // Update the polyline
                        currentPolyline.setPoints(newPoints);

                        // Move the camera smoothly along with the animation
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for (LatLng point : newPoints) {
                            builder.include(point);
                        }
                        LatLngBounds bounds = builder.build();

                        // Calculate padding for the camera bounds (adjust as needed)
                        int padding = 100;
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                        mMap.moveCamera(cameraUpdate);
                    }
                });

                progressBar1.setVisibility(View.GONE); // Hide the progress bar



                arrowAnimator.addListener(new AnimatorListenerAdapter()
                {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        startLocationUpdates();
                        updateRoute = true; // Set updateRoute to true after the animation finishes
                        startNavigationButton.setClickable(true);
                    }


                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        //stopLocationUpdates();

                        updateRoute = false;
                        startNavigationButton.setClickable(false);
                    }
                });

                arrowAnimator.start();
            }
        },delay);





    }

    private void drawRouteToBusStopforlocationchanged(LatLng destinationLatLng) {
        if (currentLocation == null || destinationLatLng == null) {
            return;
        }

        // Replace 'R.id.estimated_time_textview' with the ID of your TextView in the layout XML file
        TextView estimatedTimeTextView = findViewById(R.id.estimated_time_textview);

        origin = currentLocation.getLatitude() + "," + currentLocation.getLongitude();
        LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        destination = destinationLatLng.latitude + "," + destinationLatLng.longitude;
        System.out.println("debug 4" + currentLocation);
        GeoApiContext geoApiContext = new GeoApiContext.Builder()
                .apiKey("AIzaSyCuBp-Fnefr1Xe5RxLgxMh3D2OzOQzxyaE")
                .build();

        DirectionsApiRequest directionsApiRequest = DirectionsApi.newRequest(geoApiContext)
                .mode(TravelMode.WALKING)
                .origin(origin)
                .destination(destination);

        directionsApiRequest.setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                handler.post(() -> {
                    if (result.routes != null && result.routes.length > 0) {
                        DirectionsRoute route = result.routes[0];
                        if (route.legs != null && route.legs.length > 0) {
                            List<com.google.maps.model.LatLng> decodedPath = route.overviewPolyline.decodePath();
                            com.google.maps.model.LatLng[] points = decodedPath.toArray(new com.google.maps.model.LatLng[decodedPath.size()]);

                            List<LatLng> decodedPoints = new ArrayList<>();
                            for (com.google.maps.model.LatLng latLng : points) {
                                decodedPoints.add(new LatLng(latLng.lat, latLng.lng));
                            }
                            drawPolylineforlocationchanged(decodedPoints, currentLatLng, destinationLatLng);
                        }
                    }
                });

                DirectionsRoute route = result.routes[0];
                if (route.legs != null && route.legs.length > 0) {
                    double totalDistance = 0;
                    for (DirectionsStep step : route.legs[0].steps) {
                        totalDistance += step.distance.inMeters;
                    }
                    // Assuming average walking speed of 1.4 meters per second
                    int estimatedTimeInSeconds = (int) (totalDistance / 1.4);

                    // Format the estimated time as hours, minutes, and seconds
                    int hours = estimatedTimeInSeconds / 3600;
                    int minutes = (estimatedTimeInSeconds % 3600) / 60;
                    int seconds = estimatedTimeInSeconds % 60;
                    String estimatedTime = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);

                    estimatedTimeTextView.setText(estimatedTime);
                }



            }


            @Override
            public void onFailure(Throwable e) {
                handler.post(() -> {
                    Toast.makeText(BusStopFinderActivity.this, "Failed to retrieve directions", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void drawPolylineforlocationchanged(List<LatLng> points, LatLng originLatLng, LatLng destinationLatLng) {
        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(points)
                .color(Color.CYAN)
                .width(15)
                .clickable(false);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(originLatLng,18));

        //progressBar.setVisibility(View.VISIBLE); // Show the progress bar

                // Calculate the total distance of the polyline
                double totalDistance = 0;
                for (int i = 0; i < points.size() - 1; i++) {
                    totalDistance += SphericalUtil.computeDistanceBetween(points.get(i), points.get(i + 1));
                }
                if(currentPolyline!=null){
                    clearRoute();
                }
                currentPolyline = mMap.addPolyline(polylineOptions);
                // Find the nearest point on the polyline to the current location
                LatLng nearestPoint = findNearestPointOnPolyline(points, originLatLng);
                int nearestIndex = points.indexOf(nearestPoint);
                // Create a value animator for the polyline animation
                arrowAnimator = ValueAnimator.ofFloat(0, (float) totalDistance);
                arrowAnimator.setDuration(10000); // Animation duration in milliseconds (adjust as needed)
                arrowAnimator.setInterpolator(new LinearInterpolator()); // Use LinearInterpolator for smooth animation

                final List<LatLng> newPoints = new ArrayList<>();
                arrowAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        float distance = (float) animation.getAnimatedValue();

                        newPoints.clear();
                        newPoints.add(originLatLng); // Add the origin point

                        double accumulatedDistance = 0;
                        for (int i = nearestIndex; i < points.size() - 1; i++) {
                            LatLng startPoint = points.get(i);
                            LatLng endPoint = points.get(i + 1);
                            double segmentDistance = SphericalUtil.computeDistanceBetween(startPoint, endPoint);

                            if (accumulatedDistance + segmentDistance >= distance) {
                                // Add intermediate points within the current segment
                                double fraction = (distance - accumulatedDistance) / segmentDistance;
                                double lat = startPoint.latitude + (endPoint.latitude - startPoint.latitude) * fraction;
                                double lng = startPoint.longitude + (endPoint.longitude - startPoint.longitude) * fraction;
                                newPoints.add(new LatLng(lat, lng));
                                break;
                            } else {
                                // Add the entire segment
                                newPoints.add(endPoint);
                                accumulatedDistance += segmentDistance;
                            }
                        }

                        // Update the polyline
                        currentPolyline.setPoints(newPoints);

                    }
                });

                //progressBar.setVisibility(View.GONE); // Hide the progress bar

                //arrowAnimator.start();
                arrowAnimator.cancel();

}


    private LatLng findNearestPointOnPolyline(List<LatLng> polylinePoints, LatLng targetLatLng) {
        LatLng nearestPoint = null;
        double shortestDistance = Double.MAX_VALUE;

        for (LatLng point : polylinePoints) {
            double distance = SphericalUtil.computeDistanceBetween(point, targetLatLng);
            if (distance < shortestDistance) {
                shortestDistance = distance;
                nearestPoint = point;
            }
        }

        return nearestPoint;
    }



    private void clearRoute() {
        if (currentPolyline != null) {
            currentPolyline.remove();
            currentPolyline = null;
        }
    }

    private void clearBusStopPolyline() {
        for (Marker marker : busStopMarkers) {
            // Load the bus stop icon and resize it to 100x100 pixels
            Bitmap originalIcon = BitmapFactory.decodeResource(getResources(), R.drawable.busstop);
            Bitmap resizedIcon = Bitmap.createScaledBitmap(originalIcon, 100, 100, false);
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(resizedIcon));
        }
        busStopMarkers.clear();
        boundsBuilder = new LatLngBounds.Builder();
    }

    private void startNavigation() {

        isNavigationMode = true;
        // Hide the bus stop markers
        for (Marker marker : busStopMarkers) {
            marker.setVisible(true);
            selectedMarker.setVisible(true);
        }


        // Draw route from current location to selected bus stop
        drawRouteToBusStop(selectedBusStopLatLng);

        // Start updating location for navigation
        startLocationUpdates();

        // Show navigation UI (e.g., compass, distance, time)
        View navigationUI = LayoutInflater.from(this).inflate(R.layout.navigation_ui, null);
        ImageView compassImageView = navigationUI.findViewById(R.id.compass_image_view);
        // ...

        // Add navigation UI to the map
        mMap.setPadding(0, 0, 0, navigationUI.getHeight());
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);


    }

    private void cancelNavigation() {
        isNavigationMode = false;
        updateRoute=false;

        selectedMarker.setRotation(0); // Reset the rotation of the marker
        // Show the bus stop markers
        for (Marker marker : busStopMarkers) {
            marker.setVisible(true);
            selectedMarker.setVisible(true);
        }



        // Clear the route and bus stop polyline
        clearRoute();
        clearBusStopPolyline();

        // Stop updating location for navigation
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);

        // Remove navigation UI from the map
        mMap.setPadding(0, 0, 0, 0);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelerometerData = event.values;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magnetometerData = event.values;
        }

        if (accelerometerData != null && magnetometerData != null) {
            // Compute the device's orientation
            float[] rotationMatrix = new float[9];
            boolean success = SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerData, magnetometerData);
            if (success) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(rotationMatrix, orientation);

                // Calculate the azimuth (yaw) value
                float azimuthInRadians = orientation[0];
                float azimuthInDegrees = (float) Math.toDegrees(azimuthInRadians);

                // Rotate the camera
                rotateCamera(azimuthInDegrees);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing
    }

    private void rotateCamera(float azimuth) {
        if (mMap != null&&isNavigationMode&&updateRoute) {
            // Adjust the azimuth value to range from 0 to 360 degrees
            //float calibratedAzimuth = (azimuth + 360) % 360;

            // Apply a sensitivity factor to slow down the rotation
            float sensitivityFactor = 0.5f; // Adjust the value as needed
            float adjustedAzimuth = azimuth * sensitivityFactor;

            CameraPosition currentCameraPosition = mMap.getCameraPosition();
            CameraPosition newCameraPosition = CameraPosition.builder(currentCameraPosition)
                    .bearing(adjustedAzimuth)
                    .build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition));
        }
    }
}
