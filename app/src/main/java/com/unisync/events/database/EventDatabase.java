package com.unisync.events.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.unisync.events.models.Announcement;
import com.unisync.events.models.Event;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Event.class, Announcement.class}, version = 1)
public abstract class EventDatabase extends RoomDatabase {

    public abstract EventDao dao();

    private static volatile EventDatabase INSTANCE;

    // Room does not allow database work on the main thread, so all queries run here
    public static final ExecutorService IO = Executors.newSingleThreadExecutor();

    public static EventDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (EventDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            EventDatabase.class,
                            "unisync_db"
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
