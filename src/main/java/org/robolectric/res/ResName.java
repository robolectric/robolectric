package org.robolectric.res;

import org.jetbrains.annotations.NotNull;

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

    public static @NotNull String qualifyResourceName(@NotNull String possiblyQualifiedResourceName, String defaultPackageName, String defaultType) {
        ResName resName = qualifyResName(possiblyQualifiedResourceName, defaultPackageName, defaultType);
        return resName.getFullyQualifiedName();
    }

    public static @NotNull ResName qualifyResName(@NotNull String possiblyQualifiedResourceName, String defaultPackageName, String defaultType) {
        int indexOfColon = possiblyQualifiedResourceName.indexOf(':');
        int indexOfSlash = possiblyQualifiedResourceName.indexOf('/');
        String packageName = indexOfColon == -1 ? null : possiblyQualifiedResourceName.substring(0, indexOfColon);
        String type = indexOfSlash == -1 ? null : possiblyQualifiedResourceName.substring(indexOfColon == -1 ? 0 : indexOfColon + 1, indexOfSlash);
        int indexBeforeName = indexOfColon > indexOfSlash ? indexOfColon : indexOfSlash;

        return new ResName(packageName == null ? defaultPackageName : packageName,
                type == null ? defaultType : type,
                possiblyQualifiedResourceName.substring(indexBeforeName + 1));
    }

    public static Integer getResourceId(ResourceIndex resourceIndex, String possiblyQualifiedResourceName, String contextPackageName) {
        if (possiblyQualifiedResourceName == null ) {
            return null;
        }

        if (possiblyQualifiedResourceName.equals("@null")) {
            return 0;
        }

        String fullyQualifiedResourceName = qualifyResourceName(possiblyQualifiedResourceName, contextPackageName, null);

        fullyQualifiedResourceName = fullyQualifiedResourceName.replaceAll("[@+]", "");
        Integer resourceId = resourceIndex.getResourceId(new ResName(fullyQualifiedResourceName));
        // todo warn if resourceId is null
        return resourceId;
    }

    public ResName qualify(String string) {
        return new ResName(qualifyResourceName(string.replace("@", ""), namespace, null));
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

    public ResName withPackageName(String packageName) {
        if (packageName.equals(namespace)) return this;
        return new ResName(packageName, type, name);
    }
}
