package org.robolectric.shadows;

import android.app.DownloadManager;
import android.database.Cursor;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.tester.android.database.TestCursor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.robolectric.Robolectric.shadowOf_;

/**
 * Shadows Androids DownloadManager
 */
@Implements(DownloadManager.class)
public class ShadowDownloadManager {

  private Map<Long, DownloadManager.Request> requestMap = new HashMap<Long, DownloadManager.Request>();
  private long queueCounter = -1; // First request starts at 0 just like in the real DownloadManager.

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
    @RealObject
    DownloadManager.Request realObject;

    private CharSequence description;
    private int status;

    @Implementation
    public DownloadManager.Request setAllowedNetworkTypes(int flags) {
      return realObject;
    }

    @Implementation
    public DownloadManager.Request setMimeType(String mimeType) {
      return realObject;
    }

    @Implementation
    public DownloadManager.Request setTitle(CharSequence title) {
      return realObject;
    }

    @Implementation
    public DownloadManager.Request setDescription(CharSequence description) {
      this.description = description;
      return realObject;
    }

    @Implementation
    public DownloadManager.Request setAllowedOverRoaming(boolean allowed) {
      return realObject;
    }

    @Implementation
    public DownloadManager.Request setDestinationInExternalPublicDir(String dirType, String subPath) {
      return realObject;
    }

    public CharSequence getDescription() {
      return this.description;
    }

    public void setStatus(int status) {
      this.status = status;
    }

    public int getStatus() {
      return this.status;
    }
  }

  @Implements(DownloadManager.Query.class)
  public static class ShadowQuery {

    @RealObject
    private DownloadManager.Query realObject;

    private long[] ids = null;

    @Implementation
    public DownloadManager.Query setFilterById(long... ids) {
      this.ids = ids;
      return realObject;
    }

    public long[] getIds() {
      return this.ids;
    }
  }

  private class ResultCursor extends TestCursor {

    private static final int COLUMN_INDEX_LOCAL_FILENAME = 0;
    private static final int COLUMN_INDEX_DESCRIPTION = 1;
    private static final int COLUMN_INDEX_REASON = 2;
    private static final int COLUMN_INDEX_STATUS = 3;

    public List<DownloadManager.Request> requests = new ArrayList<DownloadManager.Request>();
    private int positionIndex;
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
      }

      return 0;
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
        case COLUMN_INDEX_DESCRIPTION:
          return request.getDescription().toString();
        case COLUMN_INDEX_REASON:
          return "reason not implemented";
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