package org.robolectric.shadows;

import android.app.DownloadManager;
import android.database.Cursor;
import android.net.Uri;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.tester.android.database.TestCursor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.fest.reflect.core.Reflection.field;
import static org.robolectric.Robolectric.shadowOf_;

/**
 * Shadows Androids DownloadManager
 */
@Implements(DownloadManager.class)
public class ShadowDownloadManager {

  private long queueCounter = -1; // First request starts at 0 just like in the real DownloadManager
  private Map<Long, DownloadManager.Request> requestMap = new HashMap<Long, DownloadManager.Request>();

  @Implementation
  public long enqueue(DownloadManager.Request request) {
    queueCounter++;
    requestMap.put(queueCounter, request);
    return queueCounter;
  }

  @Implementation
  public int remove(long... ids) {
    int removeCount = 0;
    for (long id : ids) {
      if (requestMap.remove(id) != null) {
        removeCount++;
      }
    }
    return removeCount;
  }

  @Implementation
  public Cursor query(DownloadManager.Query query) {
    ShadowQuery shadow = shadowOf_(query);
    long[] ids = shadow.getIds();

    ResultCursor result = new ResultCursor();
    for (long id : ids) {
      DownloadManager.Request request = requestMap.get(id);
      if (request != null) {
        result.requests.add(request);
      }
    }
    return result;
  }

  public DownloadManager.Request getRequest(long id) {
    return requestMap.get(id);
  }

  public int getRequestCount() {
    return requestMap.size();
  }

  @Implements(DownloadManager.Request.class)
  public static class ShadowRequest {
    @RealObject DownloadManager.Request realObject;

    private int status;

    public int getStatus() {
      return this.status;
    }

    public void setStatus(int status) {
      this.status = status;
    }

    public Uri getUri() {
      return field("mUri").ofType(Uri.class).in(realObject).get();
    }

    public Uri getDestination() {
      return field("mDestinationUri").ofType(Uri.class).in(realObject).get();
    }

    public CharSequence getTitle() {
      return field("mTitle").ofType(CharSequence.class).in(realObject).get();
    }

    public CharSequence getDescription() {
      return field("mDescription").ofType(CharSequence.class).in(realObject).get();
    }

    public CharSequence getMimeType() {
      return field("mMimeType").ofType(CharSequence.class).in(realObject).get();
    }

    public int getNotificationVisibility() {
      return field("mNotificationVisibility").ofType(int.class).in(realObject).get();
    }

    public int getAllowedNetworkTypes() {
      return field("mAllowedNetworkTypes").ofType(int.class).in(realObject).get();
    }

    public boolean getAllowedOverRoaming() {
      return field("mRoamingAllowed").ofType(boolean.class).in(realObject).get();
    }

    public boolean getAllowedOverMetered() {
      return field("mMeteredAllowed").ofType(boolean.class).in(realObject).get();
    }

    public boolean getVisibleInDownloadsUi() {
      return field("mIsVisibleInDownloadsUi").ofType(boolean.class).in(realObject).get();
    }
  }

  @Implements(DownloadManager.Query.class)
  public static class ShadowQuery {
    @RealObject DownloadManager.Query realObject;

    public long[] getIds() {
      return field("mIds").ofType(long[].class).in(realObject).get();
    }
  }

  private class ResultCursor extends TestCursor {
    private static final int COLUMN_INDEX_LOCAL_FILENAME = 0;
    private static final int COLUMN_INDEX_DESCRIPTION = 1;
    private static final int COLUMN_INDEX_REASON = 2;
    private static final int COLUMN_INDEX_STATUS = 3;
    private static final int COLUMN_INDEX_URI = 4;
    private static final int COLUMN_INDEX_LOCAL_URI = 5;

    public List<DownloadManager.Request> requests = new ArrayList<DownloadManager.Request>();
    private int positionIndex = -1;
    private boolean closed;

    @Override
    public int getCount() {
      checkClosed();
      return requests.size();
    }

    @Override
    public boolean moveToFirst() {
      checkClosed();
      positionIndex = 0;
      return !requests.isEmpty();
    }

    @Override
    public boolean moveToNext() {
      checkClosed();
      positionIndex += 1;
      return positionIndex < requests.size();
    }

    @Override
    public int getColumnIndex(String columnName) {
      checkClosed();

      if (DownloadManager.COLUMN_LOCAL_FILENAME.equals(columnName)) {
        return COLUMN_INDEX_LOCAL_FILENAME;

      } else if (DownloadManager.COLUMN_DESCRIPTION.equals(columnName)) {
        return COLUMN_INDEX_DESCRIPTION;

      } else if (DownloadManager.COLUMN_REASON.equals(columnName)) {
        return COLUMN_INDEX_REASON;

      } else if (DownloadManager.COLUMN_STATUS.equals(columnName)) {
        return COLUMN_INDEX_STATUS;

      } else if (DownloadManager.COLUMN_URI.equals(columnName)) {
        return COLUMN_INDEX_URI;

      } else if (DownloadManager.COLUMN_LOCAL_URI.equals(columnName)) {
        return COLUMN_INDEX_LOCAL_URI;
      }

      return -1;
    }

    @Override
    public void close() {
      this.closed = true;
    }

    @Override
    public boolean isClosed() {
      return closed;
    }

    @Override
    public String getString(int columnIndex) {
      checkClosed();
      ShadowRequest request = shadowOf_(requests.get(positionIndex));
      switch (columnIndex) {
        case COLUMN_INDEX_LOCAL_FILENAME:
          return "local file name not implemented";

        case COLUMN_INDEX_REASON:
          return "reason not implemented";

        case COLUMN_INDEX_DESCRIPTION:
          return request.getDescription().toString();

        case COLUMN_INDEX_URI:
          return request.getUri().toString();

        case COLUMN_INDEX_LOCAL_URI:
          return request.getDestination().toString();
      }

      return "Unknown ColumnIndex " + columnIndex;
    }

    @Override
    public int getInt(int columnIndex) {
      checkClosed();
      ShadowRequest request = shadowOf_(requests.get(positionIndex));
      if (columnIndex == COLUMN_INDEX_STATUS) {
        return request.getStatus();
      }
      return 0;
    }

    private void checkClosed() {
      if (closed) {
        throw new IllegalStateException("Cursor is already closed.");
      }
    }
  }
}