package com.unisync.events;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.bumptech.glide.Glide;
import com.unisync.events.database.EventDatabase;
import com.unisync.events.databinding.ActivityDetailsBinding;
import com.unisync.events.models.Event;

public class EventDetailsActivity extends AppCompatActivity {

    private ActivityDetailsBinding binding;
    private EventDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Seamless "One Color" Theme Setup
        getWindow().setStatusBarColor(getColor(R.color.background));
        getWindow().setNavigationBarColor(getColor(R.color.background));

        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(false);
            controller.setAppearanceLightNavigationBars(false);
        }

        db = EventDatabase.getInstance(this);
        int id = getIntent().getIntExtra("ID", -1);

        if (id != -1) {
            load(id);
        }

        setupBottomNav();
    }

    private void setupBottomNav() {
        binding.bottomNav.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home || id == R.id.nav_events) {
                finish();
                return true;
            } else if (id == R.id.nav_contact) {
                startActivity(new Intent(this, ContactUsActivity.class));
                finish();
                return true;
            }
            return false;
        });
        binding.bottomNav.bottomNavigation.setSelectedItemId(R.id.nav_events);
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void load(int id) {
        EventDatabase.IO.execute(() -> {
            Event e = db.dao().fetchById(id);
            runOnUiThread(() -> {
                if (isDestroyed() || e == null) return;
                binding.tvTitle.setText(e.title);
                binding.tvCategory.setText(e.category);
                binding.tvDate.setText(e.dateString);
                binding.tvDetails.setText(e.info);
                binding.tvLocation.setText(e.location);

                updateFavoriteIcon(e.favorite);

                binding.fabFavorite.setOnClickListener(v -> {
                    e.favorite = !e.favorite;
                    EventDatabase.IO.execute(() -> {
                        db.dao().update(e);
                        runOnUiThread(() -> {
                            if (isDestroyed()) return;
                            updateFavoriteIcon(e.favorite);
                            String msg = e.favorite ? "Added to My Events" : "Removed from My Events";
                            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                        });
                    });
                });

                Glide.with(this)
                        .load(e.image)
                        .placeholder(R.drawable.placeholder_unisync)
                        .error(R.drawable.placeholder_unisync)
                        .centerCrop()
                        .into(binding.ivEventImage);
            });
        });
    }

    private void updateFavoriteIcon(boolean isFavorite) {
        binding.fabFavorite.setImageResource(isFavorite
                ? android.R.drawable.btn_star_big_on
                : android.R.drawable.btn_star_big_off);
    }
}
