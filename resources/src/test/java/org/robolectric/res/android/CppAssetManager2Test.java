package org.robolectric.res.android;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.res.android.ApkAssetsCookie.K_INVALID_COOKIE;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.res.android.CppAssetManager2.ResolvedValue;
import org.robolectric.res.android.ResourceTypes.Res_value;

@RunWith(JUnit4.class)
public class CppAssetManager2Test {

  private static final int RES_ID_REF = 0x7f010001;
  private static final int RES_ID_CHAIN_START = 0x7f010002;
  private static final int RES_ID_CHAIN_MID = 0x7f010003;

  private final AtomicInteger getResourceCallCount = new AtomicInteger(0);
  private CppAssetManager2 assetManager;

  @Before
  public void setUp() {
    getResourceCallCount.set(0);
    assetManager =
        new CppAssetManager2() {
          @Override
          public ApkAssetsCookie GetResource(
              int resid,
              boolean mayBeBag,
              short densityOverride,
              Ref<Res_value> outValue,
              Ref<ResTable_config> outSelectedConfig,
              Ref<Integer> outFlags) {
            getResourceCallCount.incrementAndGet();
            if (resid == RES_ID_REF) {
              outValue.set(new Res_value((byte) Res_value.TYPE_STRING, 0x1234));
              if (outFlags != null) {
                outFlags.set(0x20);
              }
              return ApkAssetsCookie.forInt(1);
            } else if (resid == RES_ID_CHAIN_START) {
              // Points to mid reference
              outValue.set(new Res_value((byte) Res_value.TYPE_REFERENCE, RES_ID_CHAIN_MID));
              if (outFlags != null) {
                outFlags.set(0x02);
              }
              return ApkAssetsCookie.forInt(1);
            } else if (resid == RES_ID_CHAIN_MID) {
              // Points to final string value
              outValue.set(new Res_value((byte) Res_value.TYPE_STRING, 0x5678));
              if (outFlags != null) {
                outFlags.set(0x04);
              }
              return ApkAssetsCookie.forInt(1);
            }
            return K_INVALID_COOKIE;
          }
        };
  }

  @Test
  public void resolveReference_cachesResolvedValue() {
    Ref<Res_value> value = new Ref<>(new Res_value((byte) Res_value.TYPE_REFERENCE, RES_ID_REF));
    Ref<ResTable_config> selectedConfig = new Ref<>(new ResTable_config());
    Ref<Integer> flags = new Ref<>(0x10);
    Ref<Integer> lastRef = new Ref<>(0);

    assertThat(assetManager.cachedResolvedValues).isEmpty();

    ApkAssetsCookie cookie =
        assetManager.resolveReference(
            ApkAssetsCookie.forInt(0),
            value,
            selectedConfig,
            flags,
            lastRef,
            true /* cache_value */);

    assertThat(cookie).isEqualTo(ApkAssetsCookie.forInt(1));
    assertThat(value.get().dataType).isEqualTo((byte) Res_value.TYPE_STRING);
    assertThat(value.get().data).isEqualTo(0x1234);
    assertThat(flags.get()).isEqualTo(0x10 | 0x20);
    assertThat(lastRef.get()).isEqualTo(RES_ID_REF);
    assertThat(getResourceCallCount.get()).isEqualTo(1);

    // Validate that the resource was added to the cache
    assertThat(assetManager.cachedResolvedValues).containsKey(RES_ID_REF);
    ResolvedValue cached = assetManager.cachedResolvedValues.get(RES_ID_REF);
    assertThat(cached.cookie).isEqualTo(ApkAssetsCookie.forInt(1));
    assertThat(cached.value.dataType).isEqualTo((byte) Res_value.TYPE_STRING);
    assertThat(cached.value.data).isEqualTo(0x1234);
    // Cached flags should NOT contain the caller's initial flags (0x10)
    assertThat(cached.flags).isEqualTo(0x20);
    assertThat(cached.lastReference).isEqualTo(RES_ID_REF);
  }

  @Test
  public void resolveReference_subsequentCallHitsCache() {
    // First resolution: populates cache
    Ref<Res_value> value1 = new Ref<>(new Res_value((byte) Res_value.TYPE_REFERENCE, RES_ID_REF));
    Ref<Integer> flags1 = new Ref<>(0x10);
    assetManager.resolveReference(
        ApkAssetsCookie.forInt(0), value1, null, flags1, null, true /* cache_value */);
    assertThat(getResourceCallCount.get()).isEqualTo(1);

    // Second resolution: should hit cache and NOT invoke GetResource
    Ref<Res_value> value2 = new Ref<>(new Res_value((byte) Res_value.TYPE_REFERENCE, RES_ID_REF));
    Ref<Integer> flags2 = new Ref<>(0x40); // different initial flags
    Ref<Integer> lastRef2 = new Ref<>(0);
    ApkAssetsCookie cookie2 =
        assetManager.resolveReference(
            ApkAssetsCookie.forInt(0), value2, null, flags2, lastRef2, true /* cache_value */);

    assertThat(getResourceCallCount.get()).isEqualTo(1); // No additional call to GetResource
    assertThat(cookie2).isEqualTo(ApkAssetsCookie.forInt(1));
    assertThat(value2.get().dataType).isEqualTo((byte) Res_value.TYPE_STRING);
    assertThat(value2.get().data).isEqualTo(0x1234);
    // Caller's initial flags (0x40) are combined with cached resource flags (0x20),
    // and caller 1's flags (0x10) do NOT leak into caller 2
    assertThat(flags2.get()).isEqualTo(0x40 | 0x20);
    assertThat(lastRef2.get()).isEqualTo(RES_ID_REF);
  }

