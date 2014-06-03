package org.robolectric.shadows;

import android.database.AbstractCursor;
import android.net.Uri;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import java.util.ArrayList;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class AbstractCursorTest {

  private TestCursor cursor;

  @Before
  public void setUp() throws Exception {
    cursor = new TestCursor();
  }

  @Test
  public void testMoveToFirst() {
    cursor.theTable.add("Foobar");
    assertThat(cursor.moveToFirst()).isTrue();
    assertThat(cursor.getCount()).isEqualTo(1);
  }

  @Test
  public void testMoveToFirstEmptyList() {
    assertThat(cursor.moveToFirst()).isFalse();
    assertThat(cursor.getCount()).isEqualTo(0);
  }

  @Test
  public void testMoveToLast() {
    cursor.theTable.add("Foobar");
    cursor.theTable.add("Bletch");

    assertThat(cursor.moveToLast()).isTrue();
    assertThat(cursor.getCount()).isEqualTo(2);
  }

  @Test
  public void testMoveToLastEmptyList() {
    assertThat(cursor.moveToLast()).isFalse();
    assertThat(cursor.getCount()).isEqualTo(0);
  }

  @Test
  public void testGetPosition() {
    cursor.theTable.add("Foobar");
    cursor.theTable.add("Bletch");

    assertThat(cursor.moveToFirst()).isTrue();
    assertThat(cursor.getCount()).isEqualTo(2);
    assertThat(cursor.getPosition()).isEqualTo(0);
  }

  @Test
  public void testGetPositionSingleEntry() {
    cursor.theTable.add("Foobar");

    assertThat(cursor.moveToFirst()).isTrue();
    assertThat(cursor.getCount()).isEqualTo(1);
    assertThat(cursor.getPosition()).isEqualTo(0);
  }

  @Test
  public void testGetPositionEmptyList() {
    assertThat(cursor.moveToFirst()).isFalse();
    assertThat(cursor.getCount()).isEqualTo(0);
    assertThat(cursor.getPosition()).isEqualTo(0);
  }

  @Test
  public void testMoveToNext() {
    cursor.theTable.add("Foobar");
    cursor.theTable.add("Bletch");

    assertThat(cursor.moveToFirst()).isTrue();
    assertThat(cursor.getCount()).isEqualTo(2);
    assertThat(cursor.moveToNext()).isTrue();
    assertThat(cursor.getPosition()).isEqualTo(1);
  }

  @Test
  public void testAttemptToMovePastEnd() {
    cursor.theTable.add("Foobar");
    cursor.theTable.add("Bletch");

    assertThat(cursor.moveToFirst()).isTrue();
    assertThat(cursor.getCount()).isEqualTo(2);
    assertThat(cursor.moveToNext()).isTrue();
    assertThat(cursor.getPosition()).isEqualTo(1);
    assertThat(cursor.isLast()).isTrue();
    assertThat(cursor.moveToNext()).isFalse();
    assertThat(cursor.isAfterLast()).isTrue();
    assertThat(cursor.getPosition()).isEqualTo(2);
  }

  @Test
  public void testAttemptToMovePastSingleEntry() {
    cursor.theTable.add("Foobar");

    assertThat(cursor.moveToFirst()).isTrue();
    assertThat(cursor.getCount()).isEqualTo(1);
    assertThat(cursor.moveToNext()).isFalse();
    assertThat(cursor.getPosition()).isEqualTo(1);
  }

  @Test
  public void testAttemptToMovePastEmptyList() {
    assertThat(cursor.moveToFirst()).isFalse();
    assertThat(cursor.getCount()).isEqualTo(0);
    assertThat(cursor.moveToNext()).isFalse();
    assertThat(cursor.getPosition()).isEqualTo(0);
  }

  @Test
  public void testMoveToPrevious() {
    cursor.theTable.add("Foobar");
    cursor.theTable.add("Bletch");
    assertThat(cursor.moveToFirst()).isTrue();
    assertThat(cursor.moveToNext()).isTrue();
    assertThat(cursor.getPosition()).isEqualTo(1);
    assertThat(cursor.moveToPrevious()).isTrue();
    assertThat(cursor.getPosition()).isEqualTo(0);
  }

  @Test
  public void testAttemptToMovePastStart() {
    cursor.theTable.add("Foobar");
    cursor.theTable.add("Bletch");
    assertThat(cursor.moveToFirst()).isTrue();
    assertThat(cursor.moveToPrevious()).isFalse();
    assertThat(cursor.getPosition()).isEqualTo(-1);
  }

  @Test
  public void testIsFirst() {
    cursor.theTable.add("Foobar");
    cursor.theTable.add("Bletch");
    assertThat(cursor.moveToFirst()).isTrue();
    assertThat(cursor.isFirst()).isTrue();
    cursor.moveToNext();
    assertThat(cursor.isFirst()).isFalse();
    cursor.moveToFirst();
    cursor.moveToPrevious();
    assertThat(cursor.isFirst()).isFalse();
  }

  @Test
  public void testIsLast() {
    cursor.theTable.add("Foobar");
    cursor.theTable.add("Bletch");
    assertThat(cursor.moveToFirst()).isTrue();
    cursor.moveToNext();
    assertThat(cursor.isLast()).isTrue();
    cursor.moveToPrevious();
    assertThat(cursor.isLast()).isFalse();
    cursor.moveToFirst();
    cursor.moveToNext();
    assertThat(cursor.isLast()).isTrue();
  }

  @Test
  public void testIsBeforeFirst() {
    cursor.theTable.add("Foobar");
    cursor.theTable.add("Bletch");
    assertThat(cursor.moveToFirst()).isTrue();
    cursor.moveToNext();
    assertThat(cursor.isLast()).isTrue();
    cursor.moveToPrevious();
    assertThat(cursor.isLast()).isFalse();
    cursor.moveToPrevious();
    assertThat(cursor.isFirst()).isFalse();
    cursor.moveToPrevious();
    assertThat(cursor.isBeforeFirst()).isTrue();
  }

  @Test
  public void testIsAfterLast() {
    cursor.theTable.add("Foobar");
    cursor.theTable.add("Bletch");
    assertThat(cursor.moveToFirst()).isTrue();
    cursor.moveToNext();
    assertThat(cursor.isLast()).isTrue();
    cursor.moveToNext();
    assertThat(cursor.isAfterLast()).isTrue();
    cursor.moveToPrevious();
    assertThat(cursor.isLast()).isTrue();
    cursor.moveToPrevious();
    assertThat(cursor.isLast()).isFalse();
    cursor.moveToFirst();
    cursor.moveToNext();
    assertThat(cursor.isAfterLast()).isFalse();
    cursor.moveToNext();
    assertThat(cursor.isAfterLast()).isTrue();
  }

  @Test
  public void testGetNotificationUri() {
    Uri uri = Uri.parse("content://foo.com");
    ShadowAbstractCursor shadow = Robolectric.shadowOf_(cursor);
    assertThat(shadow.getNotificationUri_Compatibility()).isNull();
    cursor.setNotificationUri(Robolectric.application.getContentResolver(), uri);
    assertThat(shadow.getNotificationUri_Compatibility()).isEqualTo(uri);
  }

  @Test
  public void testIsClosedWhenAfterCallingClose() {
    assertThat(cursor.isClosed()).isFalse();
    cursor.close();
    assertThat(cursor.isClosed()).isTrue();
  }

  private class TestCursor extends AbstractCursor {

    public List<Object> theTable = new ArrayList<Object>();

    @Override
    public int getCount() {
      return theTable.size();
    }

    @Override
    public String[] getColumnNames() {
      throw new UnsupportedOperationException();
    }

    @Override
    public double getDouble(int columnIndex) {
      throw new UnsupportedOperationException();
    }

    @Override
    public float getFloat(int columnIndex) {
      throw new UnsupportedOperationException();
    }

    @Override
    public int getInt(int columnIndex) {
      throw new UnsupportedOperationException();
    }

    @Override
    public long getLong(int columnIndex) {
      throw new UnsupportedOperationException();
    }

    @Override
    public short getShort(int columnIndex) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getString(int columnIndex) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean isNull(int columnIndex) {
      throw new UnsupportedOperationException();
    }
  }
}