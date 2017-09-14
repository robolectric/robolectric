package org.robolectric.res.android;

import static org.robolectric.res.android.Errors.BAD_INDEX;
import static org.robolectric.res.android.Errors.NO_ERROR;
import static org.robolectric.res.android.ResTable.Res_GETENTRY;
import static org.robolectric.res.android.ResTable.Res_GETPACKAGE;
import static org.robolectric.res.android.ResTable.Res_GETTYPE;
import static org.robolectric.res.android.Util.ALOGE;
import static org.robolectric.res.android.Util.ALOGI;
import static org.robolectric.res.android.Util.ALOGV;
import static org.robolectric.res.android.Util.ALOGW;

import java.util.List;
import org.robolectric.res.android.ResTable.PackageGroup;
import org.robolectric.res.android.ResTable.Type;
import org.robolectric.res.android.ResTable.bag_entry;

public class ResTableTheme {

  private ResTable mTable;
  private boolean kDebugTableTheme = true;
  private boolean kDebugTableNoisy = true;
  private package_info[] mPackages = new package_info[Res_MAXPACKAGE];
  private Ref<Integer> mTypeSpecFlags = new Ref<>(0);

  public ResTableTheme(ResTable resources) {
    this.mTable = resources;
  }

  public ResTable getResTable() {
    return this.mTable;
  }

  public int getAttribute(int resID, Ref<ResValue> valueRef,
      Ref<Integer> outTypeSpecFlags) {
    int cnt = 20;

    if (outTypeSpecFlags != null) outTypeSpecFlags.set(0);

    do {
        final int p = mTable.getResourcePackageIndex(resID);
        final int t = Res_GETTYPE(resID);
        final int e = Res_GETENTRY(resID);

      if (kDebugTableTheme) {
        ALOGI("Looking up attr 0x%08x in theme %s", resID, this);
      }

      if (p >= 0) {
        final package_info pi = mPackages[p];
        if (kDebugTableTheme) {
          ALOGI("Found package: %s", pi);
        }
        if (pi != null) {
          if (kDebugTableTheme) {
            ALOGI("Desired type index is %d in avail %d", t, Res_MAXTYPE + 1);
          }
          if (t <= Res_MAXTYPE) {
            final type_info ti = pi.types[t];
            if (kDebugTableTheme) {
              ALOGI("Desired entry index is %d in avail %d", e, ti.numEntries);
            }
            if (e < ti.numEntries) {
              final theme_entry te = ti.entries[e];
              if (outTypeSpecFlags != null) {
                outTypeSpecFlags.set(outTypeSpecFlags.get() | te.typeSpecFlags);
              }
              if (kDebugTableTheme) {
                ALOGI("Theme value: type=0x%x, data=0x%08x",
                    te.value.dataType, te.value.data);
              }
              final int type = te.value.dataType;
              if (type == DataType.ATTRIBUTE.code()) {
                if (cnt > 0) {
                  cnt--;
                  resID = te.value.data;
                  continue;
                }
                ALOGW("Too many attribute references, stopped at: 0x%08x\n", resID);
                return BAD_INDEX;
              } else if (type != DataType.NULL.code()) {
                valueRef.set(te.value);
                return te.stringBlock;
              }
              return BAD_INDEX;
            }
          }
        }
      }
      break;

    } while (true);

    return BAD_INDEX;

  }

  public int resolveAttributeReference(Ref<ResValue> value, int block, Ref<Integer> resid,
      Ref<Integer> typeSetFlags, Ref<ResTableConfig> config) {
    return -1;
  }

