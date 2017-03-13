package org.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.robolectric.res.builder.XmlBlock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ResStore {

  public static final int TYPE_STRING = -1;
  public static final int TYPE_OTHER = -2;

  public void save(PackageResourceTable resourceTable, File file) throws IOException {
    final SaveContext saveContext = new SaveContext();

    resourceTable.receive(new ResourceTable.Visitor() {
      @Override
      public void visit(ResName key, Iterable<TypedResource> values) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);

        try {
          int count = 0;
          for (TypedResource ignored : values) count++;
          out.writeShort(count);

          List<byte[]> qBytes = new ArrayList<>();
          ByteArrayOutputStream qB = new ByteArrayOutputStream();

          for (TypedResource value : values) {
            qB.reset();
            bytesOf(value, qB);

            out.writeUTF(value.getQualifiers());
            out.writeInt(qB.size());
            qBytes.add(qB.toByteArray());
          }

          for (byte[] qByte : qBytes) {
            out.write(qByte);
          }

          saveContext.resources.put(key.getFullyQualifiedName(), bytes.toByteArray());
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }

      private void bytesOf(TypedResource value, ByteArrayOutputStream bytes) throws IOException {
        DataOutputStream out = new DataOutputStream(bytes);
        saveContext.writeTypedResource(value, out);
      }

    });

    final RandomAccessFile f = new RandomAccessFile(file, "rw");
    f.setLength(0);

    f.writeInt(-1);
    f.writeInt(saveContext.resources.size());
    long resOffsetPos = f.getFilePointer();
    f.writeLong(0); // offset to resource data
    f.writeLong(0); // offset to string data

    List<byte[]> rscBytes = new ArrayList<>(saveContext.resources.size());
    for (Map.Entry<String, byte[]> rsc : saveContext.resources.entrySet()) {
      f.writeUTF(rsc.getKey());
      byte[] value = rsc.getValue();
      f.writeInt(value.length);
      rscBytes.add(value);
    }

    f.writeUTF("!!!START OF RESOURCES!!!");
    long dataOffset = f.getFilePointer();
    for (byte[] b : rscBytes) {
      f.write(b);
    }

    long stringOffset = 0;

    f.seek(resOffsetPos);
    f.writeLong(dataOffset);
    f.writeLong(stringOffset);

    saveContext.doThingsWithStrings();
  }

  public ResourceTable load(File file) throws IOException {
    final RandomAccessFile f = new RandomAccessFile(file, "r");
    int ignored = f.readInt();
    final int totalResources = f.readInt();
    final long resourceDataOffset = f.readLong();
    final long stringsOffset = f.readLong();

    System.out.println("resourceDataOffset = " + resourceDataOffset);
    System.out.println("totalResources = " + totalResources);

    final Map<String, ResPtr> resourceIndexes = new HashMap<>();
    int pos = 0;
    for (int i = 0; i < totalResources; i++) {
      String key = f.readUTF();
      int len = f.readInt();
//      System.out.println("key = " + key + " " + len);
      resourceIndexes.put(key, new ResPtr(pos, len));
      pos += len;
    }

    return new ResourceTable() {
      @Override
      public Integer getResourceId(ResName resName) {
        return null;
      }

      @Override
      public ResName getResName(int resourceId) {
        return null;
      }

      @Override
      public TypedResource getValue(int resId, String qualifiers) {
        return null;
      }

      @Override
      public TypedResource getValue(@NotNull ResName resName, String qualifiers) {
        return null;
      }

      @Override
      public XmlBlock getXml(ResName resName, String qualifiers) {
        return null;
      }

      @Override
      public InputStream getRawValue(ResName resName, String qualifiers) {
        return null;
      }

      @Override
      public InputStream getRawValue(int resId, String qualifiers) {
        return null;
      }

      @Override
      public void receive(Visitor visitor) {
        try {
          for (Map.Entry<String, ResPtr> entry : resourceIndexes.entrySet()) {
            ResName resName = new ResName(entry.getKey());
            ResQPtr[] resQPtrs = getResQPtrs(entry.getValue());
            List<TypedResource> typedResources = new ArrayList<>(resQPtrs.length);
            for (int i = 0; i < resQPtrs.length; i++) {
              ResQPtr resQPtr = resQPtrs[i];
              typedResources.add(readTypedResource(f, resQPtr));
            }

            visitor.visit(resName, typedResources);
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }

      @NotNull
      private ResQPtr[] getResQPtrs(ResPtr resPtr) {
        try {
          f.seek(resourceDataOffset + resPtr.offset);
          short count = f.readShort();
          ResQPtr[] resQPtrs = new ResQPtr[count];
          for (int i = 0; i < count; i++) {
            String qualifiers = f.readUTF();
            int size = f.readInt();
            resQPtrs[i] = new ResQPtr(qualifiers, 0, size);
          }
          long pos = f.getFilePointer();
          for (int i = 0; i < count; i++) {
            ResQPtr resQPtr = resQPtrs[i];
            resQPtr.offset = pos;
            pos += resQPtr.length;
          }
          return resQPtrs;
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  private static class ResQPtr {
    String qualifiers;
    long offset;
    int length;

    public ResQPtr(String qualifiers, long offset, int length) {
      this.qualifiers = qualifiers;
      this.offset = offset;
      this.length = length;
    }
  }

  private static class ResPtr {
    int offset;
    int length;

    public ResPtr(int offset, int length) {
      this.offset = offset;
      this.length = length;
    }
  }

  private static class StringEntry {
    private final String string;
    int count = 0;
    int index;

    private StringEntry(String string, int index) {
      this.string = string;
      this.index = index;
    }
  }

  private static class SaveContext {
    Map<String, byte[]> resources = new HashMap<>();
    Map<String, StringEntry> strings = new HashMap<>();
    int nextIndex = 0;

    public void resource(ResName resName, byte[] bytes) {
    }

    private void writeTypedResource(TypedResource typedResource, DataOutputStream out) throws IOException {
      Object data = typedResource.getRawData();
      boolean isStringValue = typedResource.getClass().equals(TypedResource.class) && data instanceof String;

      if (isStringValue) {
        out.writeByte(TYPE_STRING);
        writeString(out, typedResource.getXmlContext().getPackageName());
        writeString(out, typedResource.getResType().name());
        writeString(out, typedResource.getXmlContext().getXmlFile().getPath());
        writeString(out, (String) data);
      } else {
        out.writeByte(TYPE_OTHER);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(baos);
        objOut.writeObject(typedResource);
        out.writeInt(baos.size());
        out.write(baos.toByteArray());
      }
    }

    private void writeString(DataOutputStream out, String s) throws IOException {
//      out.writeShort(1);
      out.writeUTF(s);
      string(s);
    }

    public int string(String s) {
      StringEntry stringEntry = strings.get(s);
      if (stringEntry == null) {
        stringEntry = new StringEntry(s, nextIndex);
        nextIndex++;
        strings.put(s, stringEntry);
      }
      stringEntry.count++;
      return stringEntry.index;
    }

    public void doThingsWithStrings() {
      Collection<StringEntry> values = strings.values();
      StringEntry[] stringEntries = values.toArray(new StringEntry[values.size()]);
      Arrays.sort(stringEntries, new Comparator<StringEntry>() {
        @Override
        public int compare(StringEntry a, StringEntry b) {
          return b.count - a.count;
        }
      });

      int i = 0;
      int iBefore1m = 0;
      int m = 0;
      for (StringEntry stringEntry : stringEntries) {
        i++;
        if (m < 64 * 1024) {
          iBefore1m++;
        }
        m += stringEntry.string.length() + 1;
      }
      System.out.println(i + " total strings");
      System.out.println(iBefore1m + " before 1M");
      System.out.println(m + " total bytes");
    }
  }

  private TypedResource readTypedResource(RandomAccessFile f, ResQPtr resQPtr) throws IOException {
    f.seek(resQPtr.offset);
    byte b = f.readByte();
    switch (b) {
      case TYPE_STRING:
        String packageName = readString(f);
        ResType resType = ResType.valueOf(readString(f));
        String file = readString(f);
        String data = readString(f);
        return new TypedResource<>(data, resType, new XmlContext(packageName, Fs.fileFromPath(file)));
      case TYPE_OTHER:
        int objSize = f.readInt();
        byte[] bytes = new byte[objSize];
        f.read(bytes);
        try {
          return (TypedResource) new ObjectInputStream(new ByteArrayInputStream(bytes)).readObject();
        } catch (ClassNotFoundException e) {
          throw new RuntimeException(e);
        }
    }

    throw new IllegalStateException("unknown type " + b);
  }

  private String readString(RandomAccessFile f) throws IOException {
    return f.readUTF();
  }

}
