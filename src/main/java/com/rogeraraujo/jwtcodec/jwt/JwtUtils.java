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

/**
 * Utility class that provides JSON Web Token-related methods.
 */
public class JwtUtils {
    // Private constructor to prevent instantiation
    private JwtUtils() { }

    /**
     * Translates an exception occurred during an attempt to decode a JSON Web
     * Token. The main motivation for this method is to provide a more
     * descriptive error message when applicable.
     *
     * @param exception To exception to translate; can be null
     *
     * @return The translated exception
     */
    public static Exception translateDecodingException(Exception exception) {
        if (exception == null) {
            return null;
        }

        if (exception instanceof InvalidJWTException) {
            return new RuntimeException(
                "The token is in an invalid format.", exception);
        }

        if (exception instanceof NoneNotAllowedException) {
            return new RuntimeException(
                "The token does not have a signature algorithm.", exception);
        }

        if (exception instanceof InvalidJWTSignatureException) {
            return new RuntimeException(
                "The token signature is invalid.", exception);
        }

        if (exception instanceof JWTExpiredException) {
            return new RuntimeException(
                "The token has already expired.", exception);
        }

        if (exception instanceof MissingSignatureException) {
            return new RuntimeException(
                "The token signature is missing.", exception);
        }

        if (exception instanceof JWTUnavailableForProcessingException) {
            return new RuntimeException(
                "The timestamps of the token make it unavailable for " +
                "processing at this time.", exception);
        }

        return exception;
    }
}
