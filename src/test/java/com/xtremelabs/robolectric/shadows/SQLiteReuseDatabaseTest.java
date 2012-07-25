// Copyright 2011-present Facebook. All Rights Reserved.

package com.xtremelabs.robolectric.shadows;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.ResultSet;
import java.sql.Statement;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Tests that the database can be opened multiple times and the same underlying connection
 * is used.
 */
@RunWith(WithTestDefaultsRunner.class)
public class SQLiteReuseDatabaseTest {

    @Test
    public void testMultiple() throws Exception {
        SQLiteDatabase database = SQLiteDatabase.openDatabase("path", null, 0);
        SQLiteStatement createStatement = database.compileStatement("CREATE TABLE `test` (`name` VARCHAR) ;");
        createStatement.execute();

        SQLiteStatement insertStatement = database.compileStatement("INSERT INTO `test` (`name`) VALUES (?)");
        insertStatement.bindString(1, "XXX");
        insertStatement.executeInsert();
        database.close();

        database = SQLiteDatabase.openDatabase("path", null, 0);
        Statement statement = shadowOf(database).getConnection().createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM `test`");
        assertThat(resultSet.next(), equalTo(true));
        database.close();
        ShadowSQLiteDatabase.deleteDatabase("path");
    }
}
