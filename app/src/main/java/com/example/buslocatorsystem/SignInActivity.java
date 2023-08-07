package com.example.buslocatorsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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


        mDatabase.child("admins").child(emailorusername).addListenerForSingleValueEvent(new ValueEventListener() {
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
            if (auserType.equals("admin")&&mPasswordField.getText().toString().equals(password)
                    &&(mEmailOrUsernameField.getText().toString().equals(adminEmail)
                    ||mEmailOrUsernameField.getText().toString().equals(username))){

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

        Intent intent = new Intent(SignInActivity.this,TransitionActivity.class);
        intent.putExtra("from","signin");
        startActivity(intent);
    }

}
