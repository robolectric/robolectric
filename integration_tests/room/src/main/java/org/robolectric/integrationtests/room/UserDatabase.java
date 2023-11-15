package org.robolectric.integrationtests.room;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/** Room database for {@link User} */
@Database(
    entities = {User.class},
    version = 1,
    exportSchema = false)
public abstract class UserDatabase extends RoomDatabase {

  public abstract UserDao userDao();

  public static synchronized UserDatabase getInstance(Context context) {
    return Room.databaseBuilder(
            context.getApplicationContext(), UserDatabase.class, "user_database")
        .fallbackToDestructiveMigration()
        .allowMainThreadQueries()
        .build();
  }
}
