package org.robolectric.integrationtests.room;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

@RunWith(RobolectricTestRunner.class)
public final class UserDatabaseTest {

  @Test
  public void upsert_conflict() {
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
