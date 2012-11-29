package com.xtremelabs.robolectric.shadows;

import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.Html.TagHandler;
import android.text.Spanned;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(Html.class)
public class ShadowHtml {

    @Implementation
    public static Spanned fromHtml(String source) {
		if (source == null) {
			/*
			 * Mimic the behavior of the real fromHtml() method. It uses a
			 * StringReader that throws a NullPointerException when a null
			 * string is passed in.
			 */
			throw new NullPointerException();
		}
        return new SpannedThatActsLikeString(source);
    }

    private static String extractSourceFromImgTag(String imgTag) {
        final String SRC_TAG = "src=\"";
        int start = imgTag.indexOf("src=\"");
        int end = imgTag.indexOf("\"", start + SRC_TAG.length());

        String source = imgTag.substring(start + SRC_TAG.length(), end);
        if (!source.endsWith("/")) {
            source += "/";
        }
        return source;
    }

    private static List<Drawable> latestHtmlDrawables = new ArrayList<Drawable>();
    public static List<Drawable> getLatestHtmlDrawables() {
        return latestHtmlDrawables;
    }

    @Implementation
    public static Spanned fromHtml(String source, ImageGetter imageGetter, TagHandler tagHandler) {
        latestHtmlDrawables.clear();
        final String IMG_START = "<img";
        final String IMG_END = ">";
        while(source.contains(IMG_START)) {
            int start = source.indexOf(IMG_START);
            int end = source.indexOf(IMG_END, start + IMG_START.length());

            String imgTag = source.substring(start, end + 1);
            source = source.substring(0, start) + source.substring(end + 1);

            String imgSrc = extractSourceFromImgTag(imgTag);
            Drawable d = imageGetter.getDrawable(imgSrc);
            latestHtmlDrawables.add(d);
        }
        return fromHtml(source);
    }

    private static class SpannedThatActsLikeString implements Spanned {
        String source;

        private SpannedThatActsLikeString(String source) {
            this.source = source;
        }

        @Override
        public <T> T[] getSpans(int start, int end, Class<T> type) {
            return null;
        }

        @Override
        public int getSpanStart(Object tag) {
            return 0;
        }

        @Override
        public int getSpanEnd(Object tag) {
            return 0;
        }

        @Override
        public int getSpanFlags(Object tag) {
            return 0;
        }

        @Override
        public int nextSpanTransition(int start, int limit, Class type) {
            return 0;
        }

        @Override
        public int length() {
            return source.length();
        }

        @Override
        public char charAt(int i) {
            return source.charAt(i);
        }

        @Override
        public CharSequence subSequence(int i, int i1) {
            return null;
        }

        @Override
        public String toString() {
            return source;
        }

        @Override
        public boolean equals(Object o) {
            return source.equals(o);
        }

        @Override
        public int hashCode() {
            return source != null ? source.hashCode() : 0;
        }
    }
}
