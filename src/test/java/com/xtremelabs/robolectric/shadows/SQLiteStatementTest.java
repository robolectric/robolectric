package com.xtremelabs.robolectric.shadows;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteStatement;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.ResultSet;
import java.sql.Statement;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class SQLiteStatementTest {
    private SQLiteDatabase database;

    @Before
    public void setUp() throws Exception {
        database = SQLiteDatabase.openDatabase("path", null, 0);
        SQLiteStatement createStatement = database.compileStatement("CREATE TABLE `routine` (`id` INTEGER PRIMARY KEY AUTOINCREMENT , `name` VARCHAR , `lastUsed` INTEGER DEFAULT 0 ,  UNIQUE (`name`)) ;");
        createStatement.execute();

        SQLiteStatement createStatement2 = database.compileStatement("CREATE TABLE `countme` (`id` INTEGER PRIMARY KEY AUTOINCREMENT , `name` VARCHAR , `lastUsed` INTEGER DEFAULT 0 ,  UNIQUE (`name`)) ;");
        createStatement2.execute();
    }

    @After
    public void tearDown() throws Exception {
        database.close();
    }

    @Test
    public void testExecuteInsert() throws Exception {
        SQLiteStatement insertStatement = database.compileStatement("INSERT INTO `routine` (`name` ,`lastUsed` ) VALUES (?,?)");
        insertStatement.bindString(1, "Leg Press");
        insertStatement.bindLong(2, 0);
        long pkeyOne = insertStatement.executeInsert();
        insertStatement.clearBindings();
        insertStatement.bindString(1, "Bench Press");
        insertStatement.bindLong(2, 1);
        long pkeyTwo = insertStatement.executeInsert();

        assertThat(pkeyOne, equalTo(1L));
        assertThat(pkeyTwo, equalTo(2L));

        Statement statement = shadowOf(database).getConnection().createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM `routine`");
        assertThat(resultSet.next(), equalTo(true));
        assertThat(resultSet.getInt(1), equalTo(2));

        statement = shadowOf(database).getConnection().createStatement();
        resultSet = statement.executeQuery("SELECT `id`, `name` ,`lastUsed` FROM `routine`");
        assertThat(resultSet.next(), equalTo(true));
        assertThat(resultSet.getInt(1), equalTo(1));
        assertThat(resultSet.getString(2), equalTo("Leg Press"));
        assertThat(resultSet.getInt(3), equalTo(0));
        assertThat(resultSet.next(), equalTo(true));
        assertThat(resultSet.getLong(1), equalTo(2L));
        assertThat(resultSet.getString(2), equalTo("Bench Press"));
        assertThat(resultSet.getInt(3), equalTo(1));
    }

    @Test
    public void simpleQueryTest() throws Exception {

        SQLiteStatement stmt = database.compileStatement("SELECT count(*) FROM `countme`");
        assertThat(stmt.simpleQueryForLong(), equalTo(0L));
        assertThat(stmt.simpleQueryForString(), equalTo("0"));

        SQLiteStatement insertStatement = database.compileStatement("INSERT INTO `countme` (`name` ,`lastUsed` ) VALUES (?,?)");
        insertStatement.bindString(1, "Leg Press");
        insertStatement.bindLong(2, 0);
        insertStatement.executeInsert();
        assertThat(stmt.simpleQueryForLong(), equalTo(1L));
        assertThat(stmt.simpleQueryForString(), equalTo("1"));
        insertStatement.bindString(1, "Bench Press");
        insertStatement.bindLong(2, 1);
        insertStatement.executeInsert();
        assertThat(stmt.simpleQueryForLong(), equalTo(2L));
        assertThat(stmt.simpleQueryForString(), equalTo("2"));
    }

    @Test(expected = SQLiteDoneException.class)
    public void simpleQueryForStringThrowsSQLiteDoneExceptionTest() throws Exception {
        //throw SQLiteDOneException if no rows returned.
        SQLiteStatement stmt = database.compileStatement("SELECT * FROM `countme` where `name`= 'cessationoftime'");

        assertThat(stmt.simpleQueryForString(), equalTo("0"));
    }

    @Test(expected = SQLiteDoneException.class)
    public void simpleQueryForLongThrowsSQLiteDoneExceptionTest() throws Exception {
        //throw SQLiteDOneException if no rows returned.
        SQLiteStatement stmt = database.compileStatement("SELECT * FROM `countme` where `name`= 'cessationoftime'");
        assertThat(stmt.simpleQueryForLong(), equalTo(0L));

    }
}
