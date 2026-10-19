package com.unisync.events;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.unisync.events.adapters.EventAdapter;
import com.unisync.events.adapters.SliderAdapter;
import com.unisync.events.database.EventDao;
import com.unisync.events.database.EventDatabase;
import com.unisync.events.databinding.ActivityMainBinding;
import com.unisync.events.models.Announcement;
import com.unisync.events.models.Event;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private final Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Runnable sliderRunnable;
    private EventAdapter eventAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Seamless "One Color" Theme Setup
        getWindow().setStatusBarColor(getColor(R.color.background));
        getWindow().setNavigationBarColor(getColor(R.color.background));

        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(false);
            controller.setAppearanceLightNavigationBars(false);
        }

        setupCarousel();
        setupLists();
        setupDrawer();
        setupBottomNav();
        prepopulateData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.bottomNav.bottomNavigation.setSelectedItemId(R.id.nav_home);
        binding.navView.setCheckedItem(R.id.nav_home);
        sliderHandler.postDelayed(sliderRunnable, 2000);
        loadEvents();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    private void open(Class<?> screen) {
        Intent intent = new Intent(this, screen);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    private void setupDrawer() {
        setSupportActionBar(binding.header.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Home");
        binding.header.toolbar.setTitleTextColor(getColor(R.color.primary));

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, binding.drawerLayout, binding.header.toolbar,
                R.string.open, R.string.close);
        toggle.getDrawerArrowDrawable().setColor(getColor(R.color.primary));
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        binding.navView.setNavigationItemSelectedListener(item -> {
            binding.drawerLayout.closeDrawers();
            int id = item.getItemId();
            if (id == R.id.nav_events) {
                open(EventsActivity.class);
                return true;
            } else if (id == R.id.nav_announcements) {
                open(AnnouncementsActivity.class);
                return true;
            } else if (id == R.id.nav_contact) {
                open(ContactUsActivity.class);
                return true;
            }
            return false;
        });
    }

    private void setupLists() {
        // Setup Feed Recycler
        eventAdapter = new EventAdapter(
                new ArrayList<>(),
                e -> {
                    Intent intent = new Intent(this, EventDetailsActivity.class);
                    intent.putExtra("ID", e.id);
                    startActivity(intent);
                },
                e -> {
                    Intent intent = new Intent(this, AddEditEventActivity.class);
                    intent.putExtra("ID", e.id);
                    startActivity(intent);
                },
                e -> EventDatabase.IO.execute(() -> {
                    EventDatabase.getInstance(this).dao().deleteEvent(e);
                    runOnUiThread(this::loadEvents);
                })
        );
        binding.rvEventsFeed.setLayoutManager(new LinearLayoutManager(this));
        binding.rvEventsFeed.setAdapter(eventAdapter);
    }

    private void loadEvents() {
        EventDatabase db = EventDatabase.getInstance(this);
        EventDatabase.IO.execute(() -> {
            List<Event> events = db.dao().fetchAll();
            // Show the 5 most recent events, newest first
            List<Event> recent = new ArrayList<>(events.subList(Math.max(0, events.size() - 5), events.size()));
            Collections.reverse(recent);
            runOnUiThread(() -> {
                if (!isDestroyed()) eventAdapter.updateData(recent);
            });
        });
    }

    private void setupCarousel() {
        List<String> images = Arrays.asList(
                "https://picsum.photos/id/1/1000/600",
                "https://picsum.photos/id/2/1000/600",
                "https://picsum.photos/id/3/1000/600"
        );
        binding.imageSlider.setAdapter(new SliderAdapter(images));

        sliderRunnable = new Runnable() {
            @Override
            public void run() {
                int current = binding.imageSlider.getCurrentItem();
                int next = (current == images.size() - 1) ? 0 : current + 1;
                binding.imageSlider.setCurrentItem(next, true);
                sliderHandler.postDelayed(this, 2000);
            }
        };
    }

    private void prepopulateData() {
        EventDatabase db = EventDatabase.getInstance(this);
        EventDatabase.IO.execute(() -> {
            EventDao dao = db.dao();
            if (dao.fetchAll().isEmpty()) {
                List<Event> dummyEvents = Arrays.asList(
                        new Event(
                                "Annual Tech Symposium",
                                "A gathering of tech enthusiasts to discuss the future of AI in Saudi Arabia.",
                                "28 April 2026 | 09:00 AM",
                                "Academic",
                                "https://picsum.photos/id/10/800/600",
                                "College of Computer Science"),
                        new Event(
                                "Cultural Heritage Festival",
                                "Celebrating the rich history and traditions of the Kingdom through art and food.",
                                "29 April 2026 | 05:00 PM",
                                "Cultural",
                                "https://picsum.photos/id/20/800/600",
                                "Central Plaza"),
                        new Event(
                                "Inter-University Football Cup",
                                "The final match of the season. Support our team!",
                                "30 April 2026 | 07:00 PM",
                                "Sports",
                                "https://picsum.photos/id/30/800/600",
                                "Main Stadium")
                );
                dao.saveEvents(dummyEvents);
            }

            if (dao.fetchAnnouncements().isEmpty()) {
                List<Announcement> dummyAnnouncements = Arrays.asList(
                        new Announcement(
                                "Registration Deadline Extended",
                                "The deadline for summer semester registration has been extended to May 5th.",
                                "24 April 2026"),
                        new Announcement(
                                "Campus Maintenance Notice",
                                "The library will be closed this Saturday for scheduled maintenance.",
                                "25 April 2026")
                );
                dao.saveAnnouncements(dummyAnnouncements);
            }
            // Refresh the feed once the sample data is in place
            runOnUiThread(() -> {
                if (!isDestroyed()) loadEvents();
            });
        });
    }

    private void setupBottomNav() {
        binding.bottomNav.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_events) {
                open(EventsActivity.class);
                return true;
            } else if (id == R.id.nav_contact) {
                open(ContactUsActivity.class);
                return true;
            }
            return false;
        });
        binding.bottomNav.bottomNavigation.setSelectedItemId(R.id.nav_home);
    }
}
