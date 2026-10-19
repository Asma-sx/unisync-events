package com.unisync.events;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.unisync.events.databinding.ActivityContactBinding;

public class ContactUsActivity extends AppCompatActivity {

    private ActivityContactBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityContactBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Seamless "One Color" Theme Setup
        getWindow().setStatusBarColor(getColor(R.color.background));
        getWindow().setNavigationBarColor(getColor(R.color.background));

        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(false);
            controller.setAppearanceLightNavigationBars(false);
        }

        setupDrawer();
        setupBottomNav();

        binding.btnEmail.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:support@unisync.edu.sa"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "UniSync Support");
            startActivity(intent);
        });

        binding.btnX.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/imamu"))));

        binding.btnFacebook.setOnClickListener(v ->
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://facebook.com/imamu"))));
    }

    private void open(Class<?> screen) {
        Intent intent = new Intent(this, screen);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    private void setupDrawer() {
        setSupportActionBar(binding.header.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Contact Us");
        binding.header.toolbar.setTitleTextColor(getColor(R.color.primary));

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, binding.drawerLayout,
                binding.header.toolbar, R.string.open, R.string.close);
        toggle.getDrawerArrowDrawable().setColor(getColor(R.color.primary));
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        binding.navView.setNavigationItemSelectedListener(menuItem -> {
            binding.drawerLayout.closeDrawers();
            int id = menuItem.getItemId();
            if (id == R.id.nav_home) {
                open(MainActivity.class);
                return true;
            } else if (id == R.id.nav_events) {
                open(EventsActivity.class);
                return true;
            } else if (id == R.id.nav_announcements) {
                open(AnnouncementsActivity.class);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.bottomNav.bottomNavigation.setSelectedItemId(R.id.nav_contact);
        binding.navView.setCheckedItem(R.id.nav_contact);
    }

    private void setupBottomNav() {
        binding.bottomNav.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                open(MainActivity.class);
                return true;
            } else if (id == R.id.nav_events) {
                open(EventsActivity.class);
                return true;
            } else if (id == R.id.nav_contact) {
                return true;
            }
            return false;
        });
        binding.bottomNav.bottomNavigation.setSelectedItemId(R.id.nav_contact);
    }
}
