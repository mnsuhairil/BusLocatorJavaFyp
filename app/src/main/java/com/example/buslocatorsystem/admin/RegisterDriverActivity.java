package com.example.buslocatorsystem.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.buslocatorsystem.constructor.Driver;
import com.example.buslocatorsystem.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterDriverActivity extends AppCompatActivity {
    private EditText editTextDriverName;
    private EditText editTextDriverBusId;
    private EditText editTextDriverPassword;
    private AutoCompleteTextView spinnerDriverRoute;
    private Button btnRegisterDriver;

    private DatabaseReference driversRef;
    private DatabaseReference usersRef;
    private FirebaseAuth mAuth;
    AutoCompleteTextView autoCompleteTextView2;
    ArrayAdapter<String> adapterItems2;
    String[] item3 = {"Available" , "Not available"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_driver);

        // Initialize Firebase database reference and authentication
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        driversRef = firebaseDatabase.getReference("drivers");
        usersRef = firebaseDatabase.getReference("users");
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        editTextDriverName = findViewById(R.id.editTextDriverName);
        editTextDriverBusId = findViewById(R.id.editTextDriverBusId);
        editTextDriverPassword = findViewById(R.id.editTextDriverPassword);
        btnRegisterDriver = findViewById(R.id.btnRegisterDriver);

        TextInputLayout materialRoutes = findViewById(R.id.materialRoutes);

        // Set the outline color to colorPrimary
        materialRoutes.setBoxStrokeColor(getResources().getColor(R.color.colorPrimary));

        // Assuming you have an array of routes as your dropdown items
        String[] routes = {"Route 1", "Route 2", "Route 3"};

        // Find the AutoCompleteTextView in the layout
        spinnerDriverRoute = findViewById(R.id.spinnerDriverRoute);

        // Set up the adapter for the AutoCompleteTextView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, routes);
        spinnerDriverRoute.setAdapter(adapter);

        btnRegisterDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerDriver();
            }
        });
    }

    private void registerDriver() {
        // Get the input values
        String driverName = editTextDriverName.getText().toString().trim();
        String driverBusId = editTextDriverBusId.getText().toString().trim();
        String driverRoute = spinnerDriverRoute.getText().toString();
        String driverPassword = editTextDriverPassword.getText().toString().trim();

        // Validate the input values
        if (driverName.isEmpty() || driverBusId.isEmpty() || driverPassword.isEmpty()) {
            Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new driver object
        Driver driver = new Driver(driverName, driverBusId, driverRoute, driverPassword);

        // Register the driver with Firebase authentication
        mAuth.createUserWithEmailAndPassword(driverBusId + "@buslocatorsystem.com", driverPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Driver registration successful
                            FirebaseUser user = mAuth.getCurrentUser();
                            String uid = user.getUid();
                            driver.setUid(uid);

                            // Save the driver information to the Firebase database
                            driversRef.child(driver.getBusId()).setValue(driver)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(RegisterDriverActivity.this, "Driver registered successfully", Toast.LENGTH_SHORT).show();
                                                clearFields();
                                            } else {
                                                Toast.makeText(RegisterDriverActivity.this, "Failed to register driver", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                            usersRef.child("drivers").child(uid).child("busId").setValue(driverBusId);

                        } else {
                            // Driver registration failed
                            Toast.makeText(RegisterDriverActivity.this, "Failed to register driver", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    private void clearFields() {
        editTextDriverName.setText("");
        editTextDriverBusId.setText("");
        spinnerDriverRoute.setSelection(0);
        editTextDriverPassword.setText("");
    }
}
