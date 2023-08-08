package com.example.buslocatorsystem.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide; // Add Glide dependency if not already added
import com.example.buslocatorsystem.constructor.Driver;
import com.example.buslocatorsystem.R; // Replace with your app's R file
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UpdateDriverActivity extends AppCompatActivity {
    private EditText editTextDriverId;
    private Button btnSearchDriver;
    private Spinner spinnerSortingCriteria;
    private Button btnSortDrivers;

    private String[] routeArray;

    private DatabaseReference driversRef;
    private AlertDialog updateDialog;
private RadioGroup radioGroupSortingOrder;
    private RecyclerView recyclerViewDrivers;
    private DriverAdapter driverAdapter;

    private boolean isSortingDescending = false;
    private CheckBox checkBoxRouteFilter;
    private List<String> selectedRoutes; // To store the selected routes
    private RadioButton radioButtonDescending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_driver);

        radioButtonDescending = findViewById(R.id.radioButtonDescending);
        // Initialize Firebase database reference
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        driversRef = firebaseDatabase.getReference("drivers");

        Resources res = getResources();
        routeArray = res.getStringArray(R.array.route_array);

        radioGroupSortingOrder = findViewById(R.id.radioGroupSortingOrder);

        // Initialize the RecyclerView
        recyclerViewDrivers = findViewById(R.id.recyclerViewDrivers);
        recyclerViewDrivers.setLayoutManager(new LinearLayoutManager(this));

        // Retrieve and display drivers by route
        displayDriversByRoute();

        // Initialize views
        editTextDriverId = findViewById(R.id.editTextDriverId);
        btnSearchDriver = findViewById(R.id.btnSearchDriver);
        spinnerSortingCriteria = findViewById(R.id.spinnerSortingCriteria);
        btnSortDrivers = findViewById(R.id.btnSortDrivers);


        // Initialize the selectedRoutes list
        selectedRoutes = new ArrayList<>();
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

        spinnerSortingCriteria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (spinnerSortingCriteria.getSelectedItem().toString().equals("Route")) {
                        radioGroupSortingOrder.setVisibility(View.GONE);

                }else {
                    radioGroupSortingOrder.setVisibility(View.VISIBLE);
                    displayDriversByRoute();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        // Sort drivers button click listener
        btnSortDrivers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sortingCriteria = spinnerSortingCriteria.getSelectedItem().toString();
                // Toggle the sorting order flag
                isSortingDescending = radioButtonDescending.isChecked();
                sortDrivers(sortingCriteria);
            }
        });

    }
    // Method to show the route filter dialog
    private void showRouteFilterDialog() {
        // Create a custom dialog with the layout containing radio buttons

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Route");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_route_filter, null);
        builder.setView(dialogView);

        // Initialize the radio buttons
        RadioButton radioButtonRoute1 = dialogView.findViewById(R.id.radioButtonRoute1);
        RadioButton radioButtonRoute2 = dialogView.findViewById(R.id.radioButtonRoute2);
        RadioButton radioButtonRoute3 = dialogView.findViewById(R.id.radioButtonRoute3);

// Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
        // Set click listeners for the radio buttons to handle route selection
        radioButtonRoute1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioButtonRoute2.setChecked(false);
                radioButtonRoute3.setChecked(false);
                onRouteSelected("Route 1");
                dialog.dismiss();
            }
        });

        radioButtonRoute2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioButtonRoute1.setChecked(false);
                radioButtonRoute3.setChecked(false);
                onRouteSelected("Route 2");
                dialog.dismiss();
            }
        });


        radioButtonRoute3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                radioButtonRoute1.setChecked(false);
                radioButtonRoute2.setChecked(false);
                onRouteSelected("Route 3");
                dialog.dismiss();
            }
        });

    }
    // Method to handle route selection and display drivers based on the selected route
    private void onRouteSelected(String route) {
        // Update the selected routes list and apply the filter
        selectedRoutes.clear();
        selectedRoutes.add(route);
        applyRouteFilter();
    }
    // Method to apply the route filter and display drivers based on selected routes
    private void applyRouteFilter() {
        driversRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Driver> filteredDrivers = new ArrayList<>();

                for (DataSnapshot driverSnapshot : dataSnapshot.getChildren()) {
                    Driver driver = driverSnapshot.getValue(Driver.class);
                    if (driver != null && selectedRoutes.contains(driver.getRoute())) {
                        filteredDrivers.add(driver);
                    }
                }

                // Initialize the adapter and set it to the RecyclerView
                driverAdapter = new DriverAdapter(UpdateDriverActivity.this, filteredDrivers);
                recyclerViewDrivers.setAdapter(driverAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UpdateDriverActivity.this, "Failed to retrieve driver information", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sortDrivers(String sortingCriteria) {
        // Get the list of drivers from the adapter
        List<Driver> drivers = driverAdapter.getDrivers();

        // Sort the drivers based on the chosen sorting criteria
        switch (sortingCriteria) {
            case "Name":
                // Sort by driver name
                Collections.sort(drivers, new Comparator<Driver>() {
                    @Override
                    public int compare(Driver driver1, Driver driver2) {
                        return driver1.getName().compareToIgnoreCase(driver2.getName());
                    }
                });
                break;
            case "Bus ID":
                // Sort by bus ID
                Collections.sort(drivers, new Comparator<Driver>() {
                    @Override
                    public int compare(Driver driver1, Driver driver2) {
                        return driver1.getBusId().compareTo(driver2.getBusId());
                    }
                });
                break;
            case "Route":
                // Sort by route
                showRouteFilterDialog();
                break;
        }

        // Check the sorting order and reverse the list if it is descending
        if (isSortingDescending) {
            Collections.reverse(drivers);
        }

        // Update the RecyclerView with the sorted data
        driverAdapter.setDrivers(drivers);
        driverAdapter.notifyDataSetChanged();
    }


    private void displayDriversByRoute() {
        driversRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Driver> allDrivers = new ArrayList<>();

                for (DataSnapshot driverSnapshot : dataSnapshot.getChildren()) {
                    Driver driver = driverSnapshot.getValue(Driver.class);
                    if (driver != null) {
                        allDrivers.add(driver);
                    }
                }

                // Initialize the adapter and set it to the RecyclerView
                driverAdapter = new DriverAdapter(UpdateDriverActivity.this, allDrivers);
                recyclerViewDrivers.setAdapter(driverAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UpdateDriverActivity.this, "Failed to retrieve driver information", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchDriver(String driverId) {
        // Retrieve the driver information from the Firebase database
        driversRef.orderByChild("busId").equalTo(driverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot driverSnapshot : dataSnapshot.getChildren()) {
                        Driver driver = driverSnapshot.getValue(Driver.class);
                        if (driver != null) {
                            showUpdateDialog(driver);
                            return;
                        }
                    }
                }
                Toast.makeText(UpdateDriverActivity.this, "Driver not found", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UpdateDriverActivity.this, "Failed to retrieve driver information", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUpdateDialog(Driver driver) {
        // Create an AlertDialog for updating driver information
        AlertDialog.Builder builder = new AlertDialog.Builder(UpdateDriverActivity.this, R.style.TransparentDialog);

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


    // Custom adapter for displaying Driver objects in the RecyclerView
    private class DriverAdapter extends RecyclerView.Adapter<DriverAdapter.ViewHolder> {
        private Context context;
        private List<Driver> drivers;

        public DriverAdapter(Context context, List<Driver> drivers) {
            this.context = context;
            this.drivers = drivers;
        }

        public void updateData(List<Driver> drivers) {
            this.drivers = drivers;
            notifyDataSetChanged();
        }
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.driver_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Driver driver = drivers.get(position);

            holder.textViewBusId.setText(driver.getBusId());
            holder.textViewDriverName.setText(driver.getName());

            String currentRoute = driver.getRoute();
            if (Objects.equals(currentRoute, "Route 1")){
                currentRoute = "1";
            }else if(Objects.equals(currentRoute, "Route 2")){
                currentRoute = "2";
            }else if (Objects.equals(currentRoute, "Route 3")){
                currentRoute = "3";
            }

            holder.textViewRoute.setText(currentRoute);
            // Load the driver image using Glide (add the Glide dependency if not already added)
            Glide.with(context)
                    .load(driver.getImageUrl()) // Use driver.getImageUrl() for loading the image from URL or use driver.getImageResourceId() for loading from resource ID
                    .placeholder(R.drawable.default_profile_image) // Replace with a placeholder drawable while loading the image
                    .error(R.drawable.error_image) // Replace with an error drawable if image loading fails
                        .into(holder.imageViewDriver);

            // Set an OnClickListener for the item view
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Call the showUpdateDialog method when the item is clicked, passing the selected driver
                    showUpdateDialog(driver);
                }
            });
        }

        @Override
        public int getItemCount() {
            return drivers.size();
        }
        // New method to get the list of drivers
        public List<Driver> getDrivers() {
            return drivers;
        }

        public void setDrivers(List<Driver> drivers) {
            this.drivers = drivers;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageViewDriver;
            TextView textViewBusId;
            TextView textViewDriverName;
            TextView textViewRoute;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                imageViewDriver = itemView.findViewById(R.id.imageViewDriver);
                textViewBusId = itemView.findViewById(R.id.textViewBusId);
                textViewDriverName = itemView.findViewById(R.id.textViewDriverName);
                textViewRoute = itemView.findViewById(R.id.textViewRoute);
            }
        }
    }
}
