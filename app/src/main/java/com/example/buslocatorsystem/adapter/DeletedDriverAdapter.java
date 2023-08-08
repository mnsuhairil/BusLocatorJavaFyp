package com.example.buslocatorsystem.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.buslocatorsystem.constructor.Driver;
import com.example.buslocatorsystem.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class DeletedDriverAdapter extends RecyclerView.Adapter<DeletedDriverAdapter.ViewHolder> {

    private Context context;
    private List<Driver> deletedDriversList;

    public DeletedDriverAdapter(Context context, List<Driver> deletedDriversList) {
        this.context = context;
        this.deletedDriversList = deletedDriversList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_deleted_driver, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Driver deletedDriver = deletedDriversList.get(position);
        holder.bind(deletedDriver);
    }

    @Override
    public int getItemCount() {
        return deletedDriversList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewName;
        private TextView textViewBusId;
        private ImageView imageViewDriver;
        private TextView textViewRoute;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName1);
            textViewBusId = itemView.findViewById(R.id.textViewBusId1);
            imageViewDriver = itemView.findViewById(R.id.imageViewDriver1);
            textViewRoute = itemView.findViewById(R.id.textViewRoute1);

            // Add click listener to the itemView (the root view of the item layout)
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Driver driver = deletedDriversList.get(position);
                        showConfirmationDialog(driver);
                    }
                }
            });
        }
        private void showConfirmationDialog(final Driver driver) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
                                                                            Toast.makeText(context, "Failed to delete driver's authentication", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }
                                                                });
                                                    } else {
                                                        Toast.makeText(context, "Failed to reauthenticate driver", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                } else {
                                    Toast.makeText(context, "Admin user not found", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(context, "Admin login failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        }
        private DatabaseReference driversRef;
        private DatabaseReference deletedDriversRef;
        private void deleteDriverData(final Driver driver) {
            // Initialize Firebase database references
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            driversRef = firebaseDatabase.getReference("drivers");
            deletedDriversRef = firebaseDatabase.getReference("deleted_drivers");
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
                                                    Toast.makeText(context, "Driver deleted successfully", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(context, "Failed to delete driver", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            } else {
                                Toast.makeText(context, "Failed to delete driver", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        public void bind(Driver deletedDriver) {
            textViewName.setText(deletedDriver.getName());
            textViewBusId.setText(deletedDriver.getBusId());
            Glide.with(context)
                    .load(deletedDriver.getImageUrl()) // Use driver.getImageUrl() for loading the image from URL or use driver.getImageResourceId() for loading from resource ID
                    .placeholder(R.drawable.default_profile_image) // Replace with a placeholder drawable while loading the image
                    .error(R.drawable.error_image) // Replace with an error drawable if image loading fails
                    .into(imageViewDriver);
            String currentRoute = deletedDriver.getRoute();
            if (Objects.equals(currentRoute, "Route 1")){
                currentRoute = "1";
            }else if(Objects.equals(currentRoute, "Route 2")){
                currentRoute = "2";
            }else if (Objects.equals(currentRoute, "Route 3")){
                currentRoute = "3";
            }

            textViewRoute.setText(currentRoute);
        }
    }
}

