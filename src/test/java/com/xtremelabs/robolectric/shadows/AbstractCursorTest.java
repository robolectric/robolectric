package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.database.AbstractCursor;
import android.net.Uri;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

@RunWith(WithTestDefaultsRunner.class)
public class AbstractCursorTest {

    private TestCursor cursor;

    @Before
    public void setUp() throws Exception {
        cursor = new TestCursor();
    }

    @Test
    public void testMoveToFirst() {
        cursor.theTable.add("Foobar");
        assertThat(cursor.moveToFirst(), equalTo(true));
        assertThat(cursor.getCount(), equalTo(1));
    }

    @Test
    public void testMoveToFirstEmptyList() {
        assertThat(cursor.moveToFirst(), equalTo(false));
        assertThat(cursor.getCount(), equalTo(0));
    }
    
    @Test
    public void testMoveToLast() {
        cursor.theTable.add("Foobar");
        cursor.theTable.add("Bletch");
    	
        assertThat(cursor.moveToLast(), equalTo(true));
        assertThat(cursor.getCount(), equalTo(2));
    }

    @Test
    public void testMoveToLastEmptyList() {
        assertThat(cursor.moveToLast(), equalTo(false));
        assertThat(cursor.getCount(), equalTo(0));
    }
    
    @Test
    public void testGetPosition() {
        cursor.theTable.add("Foobar");
        cursor.theTable.add("Bletch");

        assertThat(cursor.moveToFirst(), equalTo(true));
        assertThat(cursor.getCount(), equalTo(2));
        assertThat(cursor.getPosition(), equalTo(0));
    }

    @Test
    public void testGetPositionSingleEntry() {
        cursor.theTable.add("Foobar");

        assertThat(cursor.moveToFirst(), equalTo(true));
        assertThat(cursor.getCount(), equalTo(1));
        assertThat(cursor.getPosition(), equalTo(0));
    }

    @Test
    public void testGetPositionEmptyList() {
        assertThat(cursor.moveToFirst(), equalTo(false));
        assertThat(cursor.getCount(), equalTo(0));
        assertThat(cursor.getPosition(), equalTo(0));
    }

    @Test
    public void testMoveToNext() {
        cursor.theTable.add("Foobar");
        cursor.theTable.add("Bletch");

        assertThat(cursor.moveToFirst(), equalTo(true));
        assertThat(cursor.getCount(), equalTo(2));
        assertThat(cursor.moveToNext(), equalTo(true));
        assertThat(cursor.getPosition(), equalTo(1));
    }

    @Test
    public void testAttemptToMovePastEnd() {
        cursor.theTable.add("Foobar");
        cursor.theTable.add("Bletch");

        assertThat(cursor.moveToFirst(), equalTo(true));
        assertThat(cursor.getCount(), equalTo(2));
        assertThat(cursor.moveToNext(), equalTo(true));
        assertThat(cursor.getPosition(), equalTo(1));
        assertThat(cursor.isLast(), equalTo(true));
        assertThat(cursor.moveToNext(), equalTo(false));
        assertThat(cursor.isAfterLast(), equalTo(true));
        assertThat(cursor.getPosition(), equalTo(2));
    }

    @Test
    public void testAttemptToMovePastSingleEntry() {
        cursor.theTable.add("Foobar");

        assertThat(cursor.moveToFirst(), equalTo(true));
        assertThat(cursor.getCount(), equalTo(1));
        assertThat(cursor.moveToNext(), equalTo(false));
        assertThat(cursor.getPosition(), equalTo(1));
    }

    @Test
    public void testAttemptToMovePastEmptyList() {
        assertThat(cursor.moveToFirst(), equalTo(false));
        assertThat(cursor.getCount(), equalTo(0));
        assertThat(cursor.moveToNext(), equalTo(false));
        assertThat(cursor.getPosition(), equalTo(0));
    }
    
