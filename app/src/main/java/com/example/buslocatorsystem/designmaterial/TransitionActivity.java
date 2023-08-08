package com.example.buslocatorsystem.designmaterial;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;
import com.example.buslocatorsystem.R;
import com.example.buslocatorsystem.SignInActivity;
import com.example.buslocatorsystem.SignUpActivity;

public class TransitionActivity extends AppCompatActivity {

    private String activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transition);

        View signup = findViewById(R.id.view1);
        View signin = findViewById(R.id.view2);

        activity = getIntent().getStringExtra("from");
        int scroll = getIntent().getIntExtra("scroll",0);
        System.out.println("debug "+scroll);
        if (activity.equals("signup")){
            signup.post(() -> signup.scrollTo(0, scroll));
            signin.setVisibility(View.GONE);
            signup.setVisibility(View.VISIBLE);


        }else if (activity.equals("signin")){
            signin.setVisibility(View.VISIBLE);
            signup.setVisibility(View.GONE);
        }

        // Hide the view initially


        // Delayed visibility change after 1 second (1000 milliseconds)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (activity.equals("signup")){
                    signin.setVisibility(View.VISIBLE);
                    signup.setVisibility(View.GONE);
                }else if (activity.equals("signin")){
                    signin.setVisibility(View.GONE);
                    signup.setVisibility(View.VISIBLE);
                }

            }
        }, 1000); // 1000 milliseconds = 1 second
        LottieAnimationView lottieAnimationView = findViewById(R.id.lottieAnimationView);
        lottieAnimationView.setAnimation(R.raw.page_transition);
        lottieAnimationView.playAnimation();

        // Set a listener to know when the animation finishes
        lottieAnimationView.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Animation has finished, start the SignupActivity
                if (activity.equals("signup")){
                    startActivity(new Intent(TransitionActivity.this, SignInActivity.class));
                    finish(); // Optional: If you want to close the transition activity after starting the SignupActivity
                }else if (activity.equals("signin")){
                    startActivity(new Intent(TransitionActivity.this, SignUpActivity.class));
                    finish(); // Optional: If you want to close the transition activity after starting the SignupActivity
                }

            }
        });
    }
}
