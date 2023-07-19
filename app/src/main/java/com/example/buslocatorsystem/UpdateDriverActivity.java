package com.example.buslocatorsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateDriverActivity extends AppCompatActivity {
    private EditText editTextDriverId;
    private Button btnSearchDriver;

    private String[] routeArray;

    private DatabaseReference driversRef;
    private AlertDialog updateDialog;

    private ListView listViewFirstRoute;
    private ListView listViewSecondRoute;
    private ListView listViewThirdRoute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_driver);

        // Initialize Firebase database reference
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        driversRef = firebaseDatabase.getReference("drivers");

        Resources res = getResources();
        routeArray = res.getStringArray(R.array.route_array);

        // Initialize list views
        listViewFirstRoute = findViewById(R.id.listViewFirstRoute);
        listViewSecondRoute = findViewById(R.id.listViewSecondRoute);
        listViewThirdRoute = findViewById(R.id.listViewThirdRoute);

        // Retrieve and display drivers by route
        displayDriversByRoute();

        // Initialize views
        editTextDriverId = findViewById(R.id.editTextDriverId);
        btnSearchDriver = findViewById(R.id.btnSearchDriver);

        // Search driver button click listener
        btnSearchDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String driverId = editTextDriverId.getText().toString().trim();
                if (!driverId.isEmpty()) {
                    searchDriver(driverId);
                } else {
                    Toast.makeText(UpdateDriverActivity.this, "Please enter a driver ID", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void displayDriversByRoute() {
        driversRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Driver> firstRouteDrivers = new ArrayList<>();
                List<Driver> secondRouteDrivers = new ArrayList<>();
                List<Driver> thirdRouteDrivers = new ArrayList<>();

                for (DataSnapshot driverSnapshot : dataSnapshot.getChildren()) {
                    Driver driver = driverSnapshot.getValue(Driver.class);
                    if (driver != null) {
                        String route = driver.getRoute();

                        if (route.equals(routeArray[0])) {
                            firstRouteDrivers.add(driver);
                        } else if (route.equals(routeArray[1])) {
                            secondRouteDrivers.add(driver);
                        } else if (route.equals(routeArray[2])) {
                            thirdRouteDrivers.add(driver);
                        }
                    }
                }

                // Create adapters for each route using the custom DriverAdapter
                DriverAdapter firstRouteAdapter = new DriverAdapter(UpdateDriverActivity.this, firstRouteDrivers);
                DriverAdapter secondRouteAdapter = new DriverAdapter(UpdateDriverActivity.this, secondRouteDrivers);
                DriverAdapter thirdRouteAdapter = new DriverAdapter(UpdateDriverActivity.this, thirdRouteDrivers);

                listViewFirstRoute.setAdapter(firstRouteAdapter);
                listViewSecondRoute.setAdapter(secondRouteAdapter);
                listViewThirdRoute.setAdapter(thirdRouteAdapter);

                // Set item click listeners for each list view
                listViewFirstRoute.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Driver driver = (Driver) parent.getItemAtPosition(position);
                        if (driver != null) {
                            showUpdateDialog(driver);
                        }
                    }
                });

                listViewSecondRoute.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Driver driver = (Driver) parent.getItemAtPosition(position);
                        if (driver != null) {
                            showUpdateDialog(driver);
                        }
                    }
                });

                listViewThirdRoute.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Driver driver = (Driver) parent.getItemAtPosition(position);
                        if (driver != null) {
                            showUpdateDialog(driver);
                        }
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UpdateDriverActivity.this, "Failed to retrieve driver information", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void searchDriver(String driverId) {
        // Retrieve the driver information from the Firebase database
        driversRef.orderByChild("route").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    boolean isDriverFound = false;
                    for (DataSnapshot driverSnapshot : dataSnapshot.getChildren()) {
                        Driver driver = driverSnapshot.getValue(Driver.class);
                        if (driver != null && driver.getBusId().equals(driverId)) {
                            isDriverFound = true;
                            showUpdateDialog(driver);
                            break;
                        }
                    }
                    if (!isDriverFound) {
                        Toast.makeText(UpdateDriverActivity.this, "Driver not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(UpdateDriverActivity.this, "No drivers available", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UpdateDriverActivity.this, "Failed to retrieve driver information", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUpdateDialog(Driver driver) {
        // Create an AlertDialog for updating driver information
        AlertDialog.Builder builder = new AlertDialog.Builder(UpdateDriverActivity.this);

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_update_driver, null);
        builder.setView(dialogView);

        // Initialize views in the dialog
        EditText editTextDriverName = dialogView.findViewById(R.id.editTextDriverName);
        EditText editTextDriverBusId = dialogView.findViewById(R.id.editTextDriverBusId);
        Spinner spinnerDriverRoute = dialogView.findViewById(R.id.spinnerDriverRoute);
        Button btnUpdateDriver = dialogView.findViewById(R.id.btnUpdateDriver);

        // Set the existing driver information in the dialog
        editTextDriverName.setText(driver.getName());
        editTextDriverBusId.setText(driver.getBusId());
        editTextDriverBusId.setEnabled(false); // Disable editing the Bus ID field

        // Set the adapter for the spinner with routeArray
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(UpdateDriverActivity.this,
                android.R.layout.simple_spinner_item, routeArray);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDriverRoute.setAdapter(spinnerAdapter);

        // Get the position of the driver's route in the spinner adapter
        int routePosition = spinnerAdapter.getPosition(driver.getRoute());
        spinnerDriverRoute.setSelection(routePosition);

        // Create the AlertDialog
        updateDialog = builder.create();
        updateDialog.show();

        // Update driver button click listener
        btnUpdateDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the updated input values
                String newDriverName = editTextDriverName.getText().toString().trim();
                String newDriverRoute = spinnerDriverRoute.getSelectedItem().toString();

                // Validate the input values
                if (newDriverName.isEmpty()) {
                    Toast.makeText(UpdateDriverActivity.this, "Please enter the driver name", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create a map to hold the updated driver information
                Map<String, Object> updates = new HashMap<>();
                updates.put("name", newDriverName);
                updates.put("route", newDriverRoute);

                // Update the driver information in the Firebase database
                driversRef.child(driver.getBusId()).updateChildren(updates)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(UpdateDriverActivity.this, "Driver information updated successfully", Toast.LENGTH_SHORT).show();
                                    updateDialog.dismiss();
                                } else {
                                    Toast.makeText(UpdateDriverActivity.this, "Failed to update driver information", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    // Custom adapter for displaying Driver objects in the ListView
    private class DriverAdapter extends ArrayAdapter<Driver> {
        private LayoutInflater inflater;

        public DriverAdapter(Context context, List<Driver> drivers) {
            super(context, 0, drivers);
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);
            }

            Driver driver = getItem(position);
            TextView text1 = convertView.findViewById(android.R.id.text1);
            TextView text2 = convertView.findViewById(android.R.id.text2);

            if (driver != null) {
                text1.setText("Bus ID: " + driver.getBusId());
                text2.setText("Driver Name: " + driver.getName());
            }

            return convertView;
        }
    }
}
