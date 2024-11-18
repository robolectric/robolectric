package org.robolectric.integrationtests.room;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.SQLiteMode;

@RunWith(RobolectricTestRunner.class)
public final class UserDatabaseTest {

  /**
   * There was an issue using Room with {@link SQLiteMode.Mode#LEGACY}. The {@link
   * android.database.sqlite.SQLiteException} exceptions were wrapped in a way that was not
   * compatible with Room.
   */
  @Test
  @SQLiteMode(SQLiteMode.Mode.LEGACY)
  public void upsert_conflict_usingLegacySQLite() {
    UserDatabase db = UserDatabase.getInstance(RuntimeEnvironment.getApplication());

    User user = new User();
    user.id = 12;
    user.username = "username";
    user.email = "username@example.com";

    UserDao dao = db.userDao();
    dao.upsert(user);
    dao.upsert(user); // Should succeed

    assertThat(dao.getAllUsers()).hasSize(1);
  }
}
