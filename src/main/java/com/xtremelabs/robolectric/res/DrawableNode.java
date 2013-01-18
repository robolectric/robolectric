package com.xtremelabs.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;

class DrawableNode {
    final @NotNull Document document;
    final @NotNull XmlLoader.XmlContext xmlContext;

    DrawableNode(@NotNull Document document, @NotNull XmlLoader.XmlContext xmlContext) {
        this.document = document;
        this.xmlContext = xmlContext;
    }
}
