package org.robolectric.shadows;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Collections;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import android.content.ContentProviderOperation;
import android.net.Uri;

/**
 * Tests for {@link ShadowContentProviderOperation}.
 */
@RunWith(TestRunners.WithDefaults.class)
public class ContentProviderOperationTest {

  @Test
  public void reflectionShouldWork() {
    final Uri uri = Uri.parse("content://authority/path");

    ContentProviderOperation op = ContentProviderOperation.newInsert(uri)
        .withValue("insertKey", "insertValue")
        .withValueBackReference("backKey", 2)
        .build();

    // insert and values back references
    assertThat(op.getUri()).isEqualTo(uri);
    ShadowContentProviderOperation shadow = Robolectric.shadowOf(op);
    assertThat(shadow.getType()).isEqualTo(ShadowContentProviderOperation.TYPE_INSERT);
    assertThat(shadow.getContentValues().getAsString("insertKey")).isEqualTo("insertValue");
    assertThat(shadow.getValuesBackReferences().getAsInteger("backKey")).isEqualTo(2);

    // update and selection back references
    op = ContentProviderOperation.newUpdate(uri)
        .withValue("updateKey", "updateValue")
        .withSelection("a=? and b=?", new String[] {"abc"})
        .withSelectionBackReference(1, 3)
        .build();
    assertThat(op.getUri()).isEqualTo(uri);
    shadow = Robolectric.shadowOf(op);
    assertThat(shadow.getType()).isEqualTo(ShadowContentProviderOperation.TYPE_UPDATE);
    assertThat(shadow.getContentValues().getAsString("updateKey")).isEqualTo("updateValue");
    assertThat(shadow.getSelection()).isEqualTo("a=? and b=?");
    assertThat(shadow.getSelectionArgs()).containsExactly("abc");
    assertThat(shadow.getSelectionArgsBackReferences()).isEqualTo(Collections.<Integer, Integer>singletonMap(1, 3));

    // delete and expected count
    op = ContentProviderOperation.newDelete(uri)
        .withExpectedCount(1)
        .build();
    assertThat(op.getUri()).isEqualTo(uri);
    shadow = Robolectric.shadowOf(op);
    assertThat(shadow.getType()).isEqualTo(ShadowContentProviderOperation.TYPE_DELETE);
    assertThat(shadow.getExpectedCount()).isEqualTo(1);
  }

}
