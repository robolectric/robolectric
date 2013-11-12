package org.robolectric.shadows;

import android.content.ContentUris;
import android.content.UriMatcher;
import android.net.Uri;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static android.content.UriMatcher.NO_MATCH;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class UriMatcherTest {
  static final String AUTH = "com.foo";

  UriMatcher matcher;
  Uri URI;

  @Before public void getMatcher() {
    URI = Uri.parse("content://" + AUTH);
    matcher = new UriMatcher(NO_MATCH);
  }

  @Test public void canAddBasicMatch() {
    String path = "bar/cat";
    matcher.addURI(AUTH, path, 1);
    assertThat(matcher.match(Uri.withAppendedPath(URI, path))).isEqualTo(1);

    assertThat(matcher.match(URI)).isEqualTo(NO_MATCH);
    assertThat(matcher.match(Uri.withAppendedPath(URI, "bar"))).isEqualTo(NO_MATCH);
  }

  @Test public void canAddWildcardMatches() {
    matcher.addURI(AUTH, "#", 1);
    matcher.addURI(AUTH, "*", 2);
    assertThat(matcher.match(ContentUris.withAppendedId(URI, 2))).isEqualTo(1);
    assertThat(matcher.match(ContentUris.withAppendedId(URI, 3))).isEqualTo(1);
    assertThat(matcher.match(Uri.withAppendedPath(URI, "any"))).isEqualTo(2);
  }

  @Test public void canMatch() {
    matcher.addURI(AUTH, "bar", 1);
    assertThat(matcher.match(Uri.withAppendedPath(URI, "bar"))).isEqualTo(1);

    matcher.addURI(AUTH, "bar/#", 2);
    assertThat(matcher.match(Uri.withAppendedPath(URI, "bar/1"))).isEqualTo(2);

    matcher.addURI(AUTH, "transport/*/#/type", 3);
    assertThat(matcher.match(Uri.withAppendedPath(URI, "transport/land/45/type"))).isEqualTo(3);

    matcher.addURI(AUTH, "*", 4);
    assertThat(matcher.match(Uri.withAppendedPath(URI, "bar"))).isEqualTo(1);
    assertThat(matcher.match(Uri.withAppendedPath(URI, "cat"))).isEqualTo(4);
  }

  @Test public void returnsRootCodeForIfNoMatch() {
    matcher.addURI(AUTH, "bar/#", 1);
    assertThat(matcher.match(Uri.withAppendedPath(URI, "cat"))).isEqualTo(NO_MATCH);
    assertThat(matcher.match(Uri.withAppendedPath(URI, "bar"))).isEqualTo(NO_MATCH);
    assertThat(matcher.match(Uri.withAppendedPath(URI, "bar/cat"))).isEqualTo(NO_MATCH);
  }

}
