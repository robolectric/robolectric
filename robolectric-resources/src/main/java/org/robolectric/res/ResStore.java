package org.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.robolectric.res.builder.XmlBlock;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResStore {
  public PackageResourceTable load(Path resCachePath) throws IOException {
    return new LoadContext(resCachePath).load();
  }

  public void save(final PackageResourceTable packageResourceTable, Path resCachePath) throws IOException {
    final SaveContext saveContext = new SaveContext(resCachePath);
    packageResourceTable.receive(new ResourceTable.Visitor() {
      @Override
      public void visit(ResName key, Collection<TypedResource> values) {
        Integer resourceId = packageResourceTable.getResourceId(key);
        saveContext.add(key, resourceId, values);
      }
    });
    saveContext.save();
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

    // resource table
    {
      // one resource
      short numberOfVariants;
      {
        strid qualifiers;
        zint length;
      }*(numberOfVariants);

      {
        byte type;
        byte[] payload;
      }*(numberOfVariants);
    }*(numberOfEntries);

    // resource index table
    int numberOfEntries;
    {
      strid name;
      int id;
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
    private final ResourceNames resourceIndex = new ResourceNames(resources);

    public SaveContext(Path resCachePath) {
      this.resCachePath = resCachePath;
    }

    public void add(final ResName resName, Integer resId, final Collection<TypedResource> variants) {
      resources.add(new Resource(resName, resId, variants));
    }

    public void save() throws IOException {
      try (RandomAccessFile f = new RandomAccessFile(resCachePath.toFile(), "rw")) {
        f.setLength(0);
        writeHeader(f, resources.size(), 0, 0);
        resources.write(f);

        long resourceIndexOffset = f.getFilePointer();
        resourceIndex.write(f);

        long stringsOffset = f.getFilePointer();
        stringTable.write(f);

        writeHeader(f, resources.size(), resourceIndexOffset, stringsOffset);
      }
    }

    private void variantToBytes(TypedResource typedResource, DataOutput out) throws IOException {
      Object data = typedResource.getData();
      if (typedResource.isFile()) {
        out.writeByte(TYPE_FILE);
      } else if (data instanceof String) {
        out.writeByte(TYPE_STRING);
        out.writeUTF((String) data);
      } else {
        // todo
      }
    }

    private void writeHeader(RandomAccessFile f, int resourceCount, long resourceIndexOffset, long stringsOffset) throws IOException {
      f.seek(0);
      f.writeInt(-1); // file identifier
      f.writeInt(resourceCount);
      f.writeLong(resourceIndexOffset); // offset to resource index data
      f.writeLong(stringsOffset); // offset to string data
    }

    private class ResourceNames implements Block {
      private Resources resources;

      public ResourceNames(Resources resources) {
        this.resources = resources;
      }

      @Override
      public void write(DataOutput out) throws IOException {
        out.writeInt(resources.size());
        for (Resource resource : resources.res) {
          writeStr(out, resource.name);
          out.writeInt(resource.id);
        }
      }
    }

    private void writeStr(DataOutput out, StrPtr strPtr) throws IOException {
      int index = strPtr.index;
      if (index == -1) {
        throw new IllegalStateException();
      }
      writeZInt(out, index);
    }

    private void writeZInt(DataOutput out, int i) throws IOException {
      out.writeInt(i);
    }

    private class Resources implements Block {
      private final List<Resource> res = new ArrayList<>();

      @Override
      public void write(DataOutput out) throws IOException {
        out.writeInt(res.size());
        for (Resource resource : res) {
          resource.write(out);
        }
      }

      public void add(Resource resource) {

      }

      public int size() {
        return res.size();
      }
    }

    private class Resource implements Block {
      private final StrPtr name;
      private final int id;
      private final List<Variant> variants;
      private int size = -1;

      public Resource(ResName resName, int id, Collection<TypedResource> variants) {
        this.name = stringTable.getPtr(resName.getFullyQualifiedName());
        this.id = id;
        this.variants = new ArrayList<>(variants.size());
        for (TypedResource variant : variants) {
          this.variants.add(new Variant(variant, stringTable.getPtr(variant.getQualifiers())));
        }
      }

      @Override
      public void write(DataOutput out) throws IOException {
        List<byte[]> variantByteses = new ArrayList<>();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream valueOut = new DataOutputStream(baos);

        // write qualifier count, followed by qualifier string and size of payload...
        out.writeInt(variants.size());
        for (TypedResource value : variants) {
          baos.reset();
          variantToBytes(value, valueOut);
          byte[] b = baos.toByteArray();
          variantByteses.add(b);

          writeStr(value.getQualifiers());
          out.writeInt(b.length);
        }

        // write payloads...
        for (byte[] resourceBytes : variantByteses) {
          out.write(resourceBytes);
        }
      }
    }
  }

  private static class Variant {
    private final StrPtr qualifiers;
    private final TypedResource typedResource;
    private int size = -1;

    private Variant(StrPtr qualifiers) {
      this.qualifiers = qualifiers;
      this.typedResource = typedResource;
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

      buf.getInt();

      return new PackageResourceTable() {
        @Override
        public String getPackageName() {
          return null;
        }

        @Override
        public int getPackageIdentifier() {
          return 0;
        }

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

        }
      };
    }
  }

  interface Block {
    void write(DataOutput out) throws IOException;
  }

  static private class StringTable {
    private final Map<String, StrPtr> strs = new HashMap<>();

    StrPtr getPtr(String s) {
      StrPtr strPtr = strs.get(s);
      if (strPtr == null) {
        strPtr = new StrPtr(s);
        strs.put(s, strPtr);
      }
      strPtr.count++;
      return strPtr;
    }
  }

  static class StrPtr {
    private final String s;
    private int count;
    private int index = -1;

    public StrPtr(String s) {
      this.s = s;
    }
  }
}
