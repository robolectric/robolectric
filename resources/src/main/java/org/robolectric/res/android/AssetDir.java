package org.robolectric.res.android;

import org.robolectric.res.android.CppAssetManager.FileType;

// transliterated from https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/libs/androidfw/AssetDir.cpp and
// https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/include/androidfw/AssetDir.h
public class AssetDir {

  private SortedVector<FileInfo> mFileInfo;

  AssetDir() {
    mFileInfo = null;
  }

  AssetDir(AssetDir src) {

  }

  /*
 * Vector-style access.
 */
  public int getFileCount() {
    return mFileInfo.size();
  }

  public String8 getFileName(int idx) {
    return mFileInfo.itemAt(idx).getFileName();
  }

//    const String8& getSourceName(int idx) {
//    return mFileInfo->itemAt(idx).getSourceName();
//  }

  /*
   * Get the type of a file (usually regular or directory).
   */
//  FileType getFileType(int idx) {
//    return mFileInfo->itemAt(idx).getFileType();
//  }

  /**
   * This holds information about files in the asset hierarchy.
   */
  static class FileInfo implements Comparable<FileInfo> {
    private String8    mFileName;    // filename only
    private FileType mFileType;      // regular, directory, etc
    private String8    mSourceName;  // currently debug-only

    FileInfo() {}

    FileInfo(String8 path) {      // useful for e.g. svect.indexOf
            mFileName = path;
            mFileType = FileType.kFileTypeUnknown;
    }

    FileInfo(FileInfo src) {
      copyMembers(src);
    }
//        const FileInfo& operator= (const FileInfo& src) {
//      if (this != &src)
//        copyMembers(src);
//      return *this;
//    }

    void copyMembers(final FileInfo src) {
      mFileName = src.mFileName;
      mFileType = src.mFileType;
      mSourceName = src.mSourceName;
    }

    /* need this for SortedVector; must compare only on file name */
//    bool operator< (const FileInfo& rhs) const {
//      return mFileName < rhs.mFileName;
//    }
//
//    /* used by AssetManager */
//    bool operator== (const FileInfo& rhs) const {
//      return mFileName == rhs.mFileName;
//    }

    void set(final String8 path, FileType type) {
      mFileName = path;
      mFileType = type;
    }

    String8 getFileName()  { return mFileName; }
    void setFileName(String8 path) { mFileName = path; }

    FileType getFileType() { return mFileType; }
    void setFileType(FileType type) { mFileType = type; }

    String8 getSourceName() { return mSourceName; }
    void setSourceName(String8 path) { mSourceName = path; }

    public boolean isLessThan(FileInfo fileInfo) {
      return mFileName.string().compareTo(fileInfo.mFileName.string()) < 0;
    }

    @Override
    public int compareTo(FileInfo other) {
      return mFileName.string().compareTo(other.mFileName.string());
    }

    /*
     * Handy utility for finding an entry in a sorted vector of FileInfo.
     * Returns the index of the matching entry, or -1 if none found.
     */
    static int findEntry(SortedVector<FileInfo> pVector,
             String8 fileName) {
      FileInfo tmpInfo = new FileInfo();

      tmpInfo.setFileName(fileName);
      return pVector.indexOf(tmpInfo);
    }


  };

  /* AssetManager uses this to initialize us */
  void setFileList(SortedVector<FileInfo> list) { mFileInfo = list; }

}
