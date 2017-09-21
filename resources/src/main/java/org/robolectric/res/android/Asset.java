package org.robolectric.res.android;

import static org.robolectric.res.android.Util.ALOGI;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import org.robolectric.res.android.ZipFileRO.ZipEntryRO;

public abstract class Asset {

  //using namespace android;
//#ifndef O_BINARY
//# define O_BINARY 0
//#endif
private static final boolean kIsDebug = false;
  private static final Object gAssetLock = new Object();
  private static List<Asset> gAssets = new LinkedList<>();
  public Runnable onClose;
  private String8    mAssetSource;       // debug string

  //String8 Asset::getAssetAllocations()
//{
//    AutoMutex _l(gAssetLock);
//    String8 res;
//    Asset* cur = gHead;
//    while (cur != NULL) {
//        if (cur->isAllocated()) {
//            res.append("    ");
//            res.append(cur->getAssetSource());
//            off64_t size = (cur->getLength()+512)/1024;
//            char buf[64];
//            sprintf(buf, ": %dK\n", (int)size);
//            res.append(buf);
//        }
//        cur = cur->mNext;
//    }
//    return res;
//}
//Asset::Asset(void)
//    : mAccessMode(ACCESS_UNKNOWN), mNext(NULL), mPrev(NULL)
//{
//}
/*
 * Create a new Asset from a file on disk.  There is a fair chance that
 * the file doesn't actually exist.
 *
 * We can use "mode" to decide how we want to go about it.
 */
static Asset createFromFile(String fileName, AccessMode mode)
{
//    _FileAsset* pAsset;
//    status_t result;
//    off64_t length;
//    int fd;
//    fd = open(fileName, O_RDONLY | O_BINARY);
//    if (fd < 0)
//        return NULL;
//    /*
//     * Under Linux, the lseek fails if we actually opened a directory.  To
//     * be correct we should test the file type explicitly, but since we
//     * always open things read-only it doesn't really matter, so there's
//     * no value in incurring the extra overhead of an fstat() call.
//     */
//    // TODO(kroot): replace this with fstat despite the plea above.
//#if 1
//    length = lseek64(fd, 0, SEEK_END);
//    if (length < 0) {
//        ::close(fd);
//        return NULL;
//    }
//    (void) lseek64(fd, 0, SEEK_SET);
//#else
//    struct stat st;
//    if (fstat(fd, &st) < 0) {
//        ::close(fd);
//        return NULL;
//    }
//    if (!S_ISREG(st.st_mode)) {
//        ::close(fd);
//        return NULL;
//    }
//#endif
//    pAsset = new _FileAsset;
//    result = pAsset->openChunk(fileName, fd, 0, length);
//    if (result != NO_ERROR) {
//        delete pAsset;
//        return NULL;
//    }
//    pAsset->mAccessMode = mode;
//    return pAsset;
  try {
    RandomAccessFile f = new RandomAccessFile(fileName, "r");
    return new FileAsset(f);
  } catch (FileNotFoundException e) {
    throw new RuntimeException(e);
  }
}

  static Asset createFromCompressedFile(String fileName,
      AccessMode mode)
  {
    return null;
  //    _CompressedAsset pAsset;
  //    status_t result;
  //    off64_t fileLen;
  //    bool scanResult;
  //    long offset;
  //    int method;
  //    long uncompressedLen, compressedLen;
  //    int fd;
  //    fd = open(fileName, O_RDONLY | O_BINARY);
  //    if (fd < 0)
  //        return NULL;
  //    fileLen = lseek(fd, 0, SEEK_END);
  //    if (fileLen < 0) {
  //        ::close(fd);
  //        return NULL;
  //    }
  //    (void) lseek(fd, 0, SEEK_SET);
  //    /* want buffered I/O for the file scan; must dup so fclose() is safe */
  //    FILE* fp = fdopen(dup(fd), "rb");
  //    if (fp == NULL) {
  //        ::close(fd);
  //        return NULL;
  //    }
  //    unsigned long crc32;
  //    scanResult = ZipUtils::examineGzip(fp, &method, &uncompressedLen,
  //                    &compressedLen, &crc32);
  //    offset = ftell(fp);
  //    fclose(fp);
  //    if (!scanResult) {
  //        ALOGD("File '%s' is not in gzip format\n", fileName);
  //        ::close(fd);
  //        return NULL;
  //    }
  //    pAsset = new _CompressedAsset;
  //    result = pAsset->openChunk(fd, offset, method, uncompressedLen,
  //                compressedLen);
  //    if (result != NO_ERROR) {
  //        delete pAsset;
  //        return NULL;
  //    }
  //    pAsset->mAccessMode = mode;
  //    return pAsset;
  }

