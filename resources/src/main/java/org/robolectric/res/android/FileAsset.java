package org.robolectric.res.android;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Class providing access to a read-only asset.  Asset objects are NOT thread-safe, and should not
 * be shared across threads.
 *
 * transliterated from https://android.googlesource.com/platform/frameworks/base/+/android-7.1.1_r13/include/androidfw/Asset.h
 * and https://android.googlesource.com/platform/frameworks/base/+/android-7.1.1_r13/libs/androidfw/Asset.cpp
 *
 * Instances of this class provide read-only operations on a byte stream.
 * Access may
 * be optimized for streaming, random, or whole buffer modes.  All operations are supported
 * regardless of how the file was opened, but some things will be less efficient.  [pass that
 * in??]. "Asset" is the base class for all types of assets.  The classes below
 * provide most of the implementation.  The AssetManager uses one of the static "create"
 * functions defined here to create a new instance.
 */
public class FileAsset extends Asset {
  protected final RandomAccessFile f;

  public FileAsset(RandomAccessFile f) {
    this.f = f;
  }

  @Override
  public int getLength() {
    return (int) size();
  }

  @Override
  public long getRemainingLength() {
    try {
      return f.length() - f.getFilePointer();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public long size() {
    try {
      return f.length();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public int read() {
    try {
      return f.read();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public int read(byte[] b, int off, int len) {
    try {
      return f.read(b, off, len);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public long seek(long offset, int whence) {
    throw new UnsupportedOperationException("not yet implemented");
//    if (whence > 0) {
//      SEEK_END
//    } else if (whence < 0) {
//      SEEK_SET
//    } else {
//      SEEK_CUR
//    }
//    return f.seek();
  }

  @Override
  public void close() {
    if (onClose != null) {
      onClose.run();
    }
    try {
      f.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  //

  /*
 * Create a new Asset from a compressed file on disk.  There is a fair chance
 * that the file doesn't actually exist.
 *
 * We currently support gzip files.  We might want to handle .bz2 someday.
 */

  //#if 0
///*
// * Create a new Asset from part of an open file.
// */
///*static*/ Asset* Asset::createFromFileSegment(int fd, off64_t offset,
//    size_t length, AccessMode mode)
//{
//    _FileAsset* pAsset;
//    status_t result;
//    pAsset = new _FileAsset;
//    result = pAsset->openChunk(NULL, fd, offset, length);
//    if (result != NO_ERROR)
//        return NULL;
//    pAsset->mAccessMode = mode;
//    return pAsset;
//}
///*
// * Create a new Asset from compressed data in an open file.
// */
///*static*/ Asset* Asset::createFromCompressedData(int fd, off64_t offset,
//    int compressionMethod, size_t uncompressedLen, size_t compressedLen,
//    AccessMode mode)
//{
//    _CompressedAsset* pAsset;
//    status_t result;
//    pAsset = new _CompressedAsset;
//    result = pAsset->openChunk(fd, offset, compressionMethod,
//                uncompressedLen, compressedLen);
//    if (result != NO_ERROR)
//        return NULL;
//    pAsset->mAccessMode = mode;
//    return pAsset;
//}
//#endif
///*
// * Create a new Asset from a memory mapping.
// */
///*static*/ Asset* Asset::createFromUncompressedMap(FileMap* dataMap,
//    AccessMode mode)
//{
//    _FileAsset* pAsset;
//    status_t result;
//    pAsset = new _FileAsset;
//    result = pAsset->openChunk(dataMap);
//    if (result != NO_ERROR)
//        return NULL;
//    pAsset->mAccessMode = mode;
//    return pAsset;
//}
///*
// * Create a new Asset from compressed data in a memory mapping.
// */
///*static*/ Asset* Asset::createFromCompressedMap(FileMap* dataMap,
//    size_t uncompressedLen, AccessMode mode)
//{
//    _CompressedAsset* pAsset;
//    status_t result;
//    pAsset = new _CompressedAsset;
//    result = pAsset->openChunk(dataMap, uncompressedLen);
//    if (result != NO_ERROR)
//        return NULL;
//    pAsset->mAccessMode = mode;
//    return pAsset;
//}
///*
// * Do generic seek() housekeeping.  Pass in the offset/whence values from
// * the seek request, along with the current chunk offset and the chunk
// * length.
// *
// * Returns the new chunk offset, or -1 if the seek is illegal.
// */
//long handleSeek(long offset, int whence, long curPosn, long maxPosn)
//{
//    long newOffset;
//    switch (whence) {
//    case SEEK_SET:
//        newOffset = offset;
//        break;
//    case SEEK_CUR:
//        newOffset = curPosn + offset;
//        break;
//    case SEEK_END:
//        newOffset = maxPosn + offset;
//        break;
//    default:
//        ALOGW("unexpected whence %d\n", whence);
//        // this was happening due to an off64_t size mismatch
//        assert(false);
//        return (off64_t) -1;
//    }
//    if (newOffset < 0 || newOffset > maxPosn) {
//        ALOGW("seek out of range: want %ld, end=%ld\n",
//            (long) newOffset, (long) maxPosn);
//        return (off64_t) -1;
//    }
//    return newOffset;
//}
///*
// * ===========================================================================
// *      _FileAsset
// * ===========================================================================
// */
///*
// * Constructor.
// */
//_FileAsset::_FileAsset(void)
//    : mStart(0), mLength(0), mOffset(0), mFp(NULL), mFileName(NULL), mMap(NULL), mBuf(NULL)
//{
//    // Register the Asset with the global list here after it is fully constructed and its
//    // vtable pointer points to this concrete type. b/31113965
//    registerAsset(this);
//}
///*
// * Destructor.  Release resources.
// */
//_FileAsset::~_FileAsset(void)
//{
//    close();
//    // Unregister the Asset from the global list here before it is destructed and while its vtable
//    // pointer still points to this concrete type. b/31113965
//    unregisterAsset(this);
//}
///*
// * Operate on a chunk of an uncompressed file.
// *
// * Zero-length chunks are allowed.
// */
//status_t _FileAsset::openChunk(const char* fileName, int fd, off64_t offset, size_t length)
//{
//    assert(mFp == NULL);    // no reopen
//    assert(mMap == NULL);
//    assert(fd >= 0);
//    assert(offset >= 0);
//    /*
//     * Seek to end to get file length.
//     */
//    off64_t fileLength;
//    fileLength = lseek64(fd, 0, SEEK_END);
//    if (fileLength == (off64_t) -1) {
//        // probably a bad file descriptor
//        ALOGD("failed lseek (errno=%d)\n", errno);
//        return UNKNOWN_ERROR;
//    }
//    if ((off64_t) (offset + length) > fileLength) {
//        ALOGD("start (%ld) + len (%ld) > end (%ld)\n",
//            (long) offset, (long) length, (long) fileLength);
//        return BAD_INDEX;
//    }
//    /* after fdopen, the fd will be closed on fclose() */
//    mFp = fdopen(fd, "rb");
//    if (mFp == NULL)
//        return UNKNOWN_ERROR;
//    mStart = offset;
//    mLength = length;
//    assert(mOffset == 0);
//    /* seek the FILE* to the start of chunk */
//    if (fseek(mFp, mStart, SEEK_SET) != 0) {
//        assert(false);
//    }
//    mFileName = fileName != NULL ? strdup(fileName) : NULL;
//    return NO_ERROR;
//}
///*
// * Create the chunk from the map.
// */
//status_t _FileAsset::openChunk(FileMap* dataMap)
//{
//    assert(mFp == NULL);    // no reopen
//    assert(mMap == NULL);
//    assert(dataMap != NULL);
//    mMap = dataMap;
//    mStart = -1;            // not used
//    mLength = dataMap->getDataLength();
//    assert(mOffset == 0);
//    return NO_ERROR;
//}
///*
// * Read a chunk of data.
// */
//ssize_t _FileAsset::read(void* buf, size_t count)
//{
//    size_t maxLen;
//    size_t actual;
//    assert(mOffset >= 0 && mOffset <= mLength);
//    if (getAccessMode() == ACCESS_BUFFER) {
//        /*
//         * On first access, read or map the entire file.  The caller has
//         * requested buffer access, either because they're going to be
//         * using the buffer or because what they're doing has appropriate
//         * performance needs and access patterns.
//         */
//        if (mBuf == NULL)
//            getBuffer(false);
//    }
//    /* adjust count if we're near EOF */
//    maxLen = mLength - mOffset;
//    if (count > maxLen)
//        count = maxLen;
//    if (!count)
//        return 0;
//    if (mMap != NULL) {
//        /* copy from mapped area */
//        //printf("map read\n");
//        memcpy(buf, (char*)mMap->getDataPtr() + mOffset, count);
//        actual = count;
//    } else if (mBuf != NULL) {
//        /* copy from buffer */
//        //printf("buf read\n");
//        memcpy(buf, (char*)mBuf + mOffset, count);
//        actual = count;
//    } else {
//        /* read from the file */
//        //printf("file read\n");
//        if (ftell(mFp) != mStart + mOffset) {
//            ALOGE("Hosed: %ld != %ld+%ld\n",
//                ftell(mFp), (long) mStart, (long) mOffset);
//            assert(false);
//        }
//        /*
//         * This returns 0 on error or eof.  We need to use ferror() or feof()
//         * to tell the difference, but we don't currently have those on the
//         * device.  However, we know how much data is *supposed* to be in the
//         * file, so if we don't read the full amount we know something is
//         * hosed.
//         */
//        actual = fread(buf, 1, count, mFp);
//        if (actual == 0)        // something failed -- I/O error?
//            return -1;
//        assert(actual == count);
//    }
//    mOffset += actual;
//    return actual;
//}
///*
// * Seek to a new position.
// */
//off64_t _FileAsset::seek(off64_t offset, int whence)
//{
//    off64_t newPosn;
//    off64_t actualOffset;
//    // compute new position within chunk
//    newPosn = handleSeek(offset, whence, mOffset, mLength);
//    if (newPosn == (off64_t) -1)
//        return newPosn;
//    actualOffset = mStart + newPosn;
//    if (mFp != NULL) {
//        if (fseek(mFp, (long) actualOffset, SEEK_SET) != 0)
//            return (off64_t) -1;
//    }
//    mOffset = actualOffset - mStart;
//    return mOffset;
//}
///*
// * Close the asset.
// */
//void _FileAsset::close(void)
//{
//    if (mMap != NULL) {
//        delete mMap;
//        mMap = NULL;
//    }
//    if (mBuf != NULL) {
//        delete[] mBuf;
//        mBuf = NULL;
//    }
//    if (mFileName != NULL) {
//        free(mFileName);
//        mFileName = NULL;
//    }
//    if (mFp != NULL) {
//        // can only be NULL when called from destructor
//        // (otherwise we would never return this object)
//        fclose(mFp);
//        mFp = NULL;
//    }
//}
}
