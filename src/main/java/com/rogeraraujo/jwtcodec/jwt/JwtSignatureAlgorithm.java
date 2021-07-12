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
 * This enumeration represents the different signature algorithms that this
 * application can apply to a JSON Web Token.
 */
public enum JwtSignatureAlgorithm {
    HMAC_SHA_256("HS256", false),
    HMAC_SHA_384("HS384", false),
    HMAC_SHA_512("HS512", false),
    RSA_SSA_PKCS_V1_5_SHA_256("RS256", true),
    RSA_SSA_PKCS_V1_5_SHA_384("RS384", true),
    RSA_SSA_PKCS_V1_5_SHA_512("RS512", true),
    ECDSA_SHA_256("ES256", true),
    ECDSA_SHA_384("ES384", true),
    ECDSA_SHA_512("ES512", true),
    RSA_SSA_PSS_SHA_256("PS256", true),
    RSA_SSA_PSS_SHA_384("PS384", true),
    RSA_SSA_PSS_SHA_512("PS512", true);

    @Getter
    private final String shortName;

    @Getter
    private final boolean isAsymmetric;

    JwtSignatureAlgorithm(String shortName, boolean isAsymmetric) {
        this.shortName = shortName;
        this.isAsymmetric = isAsymmetric;
    }

    @Override
    public String toString() {
        return shortName;
    }

    /**
     * A Comparator that compares instances of JwtSignatureAlgorithm by their
     * short names.
     */
    public static class ShortNameComparator
            implements Comparator<JwtSignatureAlgorithm> {
        private final Collator collator = Collator.getInstance();

        @Override
        public int compare(JwtSignatureAlgorithm j1, JwtSignatureAlgorithm j2) {
            return collator.compare(j1.shortName, j2.shortName);
        }
    }
}
