package org.robolectric.fakes;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.robolectric.fakes.MediaUriMatcher.AUDIO_ALBUMART;
import static org.robolectric.fakes.MediaUriMatcher.AUDIO_ALBUMART_FILE_ID;
import static org.robolectric.fakes.MediaUriMatcher.AUDIO_ALBUMART_ID;
import static org.robolectric.fakes.MediaUriMatcher.AUDIO_GENRES;
import static org.robolectric.fakes.MediaUriMatcher.AUDIO_GENRES_ID;
import static org.robolectric.fakes.MediaUriMatcher.AUDIO_GENRES_ID_MEMBERS;
import static org.robolectric.fakes.MediaUriMatcher.AUDIO_MEDIA;
import static org.robolectric.fakes.MediaUriMatcher.AUDIO_MEDIA_ID;
import static org.robolectric.fakes.MediaUriMatcher.AUDIO_MEDIA_ID_GENRES;
import static org.robolectric.fakes.MediaUriMatcher.AUDIO_MEDIA_ID_GENRES_ID;
import static org.robolectric.fakes.MediaUriMatcher.AUDIO_PLAYLISTS;
import static org.robolectric.fakes.MediaUriMatcher.AUDIO_PLAYLISTS_ID;
import static org.robolectric.fakes.MediaUriMatcher.AUDIO_PLAYLISTS_ID_MEMBERS;
import static org.robolectric.fakes.MediaUriMatcher.AUDIO_PLAYLISTS_ID_MEMBERS_ID;
import static org.robolectric.fakes.MediaUriMatcher.DOWNLOADS;
import static org.robolectric.fakes.MediaUriMatcher.DOWNLOADS_ID;
import static org.robolectric.fakes.MediaUriMatcher.FILES_ID;
import static org.robolectric.fakes.MediaUriMatcher.IMAGES_MEDIA;
import static org.robolectric.fakes.MediaUriMatcher.IMAGES_MEDIA_ID;
import static org.robolectric.fakes.MediaUriMatcher.IMAGES_THUMBNAILS;
import static org.robolectric.fakes.MediaUriMatcher.IMAGES_THUMBNAILS_ID;
import static org.robolectric.fakes.MediaUriMatcher.VIDEO_MEDIA;
import static org.robolectric.fakes.MediaUriMatcher.VIDEO_MEDIA_ID;
import static org.robolectric.fakes.MediaUriMatcher.VIDEO_THUMBNAILS;
import static org.robolectric.fakes.MediaUriMatcher.VIDEO_THUMBNAILS_ID;

import android.content.ClipDescription;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Build.VERSION_CODES;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.UserHandle;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Downloads;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Video;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.io.Files;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import org.robolectric.RuntimeEnvironment;

/**
 * A fake implementation of the content provider in MediaStore. The provider is functional and can
 * be used to populate the android media store in tests.
 *
 * <p>It currently is a very simplistic implementation compared to the real <a
 * href="https://cs.android.com/android/platform/superproject/main/+/main:packages/providers/MediaProvider/src/com/android/providers/media/MediaProvider.java">MediaProvider</a>
 *
 * <p>Among other limitations, it:
 *
 * <ul>
 *   <li>Only supports media content queries
 *   <li>Only supports single external storage volume
 *   <li>Does not support monitoring for file system changes
 *   <li>Only supports single user
 * </ul>
 */
public final class FakeMediaProvider extends ContentProvider {

  private static final String TABLE_NAME = "files";

  private Supplier<SQLiteDatabase> dbSupplier;

  private final Random displayNameRandom = new Random();
  private final MediaUriMatcher uriMatcher = new MediaUriMatcher(MediaStore.AUTHORITY);
  private static final String TAG = "FakeMediaProvider";

  @Override
  public boolean onCreate() {
    // currently only Q and above is supported
    Preconditions.checkArgument(RuntimeEnvironment.getApiLevel() >= VERSION_CODES.Q);
    // lazily create the database to avoid the performance hit of loading native sqlite for tests
    // that do not need it
    dbSupplier =
        Suppliers.memoize(
            () -> {
              SQLiteDatabase db = SQLiteDatabase.create(null);
              db.execSQL(createTableStatement());
              return db;
            });

    return true;
  }

