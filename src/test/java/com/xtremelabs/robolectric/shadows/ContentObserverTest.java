package com.xtremelabs.robolectric.shadows;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class ContentObserverTest {
	
	private TestContentObserver observer;

	@Before
	public void setUp() throws Exception {
		observer = new TestContentObserver(null);
	}

	@Test
	public void testDispatchChangeBooleanUri() {
		assertThat( observer.changed, equalTo(false) );
		assertThat( observer.selfChange, equalTo(false) );
		assertThat( observer.uri, nullValue() );
		
		Uri uri = Uri.parse("http://www.somewhere.com");
		observer.dispatchChange( true, uri );

		assertThat( observer.changed, equalTo(true) );
		assertThat( observer.selfChange, equalTo(true) );
		assertThat( observer.uri, sameInstance(uri) );	
	}

	@Test
	public void testDispatchChangeBoolean() {
		assertThat( observer.changed, equalTo(false) );
		assertThat( observer.selfChange, equalTo(false) );
		
		Uri uri = Uri.parse("http://www.somewhere.com");
		observer.dispatchChange( true );

		assertThat( observer.changed, equalTo(true) );
		assertThat( observer.selfChange, equalTo(true) );
	}

	private class TestContentObserver extends ContentObserver {
		
		public TestContentObserver(Handler handler) {
			super(handler);
		}

		public boolean changed = false;
		public boolean selfChange = false;
		public Uri uri = null;
		
		@Override
		public void onChange(boolean selfChange) {
			changed = true;
			this.selfChange = selfChange;
		}

		@Override
		public void onChange(boolean selfChange, Uri uri) {
			changed = true;
			this.selfChange = selfChange;
			this.uri = uri;
		}
	}
	
}
