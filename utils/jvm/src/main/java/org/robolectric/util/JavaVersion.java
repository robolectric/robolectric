package org.robolectric.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class JavaVersion implements Comparable<JavaVersion> {

    /** Running Java version as an int value (8, 9, etc.) */
    public static final int SYSTEM = parseJavaVersion(System.getProperty("java.version"));

    private final List<Integer> versions;

    public JavaVersion(String version) {
        versions = new ArrayList<>();
        Scanner s = new Scanner(version).useDelimiter("[^\\d]+");
        while (s.hasNext()) {
            versions.add(s.nextInt());
        }
    }

    @Override public int compareTo(JavaVersion o) {
        List<Integer> versions2 = o.versions;
        int max = Math.min(versions.size(), versions2.size());
        for (int i = 0; i < max; i++) {
            int compare = versions.get(i).compareTo(versions2.get(i));
            if (compare != 0) {
                return compare;
            }
        }

        // Assume longer is newer
        return Integer.compare(versions.size(), versions2.size());
    }

    /**
     * Returns the Java version as an int value.
     *
     * @return the Java version as an int value (8, 9, etc.)
     */
    private static int parseJavaVersion(String version) {
        assert version != null;
        if (version.startsWith("1.")) {
            version = version.substring(2);
        }
        // Allow these formats:
        // 1.8.0_72-ea
        // 9-ea
        // 9
        // 9.0.1
        int dotPos = version.indexOf('.');
        int dashPos = version.indexOf('-');
        return Integer.parseInt(
                version.substring(0, dotPos > -1 ? dotPos : dashPos > -1 ? dashPos : version.length()));
    }
}
