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

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to exercise the functionality of the Utils class.
 */
@Slf4j
class UtilsTest {
    @Test
    void convertDateToZonedDateTimeTest() {
        assertNull(Utils.convertDateToZonedDateTime(null, null));

        ZonedDateTime zdt = ZonedDateTime.now();
        Date date = Date.from(zdt.toInstant());
        ZonedDateTime zdtConv = Utils.convertDateToZonedDateTime(
            date, ZoneId.of("UTC")).withZoneSameInstant(zdt.getZone());

        DateTimeFormatter formatter = DateTimeFormatter
            .ofPattern("yyyyMMdd HHmmssSSS");
        String strZdtNow = zdt.format(formatter);
        String strZdtNowConv = zdtConv.format(formatter);

        assertEquals(strZdtNow, strZdtNowConv);
        assertEquals(zdt.getZone(), zdtConv.getZone());
    }

    @Test
    void stringIsEmptyTest() {
        assertTrue(Utils.stringIsEmpty(null));
        assertTrue(Utils.stringIsEmpty(""));
        assertFalse(Utils.stringIsEmpty(" "));
        assertFalse(Utils.stringIsEmpty("a"));
    }

    @Test
    void stringIsEmptyOrBlankTest() {
        assertTrue(Utils.stringIsEmptyOrBlank(null));
        assertTrue(Utils.stringIsEmptyOrBlank(""));
        assertTrue(Utils.stringIsEmptyOrBlank(" "));
        assertTrue(Utils.stringIsEmptyOrBlank("  "));
        assertFalse(Utils.stringIsEmptyOrBlank("a"));
    }

    @Test
    void stringContainsDigitsOnlyTest() {
        assertFalse(Utils.stringContainsDigitsOnly(null));
        assertFalse(Utils.stringContainsDigitsOnly(""));
        assertFalse(Utils.stringContainsDigitsOnly(" "));
        assertTrue(Utils.stringContainsDigitsOnly(" 1 "));
        assertFalse(Utils.stringContainsDigitsOnly(" +1 "));
        assertFalse(Utils.stringContainsDigitsOnly(" -1 "));
    }

    @Test
    void objectToStrTest() {
        assertNull(Utils.objectToStr(null, null));
        assertEquals(Utils.objectToStr(null, ""), "");
        assertEquals(Utils.objectToStr(null, "a"), "a");
        assertEquals(Utils.objectToStr(1L, null), "1");
    }

    @Test
    void formatZonedDateTimeTest() {
        assertNull(Utils.formatZonedDateTime(null, null, null));

        String DATE_FORMAT_PATTERN = "yyyyMMdd HHmmssSSS";

        ZonedDateTime zdt = ZonedDateTime.now();
        assertEquals(Utils.formatZonedDateTime(zdt, null, DATE_FORMAT_PATTERN),
            zdt.format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN)));

        ZoneId utcZoneId = ZoneId.of("UTC");
        ZonedDateTime zdtUtc = zdt.withZoneSameInstant(utcZoneId);
        int offsetDiff = zdt.getOffset().getTotalSeconds() -
            zdtUtc.getOffset().getTotalSeconds();
        assertEquals(Utils.formatZonedDateTime(
                zdt.plusSeconds(offsetDiff), utcZoneId, DATE_FORMAT_PATTERN),
            zdt.format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN)));
    }

    @Test
    void safeEqualsTest() {
        assertTrue(Utils.safeEquals(null, null));
        assertFalse(Utils.safeEquals(1L, null));
        assertFalse(Utils.safeEquals(null, 1L));
        assertTrue(Utils.safeEquals(1L, 1L));
    }

    @Test
    void readTextFromResourceTest() throws IOException {
        Charset charset = StandardCharsets.UTF_8;

        assertNull(Utils.readTextFromResource(
            null, charset));
        assertEquals(Utils.readTextFromResource(
            "text/empty.txt", charset), "");
        assertEquals(Utils.readTextFromResource(
            "text/one-line.txt", charset), "Line 1");
        assertEquals(Utils.readTextFromResource(
            "text/two-lines.txt", charset), "\nLine 2");
        assertEquals(Utils.readTextFromResource(
            "text/three-lines.txt", charset), "\n\nLine 3");
        assertEquals(Utils.readTextFromResource(
                "text/last-line-empty.txt", charset),
            "The next (and last) line is empty\n");
    }

    @Test
    void stringToIntTest() {
        assertNull(Utils.stringToInt(null));
        assertNull(Utils.stringToInt(""));
        assertNull(Utils.stringToInt(" "));
        assertNull(Utils.stringToInt("a"));
        assertNull(Utils.stringToInt("1 -1"));
        assertNull(Utils.stringToInt(" 1 -1 "));
        assertNull(Utils.stringToInt("+1 -1"));
        assertNull(Utils.stringToInt(" +1 -1 "));

        assertEquals(Utils.stringToInt("1"), 1);
        assertEquals(Utils.stringToInt(" 1 "), 1);
        assertEquals(Utils.stringToInt("+1"), +1);
        assertEquals(Utils.stringToInt(" +1 "), +1);
        assertEquals(Utils.stringToInt("-1"), -1);
        assertEquals(Utils.stringToInt(" -1 "), -1);
    }
}
