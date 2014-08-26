package org.robolectric.shadows;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.Robolectric;

import java.sql.ResultSet;
import java.sql.Statement;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.robolectric.Robolectric.shadowOf;


public abstract class DatabaseTestBase {

}
