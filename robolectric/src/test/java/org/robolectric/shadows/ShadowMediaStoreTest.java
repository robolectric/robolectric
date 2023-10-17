package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.TIRAMISU;
import static android.provider.MediaStore.Images;
import static android.provider.MediaStore.Video;
import static com.google.common.truth.Truth.assertThat;

import android.content.ContentResolver;
import android.provider.MediaStore;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowMediaStoreTest {
  private static final String AUTHORITY = "authority";
  private static final String INCORRECT_AUTHORITY = "incorrect_authority";
  private static final String CURRENT_MEDIA_COLLECTION_ID = "media_collection_id";
  private final ContentResolver resolver =
      ApplicationProvider.getApplicationContext().getContentResolver();

  @Test
  public void shouldInitializeFields() {
    assertThat(Images.Media.EXTERNAL_CONTENT_URI.toString())
        .isEqualTo("content://media/external/images/media");
    assertThat(Images.Media.INTERNAL_CONTENT_URI.toString())
        .isEqualTo("content://media/internal/images/media");
    assertThat(Video.Media.EXTERNAL_CONTENT_URI.toString())
        .isEqualTo("content://media/external/video/media");
    assertThat(Video.Media.INTERNAL_CONTENT_URI.toString())
        .isEqualTo("content://media/internal/video/media");
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void notifyCloudMediaChangedEvent_storesCloudMediaChangedEvent() {
    MediaStore.notifyCloudMediaChangedEvent(resolver, AUTHORITY, CURRENT_MEDIA_COLLECTION_ID);

    ImmutableList<ShadowMediaStore.CloudMediaChangedEvent> cloudMediaChangedEventList =
        ShadowMediaStore.getCloudMediaChangedEvents();
    assertThat(cloudMediaChangedEventList).hasSize(1);
    assertThat(cloudMediaChangedEventList.get(0).authority()).isEqualTo(AUTHORITY);
    assertThat(cloudMediaChangedEventList.get(0).currentMediaCollectionId())
        .isEqualTo(CURRENT_MEDIA_COLLECTION_ID);
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void clearCloudMediaChangedEventList_clearsCloudMediaChangedEventList() {
    MediaStore.notifyCloudMediaChangedEvent(resolver, AUTHORITY, CURRENT_MEDIA_COLLECTION_ID);
    assertThat(ShadowMediaStore.getCloudMediaChangedEvents()).isNotEmpty();

    ShadowMediaStore.clearCloudMediaChangedEventList();

    assertThat(ShadowMediaStore.getCloudMediaChangedEvents()).isEmpty();
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void isSupportedCloudMediaProviderAuthority_withCorrectAuthority_returnsTrue() {
    List<String> supportedAuthorityList = new ArrayList<>();
    supportedAuthorityList.add(AUTHORITY);
    ShadowMediaStore.addSupportedCloudMediaProviderAuthorities(supportedAuthorityList);

    assertThat(MediaStore.isSupportedCloudMediaProviderAuthority(resolver, AUTHORITY)).isTrue();
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void isSupportedCloudMediaProviderAuthority_withIncorrectAuthority_returnsFalse() {
    List<String> supportedAuthorityList = new ArrayList<>();
    supportedAuthorityList.add(AUTHORITY);
    ShadowMediaStore.addSupportedCloudMediaProviderAuthorities(supportedAuthorityList);

    assertThat(MediaStore.isSupportedCloudMediaProviderAuthority(resolver, INCORRECT_AUTHORITY))
        .isFalse();
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void isCurrentCloudMediaProviderAuthority_withCorrectAuthority_returnsTrue() {
    ShadowMediaStore.setCurrentCloudMediaProviderAuthority(AUTHORITY);

    assertThat(MediaStore.isCurrentCloudMediaProviderAuthority(resolver, AUTHORITY)).isTrue();
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void isCurrentCloudMediaProviderAuthority_withIncorrectAuthority_returnsFalse() {
    ShadowMediaStore.setCurrentCloudMediaProviderAuthority(AUTHORITY);

    assertThat(MediaStore.isCurrentCloudMediaProviderAuthority(resolver, INCORRECT_AUTHORITY))
        .isFalse();
  }
}