    @Test
    public void testMoveToPrevious() {
        cursor.theTable.add("Foobar");
        cursor.theTable.add("Bletch");
        assertThat(cursor.moveToFirst(), equalTo(true));
        assertThat(cursor.moveToNext(), equalTo(true));
        assertThat(cursor.getPosition(), equalTo(1));
        assertThat(cursor.moveToPrevious(), equalTo(true));
        assertThat(cursor.getPosition(), equalTo(0));
    }
    
    @Test
    public void testAttemptToMovePastStart() {
        cursor.theTable.add("Foobar");
        cursor.theTable.add("Bletch");
        assertThat(cursor.moveToFirst(), equalTo(true));
        assertThat(cursor.moveToPrevious(), equalTo(true));
        assertThat(cursor.getPosition(), equalTo(-1));
        assertThat(cursor.moveToPrevious(), equalTo(false));
        assertThat(cursor.getPosition(), equalTo(-1));
    }

    @Test
    public void testIsFirst() {
        cursor.theTable.add("Foobar");
        cursor.theTable.add("Bletch");
        assertThat(cursor.moveToFirst(), equalTo(true));
        assertThat(cursor.isFirst(), equalTo(true));
        cursor.moveToNext();
        assertThat(cursor.isFirst(), equalTo(false));     
        cursor.moveToFirst();
        cursor.moveToPrevious();
        assertThat(cursor.isFirst(), equalTo(false));
    }

    @Test
    public void testIsLast() {
        cursor.theTable.add("Foobar");
        cursor.theTable.add("Bletch");
        assertThat(cursor.moveToFirst(), equalTo(true));
        cursor.moveToNext();
        assertThat(cursor.isLast(), equalTo(true));
        cursor.moveToPrevious();
        assertThat(cursor.isLast(), equalTo(false));     
        cursor.moveToFirst();
        cursor.moveToNext();
        assertThat(cursor.isLast(), equalTo(true));   	
    }
    
    @Test
    public void testIsBeforeFirst() {
        cursor.theTable.add("Foobar");
        cursor.theTable.add("Bletch");
        assertThat(cursor.moveToFirst(), equalTo(true));
        cursor.moveToNext();
        assertThat(cursor.isLast(), equalTo(true));
        cursor.moveToPrevious();
        assertThat(cursor.isLast(), equalTo(false));     
        cursor.moveToPrevious();
        assertThat(cursor.isFirst(), equalTo(false));
        cursor.moveToPrevious();
        assertThat(cursor.isBeforeFirst(), equalTo(true));   	
    }
    
    @Test
    public void testIsAfterLast() {
        cursor.theTable.add("Foobar");
        cursor.theTable.add("Bletch");
        assertThat(cursor.moveToFirst(), equalTo(true));
        cursor.moveToNext();
        assertThat(cursor.isLast(), equalTo(true));
        cursor.moveToNext();
        assertThat(cursor.isAfterLast(), equalTo(true));
        cursor.moveToPrevious();
        assertThat(cursor.isLast(), equalTo(true));
        cursor.moveToPrevious();
        assertThat(cursor.isLast(), equalTo(false));
        cursor.moveToFirst();
        cursor.moveToNext();
        assertThat(cursor.isAfterLast(), equalTo(false));
        cursor.moveToNext();
        assertThat(cursor.isAfterLast(), equalTo(true));    	
    }

    @Test
    public void testGetNotificationUri() {
        Uri uri = Uri.parse("content://foo.com");
        ShadowAbstractCursor shadow = Robolectric.shadowOf_(cursor);
        assertThat(shadow.getNotificationUri_Compatibility(), is(nullValue()));
        cursor.setNotificationUri(null, uri);
        assertThat(shadow.getNotificationUri_Compatibility(), is(uri));
    }

	@Test
	public void testIsClosedWhenAfterCallingClose() {
		assertThat(cursor.isClosed(), equalTo(false));
		cursor.close();
		assertThat(cursor.isClosed(), equalTo(true));
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