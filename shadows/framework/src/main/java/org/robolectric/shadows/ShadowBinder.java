package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;

import android.os.Binder;
import android.os.IBinder.DeathRecipient;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.UserHandle;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import javax.annotation.Nullable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;

@Implements(Binder.class)
public class ShadowBinder {
  @RealObject Binder realObject;

  // Global calling identity
  private static Integer callingUid;
  private static Integer callingPid;
  private static UserHandle callingUserHandle;

  // Per-thread calling identities. We use a map here instead of ThreadLocal so that reset() can
  // cleanly clear the fake calling identity for all threads. Weak references do not create a GC
  // non-determinism problem here because active threads are strongly referenced by the JVM, a
  // Thread's reference identity remains unique even after termination.
  private static final Map<Thread, CallingIdentity> callingIdsByThread =
      Collections.synchronizedMap(new WeakHashMap<>());

  private final List<WeakReference<DeathRecipient>> deathRecipients = new ArrayList<>();

  @Implementation
  protected boolean transact(int code, Parcel data, Parcel reply, int flags)
      throws RemoteException {
    if (data != null) {
      data.setDataPosition(0);
    }

    boolean result;
    try {
      result = new ShadowBinderBridge(realObject).onTransact(code, data, reply, flags);
    } catch (RemoteException e) {
      throw e;
    } catch (Exception e) {
      result = true;
      if (reply != null) {
        reply.writeException(e);
      }
    }

    if (reply != null) {
      reply.setDataPosition(0);
    }
    return result;
  }

  @Implementation
  protected void linkToDeath(DeathRecipient deathRecipient, int flags) {
    // The caller must hold a strong reference, the binder does not.
    deathRecipients.add(new WeakReference<>(deathRecipient));
  }

  @Implementation
  protected boolean unlinkToDeath(DeathRecipient deathRecipient, int flags) {
    WeakReference<DeathRecipient> itemToRemove = null;
    for (WeakReference<DeathRecipient> item : deathRecipients) {
      // If the same recipient is registered twice, it must be unregistered twice as well.
      if (item.get() == deathRecipient) {
        itemToRemove = item;
        break;
      }
    }
    if (itemToRemove != null) {
      deathRecipients.remove(itemToRemove);
      return true;
    } else {
      return false;
    }
  }

  @Implementation
  protected static int getCallingPid() {
    CallingIdentity tlIdentity = callingIdsByThread.get(Thread.currentThread());
    if (tlIdentity != null) {
      return tlIdentity.pid;
    }
    if (callingPid != null) {
      return callingPid;
    }
    return android.os.Process.myPid();
  }

  @Implementation
  protected static int getCallingUid() {
    CallingIdentity tlIdentity = callingIdsByThread.get(Thread.currentThread());
    if (tlIdentity != null) {
      return tlIdentity.uid;
    }
    if (callingUid != null) {
      return callingUid;
    }
    return android.os.Process.myUid();
  }

  /**
   * See {@link Binder#getCallingUidOrThrow()}. Whether or not this returns a value is controlled by
   * {@link #setCallingUid(int)} (to set the value to be returned) or by {@link #reset()} (to
   * trigger the exception).
   *
   * @return the value set by {@link #setCallingUid(int)}
   * @throws IllegalStateException if no UID has been set
   */
  @Implementation(minSdk = Q)
  protected static int getCallingUidOrThrow() {
    CallingIdentity tlIdentity = callingIdsByThread.get(Thread.currentThread());
    if (tlIdentity != null) {
      return tlIdentity.uid;
    }
    if (callingUid != null) {
      return callingUid;
    }

    // Typo in "transaction" intentional to match platform
    throw new IllegalStateException("Thread is not in a binder transcation");
  }

  @Implementation
  protected static UserHandle getCallingUserHandle() {
    CallingIdentity tlIdentity = callingIdsByThread.get(Thread.currentThread());
    if (tlIdentity != null) {
      return tlIdentity.userHandle;
    }
    if (callingUserHandle != null) {
      return callingUserHandle;
    }
    return android.os.Process.myUserHandle();
  }

  public List<DeathRecipient> getDeathRecipients() {
    return deathRecipients.stream()
        .map(Reference::get)
        // References that have been collected will be null.
        .filter(Objects::nonNull)
        .toList();
  }

  /**
   * Configures {@link android.os.Binder#getCallingPid} to return the specified Linux PID to
   * subsequent callers on *any* thread, for testing purposes.
   *
   * <p>Warning: This is a global setting affecting all threads in your test process (very different
   * than real Android). It can cause flakiness or unexpected behavior in multi-threaded tests.
   * Prefer to configure your simulated Binder IPC callers one at a time using {@link
   * #setCallingIdentityForCurrentThread(CallingIdentity)} instead.
   */
  public static void setCallingPid(int pid) {
    ShadowBinder.callingPid = pid;
  }

