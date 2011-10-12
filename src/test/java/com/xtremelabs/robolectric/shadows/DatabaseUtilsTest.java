package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;

@RunWith(WithTestDefaultsRunner.class)
public class DatabaseUtilsTest {

	@Test
	public void testQuote() {
		assertThat( ShadowDatabaseUtils.sqlEscapeString( "foobar" ), equalTo( "'foobar'" ) );
		assertThat( ShadowDatabaseUtils.sqlEscapeString( "Rich's" ), equalTo( "'Rich''s'" ) );
	}
}
