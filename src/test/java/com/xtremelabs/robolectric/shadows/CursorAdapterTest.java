package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.util.DatabaseConfig;

@RunWith(WithTestDefaultsRunner.class)
public class CursorAdapterTest {

	private Cursor curs;
	private CursorAdapter adapter;

	@Before
	public void setUp() throws Exception {
		Connection connection = DatabaseConfig.getMemoryConnection();

        Statement statement = connection.createStatement();
        statement.execute("CREATE TABLE table_name(_id INT PRIMARY KEY, name VARCHAR(255));" );
        String[] inserts = {
                "INSERT INTO table_name (_id, name) VALUES(1234, 'Chuck');",
                "INSERT INTO table_name (_id, name) VALUES(1235, 'Julie');",
                "INSERT INTO table_name (_id, name) VALUES(1236, 'Chris');",
                "INSERT INTO table_name (_id, name) VALUES(1237, 'Brenda');",
                "INSERT INTO table_name (_id, name) VALUES(1238, 'Jane');"
        };

        for (String insert : inserts) {
            connection.createStatement().executeUpdate(insert);
        }

        statement = connection.createStatement(DatabaseConfig.getResultSetType(), ResultSet.CONCUR_READ_ONLY);
        String sql = "SELECT * FROM table_name;";
        ResultSet resultSet = statement.executeQuery(sql);
        curs = new SQLiteCursor(null, null, null, null);
        Robolectric.shadowOf((SQLiteCursor)curs).setResultSet(resultSet,sql);

		adapter = new TestAdapter(curs);
	}

	@Test
	public void testChangeCursor() {
		assertThat(adapter.getCursor(), notNullValue());
		assertThat(adapter.getCursor(), sameInstance(curs));

		adapter.changeCursor( null );

		assertThat(curs.isClosed(), equalTo( true ) );
		assertThat(adapter.getCursor(), nullValue() );
	}

	@Test
	public void testCount() {
		assertThat(adapter.getCount(), equalTo(curs.getCount()));
		adapter.changeCursor( null );
		assertThat(adapter.getCount(), equalTo(0) );
	}

	@Test
	public void testGetItemId() {
		for ( int i = 0; i < 5; i++ ) {
			assertThat(adapter.getItemId(i), equalTo((long) 1234 + i));
		}
	}

	@Test
	public void testGetView() {
		List<View> views = new ArrayList<View>();
		for (int i = 0; i < 5; i++) {
			views.add(new View(Robolectric.application));
		}

		Robolectric.shadowOf(adapter).setViews( views );

		for (int i = 0; i < 5; i++) {
			assertThat(adapter.getView(i, null, null), sameInstance(views.get(i)));
		}
	}

	private class TestAdapter extends CursorAdapter {

		public TestAdapter( Cursor curs ) {
			super( Robolectric.application, curs, false );
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) { }

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return null;
		}
	}
}
