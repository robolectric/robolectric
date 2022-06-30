package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.Nullable;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = M)
public class ShadowIconTest {

  private static final int MSG_ICON_LOADED = 312;

  public static final int TYPE_BITMAP = 1;
  public static final int TYPE_RESOURCE = 2;
  public static final int TYPE_DATA = 3;
  public static final int TYPE_URI = 4;

  @Nullable private Drawable loadedDrawable;

  private final Context appContext = ApplicationProvider.getApplicationContext();
  private final Handler mainHandler =
      new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
          if (msg.what == MSG_ICON_LOADED) {
            loadedDrawable = (Drawable) msg.obj;
          }
        }
      };

  @Test
  public void testGetRes() {
    Icon icon = Icon.createWithResource(appContext, android.R.drawable.ic_delete);
    assertThat(shadowOf(icon).getType()).isEqualTo(TYPE_RESOURCE);
    assertThat(shadowOf(icon).getResId()).isEqualTo(android.R.drawable.ic_delete);
  }

  @Test
  public void testGetBitmap() {
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    Icon icon = Icon.createWithBitmap(bitmap);
    assertThat(shadowOf(icon).getType()).isEqualTo(TYPE_BITMAP);
    assertThat(shadowOf(icon).getBitmap().sameAs(bitmap)).isTrue();
  }

  @Test
  public void testGetData() {
    byte[] data = new byte[1000];
    Icon icon = Icon.createWithData(data, 100, 200);
    assertThat(shadowOf(icon).getType()).isEqualTo(TYPE_DATA);
    assertThat(shadowOf(icon).getDataBytes()).isEqualTo(data);
    assertThat(shadowOf(icon).getDataOffset()).isEqualTo(100);
    assertThat(shadowOf(icon).getDataLength()).isEqualTo(200);
  }

  @Test
  public void testGetUri() {
    Uri uri = Uri.parse("content://icons/icon");
    Icon icon = Icon.createWithContentUri(uri);
    assertThat(shadowOf(icon).getType()).isEqualTo(TYPE_URI);
    assertThat(shadowOf(icon).getUri()).isEqualTo(uri);
  }

  @Test
  public void testLoadDrawableAsyncWithMessage() {
    ShadowIcon.overrideExecutor(directExecutor());

    Icon icon = Icon.createWithResource(appContext, android.R.drawable.ic_delete);

    Message andThen = Message.obtain(mainHandler, MSG_ICON_LOADED);

    icon.loadDrawableAsync(appContext, andThen);
    ShadowLooper.idleMainLooper();

    assertThat(shadowOf(loadedDrawable).getCreatedFromResId())
        .isEqualTo(android.R.drawable.ic_delete);
  }

  @Test
  public void testLoadDrawableAsyncWithListener() {
    ShadowIcon.overrideExecutor(directExecutor());

    Icon icon = Icon.createWithResource(appContext, android.R.drawable.ic_delete);

    icon.loadDrawableAsync(appContext, drawable -> this.loadedDrawable = drawable, mainHandler);
    ShadowLooper.idleMainLooper();

    assertThat(shadowOf(loadedDrawable).getCreatedFromResId())
        .isEqualTo(android.R.drawable.ic_delete);
  }
}
