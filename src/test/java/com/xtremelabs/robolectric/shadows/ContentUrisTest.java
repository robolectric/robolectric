package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.ContentUris;
import android.net.Uri;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;

@RunWith(WithTestDefaultsRunner.class)
public class ContentUrisTest {
	static final Uri URI = Uri.parse("content://foo.com");

	@Test public void canAppendId() {
		assertThat(ContentUris.withAppendedId(URI, 1),
				is(Uri.parse("content://foo.com/1")));
	}

	@Test(expected=NullPointerException.class)
	public void appendIdThrowsNullPointerException() {
		ContentUris.withAppendedId(null, 1);
	}

	@Test public void canParseId() {
		assertThat(ContentUris.parseId(Uri.withAppendedPath(URI, "1")), is(1L));
		assertThat(ContentUris.parseId(URI), is(-1L));
	}

	@Test(expected=NumberFormatException.class)
	public void parseIdThrowsNumberFormatException() {
		ContentUris.parseId(Uri.withAppendedPath(URI, "bar"));
	}

	@Test(expected=UnsupportedOperationException.class)
	public void parseIdThrowsUnsupportedException() {
		ContentUris.parseId(Uri.parse("mailto:bar@foo.com"));
	}

}
