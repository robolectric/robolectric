package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;

@RunWith(WithTestDefaultsRunner.class)
public class ContentProviderTest {

	class TestContentProvider extends ContentProvider {

		@Override
		public int delete(Uri arg0, String arg1, String[] arg2) {
			return 0;
		}

		@Override
		public String getType(Uri arg0) {
			return null;
		}

		@Override
		public Uri insert(Uri arg0, ContentValues arg1) {
			return null;
		}

		@Override
		public boolean onCreate() {
			return false;
		}

		@Override
		public Cursor query(Uri arg0, String[] arg1, String arg2,
				String[] arg3, String arg4) {
			return null;
		}

		@Override
		public int update(Uri arg0, ContentValues arg1, String arg2,
				String[] arg3) {
			return 0;
		}

	}

	TestContentProvider provider;

	@Before public void instantiateProvider() {
		provider = new TestContentProvider();
	}

	@Test public void hasAContext() {
		assertThat(provider.getContext(), is(notNullValue()));
	}

	@Test public void canGetAResolver() {
		assertThat(provider.getContext().getContentResolver(), is(notNullValue()));
	}

}
