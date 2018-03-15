package org.robolectric.res.android;

import static org.robolectric.res.android.Errors.BAD_TYPE;
import static org.robolectric.res.android.Errors.NO_ERROR;
import static org.robolectric.res.android.Errors.NO_INIT;
import static org.robolectric.res.android.ResTable.kDebugResXMLTree;
import static org.robolectric.res.android.ResTable.kDebugXMLNoisy;
import static org.robolectric.res.android.ResXMLParser.SIZEOF_RESXMLTREE_ATTR_EXT;
import static org.robolectric.res.android.ResXMLParser.SIZEOF_RESXMLTREE_NODE;
import static org.robolectric.res.android.ResXMLParser.event_code_t.BAD_DOCUMENT;
import static org.robolectric.res.android.ResXMLParser.event_code_t.START_DOCUMENT;
import static org.robolectric.res.android.ResourceTypes.RES_STRING_POOL_TYPE;
import static org.robolectric.res.android.ResourceTypes.RES_XML_FIRST_CHUNK_TYPE;
import static org.robolectric.res.android.ResourceTypes.RES_XML_LAST_CHUNK_TYPE;
import static org.robolectric.res.android.ResourceTypes.RES_XML_RESOURCE_MAP_TYPE;
import static org.robolectric.res.android.ResourceTypes.RES_XML_START_ELEMENT_TYPE;
import static org.robolectric.res.android.ResourceTypes.validate_chunk;
import static org.robolectric.res.android.Util.ALOGI;
import static org.robolectric.res.android.Util.ALOGW;
import static org.robolectric.res.android.Util.SIZEOF_INT;
import static org.robolectric.res.android.Util.dtohl;
import static org.robolectric.res.android.Util.dtohs;
import static org.robolectric.res.android.Util.isTruthy;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicInteger;
import org.robolectric.res.android.ResourceTypes.ResChunk_header;
import org.robolectric.res.android.ResourceTypes.ResXMLTree_attrExt;
import org.robolectric.res.android.ResourceTypes.ResXMLTree_header;
import org.robolectric.res.android.ResourceTypes.ResXMLTree_node;

public class ResXMLTree {

  final DynamicRefTable mDynamicRefTable;
  public final ResXMLParser mParser;

  int                    mError;
  byte[]                       mOwnedData;
  XmlBuffer mBuffer;
    ResXMLTree_header mHeader;
  int                      mSize;
  //    final uint8_t*              mDataEnd;
  int mDataLen;
  ResStringPool               mStrings = new ResStringPool();
    int[]             mResIds;
  int                      mNumResIds;
    ResXMLTree_node mRootNode;
    int                 mRootExt;
  int                mRootCode;

  static volatile AtomicInteger gCount = new AtomicInteger(0);

  public ResXMLTree(DynamicRefTable dynamicRefTable) {
    mParser = new ResXMLParser(this);

    mDynamicRefTable = dynamicRefTable;
    mError = NO_INIT;
    mOwnedData = null;

    if (kDebugResXMLTree) {
      ALOGI("Creating ResXMLTree %s #%d\n", this, gCount.getAndIncrement()+1);
    }
    mParser.restart();
  }

//  ResXMLTree() {
//    this(null);
//  }

//  ~ResXMLTree()
//  {
  @Override
  protected void finalize() {
    if (kDebugResXMLTree) {
      ALOGI("Destroying ResXMLTree in %s #%d\n", this, gCount.getAndDecrement()-1);
    }
    uninit();
  }

