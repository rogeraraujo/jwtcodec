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

package com.rogeraraujo.jwtcodec.components;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test class to exercise the functionality of the KeyValue class.
 */
@Slf4j
public class KeyValueTest {
    @Test
    void equalsTest() {
        KeyValue<String, String> kv1 = new KeyValue<>("x", "y");
        Assertions.assertNotEquals(kv1, null);

        KeyValue<String, String> kv2 = new KeyValue<>("x", "y");
        Assertions.assertEquals(kv1, kv2);

        KeyValue<String, String> kv3 = new KeyValue<>("y", "z");
        Assertions.assertNotEquals(kv1, kv3);

        KeyValue<Long, Long> kv4 = new KeyValue<>(1L, 2L);
        Assertions.assertNotEquals(kv1, kv4);
    }
}
