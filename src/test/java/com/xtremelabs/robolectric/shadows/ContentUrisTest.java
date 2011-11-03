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

	@Test public void canAppendId() {
		Uri uri = Uri.parse("content://foo.com/bar");
		assertThat(ContentUris.withAppendedId(uri, 1),
				is(Uri.parse("content://foo.com/bar/1")));
	}

}
