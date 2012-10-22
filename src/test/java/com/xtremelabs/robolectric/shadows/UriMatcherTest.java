package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.UriMatcher;
import android.net.Uri;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.shadows.ShadowUriMatcher.MatchNode;

@RunWith(WithTestDefaultsRunner.class)
public class UriMatcherTest {
  static final String AUTH = "com.foo";

  UriMatcher matcher;
  MatchNode root;
  Uri URI;

  @Before
  public void getMatcher() {
    URI = Uri.parse("content://" + AUTH);
    matcher = new UriMatcher(UriMatcher.NO_MATCH);
    root = Robolectric.shadowOf(matcher).rootNode;
  }

  @Test
  public void canMatch() {
    matcher.addURI(AUTH, "bar", 1);
    assertThat(matcher.match(Uri.withAppendedPath(URI, "bar")), is(1));

    matcher.addURI(AUTH, "bar/#", 2);
    assertThat(matcher.match(Uri.withAppendedPath(URI, "bar/1")), is(2));

    matcher.addURI(AUTH, "/", 3);
    assertThat(matcher.match(Uri.withAppendedPath(URI, "/")), is(3));

    matcher.addURI(AUTH, "transport/*/#/type", 4);
    assertThat(matcher.match(Uri.withAppendedPath(URI, "transport/land/45/type")), is(4));

    matcher.addURI(AUTH, "*", 5);  
    assertThat(matcher.match(Uri.withAppendedPath(URI, "bar")), is(1));
    assertThat(matcher.match(Uri.withAppendedPath(URI, "cat")), is(5));
  }

  @Test
  public void orderOfWildcardAdditionAffectsMatching() {
    matcher.addURI(AUTH, "foo", 1);
    assertThat(matcher.match(Uri.withAppendedPath(URI, "foo")), is(1));

    matcher.addURI(AUTH, "*", 2);
    assertThat(matcher.match(Uri.withAppendedPath(URI, "foo")), is(1));

    matcher.addURI(AUTH, "bar", 3);
    assertThat(matcher.match(Uri.withAppendedPath(URI, "bar")), is(2));
  }

  @Test
  public void returnsRootCodeForIfNoMatch() {
    matcher.addURI(AUTH, "bar/#", 1);
    assertThat(matcher.match(Uri.withAppendedPath(URI, "cat")), is(UriMatcher.NO_MATCH));
    assertThat(matcher.match(Uri.withAppendedPath(URI, "bar")), is(UriMatcher.NO_MATCH));
    assertThat(matcher.match(Uri.withAppendedPath(URI, "bar/cat")), is(UriMatcher.NO_MATCH));
  }
}
