package com.example.buslocatorsystem.admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.buslocatorsystem.adapter.DeletedDriverAdapter;
import com.example.buslocatorsystem.constructor.Driver;
import com.example.buslocatorsystem.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DeleteDriverActivity extends AppCompatActivity {

    private ListView listViewDrivers;
    private ArrayAdapter<Driver> driversAdapter;
    private List<Driver> driversList;
    private RecyclerView recyclerViewDrivers;
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
        recyclerViewDrivers = findViewById(R.id.recyclerViewDrivers);

        // Set up RecyclerView
        recyclerViewDrivers.setLayoutManager(new LinearLayoutManager(this));
        driversList = new ArrayList<>();
        DeletedDriverAdapter driverAdapter = new DeletedDriverAdapter(this, driversList);
        recyclerViewDrivers.setAdapter(driverAdapter);


        // Retrieve all drivers from Firebase database
        retrieveAllDrivers();


        // Retrieve all drivers from Firebase database
        retrieveAllDrivers();

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

                recyclerViewDrivers.getAdapter().notifyDataSetChanged();
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

                recyclerViewDrivers.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(DeleteDriverActivity.this, "Failed to search drivers", Toast.LENGTH_SHORT).show();
            }
        });
    }
}