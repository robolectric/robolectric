package org.robolectric.res.android;

import static org.robolectric.res.android.Asset.AccessMode.ACCESS_BUFFER;
import static org.robolectric.res.android.Errors.NO_ERROR;
import static org.robolectric.res.android.Util.ALOGE;
import static org.robolectric.res.android.Util.ALOGV;
import static org.robolectric.res.android.Util.ALOGW;
import static org.robolectric.res.android.Util.isTruthy;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.robolectric.res.FileTypedResource;
import org.robolectric.res.FsFile;

// transliterated from https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/libs/androidfw/Asset.cpp
// and https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/libs/androidfw/include/androidfw/Asset.h
/*
 * Instances of this class provide read-only operations on a byte stream.
 *
 * Access may be optimized for streaming, random, or whole buffer modes.  All
 * operations are supported regardless of how the file was opened, but some
 * things will be less efficient.  [pass that in??]
 *
 * "Asset" is the base class for all types of assets.  The classes below
 * provide most of the implementation.  The AssetManager uses one of the
 * static "create" functions defined here to create a new instance.
 */
public abstract class Asset {
  public static final Asset EXCLUDED_ASSET = new _FileAsset();

  public Runnable onClose;

  public static Asset newFileAsset(FileTypedResource fileTypedResource) throws IOException {
    _FileAsset fileAsset = new _FileAsset();
    FsFile fsFile = fileTypedResource.getFsFile();
    fileAsset.mFileName = fsFile.getName();
    fileAsset.mLength = fsFile.length();
    fileAsset.mBuf = fsFile.getBytes();
    return fileAsset;
  }

  // public:
  // virtual ~Asset(void) = default;

  // static int getGlobalCount();
  // static String8 getAssetAllocations();

  public enum AccessMode {
    ACCESS_UNKNOWN(0),
    /* read chunks, and seek forward and backward */
    ACCESS_RANDOM(1),
    /* read sequentially, with an occasional forward seek */
    ACCESS_STREAMING(2),
    /* caller plans to ask for a read-only buffer with all data */
    ACCESS_BUFFER(3);

    private final int mode;

    AccessMode(int mode) {
      this.mode = mode;
    }

    public int mode() {
      return mode;
    }

    public static AccessMode fromInt(int mode) {
      for (AccessMode enumMode : values()) {
        if (mode == enumMode.mode()) {
          return enumMode;
        }
      }
      throw new IllegalArgumentException("invalid mode " + Integer.toString(mode));
    }
  }

  public static final int SEEK_SET = 0;
  public static final int SEEK_CUR = 1;
  public static final int SEEK_END = 2;

  public final int read(byte[] buf, int count) {
    return read(buf, 0, count);
  }

  /*
   * Read data from the current offset.  Returns the actual number of
   * bytes read, 0 on EOF, or -1 on error.
   *
   * Transliteration note: added bufOffset to translate to: index into buf to start writing at
   */
  public abstract int read(byte[] buf, int bufOffset, int count);

  /*
   * Seek to the specified offset.  "whence" uses the same values as
   * lseek/fseek.  Returns the new position on success, or (long) -1
   * on failure.
   */
  public abstract long seek(long offset, int whence);

    /*
     * Close the asset, freeing all associated resources.
     */
    public abstract void close();

    /*
     * Get a pointer to a buffer with the entire contents of the file.
     */
  public abstract byte[] getBuffer(boolean wordAligned);

  /*
   * Get the total amount of data that can be read.
   */
  public abstract long getLength();

  /*
   * Get the total amount of data that can be read from the current position.
   */
  public abstract long getRemainingLength();

    /*
     * Open a new file descriptor that can be used to read this asset.
     * Returns -1 if you can not use the file descriptor (for example if the
     * asset is compressed).
     */
  public abstract FileDescriptor openFileDescriptor(Ref<Long> outStart, Ref<Long> outLength);

  public abstract File getFile();

  public abstract String getFileName();

  /*
   * Return whether this asset's buffer is allocated in RAM (not mmapped).
   * Note: not virtual so it is safe to call even when being destroyed.
   */
  abstract boolean isAllocated(); // { return false; }

  /*
   * Get a string identifying the asset's source.  This might be a full
   * path, it might be a colon-separated list of identifiers.
   *
   * This is NOT intended to be used for anything except debug output.
   * DO NOT try to parse this or use it to open a file.
   */
  final String getAssetSource() { return mAssetSource.string(); }

  public abstract boolean isNinePatch();

//   protected:
//   /*
//    * Adds this Asset to the global Asset list for debugging and
//    * accounting.
//    * Concrete subclasses must call this in their finalructor.
//    */
//   static void registerAsset(Asset asset);
//
//   /*
//    * Removes this Asset from the global Asset list.
//    * Concrete subclasses must call this in their destructor.
//    */
//   static void unregisterAsset(Asset asset);
//
//   Asset(void);        // finalructor; only invoked indirectly
//
//   /* handle common seek() housekeeping */
//   long handleSeek(long offset, int whence, long curPosn, long maxPosn);

