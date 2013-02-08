package org.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.robolectric.res.ResourceIndex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResName {
    private static final Pattern FQN_PATTERN = Pattern.compile("^([^:]*):([^/]+)/(.+)$");
    private static final int NAMESPACE = 1;
    private static final int TYPE = 2;
    private static final int NAME = 3;

    public final @NotNull String namespace;
    public final @NotNull String type;
    public final @NotNull String name;

    public ResName(@NotNull String namespace, @NotNull String type, @NotNull String name) {
        this.name = name;
        this.namespace = namespace;
        this.type = type;
    }

    public ResName(@NotNull String fullyQualifiedName) {
        Matcher matcher = FQN_PATTERN.matcher(fullyQualifiedName);
        if (!matcher.find()) {
            throw new IllegalStateException("\"" + fullyQualifiedName + "\" is not fully qualified");
        }
        namespace = matcher.group(NAMESPACE);
        type = matcher.group(TYPE);
        name = matcher.group(NAME);

        if (namespace.equals("xmlns")) throw new IllegalStateException("\"" + fullyQualifiedName + "\" unexpected");
    }

    public static @NotNull String qualifyResourceName(String possiblyQualifiedResourceName, String contextPackageName) {
        if (possiblyQualifiedResourceName.contains(":")) {
            return possiblyQualifiedResourceName;
        } else {
            return contextPackageName + ":" + possiblyQualifiedResourceName;
        }
    }

    public static Integer getResourceId(ResourceIndex resourceIndex, String possiblyQualifiedResourceName, String contextPackageName) {
        if (possiblyQualifiedResourceName == null ) {
            return null;
        }

        if (possiblyQualifiedResourceName.equals("@null")) {
            return 0;
        }

        String fullyQualifiedResourceName = qualifyResourceName(possiblyQualifiedResourceName, contextPackageName);

        fullyQualifiedResourceName = fullyQualifiedResourceName.replaceAll("[@+]", "");
        Integer resourceId = resourceIndex.getResourceId(new ResName(fullyQualifiedResourceName));
        // todo warn if resourceId is null
        return resourceId;
    }

    public ResName qualify(String string) {
        return new ResName(qualifyResourceName(string.replace("@", ""), namespace));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResName resName = (ResName) o;

        if (!namespace.equals(resName.namespace)) return false;
        if (!type.equals(resName.type)) return false;
        if (!name.equals(resName.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = namespace.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ResName{" + getFullyQualifiedName() + "}";
    }

    public String getFullyQualifiedName() {
        return namespace + ":" + type + "/" + name;
    }
}
