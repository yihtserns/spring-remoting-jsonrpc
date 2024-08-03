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

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@Getter
@Setter
public class JsonRpcResponse {

    private static final Set<Integer> PRE_EXECUTION_ERROR_CODES = new HashSet<>(Arrays.asList(
            Error.parseError().getCode(),
            Error.invalidRequest().getCode()));

    private String jsonrpc = "2.0";
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<String> id;
    private Object result;
    private Error error;

    public boolean hasPreExecutionErrorCode() {
        return error != null && PRE_EXECUTION_ERROR_CODES.contains(error.getCode());
    }

    @JsonInclude(Include.NON_NULL)
    @Getter
    public static class Error {

        private int code;
        private String message;
        @Setter
        private Object data;

        public Error(int code, String message) {
            this.code = code;
            this.message = message;
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
            return new Error(-32600, "Invalid Request");
        }

        public static Error parseError() {
            return new Error(-32700, "Parse error");
        }
    }
}
