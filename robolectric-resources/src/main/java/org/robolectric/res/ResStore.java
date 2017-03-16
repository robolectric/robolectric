package org.robolectric.res;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.jetbrains.annotations.NotNull;
import org.robolectric.res.builder.XmlBlock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResStore {
  enum DataType {
    STRING,
    FILE,
    LIST_OF_TYPED_RESOURCES,
    SERIALIZABLE
  }

  public PackageResourceTable load(Path resCachePath) throws IOException {
    return new LoadContext(resCachePath).load();
  }

  public void save(final PackageResourceTable packageResourceTable, Path resCachePath) throws IOException {
    final SaveContext saveContext = new SaveContext(resCachePath);
    packageResourceTable.receive(new ResourceTable.Visitor() {
      @Override
      public void visit(ResName key, Collection<TypedResource> values) {
        Integer resourceId = packageResourceTable.getResourceId(key);
        if ("app_name".equals(key.name)) {
          System.out.println();
        }
        saveContext.add(key, resourceId, values);
      }
    });
    saveContext.save(packageResourceTable.getPackageName(), packageResourceTable.getPackageIdentifier());
  }

  private class SaveContext {
    /*

    Format:

    // header
    int magic = -1;
    int reserved = 0;
    int numberOfResources;
    long resourceIndexOffset;
    long stringsOffset;
    strid packageName
    int package

    // resource table
    {
      // one resource
      short numberOfVariants;
      {
        strzid qualifiers;
        zint length;
      }*(numberOfVariants);

      {
        strzid resType;
        strzid xmlFile;
        zint line;
        byte dataType;
        strzid stringData || serializable;
      }*(numberOfVariants);
    }*(numberOfEntries);

    // resource index table
    int numberOfEntries;
    {
      strzid packageName;
      strzid type;
      strzid name;
      int id;
      int size;
    }*(numberOfEntries);

    // string table (gzipped)
    int numberOfStrings;
    {
      utf8 string;
    }*(numberOfStrings);


     */

    private static final int TYPE_STRING = 1;
    private static final int TYPE_FILE = 2;

    private final Path resCachePath;
    private final StringTable stringTable = new StringTable();
    private final Resources resources = new Resources();
    private final ResourceIndex resourceIndex = new ResourceIndex(resources);

    public SaveContext(Path resCachePath) {
      this.resCachePath = resCachePath;
    }

    public void add(final ResName resName, Integer resId, final Collection<TypedResource> variants) {
      resources.add(new Resource(resName, resId, variants));
    }

    public void save(String packageName, int packageIdentifier) throws IOException {
      StrPtr packageNameStrPtr = stringTable.getPtr(packageName);

      stringTable.optimize();

      try (RandomAccessFile f = new RandomAccessFile(resCachePath.toFile(), "rw")) {
        f.setLength(0);
        writeHeader(f, resources.size(), 0, 0);
        f.writeInt(packageNameStrPtr.index);
        f.writeInt(packageIdentifier);
        resources.write(f);

        long resourceIndexOffset = f.getFilePointer();
        resourceIndex.write(f);

        long stringsOffset = f.getFilePointer();
        stringTable.write(f);

        writeHeader(f, resources.size(), resourceIndexOffset, stringsOffset);
      }
    }

    private void writeHeader(RandomAccessFile f, int resourceCount, long resourceIndexOffset, long stringsOffset) throws IOException {
      f.seek(0);
      f.writeInt(-1); // file identifier
      f.writeInt(0); // reserved
      f.writeInt(resourceCount); // numberOfResources
      f.writeLong(resourceIndexOffset); // offset to resource index data
      f.writeLong(stringsOffset); // offset to string data
    }

    private void writeStr(DataOutput out, StrPtr strPtr) throws IOException {
      int index = strPtr.index;
      if (index == -1) {
        throw new IllegalStateException();
      }
      writeZInt(out, index);
    }

    private void writeZInt(DataOutput out, int i) throws IOException {
      writeBits(out, i, 28);
      writeBits(out, i, 21);
      writeBits(out, i, 14);
      writeBits(out, i, 7);
      writeBits(out, i, 0);
    }

    private void writeBits(DataOutput out, int i, int bitOffset) throws IOException {
      if (bitOffset > 0) {
        i = 0x7f & (i >>> bitOffset);
        if (i != 0) {
          out.writeByte(0x80 | i);
        }
      } else {
        out.writeByte(0x7f & i);
      }
    }

    private class Resources {
      private final List<Resource> res = new ArrayList<>();

      public void write(RandomAccessFile out) throws IOException {
        for (Resource resource : res) {
          long filePointer = out.getFilePointer();
          resource.write(out);
          resource.size = (int) (out.getFilePointer() - filePointer);
        }
      }

      public void add(Resource resource) {
        res.add(resource);
      }

      public int size() {
        return res.size();
      }
    }

    private class Resource {
      private final StrPtr packageName;
      private final StrPtr type;
      private final StrPtr name;
      private final int id;
      private final List<Variant> variants;
      int size;

      public Resource(ResName resName, int id, Collection<TypedResource> variants) {
        this.packageName = stringTable.getPtr(resName.packageName);
        this.type = stringTable.getPtr(resName.type);
        this.name = stringTable.getPtr(resName.name);
        this.id = id;
        this.variants = new ArrayList<>(variants.size());
        for (TypedResource typedResource : variants) {
          this.variants.add(new Variant(typedResource));
        }
      }

      public void write(RandomAccessFile out) throws IOException {
        List<byte[]> variantByteses = new ArrayList<>();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream valueOut = new DataOutputStream(baos);

        // write qualifier count, followed by qualifier string and size of payload...
//        System.out.println("Writing " + id + " at position " + out.getFilePointer());

        out.writeShort(variants.size()); // numberOfVariants
        for (Variant variant : variants) {
          baos.reset();
          variant.write(valueOut);
          byte[] b = baos.toByteArray();
          variantByteses.add(b);

          writeStr(out, variant.qualifiers);
          writeZInt(out, b.length);
        }

        // write payloads...
        for (byte[] resourceBytes : variantByteses) {
          out.write(resourceBytes);
        }
      }
    }

    private class Variant {
      private final StrPtr resType;
      private final StrPtr xmlFile;
      private final int line;
      private final DataType dataType;
      private final StrPtr stringData;
      private final Serializable otherData;
      private final StrPtr qualifiers;

      private Variant(TypedResource typedResource) {
        this.resType = stringTable.getPtr(typedResource.getResType().name());
        XmlContext xmlContext = typedResource.getXmlContext();
        this.xmlFile = stringTable.getPtr(xmlContext.getXmlFile().getPath());
        this.line = -1;
        Object data = typedResource.getData();
        if (typedResource.isFile()) {
          this.dataType = DataType.FILE;
          this.stringData = stringTable.getPtr(typedResource.getFsFile().getPath());
          this.otherData = null;
        } else if (data instanceof String) {
          this.dataType = DataType.STRING;
          this.stringData = stringTable.getPtr((String) data);
          this.otherData = null;
        } else if (data instanceof List && ((List) data).size() > 0 && ((List) data).get(0) instanceof TypedResource) {
          this.dataType = DataType.LIST_OF_TYPED_RESOURCES;
          this.stringData = null;
          ArrayList<Serializable> items = new ArrayList<>();
          for (TypedResource item : (List<TypedResource>) data) {
            items.add((Serializable) item.getData());
          }
          this.otherData = items;
        } else {
          this.dataType = DataType.SERIALIZABLE;
          this.stringData = null;
          this.otherData = (Serializable) data;
        }
        this.qualifiers = stringTable.getPtr(typedResource.getQualifiers());
      }

      public void write(DataOutputStream out) throws IOException {
        out.write('x');
        out.write('y');

        writeStr(out, resType);
        writeStr(out, xmlFile);
        writeZInt(out, line);
        out.writeByte(dataType.ordinal());

        switch (dataType) {
          case STRING:
          case FILE:
            writeStr(out, stringData);
            break;
          case LIST_OF_TYPED_RESOURCES:
          case SERIALIZABLE:
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            new ObjectOutputStream(baos).writeObject(otherData);
            writeZInt(out, baos.size());
            out.write(baos.toByteArray());
            break;
        }
      }
    }

    private class ResourceIndex {
      private SaveContext.Resources resources;

      public ResourceIndex(SaveContext.Resources resources) {
        this.resources = resources;
      }

      public void write(DataOutput out) throws IOException {
        out.writeInt(resources.size());
        for (SaveContext.Resource resource : resources.res) {
          writeStr(out, resource.packageName);
          writeStr(out, resource.type);
          writeStr(out, resource.name);
          out.writeInt(resource.id);
          out.writeInt(resource.size);
        }
      }
    }
  }

  private class LoadContext {
    private Path resCachePath;

    public LoadContext(Path resCachePath) {
      this.resCachePath = resCachePath;
    }

    public PackageResourceTable load() throws IOException {
      final MappedByteBuffer buf = FileChannel.open(resCachePath)
          .map(FileChannel.MapMode.READ_ONLY, 0, Files.size(resCachePath));

      int magic = buf.getInt();
      final int reserved = buf.getInt();
      int numberOfResources = buf.getInt();
      long resourceIndexOffset = buf.getLong();
      long stringsOffset = buf.getLong();
      final int packageNameStrI = buf.getInt();
      final int packageIdentifier = buf.getInt();

      final long resourcesOffset = buf.position();

      String[] strs;
      // read strings table
      try (RandomAccessFile in = new RandomAccessFile(resCachePath.toFile(), "r")) {
        in.seek(stringsOffset);

        int numberOfStrings = in.readInt();
        strs = new String[numberOfStrings];
        for (int i = 0; i < numberOfStrings; i++) {
          strs[i] = DataInputStream.readUTF(in);
        }
      }

      final String[] stringTable = strs;

      // read resource index
      buf.position((int) resourceIndexOffset);
      final int numberOfEntries = buf.getInt();
      final BiMap<ResName, Integer> resMap = HashBiMap.create(numberOfEntries);
      final Map<Integer, Integer> resPositions = new HashMap<>();
      int ptr = (int) resourcesOffset;

      for (int i = 0; i < numberOfEntries; i++) {
        String packageName = stringTable[readZInt(buf)];
        String type = stringTable[readZInt(buf)];
        String name = stringTable[readZInt(buf)];
        int id = buf.getInt();
        int size = buf.getInt();
        try {
          resMap.put(new ResName(packageName, type, name), id);
        } catch (Exception e) {
          System.err.println("Huh? Failed for " + packageName + "... " + e.getMessage());
        }
        resPositions.put(id, ptr);
        ptr += size;
      }

      return new PackageResourceTable() {
        @Override
        public String getPackageName() {
          return stringTable[packageNameStrI];
        }

        @Override
        public int getPackageIdentifier() {
          return packageIdentifier;
        }

        @Override
        public Integer getResourceId(ResName resName) {
          Integer integer = resMap.get(resName);
          if (integer == null) {
            throw new RuntimeException("unknown resource " + resName);
          }
          return integer;
        }

        @Override
        public ResName getResName(int resourceId) {
          return resMap.inverse().get(resourceId);
        }

        @Override
        public TypedResource getValue(int resId, String qualifiers) {
          Integer resBase = resPositions.get(resId);
          buf.position(resBase);
//          System.out.println("Attempting to read " + resId + " from position " + buf.position());
          short numberOfVariants = buf.getShort();
          String[] qualifierses = new String[numberOfVariants];
          int[] offsets = new int[numberOfVariants];
          int pos = 0;
          for (int i = 0; i < numberOfVariants; i++) {
            qualifierses[i] = stringTable[readZInt(buf)];
            offsets[i] = pos;
            pos += readZInt(buf);
          }

          int variantsBase = buf.position();

          String bestQ = ResBundle.pickBestMatchingQualifier(qualifiers, Arrays.asList(qualifierses));
          int variant = 0;
          for (int i = 0; i < numberOfVariants; i++) {
            if (qualifierses[i].equals(bestQ)) {
              variant = i;
              break;
            }
          }
          buf.position(variantsBase + offsets[variant]);
          return readVariant();
        }

        @NotNull
        private TypedResource readVariant() {
          buf.get(); // 'xy'
          buf.get();
          ResType resType = ResType.valueOf(stringTable[readZInt(buf)]);
          String file = stringTable[readZInt(buf)];
          int line = readZInt(buf);
          XmlContext xmlContext = new XmlContext(stringTable[packageNameStrI], Fs.fileFromPath(file));
          DataType dataType = DataType.values()[buf.get()];
          Object data;
          switch (dataType) {
            case STRING:
              data = stringTable[readZInt(buf)];
              break;
            case FILE:
              data = Fs.fileFromPath(stringTable[readZInt(buf)]);
              break;
            case LIST_OF_TYPED_RESOURCES:
              List sitems = (List) readObject(buf);
              List<TypedResource> items = new ArrayList<>();
              for (Object sitem : sitems) {
                items.add(new TypedResource(sitem, resType, xmlContext));
              }
              data = items;
              break;
            case SERIALIZABLE:
              data = readObject(buf);
              break;
            default:
              throw new IllegalStateException();
          }

          return new TypedResource(data, resType, xmlContext, dataType == DataType.FILE);
        }

        private Object readObject(MappedByteBuffer buf) {
          int size = readZInt(buf);
          byte[] bytes = new byte[size];
          buf.get(bytes);

          Object data;
          try {
            data = new ObjectInputStream(new ByteArrayInputStream(bytes)).readObject();
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
          return data;
        }

        @Override
        public TypedResource getValue(@NotNull ResName resName, String qualifiers) {
          return getValue(getResourceId(resName), qualifiers);
        }

        @Override
        public XmlBlock getXml(ResName resName, String qualifiers) {
          TypedResource typedResource = getValue(resName, qualifiers);
          if (typedResource == null || !typedResource.isXml()) {
            return null;
          } else {
            return XmlBlock.create(typedResource.getFsFile(), resName.packageName);
          }
        }

        @Override
        public InputStream getRawValue(ResName resName, String qualifiers) {
          TypedResource typedResource = getValue(resName, qualifiers);
          FsFile file = typedResource == null ? null : typedResource.getFsFile();
          try {
            return file == null ? null : file.getInputStream();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        }

        @Override
        public InputStream getRawValue(int resId, String qualifiers) {
          return getRawValue(getResName(resId), qualifiers);
        }

        @Override
        public void receive(Visitor visitor) {

        }
      };
    }

    private int readZInt(MappedByteBuffer buf) {
      int acc = 0;
      while (true) {
        byte b = buf.get();
        acc = (acc << 7) | (b & 0x7f);
        if ((b & 0x80) == 0) break;
      }
      return acc;
    }
  }

  static private class StringTable {
    private final Map<String, StrPtr> strs = new HashMap<>();
    private StrPtr[] sortedStrs;

    StrPtr getPtr(String s) {
      StrPtr strPtr = strs.get(s);
      if (strPtr == null) {
        strPtr = new StrPtr(s);
        strs.put(s, strPtr);
      }
      strPtr.count++;
      return strPtr;
    }

    public void write(DataOutput out) throws IOException {
      out.writeInt(sortedStrs.length);

      for (StrPtr sortedStr : sortedStrs) {
        out.writeUTF(sortedStr.s);
      }
    }

    public void optimize() {
      sortedStrs = new StrPtr[strs.size()];
      strs.values().toArray(sortedStrs);
      Arrays.sort(sortedStrs, new Comparator<StrPtr>() {
        @Override
        public int compare(StrPtr o1, StrPtr o2) {
          return o2.count - o1.count;
        }
      });

      for (int i = 0; i < sortedStrs.length; i++) {
        StrPtr sortedStr = sortedStrs[i];
        sortedStr.index = i;
      }
    }
  }

  static class StrPtr {
    final String s;
    int count;
    int index = -1;

    StrPtr(String s) {
      this.s = s;
    }
  }
}
