package org.robolectric.shadows.support.v4;

import static android.support.v4.media.MediaBrowserCompat.EXTRA_PAGE;
import static android.support.v4.media.MediaBrowserCompat.EXTRA_PAGE_SIZE;

import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserCompat.ConnectionCallback;
import android.support.v4.media.MediaBrowserCompat.ItemCallback;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v4.media.MediaBrowserCompat.SearchCallback;
import android.support.v4.media.MediaBrowserCompat.SubscriptionCallback;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.support.v4.media.MediaBrowserServiceCompat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/**
 * This will mimic the connection to a {@link MediaBrowserServiceCompat} by creating and maintaining
 * its own account of {@link MediaItem}s.
 */
@Implements(MediaBrowserCompat.class)
@Deprecated
public class ShadowMediaBrowserCompat {

  private final Handler handler = new Handler();
  private final MediaSessionCompat.Token token =
      Shadow.newInstanceOf(MediaSessionCompat.Token.class);

  private @RealObject MediaBrowserCompat mediaBrowser;

  private final Map<String, MediaItem> mediaItems = new LinkedHashMap<>();
  private final Map<MediaItem, List<MediaItem>> mediaItemChildren = new LinkedHashMap<>();

  private boolean isConnected;
  private ConnectionCallback connectionCallback;
  private String rootId = "root_id";

  @Implementation
  protected void __constructor__(
      Context context,
      ComponentName serviceComponent,
      ConnectionCallback callback,
      Bundle rootHints) {
    connectionCallback = callback;
    Shadow.invokeConstructor(
        MediaBrowserCompat.class,
        mediaBrowser,
        ClassParameter.from(Context.class, context),
        ClassParameter.from(ComponentName.class, serviceComponent),
        ClassParameter.from(ConnectionCallback.class, callback),
        ClassParameter.from(Bundle.class, rootHints));
  }

  @Implementation
  protected void connect() {
    handler.post(
        () -> {
          isConnected = true;
          connectionCallback.onConnected();
        });
  }

  @Implementation
  protected void disconnect() {
    handler.post(
        () -> {
          isConnected = false;
        });
  }

  @Implementation
  protected boolean isConnected() {
    return isConnected;
  }

  @Implementation
  protected String getRoot() {
    if (!isConnected) {
      throw new IllegalStateException("Can't call getRoot() while not connected.");
    }
    return rootId;
  }

  @Implementation
  protected void getItem(@NonNull final String mediaId, @NonNull final ItemCallback cb) {
    // mediaItem will be null when there is no MediaItem that matches the given mediaId.
    final MediaItem mediaItem = mediaItems.get(mediaId);

    if (isConnected && mediaItem != null) {
      handler.post(() -> cb.onItemLoaded(mediaItem));
    } else {
      handler.post(() -> cb.onError(mediaId));
    }
  }

  @Implementation
  protected void subscribe(@NonNull String parentId, @NonNull SubscriptionCallback callback) {
    subscribe(parentId, null, callback);
  }

  @Implementation
  protected void subscribe(
      @NonNull String parentId, @Nullable Bundle options, @NonNull SubscriptionCallback callback) {
    if (isConnected) {
      final MediaItem parentItem = mediaItems.get(parentId);
      List<MediaItem> children =
          mediaItemChildren.get(parentItem) == null
              ? Collections.emptyList()
              : mediaItemChildren.get(parentItem);
      handler.post(
          () -> callback.onChildrenLoaded(parentId, applyOptionsToResults(children, options)));
    } else {
      handler.post(() -> callback.onError(parentId));
    }
  }

  private List<MediaItem> applyOptionsToResults(List<MediaItem> results, final Bundle options) {
    if (results == null || options == null) {
      return results;
    }
    final int resultsSize = results.size();
    final int page = options.getInt(EXTRA_PAGE, -1);
    final int pageSize = options.getInt(EXTRA_PAGE_SIZE, -1);
    if (page == -1 && pageSize == -1) {
      return results;
    }

    final int firstItemIndex = page * pageSize;
    final int lastItemIndex = firstItemIndex + pageSize;
    if (page < 0 || pageSize < 1 || firstItemIndex >= resultsSize) {
      return Collections.emptyList();
    }
    return results.subList(firstItemIndex, Math.min(lastItemIndex, resultsSize));
  }

  /**
   * This differs from real Android search logic. Search results will contain all {@link
   * MediaItem}'s with a title that {@param query} is a substring of.
   */
  @Implementation
  protected void search(
      @NonNull final String query, final Bundle extras, @NonNull SearchCallback callback) {
    if (isConnected) {
      final List<MediaItem> searchResults = new ArrayList<>();
      for (MediaItem item : mediaItems.values()) {
        final String mediaTitle = item.getDescription().getTitle().toString().toLowerCase();
        if (mediaTitle.contains(query.toLowerCase())) {
          searchResults.add(item);
        }
      }
      handler.post(() -> callback.onSearchResult(query, extras, searchResults));
    } else {
      handler.post(() -> callback.onError(query, extras));
    }
  }

  @Implementation
  public MediaSessionCompat.Token getSessionToken() {
    return token;
  }

  /**
   * Sets the root id. Can be called more than once.
   *
   * @param mediaId the id of the root MediaItem. This MediaItem should already have been created.
   */
  public void setRootId(String mediaId) {
    rootId = mediaId;
  }

  /**
   * Creates a MediaItem and returns it.
   *
   * @param parentId the id of the parent MediaItem. If the MediaItem to be created will be the
   *     root, parentId should be null.
   * @param mediaId the id of the MediaItem to be created.
   * @param title the title of the MediaItem to be created.
   * @param flag says if the MediaItem to be created is browsable and/or playable.
   * @return the newly created MediaItem.
   */
  public MediaItem createMediaItem(String parentId, String mediaId, String title, int flag) {
    final MediaMetadataCompat metadataCompat =
        new MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId)
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, Uri.parse(mediaId).toString())
            .build();
    final MediaItem mediaItem = new MediaItem(metadataCompat.getDescription(), flag);
    mediaItems.put(mediaId, mediaItem);

    // If this MediaItem is the child of a MediaItem that has already been created. This applies to
    // all MediaItems except the root.
    if (parentId != null) {
      final MediaItem parentItem = mediaItems.get(parentId);
      List<MediaItem> children = mediaItemChildren.get(parentItem);
      if (children == null) {
        children = new ArrayList<>();
        mediaItemChildren.put(parentItem, children);
      }
      children.add(mediaItem);
    }

    return mediaItem;
  }

  /** @return a copy of the internal {@link Map} that maps {@link MediaItem}s to their children. */
  public Map<MediaItem, List<MediaItem>> getCopyOfMediaItemChildren() {
    final Map<MediaItem, List<MediaItem>> copyOfMediaItemChildren = new LinkedHashMap<>();
    for (MediaItem parent : mediaItemChildren.keySet()) {
      List<MediaItem> children = new ArrayList<>(mediaItemChildren.get(parent));
      copyOfMediaItemChildren.put(parent, children);
    }
    return copyOfMediaItemChildren;
  }
}