  /* set the asset source string */
  void setAssetSource(final String8 path) { mAssetSource = path; }

  AccessMode getAccessMode() { return mAccessMode; }

//   private:
//   /* these operations are not implemented */
//   Asset(final Asset& src);
//   Asset& operator=(final Asset& src);
//
//     /* AssetManager needs access to our "create" functions */
//   friend class AssetManager;
//
//     /*
//      * Create the asset from a named file on disk.
//      */
//   static Asset createFromFile(final String fileName, AccessMode mode);
//
//     /*
//      * Create the asset from a named, compressed file on disk (e.g. ".gz").
//      */
//   static Asset createFromCompressedFile(final String fileName,
//       AccessMode mode);
//
// #if 0
//     /*
//      * Create the asset from a segment of an open file.  This will fail
//      * if "offset" and "length" don't fit within the bounds of the file.
//      *
//      * The asset takes ownership of the file descriptor.
//      */
//   static Asset createFromFileSegment(int fd, long offset, int length,
//       AccessMode mode);
//
//     /*
//      * Create from compressed data.  "fd" should be seeked to the start of
//      * the compressed data.  This could be inside a gzip file or part of a
//      * Zip archive.
//      *
//      * The asset takes ownership of the file descriptor.
//      *
//      * This may not verify the validity of the compressed data until first
//      * use.
//      */
//   static Asset createFromCompressedData(int fd, long offset,
//       int compressionMethod, int compressedLength,
//       int uncompressedLength, AccessMode mode);
// #endif
//
//     /*
//      * Create the asset from a memory-mapped file segment.
//      *
//      * The asset takes ownership of the FileMap.
//      */
//   static Asset createFromUncompressedMap(FileMap dataMap, AccessMode mode);
//
//     /*
//      * Create the asset from a memory-mapped file segment with compressed
//      * data.
//      *
//      * The asset takes ownership of the FileMap.
//      */
//   static Asset createFromCompressedMap(FileMap dataMap,
//       int uncompressedLen, AccessMode mode);
//
//
//     /*
//      * Create from a reference-counted chunk of shared memory.
//      */
//   // TODO

  AccessMode  mAccessMode;        // how the asset was opened
  String8    mAssetSource;       // debug string

  Asset		mNext;				// linked list.
  Asset		mPrev;

  static final boolean kIsDebug = false;

  final static Object gAssetLock = new Object();
  static int gCount = 0;
  static Asset gHead = null;
  static Asset gTail = null;

  void registerAsset(Asset asset)
  {
  //   AutoMutex _l(gAssetLock);
  //   gCount++;
  //   asset.mNext = asset.mPrev = null;
  //   if (gTail == null) {
  //     gHead = gTail = asset;
  //   } else {
  //     asset.mPrev = gTail;
  //     gTail.mNext = asset;
  //     gTail = asset;
  //   }
  //
  //   if (kIsDebug) {
  //     ALOGI("Creating Asset %s #%d\n", asset, gCount);
  //   }
  }

  void unregisterAsset(Asset asset)
  {
  //   AutoMutex _l(gAssetLock);
  //   gCount--;
  //   if (gHead == asset) {
  //     gHead = asset.mNext;
  //   }
  //   if (gTail == asset) {
  //     gTail = asset.mPrev;
  //   }
  //   if (asset.mNext != null) {
  //     asset.mNext.mPrev = asset.mPrev;
  //   }
  //   if (asset.mPrev != null) {
  //     asset.mPrev.mNext = asset.mNext;
  //   }
  //   asset.mNext = asset.mPrev = null;
  //
  //   if (kIsDebug) {
  //     ALOGI("Destroying Asset in %s #%d\n", asset, gCount);
  //   }
  }

  public static int getGlobalCount()
  {
    // AutoMutex _l(gAssetLock);
    synchronized (gAssetLock) {
      return gCount;
    }
  }

  public static String getAssetAllocations()
  {
    // AutoMutex _l(gAssetLock);
    synchronized (gAssetLock) {
      StringBuilder res = new StringBuilder();
      Asset cur = gHead;
      while (cur != null) {
        if (cur.isAllocated()) {
          res.append("    ");
          res.append(cur.getAssetSource());
          long size = (cur.getLength()+512)/1024;
          String buf = String.format(": %dK\n", (int)size);
          res.append(buf);
        }
        cur = cur.mNext;
      }

      return res.toString();
    }
  }

  Asset() {
    // : mAccessMode(ACCESS_UNKNOWN), mNext(null), mPrev(null)
    mAccessMode = AccessMode.ACCESS_UNKNOWN;
  }

