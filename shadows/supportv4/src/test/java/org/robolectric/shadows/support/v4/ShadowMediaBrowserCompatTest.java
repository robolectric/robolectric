package org.robolectric.shadows.support.v4;

import static android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_BROWSABLE;
import static android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.shadows.support.v4.Shadows.shadowOf;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

/** Tests for {@link org.robolectric.shadows.support.v4.ShadowMediaBrowserCompat}. */
@RunWith(RobolectricTestRunner.class)
public class ShadowMediaBrowserCompatTest {

  private Context context = RuntimeEnvironment.application;
  private MediaBrowserCompat mediaBrowser;
  private ShadowMediaBrowserCompat shadow;

  private MediaItem root;
  private MediaItem child;

  private final MediaItemCallBack mediaItemCallBack = new MediaItemCallBack();
  private final MediaSubscriptionCallback mediaSubscriptionCallback =
      new MediaSubscriptionCallback();
  private final MediaSearchCallback mediaSearchCallback = new MediaSearchCallback();

  private static final String ROOT_ID = "root_id";
  private static final String CHILD_ID = "child_id";

  @Before
  public void setUp() {
    final ComponentName componentName = new ComponentName("a", "b");
    mediaBrowser =
        new MediaBrowserCompat(
            context, componentName, new MediaBrowserCompat.ConnectionCallback(), null);
    shadow = shadowOf(mediaBrowser);
    mediaBrowser.connect();
    root = shadow.createMediaItem(null, ROOT_ID, "root_title", FLAG_BROWSABLE);
    shadow.setRootId(ROOT_ID);
    child = shadow.createMediaItem(ROOT_ID, CHILD_ID, "child_title", FLAG_PLAYABLE);
  }

  @Test
  public void mediaBrowserConnection_isConnected() {
    assertThat(mediaBrowser.isConnected()).isTrue();
  }

  @Test
  public void mediaBrowserConnection_isDisconnected() {
    mediaBrowser.disconnect();
    assertThat(mediaBrowser.isConnected()).isFalse();
  }

  @Test
  public void mediaBrowser_getRootId() {
    String mediaBrowserRootId = mediaBrowser.getRoot();
    assertThat(mediaBrowserRootId).isEqualTo(ROOT_ID);
  }

  @Test
  public void mediaBrowser_getItem() {
    mediaBrowser.getItem(ROOT_ID, mediaItemCallBack);
    assertThat(mediaItemCallBack.getMediaItem()).isEqualTo(root);

    mediaItemCallBack.mediaItem = null;
    mediaBrowser.getItem("fake_id", mediaItemCallBack);
    assertThat(mediaItemCallBack.getMediaItem()).isNull();
  }

  @Test
  public void mediaBrowser_subscribe() {
    mediaBrowser.subscribe(ROOT_ID, mediaSubscriptionCallback);
    assertThat(mediaSubscriptionCallback.getResults()).isEqualTo(Collections.singletonList(child));

    mediaBrowser.subscribe(CHILD_ID, mediaSubscriptionCallback);
    assertThat(mediaSubscriptionCallback.getResults()).isEmpty();

    mediaBrowser.subscribe("fake_id", mediaSubscriptionCallback);
    assertThat(mediaSubscriptionCallback.getResults()).isEmpty();
  }

  @Test
  public void mediaBrowser_search() {
    mediaBrowser.search("root", null, mediaSearchCallback);
    assertThat(mediaSearchCallback.getResults()).isEqualTo(Collections.singletonList(root));

    mediaBrowser.search("title", null, mediaSearchCallback);
    final List<MediaItem> expectedResults = Arrays.asList(root, child);
    assertThat(mediaSearchCallback.getResults()).isEqualTo(expectedResults);

    mediaBrowser.search("none", null, mediaSearchCallback);
    assertThat(mediaSearchCallback.getResults()).isEmpty();
  }

  private static class MediaSearchCallback extends MediaBrowserCompat.SearchCallback {

    List<MediaItem> results;

    @Override
    public void onSearchResult(
        @NonNull String query, Bundle bundle, @NonNull List<MediaItem> list) {
      super.onSearchResult(query, bundle, list);
      results = list;
    }

    public List<MediaItem> getResults() {
      return results;
    }
  }

  private static class MediaSubscriptionCallback extends MediaBrowserCompat.SubscriptionCallback {

    List<MediaItem> results;

    @Override
    public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaItem> list) {
      super.onChildrenLoaded(parentId, list);
      results = list;
    }

    public List<MediaItem> getResults() {
      return results;
    }
  }

  private static class MediaItemCallBack extends MediaBrowserCompat.ItemCallback {

    MediaItem mediaItem;

    @Override
    public void onItemLoaded(MediaItem mediaItem) {
      super.onItemLoaded(mediaItem);
      this.mediaItem = mediaItem;
    }

    MediaItem getMediaItem() {
      return mediaItem;
    }
  }
}
