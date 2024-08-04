/*
 * Copyright 2022 yihtserns.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.yihtserns.spring.remoting.jsonrpc;

import com.github.yihtserns.spring.remoting.jsonrpc.util.ThrowableFunction;
import com.github.yihtserns.spring.remoting.jsonrpc.util.ThrowableSupplier;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
public class JsonRpcRequest<P> {

    private String jsonrpc;
    private Id id = Id.absent();
    private String method;
    private P params;

    public static class Id {

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private final Optional<Object> value;

        private Id(@SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<Object> value) {
            this.value = value;
        }

        public <T, E extends Exception> T map(ThrowableFunction<String, T, E> stringValueMapper,
                                              ThrowableFunction<Integer, T, E> numberValueMapper,
                                              ThrowableSupplier<T, E> nullValueMapper,
                                              ThrowableSupplier<T, E> absentValueMapper) throws E {

            //noinspection OptionalAssignedToNull
            if (value == null) {
                return absentValueMapper.get();
            }
            if (!value.isPresent()) {
                return nullValueMapper.get();
            }
            Object v = value.get();
            if (v instanceof String) {
                return stringValueMapper.apply((String) v);
            }
            if (v instanceof Integer) {
                return numberValueMapper.apply((Integer) v);
            }
            throw new UnsupportedOperationException("Unhandled value type: " + v);
        }

        /**
         * @see #nullValue()
         */
        public static Id valueOf(String value) {
            if (value == null) {
                throw new IllegalArgumentException("'value' must not be null!");
            }
            return new Id(Optional.of(value));
        }

        public static Id valueOf(int value) {
            return new Id(Optional.of(value));
        }

        public static Id nullValue() {
            return new Id(Optional.empty());
        }

        public static Id absent() {
            return new Id(null);
        }
    }
}
