package com.xtremelabs.robolectric.util;

import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropertiesHelper {

    public static String doSingleSubstitution(String originalValue, Properties properties) {
        if (originalValue == null) {
            return null;
        }

        Pattern variablePattern = Pattern.compile("([^$]*)\\$\\{(.*?)\\}(.*)");

        String expandedValue = originalValue;
        Matcher variableMatcher = variablePattern.matcher(expandedValue);
        while (variableMatcher.matches()) {
            String propertyName = variableMatcher.group(2);
            String propertyValue = null;
            if (properties != null) {
                propertyValue = properties.getProperty(propertyName);
            }
            if (propertyValue == null) {
                propertyValue = System.getProperty(propertyName);
            }
            if (propertyValue == null) {
                return originalValue;
            }

            String sdkPathStart = variableMatcher.group(1);
            String sdkPathEnd = variableMatcher.group(3);
            expandedValue = sdkPathStart + propertyValue + sdkPathEnd;
            variableMatcher = variablePattern.matcher(expandedValue);
        }

        return expandedValue;
    }

    public static void doSubstitutions(Properties properties) {
        Enumeration<?> propertyNames = properties.propertyNames();
        while (propertyNames.hasMoreElements()) {
            String propertyName = (String) propertyNames.nextElement();
            String propertyValue = properties.getProperty(propertyName);
            String expandedPropertyValue = doSingleSubstitution(propertyValue, properties);
            properties.setProperty(propertyName, expandedPropertyValue);
        }
    }
}
