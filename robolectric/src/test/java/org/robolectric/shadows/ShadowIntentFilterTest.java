package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.content.IntentFilter;
import android.net.Uri;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowIntentFilterTest {
  @Test
  public void copyConstructorTest() throws Exception {
    String action = "test";
    IntentFilter intentFilter = new IntentFilter(action);
    IntentFilter copy = new IntentFilter(intentFilter);
    assertThat(copy.hasAction("test")).isTrue();
  }

  @Test
  public void setsPriority() throws Exception {
    IntentFilter filter = new IntentFilter();
    filter.setPriority(123);
    assertThat(filter.getPriority()).isEqualTo(123);
  }

  @Test
  public void addDataScheme_shouldAddTheDataScheme() throws Exception {
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addDataScheme("http");
    intentFilter.addDataScheme("ftp");

    assertThat(intentFilter.getDataScheme(0)).isEqualTo("http");
    assertThat(intentFilter.getDataScheme(1)).isEqualTo("ftp");
  }

  @Test
  public void addDataAuthority_shouldAddTheDataAuthority() throws Exception {
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addDataAuthority("test.com", "8080");
    intentFilter.addDataAuthority("example.com", "42");

    assertThat(intentFilter.getDataAuthority(0).getHost()).isEqualTo("test.com");
    assertThat(intentFilter.getDataAuthority(0).getPort()).isEqualTo(8080);
    assertThat(intentFilter.getDataAuthority(1).getHost()).isEqualTo("example.com");
    assertThat(intentFilter.getDataAuthority(1).getPort()).isEqualTo(42);
  }

  @Test
  public void addDataType_shouldAddTheDataType() throws Exception {
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addDataType("image/test");

    assertThat(intentFilter.getDataType(0)).isEqualTo("image/test");
  }

  @Test
  public void hasAction() {
    IntentFilter intentFilter = new IntentFilter();
    assertThat(intentFilter.hasAction("test")).isFalse();
    intentFilter.addAction("test");

    assertThat(intentFilter.hasAction("test")).isTrue();
  }

  @Test
  public void hasDataScheme() {
    IntentFilter intentFilter = new IntentFilter();
    assertThat(intentFilter.hasDataScheme("test")).isFalse();
    intentFilter.addDataScheme("test");

    assertThat(intentFilter.hasDataScheme("test")).isTrue();
  }

  @Test
  public void hasDataType() throws IntentFilter.MalformedMimeTypeException{
    IntentFilter intentFilter = new IntentFilter();
    assertThat(intentFilter.hasDataType("image/test")).isFalse();
    intentFilter.addDataType("image/test");

    assertThat(intentFilter.hasDataType("image/test")).isTrue();
  }

  @Test
  public void matchDataAuthority_matchHostAndPort() {
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addDataAuthority("testHost1", "1");
    intentFilter.addDataAuthority("testHost2", "2");

    Uri uriTest1 = Uri.parse("http://testHost1:1");
    Uri uriTest2 = Uri.parse("http://testHost2:2");
    assertThat(intentFilter.matchDataAuthority(uriTest1)).isEqualTo(IntentFilter.MATCH_CATEGORY_PORT);
    assertThat(intentFilter.matchDataAuthority(uriTest2)).isEqualTo(IntentFilter.MATCH_CATEGORY_PORT);
  }

  @Test
  public void matchDataAuthority_matchHostWithNoPort() {
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addDataAuthority("testHost1", "-1");
    intentFilter.addDataAuthority("testHost2", "-1");

    Uri uriTest1 = Uri.parse("http://testHost1:100");
    Uri uriTest2 = Uri.parse("http://testHost2:200");
    assertThat(intentFilter.matchDataAuthority(uriTest1)).isEqualTo(IntentFilter.MATCH_CATEGORY_HOST);
    assertThat(intentFilter.matchDataAuthority(uriTest2)).isEqualTo(IntentFilter.MATCH_CATEGORY_HOST);
  }

  @Test
  public void matchDataAuthority_NoMatch() {
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addDataAuthority("testHost1", "1");
    intentFilter.addDataAuthority("testHost2", "2");

    // Port doesn't match
    Uri uriTest1 = Uri.parse("http://testHost1:2");
    // Host doesn't match
    Uri uriTest2 = Uri.parse("http://testHost3:2");
    assertThat(intentFilter.matchDataAuthority(uriTest1)).isEqualTo(
        IntentFilter.NO_MATCH_DATA);
    assertThat(intentFilter.matchDataAuthority(uriTest2)).isEqualTo(
        IntentFilter.NO_MATCH_DATA);
  }

  @Test
  public void matchData_MatchAll() throws IntentFilter.MalformedMimeTypeException{
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addDataType("image/test");
    intentFilter.addDataScheme("http");
    intentFilter.addDataAuthority("testHost1", "1");

    Uri uriTest1 = Uri.parse("http://testHost1:1");
    assertThat(intentFilter.matchData("image/test", "http", uriTest1))
        .isAtLeast(0);
  }

  @Test
  public void matchData_MatchType() throws IntentFilter.MalformedMimeTypeException {
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addDataType("image/test");
    intentFilter.addDataScheme("http");

    Uri uriTest1 = Uri.parse("http://testHost1:1");
    assertThat(intentFilter.matchData("image/test", "http", uriTest1))
        .isAtLeast(0);
  }

  @Test
  public void matchData_MatchScheme() throws IntentFilter.MalformedMimeTypeException {
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addDataScheme("http");

    Uri uriTest1 = Uri.parse("http://testHost1:1");
    assertThat(intentFilter.matchData(null, "http", uriTest1))
        .isAtLeast(0);
  }

  @Test
  public void matchData_MatchEmpty() throws IntentFilter.MalformedMimeTypeException {
    IntentFilter intentFilter = new IntentFilter();

    assertThat(intentFilter.matchData(null, "noscheme", null))
        .isAtLeast(0);
  }

  @Test
  public void matchData_NoMatchType() throws IntentFilter.MalformedMimeTypeException {
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addDataType("image/testFail");

    Uri uriTest1 = Uri.parse("http://testHost1:1");
    assertThat(intentFilter.matchData("image/test", "http", uriTest1))
        .isLessThan(0);
  }

  @Test
  public void matchData_NoMatchScheme() throws IntentFilter.MalformedMimeTypeException {
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addDataScheme("http");
    intentFilter.addDataType("image/test");

    Uri uriTest1 = Uri.parse("https://testHost1:1");
    assertThat(intentFilter.matchData("image/test", "https", uriTest1))
        .isLessThan(0);
  }

  @Test
  public void matchData_NoMatchDataAuthority() throws IntentFilter.MalformedMimeTypeException {
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addDataType("image/test");
    intentFilter.addDataScheme("http");
    intentFilter.addDataAuthority("testHost1", "1");

    Uri uriTest1 = Uri.parse("http://testHost1:2");
    assertThat(intentFilter.matchData("image/test", "http", uriTest1))
        .isLessThan(0);
  }

  @Test
  public void matchData_MatchSchemeNoMatchType() throws IntentFilter.MalformedMimeTypeException {
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addDataScheme("http");
    intentFilter.addDataType("image/testFail");

    Uri uriTest1 = Uri.parse("http://testHost1:1");
    assertThat(intentFilter.matchData("image/test", "http", uriTest1))
        .isLessThan(0);
  }

  @Test
  public void matchData_MatchesPartialType() throws IntentFilter.MalformedMimeTypeException {
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addDataScheme("content");
    intentFilter.addDataType("image/*");

    Uri uri = Uri.parse("content://authority/images");
    assertThat(intentFilter.matchData("image/test", "content", uri)).isAtLeast(0);
    assertThat(intentFilter.matchData("video/test", "content", uri)).isLessThan(0);
  }

  @Test
  public void matchData_MatchesAnyTypeAndSubtype() throws IntentFilter.MalformedMimeTypeException {
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addDataScheme("content");
    intentFilter.addDataType("*/*");

    Uri uri = Uri.parse("content://authority/images");
    assertThat(intentFilter.matchData("image/test", "content", uri)).isAtLeast(0);
    assertThat(intentFilter.matchData("image/*", "content", uri)).isAtLeast(0);
    assertThat(intentFilter.matchData("video/test", "content", uri)).isAtLeast(0);
    assertThat(intentFilter.matchData("video/*", "content", uri)).isAtLeast(0);
  }

  @Test
  public void testCountDataTypes() throws Exception {
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addDataType("image/*");
    intentFilter.addDataType("audio/*");
    assertThat(intentFilter.countDataTypes()).isEqualTo(2);
  }
}
