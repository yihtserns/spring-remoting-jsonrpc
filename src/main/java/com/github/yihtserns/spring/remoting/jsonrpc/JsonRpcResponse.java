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

import com.github.yihtserns.spring.remoting.jsonrpc.util.Either;
import com.github.yihtserns.spring.remoting.jsonrpc.util.ThrowableFunction;
import com.github.yihtserns.spring.remoting.jsonrpc.util.ThrowableSupplier;
import lombok.Getter;
import lombok.ToString;

import javax.annotation.Nullable;

@Getter
public class JsonRpcResponse {

    private final String jsonrpc = "2.0";
    private final Id id;
    private final Either<Object, Error> result;

    private JsonRpcResponse(Id id, Object result) {
        this.id = id;
        this.result = Either.left(result);
    }

    private JsonRpcResponse(Id id, Error error) {
        this.id = id;
        this.result = Either.right(error);
    }

    /**
     * @return {@code null} if the given Request is a
     * <a href="https://www.jsonrpc.org/specification#notification">Notification</a>.
     */
    @Nullable
    static JsonRpcResponse success(Object result, JsonRpcRequest<?> request) {
        return request.getId().map(
                stringId -> new JsonRpcResponse(Id.valueOf(stringId), result),
                numberId -> new JsonRpcResponse(Id.valueOf(numberId), result),
                () -> new JsonRpcResponse(Id.nullValue(), result),
                () -> null);
    }

    /**
     * @return {@code null} if the given Request is a
     * <a href="https://www.jsonrpc.org/specification#notification">Notification</a>.
     */
    @Nullable
    static JsonRpcResponse failure(Error error, @Nullable JsonRpcRequest<?> request) {
        if (request == null) { // Failed when trying to read the Request
            // Sending back an 'id' field with 'null' value because https://www.jsonrpc.org/specification#response_object says:
            // > If there was an error in detecting the id in the Request object (e.g. Parse error/Invalid Request), it MUST be Null.
            return new JsonRpcResponse(Id.nullValue(), error);
        }
        return request.getId().map(
                stringId -> new JsonRpcResponse(Id.valueOf(stringId), error),
                numberId -> new JsonRpcResponse(Id.valueOf(numberId), error),
                () -> new JsonRpcResponse(Id.nullValue(), error),
                () -> {
                    if (error.alwaysRespond) {
                        // Sending back an 'id' field with 'null' value because https://www.jsonrpc.org/specification#response_object says:
                        // > If there was an error in detecting the id in the Request object (e.g. Parse error/Invalid Request), it MUST be Null.
                        return new JsonRpcResponse(Id.nullValue(), error);
                    }
                    return null;
                });
    }

    public static class Id {

        private final Object value;

        private Id(Object value) {
            this.value = value;
        }

        public <T, E extends Exception> T map(ThrowableFunction<String, T, E> stringValueMapper,
                                              ThrowableFunction<Integer, T, E> numberValueMapper,
                                              ThrowableSupplier<T, E> nullValueMapper) throws E {

            if (value == null) {
                return nullValueMapper.get();
            }
            if (value instanceof String) {
                return stringValueMapper.apply((String) value);
            }
            if (value instanceof Integer) {
                return numberValueMapper.apply((Integer) value);
            }
            throw new UnsupportedOperationException("Unhandled value type: " + value);
        }

        /**
         * @see #nullValue()
         */
        public static Id valueOf(String value) {
            if (value == null) {
                throw new IllegalArgumentException("'value' cannot be null!");
            }
            return new Id(value);
        }

        public static Id valueOf(int value) {
            return new Id(value);
        }

        public static Id nullValue() {
            return new Id(null);
        }
    }

    @ToString
    public static class Error {

        @Getter
        private final int code;
        @Getter
        private final String message;
        @Nullable
        @Getter
        private final Object data;

        private boolean alwaysRespond = false;

        public Error(int code, String message, Object data) {
            this.code = code;
            this.message = message;
            this.data = data;
        }

        public Error(int code, String message) {
            this(code, message, null);
        }

        private Error(int code, String message, boolean alwaysRespond) {
            this(code, message, null);
            this.alwaysRespond = alwaysRespond;
        }

        public static Error methodNotFound() {
            return new Error(-32601, "Method not found");
        }

        public static Error invalidParams() {
            return new Error(-32602, "Invalid params");
        }

        public static Error internalError() {
            return new Error(-32603, "Internal error");
        }

        public static Error invalidRequest() {
            return new Error(-32600, "Invalid Request", true);
        }

        public static Error parseError() {
            return new Error(-32700, "Parse error", true);
        }
    }
}
