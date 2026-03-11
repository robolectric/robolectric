package org.robolectric.android.controller

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.robolectric.Robolectric

class ContentProviderControllerTest {

  @Test
  fun create_callsOnCreate() {
    val provider = Robolectric.buildContentProvider(TestContentProvider::class.java).create().get()
    assertThat(provider.transcript).containsExactly("onCreate")
  }

  @Test
  fun createWithProviderInfo_registersWithContentResolver() {
    val authority = "org.robolectric.runner.platform.provider"
    val providerInfo = ProviderInfo().apply { this.authority = authority }
    val provider =
      Robolectric.buildContentProvider(TestContentProvider::class.java).create(providerInfo).get()
    val resolver = ApplicationProvider.getApplicationContext<Context>().contentResolver

    val client = resolver.acquireContentProviderClient(authority)
    try {
      client?.query(Uri.parse("content://$authority/items"), null, null, null, null)
    } finally {
      client?.release()
    }

    assertThat(provider.transcript).containsAtLeast("onCreate", "query")
  }

  class TestContentProvider : ContentProvider() {
    val transcript = mutableListOf<String>()

    override fun onCreate(): Boolean {
      transcript.add("onCreate")
      return true
    }

    override fun query(
      uri: Uri,
      projection: Array<out String>?,
      selection: String?,
      selectionArgs: Array<out String>?,
      sortOrder: String?,
    ): Cursor? {
      transcript.add("query")
      return null
    }

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
      uri: Uri,
      values: ContentValues?,
      selection: String?,
      selectionArgs: Array<out String>?,
    ): Int = 0
  }
}
