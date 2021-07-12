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

import lombok.Getter;

import java.text.Collator;
import java.util.Comparator;

/**
 * This enumeration represents the different types of cryptographic keys that
 * this application can generate. The keys can be used to sign JSON Web Tokens.
 */
public enum KeyGenerationType {
    HMAC_SHA_256("HMAC SHA-256", false),
    HMAC_SHA_384("HMAC SHA-384", false),
    HMAC_SHA_512("HMAC SHA-512", false),
    RSA_2048("RSA 2048-bit", true),
    RSA_3072("RSA 3072-bit", true),
    RSA_4096("RSA 4096-bit", true),
    EC_256("EC 256-bit", true),
    EC_384("EC 384-bit", true),
    EC_521("EC 521-bit", true);

    @Getter
    private final String description;

    @Getter
    private final boolean isAsymmetric;

    KeyGenerationType(String description, boolean isAsymmetric) {
        this.description = description;
        this.isAsymmetric = isAsymmetric;
    }

    @Override
    public String toString() {
        return description;
    }

    /**
     * A Comparator that compares KeyGenerationType instances by their
     * description.
     */
    public static class DescriptionComparator
            implements Comparator<KeyGenerationType> {
        private final Collator collator = Collator.getInstance();

        @Override
        public int compare(KeyGenerationType j1, KeyGenerationType j2) {
            return collator.compare(j1.description, j2.description);
        }
    }
}