  /*
   * Create a new Asset from a file on disk.  There is a fair chance that
   * the file doesn't actually exist.
   *
   * We can use "mode" to decide how we want to go about it.
   */
  static Asset createFromFile(final String fileName, AccessMode mode)
  {
    File file = new File(fileName);
    if (!file.exists()) {
      return null;
    }
    throw new UnsupportedOperationException();

    // _FileAsset pAsset;
    // int result;
    // long length;
    // int fd;
    //
    //   fd = open(fileName, O_RDONLY | O_BINARY);
    //   if (fd < 0)
    //     return null;
    //
    //   /*
    //    * Under Linux, the lseek fails if we actually opened a directory.  To
    //    * be correct we should test the file type explicitly, but since we
    //    * always open things read-only it doesn't really matter, so there's
    //    * no value in incurring the extra overhead of an fstat() call.
    //    */
    //   // TODO(kroot): replace this with fstat despite the plea above.
    //   #if 1
    //   length = lseek64(fd, 0, SEEK_END);
    //   if (length < 0) {
    //   ::close(fd);
    //     return null;
    //   }
    //   (void) lseek64(fd, 0, SEEK_SET);
    //   #else
    //   struct stat st;
    //   if (fstat(fd, &st) < 0) {
    //   ::close(fd);
    //   return null;
    // }
    //
    //   if (!S_ISREG(st.st_mode)) {
    //   ::close(fd);
    //     return null;
    //   }
    //   #endif
    //
    //     pAsset = new _FileAsset;
    //   result = pAsset.openChunk(fileName, fd, 0, length);
    //   if (result != NO_ERROR) {
    //     delete pAsset;
    //     return null;
    //   }
    //
    //   pAsset.mAccessMode = mode;
    //   return pAsset;
  }


  /*
   * Create a new Asset from a compressed file on disk.  There is a fair chance
   * that the file doesn't actually exist.
   *
   * We currently support gzip files.  We might want to handle .bz2 someday.
   */
  static Asset createFromCompressedFile(final String fileName,
      AccessMode mode)
  {
    throw new UnsupportedOperationException();
    // _CompressedAsset pAsset;
    // int result;
    // long fileLen;
    // boolean scanResult;
    // long offset;
    // int method;
    // long uncompressedLen, compressedLen;
    // int fd;
    //
    // fd = open(fileName, O_RDONLY | O_BINARY);
    // if (fd < 0)
    //   return null;
    //
    // fileLen = lseek(fd, 0, SEEK_END);
    // if (fileLen < 0) {
    // ::close(fd);
    //   return null;
    // }
    // (void) lseek(fd, 0, SEEK_SET);
    //
    // /* want buffered I/O for the file scan; must dup so fclose() is safe */
    // FILE* fp = fdopen(dup(fd), "rb");
    // if (fp == null) {
    // ::close(fd);
    //   return null;
    // }
    //
    // unsigned long crc32;
    // scanResult = ZipUtils::examineGzip(fp, &method, &uncompressedLen,
    // &compressedLen, &crc32);
    // offset = ftell(fp);
    // fclose(fp);
    // if (!scanResult) {
    //   ALOGD("File '%s' is not in gzip format\n", fileName);
    // ::close(fd);
    //   return null;
    // }
    //
    // pAsset = new _CompressedAsset;
    // result = pAsset.openChunk(fd, offset, method, uncompressedLen,
    //     compressedLen);
    // if (result != NO_ERROR) {
    //   delete pAsset;
    //   return null;
    // }
    //
    // pAsset.mAccessMode = mode;
    // return pAsset;
  }


//     #if 0
// /*
//  * Create a new Asset from part of an open file.
//  */
// /*static*/ Asset createFromFileSegment(int fd, long offset,
//       int length, AccessMode mode)
//   {
//     _FileAsset pAsset;
//     int result;
//
//     pAsset = new _FileAsset;
//     result = pAsset.openChunk(null, fd, offset, length);
//     if (result != NO_ERROR)
//       return null;
//
//     pAsset.mAccessMode = mode;
//     return pAsset;
//   }
//
// /*
//  * Create a new Asset from compressed data in an open file.
//  */
// /*static*/ Asset createFromCompressedData(int fd, long offset,
//       int compressionMethod, int uncompressedLen, int compressedLen,
//       AccessMode mode)
//   {
//     _CompressedAsset pAsset;
//     int result;
//
//     pAsset = new _CompressedAsset;
//     result = pAsset.openChunk(fd, offset, compressionMethod,
//         uncompressedLen, compressedLen);
//     if (result != NO_ERROR)
//       return null;
//
//     pAsset.mAccessMode = mode;
//     return pAsset;
//   }
//     #endif

