package com.example.buslocatorsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.buslocatorsystem.admin.AdminPanelActivity;
import com.example.buslocatorsystem.designmaterial.TransitionActivity;
import com.example.buslocatorsystem.driver.DriverActivity;
import com.example.buslocatorsystem.passenger.PassengerMapActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class SignInActivity extends AppCompatActivity {
    private EditText mEmailOrUsernameField;
    private EditText mPasswordField;
    private Button mSignInButton;
    private TextView mSignUpButton;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private String userName;
    private String passengerEmail;
    private String adminEmail;
    private String userType;

    boolean x = true;
    private String adminUsername,adminPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mEmailOrUsernameField = findViewById(R.id.editTextEmailOrUsernames);
        mPasswordField = findViewById(R.id.editTextPassword);
        mSignInButton = findViewById(R.id.buttonSignIn);
        mSignUpButton = findViewById(R.id.buttonSignUp);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is already authenticated, redirect to appropriate activity
            ProgressBar progressBar = findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);

            redirectToAppropriateActivity(currentUser);
            return;
        }
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });
    }
    String uname;
    private void redirectToAppropriateActivity(FirebaseUser user) {
        String userId = user.getUid();

        if (userType == null){
            // Add your logic to determine the user type based on their ID or other criteria
            // For example, you can check the database to see if the user is an admin, driver, or passenger

            mDatabase.child("users").child("passengers").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        uname = snapshot.child("uname").getValue(String.class);
                        Log.w("debug ",""+uname);
                        mDatabase.child("passengers").child(uname).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    userType = snapshot.child("userType").getValue(String.class);
                                    autoLogin();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            mDatabase.child("users").child("drivers").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        uname = snapshot.child("busId").getValue(String.class);
                        mDatabase.child("drivers").child(uname).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    userType = snapshot.child("userType").getValue(String.class);
                                    autoLogin();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    }

    private void autoLogin(){
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        // Once you determine the user type, redirect them to the appropriate activity
        switch (userType) {
            case "admin":
                startActivity(new Intent(SignInActivity.this, AdminPanelActivity.class));
                break;
            case "driver":
                startActivity(new Intent(SignInActivity.this, DriverActivity.class));
                break;
            case "passenger":
                startActivity(new Intent(SignInActivity.this, PassengerMapActivity.class));
                break;
        }

        finish(); // Close the SignInActivity
    }


    private void signIn() {
        String emailorusername = mEmailOrUsernameField.getText().toString().trim();
        String password = mPasswordField.getText().toString().trim();

        if (TextUtils.isEmpty(emailorusername)) {
            Toast.makeText(this, "Please enter your email or username or bus id.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter your password.", Toast.LENGTH_SHORT).show();
            return;
        }


        mDatabase.child("admins").child("admin").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    adminEmail = snapshot.child("admin_email").getValue(String.class);
                    userType = snapshot.child("userType").getValue(String.class);
                    adminUsername = snapshot.child("admin_user_name").getValue(String.class);
                    adminPassword = snapshot.child("admin_password").getValue(String.class);
                    x = false;
                    AdminLogin(adminEmail, adminPassword, userType,adminUsername);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        mDatabase.child("passengers").child(emailorusername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String pemail = snapshot.child("email").getValue(String.class);
                    String pUserType = snapshot.child("userType").getValue(String.class);
                    x = false;
                    PassengerLogin(pemail, password, pUserType);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        mDatabase.child("drivers").child(emailorusername).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String pemail = emailorusername + "@buslocatorsystem.com";
                    String duserType = snapshot.child("userType").getValue(String.class);
                    System.out.println(pemail + " " + password);
                    x = false;
                    driverLogin(pemail, password, duserType);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
//        if (x) {
//            Toast.makeText(SignInActivity.this, "Fail to authentication, please provide registered username or bus id", Toast.LENGTH_LONG).show();
//        }
    }

    private void AdminLogin(String adminEmail, String password, String auserType,String username) {

        /*if (!mEmailOrUsernameField.getText().toString().trim().equals(adminEmail)){
            Toast.makeText(SignInActivity.this, "Fail to authentication, email unregistered", Toast.LENGTH_LONG).show();
        }
        if (!mEmailOrUsernameField.getText().toString().trim().equals(username)){
            Toast.makeText(SignInActivity.this, "Fail to authentication, username unregistered", Toast.LENGTH_LONG).show();
        }
        if (!mPasswordField.getText().toString().trim().equals(password)){
            Toast.makeText(SignInActivity.this, "Fail to authentication, wrong password", Toast.LENGTH_LONG).show();
        }*/

        if (auserType != null) {
            Log.w(" hye"," " +password);
            if (auserType.equals("admin")&&mPasswordField.getText().toString().equals(password)
                    && mEmailOrUsernameField.getText().toString().equals(username)){

                startActivity(new Intent(SignInActivity.this, AdminPanelActivity.class));
                finish();
            }

        }

    }

    private void driverLogin(String Email, String dpassword, String duserType) {

        mAuth.signInWithEmailAndPassword(Email, dpassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            //redirectToUserActivity(user);
                            startActivity(new Intent(SignInActivity.this, DriverActivity.class));
                            finish();
                        }
                    }
                });
    }

    private void PassengerLogin(String EmailOrUsername, String ppassword, String pUserType) {

        mAuth.signInWithEmailAndPassword(EmailOrUsername, ppassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            //redirectToUserActivity(user);
                            if (pUserType != null) {
                                if (pUserType.equals("passenger")) {
                                    startActivity(new Intent(SignInActivity.this, PassengerMapActivity.class));
                                }
                                finish();
                            }
                        }
                    }
                });
    }
    private void signUp() {

        Intent intent = new Intent(SignInActivity.this, TransitionActivity.class);
        intent.putExtra("from","signin");
        startActivity(intent);
        finish();
    }

}
