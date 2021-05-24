package tech.greedylabs.crowspot.room;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Vote {
    @PrimaryKey @NonNull
    public String firebaseKey;

    public Vote(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }

}
