package tech.greedylabs.crowspot.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Vote.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public static AppDatabase instance;
    public abstract VoteDao voteDao();

    public static synchronized AppDatabase getInstance(Context context)
    {
        if(instance ==null)
        {
            instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "Crowspot")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }
}