  /*
   * Create a new Asset from a memory mapping.
   */
  static Asset createFromUncompressedMap(FileMap dataMap,
      AccessMode mode)
  {
    _FileAsset pAsset;
    int result;

    pAsset = new _FileAsset();
    result = pAsset.openChunk(dataMap);
    if (result != NO_ERROR)
      return null;

    pAsset.mAccessMode = mode;
    return pAsset;
  }

  /*
   * Create a new Asset from compressed data in a memory mapping.
   */
static Asset createFromCompressedMap(FileMap dataMap,
      int uncompressedLen, AccessMode mode)
  {
    _CompressedAsset pAsset;
    int result;

    pAsset = new _CompressedAsset();
    result = pAsset.openChunk(dataMap, uncompressedLen);
    if (result != NO_ERROR)
      return null;

    pAsset.mAccessMode = mode;
    return pAsset;
  }


  /*
   * Do generic seek() housekeeping.  Pass in the offset/whence values from
   * the seek request, along with the current chunk offset and the chunk
   * length.
   *
   * Returns the new chunk offset, or -1 if the seek is illegal.
   */
  long handleSeek(long offset, int whence, long curPosn, long maxPosn)
  {
    long newOffset;

    switch (whence) {
      case SEEK_SET:
        newOffset = offset;
        break;
      case SEEK_CUR:
        newOffset = curPosn + offset;
        break;
      case SEEK_END:
        newOffset = maxPosn + offset;
        break;
      default:
        ALOGW("unexpected whence %d\n", whence);
        // this was happening due to an long size mismatch
        assert(false);
        return (long) -1;
    }

    if (newOffset < 0 || newOffset > maxPosn) {
      ALOGW("seek out of range: want %ld, end=%ld\n",
          (long) newOffset, (long) maxPosn);
      return (long) -1;
    }

    return newOffset;
  }

  /*
   * An asset based on an uncompressed file on disk.  It may encompass the
   * entire file or just a piece of it.  Access is through fread/fseek.
   */
  static class _FileAsset extends Asset {

    // public:
//     _FileAsset(void);
//     virtual ~_FileAsset(void);
//
//     /*
//      * Use a piece of an already-open file.
//      *
//      * On success, the object takes ownership of "fd".
//      */
//     int openChunk(final String fileName, int fd, long offset, int length);
//
//     /*
//      * Use a memory-mapped region.
//      *
//      * On success, the object takes ownership of "dataMap".
//      */
//     int openChunk(FileMap dataMap);
//
//     /*
//      * Standard Asset interfaces.
//      */
//     virtual ssize_t read(void* buf, int count);
//     virtual long seek(long offset, int whence);
//     virtual void close(void);
//     virtual final void* getBuffer(boolean wordAligned);

    @Override
    public long getLength() { return mLength; }

    @Override
    public long getRemainingLength() { return mLength-mOffset; }

//     virtual int openFileDescriptor(long* outStart, long* outLength) final;
    @Override
    boolean isAllocated() { return mBuf != null; }

    @Override
    public boolean isNinePatch() {
      String fileName = getFileName();
      if (mMap != null) {
        fileName = mMap.getZipEntry().getName();
      }
      return fileName != null && fileName.toLowerCase().endsWith(".9.png");
    }

    //
// private:
    long mStart;         // absolute file offset of start of chunk
    long mLength;        // length of the chunk
    long mOffset;        // current local offset, 0 == mStart
    // FILE*       mFp;            // for read/seek
    RandomAccessFile mFp;            // for read/seek
    String mFileName;      // for opening

    /*
     * To support getBuffer() we either need to read the entire thing into
     * a buffer or memory-map it.  For small files it's probably best to
     * just read them in.
     */
// enum {
  public static int kReadVsMapThreshold = 4096;
// };

    FileMap mMap;           // for memory map
    byte[] mBuf;        // for read

    // final void* ensureAlignment(FileMap map);
/*
 * ===========================================================================
 *      _FileAsset
 * ===========================================================================
 */

    /*
     * Constructor.
     */
    _FileAsset()
    // : mStart(0), mLength(0), mOffset(0), mFp(null), mFileName(null), mMap(null), mBuf(null)
    {
      // Register the Asset with the global list here after it is fully constructed and its
      // vtable pointer points to this concrete type. b/31113965
      registerAsset(this);
    }

    /*
     * Destructor.  Release resources.
     */
    @Override
    protected void finalize() {
      close();

      // Unregister the Asset from the global list here before it is destructed and while its vtable
      // pointer still points to this concrete type. b/31113965
      unregisterAsset(this);
    }

