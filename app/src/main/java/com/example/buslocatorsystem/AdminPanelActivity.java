package com.example.buslocatorsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class AdminPanelActivity extends AppCompatActivity {

    private DatabaseReference driversRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        // Initialize Firebase database reference
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        driversRef = firebaseDatabase.getReference("drivers");

        Button btnRegisterDriver = findViewById(R.id.btnRegisterDriver);
        Button btnUpdateDriver = findViewById(R.id.btnUpdateDriver);
        Button btnDeleteDriver = findViewById(R.id.btnDeleteDriver);
        Button btnRetrieveDriver = findViewById(R.id.btnRetrieveDriver);

        btnRegisterDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminPanelActivity.this, RegisterDriverActivity.class));
            }
        });

        btnUpdateDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminPanelActivity.this, UpdateDriverActivity.class));
            }
        });

        btnDeleteDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminPanelActivity.this, DeleteDriverActivity.class));
            }
        });

        btnRetrieveDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AdminPanelActivity.this, RetrieveDriverActivity.class));
            }
        });
    }

}