  public static Asset createFromZipEntry(ZipFileRO pZipFile, ZipEntryRO entry, String8 string8) {
    return new CompressedAsset(pZipFile, entry, string8);
  }

  public abstract int getLength();

  public abstract long size();

  public abstract int read();

  public abstract int read(byte[] b, int off, int len);

  public abstract long seek(long offset, int whence);

  public abstract void close();

  /* set the asset source string */
  void setAssetSource(String8 path) {
    mAssetSource = path;
  }

  protected void registerAsset(Asset asset)
{
  int gCount = 0;
    synchronized (gAssetLock) {
      gAssets.add(asset);
      gCount = gAssets.size();
    }
  if (kIsDebug) {
    ALOGI("Creating Asset %p #%d\n", asset, gCount);
  }

}

  protected void unregisterAsset(Asset asset) {
  int gCount = 0;
  synchronized (gAssetLock) {
    gAssets.remove(asset);
    gCount = gAssets.size();
  }
  if (kIsDebug) {
        ALOGI("Destroying Asset in %p #%d\n", asset, gCount);
    }
}

  protected int getGlobalCount()
{
  synchronized (gAssetLock) {
    return gAssets.size();
  }
}

  /*
 * Return a read-only pointer to a buffer.
 *
 * We can either read the whole thing in or map the relevant piece of
 * the source file.  Ideally a map would be established at a higher
 * level and we'd be using a different object, but we didn't, so we
 * deal with it here.
 */
  public byte[] getBuffer(boolean wordAligned) {
    int size = (int) size();
    byte[] bytes = new byte[size];
    if (read(bytes, 0, size) != size) {
      throw new RuntimeException("failed to read " + size);
    }
    return bytes;
  }

  //      public:
//      virtual ~Asset(void) = default;
//      static int32_t getGlobalCount();
//      static String8 getAssetAllocations();
//
//    /* used when opening an asset */
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

  //const void* _FileAsset::getBuffer(bool wordAligned)
  //{
  //    /* subsequent requests just use what we did previously */
  //    if (mBuf != NULL)
  //        return mBuf;
  //    if (mMap != NULL) {
  //        if (!wordAligned) {
  //            return  mMap->getDataPtr();
  //        }
  //        return ensureAlignment(mMap);
  //    }
  //    assert(mFp != NULL);
  //    if (mLength < kReadVsMapThreshold) {
  //        unsigned char* buf;
  //        long allocLen;
  //        /* zero-length files are allowed; not sure about zero-len allocs */
  //        /* (works fine with gcc + x86linux) */
  //        allocLen = mLength;
  //        if (mLength == 0)
  //            allocLen = 1;
  //        buf = new unsigned char[allocLen];
  //        if (buf == NULL) {
  //            ALOGE("alloc of %ld bytes failed\n", (long) allocLen);
  //            return NULL;
  //        }
  //        ALOGV("Asset %p allocating buffer size %d (smaller than threshold)", this, (int)allocLen);
  //        if (mLength > 0) {
  //            long oldPosn = ftell(mFp);
  //            fseek(mFp, mStart, SEEK_SET);
  //            if (fread(buf, 1, mLength, mFp) != (size_t) mLength) {
  //                ALOGE("failed reading %ld bytes\n", (long) mLength);
  //                delete[] buf;
  //                return NULL;
  //            }
  //            fseek(mFp, oldPosn, SEEK_SET);
  //        }
  //        ALOGV(" getBuffer: loaded into buffer\n");
  //        mBuf = buf;
  //        return mBuf;
  //    } else {
  //        FileMap* map;
  //        map = new FileMap;
  //        if (!map->create(NULL, fileno(mFp), mStart, mLength, true)) {
  //            delete map;
  //            return NULL;
  //        }
  //        ALOGV(" getBuffer: mapped\n");
  //        mMap = map;
  //        if (!wordAligned) {
  //            return  mMap->getDataPtr();
  //        }
  //        return ensureAlignment(mMap);
  //    }
  //}
  //int _FileAsset::openFileDescriptor(off64_t* outStart, off64_t* outLength) const
  //{
  //    if (mMap != NULL) {
  //        const char* fname = mMap->getFileName();
  //        if (fname == NULL) {
  //            fname = mFileName;
  //        }
  //        if (fname == NULL) {
  //            return -1;
  //        }
  //        *outStart = mMap->getDataOffset();
  //        *outLength = mMap->getDataLength();
  //        return open(fname, O_RDONLY | O_BINARY);
  //    }
  //    if (mFileName == NULL) {
  //        return -1;
  //    }
  //    *outStart = mStart;
  //    *outLength = mLength;
  //    return open(mFileName, O_RDONLY | O_BINARY);
  //}
  //const void* _FileAsset::ensureAlignment(FileMap* map)
  //{
  //    void* data = map->getDataPtr();
  //    if ((((size_t)data)&0x3) == 0) {
  //        // We can return this directly if it is aligned on a word
  //        // boundary.
  //        ALOGV("Returning aligned FileAsset %p (%s).", this,
  //                getAssetSource());
  //        return data;
  //    }
  //    // If not aligned on a word boundary, then we need to copy it into
  //    // our own buffer.
  //    ALOGV("Copying FileAsset %p (%s) to buffer size %d to make it aligned.", this,
  //            getAssetSource(), (int)mLength);
  //    unsigned char* buf = new unsigned char[mLength];
  //    if (buf == NULL) {
  //        ALOGE("alloc of %ld bytes failed\n", (long) mLength);
  //        return NULL;
  //    }
  //    memcpy(buf, data, mLength);
  //    mBuf = buf;
  //    return buf;
  //}
  /*
   * ===========================================================================
   *      _CompressedAsset
   * ===========================================================================
   */
  private static class CompressedAsset extends Asset {