  @Override
  public Cursor query(
      Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
    return query(uri, projection, selection, selectionArgs, sortOrder, null);
  }

  @Override
  public Cursor query(
      Uri uri,
      String[] projection,
      String selection,
      String[] selectionArgs,
      String sortOrder,
      CancellationSignal cancellationSignal) {
    checkNotNull(uri);
    final SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

    qb.setTables(TABLE_NAME);

    int uriType = uriMatcher.matchUri(uri);
    switch (uriType) {
      case AUDIO_MEDIA_ID:
      case VIDEO_MEDIA_ID:
      case IMAGES_MEDIA_ID:
      case AUDIO_ALBUMART_ID:
      case VIDEO_THUMBNAILS_ID:
      case IMAGES_THUMBNAILS_ID:
      case AUDIO_PLAYLISTS_ID:
      case FILES_ID:
      case DOWNLOADS_ID:
        qb.appendWhere(MediaStore.MediaColumns._ID + "=" + uri.getLastPathSegment());
        break;
      default:
        break;
    }

    Cursor cursor =
        qb.query(
            dbSupplier.get(),
            projection,
            selection,
            selectionArgs,
            null,
            null,
            sortOrder,
            null,
            cancellationSignal);
    cursor.setNotificationUri(getContext().getContentResolver(), uri);
    return cursor;
  }

  @Override
  public String getType(Uri uri) {
    // copy of MediaProvider#getType

    final int match = uriMatcher.matchUri(uri);
    switch (match) {
      case IMAGES_MEDIA_ID:
      case AUDIO_MEDIA_ID:
      case AUDIO_PLAYLISTS_ID:
      case AUDIO_PLAYLISTS_ID_MEMBERS_ID:
      case VIDEO_MEDIA_ID:
      case DOWNLOADS_ID:
      case FILES_ID:
        return queryForType(uri);

      case IMAGES_MEDIA:
      case IMAGES_THUMBNAILS:
        return Images.Media.CONTENT_TYPE;

      case AUDIO_ALBUMART_ID:
      case AUDIO_ALBUMART_FILE_ID:
      case IMAGES_THUMBNAILS_ID:
      case VIDEO_THUMBNAILS_ID:
        return "image/jpeg";

      case AUDIO_MEDIA:
      case AUDIO_GENRES_ID_MEMBERS:
      case AUDIO_PLAYLISTS_ID_MEMBERS:
        return Audio.Media.CONTENT_TYPE;

      case AUDIO_GENRES:
      case AUDIO_MEDIA_ID_GENRES:
        return Audio.Genres.CONTENT_TYPE;
      case AUDIO_GENRES_ID:
      case AUDIO_MEDIA_ID_GENRES_ID:
        return Audio.Genres.ENTRY_CONTENT_TYPE;
      case AUDIO_PLAYLISTS:
        return Audio.Playlists.CONTENT_TYPE;

      case VIDEO_MEDIA:
        return Video.Media.CONTENT_TYPE;
      case DOWNLOADS:
        return Downloads.CONTENT_TYPE;
      default:
        throw new IllegalStateException("Unknown URI : " + uri);
    }
  }

  private String queryForType(Uri url) {
    Cursor cursor = queryForSingleItem(url, new String[] {MediaColumns.MIME_TYPE}, null, null);
    return cursor.getString(0);
  }

  private Cursor queryForSingleItem(
      Uri uri, String[] projection, String selection, String[] selectionArgs) {
    Cursor c = query(uri, projection, selection, selectionArgs, null);
    if (c == null) {
      throw new IllegalArgumentException("Failed to find single item for uri " + uri);
    }
    if (c.getCount() != 1 || !c.moveToFirst()) {
      c.close();
      throw new IllegalArgumentException("Failed to find single item for uri " + uri);
    }
    return c;
  }

