package com.xtremelabs.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;

public abstract class DrawableNode {
    static class Xml extends DrawableNode {
        final @NotNull Document document;
        final @NotNull XmlLoader.XmlContext xmlContext;

        Xml(@NotNull Document document, @NotNull XmlLoader.XmlContext xmlContext) {
            this.document = document;
            this.xmlContext = xmlContext;
        }
    }

    static class ImageFile extends DrawableNode {
        final boolean isNinePatch;

        ImageFile(boolean ninePatch) {
            isNinePatch = ninePatch;
        }
    }
}
