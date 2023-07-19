package com.example.buslocatorsystem;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    private EditText mNameField;
    private EditText mEmailField;
    private EditText mPhoneField;
    private EditText mPasswordField;
    private EditText mConfirmPasswordField;
    private EditText mUserNameField;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mNameField = findViewById(R.id.editTextName);
        mEmailField = findViewById(R.id.editTextEmail);
        mPhoneField = findViewById(R.id.editTextPhone);
        mPasswordField = findViewById(R.id.editTextPassword);
        mConfirmPasswordField = findViewById(R.id.editTextConfirmPassword);
        mUserNameField = findViewById(R.id.editTextUserNames);

        Button signUpButton = findViewById(R.id.buttonSignUp);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp(
                        mUserNameField.getText().toString(),
                        mNameField.getText().toString(),
                        mEmailField.getText().toString(),
                        mPhoneField.getText().toString(),
                        mPasswordField.getText().toString(),
                        mConfirmPasswordField.getText().toString()
                );
            }
        });
    }

    private void signUp(String uname, String name, String email, String phone, String password, String confirmPassword) {
        if (TextUtils.isEmpty(uname)) {
            Toast.makeText(this, "Please enter your user name.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please enter your name.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter your email.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Please enter your phone number.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter your password.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Please confirm your password.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            savePassengerInfo(uname,user.getUid(), name, email, phone, password);
                            Toast.makeText(SignUpActivity.this, "Sign up successful.", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(SignUpActivity.this, "Sign up failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void savePassengerInfo(String uname, String userId, String name, String email, String phone, String password) {
        String userType = "passenger";
        Passenger passenger = new Passenger(uname,userId, name, email, phone, password, userType);

        mDatabase.child("passengers").child(uname).setValue(passenger);
        mDatabase.child("users").child("passengers").child(userId).child("uname").setValue(passenger.getUname());
    }
}