  @Override
  public Uri insert(Uri uri, ContentValues originalValues) {
    ContentValues values = new ContentValues(checkNotNull(originalValues));

    final int match = uriMatcher.matchUri(uri);
    checkState(match != UriMatcher.NO_MATCH, "Unrecognized uri " + uri.toString());

    if (match == AUDIO_PLAYLISTS_ID || match == AUDIO_PLAYLISTS_ID_MEMBERS) {
      // playlist support is deprecated in android, but there are still tests
      // that rely on insert at least not crashing
      Log.w(TAG, "Ignoring insert of unsupported playlist members");
      return null;
    }

    // Give the record reasonable defaults when missing
    String displayName = values.getAsString(MediaColumns.DISPLAY_NAME);
    String mimeType = values.getAsString(MediaColumns.MIME_TYPE);
    // assume mp4 by default
    String currentDisplayExtension = displayName != null ? Files.getFileExtension(displayName) : "";
    if (TextUtils.isEmpty(currentDisplayExtension)
        && TextUtils.isEmpty(mimeType)
        && !TextUtils.isEmpty(displayName)) {
      throw new IllegalArgumentException("Could not determine mime type ");
    }
    if (TextUtils.isEmpty(displayName)) {
      // Real media provider uses System.currentTimeMillis() as default name
      // This could be problematic for Robolectric where clock is fixed, so just use
      // a random number.
      displayName = String.valueOf(Math.abs(displayNameRandom.nextInt()));
      values.put(MediaColumns.DISPLAY_NAME, displayName);
    }
    if (TextUtils.isEmpty(mimeType)) {
      if (TextUtils.isEmpty(currentDisplayExtension)) {
        mimeType = getDefaultMimeTypeFromUri(match);
      } else {
        // try to guess from extension
        mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(currentDisplayExtension);
        if (TextUtils.isEmpty(mimeType)) {
          throw new IllegalArgumentException("Could not guess mimeType from name " + displayName);
        }
      }
      values.put(MediaColumns.MIME_TYPE, mimeType);
    }
    validateMimeType(match, mimeType);

    // Check that the current given extension matches the mime type.
    // and if it doesn't, append the correct extension to the display name
    // The logic flows this way to handle mime types like image/jpeg that have multiple valid
    // extensions
    String mimeTypeFromExtension =
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(currentDisplayExtension);
    if (mimeTypeFromExtension == null || !mimeTypeFromExtension.equals(mimeType)) {
      String extensionFromMime = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
      if (extensionFromMime == null) {
        throw new IllegalArgumentException("Unsupported MIME type " + mimeType);
      }
      displayName += "." + extensionFromMime;
      values.put(MediaColumns.DISPLAY_NAME, displayName);
    }

    // Use default directories when missing
    String relativePath = values.getAsString(MediaColumns.RELATIVE_PATH);
    if (TextUtils.isEmpty(relativePath)) {
      relativePath = getDefaultRelativePath(match);
      values.put(MediaColumns.RELATIVE_PATH, relativePath + "/");
    } else {
      // TODO: support other types
      validateRelativePath(mimeType, relativePath);
    }

    validateRelativePath(relativePath, values.getAsString(MediaColumns.RELATIVE_PATH));
    values.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000);
    values.put(MediaColumns.BUCKET_DISPLAY_NAME, relativePath.split("/")[0]);

    String baseFileName = Files.getNameWithoutExtension(displayName);
    if (TextUtils.isEmpty(values.getAsString(MediaColumns.TITLE))) {
      // default to display name if not set
      values.put(MediaColumns.TITLE, baseFileName);
    }
    values.remove(FileColumns.MEDIA_TYPE);
    if (!values.containsKey(MediaColumns.IS_PENDING)) {
      values.put(MediaColumns.IS_PENDING, false);
    }
    String dataPath =
        Paths.get(
                Environment.getExternalStorageDirectory().toString(),
                relativePath,
                makeUniqueFileName(baseFileName, Files.getFileExtension(displayName)))
            .toAbsolutePath()
            .toString();
    if (values.containsKey(MediaColumns.DATA)) {
      // technically this is only rejected on SDKs >= 34 but lets just forbid it everywhere
      throw new IllegalArgumentException("Mutation of _data is not allowed");
    }
    values.put(MediaColumns.DATA, dataPath);

