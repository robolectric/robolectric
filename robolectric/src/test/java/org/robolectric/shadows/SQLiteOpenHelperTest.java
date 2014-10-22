package org.robolectric.shadows;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class SQLiteOpenHelperTest {

  private TestOpenHelper helper;

  @Before
  public void setUp() throws Exception {
    helper = new TestOpenHelper(Robolectric.application, "path", null, 1);
  }

  @Test
  public void testConstructorWithNullPathShouldCreateInMemoryDatabase() throws Exception {
    TestOpenHelper helper = new TestOpenHelper(null, null, null, 1);
    SQLiteDatabase database = helper.getReadableDatabase();
    assertDatabaseOpened(database, helper);
    assertInitialDB(database, helper);
  }

  @Test
  public void testInitialGetReadableDatabase() throws Exception {
    SQLiteDatabase database = helper.getReadableDatabase();
    assertInitialDB(database, helper);
  }

  @Test
  public void testSubsequentGetReadableDatabase() throws Exception {
    helper.getReadableDatabase();
    helper.close();
    SQLiteDatabase database = helper.getReadableDatabase();

    assertSubsequentDB(database, helper);
  }

  @Test
  public void testSameDBInstanceSubsequentGetReadableDatabase() throws Exception {
    SQLiteDatabase db1 = helper.getReadableDatabase();
    SQLiteDatabase db2 = helper.getReadableDatabase();

    assertThat(db1).isSameAs(db2);
  }

  @Test
  public void testInitialGetWritableDatabase() throws Exception {
    SQLiteDatabase database = helper.getWritableDatabase();
    assertInitialDB(database, helper);
  }

  @Test
  public void testSubsequentGetWritableDatabase() throws Exception {
    helper.getWritableDatabase();
    helper.close();

    assertSubsequentDB(helper.getWritableDatabase(), helper);
  }

  @Test
  public void testSameDBInstanceSubsequentGetWritableDatabase() throws Exception {
    SQLiteDatabase db1 = helper.getWritableDatabase();
    SQLiteDatabase db2 = helper.getWritableDatabase();

    assertThat(db1).isSameAs(db2);
  }

  @Test
  public void testClose() throws Exception {
    SQLiteDatabase database = helper.getWritableDatabase();

    assertThat(database.isOpen()).isTrue();
    helper.close();
    assertThat(database.isOpen()).isFalse();
  }

  @Test
  public void testGetPath() throws Exception {
    final String path1 = "path1", path2 = "path2";

    TestOpenHelper helper1 = new TestOpenHelper(Robolectric.application, path1, null, 1);
    String expectedPath1 = Robolectric.application.getDatabasePath(path1).getAbsolutePath();
    assertThat(helper1.getReadableDatabase().getPath()).isEqualTo(expectedPath1);

    TestOpenHelper helper2 = new TestOpenHelper(Robolectric.application, path2, null, 1);
    String expectedPath2 = Robolectric.application.getDatabasePath(path2).getAbsolutePath();
    assertThat(helper2.getReadableDatabase().getPath()).isEqualTo(expectedPath2);
  }

  @Test
  public void testCloseMultipleDbs() throws Exception {
    TestOpenHelper helper2 = new TestOpenHelper(Robolectric.application, "path2", null, 1);
    SQLiteDatabase database1 = helper.getWritableDatabase();
    SQLiteDatabase database2 = helper2.getWritableDatabase();
    assertThat(database1.isOpen()).isTrue();
    assertThat(database2.isOpen()).isTrue();
    helper.close();
    assertThat(database1.isOpen()).isFalse();
    assertThat(database2.isOpen()).isTrue();
    helper2.close();
    assertThat(database2.isOpen()).isFalse();
  }

  @Test
  public void testOpenMultipleDbsOnCreate() throws Exception {
    TestOpenHelper helper2 = new TestOpenHelper(Robolectric.application, "path2", null, 1);
    assertThat(helper.onCreateCalled).isFalse();
    assertThat(helper2.onCreateCalled).isFalse();
    helper.getWritableDatabase();
    assertThat(helper.onCreateCalled).isTrue();
    assertThat(helper2.onCreateCalled).isFalse();
    helper2.getWritableDatabase();
    assertThat(helper.onCreateCalled).isTrue();
    assertThat(helper2.onCreateCalled).isTrue();
    helper.close();
    helper2.close();
  }

  private void setupTable(SQLiteDatabase db, String table) {
    db.execSQL("CREATE TABLE " + table + " (" +
        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "testVal INTEGER DEFAULT 0" +
        ");");
  }
  private void insertData(SQLiteDatabase db, String table, int[] values) {
    for (int i : values) {
      ContentValues cv = new ContentValues();
      cv.put("testVal", i);
      db.insert(table, null, cv);
    }
  }

  private void verifyData(SQLiteDatabase db, String table, int expectedVals) {
    assertThat(db.query(table, null, null, null,
          null, null, null).getCount()).isEqualTo(expectedVals);
  }

  @Test
  public void testMultipleDbsPreserveData() throws Exception {
    final String TABLE_NAME1 = "fart", TABLE_NAME2 = "fart2";
    SQLiteDatabase db1 = helper.getWritableDatabase();
    setupTable(db1, TABLE_NAME1);
    insertData(db1, TABLE_NAME1, new int[]{1, 2});
    TestOpenHelper helper2 = new TestOpenHelper(Robolectric.application, "path2", null, 1);
    SQLiteDatabase db2 = helper2.getWritableDatabase();
    setupTable(db2, TABLE_NAME2);
    insertData(db2, TABLE_NAME2, new int[]{4, 5, 6});
    verifyData(db1, TABLE_NAME1, 2);
    verifyData(db2, TABLE_NAME2, 3);
  }

  @Test
  public void testCloseOneDbKeepsDataForOther() throws Exception {
    final String TABLE_NAME1 = "fart", TABLE_NAME2 = "fart2";
    TestOpenHelper helper2 = new TestOpenHelper(Robolectric.application, "path2", null, 1);
    SQLiteDatabase db1 = helper.getWritableDatabase();
    SQLiteDatabase db2 = helper2.getWritableDatabase();
    setupTable(db1, TABLE_NAME1);
    setupTable(db2, TABLE_NAME2);
    insertData(db1, TABLE_NAME1, new int[]{1, 2});
    insertData(db2, TABLE_NAME2, new int[]{4, 5, 6});
    verifyData(db1, TABLE_NAME1, 2);
    verifyData(db2, TABLE_NAME2, 3);
    db1.close();
    verifyData(db2, TABLE_NAME2, 3);
    db1 = helper.getWritableDatabase();
    verifyData(db1, TABLE_NAME1, 2);
    verifyData(db2, TABLE_NAME2, 3);
  }

  @Test
  public void testCreateAndDropTable() throws Exception {
    SQLiteDatabase database = helper.getWritableDatabase();
    database.execSQL("CREATE TABLE foo(id INTEGER PRIMARY KEY AUTOINCREMENT, data TEXT);");
    database.execSQL("DROP TABLE IF EXISTS foo;");
  }

  @Test
  public void testCloseThenOpen() throws Exception {
    final String TABLE_NAME1 = "fart";
    SQLiteDatabase db1 = helper.getWritableDatabase();
    setupTable(db1, TABLE_NAME1);
    insertData(db1, TABLE_NAME1, new int[]{1, 2});
    verifyData(db1, TABLE_NAME1, 2);
    db1.close();
    db1 = helper.getWritableDatabase();
    assertThat(db1.isOpen()).isTrue();
  }

  private static void assertInitialDB(SQLiteDatabase database, TestOpenHelper helper) {
    assertDatabaseOpened(database, helper);
    assertThat(helper.onCreateCalled).isTrue();
  }

  private static void assertSubsequentDB(SQLiteDatabase database, TestOpenHelper helper) {
    assertDatabaseOpened(database, helper);
    assertThat(helper.onCreateCalled).isFalse();
  }

  private static void assertDatabaseOpened(SQLiteDatabase database, TestOpenHelper helper) {
    assertThat(database).isNotNull();
    assertThat(database.isOpen()).isTrue();
    assertThat(helper.onOpenCalled).isTrue();
    assertThat(helper.onUpgradeCalled).isFalse();
  }

  private class TestOpenHelper extends SQLiteOpenHelper {
    public boolean onCreateCalled;
    public boolean onUpgradeCalled;
    public boolean onOpenCalled;

    public TestOpenHelper(Context context, String name, CursorFactory factory, int version) {
      super(context, name, factory, version);
    }

    @Override
      public void onCreate(SQLiteDatabase database) {
        onCreateCalled = true;
      }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
      onUpgradeCalled = true;
    }

    @Override
    public void onOpen(SQLiteDatabase database) {
      onOpenCalled = true;
    }

    @Override
    public synchronized void close() {
      onCreateCalled = false;
      onUpgradeCalled = false;
      onOpenCalled = false;

      super.close();
    }
  }
}
