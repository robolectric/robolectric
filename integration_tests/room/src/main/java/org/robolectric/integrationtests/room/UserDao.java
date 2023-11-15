package org.robolectric.integrationtests.room;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Upsert;
import java.util.List;

/** Dao for {@link User} */
@Dao
public interface UserDao {
  @Upsert
  void upsert(User user);

  @Insert
  void insert(User user);

  @Query("SELECT * FROM users")
  List<User> getAllUsers();
}
