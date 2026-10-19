package com.unisync.events.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "campus_events")
public class Event {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull public String title = "";
    @NonNull public String info = "";
    @NonNull public String dateString = "";
    @NonNull public String category = "";
    @NonNull public String image = "";
    @NonNull public String location = "Al Imam University";
    public boolean favorite = false;

    // Used by Room
    public Event() {
    }

    @Ignore
    public Event(@NonNull String title, @NonNull String info, @NonNull String dateString,
                 @NonNull String category, @NonNull String image, @NonNull String location) {
        this.title = title;
        this.info = info;
        this.dateString = dateString;
        this.category = category;
        this.image = image;
        this.location = location;
    }
}