  /**
   * Configures {@link android.os.Binder#getCallingUid} to return the specified Linux UID to
   * subsequent callers on *any* thread, for testing purposes.
   *
   * <p>Warning: This is a global setting affecting all threads in your test process (very different
   * than real Android). It can cause flakiness or unexpected behavior in multi-threaded tests.
   * Prefer to configure your simulated Binder IPC callers one at a time using {@link
   * #setCallingIdentityForCurrentThread(CallingIdentity)} instead.
   */
  public static void setCallingUid(int uid) {
    ShadowBinder.callingUid = uid;
  }

  /**
   * Configures {@link android.os.Binder#getCallingUserHandle} to return the specified {@link
   * UserHandle} to subsequent callers on *any* thread, for testing purposes.
   *
   * <p>Warning: This is a global setting affecting all threads in your test process (very different
   * than real Android). It can cause flakiness or unexpected behavior in multi-threaded tests.
   * Prefer to configure your simulated Binder IPC callers one at a time using {@link
   * #setCallingIdentityForCurrentThread(CallingIdentity)} instead.
   */
  public static void setCallingUserHandle(UserHandle userHandle) {
    ShadowBinder.callingUserHandle = userHandle;
  }

  /**
   * Configures all the {@code android.os.Binder#getCallingXXX} methods to reflect the specified
   * {@link CallingIdentity} when called from the current thread, for testing purposes.
   *
   * <p>Use this method to prepare the current thread to deliver a Binder IPC transaction under a
   * fake remote identity. You can configure this thread's apparent UID, PID and UserHandle. Call
   * this method again (unconditionally, in a finally block, say) after the transaction completes to
   * restore this thread's previous IPC identity (or clear it).
   *
   * @param identity the identity to set, or {@code null} to clear
   * @return this thread's previous fake identity, or {@code null} if none was set.
   */
  @Nullable
  public static CallingIdentity setCallingIdentityForCurrentThread(
      @Nullable CallingIdentity identity) {
    if (identity == null) {
      return callingIdsByThread.remove(Thread.currentThread());
    } else {
      return callingIdsByThread.put(Thread.currentThread(), identity);
    }
  }

  @Resetter
  public static void reset() {
    ShadowBinder.callingPid = null;
    ShadowBinder.callingUid = null;
    ShadowBinder.callingUserHandle = null;
    callingIdsByThread.clear();
  }

  /** Represents the faked calling identity (UID, PID, UserHandle) of a remote Binder IPC caller. */
  public static final class CallingIdentity {
    private final int uid;
    private final int pid;
    private final UserHandle userHandle;

    private CallingIdentity(Builder builder) {
      this.uid = builder.uid;
      this.pid = builder.pid;
      this.userHandle = builder.userHandle;
    }

    /** Returns the caller's UID. */
    public int getUid() {
      return uid;
    }

    /** Returns the caller's PID. */
    public int getPid() {
      return pid;
    }

    /** Returns the caller's UserHandle. */
    public UserHandle getUserHandle() {
      return userHandle;
    }

    /**
     * Creates a new {@link Builder} pre-populated with the current process defaults: {@link
     * android.os.Process#myUid()}, {@link android.os.Process#myPid()}, and {@link
     * android.os.Process#myUserHandle()}.
     */
    public static Builder newBuilder() {
      return new Builder();
    }

    /** Creates a new {@link Builder} initialized with the values of this Binder identity. */
    public Builder toBuilder() {
      return new Builder(this);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      CallingIdentity identity = (CallingIdentity) o;
      return uid == identity.uid
          && pid == identity.pid
          && Objects.equals(userHandle, identity.userHandle);
    }

    @Override
    public int hashCode() {
      return Objects.hash(uid, pid, userHandle);
    }

    @Override
    public String toString() {
      return "CallingIdentity{"
          + "uid="
          + uid
          + ", pid="
          + pid
          + ", userHandle="
          + userHandle
          + '}';
    }

    /** Builder for {@link CallingIdentity}. */
    public static final class Builder {
      private int uid;
      private int pid;
      private UserHandle userHandle;

      private Builder() {
        this.uid = android.os.Process.myUid();
        this.pid = android.os.Process.myPid();
        this.userHandle = android.os.Process.myUserHandle();
      }

      private Builder(CallingIdentity identity) {
        this.uid = identity.uid;
        this.pid = identity.pid;
        this.userHandle = identity.userHandle;
      }

      /**
       * Sets this caller's Linux UID.
       *
       * <p>If not set, defaults to {@link android.os.Process#myUid()}.
       */
      @CanIgnoreReturnValue
      public Builder setUid(int uid) {
        this.uid = uid;
        return this;
      }

      /**
       * Sets this caller's Linux PID.
       *
       * <p>If not set, defaults to {@link android.os.Process#myPid()}.
       */
      @CanIgnoreReturnValue
      public Builder setPid(int pid) {
        this.pid = pid;
        return this;
      }

      /**
       * Sets this caller's {@link UserHandle}.
       *
       * <p>If not set, defaults to {@link android.os.Process#myUserHandle()}.
       */
      @CanIgnoreReturnValue
      public Builder setUserHandle(UserHandle userHandle) {
        this.userHandle = Objects.requireNonNull(userHandle);
        return this;
      }

      /** Builds the {@link CallingIdentity}. */
      public CallingIdentity build() {
        return new CallingIdentity(this);
      }
    }
  }
}
