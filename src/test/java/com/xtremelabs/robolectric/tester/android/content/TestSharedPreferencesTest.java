package com.xtremelabs.robolectric.tester.android.content;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;

/**
 * TestSharedPreferencesTest
 */
@RunWith(WithTestDefaultsRunner.class)
public class TestSharedPreferencesTest
{
  protected final static String FILENAME = "filename";

  protected Map<String, Hashtable<String, Object>> content;

  @Before
  public void setup()
  {
    content = new HashMap<String, Hashtable<String, Object>>();
  }

  @Test
  public void testConstruction()
  {
    TestSharedPreferences preferences =
      new TestSharedPreferences(content, FILENAME, Context.MODE_PRIVATE);
    assertSame("content", content, preferences.content);
    assertEquals("filename", FILENAME, preferences.filename);
    assertEquals("mode", Context.MODE_PRIVATE, preferences.mode);
    assertTrue("content.filename", content.containsKey(FILENAME));
  }

  @Test
  public void testGetAll()
  {
    Hashtable<String, Object> fileContent = new Hashtable<String, Object>();
    fileContent.put("foo", "bar");
    content.put(FILENAME, fileContent);

    TestSharedPreferences preferences =
      new TestSharedPreferences(content, FILENAME, Context.MODE_PRIVATE);

    Map<String, ?> result = preferences.getAll();
    assertNotSame("result", fileContent, result);
    assertEquals("result", fileContent, result);
  }

  @Test
  public void testEditorCommit_ClearAndSet()
  {
    Hashtable<String, Object> fileContent = new Hashtable<String, Object>();
    fileContent.put("foo", "bar");
    content.put(FILENAME, fileContent);

    SharedPreferences.Editor editor =
      new TestSharedPreferences(content, FILENAME, Context.MODE_PRIVATE).edit();

    editor.clear();
    editor.putString("alpha", "beta");
    editor.commit();

    assertEquals("content.filename.foo", false, content.get(FILENAME)
      .containsKey("foo"));
    assertEquals("content.filename.alpha", "beta",
      content.get(FILENAME).get("alpha"));
  }

  @Test
  public void testEditorCommit_RemoveAndSet()
  {
    Hashtable<String, Object> fileContent = new Hashtable<String, Object>();
    fileContent.put("foo", "bar");
    content.put(FILENAME, fileContent);

    SharedPreferences.Editor editor =
      new TestSharedPreferences(content, FILENAME, Context.MODE_PRIVATE).edit();

    editor.remove("foo");
    editor.putString("alpha", "beta");
    editor.commit();

    assertEquals("content.filename.foo", false, content.get(FILENAME)
      .containsKey("foo"));
    assertEquals("content.filename.alpha", "beta",
      content.get(FILENAME).get("alpha"));
  }

  @Test
  public void testEditorCommit_RemoveAndReset()
  {
    Hashtable<String, Object> fileContent = new Hashtable<String, Object>();
    fileContent.put("foo", "bar");
    content.put(FILENAME, fileContent);

    SharedPreferences.Editor editor =
      new TestSharedPreferences(content, FILENAME, Context.MODE_PRIVATE).edit();

    editor.remove("foo");
    editor.putString("foo", "baz");
    editor.commit();

    assertEquals("content.filename.foo", "baz", content.get(FILENAME)
      .get("foo"));
  }
}