    /*
     * Operate on a chunk of an uncompressed file.
     *
     * Zero-length chunks are allowed.
     */
    int openChunk(final String fileName, int fd, long offset, int length) {
      throw new UnsupportedOperationException();
      // assert(mFp == null);    // no reopen
      // assert(mMap == null);
      // assert(fd >= 0);
      // assert(offset >= 0);
      //
      // /*
      //  * Seek to end to get file length.
      //  */
      // long fileLength;
      // fileLength = lseek64(fd, 0, SEEK_END);
      // if (fileLength == (long) -1) {
      //   // probably a bad file descriptor
      //   ALOGD("failed lseek (errno=%d)\n", errno);
      //   return UNKNOWN_ERROR;
      // }
      //
      // if ((long) (offset + length) > fileLength) {
      //   ALOGD("start (%ld) + len (%ld) > end (%ld)\n",
      //       (long) offset, (long) length, (long) fileLength);
      //   return BAD_INDEX;
      // }
      //
      // /* after fdopen, the fd will be closed on fclose() */
      // mFp = fdopen(fd, "rb");
      // if (mFp == null)
      //   return UNKNOWN_ERROR;
      //
      // mStart = offset;
      // mLength = length;
      // assert(mOffset == 0);
      //
      // /* seek the FILE* to the start of chunk */
      // if (fseek(mFp, mStart, SEEK_SET) != 0) {
      //   assert(false);
      // }
      //
      // mFileName = fileName != null ? strdup(fileName) : null;
      //
      // return NO_ERROR;
    }

    /*
     * Create the chunk from the map.
     */
    int openChunk(FileMap dataMap) {
      assert(mFp == null);    // no reopen
      assert(mMap == null);
      assert(dataMap != null);

      mMap = dataMap;
      mStart = -1;            // not used
      mLength = dataMap.getDataLength();
      assert(mOffset == 0);

      mBuf = dataMap.getDataPtr();

      return NO_ERROR;
    }

