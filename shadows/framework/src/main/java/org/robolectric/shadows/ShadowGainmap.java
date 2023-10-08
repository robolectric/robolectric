package org.robolectric.shadows;

import android.graphics.Bitmap;
import android.graphics.Gainmap;
import android.os.Parcel;
import android.os.Parcelable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.res.android.NativeObjRegistry;
import org.robolectric.versioning.AndroidVersions.U;

/** Fake implementation for Gainmap class. */
@Implements(
    value = Gainmap.class,
    minSdk = U.SDK_INT,
    // TODO: remove when minimum supported compileSdk is >= 34
    isInAndroidSdk = false)
public class ShadowGainmap {

  @RealObject Gainmap realGainmap;

  static final NativeObjRegistry<NativeGainmap> nativeObjectRegistry =
      new NativeObjRegistry<>(NativeGainmap.class);

  @Implementation
  protected static long nCreateEmpty() {
    return nativeObjectRegistry.register(new NativeGainmap());
  }

  @Implementation
  protected static void nSetBitmap(long ptr, Bitmap bitmap) {
    getNativeGainmap(ptr).bitmap = bitmap;
  }

  @Implementation
  protected static void nSetRatioMin(long ptr, float r, float g, float b) {
    getNativeGainmap(ptr).ratioMin = new float[] {r, g, b};
  }

  @Implementation
  protected static void nGetRatioMin(long ptr, float[] components) {
    components[0] = getNativeGainmap(ptr).ratioMin[0];
    components[1] = getNativeGainmap(ptr).ratioMin[1];
    components[2] = getNativeGainmap(ptr).ratioMin[2];
  }

  @Implementation
  protected static void nSetRatioMax(long ptr, float r, float g, float b) {
    getNativeGainmap(ptr).ratioMax = new float[] {r, g, b};
  }

  @Implementation
  protected static void nGetRatioMax(long ptr, float[] components) {
    components[0] = getNativeGainmap(ptr).ratioMax[0];
    components[1] = getNativeGainmap(ptr).ratioMax[1];
    components[2] = getNativeGainmap(ptr).ratioMax[2];
  }

  @Implementation
  protected static void nSetGamma(long ptr, float r, float g, float b) {
    getNativeGainmap(ptr).gamma = new float[] {r, g, b};
  }

  @Implementation
  protected static void nGetGamma(long ptr, float[] components) {
    components[0] = getNativeGainmap(ptr).gamma[0];
    components[1] = getNativeGainmap(ptr).gamma[1];
    components[2] = getNativeGainmap(ptr).gamma[2];
  }

  @Implementation
  protected static void nSetEpsilonSdr(long ptr, float r, float g, float b) {
    getNativeGainmap(ptr).epsilonSdr = new float[] {r, g, b};
  }

  @Implementation
  protected static void nGetEpsilonSdr(long ptr, float[] components) {
    components[0] = getNativeGainmap(ptr).epsilonSdr[0];
    components[1] = getNativeGainmap(ptr).epsilonSdr[1];
    components[2] = getNativeGainmap(ptr).epsilonSdr[2];
  }

  @Implementation
  protected static void nSetEpsilonHdr(long ptr, float r, float g, float b) {
    getNativeGainmap(ptr).epsilonHdr = new float[] {r, g, b};
  }

  @Implementation
  protected static void nGetEpsilonHdr(long ptr, float[] components) {
    components[0] = getNativeGainmap(ptr).epsilonHdr[0];
    components[1] = getNativeGainmap(ptr).epsilonHdr[1];
    components[2] = getNativeGainmap(ptr).epsilonHdr[2];
  }

  @Implementation
  protected static void nSetDisplayRatioHdr(long ptr, float max) {
    getNativeGainmap(ptr).displayRatioHdr = max;
  }

  @Implementation
  protected static float nGetDisplayRatioHdr(long ptr) {
    return getNativeGainmap(ptr).displayRatioHdr;
  }

  @Implementation
  protected static void nSetDisplayRatioSdr(long ptr, float min) {
    getNativeGainmap(ptr).displayRatioSdr = min;
  }

  @Implementation
  protected static float nGetDisplayRatioSdr(long ptr) {
    return getNativeGainmap(ptr).displayRatioSdr;
  }

