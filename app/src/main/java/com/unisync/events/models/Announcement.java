package com.unisync.events.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "announcements")
public class Announcement {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull public String title = "";
    @NonNull public String content = "";
    @NonNull public String date = "";

    // Used by Room
    public Announcement() {
    }

    @Ignore
    public Announcement(@NonNull String title, @NonNull String content, @NonNull String date) {
        this.title = title;
        this.content = content;
        this.date = date;
    }
}