    /*
     * Read a chunk of data.
     */
    @Override
    public int read(byte[] buf, int bufOffset, int count) {
      int maxLen;
      int actual;

      assert(mOffset >= 0 && mOffset <= mLength);

      if (getAccessMode() == ACCESS_BUFFER) {
          /*
           * On first access, read or map the entire file.  The caller has
           * requested buffer access, either because they're going to be
           * using the buffer or because what they're doing has appropriate
           * performance needs and access patterns.
           */
        if (mBuf == null)
          getBuffer(false);
      }

      /* adjust count if we're near EOF */
      maxLen = toIntExact(mLength - mOffset);
      if (count > maxLen)
        count = maxLen;

      if (!isTruthy(count)) {
        return 0;
      }

      if (mMap != null) {
          /* copy from mapped area */
        //printf("map read\n");
        // memcpy(buf, (String)mMap.getDataPtr() + mOffset, count);
        System.arraycopy(mMap.getDataPtr(), toIntExact(mOffset), buf, bufOffset, count);
        actual = count;
      } else if (mBuf != null) {
          /* copy from buffer */
        //printf("buf read\n");
        // memcpy(buf, (String)mBuf + mOffset, count);
        System.arraycopy(mBuf, toIntExact(mOffset), buf, bufOffset, count);
        actual = count;
      } else {
          /* read from the file */
        //printf("file read\n");
        // if (ftell(mFp) != mStart + mOffset) {
        try {
          if (mFp.getFilePointer() != mStart + mOffset) {
            ALOGE("Hosed: %ld != %ld+%ld\n",
                mFp.getFilePointer(), (long) mStart, (long) mOffset);
            assert(false);
          }

          /*
           * This returns 0 on error or eof.  We need to use ferror() or feof()
           * to tell the difference, but we don't currently have those on the
           * device.  However, we know how much data is *supposed* to be in the
           * file, so if we don't read the full amount we know something is
           * hosed.
           */
          actual = mFp.read(buf, 0, count);
          if (actual == 0)        // something failed -- I/O error?
            return -1;

          assert(actual == count);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }

      mOffset += actual;
      return actual;
    }

    /*
     * Seek to a new position.
     */
    @Override
    public long seek(long offset, int whence) {
      long newPosn;
      long actualOffset;

      // compute new position within chunk
      newPosn = handleSeek(offset, whence, mOffset, mLength);
      if (newPosn == (long) -1)
        return newPosn;

      actualOffset = mStart + newPosn;

      if (mFp != null) {
        throw new UnsupportedOperationException();
        // if (fseek(mFp, (long) actualOffset, SEEK_SET) != 0)
        //   return (long) -1;
      }

      mOffset = actualOffset - mStart;
      return mOffset;
    }

    /*
     * Close the asset.
     */
    @Override
    public void close() {
      throw new UnsupportedOperationException();
      // if (mMap != null) {
      //   delete mMap;
      //   mMap = null;
      // }
      // if (mBuf != null) {
      //   delete[] mBuf;
      //   mBuf = null;
      // }
      //
      // if (mFileName != null) {
      //   free(mFileName);
      //   mFileName = null;
      // }
      //
      // if (mFp != null) {
      //   // can only be null when called from destructor
      //   // (otherwise we would never return this object)
      //   fclose(mFp);
      //   mFp = null;
      // }
    }

    /*
     * Return a read-only pointer to a buffer.
     *
     * We can either read the whole thing in or map the relevant piece of
     * the source file.  Ideally a map would be established at a higher
     * level and we'd be using a different object, but we didn't, so we
     * deal with it here.
     */
    @Override
    public final byte[] getBuffer(boolean wordAligned) {
      /* subsequent requests just use what we did previously */
      if (mBuf != null)
        return mBuf;
      if (mMap != null) {
        // if (!wordAligned) {
          return  mMap.getDataPtr();
        // }
        // return ensureAlignment(mMap);
      }

      // assert(mFp != null);

      if (true /*mLength < kReadVsMapThreshold*/) {
        byte[] buf;
        int allocLen;

          /* zero-length files are allowed; not sure about zero-len allocs */
          /* (works fine with gcc + x86linux) */
        allocLen = toIntExact(mLength);
        if (mLength == 0)
          allocLen = 1;

        buf = new byte[allocLen];
        if (buf == null) {
          ALOGE("alloc of %ld bytes failed\n", (long) allocLen);
          return null;
        }

        ALOGV("Asset %s allocating buffer size %d (smaller than threshold)", this, (int)allocLen);
        if (mLength > 0) {
          try {
            // long oldPosn = ftell(mFp);
            long oldPosn = mFp.getFilePointer();
            // fseek(mFp, mStart, SEEK_SET);
            mFp.seek(mStart);
            // if (fread(buf, 1, mLength, mFp) != (size_t) mLength) {
            if (mFp.read(buf, 0, toIntExact(mLength)) != (int) mLength) {
              ALOGE("failed reading %ld bytes\n", (long) mLength);
              // delete[] buf;
              return null;
            }
            // fseek(mFp, oldPosn, SEEK_SET);
            mFp.seek(oldPosn);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }

        ALOGV(" getBuffer: loaded into buffer\n");

        mBuf = buf;
        return mBuf;
      } else {
        FileMap map;

        map = new FileMap();
        // if (!map.create(null, fileno(mFp), mStart, mLength, true)) {
        if (!map.create(null, -1, mStart, toIntExact(mLength), true)) {
          // delete map;
          return null;
        }

        ALOGV(" getBuffer: mapped\n");

        mMap = map;
        // if (!wordAligned) {
        //   return  mMap.getDataPtr();
        // }
        return ensureAlignment(mMap);
      }
    }

    /**
     * Return the file on disk representing this asset.
     *
     * Non-Android framework method. Based on {@link #openFileDescriptor(Ref, Ref)}.
     */
    @Override
    public File getFile() {
      if (mMap != null) {
        String fname = mMap.getFileName();
        if (fname == null) {
          fname = mFileName;
        }
        if (fname == null) {
          return null;
        }
        // return open(fname, O_RDONLY | O_BINARY);
        return new File(fname);
      }
      if (mFileName == null) {
        return null;
      }
      return new File(mFileName);
    }

    @Override
    public String getFileName() {
      File file = getFile();
      return file == null ? null : file.getName();
    }

    @Override
    public FileDescriptor openFileDescriptor(Ref<Long> outStart, Ref<Long> outLength) {
      if (mMap != null) {
        String fname = mMap.getFileName();
        if (fname == null) {
          fname = mFileName;
        }
        if (fname == null) {
          return null;
        }
        outStart.set(mMap.getDataOffset());
        outLength.set((long) mMap.getDataLength());
        // return open(fname, O_RDONLY | O_BINARY);
        return open(fname);
      }
      if (mFileName == null) {
        return null;
      }
      outStart.set(mStart);
      outLength.set(mLength);
      // return open(mFileName, O_RDONLY | O_BINARY);
      return open(mFileName);
    }

    private static FileDescriptor open(String fname) {
      try {
        return new FileInputStream(new File(fname)).getFD();
      } catch (IOException e) {
        return null;
      }
    }

    final byte[] ensureAlignment(FileMap map) {
      throw new UnsupportedOperationException();
      //   void* data = map.getDataPtr();
      //   if ((((int)data)&0x3) == 0) {
      //     // We can return this directly if it is aligned on a word
      //     // boundary.
      //     ALOGV("Returning aligned FileAsset %s (%s).", this,
      //         getAssetSource());
      //     return data;
      //   }
      //   // If not aligned on a word boundary, then we need to copy it into
      //   // our own buffer.
      //   ALOGV("Copying FileAsset %s (%s) to buffer size %d to make it aligned.", this,
      //       getAssetSource(), (int)mLength);
      //   unsigned String buf = new unsigned char[mLength];
      //   if (buf == null) {
      //     ALOGE("alloc of %ld bytes failed\n", (long) mLength);
      //     return null;
      //   }
      //   memcpy(buf, data, mLength);
      //   mBuf = buf;
      //   return buf;
      // }
    }

    @Override
    public String toString() {
      if (mFileName == null) {
        return "_FileAsset{" +
            "mMap=" + mMap +
            '}';
      } else {
        return "_FileAsset{" +
            "mFileName='" + mFileName + '\'' +
            '}';
      }
    }
  }

  /*
   * An asset based on compressed data in a file.
   */
  static class _CompressedAsset extends Asset {
// public:
//     _CompressedAsset(void);
//     virtual ~_CompressedAsset(void);
//
//     /*
//      * Use a piece of an already-open file.
//      *
//      * On success, the object takes ownership of "fd".
//      */
//     int openChunk(int fd, long offset, int compressionMethod,
//     int uncompressedLen, int compressedLen);
//
//     /*
//      * Use a memory-mapped region.
//      *
//      * On success, the object takes ownership of "fd".
//      */
//     int openChunk(FileMap dataMap, int uncompressedLen);
//
//     /*
//      * Standard Asset interfaces.
//      */
//     virtual ssize_t read(void* buf, int count);
//     virtual long seek(long offset, int whence);
//     virtual void close(void);
//     virtual final void* getBuffer(boolean wordAligned);

    @Override
    public long getLength() { return mUncompressedLen; }

    @Override
    public long getRemainingLength() { return mUncompressedLen-mOffset; }

    @Override
    public File getFile() {
      return null;
    }

    @Override
    public String getFileName() {
      ZipEntry zipEntry = mMap.getZipEntry();
      return zipEntry == null ? null : zipEntry.getName();
    }

    @Override
    public FileDescriptor openFileDescriptor(Ref<Long> outStart, Ref<Long> outLength) { return null; }

    @Override
    boolean isAllocated() { return mBuf != null; }

    @Override
    public boolean isNinePatch() {
      String fileName = getFileName();
      return fileName != null && fileName.toLowerCase().endsWith(".9.png");
    }

    // private:
    long mStart;         // offset to start of compressed data
    long mCompressedLen; // length of the compressed data
    long mUncompressedLen; // length of the uncompressed data
    long mOffset;        // current offset, 0 == start of uncomp data

    FileMap mMap;           // for memory-mapped input
    int mFd;            // for file input

// class StreamingZipInflater mZipInflater;  // for streaming large compressed assets

    byte[] mBuf;       // for getBuffer()
/*
 * ===========================================================================
 *      _CompressedAsset
 * ===========================================================================
 */

    /*
     * Constructor.
     */
    _CompressedAsset()
    // : mStart(0), mCompressedLen(0), mUncompressedLen(0), mOffset(0),
    // mMap(null), mFd(-1), mZipInflater(null), mBuf(null)
    {
      mFd = -1;

      // Register the Asset with the global list here after it is fully constructed and its
      // vtable pointer points to this concrete type. b/31113965
      registerAsset(this);
    }

    ZipFile zipFile;
    String entryName;

    // @Override
    // public byte[] getBuffer(boolean wordAligned) {
    //   ZipEntry zipEntry = zipFile.getEntry(entryName);
    //   int size = (int) zipEntry.getSize();
    //   byte[] buf = new byte[size];
    //   try (InputStream in = zipFile.getInputStream(zipEntry)) {
    //     if (in.read(buf) != size) {
    //       throw new IOException(
    //           "Failed to read " + size + " bytes from " + zipFile + "!" + entryName);
    //     }
    //     return buf;
    //   } catch (IOException e) {
    //     throw new RuntimeException(e);
    //   }
    // }

    /*
     * Destructor.  Release resources.
     */
    @Override
    protected void finalize() {
      close();

      // Unregister the Asset from the global list here before it is destructed and while its vtable
      // pointer still points to this concrete type. b/31113965
      unregisterAsset(this);
    }

    /*
     * Open a chunk of compressed data inside a file.
     *
     * This currently just sets up some values and returns.  On the first
     * read, we expand the entire file into a buffer and return data from it.
     */
    int openChunk(int fd, long offset,
        int compressionMethod, int uncompressedLen, int compressedLen) {
      throw new UnsupportedOperationException();
      // assert(mFd < 0);        // no re-open
      // assert(mMap == null);
      // assert(fd >= 0);
      // assert(offset >= 0);
      // assert(compressedLen > 0);
      //
      // if (compressionMethod != ZipFileRO::kCompressDeflated) {
      // assert(false);
      // return UNKNOWN_ERROR;
      // }
      //
      // mStart = offset;
      // mCompressedLen = compressedLen;
      // mUncompressedLen = uncompressedLen;
      // assert(mOffset == 0);
      // mFd = fd;
      // assert(mBuf == null);
      //
      // if (uncompressedLen > StreamingZipInflater::OUTPUT_CHUNK_SIZE) {
      // mZipInflater = new StreamingZipInflater(mFd, offset, uncompressedLen, compressedLen);
      // }
      //
      // return NO_ERROR;
    }

    /*
     * Open a chunk of compressed data in a mapped region.
     *
     * Nothing is expanded until the first read call.
     */
    int openChunk(FileMap dataMap, int uncompressedLen) {
      assert(mFd < 0);        // no re-open
      assert(mMap == null);
      assert(dataMap != null);

      mMap = dataMap;
      mStart = -1;        // not used
      mCompressedLen = dataMap.getDataLength();
      mUncompressedLen = uncompressedLen;
      assert(mOffset == 0);

      // if (uncompressedLen > StreamingZipInflater::OUTPUT_CHUNK_SIZE) {
      // mZipInflater = new StreamingZipInflater(dataMap, uncompressedLen);
      // }
      return NO_ERROR;
    }

    /*
     * Read data from a chunk of compressed data.
     *
     * [For now, that's just copying data out of a buffer.]
     */
    @Override
    public int read(byte[] buf, int bufOffset, int count) {
      int maxLen;
      int actual;

      assert(mOffset >= 0 && mOffset <= mUncompressedLen);

       /* If we're relying on a streaming inflater, go through that */
//       if (mZipInflater) {
//       actual = mZipInflater.read(buf, count);
//       } else {
      if (mBuf == null) {
        if (getBuffer(false) == null)
          return -1;
      }
      assert(mBuf != null);

      /* adjust count if we're near EOF */
      maxLen = toIntExact(mUncompressedLen - mOffset);
      if (count > maxLen)
        count = maxLen;

      if (!isTruthy(count))
        return 0;

      /* copy from buffer */
      //printf("comp buf read\n");
//      memcpy(buf, (String)mBuf + mOffset, count);
      System.arraycopy(mBuf, toIntExact(mOffset), buf, bufOffset, count);
      actual = count;
//       }

      mOffset += actual;
      return actual;
    }

    /*
     * Handle a seek request.
     *
     * If we're working in a streaming mode, this is going to be fairly
     * expensive, because it requires plowing through a bunch of compressed
     * data.
     */
    @Override
    public long seek(long offset, int whence) {
      long newPosn;

      // compute new position within chunk
      newPosn = handleSeek(offset, whence, mOffset, mUncompressedLen);
      if (newPosn == (long) -1)
      return newPosn;

      // if (mZipInflater) {
      //   mZipInflater.seekAbsolute(newPosn);
      // }
      mOffset = newPosn;
      return mOffset;
    }

    /*
     * Close the asset.
     */
    @Override
    public void close() {
       if (mMap != null) {
//       delete mMap;
       mMap = null;
       }

//       delete[] mBuf;
       mBuf = null;

//       delete mZipInflater;
//       mZipInflater = null;

       if (mFd > 0) {
//       ::close(mFd);
       mFd = -1;
       }
    }

    /*
     * Get a pointer to a read-only buffer of data.
     *
     * The first time this is called, we expand the compressed data into a
     * buffer.
     */
    @Override
    public byte[] getBuffer(boolean wordAligned) {
      // return mBuf = mMap.getDataPtr();
      byte[] buf = null;

      if (mBuf != null)
        return mBuf;

      /*
       * Allocate a buffer and read the file into it.
       */
      // buf = new byte[(int) mUncompressedLen];
      // if (buf == null) {
      //   ALOGW("alloc %ld bytes failed\n", (long) mUncompressedLen);
      //   return null;
      // }

      if (mMap != null) {
        buf = mMap.getDataPtr();
        // if (!ZipUtils::inflateToBuffer(mMap.getDataPtr(), buf,
        //     mUncompressedLen, mCompressedLen))
        // return null;
      } else {
        throw new UnsupportedOperationException();
        // assert(mFd >= 0);
        //
        // /*
        //    * Seek to the start of the compressed data.
        //    */
        // if (lseek(mFd, mStart, SEEK_SET) != mStart)
        // goto bail;
        //
        // /*
        //    * Expand the data into it.
        //    */
        // if (!ZipUtils::inflateToBuffer(mFd, buf, mUncompressedLen,
        //     mCompressedLen))
        // goto bail;
      }

      /*
       * Success - now that we have the full asset in RAM we
       * no longer need the streaming inflater
       */
      // delete mZipInflater;
      // mZipInflater = null;

      mBuf = buf;
      // buf = null;

      // bail:
      // delete[] buf;
      return mBuf;
    }

    @Override
    public String toString() {
      return "_CompressedAsset{" +
          "mMap=" + mMap +
          '}';
    }
  }

  // todo: remove when Android supports this
  static int toIntExact(long value) {
    if ((int)value != value) {
      throw new ArithmeticException("integer overflow");
    }
    return (int)value;
  }
}
