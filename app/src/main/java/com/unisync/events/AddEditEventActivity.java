package com.unisync.events;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.unisync.events.database.EventDatabase;
import com.unisync.events.databinding.ActivityAddEditEventBinding;
import com.unisync.events.models.Event;

import java.util.Collections;

public class AddEditEventActivity extends AppCompatActivity {

    private static final String DEFAULT_IMAGE = "https://picsum.photos/id/100/800/600";

    private ActivityAddEditEventBinding binding;
    private EventDatabase db;
    private int eventId = -1;
    private String selectedImageUri = null;
    private Event currentEvent = null;

    private final ActivityResultLauncher<String[]> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    selectedImageUri = uri.toString();
                    Glide.with(this).load(uri).into(binding.ivPreview);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEditEventBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = EventDatabase.getInstance(this);
        eventId = getIntent().getIntExtra("ID", -1);

        setupToolbar();
        setupCategorySpinner();
        setupListeners();

        if (eventId != -1) {
            loadEvent();
        }
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(eventId == -1 ? "Add New Event" : "Edit Event");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupCategorySpinner() {
        String[] categories = getResources().getStringArray(R.array.event_categories);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        binding.autoCompleteCategory.setAdapter(adapter);
    }

    private void setupListeners() {
        binding.btnPickImage.setOnClickListener(v -> pickImageLauncher.launch(new String[]{"image/*"}));
        binding.btnSave.setOnClickListener(v -> saveEvent());
    }

    private void loadEvent() {
        EventDatabase.IO.execute(() -> {
            Event event = db.dao().fetchById(eventId);
            runOnUiThread(() -> {
                if (isDestroyed() || event == null) return;
                currentEvent = event;
                binding.etTitle.setText(event.title);
                binding.etInfo.setText(event.info);
                binding.etLocation.setText(event.location);
                binding.etDate.setText(event.dateString);
                binding.autoCompleteCategory.setText(event.category, false);
                selectedImageUri = event.image;
                Glide.with(this).load(event.image).into(binding.ivPreview);
            });
        });
    }

    private static String textOf(CharSequence text) {
        return text == null ? "" : text.toString();
    }

    private void saveEvent() {
        String title = textOf(binding.etTitle.getText());
        String info = textOf(binding.etInfo.getText());
        String location = textOf(binding.etLocation.getText());
        String date = textOf(binding.etDate.getText());
        String category = textOf(binding.autoCompleteCategory.getText());

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }

        String image = selectedImageUri != null ? selectedImageUri : DEFAULT_IMAGE;
        Event event = new Event(title, info, date, category, image, location);
        if (currentEvent != null) {
            // Editing: keep the same id (so the row is replaced) and the favorite flag
            event.id = currentEvent.id;
            event.favorite = currentEvent.favorite;
        }

        EventDatabase.IO.execute(() -> {
            db.dao().saveEvents(Collections.singletonList(event));
            runOnUiThread(() -> {
                Toast.makeText(this, "Event saved successfully", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
