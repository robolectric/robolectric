package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(WithTestDefaultsRunner.class)
public class DatabaseUtilsTest {

	@Test
	public void testQuote() {
		assertThat( ShadowDatabaseUtils.sqlEscapeString( "foobar" ), equalTo( "'foobar'" ) );
		assertThat( ShadowDatabaseUtils.sqlEscapeString( "Rich's" ), equalTo( "'Rich''s'" ) );
	}
	
	@Test
	public void testQuoteWithBuilder() {
		StringBuilder builder = new StringBuilder();
		ShadowDatabaseUtils.appendEscapedSQLString( builder , "foobar" );
		assertThat( builder.toString(), equalTo( "'foobar'" ) );
		
		builder = new StringBuilder();
		ShadowDatabaseUtils.appendEscapedSQLString( builder, "Rich's" );
		assertThat( builder.toString(), equalTo( "'Rich''s'" ) );
	}
}
