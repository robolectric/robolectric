package org.robolectric.shadows;

import android.content.ContentUris;
import android.net.Uri;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class ContentUrisTest {
  Uri URI;

  @Before
  public void setUp() throws Exception {
    URI = Uri.parse("content://foo.com");
  }

  @Test public void canAppendId() {
    assertThat(ContentUris.withAppendedId(URI, 1)).isEqualTo(Uri.parse("content://foo.com/1"));
  }

  @Test(expected = NullPointerException.class)
  public void appendIdThrowsNullPointerException() {
    ContentUris.withAppendedId(null, 1);
  }

  @Test public void canParseId() {
    assertThat(ContentUris.parseId(Uri.withAppendedPath(URI, "1"))).isEqualTo(1L);
    assertThat(ContentUris.parseId(URI)).isEqualTo(-1L);
  }

  @Test(expected = NumberFormatException.class)
  public void parseIdThrowsNumberFormatException() {
    ContentUris.parseId(Uri.withAppendedPath(URI, "bar"));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void parseIdThrowsUnsupportedException() {
    ContentUris.parseId(Uri.parse("mailto:bar@foo.com"));
  }

}
