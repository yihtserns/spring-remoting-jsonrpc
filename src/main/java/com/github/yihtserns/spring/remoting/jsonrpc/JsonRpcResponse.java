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
import lombok.ToString;

import javax.annotation.Nullable;
import java.util.Optional;

@Getter
public class JsonRpcResponse {

    private final String jsonrpc = "2.0";
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private final Optional<String> id;
    private final Object result;
    private final Error error;

    private JsonRpcResponse(@SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<String> id,
                            Object result) {
        this.id = id;
        this.result = result;
        this.error = null;
    }

    private JsonRpcResponse(@SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<String> id,
                            Error error) {
        this.id = id;
        this.result = null;
        this.error = error;
    }

    /**
     * @return {@code null} if the given Request is a
     * <a href="https://www.jsonrpc.org/specification#notification">Notification</a>.
     */
    @Nullable
    public static JsonRpcResponse success(Object result, JsonRpcRequest<?> request) {
        if (request.isNotification()) {
            return null;
        }
        return new JsonRpcResponse(request.getId(), result);
    }

    /**
     * @return {@code null} if the given Request is a
     * <a href="https://www.jsonrpc.org/specification#notification">Notification</a>.
     */
    @Nullable
    public static JsonRpcResponse failure(Error error, @Nullable JsonRpcRequest<?> request) {
        if (request == null) { // Failed when trying to read the Request
            // Sending back an 'id' field with 'null' value because https://www.jsonrpc.org/specification#response_object says:
            // > If there was an error in detecting the id in the Request object (e.g. Parse error/Invalid Request), it MUST be Null.
            return new JsonRpcResponse(Optional.empty(), error);
        }
        if (request.isNotification()) {
            if (error.alwaysRespond) {
                // Sending back an 'id' field with 'null' value because https://www.jsonrpc.org/specification#response_object says:
                // > If there was an error in detecting the id in the Request object (e.g. Parse error/Invalid Request), it MUST be Null.
                return new JsonRpcResponse(Optional.empty(), error);
            }
            return null;
        }
        return new JsonRpcResponse(request.getId(), error);
    }

    @ToString
    public static class Error {

        @Getter
        private final int code;
        @Getter
        private final String message;
        @Nullable
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
            return new Error(-32601, "Method not found", false);
        }

        public static Error invalidParams() {
            return new Error(-32602, "Invalid params", false);
        }

        public static Error internalError() {
            return new Error(-32603, "Internal error", false);
        }

        public static Error invalidRequest() {
            return new Error(-32600, "Invalid Request", true);
        }

        public static Error parseError() {
            return new Error(-32700, "Parse error", true);
        }
    }
}
