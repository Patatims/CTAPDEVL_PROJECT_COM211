package com.example.imagepro;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;

import com.example.imagepro.databinding.ActivityMainBinding;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {
    static {
        if (OpenCVLoader.initDebug()) {
            Log.d("MainActivity: ", "Opencv is loaded");
        } else {
            Log.d("MainActivity: ", "Opencv failed to load");
        }
    }

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Show splash screen layout
        setContentView(R.layout.splash_screen);

        // Hide status bar and action bar for splash screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Delayed execution to transition to main content
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // After delay, navigate to main content (HomeFragment)
                setContentView(R.layout.activity_main);
                setupBottomNavigation(); // Setup bottom navigation and fragment management
                replaceFragment(new HomeFragment());
            }
        }, 5000); // 5secs
    }

    private void setupBottomNavigation() {
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home:
                    replaceFragment(new HomeFragment());
                    break;
                case R.id.camera:
                    replaceFragment(new EDMFragment());
                    break;
                case R.id.about:
                    replaceFragment(new AboutFragment());
                    break;
            }
            return true;
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}