    long rowId = dbSupplier.get().insert(TABLE_NAME, null, values);
    return rowId > 0 ? ContentUris.withAppendedId(uri, rowId) : null;
  }

  // guard against two entries with same displayname and relative path being inserted by adding a
  // random number to each entry
  private String makeUniqueFileName(String baseName, String ext) {
    return String.format("%s-%d.%s", baseName, Math.abs(displayNameRandom.nextInt()), ext);
  }

  private String getDefaultRelativePath(int match) {
    switch (match) {
      case AUDIO_MEDIA:
      case AUDIO_MEDIA_ID:
      case AUDIO_PLAYLISTS:
      case AUDIO_PLAYLISTS_ID:
        return Environment.DIRECTORY_MUSIC;
      case VIDEO_MEDIA:
      case VIDEO_MEDIA_ID:
        return Environment.DIRECTORY_MOVIES;
      case IMAGES_MEDIA:
      case IMAGES_MEDIA_ID:
      case AUDIO_ALBUMART:
      case AUDIO_ALBUMART_ID:
      case VIDEO_THUMBNAILS:
      case VIDEO_THUMBNAILS_ID:
      case IMAGES_THUMBNAILS:
      case IMAGES_THUMBNAILS_ID:
        return Environment.DIRECTORY_PICTURES;
      case DOWNLOADS:
      case DOWNLOADS_ID:
      default:
        return Environment.DIRECTORY_DOWNLOADS;
    }
  }

  private String getDefaultMimeTypeFromUri(int match) {
    // stripped copy of MediaProvider#ensureFileColumns
    switch (match) {
      case AUDIO_MEDIA:
      case AUDIO_MEDIA_ID:
        return "audio/mpeg";
      case VIDEO_MEDIA:
      case VIDEO_MEDIA_ID:
        return "video/mp4";
      case IMAGES_MEDIA:
      case IMAGES_MEDIA_ID:
      case AUDIO_ALBUMART:
      case AUDIO_ALBUMART_ID:
      case VIDEO_THUMBNAILS:
      case VIDEO_THUMBNAILS_ID:
      case IMAGES_THUMBNAILS:
      case IMAGES_THUMBNAILS_ID:
        return "image/jpeg";
      case AUDIO_PLAYLISTS:
      case AUDIO_PLAYLISTS_ID:
        return "audio/mpegurl";
      default:
        return ClipDescription.MIMETYPE_UNKNOWN;
    }
  }

  private void validateMimeType(int match, String mimeType) {
    if (match == AUDIO_MEDIA && !mimeType.startsWith("audio")) {
      throw new IllegalArgumentException("Invalid mimeType for uri " + mimeType + " " + match);
    } else if (match == VIDEO_MEDIA && !mimeType.startsWith("video")) {
      throw new IllegalArgumentException("Invalid mimeType for uri " + mimeType + " " + match);
    } else if (match == IMAGES_MEDIA && !mimeType.startsWith("image")) {
      throw new IllegalArgumentException("Invalid mimeType for uri " + mimeType + " " + match);
    }
  }

  private void validateRelativePath(String mimeType, String relativePath) {
    if (mimeType.startsWith("video")) {
      String primaryDir = relativePath.split("/")[0];
      Set<String> allowedPaths = Set.of("DCIM", "Movies", "Pictures");
      if (!allowedPaths.contains(primaryDir)) {
        throw new IllegalArgumentException(
            String.format(
                "Primary directory %s not allowed for content://media/external/video/media; allowed"
                    + " directories are %s",
                primaryDir, allowedPaths));
      }
    }
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    // TODO: also delete the associated file
    SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

    qb.setTables(TABLE_NAME);

    selection =
        selection == null
            ? MediaStore.MediaColumns._ID + " = ?"
            : selection + " AND " + MediaStore.MediaColumns._ID + " = ?";
    selectionArgs =
        selectionArgs == null
            ? new String[] {uri.getLastPathSegment()}
            : appendSelectionArgs(selectionArgs, uri.getLastPathSegment());

    int count = 0;
    try (Cursor cursor =
        qb.query(
            dbSupplier.get(),
            new String[] {MediaStore.MediaColumns._ID},
            selection,
            selectionArgs,
            /* groupBy= */ null,
            /* having= */ null,
            /* orderBy= */ null)) {
      while (cursor != null && cursor.moveToNext()) {
        long mediaStoreId = cursor.getLong(/* columnIndex= */ 0);

        count +=
            qb.delete(
                dbSupplier.get(),
                MediaStore.MediaColumns._ID + " = ?",
                new String[] {String.valueOf(mediaStoreId)});
      }
    }

    // real Android will just throw SecurityException if it doesn't recognize the id.
    // so just do that
    if (count <= 0) {
      throw new SecurityException("Could not find entries " + uri.toString());
    }
    return count;
  }

  private static String[] appendSelectionArgs(String[] originalArgs, String additionalArgs) {
    int length = originalArgs.length + 1;
    String[] result = Arrays.copyOf(originalArgs, length);
    result[length - 1] = additionalArgs;
    return result;
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    selection =
        selection == null
            ? MediaStore.MediaColumns._ID + " = ?"
            : selection + " AND " + MediaStore.MediaColumns._ID + " = ?";
    selectionArgs =
        selectionArgs == null
            ? new String[] {uri.getLastPathSegment()}
            : appendSelectionArgs(selectionArgs, uri.getLastPathSegment());

    return dbSupplier.get().update(TABLE_NAME, values, selection, selectionArgs);
  }

  @Override
  public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
    try (Cursor cursor =
        query(
            uri,
            /* projection= */ new String[] {
              MediaColumns.DATA,
            },
            /* selection= */ null,
            /* selectionArgs= */ null,
            /* sortOrder= */ null)) {
      if (cursor.getCount() == 1 && cursor.moveToFirst()) {
        String data = cursor.getString(0);
        File file = new File(data);
        if (!file.exists()) {
          if (!mode.contains("w")) {
            throw new FileNotFoundException(
                "File does not exist and mode is not write: " + file.getPath() + " " + mode);
          }
          file.getParentFile().mkdirs();
          try {
            // On real MediaProvider, the behavior for opening a non-existing file to write is to
            // proactively create a new one as opposed to throwing exception.
            file.createNewFile();
          } catch (IOException e) {
            throw new FileNotFoundException(e.getMessage());
          }
        }
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.parseMode(mode));
      }
    }
    throw new FileNotFoundException("Can't find file for uri: " + uri);
  }

  private static String createTableStatement() {
    // copy of MediaProvider's DatabaseHelper#createLatestSchema
    return "CREATE TABLE files (_id INTEGER PRIMARY KEY AUTOINCREMENT,"
        + "_data TEXT UNIQUE COLLATE NOCASE,_size INTEGER,format INTEGER,parent INTEGER,"
        + "date_added INTEGER,date_modified INTEGER,mime_type TEXT,title TEXT,"
        + "description TEXT,_display_name TEXT,picasa_id TEXT,orientation INTEGER,"
        + "latitude DOUBLE,longitude DOUBLE,datetaken INTEGER,mini_thumb_magic INTEGER,"
        + "bucket_id TEXT,bucket_display_name TEXT,isprivate INTEGER,title_key TEXT,"
        + "artist_id INTEGER,album_id INTEGER,composer TEXT,track INTEGER,"
        + "year INTEGER CHECK(year!=0),is_ringtone INTEGER,is_music INTEGER,"
        + "is_alarm INTEGER,is_notification INTEGER,is_podcast INTEGER,album_artist TEXT,"
        + "duration INTEGER,bookmark INTEGER,artist TEXT,album TEXT,resolution TEXT,"
        + "tags TEXT,category TEXT,language TEXT,mini_thumb_data TEXT,name TEXT,"
        + "media_type INTEGER,old_id INTEGER,is_drm INTEGER,"
        + "width INTEGER, height INTEGER, title_resource_uri TEXT,"
        + "owner_package_name TEXT DEFAULT NULL,"
        + "color_standard INTEGER, color_transfer INTEGER, color_range INTEGER,"
        + "_hash BLOB DEFAULT NULL, is_pending INTEGER DEFAULT 0,"
        + "is_download INTEGER DEFAULT 0, download_uri TEXT DEFAULT NULL,"
        + "referer_uri TEXT DEFAULT NULL, is_audiobook INTEGER DEFAULT 0,"
        + "date_expires INTEGER DEFAULT NULL,is_trashed INTEGER DEFAULT 0,"
        + "group_id INTEGER DEFAULT NULL,primary_directory TEXT DEFAULT NULL,"
        + "secondary_directory TEXT DEFAULT NULL,document_id TEXT DEFAULT NULL,"
        + "instance_id TEXT DEFAULT NULL,original_document_id TEXT DEFAULT NULL,"
        + "relative_path TEXT DEFAULT NULL,volume_name TEXT DEFAULT NULL,"
        + "artist_key TEXT DEFAULT NULL,album_key TEXT DEFAULT NULL,"
        + "genre TEXT DEFAULT NULL,genre_key TEXT DEFAULT NULL,genre_id INTEGER,"
        + "author TEXT DEFAULT NULL, bitrate INTEGER DEFAULT NULL,"
        + "capture_framerate REAL DEFAULT NULL, cd_track_number TEXT DEFAULT NULL,"
        + "compilation INTEGER DEFAULT NULL, disc_number TEXT DEFAULT NULL,"
        + "is_favorite INTEGER DEFAULT 0, num_tracks INTEGER DEFAULT NULL,"
        + "writer TEXT DEFAULT NULL, exposure_time TEXT DEFAULT NULL,"
        + "f_number TEXT DEFAULT NULL, iso INTEGER DEFAULT NULL,"
        + "scene_capture_type INTEGER DEFAULT NULL, generation_added INTEGER DEFAULT 0,"
        + "generation_modified INTEGER DEFAULT 0, xmp BLOB DEFAULT NULL,"
        + "_transcode_status INTEGER DEFAULT 0, _video_codec_type TEXT DEFAULT NULL,"
        + "_modifier INTEGER DEFAULT 0, is_recording INTEGER DEFAULT 0,"
        + "redacted_uri_id TEXT DEFAULT NULL, _user_id INTEGER DEFAULT "
        + UserHandle.myUserId()
        + ", _special_format INTEGER DEFAULT NULL,"
        + "oem_metadata BLOB DEFAULT NULL,"
        + "inferred_media_date INTEGER,"
        + "bits_per_sample INTEGER DEFAULT NULL, samplerate INTEGER DEFAULT NULL,"
        + "inferred_date INTEGER,"
        // in real mediaprovider thumbnail metadata is stored in a separate table
        // that adds a lot of complexity, so just add some common fields to this 'files' table
        + "kind INTEGER,"
        + "image_id INTEGER"
        + ")";
  }

  private static String extractFileName(String data) {
    if (data == null) {
      return null;
    }
    data = extractDisplayName(data);

    final int lastDot = data.lastIndexOf('.');
    if (lastDot == -1) {
      return data;
    } else {
      return data.substring(0, lastDot);
    }
  }

  private static String extractDisplayName(String data) {
    if (data == null) {
      return null;
    }
    if (data.indexOf('/') == -1) {
      return data;
    }
    if (data.endsWith("/")) {
      data = data.substring(0, data.length() - 1);
    }
    return data.substring(data.lastIndexOf('/') + 1);
  }
}
