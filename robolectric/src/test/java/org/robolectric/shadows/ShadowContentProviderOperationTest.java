package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.content.ContentProviderOperation;
import android.net.Uri;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;

/** Tests for {@link ShadowContentProviderOperation}. */
@RunWith(AndroidJUnit4.class)
public class ShadowContentProviderOperationTest {

  @Test
  public void reflectionShouldWork() {
    final Uri uri = Uri.parse("content://authority/path");

    ContentProviderOperation op = ContentProviderOperation.newInsert(uri)
        .withValue("insertKey", "insertValue")
        .withValueBackReference("backKey", 2)
        .build();

    // insert and values back references
    assertThat(op.getUri()).isEqualTo(uri);
    ShadowContentProviderOperation shadow = Shadows.shadowOf(op);
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
    shadow = Shadows.shadowOf(op);
    assertThat(shadow.getType()).isEqualTo(ShadowContentProviderOperation.TYPE_UPDATE);
    assertThat(shadow.getContentValues().getAsString("updateKey")).isEqualTo("updateValue");
    assertThat(shadow.getSelection()).isEqualTo("a=? and b=?");
    assertThat(shadow.getSelectionArgs()).asList().containsExactly("abc");
    assertThat(shadow.getSelectionArgsBackReferences()).isEqualTo(Collections.<Integer, Integer>singletonMap(1, 3));

    // delete and expected count
    op = ContentProviderOperation.newDelete(uri)
        .withExpectedCount(1)
        .build();
    assertThat(op.getUri()).isEqualTo(uri);
    shadow = Shadows.shadowOf(op);
    assertThat(shadow.getType()).isEqualTo(ShadowContentProviderOperation.TYPE_DELETE);
    assertThat(shadow.getExpectedCount()).isEqualTo(1);
  }

}
