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

@Getter
@Setter
public class JsonRpcResponse {

    private String jsonrpc = "2.0";
    private String id;
    private Object result;
    private Error error;

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
    }
}
