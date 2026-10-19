package com.unisync.events.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.unisync.events.models.Announcement;
import com.unisync.events.models.Event;

import java.util.List;

@Dao
public interface EventDao {

    @Query("SELECT * FROM campus_events")
    List<Event> fetchAll();

    @Query("SELECT * FROM campus_events WHERE title LIKE '%' || :query || '%'")
    List<Event> searchEvents(String query);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveEvents(List<Event> events);

    @Update
    void update(Event event);

    @Query("SELECT * FROM campus_events WHERE id = :id")
    Event fetchById(int id);

    @Query("SELECT * FROM announcements")
    List<Announcement> fetchAnnouncements();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveAnnouncements(List<Announcement> list);

    @Delete
    void deleteEvent(Event event);

    @Update
    void updateAnnouncement(Announcement announcement);

    @Delete
    void deleteAnnouncement(Announcement announcement);
}
