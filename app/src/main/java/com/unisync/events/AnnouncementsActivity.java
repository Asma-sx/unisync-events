package com.unisync.events;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.unisync.events.database.EventDatabase;
import com.unisync.events.databinding.ActivityAnnouncementsBinding;
import com.unisync.events.databinding.ItemAnnouncementBinding;
import com.unisync.events.models.Announcement;

import java.util.Collections;
import java.util.List;

public class AnnouncementsActivity extends AppCompatActivity {

    private ActivityAnnouncementsBinding binding;
    private EventDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAnnouncementsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = EventDatabase.getInstance(this);

        getWindow().setStatusBarColor(getColor(R.color.background));
        getWindow().setNavigationBarColor(getColor(R.color.background));

        setupDrawer();
        setupBottomNav();
        setupFAB();
        loadAnnouncements();
    }

    private void setupFAB() {
        binding.fabAddAnnouncement.setOnClickListener(v -> showAddAnnouncementDialog());
    }

    private void showAddAnnouncementDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_announcement, null);
        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etContent = dialogView.findViewById(R.id.etContent);

        new AlertDialog.Builder(this)
                .setTitle("Add New Announcement")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String title = etTitle.getText().toString();
                    String content = etContent.getText().toString();
                    if (!title.trim().isEmpty() && !content.trim().isEmpty()) {
                        Announcement newAnnouncement = new Announcement(title, content, "Just now");
                        EventDatabase.IO.execute(() -> {
                            db.dao().saveAnnouncements(Collections.singletonList(newAnnouncement));
                            runOnUiThread(() -> {
                                if (isDestroyed()) return;
                                loadAnnouncements();
                                Toast.makeText(this, "Announcement added", Toast.LENGTH_SHORT).show();
                            });
                        });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.bottomNav.bottomNavigation.setSelectedItemId(R.id.nav_announcements);
        binding.navView.setCheckedItem(R.id.nav_announcements);
        loadAnnouncements();
    }

    private void open(Class<?> screen) {
        Intent intent = new Intent(this, screen);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    private void setupDrawer() {
        setSupportActionBar(binding.header.toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Announcements");
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
            } else if (id == R.id.nav_contact) {
                open(ContactUsActivity.class);
                return true;
            }
            return false;
        });
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
                open(ContactUsActivity.class);
                return true;
            }
            return false;
        });
    }

    private void loadAnnouncements() {
        binding.rvAnnouncements.setLayoutManager(new LinearLayoutManager(this));
        EventDatabase.IO.execute(() -> {
            List<Announcement> list = db.dao().fetchAnnouncements();
            Collections.reverse(list); // newest first
            runOnUiThread(() -> {
                if (!isDestroyed()) binding.rvAnnouncements.setAdapter(new AnnouncementAdapter(list));
            });
        });
    }

    private void confirmDelete(Announcement item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Announcement")
                .setMessage("Are you sure you want to delete this announcement?")
                .setPositiveButton("Delete", (dialog, which) -> EventDatabase.IO.execute(() -> {
                    db.dao().deleteAnnouncement(item);
                    runOnUiThread(() -> {
                        if (isDestroyed()) return;
                        loadAnnouncements();
                        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                    });
                }))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditAnnouncementDialog(Announcement announcement) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_announcement, null);
        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etContent = dialogView.findViewById(R.id.etContent);

        etTitle.setText(announcement.title);
        etContent.setText(announcement.content);

        new AlertDialog.Builder(this)
                .setTitle("Edit Announcement")
                .setView(dialogView)
                .setPositiveButton("Update", (dialog, which) -> {
                    String title = etTitle.getText().toString();
                    String content = etContent.getText().toString();
                    if (!title.trim().isEmpty() && !content.trim().isEmpty()) {
                        Announcement updated = new Announcement(title, content, announcement.date);
                        updated.id = announcement.id;
                        EventDatabase.IO.execute(() -> {
                            db.dao().updateAnnouncement(updated);
                            runOnUiThread(() -> {
                                if (isDestroyed()) return;
                                loadAnnouncements();
                                Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show();
                            });
                        });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementVH> {
        private final List<Announcement> list;

        AnnouncementAdapter(List<Announcement> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public AnnouncementVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemAnnouncementBinding b = ItemAnnouncementBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new AnnouncementVH(b);
        }

        @Override
        public void onBindViewHolder(@NonNull AnnouncementVH holder, int position) {
            Announcement item = list.get(position);
            holder.binding.tvTitle.setText(item.title);
            holder.binding.tvContent.setText(item.content);
            holder.binding.tvDate.setText(item.date);

            holder.binding.btnEdit.setOnClickListener(v -> showEditAnnouncementDialog(item));
            holder.binding.btnDelete.setOnClickListener(v -> confirmDelete(item));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    static class AnnouncementVH extends RecyclerView.ViewHolder {
        final ItemAnnouncementBinding binding;

        AnnouncementVH(ItemAnnouncementBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
