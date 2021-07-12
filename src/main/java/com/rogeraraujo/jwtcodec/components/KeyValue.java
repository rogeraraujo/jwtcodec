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

import com.rogeraraujo.jwtcodec.Utils;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * Stores a key and a value together.
 *
 * @param <K> Type of the key
 * @param <V> Type of the value
 */
public class KeyValue<K, V> {
    @Getter @Setter
    private K key;

    @Getter @Setter
    private V value;

    public KeyValue(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (!(obj instanceof KeyValue)) {
            return false;
        }

        KeyValue<?, ?> anotherObj = (KeyValue<?, ?>) obj;

        return Utils.safeEquals(this.key, anotherObj.key);
    }

    @Override
    public int hashCode() {
        return (key != null) ?
            Objects.hash(this.key) : Objects.hash(this);
    }

    @Override
    public String toString() {
        return "key=" + Utils.objectToStr(this.key, "[null]");
    }
}