  @Test
  public void resolveReference_multiHopChain_cachesInitialResIdToUltimateValue() {
    Ref<Res_value> value =
        new Ref<>(new Res_value((byte) Res_value.TYPE_REFERENCE, RES_ID_CHAIN_START));
    Ref<Integer> flags = new Ref<>(0x01);
    Ref<Integer> lastRef = new Ref<>(0);

    ApkAssetsCookie cookie =
        assetManager.resolveReference(
            ApkAssetsCookie.forInt(0), value, null, flags, lastRef, true /* cache_value */);

    assertThat(cookie).isEqualTo(ApkAssetsCookie.forInt(1));
    assertThat(value.get().dataType).isEqualTo((byte) Res_value.TYPE_STRING);
    assertThat(value.get().data).isEqualTo(0x5678);
    assertThat(flags.get()).isEqualTo(0x01 | 0x02 | 0x04);
    assertThat(lastRef.get()).isEqualTo(RES_ID_CHAIN_MID);
    // Invoked GetResource twice: once for START -> MID, once for MID -> final value
    assertThat(getResourceCallCount.get()).isEqualTo(2);

    // Initial resid is cached with cumulative flags (0x02 | 0x04 = 0x06) and final value
    assertThat(assetManager.cachedResolvedValues).containsKey(RES_ID_CHAIN_START);
    ResolvedValue cached = assetManager.cachedResolvedValues.get(RES_ID_CHAIN_START);
    assertThat(cached.value.dataType).isEqualTo((byte) Res_value.TYPE_STRING);
    assertThat(cached.value.data).isEqualTo(0x5678);
    assertThat(cached.flags).isEqualTo(0x02 | 0x04);

    // Subsequent resolution of RES_ID_CHAIN_START hits cache in 1 step without calling GetResource
    Ref<Res_value> value2 =
        new Ref<>(new Res_value((byte) Res_value.TYPE_REFERENCE, RES_ID_CHAIN_START));
    Ref<Integer> flags2 = new Ref<>(0x10);
    assetManager.resolveReference(
        ApkAssetsCookie.forInt(0), value2, null, flags2, null, true /* cache_value */);
    assertThat(getResourceCallCount.get()).isEqualTo(2); // Still 2 calls
    assertThat(value2.get().data).isEqualTo(0x5678);
    assertThat(flags2.get()).isEqualTo(0x10 | 0x06);
  }

  @Test
  public void resolveReference_withoutCache_doesNotPopulateCache() {
    Ref<Res_value> value = new Ref<>(new Res_value((byte) Res_value.TYPE_REFERENCE, RES_ID_REF));
    Ref<Integer> flags = new Ref<>(0);
    assetManager.resolveReference(
        ApkAssetsCookie.forInt(0), value, null, flags, null, false /* cache_value */);

    assertThat(getResourceCallCount.get()).isEqualTo(1);
    assertThat(assetManager.cachedResolvedValues).isEmpty();
  }

  @Test
  public void invalidateCaches_clearsResolvedValuesCache() {
    Ref<Res_value> value = new Ref<>(new Res_value((byte) Res_value.TYPE_REFERENCE, RES_ID_REF));
    assetManager.resolveReference(
        ApkAssetsCookie.forInt(0), value, null, null, null, true /* cache_value */);
    assertThat(assetManager.cachedResolvedValues).isNotEmpty();

    assetManager.invalidateCaches(0);

    assertThat(assetManager.cachedResolvedValues).isEmpty();

    // After invalidation, subsequent call re-invokes GetResource
    Ref<Res_value> value2 = new Ref<>(new Res_value((byte) Res_value.TYPE_REFERENCE, RES_ID_REF));
    assetManager.resolveReference(
        ApkAssetsCookie.forInt(0), value2, null, null, null, true /* cache_value */);
    assertThat(getResourceCallCount.get()).isEqualTo(2);
  }

  @Test
  public void resolveReference_nonReferenceType_doesNotCache() {
    Ref<Res_value> value = new Ref<>(new Res_value((byte) Res_value.TYPE_INT_DEC, 42));
    assetManager.resolveReference(
        ApkAssetsCookie.forInt(0), value, null, null, null, true /* cache_value */);

    assertThat(getResourceCallCount.get()).isEqualTo(0);
    assertThat(assetManager.cachedResolvedValues).isEmpty();
  }

  @Test
  public void resolveReference_zeroData_doesNotCache() {
    Ref<Res_value> value = new Ref<>(new Res_value((byte) Res_value.TYPE_REFERENCE, 0));
    assetManager.resolveReference(
        ApkAssetsCookie.forInt(0), value, null, null, null, true /* cache_value */);

    assertThat(getResourceCallCount.get()).isEqualTo(0);
    assertThat(assetManager.cachedResolvedValues).isEmpty();
  }
}
