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

package com.rogeraraujo.jwtcodec.jwt;

import io.fusionauth.jwt.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class to exercise the functionality of the JwtUtils class.
 */
@Slf4j
class JwtUtilsTest {
    @Test
    void translateDecodingExceptionTest() {
        assertNull(JwtUtils.translateDecodingException(null));

        Exception ex1 = new InvalidJWTException("");
        assertSame(JwtUtils.translateDecodingException(ex1).getCause(), ex1);

        Exception ex2 = new NoneNotAllowedException();
        assertSame(JwtUtils.translateDecodingException(ex2).getCause(), ex2);

        Exception ex3 = new InvalidJWTSignatureException();
        assertSame(JwtUtils.translateDecodingException(ex3).getCause(), ex3);

        Exception ex4 = new JWTExpiredException();
        assertSame(JwtUtils.translateDecodingException(ex4).getCause(), ex4);

        Exception ex5 = new MissingSignatureException("");
        assertSame(JwtUtils.translateDecodingException(ex5).getCause(), ex5);

        Exception ex6 = new JWTUnavailableForProcessingException();
        assertSame(JwtUtils.translateDecodingException(ex6).getCause(), ex6);

        Exception ex7 = new MissingSignatureException("");
        assertSame(JwtUtils.translateDecodingException(ex7).getCause(), ex7);

        Exception ex8 = new IllegalArgumentException("");
        assertSame(JwtUtils.translateDecodingException(ex8), ex8);
    }
}
