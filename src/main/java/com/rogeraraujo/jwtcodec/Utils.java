/*
 * Copyright (c) 2021, Roger Araújo, All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */

package com.rogeraraujo.jwtcodec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class to provide miscellaneous methods.
 */
public class Utils {
    // Private constructor to prevent instantiation
    private Utils() { }

    /**
     * Converts a java.util.Date instance to a ZonedDateTime instance.
     *
     * @param date java.util.Date instance to convert; can be null
     * @param zoneId Zone to apply to the ZonedDateTime instance; can be null.
     *               If it is null, the default zone of the system will be
     *               applied instead
     *
     * @return The newly-created ZonedDateTime instance
     */
    public static ZonedDateTime convertDateToZonedDateTime(
            java.util.Date date, ZoneId zoneId) {
        if (date == null) {
            return null;
        }

        return ZonedDateTime.ofInstant(date.toInstant(),
            (zoneId != null) ? zoneId : ZoneId.systemDefault());
    }

    /**
     * Checks whether a String is empty. Null Strings are considered empty.
     *
     * @param str String to check; can be null
     *
     * @return Boolean indicating whether the String is empty
     */
    public static boolean stringIsEmpty(String str) {
        return (str == null) || str.isEmpty();
    }

    /**
     * Checks whether a String is empty or blank. Null Strings are considered
     * empty.
     *
     * @param str String to check; can be null
     *
     * @return Boolean indicating whether the String is empty or blank
     */
    public static boolean stringIsEmptyOrBlank(String str) {
        return (str == null) || str.isEmpty() || (str.trim().length() < 1);
    }

    /**
     * Checks whether a String contains digits only. Blank spaces in the
     * beginning or the end of the String are ignored.
     *
     * @param str String to check; can be null
     *
     * @return Boolean indicating whether the String contains digits only
     */
    public static boolean stringContainsDigitsOnly(String str) {
        if (str == null) {
            return false;
        }

        str = str.trim();
        int digitCount = 0;

        for (int i = 0, len = str.length(); i < len; ++i) {
            if (Character.isDigit(str.charAt(i))) {
                ++digitCount;
            }
            else {
                return false;
            }
        }

        return (digitCount > 0);
    }

    /**
     * Converts an Object to its String representation, allowing the use of a
     * default value in case the Object is null.
     *
     * @param obj Object to convert; can be null
     * @param defaultStr Default value to return in case the Object to convert
     *                   is null
     *
     * @return String representation of the object
     */
    public static String objectToStr(Object obj, String defaultStr) {
        return (obj != null) ? obj.toString() : defaultStr;
    }

    /**
     * Formats an instance of ZonedDateTime according to a specific date format
     * pattern.
     *
     * @param zdt Instance of ZonedDateTime to format; can be null
     * @param newZoneId Zone ID to apply to the ZonedDateTime instance
     * @param dateFormatPattern Date format pattern to use; can be null. In
     *                          case it is null, the ISO date format pattern
     *                          will be used instead
     *
     * @return The formatted value of the ZonedDateTime instance
     */
    public static String formatZonedDateTime(
            ZonedDateTime zdt, ZoneId newZoneId, String dateFormatPattern) {
        if (zdt == null) {
            return null;
        }

        DateTimeFormatter formatter = (dateFormatPattern != null) ?
            DateTimeFormatter.ofPattern(dateFormatPattern) :
            DateTimeFormatter.ISO_DATE_TIME;

        return (newZoneId != null) ?
            zdt.withZoneSameInstant(newZoneId).format(formatter) :
            zdt.format(formatter);
    }

    /**
     * Safely compares two Objects. Two null Objects are always considered
     * equal, and two non-null Objects are considered equal if the equals()
     * method of the first Object reports that it is equal to the second
     * Object. A non-null Object is never considered equal to a null Object.
     *
     * @param obj1 First Object to compare
     * @param obj2 Second Object to compare
     *
     * @return Boolean indicating whether the two Objects are equal
     */
    public static boolean safeEquals(Object obj1, Object obj2) {
        if (obj1 == obj2) {
            return true;
        }

        return ((obj1 != null) && (obj2 != null)) ?
            obj1.equals(obj2) : false;
    }

    /**
     * Reads and returns the complete contents of a plain text resource file.
     *
     * @param resourceName Name of the resource file; can be null
     * @param charset Character set of the resource file
     *
     * @return Complete contents of the resource file
     *
     * @throws IOException If an error occurs when reading the resource file
     */
    public static String readTextFromResource(
            String resourceName, Charset charset) throws IOException {
        if (resourceName == null) {
            return null;
        }

        InputStream inpStream = null;
        InputStreamReader inpStreamReader = null;
        BufferedReader bufReader = null;
        StringBuilder result = new StringBuilder();

        try {
            // Tries to open the resource
            inpStream = Utils.class.getClassLoader()
                .getResourceAsStream(resourceName);

            if (inpStream == null) {
                return null;
            }

            // Reads the resource line by line
            inpStreamReader = (charset == null) ?
                new InputStreamReader(inpStream) :
                new InputStreamReader(inpStream, charset);
            bufReader = new BufferedReader(inpStreamReader);

            String lineStr = bufReader.readLine();
            int lineNum = 0;

            while (lineStr != null) {
                if (lineNum > 0) {
                    result.append('\n');
                }

                ++lineNum;
                result.append(lineStr);
                lineStr = bufReader.readLine();
            }
        } finally {
            // Frees resources
            if (bufReader != null) {
                try { bufReader.close(); }
                catch (Exception ignored) { }
            }

            if (inpStreamReader != null) {
                try { inpStreamReader.close(); }
                catch (Exception ignored) { }
            }

            if (inpStream != null) {
                try { inpStream.close(); }
                catch (Exception ignored) { }
            }
        }

        return result.toString();
    }

    /**
     * Converts a String to Integer, returning null in case the conversion
     * fails. Blank spaces in the beginning or the end of the String are
     * ignored.
     *
     * @param str String to convert; can be null
     *
     * @return Integer value of the String
     */
    public static Integer stringToInt(String str) {
        if (str == null) {
            return null;
        }

        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
