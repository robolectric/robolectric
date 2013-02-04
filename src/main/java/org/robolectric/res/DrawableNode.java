package org.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;

public abstract class DrawableNode {
    public static class Xml extends DrawableNode {
        public final @NotNull Document document;
        public final @NotNull XmlLoader.XmlContext xmlContext;

        Xml(@NotNull Document document, @NotNull XmlLoader.XmlContext xmlContext) {
            this.document = document;
            this.xmlContext = xmlContext;
        }
    }

    public static class ImageFile extends DrawableNode {
        public final boolean isNinePatch;

        ImageFile(boolean ninePatch) {
            isNinePatch = ninePatch;
        }
    }
}
