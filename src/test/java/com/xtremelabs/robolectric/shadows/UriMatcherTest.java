package com.xtremelabs.robolectric.shadows;

import android.content.UriMatcher;
import android.net.Uri;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.shadows.ShadowUriMatcher.MatchNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

@RunWith(WithTestDefaultsRunner.class)
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
		assertThat(root.code, is(NO_MATCH));
		assertThat(root.map.isEmpty(), is(true));
		assertThat(root.number, is(nullValue()));
		assertThat(root.text, is(nullValue()));
	}

	@Test public void canAddBasicMatch() {
		MatchNode node = root;
		String path = "bar/cat";

		matcher.addURI(AUTH, path, 1);
		assertThat(node.map.keySet(), hasItem(AUTH));

		node = node.map.get(AUTH);
		assertThat(node.map.keySet(), hasItem("bar"));

		node = node.map.get("bar");
		assertThat(node.map.keySet(), hasItem("cat"));

		node = node.map.get("cat");
		assertThat(node.code, is(1));
	}

	@Test public void canAddWildcardMatches() {
		matcher.addURI(AUTH, "#", 1);
		matcher.addURI(AUTH, "*", 2);
		MatchNode node = root.map.get(AUTH);

		assertThat(node.number.code, is(1));
		assertThat(node.text.code, is(2));
	}

	@Test public void canMatch() {
		matcher.addURI(AUTH, "bar", 1);
		assertThat(matcher.match(Uri.withAppendedPath(URI, "bar")), is(1));

		matcher.addURI(AUTH, "bar/#", 2);
		assertThat(matcher.match(Uri.withAppendedPath(URI, "bar/1")), is(2));

		matcher.addURI(AUTH, "*", 3);
		assertThat(matcher.match(Uri.withAppendedPath(URI, "bar")), is(1));
		assertThat(matcher.match(Uri.withAppendedPath(URI, "cat")), is(3));

		matcher.addURI(AUTH, "transport/*/#/type", 4);
		assertThat(matcher.match(Uri.withAppendedPath(URI, "transport/land/45/type")), is(4));
	}

	@Test public void returnsRootCodeForIfNoMatch() {
		matcher.addURI(AUTH, "bar/#", 1);
		assertThat(matcher.match(Uri.withAppendedPath(URI, "cat")), is(NO_MATCH));
		assertThat(matcher.match(Uri.withAppendedPath(URI, "bar")), is(NO_MATCH));
		assertThat(matcher.match(Uri.withAppendedPath(URI, "bar/cat")), is(NO_MATCH));
	}

}
