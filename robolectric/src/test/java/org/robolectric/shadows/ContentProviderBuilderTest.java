package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertThrows;

import android.content.ContentResolver;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.io.ByteStreams;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.android.controller.ContentProviderController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ContentProviderBuilder.FakeContentProvider;
import org.robolectric.shadows.ContentProviderBuilder.Query;

@RunWith(AndroidJUnit4.class)
public class ContentProviderBuilderTest {
  private static final String AUTHORITY = "org.robolectric.test.builder";
  private static final Uri CONTACTS = Uri.parse("content://" + AUTHORITY + "/contacts");

  private ContentResolver contentResolver;

  @Before
  public void setUp() {
    contentResolver = ApplicationProvider.getApplicationContext().getContentResolver();
  }

  private FakeContentProvider install(ContentProviderBuilder builder) {
    return ContentProviderController.of(builder.build()).create(AUTHORITY).get();
  }

  private static MatrixCursor names(String... names) {
    MatrixCursor cursor = new MatrixCursor(new String[] {"name"});
    for (String name : names) {
      cursor.addRow(new Object[] {name});
    }
    return cursor;
  }

  @Test
  public void query_matchingPath_returnsCursor() {
    install(ContentProviderBuilder.newBuilder().addCursor("/contacts", names("Alice")));

    try (Cursor cursor = contentResolver.query(CONTACTS, null, null, null, null)) {
      assertThat(cursor.moveToFirst()).isTrue();
      assertThat(cursor.getString(0)).isEqualTo("Alice");
    }
  }

  @Test
  public void query_sameCursorTwice_startsBeforeFirstRow() {
    MatrixCursor names = names("Alice", "Bob");
    install(ContentProviderBuilder.newBuilder().addCursor("/contacts", names));

    Cursor first = contentResolver.query(CONTACTS, null, null, null, null);
    first.moveToLast();
    Cursor second = contentResolver.query(CONTACTS, null, null, null, null);

    assertThat(second.moveToNext()).isTrue();
    assertThat(second.getString(0)).isEqualTo("Alice");
  }

  @Test
  public void query_cursorSupplier_returnsNewCursorEachTime() {
    install(ContentProviderBuilder.newBuilder().addCursor("/contacts", () -> names("Alice")));

    contentResolver.query(CONTACTS, null, null, null, null).close();
    try (Cursor cursor = contentResolver.query(CONTACTS, null, null, null, null)) {
      assertThat(cursor.isClosed()).isFalse();
      assertThat(cursor.getCount()).isEqualTo(1);
    }
  }

  @Test
  public void query_pathWithQuery_matchesPatternWithQuery() {
    install(
        ContentProviderBuilder.newBuilder()
            .addCursor("/contacts\\?starred=1", names("Alice"))
            .addCursor("/contacts", names("Alice", "Bob")));

    try (Cursor starred =
            contentResolver.query(
                CONTACTS.buildUpon().appendQueryParameter("starred", "1").build(),
                null,
                null,
                null,
                null);
        Cursor all = contentResolver.query(CONTACTS, null, null, null, null)) {
      assertThat(starred.getCount()).isEqualTo(1);
      assertThat(all.getCount()).isEqualTo(2);
    }
  }

  @Test
  public void query_severalMatchingPatterns_firstAddedWins() {
    install(
        ContentProviderBuilder.newBuilder()
            .addCursor("/contacts", names("Alice"))
            .addCursor("/.*", names("Alice", "Bob")));

    try (Cursor cursor = contentResolver.query(CONTACTS, null, null, null, null)) {
      assertThat(cursor.getCount()).isEqualTo(1);
    }
  }

  @Test
  public void query_noMatchingPath_returnsNull() {
    install(ContentProviderBuilder.newBuilder().addCursor("/contacts", names("Alice")));

    assertThat(
            contentResolver.query(
                Uri.parse("content://" + AUTHORITY + "/groups"), null, null, null, null))
        .isNull();
  }

  @Test
  public void query_uriWithoutPath_returnsNull() {
    FakeContentProvider provider =
        ContentProviderBuilder.newBuilder().addCursor(".*", names("Alice")).build();

    assertThat(provider.query(Uri.parse("mailto:alice@example.com"), null, null, null, null))
        .isNull();
  }

  @Test
  public void build_laterPaths_doNotChangeProvider() {
    ContentProviderBuilder builder = ContentProviderBuilder.newBuilder();
    FakeContentProvider provider =
        ContentProviderController.of(builder.build()).create(AUTHORITY).get();
    builder.addCursor("/contacts", names("Alice"));

    assertThat(contentResolver.query(CONTACTS, null, null, null, null)).isNull();
    assertThat(provider.getQueries()).hasSize(1);
  }

