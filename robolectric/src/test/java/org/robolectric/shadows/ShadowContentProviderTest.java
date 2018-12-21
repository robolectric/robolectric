package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.testing.TestContentProvider1;

@RunWith(AndroidJUnit4.class)
public class ShadowContentProviderTest {
  @Config(minSdk = KITKAT)
  @Test public void testSetCallingPackage() throws Exception {
    ContentProvider provider = new TestContentProvider1();
    shadowOf(provider).setCallingPackage("calling-package");
    assertThat(provider.getCallingPackage()).isEqualTo("calling-package");
  }

  @Config(minSdk = KITKAT)
  @Test public void getCallingPackage_normal() throws Exception {
    Context context = ApplicationProvider.getApplicationContext();
    Robolectric.buildContentProvider(TestContentProvider1.class).create();
    ContentResolver resolver = context.getContentResolver();

    Cursor cursor = resolver
        .query(Uri.parse("content://org.robolectric.authority1/some/path"), null, null, null, null);
    cursor.moveToFirst();

    assertThat(cursor.getString(0)).isEqualTo(context.getPackageName());
  }
}
