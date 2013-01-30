package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import com.xtremelabs.robolectric.TestRunners;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(TestRunners.WithDefaults.class)
public class DatabaseUtilsTest {

	@Test
	public void testQuote() {
		assertThat( ShadowDatabaseUtils.sqlEscapeString( "foobar" ), equalTo( "'foobar'" ) );
		assertThat( ShadowDatabaseUtils.sqlEscapeString( "Rich's" ), equalTo( "'Rich''s'" ) );
	}
}
