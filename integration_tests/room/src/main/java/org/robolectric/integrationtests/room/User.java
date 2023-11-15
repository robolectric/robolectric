package org.robolectric.integrationtests.room;

import androidx.room.Entity;

/** A simple User entity */
@Entity(
    tableName = "users",
    primaryKeys = {"id"})
public class User {
  public long id;

  public String username;

  public String email;
}