    private final ZipFileRO pZipFile;
    private final ZipEntryRO entry;
    private final InputStream entryInputStream;
    private long     mStart;         // offset to start of compressed data
    private long     mCompressedLen; // length of the compressed data
    private long     mUncompressedLen; // length of the uncompressed data
    private long     mOffset;        // current offset, 0 == start of uncomp data
    //FileMap*    mMap;           // for memory-mapped input
    private int         mFd;            // for file input
    //StreamingZipInflater mZipInflater;  // for streaming large compressed assets
    private byte[]  mBuf;       // for getBuffer()

    /*
     * Constructor.
     */
  public CompressedAsset(ZipFileRO pZipFile, ZipEntryRO entry, String8 string8) {
    this.pZipFile = pZipFile;
    this.entry = entry;
    mStart = 0;
      mCompressedLen = 0;
      mUncompressedLen = 0;
      mOffset = 0;
      // mMap = null;
      mFd = -1;
      //mZipInflater = null;
      mBuf = null;

      // Register the Asset with the global list here after it is fully constructed and its
      // vtable pointer points to this concrete type. b/31113965
      registerAsset(this);

    ZipEntry zipEntry = entry.entry;
    ZipFile zipFile = pZipFile.mHandle.zipFile;
    try {
      entryInputStream = zipFile.getInputStream(zipEntry);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

    @Override
    public int getLength() {
      return (int) entry.entry.getSize();
    }

    @Override
    public long size() {
      return entry.entry.getSize();
    }

    @Override
    public int read() {
      try {
        return entryInputStream.read();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public int read(byte[] b, int off, int len) {
      try {
        return entryInputStream.read(b, off, len);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public long seek(long offset, int whence) {
      throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void close() {
      try {
        entryInputStream.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    /*
     * Destructor.  Release resources.
     */
  @Override
  public void finalize()
    {
      // close();
      // Unregister the Asset from the global list here before it is destructed and while its vtable
      // pointer still points to this concrete type. b/31113965
      unregisterAsset(this);
  }
  ///*
  // * Open a chunk of compressed data inside a file.
  // *
  // * This currently just sets up some values and returns.  On the first
  // * read, we expand the entire file into a buffer and return data from it.
  // */
  //status_t _CompressedAsset::openChunk(int fd, off64_t offset,
  //    int compressionMethod, size_t uncompressedLen, size_t compressedLen)
  //{
  //    assert(mFd < 0);        // no re-open
  //    assert(mMap == NULL);
  //    assert(fd >= 0);
  //    assert(offset >= 0);
  //    assert(compressedLen > 0);
  //    if (compressionMethod != ZipFileRO::kCompressDeflated) {
  //        assert(false);
  //        return UNKNOWN_ERROR;
  //    }
  //    mStart = offset;
  //    mCompressedLen = compressedLen;
  //    mUncompressedLen = uncompressedLen;
  //    assert(mOffset == 0);
  //    mFd = fd;
  //    assert(mBuf == NULL);
  //    if (uncompressedLen > StreamingZipInflater::OUTPUT_CHUNK_SIZE) {
  //        mZipInflater = new StreamingZipInflater(mFd, offset, uncompressedLen, compressedLen);
  //    }
  //    return NO_ERROR;
  //}
  ///*
  // * Open a chunk of compressed data in a mapped region.
  // *
  // * Nothing is expanded until the first read call.
  // */
  //status_t _CompressedAsset::openChunk(FileMap* dataMap, size_t uncompressedLen)
  //{
  //    assert(mFd < 0);        // no re-open
  //    assert(mMap == NULL);
  //    assert(dataMap != NULL);
  //    mMap = dataMap;
  //    mStart = -1;        // not used
  //    mCompressedLen = dataMap->getDataLength();
  //    mUncompressedLen = uncompressedLen;
  //    assert(mOffset == 0);
  //    if (uncompressedLen > StreamingZipInflater::OUTPUT_CHUNK_SIZE) {
  //        mZipInflater = new StreamingZipInflater(dataMap, uncompressedLen);
  //    }
  //    return NO_ERROR;
  //}
  ///*
  // * Read data from a chunk of compressed data.
  // *
  // * [For now, that's just copying data out of a buffer.]
  // */
  //ssize_t _CompressedAsset::read(void* buf, size_t count)
  //{
  //    size_t maxLen;
  //    size_t actual;
  //    assert(mOffset >= 0 && mOffset <= mUncompressedLen);
  //    /* If we're relying on a streaming inflater, go through that */
  //    if (mZipInflater) {
  //        actual = mZipInflater->read(buf, count);
  //    } else {
  //        if (mBuf == NULL) {
  //            if (getBuffer(false) == NULL)
  //                return -1;
  //        }
  //        assert(mBuf != NULL);
  //        /* adjust count if we're near EOF */
  //        maxLen = mUncompressedLen - mOffset;
  //        if (count > maxLen)
  //            count = maxLen;
  //        if (!count)
  //            return 0;
  //        /* copy from buffer */
  //        //printf("comp buf read\n");
  //        memcpy(buf, (char*)mBuf + mOffset, count);
  //        actual = count;
  //    }
  //    mOffset += actual;
  //    return actual;
  //}
  ///*
  // * Handle a seek request.
  // *
  // * If we're working in a streaming mode, this is going to be fairly
  // * expensive, because it requires plowing through a bunch of compressed
  // * data.
  // */
  //off64_t _CompressedAsset::seek(off64_t offset, int whence)
  //{
  //    off64_t newPosn;
  //    // compute new position within chunk
  //    newPosn = handleSeek(offset, whence, mOffset, mUncompressedLen);
  //    if (newPosn == (off64_t) -1)
  //        return newPosn;
  //    if (mZipInflater) {
  //        mZipInflater->seekAbsolute(newPosn);
  //    }
  //    mOffset = newPosn;
  //    return mOffset;
  //}
  ///*
  // * Close the asset.
  // */
  //void _CompressedAsset::close(void)
  //{
  //    if (mMap != NULL) {
  //        delete mMap;
  //        mMap = NULL;
  //    }
  //    delete[] mBuf;
  //    mBuf = NULL;
  //    delete mZipInflater;
  //    mZipInflater = NULL;
  //    if (mFd > 0) {
  //        ::close(mFd);
  //        mFd = -1;
  //    }
  //}
  ///*
  // * Get a pointer to a read-only buffer of data.
  // *
  // * The first time this is called, we expand the compressed data into a
  // * buffer.
  // */
  //const void* _CompressedAsset::getBuffer(bool)
  //{
  //    unsigned char* buf = NULL;
  //    if (mBuf != NULL)
  //        return mBuf;
  //    /*
  //     * Allocate a buffer and read the file into it.
  //     */
  //    buf = new unsigned char[mUncompressedLen];
  //    if (buf == NULL) {
  //        ALOGW("alloc %ld bytes failed\n", (long) mUncompressedLen);
  //        goto bail;
  //    }
  //    if (mMap != NULL) {
  //        if (!ZipUtils::inflateToBuffer(mMap->getDataPtr(), buf,
  //                mUncompressedLen, mCompressedLen))
  //            goto bail;
  //    } else {
  //        assert(mFd >= 0);
  //        /*
  //         * Seek to the start of the compressed data.
  //         */
  //        if (lseek(mFd, mStart, SEEK_SET) != mStart)
  //            goto bail;
  //        /*
  //         * Expand the data into it.
  //         */
  //        if (!ZipUtils::inflateToBuffer(mFd, buf, mUncompressedLen,
  //                mCompressedLen))
  //            goto bail;
  //    }
  //    /*
  //     * Success - now that we have the full asset in RAM we
  //     * no longer need the streaming inflater
  //     */
  //    delete mZipInflater;
  //    mZipInflater = NULL;
  //    mBuf = buf;
  //    buf = NULL;
  //bail:
  //    delete[] buf;
  //    return mBuf;
  //
  }
}