  public int applyStyle(int resID, boolean force) {
    final Ref<bag_entry[]> bag = new Ref<>(null);
    Ref<Integer> bagTypeSpecFlags = new Ref(0);
    mTable.lock();
    final int N = mTable.getBagLocked(resID, bag, bagTypeSpecFlags);
    if (kDebugTableNoisy) {
      ALOGV("Applying style 0x%08x to theme %s, count=%d", resID, this, N);
    }
    if (N < 0) {
      mTable.unlock();
      return N;
    }

    mTypeSpecFlags.set(mTypeSpecFlags.get() | bagTypeSpecFlags.get());

    int curPackage = 0xffffffff;
    int curPackageIndex = 0;
    package_info curPI = null;
    int curType = 0xffffffff;
    int numEntries = 0;
    theme_entry[] curEntries = null;

    final int end = N;
    int bagIndex = 0;
    while (bagIndex < end) {
        final int attrRes = bag.get()[bagIndex].map.nameIdent;
        final int p = Res_GETPACKAGE(attrRes);
        final int t = Res_GETTYPE(attrRes);
        final int e = Res_GETENTRY(attrRes);

      if (curPackage != p) {
        final int pidx = mTable.getResourcePackageIndex(attrRes);
        if (pidx < 0) {
          ALOGE("Style contains key with bad package: 0x%08x\n", attrRes);
          bagIndex++;
          continue;
        }
        curPackage = p;
        curPackageIndex = pidx;
        curPI = mPackages[pidx];
        if (curPI == null) {
          curPI = new package_info();
          mPackages[pidx] = curPI;
        }
        curType = 0xffffffff;
      }
      if (curType != t) {
        if (t > Res_MAXTYPE) {
          ALOGE("Style contains key with bad type: 0x%08x\n", attrRes);
          bagIndex++;
          continue;
        }
        curType = t;
        curEntries = curPI.types[t] != null ? curPI.types[t].entries: null;
        if (curEntries == null) {
          final PackageGroup grp = mTable.mPackageGroups.get(curPackageIndex);
          final List<Type> typeList = grp.types.get(t);
          int cnt = typeList.isEmpty() ? 0 : typeList.get(0).entryCount;
          curEntries = new theme_entry[cnt];
          curPI.types[t] = new type_info();
          curPI.types[t].numEntries = cnt;
          curPI.types[t].entries = curEntries;
        }
        numEntries = curPI.types[t].numEntries;
      }
      if (e >= numEntries) {
        ALOGE("Style contains key with bad entry: 0x%08x\n", attrRes);
        bagIndex++;
        continue;
      }

      if (curEntries[e] == null) {
        curEntries[e] = new theme_entry();
      }
      theme_entry curEntry = curEntries[e];

      //new theme_entry();

      //curEntries[e] = curEntry;
//      if (kDebugTableNoisy) {
//        ALOGV("Attr 0x%08x: type=0x%x, data=0x%08x; curType=0x%x",
//            attrRes, bag.get()[bagIndex].map.value.dataType, bag.get()[bagIndex].map.value.data,
//            curEntry.value.dataType);
//      }
      if (force || curEntry.value.dataType == DataType.NULL.code()) {
        curEntry.stringBlock = bag.get()[bagIndex].stringBlock;
        curEntry.typeSpecFlags |= bagTypeSpecFlags.get();
        curEntry.value = bag.get()[bagIndex].map.value;
      }

      bagIndex++;
    }

    mTable.unlock();

    if (kDebugTableTheme) {
      ALOGI("Applying style 0x%08x (force=%s)  theme %s...\n", resID, force, this);
      dumpToLog();
    }

    return NO_ERROR;

  }

  private void dumpToLog() {

  }

  public int setTo(ResTableTheme other) {
    if (kDebugTableTheme) {
      ALOGI("Setting theme %s from theme %s...\n", this, other);
      dumpToLog();
      other.dumpToLog();
    }

    if (mTable == other.mTable) {
      for (int i=0; i<Res_MAXPACKAGE; i++) {
        if (mPackages[i] != null) {
          mPackages[i] = null;
        }
        if (other.mPackages[i] != null) {
          mPackages[i] = copy_package(other.mPackages[i]);
        } else {
          mPackages[i] = null;
        }
      }
    } else {
      // @todo: need to really implement this, not just copy
      // the system package (which is still wrong because it isn't
      // fixing up resource references).
      for (int i=0; i<Res_MAXPACKAGE; i++) {
        if (mPackages[i] != null) {
          mPackages[i] = null;
        }
        // todo: C++ code presumably assumes index 0 is system, and only system
        //if (i == 0 && other.mPackages[i] != null) {
        if (other.mPackages[i] != null) {
          mPackages[i] = copy_package(other.mPackages[i]);
        } else {
          mPackages[i] = null;
        }
      }
    }

    mTypeSpecFlags = other.mTypeSpecFlags;

    if (kDebugTableTheme) {
      ALOGI("Final theme:");
      dumpToLog();
    }

    return NO_ERROR;
  }

  private package_info copy_package(package_info pi) {
    return new package_info(pi);
//    package_info newpi = new package_info();
//    for (int j = 0; j <= Res_MAXTYPE; j++) {
//      if (pi.types[j] == null) {
//        newpi.types[j] = null;
//        continue;
//      }
//      int cnt = pi.types[j].numEntries;
//      newpi.types[j] = new type_info();
//      newpi.types[j].numEntries = cnt;
//      theme_entry[] te = pi.types[j].entries;
//      if (te != null) {
//        theme_entry[] newte = new theme_entry[cnt];
//        newpi.types[j].entries = newte;
//        System.arraycopy(te, 0, newte, 0, te.length);
//      } else {
//        newpi.types[j].entries = null;
//      }
//    }
//    return newpi;
  }

  class theme_entry {
    int stringBlock;
    int typeSpecFlags;
    ResValue value = new ResValue();

    theme_entry() {}

    /** copy constructor. Performs a deep copy */
    public theme_entry(theme_entry src) {
      stringBlock = src.stringBlock;
      typeSpecFlags = src.typeSpecFlags;
      value = new ResValue(src.value);
    }
  };

  class type_info {
    int numEntries;
    theme_entry[] entries;

    type_info() {}

    /** copy constructor. Performs a deep copy */
    type_info(type_info src) {
      numEntries = src.numEntries;
      entries = new theme_entry[src.entries.length];
      for (int i=0; i < src.entries.length; i++) {
        if (src.entries[i] == null) {
          entries[i] = null;
        } else {
          entries[i] = new theme_entry(src.entries[i]);
        }
      }
    }
  };

  class package_info {
    type_info[] types = new type_info[Res_MAXTYPE + 1];

    package_info() {}

    /** copy constructor. Performs a deep copy */
    package_info(package_info src) {
      for (int i=0; i < src.types.length; i++) {
        if (src.types[i] == null) {
          types[i] = null;
        } else {
          types[i] = new type_info(src.types[i]);
        }
      }
    }
  };

  static final int Res_MAXPACKAGE = 255;
  static final int Res_MAXTYPE = 255;
}
