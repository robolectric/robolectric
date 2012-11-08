package com.xtremelabs.robolectric.shadows;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.util.Join;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(BitmapFactory.class)
public class ShadowBitmapFactory {
    private static Map<String, Point> widthAndHeightMap = new HashMap<String, Point>();

    @Implementation
    public static Bitmap decodeResource(Resources res, int id) {
        Bitmap bitmap = create("resource:" + getResourceName(id));
        shadowOf(bitmap).setLoadedFromResourceId(id);
        return bitmap;
    }

    @Implementation
    public static Bitmap decodeResource(Resources res, int id, BitmapFactory.Options options) {
        Bitmap bitmap = create("resource:" + getResourceName(id), options);
        shadowOf(bitmap).setLoadedFromResourceId(id);
        return bitmap;
    }

    private static String getResourceName(int id) {
        return shadowOf(Robolectric.application).getResourceLoader().getNameForId(id);
    }

    @Implementation
    public static Bitmap decodeFile(String pathName) {
        return create("file:" + pathName);
    }

    @Implementation
    public static Bitmap decodeFile(String pathName, BitmapFactory.Options options) {
        return create("file:" + pathName, options);
    }

    @Implementation
    public static Bitmap decodeStream(InputStream is) {
        return decodeStream(is, null, new BitmapFactory.Options());
    }

    @Implementation
    public static Bitmap decodeStream(InputStream is, Rect outPadding, BitmapFactory.Options opts) {
        return create(is.toString().replaceFirst("stream for ", ""), opts);
    }
    
    @Implementation
    public static Bitmap decodeByteArray(byte[] data, int offset, int length) {
    	return decodeByteArray( data, offset, length, new BitmapFactory.Options() );
    }

    @Implementation
    public static Bitmap decodeByteArray(byte[] data, int offset, int length, BitmapFactory.Options opts) {
    	Checksum checksumEngine = new CRC32();
    	checksumEngine.update(data, 0, data.length);
    	return create("byte array, checksum:" + checksumEngine.getValue() + " offset: " + offset + " length: " + data.length, opts );
    }
    
    static Bitmap create(String name) {
        return create(name, new BitmapFactory.Options());
    }

    public static Bitmap create(String name, BitmapFactory.Options options) {
        Bitmap bitmap = Robolectric.newInstanceOf(Bitmap.class);
        ShadowBitmap shadowBitmap = shadowOf(bitmap);
        shadowBitmap.appendDescription("Bitmap for " + name);

        String optionsString = stringify(options);
        if (optionsString.length() > 0) {
            shadowBitmap.appendDescription(" with options ");
            shadowBitmap.appendDescription(optionsString);
        }

        Point widthAndHeight = widthAndHeightMap.get(name);
        if (widthAndHeight == null) {
            widthAndHeight = new Point(100, 100);
        }

        Point p = new Point(widthAndHeight);
        if (options != null && options.inSampleSize > 1) {
        	p.x = p.x / options.inSampleSize;
        	p.y = p.y / options.inSampleSize;
        	
        	p.x = p.x == 0 ? 1 : p.x;
        	p.y = p.y == 0 ? 1 : p.y;
        }
        
        shadowBitmap.setWidth(p.x);
        shadowBitmap.setHeight(p.y);
        if (options != null) {
            options.outWidth = p.x;
            options.outHeight = p.y;
        }
        return bitmap;
    }

    public static void provideWidthAndHeightHints(Uri uri, int width, int height) {
        widthAndHeightMap.put(uri.toString(), new Point(width, height));
    }

    public static void provideWidthAndHeightHints(int resourceId, int width, int height) {
        widthAndHeightMap.put("resource:" + getResourceName(resourceId), new Point(width, height));
    }

    public static void provideWidthAndHeightHints(String file, int width, int height) {
        widthAndHeightMap.put("file:" + file, new Point(width, height));
    }

    private static String stringify(BitmapFactory.Options options) {
        if (options == null) return "";
        List<String> opts = new ArrayList<String>();

        if (options.inJustDecodeBounds) opts.add("inJustDecodeBounds");
        if (options.inSampleSize > 1) opts.add("inSampleSize=" + options.inSampleSize);

        return Join.join(", ", opts);
    }

    public static void reset() {
        widthAndHeightMap.clear();
    }
}
