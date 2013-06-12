package org.robolectric.shadows;

import android.content.UriMatcher;
import android.net.Uri;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.shadows.ShadowUriMatcher.MatchNode;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

@RunWith(TestRunners.WithDefaults.class)
public class UriMatcherTest {
  static final String AUTH = "com.foo";
  static final int NO_MATCH = -2;

  UriMatcher matcher;
  MatchNode root;
  Uri URI;

  @Before public void getMatcher() {
    URI = Uri.parse("content://" + AUTH);
    matcher = new UriMatcher(NO_MATCH);
    root = Robolectric.shadowOf(matcher).rootNode;
  }

  @Test public void canInstantiate() {
    assertThat(root.code).isEqualTo(NO_MATCH);
    assertThat(root.map.isEmpty()).isTrue();
    assertThat(root.number).isNull();
    assertThat(root.text).isNull();
  }

  @Test public void canAddBasicMatch() {
    MatchNode node = root;
    String path = "bar/cat";

    matcher.addURI(AUTH, path, 1);
    assertThat(node.map.keySet()).contains(AUTH);

    node = node.map.get(AUTH);
    assertThat(node.map.keySet()).contains("bar");

    node = node.map.get("bar");
    assertThat(node.map.keySet()).contains("cat");

    node = node.map.get("cat");
    assertThat(node.code).isEqualTo(1);
  }

  @Test public void canAddWildcardMatches() {
    matcher.addURI(AUTH, "#", 1);
    matcher.addURI(AUTH, "*", 2);
    MatchNode node = root.map.get(AUTH);

    assertThat(node.number.code).isEqualTo(1);
    assertThat(node.text.code).isEqualTo(2);
  }

  @Test public void canMatch() {
    matcher.addURI(AUTH, "bar", 1);
    assertThat(matcher.match(Uri.withAppendedPath(URI, "bar"))).isEqualTo(1);

    matcher.addURI(AUTH, "bar/#", 2);
    assertThat(matcher.match(Uri.withAppendedPath(URI, "bar/1"))).isEqualTo(2);

    matcher.addURI(AUTH, "*", 3);
    assertThat(matcher.match(Uri.withAppendedPath(URI, "bar"))).isEqualTo(1);
    assertThat(matcher.match(Uri.withAppendedPath(URI, "cat"))).isEqualTo(3);

    matcher.addURI(AUTH, "transport/*/#/type", 4);
    assertThat(matcher.match(Uri.withAppendedPath(URI, "transport/land/45/type"))).isEqualTo(4);
  }

  @Test public void returnsRootCodeForIfNoMatch() {
    matcher.addURI(AUTH, "bar/#", 1);
    assertThat(matcher.match(Uri.withAppendedPath(URI, "cat"))).isEqualTo(NO_MATCH);
    assertThat(matcher.match(Uri.withAppendedPath(URI, "bar"))).isEqualTo(NO_MATCH);
    assertThat(matcher.match(Uri.withAppendedPath(URI, "bar/cat"))).isEqualTo(NO_MATCH);
  }

}
