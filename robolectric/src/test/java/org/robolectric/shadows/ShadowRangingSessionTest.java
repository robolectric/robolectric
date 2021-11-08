package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.S;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import android.os.PersistableBundle;
import android.uwb.RangingSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/** Unit tests for {@link ShadowRangingSession}. */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = S)
public class ShadowRangingSessionTest {
  private /* RangingSession.Callback */ Object callbackObject;
  private /* RangingSession */ Object rangingSessionObject;
  private ShadowRangingSession.Adapter adapter;

  @Before
  public void setUp() {
    callbackObject = mock(RangingSession.Callback.class);
    adapter = mock(ShadowRangingSession.Adapter.class);
    rangingSessionObject =
        ShadowRangingSession.newInstance(
            directExecutor(), (RangingSession.Callback) callbackObject, adapter);
  }

  @After
  public void tearDown() {
    verifyNoMoreInteractions(adapter);
  }

  @Test
  public void open_notifyAdaptor() {
    RangingSession session = (RangingSession) rangingSessionObject;
    RangingSession.Callback callback = (RangingSession.Callback) callbackObject;
    Shadow.<ShadowRangingSession>extract(session).open(genParams("open"));
    verify(adapter).onOpen(eq(session), eq(callback), argThat(checkParams("open")));
  }

  @Test
  public void start_notifyAdaptor() {
    RangingSession session = (RangingSession) rangingSessionObject;
    RangingSession.Callback callback = (RangingSession.Callback) callbackObject;
    session.start(genParams("start"));
    verify(adapter).onStart(eq(session), eq(callback), argThat(checkParams("start")));
  }

  @Test
  public void reconfigure_notifyAdaptor() {
    RangingSession session = (RangingSession) rangingSessionObject;
    RangingSession.Callback callback = (RangingSession.Callback) callbackObject;
    session.reconfigure(genParams("reconfigure"));
    verify(adapter).onReconfigure(eq(session), eq(callback), argThat(checkParams("reconfigure")));
  }

  @Test
  public void stop_notifyAdaptor() {
    RangingSession session = (RangingSession) rangingSessionObject;
    RangingSession.Callback callback = (RangingSession.Callback) callbackObject;
    session.stop();
    verify(adapter).onStop(eq(session), eq(callback));
  }

  @Test
  public void close_notifyAdaptor() {
    RangingSession session = (RangingSession) rangingSessionObject;
    RangingSession.Callback callback = (RangingSession.Callback) callbackObject;
    session.close();
    verify(adapter).onClose(eq(session), eq(callback));
  }

  private static PersistableBundle genParams(String name) {
    PersistableBundle params = new PersistableBundle();
    params.putString("test", name);
    return params;
  }

  private static ArgumentMatcher<PersistableBundle> checkParams(String name) {
    return params -> params.getString("test").equals(name);
  }
}