  @Implementation
  protected static void nWriteGainmapToParcel(long ptr, Parcel dest) {
    if (dest == null) {
      return;
    }
    // write gainmap to parcel
    // ratio min
    dest.writeFloat(getNativeGainmap(ptr).ratioMin[0]);
    dest.writeFloat(getNativeGainmap(ptr).ratioMin[1]);
    dest.writeFloat(getNativeGainmap(ptr).ratioMin[2]);
    // ratio max
    dest.writeFloat(getNativeGainmap(ptr).ratioMax[0]);
    dest.writeFloat(getNativeGainmap(ptr).ratioMax[1]);
    dest.writeFloat(getNativeGainmap(ptr).ratioMax[2]);
    // gamma
    dest.writeFloat(getNativeGainmap(ptr).gamma[0]);
    dest.writeFloat(getNativeGainmap(ptr).gamma[1]);
    dest.writeFloat(getNativeGainmap(ptr).gamma[2]);
    // epsilonsdr
    dest.writeFloat(getNativeGainmap(ptr).epsilonSdr[0]);
    dest.writeFloat(getNativeGainmap(ptr).epsilonSdr[1]);
    dest.writeFloat(getNativeGainmap(ptr).epsilonSdr[2]);
    // epsilonhdr
    dest.writeFloat(getNativeGainmap(ptr).epsilonHdr[0]);
    dest.writeFloat(getNativeGainmap(ptr).epsilonHdr[1]);
    dest.writeFloat(getNativeGainmap(ptr).epsilonHdr[2]);
    // display ratio sdr
    dest.writeFloat(getNativeGainmap(ptr).displayRatioSdr);
    // display ratio hdr
    dest.writeFloat(getNativeGainmap(ptr).displayRatioHdr);
    // base image type
    // TODO: Figure out how to get the BaseImageType
    dest.writeInt(0);
    // type
    // TODO: Figure out how to get the Type
    dest.writeInt(0);
  }

  @Implementation
  protected static void nReadGainmapFromParcel(long ptr, Parcel dest) {
    if (dest == null) {
      return;
    }
    // write gainmap to parcel
    // ratio min
    getNativeGainmap(ptr).ratioMin[0] = dest.readFloat();
    getNativeGainmap(ptr).ratioMin[1] = dest.readFloat();
    getNativeGainmap(ptr).ratioMin[2] = dest.readFloat();
    // ratio max
    getNativeGainmap(ptr).ratioMax[0] = dest.readFloat();
    getNativeGainmap(ptr).ratioMax[1] = dest.readFloat();
    getNativeGainmap(ptr).ratioMax[2] = dest.readFloat();
    // gamma
    getNativeGainmap(ptr).gamma[0] = dest.readFloat();
    getNativeGainmap(ptr).gamma[1] = dest.readFloat();
    getNativeGainmap(ptr).gamma[2] = dest.readFloat();
    // epsilonsdr
    getNativeGainmap(ptr).epsilonSdr[0] = dest.readFloat();
    getNativeGainmap(ptr).epsilonSdr[1] = dest.readFloat();
    getNativeGainmap(ptr).epsilonSdr[2] = dest.readFloat();
    // epsilonhdr
    getNativeGainmap(ptr).epsilonHdr[0] = dest.readFloat();
    getNativeGainmap(ptr).epsilonHdr[1] = dest.readFloat();
    getNativeGainmap(ptr).epsilonHdr[2] = dest.readFloat();
    // display ratio sdr
    getNativeGainmap(ptr).displayRatioSdr = dest.readFloat();
    // display ratio hdr
    getNativeGainmap(ptr).displayRatioHdr = dest.readFloat();
    // base image type (unused in java)
    dest.readInt();
    // type (unused in java)
    dest.readInt();
  }

  private static NativeGainmap getNativeGainmap(long ptr) {
    return nativeObjectRegistry.getNativeObject(ptr);
  }

  public static final Parcelable.Creator<Gainmap> CREATOR =
      new Parcelable.Creator<Gainmap>() {
        @Override
        public Gainmap createFromParcel(Parcel in) {
          in.setDataPosition(0);
          Gainmap gm = new Gainmap(in.readTypedObject(Bitmap.CREATOR));
          return gm;
        }

        @Override
        public Gainmap[] newArray(int size) {
          return new Gainmap[size];
        }
      };

  private static class NativeGainmap {
    public float[] ratioMin = new float[3];
    public float[] ratioMax = new float[3];
    public float[] gamma = new float[3];
    public float[] epsilonSdr = new float[3];
    public float[] epsilonHdr = new float[3];
    public float displayRatioSdr = 1.0f;
    public float displayRatioHdr = 1.0f;
    public Bitmap bitmap = null;
  }
}
