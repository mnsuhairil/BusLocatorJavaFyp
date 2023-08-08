package com.example.buslocatorsystem.passenger;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.example.buslocatorsystem.R;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
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
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BusStopFinderActivity extends AppCompatActivity implements SensorEventListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private LottieAnimationView progressBar1;
    private ProgressBar progressBar;

    private static final int PROGRESS_DELAY = 1000;

    private ProgressDialog progressDialog;

    private GoogleMap mMap1;
    private FusedLocationProviderClient fusedLocationProviderClient1;
    private Location currentLocation1;
    private LinearLayout ETA;
    private OkHttpClient client;
    private Gson gson;
    private ValueAnimator arrowAnimator = null;
    private Interpolator interpolator = new LinearInterpolator();

    private static final int PROGRESS_MAX = 100;
    private static final int PROGRESS_DURATION = 3000; // Duration in milliseconds
    private static final int PROGRESS_INCREMENT = 1; // Progress increment value
    private int currentProgress = 0; // Current progress value


    private Polyline currentPolyline1;
    private LatLng selectedBusStopLatLng1;
    private boolean isNavigationMode1 = false;
    private Handler handler1 = new Handler(Looper.getMainLooper());
    private LatLngBounds.Builder boundsBuilder1 = new LatLngBounds.Builder();
    private List<Marker> busStopMarkers1 = new ArrayList<>();
    private Marker selectedMarker1;
    private int delay = 0;


    // Declare the LocationCallback and LocationRequest variables
    private LocationCallback locationCallback1;
    private LocationRequest locationRequest1;

    private double currentLocationLatitude1, currentLocationLongitude1;
    private String currentUid;
    private DatabaseReference mDatabase;
    private String origin1, destination1;

    private LocationManager locationManager1;
    private static final long MIN_TIME_INTERVAL1 = 0; // Minimum time interval for location updates (in milliseconds)
    private static final float MIN_DISTANCE1 = 0; // Minimum distance interval for location updates (in meters)
    private boolean updateRoute1 = false;
    private Button startNavigationButton1;

    // Declare variables for accelerometer and magnetometer data
    private float[] accelerometerData1;
    private float[] magnetometerData1;
    private boolean busStopDataFetched1 = false;
    private boolean isNotiAriveShowing1 = false;
    private String menuMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_stop_finder);

        menuMap = getIntent().getStringExtra("mapTypeSelected");

        progressBar1 = findViewById(R.id.progressBar);

        ETA = findViewById(R.id.ETA);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        locationManager1 = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        fusedLocationProviderClient1 = LocationServices.getFusedLocationProviderClient(this);

        // Create an instance of OkHttpClient and Gson
        client = new OkHttpClient();
        gson = new Gson();

        // Initialize the LocationCallback and LocationRequest
        locationCallback1 = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    currentLocation1 = location;
                    currentLocationLatitude1 = currentLocation1.getLatitude();
                    currentLocationLongitude1 = currentLocation1.getLongitude();
                    System.out.println("debug 5"+ currentLocation1);
                    if (busStopMarkers1.isEmpty()){
                        // Display all bus stop markers
                        displayAllBusStopMarkers();

                        // Focus the camera on the current location
                        focusCameraOnCurrentLocation();
                    }

                    if (!isNavigationMode1) {
                        focusCameraOnCurrentLocation();
                    }

                }
                if (isNavigationMode1){
                    for (Location location : locationResult.getLocations()) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        LatLng busStopLocation = new LatLng(selectedBusStopLatLng1.latitude, selectedBusStopLatLng1.longitude);

                        System.out.println("debug bustop latlng "+ busStopLocation);

                        if (hasArrived(currentLocation, busStopLocation)&&!isNotiAriveShowing1) {
                            showArrivalDialog();
                            stopLocationUpdates();  // Stop further location updates
                        }
                    }
                }

            }
        };

        locationRequest1 = LocationRequest.create();
        locationRequest1.setInterval(5000);
        locationRequest1.setFastestInterval(2000);
        locationRequest1.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Start the periodic location updates
        startLocationUpdates();

        startNavigationButton1 = findViewById(R.id.start_navigation_button);
        startNavigationButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startNavigationButton1.getText().equals("Start Navigation")) {
                    if (selectedMarker1 != null) {
                        startNavigationButton1.setText("Cancel Navigation");
                        // Start the polyline arrow animation
                        startNavigation();
                    } else {
                        Toast.makeText(BusStopFinderActivity.this, "Please select a bus stop first", Toast.LENGTH_SHORT).show();
                    }
                } else if (startNavigationButton1.getText().equals("Cancel Navigation")) {
                    ETA.setVisibility(View.GONE);
                    startNavigationButton1.setText("Start Navigation");
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

        isNotiAriveShowing1 = true;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Arrival Notification")
                .setMessage("You have arrived at the bus stop!")
                .setInverseBackgroundForced(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle OK button click, redirect to other activity
                        isNotiAriveShowing1 = false;
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
        intent.putExtra("mapFinder",menuMap);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient1.requestLocationUpdates(locationRequest1, locationCallback1, null);
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
        fusedLocationProviderClient1.removeLocationUpdates(locationCallback1);
        stopLocationUpdates();
        // Unregister the sensor listener
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager.unregisterListener(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap1 = googleMap;

        if(Objects.equals(menuMap, "MAP_TYPE_NORMAL")){
            mMap1.setMapType(googleMap.MAP_TYPE_NORMAL);
        }else if (Objects.equals(menuMap, "MAP_TYPE_TERRAIN")){
            mMap1.setMapType(googleMap.MAP_TYPE_TERRAIN);
        }else if (Objects.equals(menuMap, "MAP_TYPE_SATELLITE")){
            mMap1.setMapType(googleMap.MAP_TYPE_SATELLITE);
        }else if (Objects.equals(menuMap, "MAP_TYPE_NONE")){
            mMap1.setMapType(googleMap.MAP_TYPE_NONE);
        }else if (Objects.equals(menuMap, "MAP_TYPE_HYBRID")){
            mMap1.setMapType(googleMap.MAP_TYPE_HYBRID);
        }

        // Enable the "My Location" layer on the map
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            mMap1.setMyLocationEnabled(true);
            //mMap.setTrafficEnabled(true);
            mMap1.setIndoorEnabled(true);
            mMap1.setBuildingsEnabled(true);
            mMap1.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
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
        /*mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));*/


        // Set up marker click listener
        mMap1.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker.equals(selectedMarker1)) {
                    return true; // Prevent default marker click behavior
                }
                if (selectedMarker1 != null) {
                    deselectBusStopMarker();
                }
                selectBusStopMarker(marker);
                return true; // Prevent default marker click behavior
            }
        });


        showProgressDialog("Finding nearest Bus Stops");
        // Retrieve the user's last known location and update it on the map
        fusedLocationProviderClient1.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentLocation1 = location;
                LatLng latLng = new LatLng(currentLocation1.getLatitude(), currentLocation1.getLongitude());
                mMap1.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            }
        });
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient1.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    currentLocation1 = location;
                    System.out.println("debug 1"+ currentLocation1);
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
        currentLocation1 = location;
        System.out.println("debug 2" + updateRoute1);
        if (isNavigationMode1 && updateRoute1) {

            //clearRoute();
            drawRouteToBusStopforlocationchanged(selectedBusStopLatLng1); // Add a new polyline without animation
            //updatePolylineWithDirection();

        }
    }

    private void updatePolylineWithDirection() {
        LatLng currentLatLng = new LatLng(currentLocation1.getLatitude(), currentLocation1.getLongitude());

        // Fetch the updated polyline points with direction
        getPolylineWithDirection(currentLatLng, selectedBusStopLatLng1, new PolylineWithDirectionCallback() {
            @Override
            public void onPolylineWithDirection(PolylineOptions polylineOptions) {
                // Remove the previous polyline
                currentPolyline1.remove();

                // Add the updated polyline to the map
                currentPolyline1 = mMap1.addPolyline(polylineOptions);
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
        if (currentPolyline1 != null) {
            currentPolyline1.remove();
            currentPolyline1 = null;
        }
    }
    private void addNewPolylineWithoutAnimation() {
        // Replace the following line with your own polyline creation logic
        PolylineOptions polylineOptions = new PolylineOptions()
                .add(new LatLng(currentLocation1.getLatitude(), currentLocation1.getLongitude()))
                .add(selectedBusStopLatLng1)
                .color(Color.RED);

        currentPolyline1 = mMap1.addPolyline(polylineOptions);
    }
    private void stopLocationUpdates() {
        locationManager1.removeUpdates(this);
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
            fusedLocationProviderClient1.requestLocationUpdates(locationRequest1, locationCallback1, null);
            locationManager1.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_INTERVAL1, MIN_DISTANCE1, this);
            locationManager1.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_INTERVAL1, MIN_DISTANCE1, this);

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
        if (currentLocation1 != null) {
            System.out.println("debug 3"+ currentLocation1);
            currentLocationLatitude1 = currentLocation1.getLatitude();
            currentLocationLongitude1 = currentLocation1.getLongitude();
            LatLng latLng = new LatLng(currentLocation1.getLatitude(), currentLocation1.getLongitude());
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
                            LatLng currentLatLng = new LatLng(currentLocationLatitude1, currentLocationLongitude1);
                            LatLng latLng1 = new LatLng(busStop1.getLatitude(), busStop1.getLongitude());
                            LatLng latLng2 = new LatLng(busStop2.getLatitude(), busStop2.getLongitude());

                            double distance1 = SphericalUtil.computeDistanceBetween(currentLatLng, latLng1);
                            double distance2 = SphericalUtil.computeDistanceBetween(currentLatLng, latLng2);

                            return Double.compare(distance1, distance2);
                        }
                    });
                    // Get the first nearest bus stop and the current location
                    LatLng firstNearestLatLng = new LatLng(busStops.get(0).getLatitude(), busStops.get(0).getLongitude());
                    LatLng currentLatLng = new LatLng(currentLocationLatitude1, currentLocationLongitude1);

                    // Include the first nearest bus stop and the current location in the camera bounds
                    boundsBuilder1.include(firstNearestLatLng);
                    boundsBuilder1.include(currentLatLng);

                    // Move the camera to show the bus stops and the current location
                    LatLngBounds bounds = boundsBuilder1.build();
                    int padding = 100; // Adjust padding as needed
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);


                    mMap1.moveCamera(cameraUpdate);
                }
            }



        }
    }




    private void displayAllBusStopMarkers() {
        // Clear existing bus stop markers and the list
        for (Marker marker : busStopMarkers1) {
            marker.remove();
        }
        busStopMarkers1.clear();

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

                    Marker marker = mMap1.addMarker(markerOptions);
                    marker.setTag(busStop); // Set the bus stop object as the marker's tag
                    busStopMarkers1.add(marker);
                    boundsBuilder1.include(latLng);
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

                                    Marker marker = mMap1.addMarker(new MarkerOptions()
                                            .position(busStopLatLng)
                                            .title(placeName)
                                            .icon(BitmapDescriptorFactory.fromBitmap(resizedIcon))
                                            .anchor(0.5f, 0.5f) // Center the icon on the marker
                                            .infoWindowAnchor(0.5f, 0.5f)); // Center the info window on the marker

                                    assert marker != null;
                                    marker.setTag(busStopLatLng);
                                    busStopMarkers1.add(marker);


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }

                            mMap1.setOnMarkerClickListener(marker -> {
                                if (selectedMarker1 != null) {
                                    // Clear previous selection

                                    // Load the bus stop icon and resize it to 100x100 pixels
                                    Bitmap originalIcon = BitmapFactory.decodeResource(getResources(), R.drawable.busstopselect);
                                    Bitmap resizedIcon = Bitmap.createScaledBitmap(originalIcon, 100, 100, false);
                                    selectedMarker1.setIcon(BitmapDescriptorFactory.fromBitmap(resizedIcon));

                                }

                                // Select the bus stop marker
                                selectedBusStopLatLng1 = (LatLng) marker.getTag();
                                selectedMarker1 = marker;
                                // Load the bus stop icon and resize it to 100x100 pixels
                                Bitmap originalIcon = BitmapFactory.decodeResource(getResources(), R.drawable.busstopselect);
                                Bitmap resizedIcon = Bitmap.createScaledBitmap(originalIcon, 100, 100, false);
                                selectedMarker1.setIcon(BitmapDescriptorFactory.fromBitmap(resizedIcon));

                                return false;
                            });

                            busStopDataFetched1 = true; // Set the flag to indicate that the bus stop data has been fetched
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
        if (busStopDataFetched1) {
            List<BusStop> busStops = new ArrayList<>();
            for (Marker marker : busStopMarkers1) {
                LatLng busStopLatLng = (LatLng) marker.getTag();
                String placeName = marker.getTitle();
                busStops.add(new BusStop(placeName, busStopLatLng.latitude, busStopLatLng.longitude));
            }

            return busStops;
        } else {
            // Fetch bus stop data using the fetchBusStops() method
            fetchBusStops(currentLocationLatitude1, currentLocationLongitude1);

            // Return an empty list as a placeholder until the data is fetched
            return new ArrayList<>();
        }
    }


    private void selectBusStopMarker(Marker marker) {
        selectedMarker1 = marker;
        // Load the bus stop icon and resize it to 100x100 pixels
        Bitmap originalIcon = BitmapFactory.decodeResource(getResources(), R.drawable.busstop);
        Bitmap resizedIcon = Bitmap.createScaledBitmap(originalIcon, 100, 100, false);
        selectedMarker1.setIcon(BitmapDescriptorFactory.fromBitmap(resizedIcon));

        // Update the selected bus stop's latitude and longitude
        BusStop selectedBusStop = (BusStop) selectedMarker1.getTag();
        if (selectedBusStop != null) {
            selectedBusStopLatLng1 = new LatLng(selectedBusStop.getLatitude(), selectedBusStop.getLongitude());
        }

        // Draw route to the selected bus stop
        drawRouteToBusStop(selectedBusStopLatLng1);

        // Update the button text
        Button startNavigationButton = findViewById(R.id.start_navigation_button);
        startNavigationButton.setText("Start Navigation");
    }

    private void deselectBusStopMarker() {
        if (selectedMarker1 != null) {
            // Load the bus stop icon and resize it to 100x100 pixels
            Bitmap originalIcon = BitmapFactory.decodeResource(getResources(), R.drawable.busstopselect);
            Bitmap resizedIcon = Bitmap.createScaledBitmap(originalIcon, 100, 100, false);
            selectedMarker1.setIcon(BitmapDescriptorFactory.fromBitmap(resizedIcon));
            selectedMarker1 = null;
        }
        clearRoute();
        clearBusStopPolyline();
        Button startNavigationButton = findViewById(R.id.start_navigation_button);
        startNavigationButton.setText("Start Navigation");
    }

    private void drawRouteToBusStop(LatLng destinationLatLng) {
        if (currentLocation1 == null || destinationLatLng == null) {
            return;
        }

        origin1 = currentLocation1.getLatitude() + "," + currentLocation1.getLongitude();
        LatLng currentLatLng = new LatLng(currentLocation1.getLatitude(), currentLocation1.getLongitude());
        destination1 = destinationLatLng.latitude + "," + destinationLatLng.longitude;
        System.out.println("debug 4" + currentLocation1);
        GeoApiContext geoApiContext = new GeoApiContext.Builder()
                .apiKey("AIzaSyCuBp-Fnefr1Xe5RxLgxMh3D2OzOQzxyaE")
                .build();

        DirectionsApiRequest directionsApiRequest = DirectionsApi.newRequest(geoApiContext)
                .mode(TravelMode.WALKING)
                .origin(origin1)
                .destination(destination1);

        directionsApiRequest.setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                handler1.post(() -> {
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
                handler1.post(() -> {
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



        mMap1.moveCamera(CameraUpdateFactory.newLatLngZoom(originLatLng,18));

        progressBar1.setVisibility(View.VISIBLE); // Show the progress bar
        progressBar1.playAnimation();
        // Delay the start of the animation by a short duration (adjust as needed)
        Handler handler = new Handler();
        int delay = 4000; // 1 second delay before starting the animation
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Calculate the total distance of the polyline
                double totalDistance = 0;
                for (int i = 0; i < points.size() - 1; i++) {
                    totalDistance += SphericalUtil.computeDistanceBetween(points.get(i), points.get(i + 1));
                }
                currentPolyline1 = mMap1.addPolyline(polylineOptions);
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
                        currentPolyline1.setPoints(newPoints);

                        // Move the camera smoothly along with the animation
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for (LatLng point : newPoints) {
                            builder.include(point);
                        }
                        LatLngBounds bounds = builder.build();

                        // Calculate padding for the camera bounds (adjust as needed)
                        int padding = 100;
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                        mMap1.moveCamera(cameraUpdate);
                    }
                });
                progressBar1.cancelAnimation();
                progressBar1.setVisibility(View.GONE); // Hide the progress bar



                arrowAnimator.addListener(new AnimatorListenerAdapter()
                {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        startLocationUpdates();
                        updateRoute1 = true; // Set updateRoute to true after the animation finishes
                        startNavigationButton1.setClickable(true);
                        ETA.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationStart(Animator animation) {
                        super.onAnimationStart(animation);
                        //stopLocationUpdates();

                        updateRoute1 = false;
                        startNavigationButton1.setClickable(false);
                    }
                });

                arrowAnimator.start();
            }
        },delay);





    }

    private void drawRouteToBusStopforlocationchanged(LatLng destinationLatLng) {
        if (currentLocation1 == null || destinationLatLng == null) {
            return;
        }

        // Replace 'R.id.estimated_time_textview' with the ID of your TextView in the layout XML file
        TextView estimatedTimeTextView = findViewById(R.id.estimated_time_textview);

        origin1 = currentLocation1.getLatitude() + "," + currentLocation1.getLongitude();
        LatLng currentLatLng = new LatLng(currentLocation1.getLatitude(), currentLocation1.getLongitude());
        destination1 = destinationLatLng.latitude + "," + destinationLatLng.longitude;
        System.out.println("debug 4" + currentLocation1);
        GeoApiContext geoApiContext = new GeoApiContext.Builder()
                .apiKey("AIzaSyCuBp-Fnefr1Xe5RxLgxMh3D2OzOQzxyaE")
                .build();

        DirectionsApiRequest directionsApiRequest = DirectionsApi.newRequest(geoApiContext)
                .mode(TravelMode.WALKING)
                .origin(origin1)
                .destination(destination1);

        directionsApiRequest.setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
                handler1.post(() -> {
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
                handler1.post(() -> {
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

        mMap1.moveCamera(CameraUpdateFactory.newLatLngZoom(originLatLng,18));

        //progressBar.setVisibility(View.VISIBLE); // Show the progress bar

                // Calculate the total distance of the polyline
                double totalDistance = 0;
                for (int i = 0; i < points.size() - 1; i++) {
                    totalDistance += SphericalUtil.computeDistanceBetween(points.get(i), points.get(i + 1));
                }
                if(currentPolyline1 !=null){
                    clearRoute();
                }
                currentPolyline1 = mMap1.addPolyline(polylineOptions);
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
                        currentPolyline1.setPoints(newPoints);

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
        if (currentPolyline1 != null) {
            currentPolyline1.remove();
            currentPolyline1 = null;
        }
    }

    private void clearBusStopPolyline() {
        for (Marker marker : busStopMarkers1) {
            // Load the bus stop icon and resize it to 100x100 pixels
            Bitmap originalIcon = BitmapFactory.decodeResource(getResources(), R.drawable.busstop);
            Bitmap resizedIcon = Bitmap.createScaledBitmap(originalIcon, 100, 100, false);
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(resizedIcon));
        }
        busStopMarkers1.clear();
        boundsBuilder1 = new LatLngBounds.Builder();
    }

    private void startNavigation() {

        isNavigationMode1 = true;
        // Hide the bus stop markers
        for (Marker marker : busStopMarkers1) {
            marker.setVisible(true);
            selectedMarker1.setVisible(true);
        }


        // Draw route from current location to selected bus stop
        drawRouteToBusStop(selectedBusStopLatLng1);

        // Start updating location for navigation
        startLocationUpdates();

        // Show navigation UI (e.g., compass, distance, time)
        View navigationUI = LayoutInflater.from(this).inflate(R.layout.navigation_ui, null);
        ImageView compassImageView = navigationUI.findViewById(R.id.compass_image_view);
        // ...

        // Add navigation UI to the map
        mMap1.setPadding(0, 0, 0, navigationUI.getHeight());
        mMap1.getUiSettings().setCompassEnabled(true);
        mMap1.getUiSettings().setMapToolbarEnabled(true);


    }

    private void cancelNavigation() {
        isNavigationMode1 = false;
        updateRoute1 =false;

        selectedMarker1.setRotation(0); // Reset the rotation of the marker
        // Show the bus stop markers
        for (Marker marker : busStopMarkers1) {
            marker.setVisible(true);
            selectedMarker1.setVisible(true);
        }



        // Clear the route and bus stop polyline
        clearRoute();
        clearBusStopPolyline();

        // Stop updating location for navigation
        fusedLocationProviderClient1.removeLocationUpdates(locationCallback1);

        // Remove navigation UI from the map
        mMap1.setPadding(0, 0, 0, 0);
        mMap1.getUiSettings().setCompassEnabled(true);
        mMap1.getUiSettings().setMapToolbarEnabled(false);
        mMap1.getUiSettings().setMyLocationButtonEnabled(true);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelerometerData1 = event.values;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magnetometerData1 = event.values;
        }

        if (accelerometerData1 != null && magnetometerData1 != null) {
            // Compute the device's orientation
            float[] rotationMatrix = new float[9];
            boolean success = SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerData1, magnetometerData1);
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
        if (mMap1 != null&& isNavigationMode1 && updateRoute1) {
            // Adjust the azimuth value to range from 0 to 360 degrees
            //float calibratedAzimuth = (azimuth + 360) % 360;

            // Apply a sensitivity factor to slow down the rotation
            float sensitivityFactor = 0.5f; // Adjust the value as needed
            float adjustedAzimuth = azimuth * sensitivityFactor;

            CameraPosition currentCameraPosition = mMap1.getCameraPosition();
            CameraPosition newCameraPosition = CameraPosition.builder(currentCameraPosition)
                    .bearing(adjustedAzimuth)
                    .build();
            mMap1.moveCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition));
        }
    }
}
