package com.unisync.events;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.unisync.events.adapters.EventAdapter;
import com.unisync.events.database.EventDatabase;
import com.unisync.events.databinding.ActivityEventsBinding;
import com.unisync.events.models.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EventsActivity extends AppCompatActivity {

    private ActivityEventsBinding binding;
    private EventDatabase db;
    private EventAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEventsBinding.inflate(getLayoutInflater());
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
        setupDrawer();
        setupBottomNav();
        setupRecycler();
        setupFilters();
        setupSearch();
        setupFAB();
        load("");
    }

    private void setupSearch() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                loadCurrentCategory();
                return true;
            }
        });
    }

    private void setupFAB() {
        binding.fabAddEvent.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditEventActivity.class)));
    }

    private String categoryForChip(int chipId) {
        if (chipId == R.id.chipAcademic) return "Academic";
        if (chipId == R.id.chipCultural) return "Cultural";
        if (chipId == R.id.chipSports) return "Sports";
        if (chipId == R.id.chipFavorites) return "Favorites";
        return ""; // chipAll or nothing selected
    }

    private void loadCurrentCategory() {
        load(categoryForChip(binding.chipGroup.getCheckedChipId()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.bottomNav.bottomNavigation.setSelectedItemId(R.id.nav_events);
        binding.navView.setCheckedItem(R.id.nav_events);
        loadCurrentCategory(); // Refresh list when returning from Add/Edit page
    }

    private void open(Class<?> screen) {
        Intent intent = new Intent(this, screen);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    private void setupDrawer() {
        setSupportActionBar(binding.header.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Events");
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

    private void setupRecycler() {
        adapter = new EventAdapter(
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
                e -> new AlertDialog.Builder(this)
                        .setTitle("Delete Event")
                        .setMessage("Are you sure you want to delete '" + e.title + "'?")
                        .setPositiveButton("Delete", (dialog, which) -> EventDatabase.IO.execute(() -> {
                            db.dao().deleteEvent(e);
                            runOnUiThread(() -> {
                                if (isDestroyed()) return;
                                loadCurrentCategory();
                                Toast.makeText(this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
                            });
                        }))
                        .setNegativeButton("Cancel", null)
                        .show()
        );
        binding.rvEvents.setLayoutManager(new LinearLayoutManager(this));
        binding.rvEvents.setAdapter(adapter);

        RecyclerView.ItemAnimator animator = binding.rvEvents.getItemAnimator();
        if (animator instanceof DefaultItemAnimator) {
            ((DefaultItemAnimator) animator).setSupportsChangeAnimations(false);
        }
    }

    private void setupBottomNav() {
        binding.bottomNav.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                open(MainActivity.class);
                return true;
            } else if (id == R.id.nav_events) {
                return true;
            } else if (id == R.id.nav_contact) {
                open(ContactUsActivity.class);
                return true;
            }
            return false;
        });
        binding.bottomNav.bottomNavigation.setSelectedItemId(R.id.nav_events);
    }

    private void setupFilters() {
        binding.chipGroup.setOnCheckedChangeListener((group, checkedId) -> load(categoryForChip(checkedId)));
        binding.chipAll.setChecked(true);
    }

    private void load(String category) {
        String query = binding.searchView.getQuery().toString().toLowerCase(Locale.ROOT);
        EventDatabase.IO.execute(() -> {
            List<Event> list = db.dao().fetchAll();
            List<Event> filtered = new ArrayList<>();
            for (Event e : list) {
                boolean matchesCategory;
                if (category.isEmpty()) {
                    matchesCategory = true;
                } else if (category.equals("Favorites")) {
                    matchesCategory = e.favorite;
                } else {
                    matchesCategory = e.category.equalsIgnoreCase(category);
                }
                boolean matchesQuery = query.isEmpty()
                        || e.title.toLowerCase(Locale.ROOT).contains(query)
                        || e.info.toLowerCase(Locale.ROOT).contains(query);
                if (matchesCategory && matchesQuery) filtered.add(e);
            }
            runOnUiThread(() -> {
                if (!isDestroyed()) adapter.updateData(filtered);
            });
        });
    }
}
