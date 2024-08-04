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

import lombok.Getter;
import lombok.Setter;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@Getter
@Setter
public class JsonRpcRequest<P> {

    private String jsonrpc;
    private Id id = Id.absent();
    private String method;
    private P params;

    public static class Id {

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private final Optional<String> value;

        private Id(@SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<String> value) {
            this.value = value;
        }

        public <T> T map(Function<String, T> valueMapper,
                         Supplier<T> nullValueMapper,
                         Supplier<T> absentValueMapper) {

            //noinspection OptionalAssignedToNull
            if (value == null) {
                return absentValueMapper.get();
            }
            return value.map(valueMapper).orElseGet(nullValueMapper);
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

        public static Id nullValue() {
            return new Id(Optional.empty());
        }

        public static Id absent() {
            return new Id(null);
        }
    }
}
