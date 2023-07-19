package com.example.buslocatorsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DeleteDriverActivity extends AppCompatActivity {

    private ListView listViewDrivers;
    private ArrayAdapter<Driver> driversAdapter;
    private List<Driver> driversList;
    private DatabaseReference driversRef;
    private DatabaseReference deletedDriversRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_driver);

        // Initialize Firebase database references
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        driversRef = firebaseDatabase.getReference("drivers");
        deletedDriversRef = firebaseDatabase.getReference("deleted_drivers");

        // Initialize views
        listViewDrivers = findViewById(R.id.listViewDrivers);

        // Initialize drivers list
        driversList = new ArrayList<>();

        // Initialize drivers adapter
        driversAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, driversList);

        // Set the adapter to the list view
        listViewDrivers.setAdapter(driversAdapter);

        // Retrieve all drivers from Firebase database
        retrieveAllDrivers();

        // Set item click listener for the list view
        listViewDrivers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected driver
                Driver selectedDriver = driversList.get(position);

                // Show confirmation dialog before deleting the driver
                showConfirmationDialog(selectedDriver);
            }
        });

        // Set up search functionality
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchDrivers(newText);
                return false;
            }
        });
    }

    private void retrieveAllDrivers() {
        driversRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                driversList.clear();

                for (DataSnapshot driverSnapshot : dataSnapshot.getChildren()) {
                    // Get the driver object
                    Driver driver = driverSnapshot.getValue(Driver.class);
                    if (driver != null) {
                        driversList.add(driver);
                    }
                }

                driversAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(DeleteDriverActivity.this, "Failed to retrieve drivers", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchDrivers(String searchText) {
        Query query = driversRef.orderByChild("busId").startAt(searchText).endAt(searchText + "\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                driversList.clear();

                for (DataSnapshot driverSnapshot : dataSnapshot.getChildren()) {
                    // Get the driver object
                    Driver driver = driverSnapshot.getValue(Driver.class);
                    if (driver != null) {
                        driversList.add(driver);
                    }
                }

                driversAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(DeleteDriverActivity.this, "Failed to search drivers", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showConfirmationDialog(final Driver driver) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Driver");
        builder.setMessage("Are you sure you want to delete this driver?");

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteDriver(driver);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteDriver(final Driver driver) {
        // Delete the driver's authentication
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        AuthCredential credential = EmailAuthProvider.getCredential(driver.getBusId() + "@buslocatorsystem.com", driver.getPassword());

        //sign out admin user
        firebaseAuth.signOut();

        //sign in selected driver user
        firebaseAuth.signInWithEmailAndPassword(driver.getBusId()+"@buslocatorsystem.com", driver.getPassword()) // Replace with your admin credentials
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser adminUser = FirebaseAuth.getInstance().getCurrentUser();

                            if (adminUser != null) {
                                adminUser.reauthenticate(credential)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    adminUser.delete()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()) {
                                                                        // Driver's authentication deleted successfully
                                                                        deleteDriverData(driver);

                                                                        //sign out selected driver user
                                                                        firebaseAuth.signOut();

                                                                        //sign in admin user
                                                                        firebaseAuth.signInWithEmailAndPassword("admin@gmail.com", "123123");

                                                                    } else {
                                                                        Toast.makeText(DeleteDriverActivity.this, "Failed to delete driver's authentication", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                }
                                                            });
                                                } else {
                                                    Toast.makeText(DeleteDriverActivity.this, "Failed to reauthenticate driver", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            } else {
                                Toast.makeText(DeleteDriverActivity.this, "Admin user not found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(DeleteDriverActivity.this, "Admin login failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }


    private void deleteDriverData(final Driver driver) {
        // Create a HashMap to hold the driver's data
        HashMap<String, Object> deletedDriverData = new HashMap<>();
        deletedDriverData.put("busId", driver.getBusId());
        deletedDriverData.put("name", driver.getName());
        deletedDriverData.put("password", driver.getPassword());
        deletedDriverData.put("route", driver.getRoute());

        // Save the deleted driver's data to the "deleted_drivers" node in the database
        deletedDriversRef.child(driver.getBusId()).setValue(deletedDriverData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // Delete the driver from the "drivers" node in the database
                            driversRef.child(driver.getBusId()).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(DeleteDriverActivity.this, "Driver deleted successfully", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(DeleteDriverActivity.this, "Failed to delete driver", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(DeleteDriverActivity.this, "Failed to delete driver", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}