  public int setTo(byte[] data, int size, boolean copyData)
  {
    uninit();
    mParser.mEventCode = START_DOCUMENT;

    if (!isTruthy(data) || !isTruthy(size)) {
      return (mError=BAD_TYPE);
    }

    if (copyData) {
      mOwnedData = new byte[size];
//      if (mOwnedData == null) {
//        return (mError=NO_MEMORY);
//      }
//      memcpy(mOwnedData, data, size);
      System.arraycopy(data, 0, mOwnedData, 0, size);
      data = mOwnedData;
    }

    mBuffer = new XmlBuffer(data);
    mHeader = new ResXMLTree_header(mBuffer.buf, 0);
    mSize = dtohl(mHeader.header.size);
    if (dtohs(mHeader.header.headerSize) > mSize || mSize > size) {
      ALOGW("Bad XML block: header size %d or total size %d is larger than data size %d\n",
          (int)dtohs(mHeader.header.headerSize),
          (int)dtohl(mHeader.header.size), (int)size);
      mError = BAD_TYPE;
      mParser.restart();
      return mError;
    }
//    mDataEnd = ((final uint8_t*)mHeader) + mSize;
    mDataLen = mSize;

    mStrings.uninit();
    mRootNode = null;
    mResIds = null;
    mNumResIds = 0;

    // First look for a couple interesting chunks: the string block
    // and first XML node.
    ResChunk_header chunk =
//      (final ResChunk_header*)(((final uint8_t*)mHeader) + dtohs(mHeader.header.headerSize));
        new ResChunk_header(mBuffer.buf, mHeader.header.headerSize);

    ResChunk_header lastChunk = chunk;
    while (chunk.myOffset() /*((final uint8_t*)chunk)*/ < (mDataLen- ResChunk_header.SIZEOF /*sizeof(ResChunk_header)*/) &&
        chunk.myOffset() /*((final uint8_t*)chunk)*/ < (mDataLen-dtohl(chunk.size))) {
      int err = validate_chunk(chunk, ResChunk_header.SIZEOF /*sizeof(ResChunk_header)*/, mDataLen, "XML");
      if (err != NO_ERROR) {
        mError = err;
//          goto done;
        mParser.restart();
        return mError;
      }
      final short type = dtohs(chunk.type);
      final int size1 = dtohl(chunk.size);
      if (kDebugXMLNoisy) {
//        System.out.println(String.format("Scanning @ %s: type=0x%x, size=0x%zx\n",
//            (void*)(((uintptr_t)chunk)-((uintptr_t)mHeader)), type, size1);
      }
      if (type == RES_STRING_POOL_TYPE) {
        mStrings.setTo(mBuffer.buf, chunk.myOffset(), size, false);
      } else if (type == RES_XML_RESOURCE_MAP_TYPE) {
//        mResIds = (final int*)
//        (((final uint8_t*)chunk)+dtohs(chunk.headerSize()));
        mNumResIds = (dtohl(chunk.size)-dtohs(chunk.headerSize))/SIZEOF_INT /*sizeof(int)*/;
        mResIds = new int[mNumResIds];
        for (int i = 0; i < mNumResIds; i++) {
          mResIds[i] = mBuffer.buf.getInt(chunk.myOffset() + chunk.headerSize + i * SIZEOF_INT);
        }
      } else if (type >= RES_XML_FIRST_CHUNK_TYPE
          && type <= RES_XML_LAST_CHUNK_TYPE) {
        if (validateNode(new ResXMLTree_node(mBuffer.buf, chunk)) != NO_ERROR) {
          mError = BAD_TYPE;
//          goto done;
          mParser.restart();
          return mError;
        }
        mParser.mCurNode = new ResXMLTree_node(mBuffer.buf, lastChunk.myOffset());
        if (mParser.nextNode() == BAD_DOCUMENT) {
          mError = BAD_TYPE;
//          goto done;
          mParser.restart();
          return mError;
        }
        mRootNode = mParser.mCurNode;
        mRootExt = mParser.mCurExt;
        mRootCode = mParser.mEventCode;
        break;
      } else {
        if (kDebugXMLNoisy) {
          System.out.println("Skipping unknown chunk!\n");
        }
      }
      lastChunk = chunk;
//      chunk = (final ResChunk_header*)
//      (((final uint8_t*)chunk) + size1);
      chunk = new ResChunk_header(mBuffer.buf, chunk.myOffset() + size1);
  }

    if (mRootNode == null) {
      ALOGW("Bad XML block: no root element node found\n");
      mError = BAD_TYPE;
//          goto done;
      mParser.restart();
      return mError;
    }

    mError = mStrings.getError();

  done:
    mParser.restart();
    return mError;
  }