  @Test
  public void getQueries_recordsEachQuery() {
    FakeContentProvider provider =
        install(ContentProviderBuilder.newBuilder().addCursor("/contacts", names("Alice")));
    assertThat(provider.getLastQuery()).isNull();

    contentResolver.query(
        CONTACTS, new String[] {"name"}, "name = ?", new String[] {"Alice"}, "name");
    contentResolver.query(CONTACTS, null, null, null, null);

    assertThat(provider.getQueries()).hasSize(2);
    Query first = provider.getQueries().get(0);
    assertThat(first.getUri()).isEqualTo(CONTACTS);
    assertThat(first.getProjection()).asList().containsExactly("name");
    assertThat(first.getSelection()).isEqualTo("name = ?");
    assertThat(first.getSelectionArgs()).asList().containsExactly("Alice");
    assertThat(first.getSortOrder()).isEqualTo("name");
    Query last = provider.getLastQuery();
    assertThat(last.getProjection()).isNull();
    assertThat(last.getSelection()).isNull();
  }

  @Test
  public void getQueries_recordedArraysAreCopies() {
    FakeContentProvider provider =
        install(ContentProviderBuilder.newBuilder().addCursor("/contacts", names("Alice")));
    String[] selectionArgs = {"Alice"};

    contentResolver.query(CONTACTS, null, "name = ?", selectionArgs, null);
    selectionArgs[0] = "Bob";

    assertThat(Arrays.asList(provider.getLastQuery().getSelectionArgs())).containsExactly("Alice");
  }

  @Test
  @Config(minSdk = O)
  public void getQueries_bundleQuery_recordsQueryArgsAndSelection() {
    FakeContentProvider provider =
        install(ContentProviderBuilder.newBuilder().addCursor("/contacts", names("Alice")));
    Bundle queryArgs = new Bundle();
    queryArgs.putString(ContentResolver.QUERY_ARG_SQL_SELECTION, "name = ?");
    queryArgs.putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, new String[] {"Alice"});
    queryArgs.putInt(ContentResolver.QUERY_ARG_LIMIT, 5);

    try (Cursor cursor = contentResolver.query(CONTACTS, null, queryArgs, null)) {
      assertThat(cursor.getCount()).isEqualTo(1);
    }

    Query query = provider.getLastQuery();
    assertThat(provider.getQueries()).hasSize(1);
    assertThat(query.getSelection()).isEqualTo("name = ?");
    assertThat(query.getSelectionArgs()).asList().containsExactly("Alice");
    assertThat(query.getQueryArgs().getInt(ContentResolver.QUERY_ARG_LIMIT)).isEqualTo(5);
  }

  @Test
  public void openInputStream_returnsFileContentsEachTime() throws Exception {
    byte[] contents = new byte[200_000];
    Arrays.fill(contents, (byte) 7);
    install(ContentProviderBuilder.newBuilder().addFile("/photo", contents));
    Uri photo = Uri.parse("content://" + AUTHORITY + "/photo");

    for (int i = 0; i < 2; i++) {
      try (InputStream in = contentResolver.openInputStream(photo)) {
        assertThat(ByteStreams.toByteArray(in)).isEqualTo(contents);
      }
    }
  }

  @Test
  public void addFile_inputStream_readsAndClosesIt() throws Exception {
    ClosingInputStream in = new ClosingInputStream("hello".getBytes(UTF_8));
    install(ContentProviderBuilder.newBuilder().addFile("/note", in));
    assertThat(in.closed).isTrue();

    try (InputStream note =
        contentResolver.openInputStream(Uri.parse("content://" + AUTHORITY + "/note"))) {
      assertThat(new String(ByteStreams.toByteArray(note), UTF_8)).isEqualTo("hello");
    }
  }

  @Test
  public void openOutputStream_throwsFileNotFoundException() {
    install(ContentProviderBuilder.newBuilder().addFile("/note", new byte[] {1}));

    assertThrows(
        FileNotFoundException.class,
        () -> contentResolver.openOutputStream(Uri.parse("content://" + AUTHORITY + "/note")));
  }

  @Test
  public void openInputStream_noMatchingPath_throwsFileNotFoundException() {
    install(ContentProviderBuilder.newBuilder().addFile("/note", new byte[] {1}));

    assertThrows(
        FileNotFoundException.class,
        () -> contentResolver.openInputStream(Uri.parse("content://" + AUTHORITY + "/other")));
  }

  private static final class ClosingInputStream extends ByteArrayInputStream {
    boolean closed;

    ClosingInputStream(byte[] bytes) {
      super(bytes);
    }

    @Override
    public void close() {
      closed = true;
    }
  }
}
