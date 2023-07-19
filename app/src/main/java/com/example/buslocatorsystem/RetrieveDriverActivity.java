package com.example.buslocatorsystem;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RetrieveDriverActivity extends AppCompatActivity {

    private RecyclerView recyclerViewDeletedDrivers;
    private DeletedDriverAdapter deletedDriversAdapter;
    private List<Driver> deletedDriversList;
    private DatabaseReference deletedDriversRef;
    private FirebaseAuth firebaseAuth;
    private GestureDetectorCompat gestureDetectorCompat;
    private Drawable deleteIcon;
    private ColorDrawable deleteBackground;
    private Drawable retrieveIcon;
    private ColorDrawable retrieveBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retrieve_driver);

        // Initialize Firebase database reference
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        deletedDriversRef = firebaseDatabase.getReference("deleted_drivers");

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize views
        recyclerViewDeletedDrivers = findViewById(R.id.recyclerViewDeletedDrivers);
        SearchView searchView = findViewById(R.id.searchView);

        // Initialize deleted drivers list
        deletedDriversList = new ArrayList<>();

        // Initialize deleted drivers adapter
        deletedDriversAdapter = new DeletedDriverAdapter(this, deletedDriversList);

        // Set the adapter to the recycler view
        recyclerViewDeletedDrivers.setAdapter(deletedDriversAdapter);
        recyclerViewDeletedDrivers.setLayoutManager(new LinearLayoutManager(this));

        // Retrieve all deleted drivers from Firebase database
        retrieveAllDeletedDrivers();

        // Set up search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchDeletedDrivers(newText);
                return true;
            }
        });

        // Initialize gesture detector
        gestureDetectorCompat = new GestureDetectorCompat(this, new GestureListener());

        // Set up swipe functionality
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Driver deletedDriver = deletedDriversList.get(position);

                if (direction == ItemTouchHelper.LEFT) {
                    // Swipe left (delete)
                    showConfirmationDialog(deletedDriver);
                } else if (direction == ItemTouchHelper.RIGHT) {
                    // Swipe right (retrieve)
                    retrieveDriver(deletedDriver);
                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                int backgroundCornerOffset = 20;

                if (dX > 0) {
                    // Swiping to the right (retrieve)
                    retrieveBackground.setBounds(itemView.getLeft(), itemView.getTop(), (int) dX, itemView.getBottom());
                    retrieveBackground.draw(c);

                    retrieveIcon.setBounds(itemView.getLeft() + backgroundCornerOffset, itemView.getTop() + backgroundCornerOffset,
                            itemView.getLeft() + backgroundCornerOffset + retrieveIcon.getIntrinsicWidth(),
                            itemView.getBottom() - backgroundCornerOffset);
                    retrieveIcon.draw(c);
                } else if (dX < 0) {
                    // Swiping to the left (delete)
                    deleteBackground.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
                    deleteBackground.draw(c);

                    deleteIcon.setBounds(itemView.getRight() - backgroundCornerOffset - deleteIcon.getIntrinsicWidth(), itemView.getTop() + backgroundCornerOffset,
                            itemView.getRight() - backgroundCornerOffset, itemView.getBottom() - backgroundCornerOffset);
                    deleteIcon.draw(c);
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerViewDeletedDrivers);
        deleteIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_delete, null);
        deleteBackground = new ColorDrawable(Color.RED);
        retrieveIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_retrieve, null);
        retrieveBackground = new ColorDrawable(Color.GREEN);
    }

    private void retrieveAllDeletedDrivers() {
        deletedDriversRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                deletedDriversList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Driver deletedDriver = snapshot.getValue(Driver.class);
                    deletedDriversList.add(deletedDriver);
                }
                deletedDriversAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(RetrieveDriverActivity.this, "Failed to retrieve deleted drivers.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchDeletedDrivers(String query) {
        Query searchQuery = deletedDriversRef.orderByChild("busId").startAt(query).endAt(query + "\uf8ff");
        searchQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                deletedDriversList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Driver deletedDriver = snapshot.getValue(Driver.class);
                    deletedDriversList.add(deletedDriver);
                }
                deletedDriversAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(RetrieveDriverActivity.this, "Failed to search deleted drivers.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void retrieveDriver(final Driver driver) {
        deletedDriversRef.child(driver.getBusId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                if (task.isSuccessful()) {
                    // Register driver in Firebase Authentication and store UID
                    firebaseAuth.createUserWithEmailAndPassword(driver.getBusId()+"@buslocatorsystem.com", driver.getPassword())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = firebaseAuth.getCurrentUser();
                                        String uid = user.getUid();

                                        // Update driver's UID in Firebase Realtime Database
                                        DatabaseReference driversRef = FirebaseDatabase.getInstance().getReference("drivers");
                                        driver.setUid(uid);
                                        driversRef.child(driver.getBusId()).setValue(driver)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(RetrieveDriverActivity.this, "Driver retrieved successfully.", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(RetrieveDriverActivity.this, "Failed to retrieve driver.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(RetrieveDriverActivity.this, "Failed to register driver.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    Toast.makeText(RetrieveDriverActivity.this, "Failed to retrieve driver.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showConfirmationDialog(final Driver driver) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Driver")
                .setMessage("Are you sure you want to delete this driver permanently?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteDriver(driver);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        retrieveAllDeletedDrivers();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void deleteDriver(Driver driver) {
        deletedDriversRef.child(driver.getBusId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(RetrieveDriverActivity.this, "Driver deleted permanently.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RetrieveDriverActivity.this, "Failed to delete driver.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetectorCompat.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }
}