  public int getError()
  {
    return mError;
  }

  void uninit()
  {
    mError = NO_INIT;
    mStrings.uninit();
    if (isTruthy(mOwnedData)) {
//      free(mOwnedData);
      mOwnedData = null;
    }
    mParser.restart();
  }

  int validateNode(final ResXMLTree_node node)
  {
    final short eventCode = dtohs(node.header.type);

    int err = validate_chunk(
        node.header, SIZEOF_RESXMLTREE_NODE /*sizeof(ResXMLTree_node)*/,
      mDataLen, "ResXMLTree_node");

    if (err >= NO_ERROR) {
      // Only perform additional validation on START nodes
      if (eventCode != RES_XML_START_ELEMENT_TYPE) {
        return NO_ERROR;
      }

        final short headerSize = dtohs(node.header.headerSize);
        final int size = dtohl(node.header.size);
//        final ResXMLTree_attrExt attrExt = (final ResXMLTree_attrExt*)
//      (((final uint8_t*)node) + headerSize);
      ResXMLTree_attrExt attrExt = new ResXMLTree_attrExt(mBuffer.buf, node.myOffset() + headerSize);
      // check for sensical values pulled out of the stream so far...
      if ((size >= headerSize + SIZEOF_RESXMLTREE_ATTR_EXT /*sizeof(ResXMLTree_attrExt)*/)
          && (attrExt.myOffset() > node.myOffset())) {
            final int attrSize = ((int)dtohs(attrExt.attributeSize))
            * dtohs(attrExt.attributeCount);
        if ((dtohs(attrExt.attributeStart)+attrSize) <= (size-headerSize)) {
          return NO_ERROR;
        }
        ALOGW("Bad XML block: node attributes use 0x%x bytes, only have 0x%x bytes\n",
            (int)(dtohs(attrExt.attributeStart)+attrSize),
            (int)(size-headerSize));
      }
        else {
        ALOGW("Bad XML start block: node header size 0x%x, size 0x%x\n",
            (int)headerSize, (int)size);
      }
      return BAD_TYPE;
    }

    return err;

//    if (false) {
//      final boolean isStart = dtohs(node.header().type()) == RES_XML_START_ELEMENT_TYPE;
//
//      final short headerSize = dtohs(node.header().headerSize());
//      final int size = dtohl(node.header().size());
//
//      if (headerSize >= (isStart ? sizeof(ResXMLTree_attrNode) : sizeof(ResXMLTree_node))) {
//        if (size >= headerSize) {
//          if ((( final uint8_t*)node) <=(mDataEnd - size)){
//            if (!isStart) {
//              return NO_ERROR;
//            }
//            if ((((int) dtohs(node.attributeSize)) * dtohs(node.attributeCount))
//                <= (size - headerSize)) {
//              return NO_ERROR;
//            }
//            ALOGW("Bad XML block: node attributes use 0x%x bytes, only have 0x%x bytes\n",
//                ((int) dtohs(node.attributeSize)) * dtohs(node.attributeCount),
//                (int) (size - headerSize));
//            return BAD_TYPE;
//          }
//          ALOGW("Bad XML block: node at 0x%x extends beyond data end 0x%x\n",
//              (int) ((( final uint8_t*)node)-(( final uint8_t*)mHeader)),(int) mSize);
//          return BAD_TYPE;
//        }
//        ALOGW("Bad XML block: node at 0x%x header size 0x%x smaller than total size 0x%x\n",
//            (int) ((( final uint8_t*)node)-(( final uint8_t*)mHeader)),
//        (int) headerSize, (int) size);
//        return BAD_TYPE;
//      }
//      ALOGW("Bad XML block: node at 0x%x header size 0x%x too small\n",
//          (int) ((( final uint8_t*)node)-(( final uint8_t*)mHeader)),
//      (int) headerSize);
//      return BAD_TYPE;
//    }
  }

  public ResStringPool getStrings() {
    return mParser.getStrings();
  }

  static class XmlBuffer {
    final ByteBuffer buf;

    public XmlBuffer(byte[] data) {
      this.buf = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
    }
  }
}
