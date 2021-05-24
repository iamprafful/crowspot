package tech.greedylabs.crowspot.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface VoteDao {

    @Query("SELECT count(*) from vote where firebaseKey=:key")
    int voted(String key);

    @Insert
    void insert(Vote vote);
}
